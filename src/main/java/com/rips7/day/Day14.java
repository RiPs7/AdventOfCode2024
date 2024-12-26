package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Position;
import com.rips7.util.maths.Combinatorics.Triplet;
import com.rips7.util.maths.Maths.Vector2D;

import java.util.Comparator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day14 implements Day<Long> {

  private static final boolean PRINT_ANSWER_PART_2 = false;

  private static final int WIDTH = 101;
  private static final int HEIGHT = 103;

  @Override
  public Long part1(String input) {
    final Set<Position> positions = Util.lines(input)
      .map(Robot::parse)
      .map(robot -> robot.simulate(100))
      .collect(Collectors.toSet());
    return safetyFactor(positions);
  }

  @Override
  public Long part2(String input) {
    // The safety factor is an indication of entropy. If a Christmas Tree appears, it minimises the entropy of the
    // robots position on the grid. We look for the configuration that produces the minimum safety factor.
    final Triplet<Long, Set<Position>, Integer> christmasTreeConfiguration = IntStream.range(1, WIDTH * HEIGHT)
      .parallel()
      .mapToObj(i -> {
        final Set<Position> positions = Util.lines(input)
          .map(Robot::parse)
          .map(robot -> robot.simulate(i))
          .collect(Collectors.toSet());
        return Triplet.of(safetyFactor(positions), positions, i);
      })
      .min(Comparator.comparingLong(Triplet::first))
      .orElseThrow();

    // Print the grid if the flag is set just to see the Christmas tree
    if (PRINT_ANSWER_PART_2) {
      printAnswer(christmasTreeConfiguration.second());
    }

    return (long) christmasTreeConfiguration.third();
  }

  private long safetyFactor(final Set<Position> positions) {
    final Quadrant topLeft = new Quadrant(0, 0, WIDTH / 2, HEIGHT / 2);
    final Quadrant topRight = new Quadrant(WIDTH / 2 + 1, 0, WIDTH / 2, HEIGHT / 2);
    final Quadrant bottomLeft = new Quadrant(0, HEIGHT / 2 + 1, WIDTH / 2, HEIGHT / 2);
    final Quadrant bottomRight = new Quadrant(WIDTH / 2 + 1, HEIGHT / 2 + 1, WIDTH / 2, HEIGHT / 2);

    long inTopLeft = 0;
    long inTopRight = 0;
    long inBottomLeft = 0;
    long inBottomRight = 0;

    for (final Position pos : positions) {
      if (topLeft.contains(pos)) {
        inTopLeft++;
      } else if (topRight.contains(pos)) {
        inTopRight++;
      } else if (bottomLeft.contains(pos)) {
        inBottomLeft++;
      } else if (bottomRight.contains(pos)) {
        inBottomRight++;
      }
    }

    return inTopLeft * inTopRight * inBottomLeft * inBottomRight;
  }

  private void printAnswer(final Set<Position> positions) {
    System.out.println(
      IntStream.range(0, HEIGHT).mapToObj(r ->
        IntStream.range(0, WIDTH).mapToObj(c -> positions.contains(Position.of(c, r)) ? "#" : ".")
          .collect(Collectors.joining()))
        .collect(Collectors.joining("\n")));
  }

  /**
   * Models a Quadrant
   *
   * @param x      the x-coordinate of its top left corner
   * @param y      the y-coordinate of its top left corner
   * @param width  the width of the quadrant
   * @param height the height of the quadrant
   */
  private record Quadrant(int x, int y, int width, int height) {

    /**
     * Checks if the quadrant contains a given position
     *
     * @param pos the position
     * @return true if the given position is in the quadrant
     */
    private boolean contains(Position pos) {
      return pos.x() >= x && pos.x() < x + width &&
             pos.y() >= y && pos.y() < y + height;
    }
  }

  /**
   * Models a Robot
   *
   * @param pos the {@link Position}
   * @param vel the {@link Vector2D} that represents the velocity
   */
  private record Robot(Position pos, Vector2D<Integer> vel) {
    // The pattern for each input line
    private static final Pattern PATTERN = Pattern.compile("p=(-?\\d+),(-?\\d+) v=(-?\\d+),(-?\\d+)");

    /**
     * Parses the given input into a {@link Robot}
     *
     * @param input the input
     * @return the parsed {@link Robot}
     */
    private static Robot parse(final String input) {
      final Matcher matcher = PATTERN.matcher(input);
      if (!matcher.matches()) {
        throw new RuntimeException("Cannot parse robot from input %s".formatted(input));
      }
      final int posX = Integer.parseInt(matcher.group(1));
      final int posY = Integer.parseInt(matcher.group(2));
      final int velX = Integer.parseInt(matcher.group(3));
      final int velY = Integer.parseInt(matcher.group(4));
      return new Robot(Position.of(posX, posY), Vector2D.of(velX, velY));
    }

    /**
     * Simulates the movement of the {@link Robot} by calculating its final position after the given time has elapsed
     *
     * @param time the time
     * @return the final {@link Position}
     */
    private Position simulate(int time) {
      // Px' = (Px + Vx * t) % WIDTH
      final int finalX = Math.floorMod(pos.x() + time * vel.x(), WIDTH);
      // Py' = (Py + Vy * t) % HEIGHT
      final int finalY = Math.floorMod(pos.y() + time * vel.y(),  HEIGHT);

      return Position.of(finalX, finalY);
    }
  }
}
