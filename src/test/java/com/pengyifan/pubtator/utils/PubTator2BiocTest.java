package com.pengyifan.pubtator.utils;

import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.io.PubTatorIO;
import org.junit.Test;

import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PubTator2BiocTest {

  private static final PubTator2BioC converter = new PubTator2BioC();

  @Test
  public void testApply() throws Exception {
    Path path = getReader("cdr-sample1.txt");
    List<PubTatorDocument> pDocs = PubTatorIO.readPubTatorFormat(new FileReader(path.toFile()));
    List<BioCDocument> bDocs = pDocs.stream()
        .map(converter::apply)
        .collect(Collectors.toList());
    assertEquals(2, bDocs.size());

    BioCDocument bDoc1 = bDocs.get(0);
    assertEquals("26094", bDoc1.getID());
    assertEquals(1, bDoc1.getRelations().size());

    BioCDocument bDoc2 = bDocs.get(1);
    assertEquals("3403780", bDoc2.getID());
    assertEquals(3, bDoc2.getRelations().size());
  }

  private Path getReader(String filename) throws URISyntaxException {
    URL url = getClass().getResource("/" + filename);
    assertNotNull("Test file missing", url);
    return Paths.get(getClass().getResource("/" + filename).toURI());
  }
}