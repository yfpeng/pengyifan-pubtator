package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCRelation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PubTatorRelationAnnotation extends PubTatorAnnotation {

  final BioCRelation bioCRelation;

  public PubTatorRelationAnnotation(BioCDocument bioCDocument, BioCRelation bioCRelation) {
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

  public String getComment() {
    return bioCRelation.getInfon("comment").orElse(null);
  }

  @Override
  public String toPubTatorString() {
    return Joiner.on("\t").skipNulls().join(
        getId(),
        getType(),
        getConceptId1(),
        getConceptId2(),
        getComment());
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", getId())
        .append("type", getType())
        .append("conceptId1", getConceptId1())
        .append("conceptId2", getConceptId2())
        .append("comment", getComment())
        .toString();
  }

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
    PubTatorRelationAnnotation rhs = (PubTatorRelationAnnotation) obj;
    return new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(this.getType(), rhs.getType())
        .append(this.getConceptId1(), rhs.getConceptId1())
        .append(this.getConceptId2(), rhs.getConceptId2())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .appendSuper(super.hashCode())
        .append(getType())
        .append(getConceptId1())
        .append(getConceptId2())
        .toHashCode();
  }
}
