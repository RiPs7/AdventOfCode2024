package com.rips7.day;

import com.rips7.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Day9 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return DiskMap.parse(Util.lines(input).findFirst().orElseThrow())
      .compactFileBlocks();
  }

  @Override
  public Long part2(String input) {
    return DiskMap.parse(Util.lines(input).findFirst().orElseThrow())
      .compactFiles();
  }

  /**
   * Models the disk map
   *
   * @param map a list of integers for the layout (-1 means empty space)
   */
  private record DiskMap(List<Integer> map) {

    /**
     * Parses the given input into a {@link DiskMap}
     *
     * @param input the input
     * @return the parsed {@link DiskMap}
     */
    private static DiskMap parse(final String input) {
      final List<Integer> map = new ArrayList<>();
      // Toggle this flag to indicate if it's a free space or a file
      final AtomicBoolean free = new AtomicBoolean(false);
      // Keep track of the file id
      final AtomicInteger fileId = new AtomicInteger();
      input.chars()
        .mapToObj(c -> (char) c)
        .map(String::valueOf)
        .map(Integer::parseInt)
        .forEach(digit -> {
          if (free.get()) { // Populate empty values in the map equal in number to the digit
            for (int i = 0; i < digit; i++) {
              map.add(-1);
            }
          } else { // Populate file values in the map equal in number to the digit
            for (int i = 0; i < digit; i++) {
              map.add(fileId.get());
            }
            fileId.incrementAndGet();
          }
          // Toggle the flag
          free.set(!free.get());
        });
      return new DiskMap(map);
    }

    /**
     * Compacts the disk map by moving file blocks one at a time from the end of the disk to the leftmost available
     * space, and returns the checksum
     *
     * @return the checksum
     */
    private long compactFileBlocks() {
      // Get the first empty space
      Empty empty = findEmpty(0);
      // Loop through the disk map backwards
      for (int i = map.size() - 1; i >= 0 && empty != null; i--) {
        // Get the current block
        final int currentBlock = map.get(i);
        // If it's an empty space, skip
        if (currentBlock == -1) {
          continue;
        }
        // Copy the current block into the empty space
        map.set(empty.start, currentBlock);
        // Convert the original block into an empty space
        map.set(i, -1);
        // Get the next empty space
        empty = findEmpty(empty.start + 1);
      }

      // Return the checksum
      return checksum();
    }

    /**
     * Compacts the disk map by moving entire files one at a time from the end of the disk to the leftmost available
     * space, and returns the checksum
     *
     * @return the checksum
     */
    private long compactFiles() {
      // Get the last file
      File file = findFile(map.size() - 1);

      while (file != null) {
        // Get the first empty space
        Empty empty = findEmpty(0);

        // While an empty space is available on the left of the file
        while (empty != null && empty.start < file.start) {
          // If the file fits in the empty space
          if (file.length <= empty.length) {
            // Copy the entire file into the space and convert the original file into an empty space
            for (int e = 0; e < file.length; e++) {
              map.set(empty.start + e, file.id);
              map.set(file.start + e, -1);
            }
            break;
          }
          // Get the next empty space
          empty = findEmpty(empty.start + empty.length);
        }

        // Find the previous file
        file = findFile(file.start - 1);
      }

      // Return the checksum
      return checksum();
    }

    /**
     * Finds the next empty space, starting at the given index, and searching forwards
     *
     * @param start the index to start the search
     * @return the {@link Empty}, or {@code null} if none found
     */
    private Empty findEmpty(final int start) {
      int emptyStartId;
      int emptyEndId;
      int i = start;
      while (map.get(i) != -1) {
        i++;
        if (i == map.size()) {
          return null;
        }
      }
      emptyStartId = i;
      while (map.get(i) == -1) {
        i++;
        if (i == map.size()) {
          return null;
        }
      }
      emptyEndId = i - 1;
      return new Empty(emptyStartId, emptyEndId - emptyStartId + 1);
    }

    /**
     * Finds the next file, starting at the given index, and searching backwards
     *
     * @param start the index to start the search
     * @return the {@link File}, or {@code null} if none found
     */
    private File findFile(final int start) {
      int fileStartId;
      int fileEndId;
      int i = start;
      while (map.get(i) == -1) {
        i--;
        if (i == -1) {
          return null;
        }
      }
      fileEndId = i;
      final int fileId = map.get(fileEndId);
      while (map.get(i) == fileId) {
        i--;
        if (i == -1) {
          return null;
        }
      }
      fileStartId = i + 1;
      return new File(fileId, fileStartId, fileEndId - fileStartId + 1);
    }

    /**
     * Calculates the checksum by multiplying each file block by its position and summing up the result
     *
     * @return the checksum
     */
    private long checksum() {
      return IntStream.range(0, map.size())
        .mapToObj(i -> map.get(i) == -1 ? 0 : i * map.get(i))
        .map(Long::valueOf)
        .reduce(Long::sum)
        .orElseThrow();
    }

    /**
     * Wrapper class for information on a File
     *
     * @param id     the file id
     * @param start  the start index
     * @param length the length
     */
    private record File(int id, int start, int length) { }

    /**
     * Wrapper class for information on a Space
     *
     * @param start  the start index
     * @param length the length
     */
    private record Empty(int start, int length) { }
  }

}
