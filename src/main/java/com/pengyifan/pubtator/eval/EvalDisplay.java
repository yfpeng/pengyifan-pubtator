package com.pengyifan.pubtator.eval;

public abstract class EvalDisplay {

  enum Mode {
    Disease, Chemical, CID;
  }

  private int firstColumnWidth;

  EvalDisplay(int firstColumnWidth) {
    this.firstColumnWidth = firstColumnWidth;
  }

  public String getDiseaseResult(PubTatorEval eval) {
    ResultPrinter resultPrinter = new ResultPrinter(firstColumnWidth);
    resultPrinter.printTitle();
    get(Mode.Disease, resultPrinter, eval);
    return resultPrinter.toString();
  }

  public String getNERResult(PubTatorEval eval) {
    ResultPrinter resultPrinter = new ResultPrinter(firstColumnWidth);
    resultPrinter.printTitle();
    get(Mode.Disease, resultPrinter, eval);
    get(Mode.Chemical, resultPrinter, eval);
    return resultPrinter.toString();
  }

  public String getCIDResult(PubTatorEval eval) {
    ResultPrinter resultPrinter = new ResultPrinter(firstColumnWidth);
    resultPrinter.printTitle();
    get(Mode.CID, resultPrinter, eval);
    return resultPrinter.toString();
  }

  public String getChemicalResult(PubTatorEval eval) {
    ResultPrinter resultPrinter = new ResultPrinter(firstColumnWidth);
    resultPrinter.printTitle();
    get(Mode.Chemical, resultPrinter, eval);
    return resultPrinter.toString();
  }

  public String getAllResult(PubTatorEval eval) {
    ResultPrinter resultPrinter = new ResultPrinter(firstColumnWidth);
    resultPrinter.printTitle();
    get(Mode.Disease, resultPrinter, eval);
    get(Mode.Chemical, resultPrinter, eval);
    get(Mode.CID, resultPrinter, eval);
    return resultPrinter.toString();
  }

  protected abstract void get(Mode mode, ResultPrinter resultPrinter, PubTatorEval eval);
}
