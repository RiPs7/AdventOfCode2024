package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Day11 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return solve(input, 25);
  }

  @Override
  public Long part2(String input) {
    return solve(input, 75);
  }

  /**
   * Solves the problem from the input with the given number of steps
   *
   * @param input the input of stones
   * @param steps the number of steps
   * @return the solution
   */
  private long solve(final String input, final int steps) {
    return Arrays.stream(input.split("\\s+"))
      .map(Long::parseLong)
      .map(stone -> count(stone, steps))
      .reduce(Long::sum)
      .orElseThrow();
  }

  /**
   * Counts how many stones the given {@code stone} will generate after {@code steps} number of steps
   *
   * @param stone the initial stone
   * @param steps the number of steps
   * @return the number of stones that will be generated
   */
  private long count(final long stone, final int steps) {
    return count(stone, steps, new HashMap<>());
  }

  /**
   * Main implementation of stone generation logic using a cache for memoization
   *
   * @param stone the initial stone
   * @param steps the number of steps
   * @return the number of stones that will be generated
   */
  private long count(final long stone, final int steps, final Map<Pair<Long, Integer>, Long> cache) {
    // If we have cached the stone-steps combination, return the result from the cache
    final Pair<Long, Integer> key = Pair.of(stone, steps);
    if (cache.containsKey(key)) {
      return cache.get(key);
    }

    // Base case: If no more steps are left, we return a single stone (the initial one)
    if (steps == 0) {
      return 1;
    }

    // Rule 1: If the stone is engraved with the number 0, it is replaced by a stone engraved with the number 1
    if (stone == 0) {
      // Recursively calculate new stones with one less step, and cache the result
      return cacheResult(key, count(1L, steps - 1, cache), cache);
    }

    // Rule 2: If the stone is engraved with a number that has an even number of digits, it is replaced by two stones
    final String stoneStr = String.valueOf(stone);
    if (stoneStr.length() % 2 == 0) {
      final String leftStoneStr = stoneStr.substring(0, stoneStr.length() / 2);
      final String rightStoneStr = stoneStr.substring(stoneStr.length() / 2);
      final long leftStone = Long.parseLong(leftStoneStr);
      final long rightStone = Long.parseLong(rightStoneStr);
      // Recursively calculate new stones for each case with one less step, and cache the result
      return cacheResult(key, count(leftStone, steps - 1, cache) + count(rightStone, steps - 1, cache), cache);
    }

    // Rule 3: If none of the other rules apply, the stone is replaced by a new stone; the old stone's number multiplied by 2024 is engraved on the new stone.
    // Recursively calculate new stones with one less step, and cache the result
    return cacheResult(key, count(stone * 2024, steps - 1, cache), cache);
  }

  /**
   * Adds a new cache entry and returns the result
   *
   * @param key   the key for the new cache entry
   * @param value the value for the new cache entry
   * @param cache the cache to add the entry to
   * @return the value from the entry that was cached
   */
  private long cacheResult(final Pair<Long, Integer> key, final long value, final Map<Pair<Long, Integer>, Long> cache) {
    cache.put(key, value);
    return value;
  }
}
