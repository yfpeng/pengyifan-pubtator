package com.pengyifan.pubtator.utils;

import com.google.common.collect.Sets;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;
import com.pengyifan.pubtator.io.PubTatorIO;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class PubTatorMergeTest {

  private static final PubTatorMentionAnnotation expectedMention1 =
      new PubTatorMentionAnnotation("26094", "Disease", 287, 297, "depression",
          Sets.newHashSet("D003866"));

  private static final PubTatorRelationAnnotation expected =
      new PubTatorRelationAnnotation("26094", "CID", "D008750", "D003866");

  @Test
  public void testGetDoc() throws Exception {
    PubTatorMerge merge = new PubTatorMerge();
    add("cdr-test1.txt", merge);
    add("cdr-test2.txt", merge);
    add("cdr-test3.txt", merge);

    PubTatorDocument document = merge.getDoc();
    assertEquals("26094", document.getId());
    assertEquals("Antihypertensive drugs and depression: a reappraisal.", document.getTitle());

    List<PubTatorMentionAnnotation> mentions = document.getMentions();
    assertEquals(8, mentions.size());
    assertThat(mentions, hasItem(expectedMention1));

    List<PubTatorRelationAnnotation> relations = document.getRelations();
    assertEquals(1, relations.size());
    assertThat(relations, hasItem(expected));
  }

  private void add(String filename, PubTatorMerge merge) throws URISyntaxException, IOException {
    Path p = getReader(filename);
    try {
      List<PubTatorDocument> pDocs = PubTatorIO.readPubTatorFormat(Files.newBufferedReader(p));
      merge.addDocument(pDocs.get(0));
    } catch (Exception e) {
      System.err.printf("Cannot parse file: %s\n", filename);
      e.printStackTrace();
    }
  }

  private Path getReader(String filename) throws URISyntaxException {
    URL url = getClass().getResource("/" + filename);
    assertNotNull("Test file missing", url);
    return Paths.get(getClass().getResource("/" + filename).toURI());
  }
}