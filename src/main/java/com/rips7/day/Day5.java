package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.maths.Combinatorics;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day5 implements Day<Long> {

  @Override
  public Long part1(String input) {
    // Parse Rulebook and list of Updates
    final Pair<RuleBook, List<Update>> ruleBookAndUpdates = parseRuleBookAndUpdates(input);

    final RuleBook ruleBook = ruleBookAndUpdates.left();
    final List<Update> updates = ruleBookAndUpdates.right();

    return updates.stream()
        .filter(update -> update.isInRightOrder(ruleBook)) // get the Updates that are in right order
        .map(Update::getMiddlePage) // get the middle pages
        .map(Long::valueOf)
        .reduce(Long::sum) // sum them up
        .orElseThrow();
  }

  @Override
  public Long part2(String input) {
    // Parse Rulebook and list of Updates
    final Pair<RuleBook, List<Update>> ruleBookAndUpdates = parseRuleBookAndUpdates(input);

    final RuleBook ruleBook = ruleBookAndUpdates.left();
    final List<Update> updates = ruleBookAndUpdates.right();

    return updates.stream()
        .filter(update -> !update.isInRightOrder(ruleBook)) // get the Updates that are not in the right order
        .map(update -> update.fix(ruleBook)) // fix them
        .map(Update::getMiddlePage) // get the middle pages
        .map(Long::valueOf)
        .reduce(Long::sum) // sum them up
        .orElseThrow();
  }

  /**
   * Parses the list of rules and the list of updates
   *
   * @param input the input
   * @return a {@link Pair} of a {@link RuleBook} and a list of {@link Update}s
   */
  private Pair<RuleBook, List<Update>> parseRuleBookAndUpdates(final String input) {
    return Util.lines(input)
        .collect(Collectors.teeing(
            Collectors.filtering(Rule::check, Collectors.mapping(Rule::parse, Collectors.toList())),
            Collectors.filtering(Update::check, Collectors.mapping(Update::parse, Collectors.toList())),
            (rules, updates) -> Pair.of(RuleBook.compile(rules), updates)));
  }

  /**
   * Models a Rule
   *
   * @param page1 the first page in order
   * @param page2 the second page in order
   */
  private record Rule(int page1, int page2) {
    private static final Pattern RULE_PATTERN = Pattern.compile("\\d+\\|\\d+");

    /**
     * Checks if the given input matches the {@link Rule} pattern
     *
     * @param input the input
     * @return true if the input matches a {@link Rule}, false otherwise
     */
    private static boolean check(final String input) {
      return RULE_PATTERN.matcher(input).matches();
    }

    /**
     * Parses the input into a {@link Rule}
     *
     * @param input the input
     * @return the {@link Rule}
     */
    private static Rule parse(final String input) {
      final String[] parts = input.split("\\|");
      return new Rule(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
  }

  /**
   * Compiles information on page ordering. This is basically a map keyed by a page, pointing to a set of pages that the
   * former one must come before. So, for an entry such as: <code>1 -> [2, 3, 4]</code> it means that page 1 must appear
   * before page 2, and page 3, and page 4.
   *
   * @param rules the rule
   */
  private record RuleBook(Map<Integer, Set<Integer>> rules) {

    /**
     * Compiles the given rules into a rule book
     *
     * @param rules a list of rules
     * @return a {@link RuleBook}
     */
    private static RuleBook compile(final List<Rule> rules) {
      final Map<Integer, Set<Integer>> mappedRules = rules.stream()
          .collect(Collectors.groupingBy(
              Rule::page1,
              Collectors.mapping(Rule::page2, Collectors.toSet())));
      return new RuleBook(mappedRules);
    }

    /**
     * Checks if the given pages are in the right order
     *
     * @param page1 the first pages
     * @param page2 the second page
     * @return true if the pages are in the right order, false otherwise
     */
    private boolean arePagesCorrect(final Integer page1, final Integer page2) {
      if (rules.containsKey(page1) && rules.get(page1).contains(page2)) {
        // If there is an explicit rule that page1 precedes page2, i.e. page1 -> [..., page2, ...] => true
        return true;
      }
      if (rules.containsKey(page2) && rules.get(page2).contains(page1)) {
        // If there is an explicit rule that page2 precedes page1, i.e. page2 -> [..., page1, ...] => false
        return false;
      }
      // If we don't have information about page1, ignore it => true
      return !rules.containsKey(page1);
    }
  }

  /**
   * Models an Update
   *
   * @param pages the list of pages
   */
  private record Update(List<Integer> pages) {
    private static final Pattern UPDATE_PATTERN_MATCH = Pattern.compile("((\\d+)|(\\d+,)+(\\d+))");

    /**
     * Checks if the given input matches the {@link Update} list pattern
     *
     * @param input the input
     * @return true if the input matches an {@link Update} list, false otherwise
     */
    private static boolean check(final String input) {
      return UPDATE_PATTERN_MATCH.matcher(input).matches();
    }

    /**
     * Parses the input into an {@link Update}
     *
     * @param input the input
     * @return the {@link Update}
     */
    private static Update parse(final String input) {
      return new Update(Arrays.stream(input.split(","))
          .map(Integer::parseInt)
          .toList());
    }

    /**
     * Checks if the list of pages are in the right order
     *
     * @param ruleBook the {@link RuleBook} to check the order of pages against
     * @return true if the list of pages are in the right order, false otherwise
     */
    private boolean isInRightOrder(final RuleBook ruleBook) {
      return Combinatorics.unorderedPairs(pages, true) // create all unordered pairs of pages
          .stream()
          .allMatch(pair -> ruleBook.arePagesCorrect(pair.left(), pair.right())); // check if all of them are in the right order
    }

    /**
     * Gets the middle page of the Update
     *
     * @return the middle page
     */
    private int getMiddlePage() {
      return pages.get(pages.size() / 2);
    }

    /**
     * Fixes an Update
     *
     * @param ruleBook the {@link RuleBook} to check the order of pages against
     * @return the fixed Update where the original pages are in the correct order
     */
    // The comparison does not care about "equal" pages
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private Update fix(final RuleBook ruleBook) {
      final List<Integer> orderedPages = pages.stream()
          .sorted((p1, p2) -> ruleBook.arePagesCorrect(p1, p2) ? 1 : -1)
          .toList();
      return new Update(orderedPages);
    }

  }

}
