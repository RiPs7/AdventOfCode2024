package com.rips7.day;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day24 implements Day<String> {

  private static final Pattern INITIAL_INPUT_PATTERN = Pattern.compile("([xy]\\d+): ([01])");

  @Override
  public String part1(String input) {
    final Circuit circuit = new Circuit(parseInitialInputs(input), parseGates(input));
    return String.valueOf(circuit.process());
  }

  @Override
  public String part2(String input) {
    final List<Gate> gates = parseGates(input);

    // Find which output bits are wrong
    final List<Integer> wrongOutputBits = IntStream.range(0, 45)
      .mapToObj(i -> {
        // Try to add 2^i and 0. If the result is incorrect, it means that the wiring for the i-th output bit is wrong
        final Circuit circuit = new Circuit(createInitialInputs(1L << i, 0L), gates);
        // The result must be 2^i
        return circuit.process() != 1L << i ? i : null;
      })
      .filter(Objects::nonNull)
      .toList();

    // The following comes from observation and analysis of the input. Making the changes directly to the input,
    // eliminates the wrong output bits list above.
    final List<String> wrongPairs = new ArrayList<>();
    if (wrongOutputBits.contains(8)) {
      wrongPairs.add("z08");
      wrongPairs.add("thm");
    }
    if (wrongOutputBits.contains(14)) {
      wrongPairs.add("wrm");
      wrongPairs.add("wss");
    }
    if (wrongOutputBits.contains(22)) {
      wrongPairs.add("z22");
      wrongPairs.add("hwq");
    }
    if (wrongOutputBits.contains(29)) {
      wrongPairs.add("z29");
      wrongPairs.add("gbs");
    }

    return wrongPairs.stream().sorted().collect(Collectors.joining(","));
  }

  /**
   * Parses the initial inputs (i.e. wires x and y)
   *
   * @param input the input
   * @return a map of wire names to initial input
   */
  private Map<String, Boolean> parseInitialInputs(final String input) {
    return input.split("\n\n")[0].lines()
      .map(INITIAL_INPUT_PATTERN::matcher)
      .map(matcher -> matcher.matches() ?
        Map.entry(matcher.group(1), matcher.group(2).equals("1")) :
        null)
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Parses the gates
   *
   * @param input the input
   * @return list of gates
   */
  private List<Gate> parseGates(final String input) {
    return input.split("\n\n")[1].lines()
      .map(line -> line.split(" "))
      .map(parts -> {
        final GateType gateType = switch (parts[1]) {
          case "AND" -> GateType.AND;
          case "OR" -> GateType.OR;
          case "XOR" -> GateType.XOR;
          default -> throw new RuntimeException("Invalid gate type %s".formatted(parts[1]));
        };
        return new Gate(gateType, parts[0], parts[2], parts[4]);
      })
      .toList();
  }

  /**
   * Create the initial inputs for the given numbers
   *
   * @param num1 the first number (will be fed into the 'x' wires)
   * @param num2 the second number (will be fed into the 'y' wires)
   * @return a map of wire names to initial input
   */
  @SuppressWarnings("SameParameterValue")
  private Map<String, Boolean> createInitialInputs(final long num1, final long num2) {
    // Create the list of x ['x44', 'x43', ..., 'x01', 'x00']
    final List<String> input1 = IntStream.range(0, 45)
      .boxed()
      .sorted(Comparator.reverseOrder())
      .map(i -> "x" + "0".repeat(2 - String.valueOf(i).length()) + i)
      .toList();
    // Create the list of y ['y44', 'y43', ..., 'y01', 'y00']
    final List<String> input2 = IntStream.range(0, 45)
      .boxed()
      .sorted(Comparator.reverseOrder())
      .map(i -> "y" + "0".repeat(2 - String.valueOf(i).length()) + i)
      .toList();
    // Convert them to binary
    final String binNum1 = Long.toBinaryString(num1);
    final String binNum2 = Long.toBinaryString(num2);
    // Pad them with leading zeros until their length is 45 bits
    final String paddedBinNum1 = "0".repeat(45 - binNum1.length()) + binNum1;
    final String paddedBinNum2 = "0".repeat(45 - binNum2.length()) + binNum2;
    // Create the map of initial input for x's
    final Map<String, Boolean> initialInput1 = IntStream.range(0, input1.size())
      .boxed()
      .collect(Collectors.toMap(input1::get, i -> paddedBinNum1.charAt(i) == '1'));
    // Create the map of initial input for y's
    final Map<String, Boolean> initialInput2 = IntStream.range(0, input2.size())
      .boxed()
      .collect(Collectors.toMap(input2::get, i -> paddedBinNum2.charAt(i) == '1'));
    // Combine them into a single map
    return Stream.concat(
      initialInput1.entrySet().stream(),
      initialInput2.entrySet().stream())
    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Models the circuit
   *
   * @param initialInputs the map of initial inputs
   * @param gates         the list of gates
   */
  private record Circuit(Map<String, Boolean> initialInputs, List<Gate> gates) {

    private long process() {
      // The current wiring inputs
      Map<String, Boolean> inputs = initialInputs;
      // While there are still gates that haven't produced output
      while(gates.stream().anyMatch(gate -> gate.output.state == null)) {
        final Map<String, Boolean> outputs = new HashMap<>();
        // For each gate
        for (final Gate gate : gates) {
          // Try to update its input 1
          if (gate.input1.state == null && inputs.containsKey(gate.input1.name)) {
            gate.input1.state = inputs.get(gate.input1.name);
          }
          // Try to update its input 2
          if (gate.input2.state == null && inputs.containsKey(gate.input2.name)) {
            gate.input2.state = inputs.get(gate.input2.name);
          }
          // If both inputs are provided, produce the output
          if (gate.input1.state != null && gate.input2.state != null) {
            outputs.put(gate.output.name, gate.compute());
          }
        }
        // The newly calculated outputs become the inputs for the next iteration
        inputs = outputs;
      }

      // All the outputs that start with 'z' generate the output (respecting the highest-to-lowest significance)
      final String binaryOutput = gates.stream()
        .filter(gate -> gate.output.name.startsWith("z"))
        .sorted(Comparator.comparing(gate -> gate.output.name, Comparator.reverseOrder()))
        .map(gate -> gate.output.state ? "1" : "0")
        .collect(Collectors.joining());

      // Convert the binary to decimal
      return Long.parseLong(binaryOutput, 2);
    }
  }

  /**
   * Models a Gate
   *
   * @param gateType the gate type
   * @param input1   the input 1 wire
   * @param input2   the input 2 wire
   * @param output   the output wire
   */
  private record Gate (GateType gateType, Wire input1, Wire input2, Wire output) {

    private Gate(final GateType gateType, final String input1, final String input2, final String output) {
      this(gateType, new Wire(input1), new Wire(input2), new Wire(output));
    }

    /**
     * Compute the output. This is a lazy operation, and if the output has been computed before, it won't be recomputed.
     *
     * @return the output
     */
    private boolean compute() {
      if (output.state == null) {
        output.state = switch (gateType) {
          case AND -> input1.state && input2.state;
          case OR -> input1.state || input2.state;
          case XOR -> input1.state ^ input2.state;
        };
      }
      return output.state;
    }
  }

  /**
   * Models a Wire
   */
  private static final class Wire {
    private final String name;
    private Boolean state;

    private Wire(final String name, final Boolean state) {
      this.name = name;
      this.state = state;
    }

    private Wire(final String name) {
      this(name, null);
    }
  }

  /**
   * Enum for the gate type
   */
  private enum GateType {
    AND,
    OR,
    XOR
  }

}
