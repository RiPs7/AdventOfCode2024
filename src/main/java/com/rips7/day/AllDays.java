package com.rips7.day;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

public class AllDays {

  public static Stream<Day<?>> getAllDays() {
    return Stream.of(
        Day1.class,
        Day2.class,
        Day3.class,
        Day4.class,
        Day5.class,
        Day6.class,
        Day7.class,
        Day8.class,
        Day9.class,
        Day10.class,
        Day11.class,
        Day12.class,
        Day13.class,
        Day14.class,
        Day15.class,
        Day16.class,
        Day17.class,
        Day18.class,
        Day19.class,
        Day20.class,
        Day21.class,
        Day22.class,
        Day23.class,
        Day24.class,
        Day25.class)
      .map(clazz -> {
        try {
          return clazz.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      });
  }
}
