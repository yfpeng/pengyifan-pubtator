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
    PubTatorLoader loader = new PubTatorLoader(reader);
    List<PubTatorDocument> documents = loader.read();
    loader.close();

    if (loader.hasErrors()) {
      throw new IOException(loader.getErrorMessage());
    }
    return documents;
  }

  public static String toPubTatorString(List<PubTatorDocument> documentList) {
    return Joiner.on("\n\n").join(documentList);
  }

  public static void write(Writer writer, List<PubTatorDocument> documentList)
      throws IOException {
    writer.write(Joiner.on("\n\n").join(documentList));
    writer.flush();
  }

  public static String finalizeConceptId(String conceptId) {
    if (conceptId == null || conceptId.length() == 0 || conceptId.equals("-1")) {
      return "-1";
    }
    int col = conceptId.indexOf(':');
    if (col != -1) {
      conceptId = conceptId.substring(col + 1);
    }
    return conceptId;
  }
}
