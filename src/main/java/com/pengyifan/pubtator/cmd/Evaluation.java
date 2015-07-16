package com.pengyifan.pubtator.cmd;

import com.google.common.collect.Lists;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.eval.DetailedTextDisplay;
import com.pengyifan.pubtator.io.PubTatorIO;
import com.pengyifan.pubtator.eval.PubTatorEval;
import org.kohsuke.args4j.*;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Evaluation {
  @Option(name = "-h", usage = "Give this help list")
  private boolean help = false;

  @Option(name = "-f", usage = "Specifies the format of input", metaVar = "FORMAT")
  private String format = null;

  @Option(name = "-t", usage = "Specifies the type of evaluation. dner, cid, or all (default)",
      metaVar = "TYPE")
  private String type = "all";

  @Argument
  private List<String> arguments = Lists.newArrayList();

  public static void main(String[] args) throws IOException, XMLStreamException, SAXException {
    new Evaluation().doMain(args);
  }

  private void printHelp(CmdLineParser parser) {
    System.err.println("Usage: java Evaluation GOLD_FILE PRED_FILE");
    System.err.println("Evaluate the predicated files using the golden file.");
    System.err.println();
    parser.printUsage(System.err);
    System.err.println();
  }

  private void doMain(String[] args) throws IOException, XMLStreamException, SAXException {
    ParserProperties properties = ParserProperties.defaults()
        .withUsageWidth(80);
    CmdLineParser parser = new CmdLineParser(this, properties);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      printHelp(parser);
    }

    if (help) {
      printHelp(parser);
      return;
    }

    switch (format) {
    case "bioc":
    case "pubtator":
      break;
    default:
      System.err.printf("Format `%s` is not supported\n", format);
      printHelp(parser);
      return;
    }

    switch (type) {
    case "dner":
    case "cid":
    case "all":
      break;
    default:
      System.err.printf("Type `%s` is not supported\n", type);
      printHelp(parser);
      return;
    }

    if (arguments.size() != 2) {
      System.err.println("Only two files are supported");
      printHelp(parser);
      return;
    }

    List<PubTatorDocument> goldDocuments = null;
    List<PubTatorDocument> predDocuments = null;

    switch (format) {
    case "bioc":
      goldDocuments = PubTatorIO.readBioCFormat(
          Files.newBufferedReader(Paths.get(arguments.get(0))));
      predDocuments = PubTatorIO.readBioCFormat(
          Files.newBufferedReader(Paths.get(arguments.get(1))));
      break;
    case "pubtator":
      goldDocuments = PubTatorIO.readPubTatorFormat(
          Files.newBufferedReader(Paths.get(arguments.get(0))));
      predDocuments = PubTatorIO.readPubTatorFormat(
          Files.newBufferedReader(Paths.get(arguments.get(1))));
      break;
    default:
      return;
    }

    PubTatorEval eval = new PubTatorEval(goldDocuments, predDocuments);
    eval.eval();

    int mode = 2;
    switch (type) {
    case "dner":
      mode = 0;
      break;
    case "cid":
      mode = 1;
      break;
    }
    System.out.println(eval.getResult(mode, new DetailedTextDisplay()));
  }
}
