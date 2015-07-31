package com.pengyifan.pubtator;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;

public abstract class PubTatorAnnotation {

  final BioCDocument bioCDocument;


  public PubTatorAnnotation(BioCDocument bioCDocument) {
    this.bioCDocument = bioCDocument;
  }

  public String getId() {
    return bioCDocument.getID();
  }

  public abstract String toPubTatorString(String docId);

}
