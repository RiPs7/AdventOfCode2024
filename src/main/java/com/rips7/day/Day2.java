package com.rips7.day;

import com.rips7.util.Util;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day2 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return parseLines(input)
        .filter(this::checkSafe)
        .count();
  }

  @Override
  public Long part2(String input) {
    return parseLines(input)
        .filter(this::checkSafeWithDampener)
        .count();
  }

  private Stream<Long[]> parseLines(final String input) {
    return Util.lines(input)
        .map(line -> line.split("\\s+"))
        .map(parts -> Arrays.stream(parts).map(Long::parseLong).toArray(Long[]::new));
  }

  private boolean checkSafe(final Long[] levels) {
    // Determine behaviour from first two levels
    if (levels[0].equals(levels[1])) {
      return false;
    }
    final boolean increasing = levels[0] < levels[1];

    for (int i = 0; i < levels.length - 1; i++) {
      // If two levels are the same, it's not safe
      if (levels[i].equals(levels[i + 1])) {
        return false;
      }
      // If two levels are increasing, but behaviour is not increasing, it's not safe
      if (levels[i] < levels[i + 1] && !increasing) {
        return false;
      }
      // If two levels are decreasing, but behaviour is increasing, it's not safe
      if (levels[i] > levels[i + 1] && increasing) {
        return false;
      }
      // If two levels differ by more than 3, it's not safe
      if (Math.abs(levels[i] - levels[i + 1]) > 3) {
        return false;
      }
    }
    // Otherwise, it's safe
    return true;
  }

  private boolean checkSafeWithDampener(final Long[] levels) {
    // Determine behaviour from first two levels
    if (levels[0].equals(levels[1])) {
      return dampenUnsafe(levels);
    }
    final boolean increasing = levels[0] < levels[1];

    for (int i = 0; i < levels.length - 1; i++) {
      // If two levels are the same, it's not safe
      if (levels[i].equals(levels[i + 1])) {
        return dampenUnsafe(levels);
      }
      // If two levels are increasing, but behaviour is not increasing, it's not safe
      if (levels[i] < levels[i + 1] && !increasing) {
        return dampenUnsafe(levels);
      }
      // If two levels are decreasing, but behaviour is increasing, it's not safe
      if (levels[i] > levels[i + 1] && increasing) {
        return dampenUnsafe(levels);
      }
      // If two levels differ by more than 3, it's not safe
      if (Math.abs(levels[i] - levels[i + 1]) > 3) {
        return dampenUnsafe(levels);
      }
    }
    // Otherwise, it's safe
    return true;
  }

  private boolean dampenUnsafe(final Long[] levels) {
    // Try to dampen each level and, if at any point, the new levels are safe, return true
    return IntStream.range(0, levels.length)
        .anyMatch(i -> checkSafe(dampenLevel(levels, i)));
  }

  private Long[] dampenLevel(final Long[] levels, final int index) {
    final Long[] newLevels = new Long[levels.length - 1];
    System.arraycopy(levels, 0, newLevels, 0, index);
    System.arraycopy(levels, index + 1, newLevels, index, levels.length - index - 1);
    return newLevels;
  }

}
