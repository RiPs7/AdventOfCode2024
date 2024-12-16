package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Direction;
import com.rips7.util.Util.Position;
import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class Day15 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final Pair<Warehouse, List<Move>> warehouseAndMoves = parseProblem(input, false);
    final Warehouse warehouse = warehouseAndMoves.left();
    final List<Move> moves = warehouseAndMoves.right();

    moves.forEach(warehouse::makeMove);

    return warehouse.getBoxesGps();
  }

  @Override
  public Long part2(String input) {
    final Pair<Warehouse, List<Move>> warehouseAndMoves = parseProblem(input, true);
    final Warehouse warehouse = warehouseAndMoves.left();
    final List<Move> moves = warehouseAndMoves.right();

    moves.forEach(warehouse::makeMove);

    return warehouse.getBoxesGps();
  }

  /**
   * Parses the problem parameters and returns a pair of the {@link Warehouse} and the list of {@link Move}s
   *
   * @param input   the input
   * @param enlarge whether to enlarge the input
   * @return a pair of the {@link Warehouse} and the list of {@link Move}s
   */
  final Pair<Warehouse, List<Move>> parseProblem(final String input, final boolean enlarge) {
    final List<String> lines = Util.lines(input).toList();
    final int emptyLineIndex = IntStream.range(0, lines.size())
      .filter(i -> Util.isBlank(lines.get(i)))
      .findFirst()
      .orElseThrow();
    Warehouse warehouse = enlarge ?
      BigWarehouse.parse(String.join("\n", lines.subList(0, emptyLineIndex))) :
      SmallWarehouse.parse(String.join("\n", lines.subList(0, emptyLineIndex)));
    final List<Move> moves = String.join("", lines.subList(emptyLineIndex + 1, lines.size()))
      .chars()
      .mapToObj(c -> (char) c)
      .map(Move::parse)
      .toList();
    return Pair.of(warehouse, moves);
  }

  /**
   * Abstract model of the Warehouse
   */
  private static abstract class Warehouse {
    protected final Character[][] grid;
    protected final Robot robot;

    protected Warehouse(final Character[][] grid, final Robot robot) {
      this.grid = grid;
      this.robot = robot;
    }

    /**
     * Gets the element at the given {@link Position}
     *
     * @param pos the position
     * @return the element at that position, or {@code X} if the position is not within the grid
     */
    protected char get(final Position pos) {
      return Util.isWithinGrid(pos, grid) ? grid[pos.x()][pos.y()] : 'X';
    }

    /**
     * Moves the robot by placing it at the new position, and changing its original space to an empty cell
     *
     * @param move the {@link Move} to make
     */
    protected void moveRobot(final Move move) {
      grid[robot.pos.x()][robot.pos.y()] = '.';
      robot.move(robot.nextPosition(move));
      grid[robot.pos.x()][robot.pos.y()] = '@';
    }

    /**
     * Makes the given {@link Move}
     *
     * @param move the move
     */
    protected abstract void makeMove(final Move move);

    /**
     * Determines if the given character represents a box
     *
     * @param c the character to check
     * @return true if it is a box, false otherwise
     */
    protected abstract boolean isBox(final Character c);

    /**
     * Calculates the boxes GPS
     *
     * @return the boxes GPS
     */
    protected long getBoxesGps() {
      final AtomicLong boxesGps = new AtomicLong();
      Util.loop2D(grid, (e, i, j) -> {
        if (isBox(e)) {
          boxesGps.addAndGet(100L * i + j);
        }
      });
      return boxesGps.get();
    }
  }

  /**
   * Models the small Warehouse (part 1)
   */
  private static final class SmallWarehouse extends Warehouse {

    /**
     * Parses a {@link SmallWarehouse}
     *
     * @param input the input
     * @return the {@link SmallWarehouse}
     */
    private static SmallWarehouse parse(final String input) {
      // Parse the grid
      final Character[][] grid = Util.lines(input)
        .map(line -> line.chars()
          .mapToObj(c -> (char) c)
          .toArray(Character[]::new))
        .toArray(Character[][]::new);
      final AtomicReference<Position> robotPosition = new AtomicReference<>();
      // Find the robot's position
      Util.loop2D(grid, (e, r, c) -> {
        if (e == '@') {
          robotPosition.set(Position.of(r, c));
        }
      });
      return new SmallWarehouse(grid, new Robot(robotPosition.get()));
    }

    private SmallWarehouse(final Character[][] grid, final Robot robot) {
      super(grid, robot);
    }

    @Override
    protected void makeMove(final Move move) {
      final Position nextPos = robot.nextPosition(move);
      switch(get(nextPos)) {
        case '.' -> moveRobot(move);
        case 'O' -> pushObjects(move);
      }
    }

    @Override
    protected boolean isBox(final Character c) {
      return c == 'O';
    }

    /**
     * Pushes the objects by performing the given {@link Move}
     *
     * @param move the move
     */
    private void pushObjects(final Move move) {
      // Keep track of the objects to move
      final List<Position> objectsToMove = new ArrayList<>();
      Position positionToCheck = robot.nextPosition(move);
      // For as long we have a box in the checked position
      while(get(positionToCheck) == 'O') {
        // Add the position to the list of objects to move
        objectsToMove.add(positionToCheck);
        // Go to the next position
        positionToCheck = positionToCheck.apply(move.dir.offset());
      }

      // If there is a space at the last checked position, move the boxes in a reverse manner
      if (get(positionToCheck) == '.') {
        for (final Position objectToMove : objectsToMove.reversed()) {
          final Position nextPosition = objectToMove.apply(move.dir.offset());
          grid[nextPosition.x()][nextPosition.y()] = 'O';
          grid[objectToMove.x()][objectToMove.y()] = '.';
        }

        // Move the robot at the end, if the move is performed
        moveRobot(move);
      }
    }
  }

  /**
   * Models the big Warehouse (part 2)
   */
  private static final class BigWarehouse extends Warehouse {
    /**
     * Rule to enlarge the map
     */
    private static final Map<String, String> ENLARGEMENT_RULES = Map.of(
      "#", "##",
      "O", "[]",
      ".", "..",
      "@", "@."
    );

    /**
     * Parses a {@link BigWarehouse}, by applying the enlargement rules
     *
     * @param input the input
     * @return the {@link BigWarehouse}
     */
    private static BigWarehouse parse(final String input) {
      // Parse the grid
      final Character[][] grid = Util.lines(input)
        .map(line -> line.chars()
          .mapToObj(c -> String.valueOf((char) c))
          // Apply enlargement rules
          .flatMap(s -> ENLARGEMENT_RULES.get(s).chars().mapToObj(c -> (char) c))
          .toArray(Character[]::new))
        .toArray(Character[][]::new);
      // Find the robot's position
      final AtomicReference<Position> robotPosition = new AtomicReference<>();
      Util.loop2D(grid, (e, r, c) -> {
        if (e == '@') {
          robotPosition.set(Position.of(r, c));
        }
      });
      return new BigWarehouse(grid, new Robot(robotPosition.get()));
    }

    private BigWarehouse(final Character[][] grid, final Robot robot) {
      super(grid, robot);
    }

    @Override
    protected void makeMove(Move move) {
      final Position nextPos = robot.nextPosition(move);
      switch(get(nextPos)) {
        case '.' -> moveRobot(move);
        case '[', ']' -> pushObjects(move);
      }
    }

    @Override
    protected boolean isBox(final Character c) {
      return c == '[';
    }

    /**
     * Pushes the objects by performing the given {@link Move}. Uses a DFS implementation to find the objects to push
     *
     * @param move the move
     */
    private void pushObjects(Move move) {
      // Keep track of the objects to move, along with the part of the box ('[' or ']')
      final Set<Pair<Character, Position>> objectsToMove = new HashSet<>();

      // DFS implementation
      final Stack<Position> frontier = new Stack<>();
      final Set<Position> closedSet = new HashSet<>();
      final Position nextPos = robot.nextPosition(move);
      frontier.add(nextPos);

      objectsToMove.add(Pair.of(get(nextPos), nextPos));
      // When moving UP and DOWN, we need to add both parts of the box
      if (move.dir == Direction.UP || move.dir == Direction.DOWN) {
        if (get(nextPos) == '[') {
          frontier.add(Position.of(nextPos.x(), nextPos.y() + 1));
          objectsToMove.add(Pair.of(']', Position.of(nextPos.x(), nextPos.y() + 1)));
        } else if (get(nextPos) == ']') {
          frontier.add(Position.of(nextPos.x(), nextPos.y() - 1));
          objectsToMove.add(Pair.of('[', Position.of(nextPos.x(), nextPos.y() - 1)));
        }
      }

      while(!frontier.isEmpty()) {
        final Position current = frontier.pop();
        if (closedSet.contains(current)) {
          continue;
        }
        final Position next = current.apply(move.dir.offset());
        // If we hit a wall, return, since the move is not valid at all
        if (get(next) == '#') {
          return;
        }
        if (get(next) == '.') {
          continue;
        }

        objectsToMove.add(Pair.of(get(next), next));
        // When moving UP and DOWN, we need to add both parts of the box
        if (move.dir == Direction.UP || move.dir == Direction.DOWN) {
          if (get(next) == '[') {
            frontier.add(next);
            frontier.add(Position.of(next.x(), next.y() + 1));
            objectsToMove.add(Pair.of('[', next));
            objectsToMove.add(Pair.of(']', Position.of(next.x(), next.y() + 1)));
          } else if (get(next) == ']') {
            frontier.add(next);
            frontier.add(Position.of(next.x(), next.y() - 1));
            objectsToMove.add(Pair.of(']', next));
            objectsToMove.add(Pair.of('[', Position.of(next.x(), next.y() - 1)));
          }
        } else if (get(next) != '.') {
          // For other types of move, just add the part of the box
          frontier.add(next);
          objectsToMove.add(Pair.of(get(next), next));
        }
        closedSet.add(current);
      }

      // Sort the objects, so that the ones further away from the robot are moved first
      final List<Pair<Character, Position>> objectsToMoveSorted = objectsToMove.stream()
        .sorted((p1, p2) -> switch (move.dir) {
          case LEFT -> Comparator.comparingInt((Pair<Character, Position> p) -> p.right().y()).compare(p1, p2);
          case RIGHT -> Comparator.comparingInt((Pair<Character, Position> p) -> p.right().y()).compare(p2, p1);
          case UP -> Comparator.comparingInt((Pair<Character, Position> p) -> p.right().x()).compare(p1, p2);
          case DOWN -> Comparator.comparingInt((Pair<Character, Position> p) -> p.right().x()).compare(p2, p1);
          default -> throw new RuntimeException("Unknown move %s".formatted(move.dir));
        })
        .toList();

      // Move the boxes in the way they are sorted
      for (final Pair<Character, Position> objectToMove : objectsToMoveSorted) {
        final Position nextPosition = objectToMove.right().apply(move.dir.offset());
        grid[nextPosition.x()][nextPosition.y()] = objectToMove.left();
        grid[objectToMove.right().x()][objectToMove.right().y()] = '.';
      }

      // Move the robot at the end, if the move is performed
      moveRobot(move);
    }
  }

  /**
   * Models a Robot
   */
  private static final class Robot {
    private Position pos;

    private Robot(final Position pos) {
      this.pos = pos;
    }

    /**
     * Gets the next {@link Position} if the robot was to perform the given {@link Move}
     *
     * @param move the {@link Move}
     * @return the next {@link Position}
     */
    private Position nextPosition(final Move move) {
      return this.pos.apply(move.dir.offset());
    }

    /**
     * Perform the move by setting the robot's {@link Position} to the given one
     *
     * @param newPos the new robot {@link Position}
     */
    private void move(final Position newPos) {
      this.pos = newPos;
    }
  }

  /**
   * Models a Move
   *
   * @param name the name of the move
   * @param dir  the {@link Direction}
   */
  private record Move(Character name, Direction dir) {

    /**
     * Parses a {@link Move}
     *
     * @param c the character to parse the {@link Move} from
     * @return the {@link Move}
     */
    private static Move parse(final Character c) {
      final Direction dir = switch (c) {
        case '^' -> Direction.UP;
        case '>' -> Direction.RIGHT;
        case 'v' -> Direction.DOWN;
        case '<' -> Direction.LEFT;
        default -> throw new RuntimeException("Unknown move %s".formatted(c));
      };
      return new Move(c, dir);
    }
  }
}
