package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Direction;
import com.rips7.util.Util.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Day6 implements Day<Long> {

  // Define special character
  private static final char OBSTACLE = '#';
  private static final char EMPTY = '.';
  private static final char OUTSIDE = '-';
  private static final char UP = '^';
  private static final char RIGHT = '>';
  private static final char DOWN = 'v';
  private static final char LEFT = '<';

  @Override
  public Long part1(String input) {
    final Lab lab = Lab.parse(input);
    final Guard guard = lab.findGuard();

    // Keep a set for visited positions
    final Set<Position> visited = new HashSet<>();

    while(guard.isInside(lab)) {
      // Add the guard's position to the visited positions
      visited.add(guard.pos);
      // Move the guard to next position
      guard.move(lab);
    }

    return (long) visited.size();
  }

  @Override // NOTE: Takes about 4 seconds to run
  public Long part2(String input) {
    final Lab lab = Lab.parse(input);
    final Guard guard = lab.findGuard();

    return IntStream.range(0, lab.map.length).parallel()
        .mapToObj(r -> IntStream.range(0, lab.map[r].length).parallel()
            .mapToObj(c -> {
              // If the current cell is not an empty one, we skip it, as we can't add an obstacle there
              if (lab.map[r][c] != EMPTY) {
                return 0L;
              }

              // Clone the lab and the guard to operate on the copies instead of mutating the original objects
              final Lab labCopy = Lab.copyWithObstacle(lab, r, c);
              final Guard guardCopy = Guard.copy(guard);

              // Keep a history map, keyed by positions, with values the directions of the guard when moved through
              // these positions
              final Map<Position, Set<Direction>> history = new HashMap<>();

              while(guardCopy.isInside(labCopy)) {
                // Move the guard to next position
                guardCopy.move(labCopy);

                // If the history contains the guard's position and direction, we found a loop
                if (history.getOrDefault(guardCopy.pos, Set.of()).contains(guardCopy.dir)) {
                  return 1L;
                }

                // Add the position - direction into the history
                history.merge(
                    guardCopy.pos,
                    new HashSet<>(Set.of(guardCopy.dir)),
                    (existingSet, newSet) -> {
                      existingSet.add(guardCopy.dir);
                      return existingSet;
                    });
              }
              return 0L;
            }))
        .flatMap(Function.identity())
        .reduce(Long::sum)
        .orElse(0L);
  }

  /**
   * Models the Lab
   *
   * @param map the 2D character map of the lab
   */
  private record Lab(Character[][] map) {

    /**
     * Parses the given input into a {@link Lab}
     *
     * @param input the input
     * @return the {@link Lab}
     */
    private static Lab parse(final String input) {
      final Character[][] map = Util.lines(input)
          .map(line -> line.chars()
              .mapToObj(c -> (char) c)
              .toArray(Character[]::new))
          .toArray(Character[][]::new);
      return new Lab(map);
    }

    /**
     * Clones a {@link Lab} with an obstacle at the given row and column
     *
     * @param source      the source {@link Lab}
     * @param obstacleRow the row of the obstacle
     * @param obstacleCol the column of the obstacle
     * @return the cloned {@link Lab}
     */
    private static Lab copyWithObstacle(final Lab source, final int obstacleRow, final int obstacleCol) {
      final Character[][] mapCopy = Util.copy2D(Character.class, source.map);
      mapCopy[obstacleRow][obstacleCol] = OBSTACLE;
      return new Lab(mapCopy);
    }

    /**
     * Finds the {@link Guard} in the {@link Lab}
     *
     * @return a {@link Guard} instance
     */
    private Guard findGuard() {
      for (int r = 0; r < map.length; r++) {
        for (int c = 0; c < map[r].length; c++) {
          switch (map[r][c]) {
            case UP -> {
              return new Guard(Position.of(r, c), Direction.UP);
            }
            case RIGHT -> {
              return new Guard(Position.of(r, c), Direction.RIGHT);
            }
            case DOWN -> {
              return new Guard(Position.of(r, c), Direction.DOWN);
            }
            case LEFT -> {
              return new Guard(Position.of(r, c), Direction.LEFT);
            }
            default -> {
              // ignore
            }
          }
        }
      }
      throw new RuntimeException("Cannot find guard");
    }

    /**
     * Gets the character of a cell in the given position
     *
     * @param pos the {@link Position}
     * @return the character
     */
    private Character getCell(final Position pos) {
      return Util.isWithinGrid(pos, map) ?
          map[pos.x()][pos.y()] :
          OUTSIDE;
    }
  }

  /**
   * Models the Guard
   */
  private static final class Guard {
    private Position pos;
    private Direction dir;

    /**
     * Clones a {@link Guard}
     *
     * @param source the source guard
     * @return the cloned {@link Guard}
     */
    private static Guard copy(final Guard source) {
      return new Guard(Position.of(source.pos.x(), source.pos.y()), source.dir);
    }

    /**
     * Constructor
     *
     * @param pos the position
     * @param dir the direction
     */
    public Guard(final Position pos, final Direction dir) {
      this.pos = pos;
      this.dir = dir;
    }

    /**
     * Checks if the guard is inside the {@link Lab}
     *
     * @param lab the {@link Lab}
     * @return true if the guard is inside, false otherwise
     */
    private boolean isInside(final Lab lab) {
      return Util.isWithinGrid(pos, lab.map);
    }

    /**
     * Moves the {@link Guard} inside the given {@link Lab}
     * <li>
     *     <ul>If the next position is an obstacle, just rotate 90º</ul>
     *     <ul>Otherwise, move forwards</ul>
     * </li>
     *
     * @param lab the {@link Lab}
     */
    private void move(final Lab lab) {
      final Position nextPos = pos.apply(dir.offset());
      final Character nextCell = lab.getCell(nextPos);
      if (nextCell == OBSTACLE) {
        dir = dir.rotate90();
      } else {
        pos = nextPos;
      }
    }
  }

}
