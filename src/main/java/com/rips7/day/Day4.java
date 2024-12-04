package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;
import com.rips7.util.Util.TriConsumer;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Day4 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final String wordToLookFor = "XMAS";

    final Puzzle puzzle = Puzzle.parse(input);

    final AtomicInteger res = new AtomicInteger();

    puzzle.search((e, r, c) -> {
        // We only consider starting positions that match the initial letter
        if (e != wordToLookFor.charAt(0)) {
          return;
        }
        // Check every possible direction
        for (final Direction dir : Direction.values()) {
          // Create a Position object for easier indexing
          Position pos = Position.of(r, c);
          int letterIndex;
          for (letterIndex = 1; letterIndex < wordToLookFor.length(); letterIndex++) {
            // Move along the direction we are checking
            final Position nextPos = pos.apply(dir.offset);
            // If we end up outside the grid, or the current spot does not contain the letter we are looking for, break
            if (puzzle.get(nextPos) != wordToLookFor.charAt(letterIndex)) {
              break;
            }
            // Update the position
            pos = nextPos;
          }
          // If we have reached the end of the word, we have a match, so increment the counter
          if (letterIndex == wordToLookFor.length()) {
            res.incrementAndGet();
          }
        }
    });

    return res.longValue();
  }

  @Override
  public Long part2(String input) {
    final Set<String> validWords = Set.of("MAS", "SAM");

    final Puzzle puzzle = Puzzle.parse(input);

    final AtomicInteger res = new AtomicInteger();

    puzzle.search((e, r, c) -> {
      // We don't need to check the edges
      if (r == 0 || c == 0 || r == puzzle.height() - 1 || c == puzzle.width() - 1) {
        return;
      }

      // We only consider starting positions that match the middle letter
      if (e != 'A') {
        return;
      }

      // Create a Position object for easier indexing
      final Position pos = Position.of(r, c);

      // Form the diagonal word going from up-right to down-left
      final String upRightDownLeftWord = "%s%s%s"
          .formatted(puzzle.get(pos.apply(Offset.UP_RIGHT)), e, puzzle.get(pos.apply(Offset.DOWN_LEFT)));

      // Form the diagonal word going from up-left to down-right
      final String upLeftDownRightWord = "%s%s%s"
          .formatted(puzzle.get(pos.apply(Offset.LEFT_UP)), e, puzzle.get(pos.apply(Offset.RIGHT_DOWN)));

      // Check if they are valid words, and if so, increment the counter
      if (validWords.contains(upRightDownLeftWord) && validWords.contains(upLeftDownRightWord)) {
        res.incrementAndGet();
      }
    });

    return res.longValue();
  }

  private record Puzzle(Character[][] chars) {
    private static Puzzle parse(final String input) {
      final Character[][] chars = Util.lines(input)
          .map(line -> line.chars()
              .mapToObj(c -> (char) c)
              .toArray(Character[]::new))
          .toArray(Character[][]::new);
      return new Puzzle(chars);
    }

    private void search(final TriConsumer<Character, Integer, Integer> callback) {
      Util.loop2D(chars, callback);
    }

    private char get(final Position pos) {
      return Util.isWithinGrid(pos, chars) ? chars[pos.x()][pos.y()] : '-';
    }

    private int height() {
      return chars.length;
    }

    private int width() {
      return chars[0].length;
    }
  }

  private enum Direction {
    UP(Offset.UP),
    UP_RIGHT(Offset.UP_RIGHT),
    RIGHT(Offset.RIGHT),
    RIGHT_DOWN(Offset.RIGHT_DOWN),
    DOWN(Offset.DOWN),
    DOWN_LEFT(Offset.DOWN_LEFT),
    LEFT(Offset.LEFT),
    LEFT_UP(Offset.LEFT_UP);

    final Offset offset;

    Direction(final Offset offset) {
      this.offset = offset;
    }
  }
}
