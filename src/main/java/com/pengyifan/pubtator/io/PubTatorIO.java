package com.pengyifan.pubtator.io;

import com.google.common.base.Joiner;
import com.pengyifan.pubtator.PubTatorDocument;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class PubTatorIO {

  public static List<PubTatorDocument> readBioCFormat(Reader reader)
      throws XMLStreamException, IOException {
    return new BioCLoader().load(reader);
  }

  public static List<PubTatorDocument> readPubTatorFormat(Reader reader)
      throws IOException {
    return new PubTatorLoader().load(reader);
  }

  public static String toPubTatorString(List<PubTatorDocument> documentList) {
    return Joiner.on("\n\n").join(documentList);
  }

  public static void write(Writer writer, List<PubTatorDocument> documentList)
      throws IOException {
    writer.write(Joiner.on("\n\n").join(documentList));
    writer.flush();
  }
}
