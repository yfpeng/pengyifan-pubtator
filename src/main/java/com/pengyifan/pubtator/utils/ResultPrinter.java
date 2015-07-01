package com.pengyifan.pubtator.utils;

import com.google.common.base.Strings;

import java.text.DecimalFormat;

public class ResultPrinter {

  private static final String FORMAT = "%-31s %7s (%5s) %7s (%5s) %7s %7s %7s";
  private static final String HEADER = String.format(
      FORMAT, "Class", "gold", "match", "answer", "match", "recall",
      "prec.", "fscore");
  private static final String HEADER_LINE = Strings
      .repeat("-", HEADER.length());

  private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat(
      "00.00");
  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat(
      "#,###");

  private final StringBuilder display;


  public ResultPrinter() {
    display = new StringBuilder();
  }

  public void printTitle() {
    display.append(HEADER).append(System.lineSeparator());
    display.append(HEADER_LINE).append(System.lineSeparator());
  }

  public void printRow(String type) {
    display.append(type).append(System.lineSeparator());
  }

  public void printRow(String type, PrecisionRecallStats stats) {
    String row = String.format(
        FORMAT,
        type,
        getInteger(stats.getTP() + stats.getFN()),
        getInteger(stats.getTP()),
        getInteger(stats.getTP() + stats.getFP()),
        getInteger(stats.getTP()),
        getPercentage(stats.getRecall() * 100),
        getPercentage(stats.getPrecision() * 100),
        getPercentage(stats.getFMeasure() * 100));
    display.append(row).append(System.lineSeparator());
  }

  private String getInteger(int integer) {
    return INTEGER_FORMAT.format(integer);
  }

  private String getPercentage(double percentage) {
    if (Double.isNaN(percentage)) {
      return "--";
    } else {
      return PERCENTAGE_FORMAT.format(percentage);
    }
  }

  @Override
  public String toString() {
    return display.toString();
  }
}
