package com.pengyifan.pubtator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class PubTatorAnnotation {
  private String id;
  private String type;

  public PubTatorAnnotation(String id, String type) {
    this.id = id;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
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
        .append(this.id, rhs.id)
        .append(this.type, rhs.type)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(id)
        .append(type)
        .toHashCode();
  }
}
