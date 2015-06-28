package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCLocation;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;

public class PubTatorMentionAnnotation extends PubTatorAnnotation {

  PubTatorMentionAnnotation(BioCAnnotation bioCAnnotation) {
    super(bioCAnnotation);
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

  @Override
  public String toPubTatorString() {
    return Joiner.on("\t").skipNulls().join(
        getId(),
        getStart(),
        getEnd(),
        getText(),
        getType(),
        bioCAnnotation.getInfon("MESH").get(),
        bioCAnnotation.getInfon("comment").orElse(null));
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", getId())
        .append("type", getType())
        .append("start", getStart())
        .append("end", getEnd())
        .append("text", getText())
        .append("conceptId", bioCAnnotation.getInfon("MESH").get())
        .toString();
  }
}
