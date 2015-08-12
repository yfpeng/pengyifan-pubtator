package com.pengyifan.pubtator;

import com.google.common.testing.EqualsTester;
import com.pengyifan.bioc.BioCAnnotation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PubTatorAnnotationTest {

  @Test
  public void testEquals() throws Exception {
    PubTatorAnnotation base = new DummyAnnotation("id", "type");
    PubTatorAnnotation baseCopy = new DummyAnnotation("id", "type");
    PubTatorAnnotation diffId = new DummyAnnotation("id2", "type");
    PubTatorAnnotation diffType = new DummyAnnotation("id", "type2");

    new EqualsTester()
        .addEqualityGroup(base, baseCopy)
        .addEqualityGroup(diffId)
        .addEqualityGroup(diffType)
        .testEquals();
  }

  @Test
  public void testGetId() throws Exception {
    PubTatorAnnotation annotation = new DummyAnnotation("id", "type");
    assertEquals("id", annotation.getId());
  }

  @Test
  public void testGetType() throws Exception {
    PubTatorAnnotation annotation = new DummyAnnotation("id", "type");
    assertEquals("type", annotation.getType());
  }

  static private class DummyAnnotation extends PubTatorAnnotation {

    public DummyAnnotation(String id, String type) {
      super(id, type);
    }

    @Override
    public String toPubTatorString() {
      return null;
    }
  }
}