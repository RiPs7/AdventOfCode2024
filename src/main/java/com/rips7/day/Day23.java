package com.rips7.day;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day23 implements Day<String> {

  @Override
  public String part1(String input) {
    final Map<String, Set<String>> computers = parseComputers(input);

    // Create the LANs as follows
    final Set<List<String>> LANs = computers.keySet().stream()
      // For each computer 'computer1'
      .flatMap(computer1 -> computers.get(computer1).stream()
        // For each next computer 'computer2'
        .flatMap(computer2 -> computers.get(computer2).stream()
          // For each next computer 'computer3'
          // Keep all 'computer3' candidates that link back to 'computer1'
          .filter(computer3 -> !computer3.equals(computer1) && computers.get(computer3).contains(computer1))
          // Create a lan of 'computer1', 'computer2' and 'computer3' (sorted to avoid double counting LANs)
          .map(computer3 -> Stream.of(computer1, computer2, computer3)
            .sorted()
            .toList())))
      .collect(Collectors.toSet());

    // Count the LANs that contain a computer that starts with 't'
    return String.valueOf(LANs.stream()
      .filter(lan -> lan.stream().anyMatch(computer -> computer.startsWith("t")))
      .count());
  }

  @Override
  public String part2(String input) {
    final Map<String, Set<String>> computers = parseComputers(input);

    // Find all LANs
    final Set<List<String>> allLANs = new HashSet<>();
    for (final String computer : computers.keySet()) {
      search(computer, Set.of(computer), computers, allLANs);
    }

    // Find the biggest LAN
    final List<String> biggestLAN = allLANs.stream()
      .max(Comparator.comparingInt(List::size))
      .orElseThrow();

    // Create the LAN password by joining the computers (they are already sorted) with a ','
    return String.join(",", biggestLAN);
  }

  /**
   * Recursively searches the next computers that belong to the LAN
   *
   * @param computer  the computer to start searching from
   * @param LAN       the LAN so far
   * @param computers the connectivity map of the computers
   * @param allLANs   the set of LANs
   */
  private void search(final String computer, final Set<String> LAN, final Map<String, Set<String>> computers,
                      final Set<List<String>> allLANs) {
    // Compute the LAN key as a sorted list of its computers
    final List<String> LANKey = LAN.stream().sorted().toList();
    // If the current LAN is part of all LANs, skip it
    if (allLANs.contains(LANKey)) {
      return;
    }
    // Add the current LAN to the set of all LANs
    allLANs.add(LANKey);
    // For each computer that the current one connects to
    for (final String next : computers.get(computer)) {
      // If it's already in the current LAN, skip it
      if (LAN.contains(next)) {
        continue;
      }
      // If there is no connection from the LAN that gets to the next one, skip it
      if (LAN.stream().map(computers::get).anyMatch(connected -> !connected.contains(next))) {
        continue;
      }
      // Recursively search from the next computer, extending the current LAN with the next computer (a copy of it)
      search(next, Stream.concat(LAN.stream(), Stream.of(next)).collect(Collectors.toSet()), computers, allLANs);
    }
  }

  /**
   * Parses the computers and the connections from the given input. The connectivity is non-directional.
   *
   * @param input the input
   * @return a map of computers to a set of connected computers
   */
  private Map<String, Set<String>> parseComputers(final String input) {
    final List<String> connections = input.lines().toList();
    final Map<String, Set<String>> computers = new HashMap<>();
    connections.stream()
      // Split each line on '-' to get the two connected computers
      .map(line -> line.split("-"))
      .forEach(ends -> {
        // Merge into the connectivity map the connection A -> B
        computers.merge(
          ends[0],
          new HashSet<>(List.of(ends[1])),
          (oldSet, newSet) -> {
            oldSet.add(ends[1]);
            return oldSet;
          });
        // Merge into the connectivity map the connection B -> A
        computers.merge(
          ends[1],
          new HashSet<>(List.of(ends[0])),
          (oldSet, newSet) -> {
            oldSet.add(ends[0]);
            return oldSet;
          });
      });
    return computers;
  }
}
