package com.rips7.day;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day22 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return input.lines()
      .map(Long::parseLong)
      .map(seed -> {
        // Run the seed secret through the 2000 steps
        long secret = seed;
        for (int i = 0; i < 2000; i++) {
          secret = nextSecret(secret);
        }
        return secret;
      })
      // Sum up all the 2000th secrets
      .reduce(Long::sum)
      .orElseThrow();
  }

  @Override
  public Long part2(String input) {
    final List<Long> seeds = input.lines()
      .map(Long::parseLong)
      .toList();

    // Keeps track of all the totals if we were to sell after a given four-price-changes
    final Map<FourPriceChanges, Long> fourPriceChangesTotals = new HashMap<>();

    // Loop through the seeds / buyers
    for (final long seed : seeds) {
      // Construct all the secrets and keep track of the prices
      long secret = seed;
      final List<Long> prices = new ArrayList<>(List.of(secret % 10));
      for (int i = 0; i < 2000; i++) {
        secret = nextSecret(secret);
        prices.add(secret % 10);
      }
      // Keep track of all the seen four-price-changes
      final Set<FourPriceChanges> seen = new HashSet<>();
      for (int i = 0; i < prices.size() - 4; i++) {
        // Construct all four price changes
        final FourPriceChanges fourPriceChanges = new FourPriceChanges(
          prices.get(i + 1) - prices.get(i),
          prices.get(i + 2) - prices.get(i + 1),
          prices.get(i + 3) - prices.get(i + 2),
          prices.get(i + 4) - prices.get(i + 3));
        // If we've seen this four-price-change, skip it
        if (seen.contains(fourPriceChanges)) {
          continue;
        }
        // Otherwise, add it to the seen set
        seen.add(fourPriceChanges);
        // And merge it to the totals map, i.e. try to associate the current price with the four-price-changes, but if
        // we have seen it before from a previous buyer, add to the sum
        fourPriceChangesTotals.merge(fourPriceChanges, prices.get(i + 4), Long::sum);
      }
    }

    // We want the maximum from totals
    return fourPriceChangesTotals.values().stream()
      .max(Long::compareTo)
      .orElseThrow();
  }

  /**
   * Calculates the next secret
   *
   * @param secret the original secret
   * @return the next secret
   */
  private long nextSecret(final long secret) {
    final long step1 = mixAndPrune(secret << 6, secret);
    final long step2 = mixAndPrune(step1 >> 5, step1);
    return mixAndPrune(step2 << 11, step2);
  }

  /**
   * Mixes a number with the secret, and then prunes
   *
   * @param n      the number
   * @param secret the secret
   * @return the mixed and pruned secret
   */
  private long mixAndPrune(final long n, final long secret) {
    return Math.floorMod(n ^ secret, 16777216);
  }

  /**
   * Wrapper record for four changes
   *
   * @param change1 the first change
   * @param change2 the second change
   * @param change3 the third change
   * @param change4 the fourth change
   */
  private record FourPriceChanges(long change1, long change2, long change3, long change4) {

  }
}
