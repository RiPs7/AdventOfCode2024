package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day19 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final Pair<List<String>, List<String>> towelsAndPatterns = parseTowelsAndPatterns(input);
    final List<String> towels = towelsAndPatterns.left();
    final List<String> patterns = towelsAndPatterns.right();
    return patterns.stream()
      .filter(pattern -> isPossible(pattern, towels, new HashMap<>()))
      .count();
  }

  @Override
  public Long part2(String input) {
    final Pair<List<String>, List<String>> towelsAndPatterns = parseTowelsAndPatterns(input);
    final List<String> towels = towelsAndPatterns.left();
    final List<String> patterns = towelsAndPatterns.right();
    return patterns.stream()
      .map(pattern -> countPossible(pattern, towels, new HashMap<>()))
      .reduce(Long::sum)
      .orElseThrow();
  }

  /**
   * Parses a list of towels and list of patterns from the given input
   *
   * @param input the input
   * @return a {@link Pair} of a list of towels and a list of patterns
   */
  private Pair<List<String>, List<String>> parseTowelsAndPatterns(final String input) {
    final List<String> lines = input.lines().toList();
    final List<String> towels = Arrays.stream(lines.getFirst().split(", ")).toList();
    final List<String> patterns = lines.subList(2, lines.size());
    return Pair.of(towels, patterns);
  }

  /**
   * Checks if the given pattern can be created with the given towels.
   * This method is memoized with a cache.
   *
   * @param pattern the pattern
   * @param towels  the list of towels
   * @param cache   the cache for memoization
   * @return true if the pattern is possible, false otherwise
   */
  private boolean isPossible(final String pattern, final List<String> towels, final Map<String, Boolean> cache) {
    // Try and retrieve the answer from the cache
    if (cache.containsKey(pattern)) {
      return cache.get(pattern);
    }
    // Base case: An empty pattern is possible
    if (pattern.isBlank()) {
      return cacheAndGet(pattern, true, cache);
    }
    // For each towel that the pattern starts with, we check if the rest of the pattern is possible, using recursion
    return towels.stream()
      .filter(pattern::startsWith)
      .filter(towel -> isPossible(pattern.replaceFirst(towel, ""), towels, cache))
      .findFirst()
      .map(towel -> cacheAndGet(pattern, true, cache))
      .orElse(cacheAndGet(pattern, false, cache));
  }

  /**
   * Counts the possible ways the given pattern can be created using the given towels.
   * This method is memoized with a cache.
   *
   * @param pattern the pattern
   * @param towels  the list of towels
   * @param cache   the cache for memoization
   * @return the number of possible ways the pattern can be created
   */
  private long countPossible(final String pattern, final List<String> towels, final Map<String, Long> cache) {
    // Try and retrieve the answer from the cache
    if (cache.containsKey(pattern)) {
      return cache.get(pattern);
    }
    // Base case: An empty pattern is created in a single way
    if (pattern.isBlank()) {
      return cacheAndGet(pattern, 1L, cache);
    }
    // For each towel that the pattern starts with, we count the number of possible ways that the rest of the pattern is
    // can be created, using recursion
    final long count = towels.stream()
      .filter(pattern::startsWith)
      .map(towel -> countPossible(pattern.replaceFirst(towel, ""), towels, cache))
      .reduce(Long::sum)
      .orElse(0L);
    return cacheAndGet(pattern, count, cache);
  }

  /**
   * Small utility method to add an entry to the cache, and return the value
   *
   * @param key   the entry key
   * @param value the entry value
   * @param cache the cache
   * @return the value that was cached
   * @param <T> the type of the value
   */
  private <T> T cacheAndGet(final String key, final T value, final Map<String, T> cache) {
    cache.put(key, value);
    return value;
  }

}
