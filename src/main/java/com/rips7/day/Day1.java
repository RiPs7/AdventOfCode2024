package com.rips7.day;

import com.rips7.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day1 implements Day<Long> {

  @Override
  public Long part1(String input) {
    // Define two queues to keep elements sorted
    final Queue<Long> firstList = new PriorityQueue<>();
    final Queue<Long> secondList = new PriorityQueue<>();

    // Parse numbers from each line and add them to the corresponding queue
    parseLines(input).forEach(parts -> {
      firstList.offer(parts[0]);
      secondList.offer(parts[1]);
    });

    // Calculate all the distances and return their sum
    return IntStream.range(0, firstList.size())
        .mapToObj(i -> Objects.requireNonNull(firstList.poll()) - Objects.requireNonNull(secondList.poll()))
        .map(Math::abs)
        .reduce(Long::sum)
        .orElse(0L);
  }

  @Override
  public Long part2(String input) {
    // Parse numbers from each line into two separate lists
    final List<List<Long>> lists = parseLines(input)
        .collect(Collectors.teeing(
            Collectors.mapping(parts -> parts[0], Collectors.toList()),
            Collectors.mapping(parts -> parts[1], Collectors.toList()),
            List::of));

    // Keep the first list as is, and calculate a frequency map for the second one
    final List<Long> firstList = lists.getFirst();
    final Map<Long, Long> frequencyMap = lists.getLast().stream()
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    // Calculate similarity scores and return their sum
    return firstList.stream()
        .map(element -> element * frequencyMap.getOrDefault(element, 0L))
        .reduce(Long::sum)
        .orElse(0L);
  }

  private Stream<long[]> parseLines(final String input) {
    return Util.lines(input)
        .map(line -> line.split("\\s+"))
        .map(parts -> new long[] { Long.parseLong(parts[0]), Long.parseLong(parts[1]) });
  }

}
