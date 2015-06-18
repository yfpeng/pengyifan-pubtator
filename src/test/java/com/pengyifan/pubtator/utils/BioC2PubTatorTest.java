package com.pengyifan.pubtator.utils;

import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

public class BioC2PubTatorTest {

  @Test
  public void testConvert() throws Exception {
    Path path = getReader("cdr-sample1.xml");
  }

  private Path getReader(String filename) throws URISyntaxException {
    assertNotNull("Test file missing", getClass().getResource("/" + filename));
    return Paths.get(getClass().getResource("/" + filename).toURI());
  }
}