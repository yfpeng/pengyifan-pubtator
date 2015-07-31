package com.pengyifan.pubtator.utils;

import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.pubtator.PubTatorDocument;

import java.util.function.Function;

public class PubTator2BioC implements Function<PubTatorDocument, BioCDocument> {
  @Override
  public BioCDocument apply(PubTatorDocument pubTatorDocument) {
    return pubTatorDocument.getBioCDocument();
  }
}
