package com.pengyifan.pubtator;

import com.pengyifan.bioc.BioCAnnotation;

public abstract class PubTatorAnnotation {

  final BioCAnnotation bioCAnnotation;

  public PubTatorAnnotation(BioCAnnotation bioCAnnotation) {
    this.bioCAnnotation = bioCAnnotation;
  }

  public String getId() {
    return bioCAnnotation.getID();
  }

  public abstract String toPubTatorString();

}
