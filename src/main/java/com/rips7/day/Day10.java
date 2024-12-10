package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Day10 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final TrailMap trailMap = TrailMap.parse(input);
    return (long) trailMap.findHikingTrailsScore();
  }

  @Override
  public Long part2(String input) {
    final TrailMap trailMap = TrailMap.parse(input);
    return (long) trailMap.findHikingTrailsRating();
  }

  /**
   * Models the Trail Map
   *
   * @param heights the 2D array of heights
   */
  private record TrailMap(int[][] heights) {

    /**
     * Parses the given input into a {@link TrailMap}
     *
     * @param input the input
     * @return the parsed {@link TrailMap}
     */
    private static TrailMap parse(final String input) {
      final int[][] heights = Util.lines(input)
        .map(line -> line.chars()
          .map(c -> c - '0')
          .toArray())
        .toArray(int[][]::new);
      return new TrailMap(heights);
    }

    /**
     * Retrieves the height at a given {@link Position}, or {@code -1} if the position is outside the map
     *
     * @param pos the position
     * @return the height
     */
    private int get(final Position pos) {
      return Util.isWithinGrid(pos, heights.length, heights[0].length) ?
        heights[pos.x()][pos.y()] :
        -1;
    }

    /**
     * Basic BFS implementation to find all the reachable 9-heights from all the 0-heights
     *
     * @return the final hiking trail scores
     */
    private int findHikingTrailsScore() {
      int score = 0;
      for (int r = 0; r < heights.length; r++) {
        for (int c = 0; c < heights[r].length; c++) {
          // Skip non-0-heights
          if (heights[r][c] != 0) {
            continue;
          }
          // BFS implementation
          final Queue<Position> frontier = new ArrayDeque<>();
          final Set<Position> closed = new HashSet<>();
          frontier.add(Position.of(r, c));
          while (!frontier.isEmpty()) {
            final Position currentPos = frontier.poll();
            final int currentHeight = this.get(currentPos);
            if (closed.contains(currentPos)) {
              continue;
            }
            if (currentHeight == 9) {
              score++;
            }
            List.of(Offset.UP, Offset.RIGHT, Offset.DOWN, Offset.LEFT).forEach(offset -> {
              final Position neighbor = currentPos.apply(offset);
              if (this.get(neighbor) == currentHeight + 1) {
                frontier.add(neighbor);
              }
            });
            closed.add(currentPos);
          }
        }
      }
      return score;
    }

    /**
     * Basic DFS implementation to find all the trails from 0-heights to 9-heights
     *
     * @return the final hiking trail ratings
     */
    private int findHikingTrailsRating() {
      int ratings = 0;
      for (int r = 0; r < heights.length; r++) {
        for (int c = 0; c < heights[r].length; c++) {
          // Skip non-0-heights
          if (heights[r][c] != 0) {
            continue;
          }
          // DFS implementation (removed 'closedSet' because certain cells will be revisited, to find all paths)
          final Stack<Position> frontier = new Stack<>();
          frontier.add(Position.of(r, c));
          while (!frontier.isEmpty()) {
            final Position currentPos = frontier.pop();
            final int currentHeight = this.get(currentPos);
            if (currentHeight == 9) {
              ratings++;
            }
            List.of(Offset.UP, Offset.RIGHT, Offset.DOWN, Offset.LEFT).forEach(offset -> {
              final Position neighbor = currentPos.apply(offset);
              if (this.get(neighbor) == currentHeight + 1) {
                frontier.add(neighbor);
              }
            });
          }
        }
      }
      return ratings;
    }
  }
}
