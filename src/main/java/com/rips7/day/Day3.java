package com.rips7.day;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day3 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return Program.parse(input, false).run();
  }

  @Override
  public Long part2(String input) {
    return Program.parse(input, true).run();
  }

  /**
   * Models the entire program as a set of instructions
   *
   * @param instructions   the instructions
   * @param withConditions whether the program supports conditions
   * @param shouldDo       whether the next instruction should be executed
   */
  private record Program(List<Instruction<?>> instructions, boolean withConditions, AtomicBoolean shouldDo) {
    private static Program parse(final String input, final boolean withConditions) {
      // By default, the next instruction should be executed
      final AtomicBoolean shouldDo = new AtomicBoolean(true);

      return new Program(parseInstructions(input, withConditions, shouldDo), withConditions, shouldDo);
    }

    /**
     * Parse the instructions. Reads through the program code and:
     * <ul>
     *  <li>Try to parse as a Mul instruction</li>
     *  <li>If program supports conditions, try to parse as Condition</li>
     *  <li>Otherwise, return a null instruction</li>
     * </ul>
     *
     * @param program        the program code
     * @param withConditions whether the program supports conditions
     * @param shouldDo       whether the next instruction should be executed (will be passed to Condition instructions)
     * @return the instructions
     */
    private static List<Instruction<?>> parseInstructions(final String program, final boolean withConditions,
                                                          final AtomicBoolean shouldDo) {
      return IntStream.range(0, program.length())
          .mapToObj(i ->
              tryParseMul(program, i)
                  .orElseGet(() -> withConditions ?
                      tryParseCondition(program, i, shouldDo).orElse(null) :
                      null))
          .filter(Objects::nonNull)
          .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Tries to parse a Mul instruction
     *
     * @param program the program code
     * @param i       the current index
     * @return an optional of the parsed instruction
     */
    private static Optional<Instruction<?>> tryParseMul(final String program, final int i) {
      if (!(
          program.charAt(i)     == 'm' &&
          program.charAt(i + 1) == 'u' &&
          program.charAt(i + 2) == 'l' &&
          program.charAt(i + 3) == '('
      )) {
        return Optional.empty();
      }

      final StringBuilder sb = new StringBuilder();
      int j = i + 4;
      while (program.charAt(j) != ')') {
        sb.append(program.charAt(j));
        j++;
      }
      if (!sb.toString().contains(",")) {
        return Optional.empty();
      }
      final String[] numbers = sb.toString().split(",");
      try {
        return Optional.of(new Mul(Long.parseLong(numbers[0]), Long.parseLong(numbers[1])));
      } catch (final Exception e) {
        return Optional.empty();
      }
    }

    /**
     * Tries to parse a Condition instruction
     *
     * @param program  the program code
     * @param i        the current index
     * @param shouldDo whether the next instruction should be executed (will be used in a callback for the instruction)
     * @return an optional of the parsed instruction
     */
    private static Optional<Instruction<?>> tryParseCondition(final String program, final int i, final AtomicBoolean shouldDo) {
      if (
          program.charAt(i)     == 'd' &&
          program.charAt(i + 1) == 'o' &&
          program.charAt(i + 2) == 'n' &&
          program.charAt(i + 3) == '\'' &&
          program.charAt(i + 4) == 't' &&
          program.charAt(i + 5) == '(' &&
          program.charAt(i + 6) == ')'
      ) {
        return Optional.of(new Condition(() -> shouldDo.set(false)));
      }
      if (
          program.charAt(i)     == 'd' &&
          program.charAt(i + 1) == 'o' &&
          program.charAt(i + 2) == '(' &&
          program.charAt(i + 3) == ')'
      ) {
        return Optional.of(new Condition(() -> shouldDo.set(true)));
      }
      return Optional.empty();
    }

    /**
     * Runs a list of instructions.
     * <ul>
     *  <li>For {@link Mul} instructions, they only run if the {@code shouldDo} flag is true, and return their result.</li>
     *  <li>For other types, they run and return 0</li>
     * </ul>
     *
     * @return the sum of the calculated multiplications
     */
    private long run() {
      return instructions.stream()
          .filter(i -> withConditions || i instanceof Mul)
          .map(i -> {
            if (i instanceof Mul mul && shouldDo.get()) {
              return mul.run();
            }
            i.run();
            return 0L;
          })
          .reduce(Long::sum)
          .orElse(0L);
    }
  }

  /**
   * An interface for an instruction
   *
   * @param <T> the type of the returned result
   */
  private interface Instruction<T> {
    T run();
  }

  /**
   * An instruction of condition type, which runs the given runnable
   *
   * @param runnable the runnable to run
   */
  private record Condition(Runnable runnable) implements Instruction<Void> {
    @Override
    public Void run() {
      runnable.run();
      return null;
    }
  }

  /**
   * An instruction of multiplication type, which perform a multiplication of its two numbers
   *
   * @param a the first number
   * @param b the second number
   */
  private record Mul(long a, long b) implements Instruction<Long> {
    @Override
    public Long run() {
      return a * b;
    }
  }
}
