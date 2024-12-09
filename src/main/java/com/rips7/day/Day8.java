package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Position;
import com.rips7.util.maths.Combinatorics;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Day8 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return (long) Grid.parse(input)
      .findAntinodes(false)
      .size();
  }

  @Override
  public Long part2(String input) {
    return (long) Grid.parse(input)
      .findAntinodes(true)
      .size();
  }

  /**
   * Models a grid of antennas
   *
   * @param grid                  the original grid
   * @param antennaPositionsByTpe the antenna positions map, keyed by the antenna type
   */
  private record Grid(Character[][] grid, Map<Character, List<Position>> antennaPositionsByTpe) {

    /**
     * Parses the given input into a {@link Grid}
     *
     * @param input the input
     * @return the parsed {@link Grid}
     */
    private static Grid parse(final String input) {
      // Parse the character grid
      final Character[][] grid = Util.lines(input)
        .map(line -> line.chars()
          .mapToObj(c -> (char) c)
          .toArray(Character[]::new))
        .toArray(Character[][]::new);

      // Group all the antenna positions by type
      final Map<Character, List<Position>> antennaPositionsByType = new HashMap<>();
      Util.loop2D(grid, (e, r, c) -> {
        if (e == '.') {
          return;
        }
        antennaPositionsByType.merge(
          grid[r][c],
          new ArrayList<>(List.of(Position.of(r, c))),
          (prevList, newList) -> {
            prevList.add(Position.of(r, c));
            return prevList;
          });
      });

      return new Grid(grid, antennaPositionsByType);
    }

    /**
     * Finds the antinodes, by creating all the pairs of antennas of the same type, finding all the antinodes along
     * their connecting lines.
     *
     * @param findAll true if we want to find all the antinodes on the connecting line of the antennas, false otherwise
     * @return a set of {@link Position}s for the antinodes
     */
    private Set<Position> findAntinodes(final boolean findAll) {
      return antennaPositionsByTpe.values().stream()
        .map(list -> Combinatorics.unorderedPairs(list, true))                    // Create all the unordered pairs of antennas of each type
        .flatMap(List::stream)
        .map(pair -> this.getAntinodes(pair, findAll))                            // Get the antinodes of each pair of antennas of each type
        .flatMap(Set::stream)
        .filter(pos -> Optional.ofNullable(antennaPositionsByTpe.get(grid[pos.x()][pos.y()])) // If there is an antenna in this position, get the list of all antenna positions of this type
          .map(antennasOfType -> antennasOfType.size() > 1)                              // Keep the antinode if there's more than 1 antenna of this type
          .orElse(true))                                                                        // If there is no antenna in this position, keep the antinode
        .collect(Collectors.toSet());
    }

    /**
     * Finds all the antinodes by projecting their position on the connecting line of the two antennas. Depending on the
     * {@code findAll} flag, we only find the antinodes on either side of the connecting segment, or all the antinodes
     * along the connecting line.
     *
     * @param antennaPair a pair of antennas
     * @param findAll     true if we want to find all the antinodes on the connecting line, false otherwise
     * @return a set of {@link Position}s for the antinodes that are in line with the two antennas
     */
    private Set<Position> getAntinodes(final Pair<Position, Position> antennaPair, final boolean findAll) {
      final int dx = antennaPair.left().x() - antennaPair.right().x(); // Find the horizontal distance between the antennas
      final int dy = antennaPair.left().y() - antennaPair.right().y(); // Find the vertical distance between the antennas

      final Set<Position> antinodes = new HashSet<>();

      final int min = findAll ? 0 : 1;                                     // If 'findAll' is true, the antennas themselves produce antinodes
      final int max = findAll ? Math.max(grid.length, grid[0].length) : 1; // If 'findAll' is true, the antinodes go all the way to the end of the grid

      for (int i = min; i <= max; i++) {
        // Get the antinode on the positive side of the left antenna
        final Position antinode = Position.of(antennaPair.left().x() + i * dx, antennaPair.left().y() + i * dy);
        // If we end up outside the grid, break
        if (!Util.isWithinGrid(antinode, grid)) {
          break;
        }
        antinodes.add(antinode);
      }

      for (int i = min; i <= max; i++) {
        // Get the antinode on the negative side of the right antenna
        final Position antinode = Position.of(antennaPair.right().x() - i * dx, antennaPair.right().y() - i * dy);
        // If we end up outside the grid, break
        if (!Util.isWithinGrid(antinode, grid)) {
          break;
        }
        antinodes.add(antinode);
      }

      return antinodes;
    }
  }
}
