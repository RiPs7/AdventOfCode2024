package com.rips7.day;

import com.rips7.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Day17 implements Day<String> {

  private static final boolean DEBUG = false;

  @Override
  public String part1(String input) {
    return Computer.parse(input).execute();
  }

  @Override
  public String part2(String input) {
    final long requiredA = Computer.parse(input).reverseEngineerSolve();
    return String.valueOf(requiredA);
  }

  /**
   * Models the Computer
   *
   * @param ptr the instruction pointer
   * @param a   register A
   * @param b   register B
   * @param c   register C
   * @param program the program
   */
  private record Computer(AtomicInteger ptr, AtomicLong a, AtomicLong b, AtomicLong c, List<Integer> program) {

    /**
     * Parses a {@link Computer}
     *
     * @param input the input
     * @return the {@link Computer}
     */
    private static Computer parse(final String input) {
      final List<String> lines = Util.lines(input).toList();
      final int a = Integer.parseInt(lines.getFirst().replace("Register A: ", ""));
      final int b = Integer.parseInt(lines.get(1).replace("Register B: ", ""));
      final int c = Integer.parseInt(lines.get(2).replace("Register C: ", ""));
      final List<Integer> program = Arrays.stream(lines.get(4).replace("Program: ", "").split(","))
        .map(Integer::parseInt)
        .toList();
      return new Computer(new AtomicInteger(), new AtomicLong(a), new AtomicLong(b), new AtomicLong(c), program);
    }

    /**
     * Get the combo operand from the given operand
     *
     * @param operand the operand
     * @return the combo operand
     */
    private long comboOperand(final long operand) {
      if (0L <= operand && operand <= 3L) {
        return operand;
      } else if (operand == 4L) {
        return a.get();
      } else if (operand == 5L) {
        return b.get();
      } else if (operand == 6L) {
        return c.get();
      } else {
        throw new RuntimeException("Unknown operand %s".formatted(operand));
      }
    }

    /**
     * Executes the program by implementing the instruction actions according to the problem description.
     * Note: For reverse engineering, toggle the {@code DEBUG} flag so that the execution prints the commands.
     *
     * @return the output
     */
    private String execute() {
      final List<Long> output = new ArrayList<>();
      while(ptr.get() < program.size()) {
        final int command = program.get(ptr.get());
        final int operand = program.get(ptr.get() + 1);
        if (command == 0) {        // adv
          debug("a = a >> %s".formatted(comboOperand(operand)));
          a.set(a.get() >> comboOperand(operand));
        } else if (command == 1) { // bxl
          debug("b = b ^ %s".formatted(operand));
          b.set(b.get() ^ operand);
        } else if (command == 2) { // bst
          debug("b = %s %% 8".formatted(comboOperand(operand)));
          b.set(comboOperand(operand) % 8);
        } else if (command == 3) { // jnz
          debug("if (%s != 0) jump to %s".formatted(a.get(), operand));
          if (a.get() != 0) {
            ptr.set(operand);
            continue;
          }
        } else if (command == 4) { // bxc
          debug("b = b ^ c");
          b.set(b.get() ^ c.get());
        } else if (command == 5) { // out
          debug("out(%s %% 8)".formatted(comboOperand(operand)));
          output.add(comboOperand(operand) % 8);
        } else if (command == 6) { // bdv
          debug("b = a >> %s".formatted(comboOperand(operand)));
          b.set(a.get() >> comboOperand(operand));
        } else if (command == 7) { // cdv
          debug("c = a >> %s".formatted(comboOperand(operand)));
          c.set(a.get() >> comboOperand(operand));
        }
        ptr.set(ptr.get() + 2);
      }
      return output.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
    }

    /**
     * After inspecting the output from the debug statements, we can reverse engineer the program to the following
     * logic. The output of the method below is the same as the original problem.
     */
    @SuppressWarnings("unused")
    private String reverseEngineer() {
      final List<Long> output = new ArrayList<>();
      for (long a = this.a.get(); a != 0; a /= 8) {
        long b = ((a % 8) ^ 3);
        long c = a >> b;
        b = (b ^ 5) ^ c;
        output.add(b % 8);
      }
      return output.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
    }

    /**
     * Reverse engineer the program
     *
     * @return the value of register A after reverse engineering the program
     */
    private long reverseEngineerSolve() {
      return reverseEngineerSolve(this.program, 0);
    }

    /**
     * A stage in the reverse engineering process where it tries to find the next value for register A, given the
     * current program state and the current value for register A.
     * Note: This is called recursively.
     *
     * @param program  the current program
     * @param currentA the current value for register A
     * @return the next value for register A, or -1 if none is found
     */
    private long reverseEngineerSolve(final List<Integer> program, final long currentA) {
      if (program.isEmpty()) {
        return currentA;
      }
      for (int t = 0; t < 8; t++) {
        long a = (currentA << 3) | t;
        long b = (a % 8) ^ 3;
        long c = a >> b;
        b = (b ^ 5) ^ c;
        if (b % 8 == program.getLast()) {
          long sub = reverseEngineerSolve(program.subList(0, program.size() - 1), a);
          if (sub != -1) {
            return sub;
          }
        }
      }
      return -1;
    }

    /**
     * Helper function to print the given line if th {@code DEBUG} flag is set
     *
     * @param line the line to print
     */
    private void debug(final String line) {
      if (DEBUG) {
        System.out.println(line);
      }
    }
  }

}
