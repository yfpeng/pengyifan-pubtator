package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;

public class PubTatorMentionAnnotation extends PubTatorAnnotation {

  final BioCAnnotation bioCAnnotation;

  public PubTatorMentionAnnotation(BioCDocument bioCDocument, BioCAnnotation bioCAnnotation) {
    super(bioCDocument);
    this.bioCAnnotation = bioCAnnotation;
  }

  public String getText() {
    return bioCAnnotation.getText().get();
  }

  public int getEnd() {
    BioCLocation bioCLocation = bioCAnnotation.getTotalLocation();
    return bioCLocation.getOffset() + bioCLocation.getLength();
  }

  public int getStart() {
    return bioCAnnotation.getTotalLocation().getOffset();
  }

  public String getType() {
    return bioCAnnotation.getInfon("type").get();
  }

  public Set<String> getConceptIds() {
    return Sets.newHashSet(Splitter.on('|').split(bioCAnnotation.getInfon("MESH").get()));
  }

  public String getComment() {
    return bioCAnnotation.getInfon("comment").orElse(null);
  }

  @Override
  public String toPubTatorString() {
    return Joiner.on("\t").skipNulls().join(
        getId(),
        getStart(),
        getEnd(),
        getText(),
        getType(),
        Joiner.on('|').join(getConceptIds().stream().sorted().toArray()),
        getComment());
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", getId())
        .append("type", getType())
        .append("start", getStart())
        .append("end", getEnd())
        .append("text", getText())
        .append("conceptId", Joiner.on('|').join(getConceptIds()))
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
    PubTatorMentionAnnotation rhs = (PubTatorMentionAnnotation) obj;
    return new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(this.getText(), rhs.getText())
        .append(this.getType(), rhs.getType())
        .append(this.getStart(), rhs.getStart())
        .append(this.getEnd(), rhs.getEnd())
        .append(this.getConceptIds(), rhs.getConceptIds())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .appendSuper(super.hashCode())
        .append(getText())
        .append(getType())
        .append(getStart())
        .append(getEnd())
        .append(getConceptIds())
        .toHashCode();
  }
}
