package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCRelation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PubTatorRelationAnnotation extends PubTatorAnnotation {

  final BioCRelation bioCRelation;

  PubTatorRelationAnnotation(BioCDocument bioCDocument, BioCRelation bioCRelation) {
    super(bioCDocument);
    this.bioCRelation = bioCRelation;
  }

  public String getConceptId1() {
    return bioCRelation.getInfon("Chemical").get();
  }

  public String getConceptId2() {
    return bioCRelation.getInfon("Disease").get();
  }

  public String getType() {
    return bioCRelation.getInfon("relation").get();
  }

  @Override
  public String toPubTatorString(String docId) {
    return Joiner.on("\t").join(docId, getType(), getConceptId1(), getConceptId2());
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", getId())
        .append("type", getType())
        .append("chemical", getConceptId1())
        .append("disease", getConceptId2())
        .toString();
  }
}
