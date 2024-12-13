package com.rips7.day;

import com.rips7.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return parseMachines(input, 0L).stream()
      .map(Machine::findWinningPrizeCost)
      .reduce(Long::sum)
      .orElseThrow();
  }

  @Override
  public Long part2(String input) {
    return parseMachines(input, 10000000000000L).stream()
      .map(Machine::findWinningPrizeCost)
      .reduce(Long::sum)
      .orElseThrow();
  }

  /**
   * Parses the {@link Machine}s
   *
   * @param input  the input
   * @param offset the offset for the prize
   * @return a list of {@link Machine}s
   */
  private List<Machine> parseMachines(final String input, final long offset) {
    final List<String> lines = Util.lines(input).toList();
    final List<Machine> machines = new ArrayList<>();
    for (int i = 0; i < lines.size(); i += 4) {
      final Machine machine = new Machine(
        Button.parse(lines.get(i)),
        Button.parse(lines.get(i + 1)),
        Prize.parse(lines.get(i + 2), offset));
      machines.add(machine);
    }
    return machines;
  }

  /**
   * Models a Machine
   *
   * @param buttonA the {@link Button} A
   * @param buttonB the {@link Button} B
   * @param prize   the {@link Prize}
   */
  private record Machine(Button buttonA, Button buttonB, Prize prize) {

    /**
     * We need to solve the following system of equations
     * <pre>
     *   Ax * A + Bx * B = Px
     *   Ay * A + By * B = Py
     * </pre>
     * where
     * <ul>
     *   <li>Ax: The offset on x-axis from button A (known)</li>
     *   <li>Ay: The offset on y-axis from button A (known)</li>
     *   <li>Bx: The offset on x-axis from button B (known)</li>
     *   <li>By: The offset on y-axis from button B (known)</li>
     *   <li>Px: The prize x coordinate (known)</li>
     *   <li>Py: The prize y coordinate (known)</li>
     *   <li>A : The number of button A pushes (unknown)</li>
     *   <li>B : The number of button B pushes (unknown)</li>
     * </ul>
     * To eliminate one of the unknowns (B), we multiply first equation by {@code By} and second equation by {@code Bx}.
     * <pre>
     *   Ax * By * A + Bx * By * B = Px * By
     *   Ay * Bx * A + By * Bx * B = Py * Bx
     * </pre>
     * Subtracting the two equations:
     * <pre>
     *   Ax * By * A - Ay * Bx * A = Px * By - Py * Bx
     * </pre>
     * Solving for A, we have:
     * <pre>
     *   A = (Px * By - Py * Bx) / (Ax * By - Ay * Bx)    (1)
     * </pre>
     * Equally, we have:
     * <pre>
     *   B = (Px * Ay - Py * Ax) / (Bx * Ay - By * Ax)    (2)
     * </pre>
     *
     * @return the winning prize cost
     */
    private long findWinningPrizeCost() {
      // Find Button A pushes using (1)
      final long buttonAPushes =
        (prize.x * buttonB.offY - prize().y * buttonB.offX) /
        (buttonA.offX * buttonB.offY - buttonA.offY * buttonB.offX);
      // Find Button A pushes using (2)
      final long buttonBPushes =
        (prize.x * buttonA.offY - prize().y * buttonA.offX) /
        (buttonB.offX * buttonA.offY - buttonB.offY * buttonA.offX);
      // If the above candidate values are actually solutions, return the total cost
      if (buttonAPushes * buttonA.offX + buttonBPushes * buttonB.offX == prize().x &&
        buttonAPushes * buttonA.offY + buttonBPushes * buttonB.offY == prize().y) {
        return buttonAPushes * buttonA.cost + buttonBPushes * buttonB.cost;
      }
      // Else, return 0
      return 0L;
    }
  }

  /**
   * Models a Button
   *
   * @param name the name of the button
   * @param offX the offset in x-axis
   * @param offY the offset in y-axis
   * @param cost the cost for pressing the button
   */
  private record Button(char name, long offX, long offY, long cost) {

    private static final Pattern PATTERN = Pattern.compile("Button ([AB]): X\\+(\\d+), Y\\+(\\d+)");

    /**
     * Parses the given input into a {@link Button}
     *
     * @param input the input
     * @return the parsed {@link Button}
     */
    private static Button parse(final String input) {
      final Matcher matcher = PATTERN.matcher(input);
      if (!matcher.matches()) {
        throw new RuntimeException("Cannot parse %s".formatted(input));
      }
      final char name = matcher.group(1).charAt(0);
      final long offX = Integer.parseInt(matcher.group(2));
      final long offY = Integer.parseInt(matcher.group(3));
      final long cost = name == 'A' ? 3 : 1;
      return new Button(name, offX, offY, cost);
    }
  }

  /**
   * Models a Prize
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  private record Prize(long x, long y) {

    private static final Pattern PATTERN = Pattern.compile("Prize: X=(\\d+), Y=(\\d+)");

    /**
     * Parses the given input into a {@link Prize}
     *
     * @param input the input
     * @return the parsed {@link Prize}
     */
    private static Prize parse(final String input, final long offset) {
      final Matcher matcher = PATTERN.matcher(input);
      if (!matcher.matches()) {
        throw new RuntimeException("Cannot parse %s".formatted(input));
      }
      final long x = Long.parseLong(matcher.group(1)) + offset;
      final long y = Long.parseLong(matcher.group(2)) + offset;
      return new Prize(x, y);
    }
  }

}
