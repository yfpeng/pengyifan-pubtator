package com.pengyifan.pubtator.io;

import com.pengyifan.pubtator.PubTatorDocument;
import junit.framework.TestCase;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class PubTatorLoader2Test extends TestCase {

  private static String line1 = "26094\tCID\tD008750\tD003866";

  private static String line2 = "26094|t|Antihypertensive drugs and depression: a reappraisal.";

  public void testError1() throws Exception {
    PubTatorLoader2 loader = new PubTatorLoader2(new StringReader(line1));
    List<PubTatorDocument> pDocs = loader.read();
    loader.close();

    if (loader.hasErrors()) {
      System.out.println(loader.getErrorMessage());
    }

    for (PubTatorDocument pDoc : pDocs) {
      System.out.println(pDoc.toPubTatorString());
      System.out.println();
    }
  }

  public void testError2() throws Exception {
    PubTatorLoader2 loader = new PubTatorLoader2(new StringReader(line2));
    List<PubTatorDocument> pDocs = loader.read();
    loader.close();

    if (loader.hasErrors()) {
      System.out.println(loader.getErrorMessage());
    }

    for (PubTatorDocument pDoc : pDocs) {
      System.out.println(pDoc.toPubTatorString());
      System.out.println();
    }
  }

  public void testRead() throws Exception {
    Path path = getReader("cdr-sample1.txt");
    Reader reader = new FileReader(path.toFile());
    PubTatorLoader2 loader = new PubTatorLoader2(reader);
    List<PubTatorDocument> pDocs = loader.read();
    loader.close();

    for (PubTatorDocument pDoc : pDocs) {
      System.out.println(pDoc.toPubTatorString());
      System.out.println();
    }
  }

  private Path getReader(String filename) throws URISyntaxException {
    assertNotNull("Test file missing", getClass().getResource("/" + filename));
    return Paths.get(getClass().getResource("/" + filename).toURI());
  }
}