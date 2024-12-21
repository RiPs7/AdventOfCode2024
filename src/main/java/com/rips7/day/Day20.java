package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Grid;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day20 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final Grid<Character> grid = parseGrid(input);
    final Long[][] distances = calculateDistances(grid);
    return countCheats(grid, distances, 2, 100);
  }

  @Override
  public Long part2(String input) {
    final Grid<Character> grid = parseGrid(input);
    final Long[][] distances = calculateDistances(grid);
    return countCheats(grid, distances, 20, 100);
  }

  /**
   * Parses the input into a {@link Grid}
   *
   * @param input the input
   * @return the parsed {@link Grid}
   */
  private Grid<Character> parseGrid(final String input) {
    return Grid.of(input.lines()
        .map(line -> line.chars()
          .mapToObj(c -> (char) c)
          .toArray(Character[]::new))
        .toArray(Character[][]::new),
      '#');
  }

  /**
   * Calculates a matrix of distances where each empty cell has its distance from the start, and all the walls
   * have -1
   *
   * @param grid the grid
   * @return the distance matri
   */
  private Long[][] calculateDistances(final Grid<Character> grid) {
    final Position start = grid.find('S');
    final Position end = grid.find('E');
    Position current = start;
    final Long[][] distances = Util.newGeneric2DArray(Long.class, grid.rows(), grid.cols());
    Util.loop2D(grid.rows(), grid.cols(), (r, c) -> distances[r][c] = -1L);
    // Distance to start is 0
    distances[start.x()][start.y()] = 0L;
    // There is a single path from start to end, so we just move one step at a time until we reach the end
    while (!current.equals(end)) {
      for (final Offset offset : List.of(Offset.UP, Offset.RIGHT, Offset.DOWN, Offset.LEFT)) {
        final Position next = current.apply(offset);
        // If it's a wall, skip it
        if (grid.get(next) == '#') {
          continue;
        }
        // If the distance to this cell has been calculated before (i.e. is not -1), skip it
        if (distances[next.x()][next.y()] != -1) {
          continue;
        }
        // Otherwise, the distance to the new cell is the distance to the current one, plus 1
        distances[next.x()][next.y()] = distances[current.x()][current.y()] + 1;
        current = next;
      }
    }
    return distances;
  }

  /**
   * Counts the number of cheats to achieve the target picoseconds
   *
   * @param grid              the grid
   * @param distances         the distances matrix
   * @param maxCheatsAllowed  the maximum number of cheats allowed
   * @param targetPicoseconds the target picoseconds to achieve
   * @return the number of cheats that can achieve the target picoseconds
   */
  @SuppressWarnings("SameParameterValue")
  private long countCheats(final Grid<Character> grid, final Long[][] distances, final int maxCheatsAllowed,
      final int targetPicoseconds) {
    final AtomicLong count = new AtomicLong();
    Util.loop2D(distances, (d, r, c) -> {
      final Position current = Position.of(r, c);
      // If the current position is a wall, skip it
      if (grid.get(current) == '#') {
        return;
      }
      // Loop through the available number of cheats
      for (int cheats = 2; cheats <= maxCheatsAllowed; cheats++) {
        // Loop through the current number of cheats, as the row offset
        for (int rowOffset = 0; rowOffset <= cheats; rowOffset++) {
          // The remaining number of cheats, are the column offset
          final int colOffset = cheats - rowOffset;
          // Get the next positions from the row / column offsets (i.e. applying the current cheats)
          // This has to be a set, because some positions could be double-counted
          final Set<Position> nextPositions = Stream.of(
              Position.of(r + rowOffset, c + colOffset),
              Position.of(r + rowOffset, c - colOffset),
              Position.of(r - rowOffset, c + colOffset),
              Position.of(r - rowOffset, c - colOffset))
            .collect(Collectors.toSet());
          for (final Position next : nextPositions) {
            // If after applying the cheats, we hit a wall, skip it
            if (grid.get(next) == '#') {
              continue;
            }
            // If we have achieved the target picoseconds, increment the count
            if (distances[next.x()][next.y()] - d >= targetPicoseconds + cheats) {
              count.incrementAndGet();
            }
          }
        }
      }
    });
    return count.get();
  }
}
