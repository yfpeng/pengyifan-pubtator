package com.pengyifan.pubtator.io;

import com.pengyifan.pubtator.PubTatorDocument;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class PubTatorIOTest {

  @Test
  public void testReadBioCFormat() throws Exception {
    Path path = getReader("cdr-sample1.xml");
    Reader reader = new FileReader(path.toFile());
    List<PubTatorDocument> pDocs = PubTatorIO.readBioCFormat(reader);
    for (PubTatorDocument pDoc : pDocs) {
      System.out.println(pDoc.toPubTatorString());
      System.out.println();
    }
  }

  @Test
  public void testReadPubTatorFormat() throws Exception {
    Path path = getReader("cdr-sample1.txt");
    Reader reader = new FileReader(path.toFile());
    List<PubTatorDocument> pDocs = PubTatorIO.readPubTatorFormat(reader);
    for(PubTatorDocument pDoc: pDocs) {
      System.out.println(pDoc.toPubTatorString());
      System.out.println();
    }
  }

  private Path getReader(String filename) throws URISyntaxException {
    assertNotNull("Test file missing", getClass().getResource("/" + filename));
    return Paths.get(getClass().getResource("/" + filename).toURI());
  }
}
