package com.pengyifan.pubtator;

import com.pengyifan.bioc.BioCDocument;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class PubTatorAnnotation {

  final BioCDocument bioCDocument;


  public PubTatorAnnotation(BioCDocument bioCDocument) {
    this.bioCDocument = bioCDocument;
  }

  public String getId() {
    return bioCDocument.getID();
  }

  public abstract String toPubTatorString();

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    PubTatorAnnotation rhs = (PubTatorAnnotation) obj;
    return new EqualsBuilder()
        .append(this.getId(), rhs.getId())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(getId())
        .toHashCode();
  }
}
