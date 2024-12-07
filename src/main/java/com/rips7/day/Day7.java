package com.rips7.day;

import com.rips7.util.Util;

import java.util.Arrays;

public class Day7 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return Util.lines(input)
        .map(Equation::parse)
        .parallel()
        .filter(Equation::isValidTwoOperators)
        .map(Equation::result)
        .reduce(Long::sum)
        .orElse(0L);
  }

  @Override
  public Long part2(String input) {
    return Util.lines(input)
        .map(Equation::parse)
        .parallel()
        .filter(Equation::isValidThreeOperators)
        .map(Equation::result)
        .reduce(Long::sum)
        .orElse(0L);
  }

  /**
   * Models an Equation, with a result, and some operands
   *
   * @param result   the result
   * @param operands the operands
   */
  private record Equation(long result, long[] operands) {

    /**
     * Parses an {@link Equation}
     *
     * @param line the line to parse
     * @return the parsed {@link Equation}
     */
    private static Equation parse(final String line) {
      final String[] resultAndOperands = line.split(":\\s+");
      final long result = Long.parseLong(resultAndOperands[0]);
      final long[] operands = Arrays.stream(resultAndOperands[1].split("\\s+"))
          .map(Long::parseLong)
          .mapToLong(Long::longValue)
          .toArray();
      return new Equation(result, operands);
    }

    /**
     * Checks if the {@link Equation} is valid with two operators, {@code +} and {@code *}
     *
     * @return true if a combination of operators yields the correct result, false otherwise
     */
    private boolean isValidTwoOperators() {
      // The number operator combinations is 3 ^ spaces
      final int combinations = (int) Math.pow(2, operands.length - 1);

      for (int i = 0; i < combinations; i++) {
        // Get the binary representation of the current combination, padded to the number of space
        final String binary = ("%" + (operands.length - 1) + "s").formatted(Integer.toBinaryString(i)).replace(' ', '0');
        // Evaluate the result with the current operators
        long currentResult = evaluate(binary);
        // If the result is achieved, it's a valid equation
        if (currentResult == result) {
          return true;
        }
      }
      return false;
    }

    /**
     * Checks if the {@link Equation} is valid with three operators, {@code +}, {@code *} and {@code ||}
     *
     * @return true if a combination of operators yields the correct result, false otherwise
     */
    private boolean isValidThreeOperators() {
      // The number of operator combinations is 3 ^ spaces
      final int combinations = (int) Math.pow(3, operands.length - 1);
      for (int i = 0; i < combinations; i++) {
        // Get the base 3 representation of the current combination, padded to the number of space
        final String base3 = ("%" + (operands.length - 1) + "s").formatted(Integer.toString(i, 3)).replace(' ', '0');
        // Evaluate the result with the current operators
        long currentResult = evaluate(base3);
        // If the result is achieved, it's a valid equation
        if (currentResult == result) {
          return true;
        }
      }
      return false;
    }

    /**
     * Evaluate the expression with the operands and the operators from the representation
     *
     * @param representation the operator representation
     * @return the evaluation result
     */
    private long evaluate(final String representation) {
      long currentResult = operands[0];
      for (int j = 0; j < operands.length - 1; j++) {
        currentResult = switch(representation.charAt(j)) {
          case '0' -> currentResult + operands[j + 1];                          // a '0' in the combination means '+'
          case '1' -> currentResult * operands[j + 1];                          // a '1' in the combination means '*'
          case '2' -> Long.parseLong(currentResult + "" + operands[j + 1]);  // a '2' in the combination means '||'
          default -> throw new RuntimeException("Invalid");
        };
      }
      return currentResult;
    }
  }

}
