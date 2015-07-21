package com.pengyifan.pubtator.cmd;

import com.google.common.collect.Lists;
import com.pengyifan.bioc.BioCCollection;
import com.pengyifan.bioc.io.BioCCollectionWriter;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.io.PubTatorIO;
import com.pengyifan.pubtator.utils.PubTator2BioC;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Converter {

  @Option(name = "-f", usage = "Specifies the format of input", metaVar = "FORMAT")
  private String from = null;

  @Option(name = "-t", usage = "Specifies the format of output", metaVar = "FORMAT")
  private String to = null;

  @Option(name = "-l", usage = "list all known format sets")
  private boolean list = false;

  @Option(name = "-o", usage = "output to this file", metaVar = "FILE")
  private File out = null;

  @Option(name = "-h", usage = "Give this help list")
  private boolean help = false;

  @Argument
  private List<String> arguments = Lists.newArrayList();

  public static void main(String[] args) throws IOException, XMLStreamException {
    new Converter().doMain(args);
  }

  private void printHelp(CmdLineParser parser) {
    System.err.println("Usage: java Converter [OPTION... ] [FILE...]");
    System.err.println("Convert given files from one format to another.");
    System.err.println("The result is written to standard output unless otherwise specified by the -o option.");
    System.err.println();
    parser.printUsage(System.err);
    System.err.println();
  }

  public void doMain(String[] args) throws IOException, XMLStreamException {
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

    if (list) {
      System.err.println("The following list contains all the format sets known.");
      System.err.println("pubtator, bioc");
      return;
    }

    if (from == null) {
      System.err.println("Missing -f");
      printHelp(parser);
      return;
    } else if (!checkFormat(from)) {
      System.err.printf("Conversion from `%s` is not supported\n", from);
      printHelp(parser);
      return;
    }

    if (to == null) {
      System.err.println("Missing -t");
      printHelp(parser);
      return;
    } else if (!checkFormat(to)) {
      System.err.printf("Conversion to `%s` is not supported\n", to);
      printHelp(parser);
      return;
    }

    List<PubTatorDocument> documents = Lists.newArrayList();
    for(String argument: arguments) {
      Path file = Paths.get(argument);
      switch(from) {
      case "pubtator":
        documents.addAll(PubTatorIO.readPubTatorFormat(Files.newBufferedReader(file)));
        break;
      case "bioc":
        documents.addAll(PubTatorIO.readBioCFormat(Files.newBufferedReader(file)));
        break;
      }
    }

    Writer writer;
    if (out == null) {
      writer = new PrintWriter(System.out);
    } else {
      writer = new FileWriter(out);
    }
    switch (to) {
    case "pubtator":
      PubTatorIO.write(writer, documents);
      break;
    case "bioc":
      PubTator2BioC p2b = new PubTator2BioC();
      BioCCollection collection = new BioCCollection();
      collection.setKey("PubTator.key");
      collection.setSource("PubTator");
      documents.stream().forEach(d -> collection.addDocument(p2b.apply(d)));
      BioCCollectionWriter w = new BioCCollectionWriter(writer);
      w.writeCollection(collection);
      w.close();
      break;
    }
  }

  private boolean checkFormat(String format) {
    return format.equals("pubtator") || format.equals("bioc");
  }
}
