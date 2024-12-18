package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Grid;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;
import com.rips7.util.algorithms.pathfinding.BFS;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day18 implements Day<String> {

  private static final int DIM = 71;
  private static final List<Offset> OFFSETS = List.of(Offset.UP, Offset.RIGHT, Offset.DOWN, Offset.LEFT);

  @Override
  public String part1(String input) {
    final List<Position> bytes = parseBytes(input);
    return String.valueOf(getPathSize(bytes, 1024));
  }

  @Override
  public String part2(String input) {
    final List<Position> bytes = parseBytes(input);
    final Position blockingByte = findBlockingByte(bytes);
    return "%s,%s".formatted(blockingByte.y(), blockingByte.x());
  }

  /**
   * Parses the bytes from the given input
   *
   * @param input the input
   * @return a list of the bytes {@link Position}
   */
  private List<Position> parseBytes(final String input) {
    return Util.lines(input)
      .map(l -> Arrays.stream(l.split(","))
        .map(Integer::parseInt)
        .toArray(Integer[]::new))
      .map(pos -> Position.of(pos[1], pos[0]))
      .toList();
  }

  /**
   * Parses the memory space into a {@link Grid} for better handling, from the given bytes {@link Position}s, limited by
   * the given limit
   *
   * @param allBytes the positions for all the bytes
   * @param limit    the limit
   * @return a {@link Grid} of characters that represents the memory space
   */
  private Grid<Character> parseMemory(final List<Position> allBytes, final int limit) {
    final Set<Position> bytes = allBytes.stream()
      .limit(limit)
      .collect(Collectors.toSet());
    final Character[][] memory = IntStream.range(0, DIM)
      .mapToObj(c -> IntStream.range(0, DIM)
        .mapToObj(r -> bytes.contains(Position.of(c, r)) ? '#' : '.')
        .toArray(Character[]::new))
      .toArray(Character[][]::new);
    return Grid.of(memory, '#');
  }

  /**
   * Gets the path size using the BFS implementation
   *
   * @param bytes the list of bytes
   * @param limit the limit
   * @return the path size from start to end, or -1 if no path exists
   */
  private int getPathSize(final List<Position> bytes, final int limit) {
    final Grid<Character> memory = parseMemory(bytes, limit);
    try {
      final List<Position> result = new BFS<Position>().run(
        Position.of(0, 0),
        Position.of(DIM - 1, DIM - 1),
        current -> OFFSETS.stream()
          .map(current::apply)
          .filter(next -> memory.get(next.y(), next.x()) == '.')
          .toList()
      );
      return result.size() - 1;
    } catch (final Exception e) {
      return -1;
    }
  }

  /**
   * Finds the blocking byte position, using a binary search, to narrow down the index of the first byte that blocks
   * the path to the exit
   *
   * @param bytes the list of bytes
   * @return the position of the byte that blocks the path to the exit
   */
  private Position findBlockingByte(final List<Position> bytes) {
    int low = 0;
    int high = bytes.size() - 1;
    while (low < high) {
      int limit = (low + high) / 2;
      if (getPathSize(bytes, limit) != -1) {
        low = limit + 1;
      } else {
        high = limit;
      }
    }
    return bytes.get(low - 1);
  }

}
