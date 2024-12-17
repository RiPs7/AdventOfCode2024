package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Direction;
import com.rips7.util.Util.Grid;
import com.rips7.util.Util.Position;
import com.rips7.util.algorithms.pathfinding.Dijkstra;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class Day16 implements Day<Long> {

  @Override
  public Long part1(String input) {
    // Parse grid, starting position and ending position
    final Grid<Character> grid = Grid.of(Util.grid(input), 'X');
    final Position startPos = grid.find('S');
    final Position endPos = grid.find('E');

    // Start at the starting position, facing right
    final PositionAndDirection start = new PositionAndDirection(startPos, Direction.RIGHT);

    // Use implementation of Dijkstra's algorithm
    final Pair<List<PositionAndDirection>, Double> result = new Dijkstra<PositionAndDirection>().run(
      // The start position
      start,
      // A predicate for whether we've reached the end
      posDir -> endPos.equals(posDir.pos),
      // The neighbor generator given the current position, direction and cost
      (currPosDir, currCost) -> getNeighbors(currPosDir, currCost, grid));

    // Return the accumulated cost
    return result.right().longValue();
  }

  @Override
  public Long part2(String input) {
    // Parse grid, starting position and ending position
    final Grid<Character> grid = Grid.of(Util.grid(input), 'X');
    final Position startPos = grid.find('S');
    final Position endPos = grid.find('E');

    // Start at the starting position, facing right
    final PositionAndDirection start = new PositionAndDirection(startPos, Direction.RIGHT);

    // A tweaked implementation of Dijkstra's algorithm to handle multiple routes with the same cost
    final PriorityQueue<Pair<PositionAndDirection, Double>> frontier = new PriorityQueue<>(Comparator.comparingDouble(Pair::right));
    frontier.add(Pair.of(start, 0.0));
    final Map<PositionAndDirection, Double> lowestCosts = new HashMap<>();
    lowestCosts.put(start, 0.0);
    // Keep track of all the positions / directions that lead to a certain node with the same cost
    final Map<PositionAndDirection, Set<PositionAndDirection>> backtrack = new HashMap<>();
    // Keep a set for end states because we can end up there from multiple directions
    final Set<PositionAndDirection> endStates = new HashSet<>();
    // Initialize best score to the max value
    double bestScore = Double.MAX_VALUE;

    while(!frontier.isEmpty()) {
      final Pair<PositionAndDirection, Double> currentPosAndDirAndCost = frontier.poll();
      final PositionAndDirection currentPosAndDir = currentPosAndDirAndCost.left();
      final Position currentPos = currentPosAndDir.pos();
      final Double currentCost = currentPosAndDirAndCost.right();
      // If we have a worse score for the current position/direction than before
      if (currentCost > lowestCosts.getOrDefault(currentPosAndDir, Double.MAX_VALUE)) {
        continue;
      }
      // Update best score to end state, and add the state to the end states
      if (currentPos.equals(endPos)) {
        if (currentCost > bestScore) {
          break;
        }
        bestScore = currentCost;
        endStates.add(currentPosAndDir);
      }

      getNeighbors(currentPosAndDir, currentCost, grid).forEach((nextPosAndDir, nextCost) -> {
        final double lowest = lowestCosts.getOrDefault(nextPosAndDir, Double.MAX_VALUE);
        // If we've visited the neighbor with a lower score, skip
        if (nextCost > lowest) {
          return;
        }
        // If it's better score, update the backtracking map
        if (nextCost < lowest) {
          backtrack.put(nextPosAndDir, new HashSet<>());
          lowestCosts.put(nextPosAndDir, nextCost);
        }
        backtrack.get(nextPosAndDir).add(currentPosAndDir);
        frontier.add(Pair.of(nextPosAndDir, nextCost));
      });
    }

    // Simple DFS to get all routes back to the start
    final Queue<PositionAndDirection> backtrackFrontier = new ArrayDeque<>(endStates);
    // Keep track of all the positions / directions
    final Set<PositionAndDirection> seen = new HashSet<>(endStates);
    while(!backtrackFrontier.isEmpty()) {
      final PositionAndDirection current = backtrackFrontier.poll();
      for (final PositionAndDirection previous : backtrack.getOrDefault(current, Set.of())) {
        if (seen.contains(previous)) {
          continue;
        }
        seen.add(previous);
        backtrackFrontier.add(previous);
      }
    }

    // Keep only the unique positions
    return (long) seen.stream()
      .map(PositionAndDirection::pos)
      .collect(Collectors.toSet())
      .size();
  }

  /**
   * Gets the neighbors of a given position, direction and cost
   *
   * @param currPosDir the current {@link PositionAndDirection}
   * @param currCost   the current cost
   * @param grid       the {@link Grid}
   * @return a map of neighbors and costs
   */
  private Map<PositionAndDirection, Double> getNeighbors(final PositionAndDirection currPosDir, final Double currCost,
      final Grid<Character> grid) {
    final Position currentPos = currPosDir.pos;
    final Direction currentDir = currPosDir.dir;
    final Position nextPosition = currentPos.apply(currentDir.offset());
    final Character nextCell = grid.get(nextPosition);

    final Map<PositionAndDirection, Double> neighbors = new HashMap<>();
    if (nextCell != '#') {
      // Add a forward step if there is no wall
      neighbors.put(new PositionAndDirection(nextPosition, currentDir), currCost + 1);
    }
    // Add the turns as next steps
    neighbors.put(new PositionAndDirection(currentPos, currentDir.rotate90()), currCost + 1000);
    neighbors.put(new PositionAndDirection(currentPos, currentDir.rotateNeg90()), currCost + 1000);

    return neighbors;
  }

  /**
   * Wrapper around {@link Position} and {@link Direction}
   *
   * @param pos the {@link Position}
   * @param dir the {@link Direction}
   */
  private record PositionAndDirection(Position pos, Direction dir) { }
}
