package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day25 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final Pair<List<Lock>, List<Key>> locksAndKeys = Arrays.stream(input.split("\n\n"))
      .map(block -> Arrays.stream(block.split("\n")).toList())
      .collect(Collectors.teeing(
        Collectors.filtering(Lock::check, Collectors.mapping(Lock::parse, Collectors.toList())),
        Collectors.filtering(Key::check, Collectors.mapping(Key::parse, Collectors.toList())),
        Pair::of));

    final List<Lock> locks = locksAndKeys.left();
    final List<Key> keys = locksAndKeys.right();

    return locks.stream()
      .map(lock -> keys.stream()
        .filter(lock::fits)
        .count())
      .reduce(Long::sum)
      .orElseThrow();
  }

  @Override
  public Long part2(String input) {
    return 0L;
  }

  private record Lock(int size, int[] heights) {

    public static boolean check(final List<String> lines) {
      return lines.getFirst().chars().allMatch(c -> c == '#');
    }

    public static Lock parse(final List<String> lines) {
      final int[] heights = IntStream.range(0, lines.getFirst().length())
        .map(c -> (int) lines.stream().filter(line -> line.charAt(c) == '#').count() - 1)
        .toArray();
      return new Lock(lines.size() - 2, heights);
    }

    public boolean fits(final Key key) {
      return IntStream.range(0, heights.length)
        .allMatch(i -> heights[i] + key.heights[i] <= size);
    }

  }

  private record Key(int[] heights) {

    public static boolean check(final List<String> lines) {
      return lines.getLast().chars().allMatch(c -> c == '#');
    }

    public static Key parse(final List<String> lines) {
      final int[] heights = IntStream.range(0, lines.getFirst().length())
        .map(c -> (int) lines.stream().filter(line -> line.charAt(c) == '#').count() - 1)
        .toArray();
      return new Key(heights);
    }

  }

}
