package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day12 implements Day<Long> {

  private static final List<Offset> OFFSETS = List.of(Offset.UP, Offset.RIGHT, Offset.DOWN, Offset.LEFT);

  @Override
  public Long part1(String input) {
    return Garden.parse(input).parseRegions().stream()
      .map(region -> region.area() * region.perimeter())
      .reduce(Long::sum)
      .orElseThrow();
  }

  @Override
  public Long part2(String input) {
    return Garden.parse(input).parseRegions().stream()
      .map(region -> region.area() * region.sides())
      .reduce(Long::sum)
      .orElseThrow();
  }

  /**
   * Models the Garden
   *
   * @param crops the crops in the garden
   */
  private record Garden(Character[][] crops) {

    /**
     * Parses the given input into a {@link Garden}
     *
     * @param input the input
     * @return the parsed {@link Garden}
     */
    private static Garden parse(final String input) {
      final Character[][] crops = Util.lines(input)
        .map(line -> line.chars()
          .mapToObj(c -> (char) c)
          .toArray(Character[]::new))
        .toArray(Character[][]::new);
      return new Garden(crops);
    }

    /**
     * Retrieves the crop at a given {@link Position}, or {@code null} if the position is outside the garden
     *
     * @param pos the position
     * @return the height
     */
    private Character get(final Position pos) {
      return Util.isWithinGrid(pos, crops) ? crops[pos.x()][pos.y()] : null;
    }

    /**
     * Implements a flood fill algorithm to parse the {@link Region}s
     *
     * @return a set of {@link Region}s
     */
    private Set<Region> parseRegions() {
      final Set<Region> regions = new HashSet<>();

      // Loop through the crops
      Util.loop2D(crops, (crop, r, c) -> {
        final Position currentPos = Position.of(r, c);
        // The current region
        final Set<Position> region = new HashSet<>();
        region.add(currentPos);
        // The frontier for flood fill
        final Queue<Position> frontier = new ArrayDeque<>();
        frontier.add(currentPos);
        // The closed set for flood fill
        final Set<Position> closedSet = new HashSet<>();
        while(!frontier.isEmpty()) {
          final Position current = frontier.poll();
          // If the closed set contains the current position, skip
          if (closedSet.contains(current)) {
            continue;
          }
          // Get all the next positions based on the offsets
          OFFSETS.forEach(offset -> {
            final Position next = current.apply(offset);
            // If the next position is outside the grid, skip
            if (!Util.isWithinGrid(next, crops)) {
              return;
            }
            // If the crop in the next position is not the same as the crop in this region
            if (get(next) != crop) {
              return;
            }
            // If the region contains the next position, skip
            if (region.contains(next)) {
              return;
            }
            // Add the next to the region and the frontier
            region.add(next);
            frontier.add(next);
          });
          // Add the current to the closed set to not process it again
          closedSet.add(current);
        }
        // Create the region with the name of the current crop
        regions.add(new Region(crop, region));
      });

      return regions;
    }
  }

  /**
   * Models a region
   *
   * @param name  the name of the region
   * @param cells the set of all the cells within the region
   */
  private record Region(char name, Set<Position> cells) {

    /**
     * Gets the area of the region
     *
     * @return the area of the region
     */
    private long area() {
      // The area is just the number of cells in the region
      return cells.size();
    }

    /**
     * Gets the perimeter of the region
     *
     * @return the perimeter of the region
     */
    private long perimeter() {
      // Each cell contributes maximum of 4 edges to the perimeter, but we take away a number of edges equal to the
      // adjacent cells within the region
      return cells.stream()
        .map(cell -> 4L - OFFSETS.stream()
          .map(cell::apply)
          .filter(cells::contains)
          .count())
        .reduce(Long::sum)
        .orElseThrow();
    }

    // Converts a pair of coordinates to a list of pairs of coordinates.
    // If the source pair of coordinates is the center of a cell, it produces the coordinates of its corners.
    // If the source pair of coordinates is a corner, it produces the coordinates of the centers of the surrounding cells.
    private static final Function<Pair<Double, Double>, List<Pair<Double, Double>>> CONVERT = pair -> List.of(
      Pair.of(pair.left() - 0.5, pair.right() - 0.5),
      Pair.of(pair.left() + 0.5, pair.right() - 0.5),
      Pair.of(pair.left() + 0.5, pair.right() + 0.5),
      Pair.of(pair.left() - 0.5, pair.right() + 0.5));

    /**
     * Gets the sides of the region. In reality, it counts the number of corners around the region, but the number of
     * sides are equal to the number of corners.
     *
     * @return the sides of the region
     */
    private long sides() {
      return cells.stream()
        // Convert each cell to its center coordinates
        .map(pos -> Pair.of((double) pos.x(), (double) pos.y()))
        // Get all its corners
        .map(CONVERT)
        .flatMap(List::stream)
        // Collect to set to keep the distinct corners
        .collect(Collectors.toSet()).stream()
        // Convert the corners back to the surrounding cells centre
        .map(CONVERT)
        .map(centres -> centres.stream()
          // Convert the centre of each cell back to a position
          .map(center -> Position.of(center.left().intValue(), center.right().intValue()))
          // Check if the cell is within the region
          .map(cells::contains)
          .toList())
        .map(cellsInRegion -> Pair.of(
          // Count how many cells are within the region
          cellsInRegion.stream()
            .map(inRegion -> inRegion ? 1 : 0)
            .reduce(Integer::sum)
            .orElseThrow(),
          cellsInRegion))
        .map(cornerCountAndConfig -> switch(cornerCountAndConfig.left()) {
          // If 1 or 3 cells around a corner are in the region, it contributes 1 valid corner
          case 1, 3 -> 1L;
          // If 2 cells around a corner are in the region, only the following configuration contribute 2 valid corners
          case 2 -> cornerCountAndConfig.right().equals(List.of(true, false, true, false)) ||
                    cornerCountAndConfig.right().equals(List.of(false, true, false, true)) ?
                      2L :
                      0L;
          // Otherwise, it's not a valid corners
          default -> 0L;
        })
        // Tally up all the valid corners
        .reduce(Long::sum)
        .orElseThrow();
    }
  }
}
