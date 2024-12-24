package com.rips7.day;

import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rips7.util.Util.isWithinGrid;
import static com.rips7.util.maths.Combinatorics.product;

public class Day21 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return solve(input, 2);
  }

  @Override
  public Long part2(String input) {
    return solve(input, 25);
  }

  /**
   * Solves the problem for the given input and the parameter of the number of directional keypads
   *
   * @param input      the inut
   * @param dirKeypads the number of direction keypads
   * @return the answer
   */
  private long solve(final String input, final int dirKeypads) {
    final List<String> codes = input.lines().toList();

    final NumKeypad numKeypad = new NumKeypad();
    final DirKeypad dirKeypad = new DirKeypad();

    return codes.stream()
      // For each code, get the sequence options from the numeric keypad
      .map(code -> numKeypad.getSequenceOptions(code).stream()
        // For each option, compute the length of the shortest sequence
        .map(option -> dirKeypad.computeLength(option, dirKeypads, new HashMap<>()))
        .min(Long::compareTo)
        // Calculate complexity
        .map(minOptionLength -> minOptionLength * Long.parseLong(code.replace("A", "")))
        .orElseThrow())
      .reduce(Long::sum)
      .orElseThrow();
  }

  /**
   * Models a numeric keypad like the following:
   * <pre>
   *   +---+---+---+
   *   | 7 | 8 | 9 |
   *   +---+---+---+
   *   | 4 | 5 | 6 |
   *   +---+---+---+
   *   | 1 | 2 | 3 |
   *   +---+---+---+
   *       | 0 | A |
   *       +---+---+
   * </pre>
   */
  private static final class NumKeypad extends Keypad {

    private NumKeypad() {
      super(new Character[][]{
        { '7' , '8', '9' },
        { '4' , '5', '6' },
        { '1' , '2', '3' },
        { null, '0', 'A' },
      });
    }

    /**
     * Gets a list of sequence options to input the give code
     *
     * @param code the code
     * @return the list of sequence options
     */
    private List<String> getSequenceOptions(final String code) {
      // Prepend 'A' because that's where the arm starts at
      final String fullSequence = "A" + code;
      // Construct the sequences to step through the whole code, i.e. 029A => A -> 0, 0 -> 2, 2 -> 9, 9 -> A
      final List<List<String>> sequences = IntStream.range(0, fullSequence.length() - 1)
        .mapToObj(i -> Pair.of(fullSequence.charAt(i), code.charAt(i)))
        .map(pair -> keySequences().get(pair.left()).get(pair.right()))
        .toList();
      // Get the 'cartesian product' of all the subsequences
      return product(sequences, (s1, s2) -> s1 + s2);
    }
  }

  /**
   * Models a directional keypad like the following:
   * <pre>
   *       +---+---+
   *       | ^ | A |
   *   +---+---+---+
   *   | < | v | > |
   *   +---+---+---+
   * </pre>
   */
  private static final class DirKeypad extends Keypad {
    private final Map<Character, Map<Character, Long>> shortestKeySequences;

    private DirKeypad() {
      super(new Character[][] {
        { null, '^', 'A' },
        { '<' , 'v', '>' }
      });
      // Pre-calculate all the shortest sequences to get from each button to all the others
      shortestKeySequences = keySequences().entrySet().stream()
        .collect(Collectors.toMap(
          Entry::getKey,
          e1 -> e1.getValue().entrySet().stream()
            .collect(Collectors.toMap(
              Entry::getKey,
              e2 -> (long) e2.getValue().getFirst().length())
        )));
    }

    /**
     * Computes the final length of the given sequence from the given depth. This is used recursively, so that given a
     * sequence at a specific depth, it will calculate the length of the final sequence after it goes through all the
     * remaining depths
     *
     * @param sequence the sequence
     * @param depth    the current depth
     * @param cache    the cache to improve recursion calculations
     * @return the length of the final sequence
     */
    private long computeLength(final String sequence, final int depth, final Map<Pair<String, Integer>, Long> cache) {
      // Prepend 'A' because that's where the arm starts at
      final String fullSequence = "A" + sequence;
      // Construct the consecutive pairs to step through the whole sequence
      final List<Pair<Character, Character>> pairs = IntStream.range(0, fullSequence.length() - 1)
        .mapToObj(i -> Pair.of(fullSequence.charAt(i), sequence.charAt(i)))
        .toList();
      // The key for the cache
      final Pair<String, Integer> cacheKey = Pair.of(sequence, depth);
      // If we have computed the final length of a certain sequence at a certain depth, we retrieve it from the cache
      if (cache.containsKey(cacheKey)) {
        return cache.get(cacheKey);
      }
      // Base case: Retrieve the shortest distance from the pre-computed shortest key distances
      if (depth == 1) {
        final long result = pairs.stream()
          .map(pair -> shortestKeySequences.get(pair.left()).get(pair.right()))
          .reduce(Long::sum)
          .orElseThrow();
        // Update cache and return the result
        cache.put(cacheKey, result);
        return result;
      }

      final long length = pairs.stream()
        .map(pair -> keySequences().get(pair.left()).get(pair.right()))
        .map(keySequences -> keySequences.stream()
          // Recursively call this function with each consecutive pair from the sequence, and one depth less
          .map(keySequence -> computeLength(keySequence, depth - 1, cache))
          // Find the shortest sequence from the result
          .min(Long::compareTo)
          .orElseThrow())
        // Add up all the shortest sequence lengths
        .reduce(Long::sum)
        .orElseThrow();

      // Update cache and return the result
      cache.put(cacheKey, length);
      return length;
    }
  }

  /**
   * Abstract class for a keypad
   */
  private abstract static class Keypad {
    private final Character[][] keypad;
    private final Map<Character, Map<Character, List<String>>> keySequences;

    private Keypad(final Character[][] keypad) {
      this.keypad = keypad;
      this.keySequences = calculateKeySequences();
    }

    protected Map<Character, Map<Character, List<String>>> keySequences() {
      return keySequences;
    }

    /**
     * Calculate all the shortest sequences to get from each button to all the others
     *
     * @return a map of maps, for going from each button to all the others, and a list of the shortest sequences
     */
    private Map<Character, Map<Character, List<String>>> calculateKeySequences() {
      // Create a map of characters - positions, for each button of the keypad
      final Map<Character, Position> keyPositions = IntStream.range(0, keypad.length).mapToObj(r ->
          IntStream.range(0, keypad[r].length).mapToObj(c -> Position.of(r, c)))
        .flatMap(Function.identity())
        .filter(pos -> keypad[pos.x()][pos.y()] != null)
        .collect(Collectors.toMap(pos -> keypad[pos.x()][pos.y()], Function.identity()));

      // Initialize the key sequences map with an empty map for all buttons
      final Map<Character, Map<Character, List<String>>> keySequences = new HashMap<>();
      keyPositions.keySet().forEach(start -> keySequences.put(start, new HashMap<>()));

      for (final Character start : keyPositions.keySet()) {
        for (final Character end : keyPositions.keySet()) {
          // If we start and end the same button, we only need to press 'A'
          if (start.equals(end)) {
            keySequences.get(start).put(end, new ArrayList<>(List.of("A")));
            continue;
          }
          // Perform a BFS to get the shortest sequences
          final List<String> sequences = new ArrayList<>();
          final Queue<Pair<Position, String>> frontier = new ArrayDeque<>();
          frontier.add(Pair.of(keyPositions.get(start), ""));
          int optimal = Integer.MAX_VALUE;
          while(!frontier.isEmpty()) {
            final Pair<Position, String> current = frontier.poll();
            final Position currentPos = current.left();
            final String currentSeq = current.right();
            // If we are currently checking a suboptimal case, we can break, since in BFS, we process shortest paths first
            if (optimal < currentSeq.length() + 1) {
              break;
            }
            // All the next options, along with their representation
            for (final Pair<Position, String> next : List.of(
                Pair.of(currentPos.apply(Offset.UP), "^"),
                Pair.of(currentPos.apply(Offset.RIGHT), ">"),
                Pair.of(currentPos.apply(Offset.DOWN), "v"),
                Pair.of(currentPos.apply(Offset.LEFT), "<"))) {
              final Position nextPos = next.left();
              final String nextMove = next.right();
              // If it's not within the grid, skip it
              if (!isWithinGrid(nextPos, keypad)) {
                continue;
              }
              final Character nextButton = keypad[nextPos.x()][nextPos.y()];
              // Skip if there is no button in that position
              if (nextButton == null) {
                continue;
              }
              // If we've reached the target button
              if (nextButton == end) {
                // Update the optimal length (add 1 for the final move)
                optimal = currentSeq.length() + 1;
                sequences.add(currentSeq + nextMove + "A");
              } else {
                // Otherwise, just add it to the frontier
                frontier.add(Pair.of(nextPos, currentSeq + nextMove));
              }
            }
          }
          keySequences.get(start).put(end, sequences);
        }
      }
      return keySequences;
    }
  }
}
