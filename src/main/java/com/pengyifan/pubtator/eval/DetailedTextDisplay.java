package com.pengyifan.pubtator.eval;

public class DetailedTextDisplay extends EvalDisplay {
  public DetailedTextDisplay() {
    super(27);
  }

  @Override
  protected void get(Mode mode, ResultPrinter resultPrinter, PubTatorEval eval) {
    switch (mode) {
    case Disease:
      resultPrinter.printRow("Disease");
      resultPrinter.printRow("  Concept id matching", eval.getDiseaseIdStats());
      resultPrinter.printRow("  Mention (Strict matching)", eval.getDiseaseMentionStats());
      resultPrinter.printRow("  Mention (Appro. matching)", eval.getDiseaseApproMentionStats());
      break;
    case Chemical:
      resultPrinter.printRow("Chemical");
      resultPrinter.printRow("  Concept id matching", eval.getChemicalIdStats());
      resultPrinter.printRow("  Mention (Strict matching)", eval.getChemicalMentionStats());
      resultPrinter.printRow("  Mention (Appro. matching)", eval.getChemicalApproMentionStats());
      break;
    case CID:
      resultPrinter.printRow("CID");
      resultPrinter.printRow("  Concept id matching", eval.getCdrStats());
      resultPrinter.printRow("  Mention (Strict matching)", eval.getCdrWithMentionStats());
      resultPrinter.printRow("  Mention (Appro matching)", eval.getCdrWithApproMentionStats());
      break;
    }
  }
}
