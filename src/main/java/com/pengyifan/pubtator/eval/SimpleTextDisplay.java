package com.pengyifan.pubtator.eval;

public class SimpleTextDisplay extends EvalDisplay {

  public SimpleTextDisplay() {
    super(27);
  }

  @Override
  protected void get(Mode mode, ResultPrinter resultPrinter, PubTatorEval eval) {
    switch (mode) {
    case Disease:
      resultPrinter.printRow("Disease");
      resultPrinter.printRow("  Concept id matching", eval.getDiseaseIdStats());
      resultPrinter.printRow("  Mention matching", eval.getDiseaseMentionStats());
      break;
    case Chemical:
      resultPrinter.printRow("Chemical");
      resultPrinter.printRow("  Concept id matching", eval.getChemicalIdStats());
      resultPrinter.printRow("  Mention matching", eval.getChemicalMentionStats());
      break;
    case CID:
      resultPrinter.printRow("CID");
      resultPrinter.printRow("  Concept id matching", eval.getCdrStats());
      resultPrinter.printRow("  Mention matching", eval.getCdrWithMentionStats());
      break;
    }
  }
}
