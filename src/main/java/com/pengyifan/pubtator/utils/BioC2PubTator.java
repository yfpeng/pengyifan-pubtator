package com.pengyifan.pubtator.utils;

import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.pubtator.PubTatorDocument;

import java.util.function.Function;

public class BioC2PubTator implements Function<BioCDocument, PubTatorDocument> {

  @Override
  public PubTatorDocument apply(BioCDocument bioCDocument) {
    return new PubTatorDocument(bioCDocument);
  }
}
