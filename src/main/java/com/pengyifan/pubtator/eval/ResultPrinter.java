package com.pengyifan.pubtator.eval;

import com.google.common.base.Strings;
import com.pengyifan.commons.math.PrecisionRecallStats;

import java.text.DecimalFormat;

public class ResultPrinter {

  private final String format;
  private final String header;
  private final String headerLine;

  private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat(
      "00.00");
  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat(
      "#,###");

  private final StringBuilder display;


  public ResultPrinter() {
    this(25);
  }

  public ResultPrinter(int firstColumnWidth) {
    display = new StringBuilder();
    format = "%-" + firstColumnWidth + "s %7s (%5s) %7s (%5s) %7s %7s %7s";
    header = String.format(
        format, "Class", "gold", "match", "answer", "match", "recall",
        "prec.", "fscore");
    headerLine = Strings.repeat("-", header.length());
  }

  public void printTitle() {
    display.append(header).append(System.lineSeparator());
    display.append(headerLine).append(System.lineSeparator());
  }

  public void printRow(String type) {
    display.append(type).append(System.lineSeparator());
  }

  public void printRow(String type, PrecisionRecallStats stats) {
    String row = String.format(
        format,
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

  public static String getInteger(int integer) {
    return INTEGER_FORMAT.format(integer);
  }

  public static String getPercentage(double percentage) {
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
