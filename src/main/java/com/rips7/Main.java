package com.rips7;

import com.rips7.day.Day;
import com.rips7.util.Util;
import com.rips7.util.Util.AnsiColor;
import com.rips7.util.Util.TimedResult;

import static com.rips7.day.AllDays.getAllDays;
import static com.rips7.util.Util.printColor;
import static com.rips7.util.Util.time;

public class Main {

  public static void main(String[] args) {
    TimedResult<?> result = time(() -> {
      getAllDays().forEach(Day::run);
      return null;
    });
    printColor("\nRan all days\n", Util.AnsiColor.GREEN);
    printColor(result.timeInfo(), AnsiColor.YELLOW);
  }

}
