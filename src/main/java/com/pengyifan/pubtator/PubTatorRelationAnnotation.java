package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.pengyifan.bioc.BioCAnnotation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PubTatorRelationAnnotation extends PubTatorAnnotation {

  PubTatorRelationAnnotation(BioCAnnotation bioCAnnotation) {
    super(bioCAnnotation);
  }

  public String getConceptId1() {
    return bioCAnnotation.getInfon("conceptId1").get();
  }

  public String getConceptId2() {
    return bioCAnnotation.getInfon("conceptId2").get();
  }

  public String getType() {
    return bioCAnnotation.getInfon("relation").get();
  }

  @Override
  public String toPubTatorString() {
    return Joiner.on("\t").join(getId(), getType(), getConceptId1(), getConceptId2());
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", getId())
        .append("type", getType())
        .append("conceptId1", getConceptId1())
        .append("conceptId2", getConceptId2())
        .toString();
  }
}
