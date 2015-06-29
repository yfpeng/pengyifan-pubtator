package com.pengyifan.pubtator.utils;

import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.io.PubTatorIO;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BioC2PubTatorTest {

  private static final String ID0 = "26094";
  private static final String TITLE0 = "Antihypertensive drugs and depression: a reappraisal.";

  private static final String ID1 = "3403780";
  private static final String TITLE1 = "Paracetamol-associated coma, metabolic acidosis, renal and hepatic failure.";

  @Test
  public void testApply() throws Exception {
    Path path = getReader("cdr-sample1.xml");
    Reader reader = new FileReader(path.toFile());
    List<PubTatorDocument> pDocs = PubTatorIO.readBioCFormat(reader);

    assertEquals(2, pDocs.size());
    PubTatorDocument pDoc0 = pDocs.get(0);
    assertEquals(ID0, pDoc0.getId());
    assertEquals(TITLE0, pDoc0.getTitle());

    PubTatorDocument pDoc1 = pDocs.get(1);
    assertEquals(ID1, pDoc1.getId());
    assertEquals(TITLE1, pDoc1.getTitle());
  }

  private Path getReader(String filename) throws URISyntaxException {
    assertNotNull("Test file missing", getClass().getResource("/" + filename));
    return Paths.get(getClass().getResource("/" + filename).toURI());
  }
}