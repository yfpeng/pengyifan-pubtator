package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PubTatorRelationAnnotation extends PubTatorAnnotation {

  private String conceptId1;
  private String conceptId2;

  public PubTatorRelationAnnotation(String id, String type, String conceptId1, String conceptId2) {
    super(id, type);
    this.conceptId1 = conceptId1;
    this.conceptId2 = conceptId2;
  }

  public String getConceptId1() {
    return conceptId1;
  }

  public void setConceptId1(String conceptId1) {
    this.conceptId1 = conceptId1;
  }

  public String getConceptId2() {
    return conceptId2;
  }

  public void setConceptId2(String conceptId2) {
    this.conceptId2 = conceptId2;
  }

  @Override
  public String toPubTatorString() {
    return Joiner.on("\t").join(getId(), getType(), conceptId1, conceptId2);
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
        .append(this.conceptId1, rhs.conceptId1)
        .append(this.conceptId2, rhs.conceptId2)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .appendSuper(super.hashCode())
        .append(conceptId1)
        .append(conceptId2)
        .toHashCode();
  }
}
