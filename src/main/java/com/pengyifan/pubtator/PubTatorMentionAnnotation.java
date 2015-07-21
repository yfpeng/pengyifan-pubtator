package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;

public class PubTatorMentionAnnotation extends PubTatorAnnotation {

  private String text;
  private int start;
  private int end;
  private Set<String> conceptIds;
  private String comment;

  public PubTatorMentionAnnotation(String id, String type, int start, int end, String text,
      Set<String> conceptIds) {
    super(id, type);
    this.start = start;
    this.end = end;
    this.text = text;
    this.conceptIds = conceptIds;
  }

  public PubTatorMentionAnnotation(String id, String type, int start, int end, String text,
      Set<String> conceptIds, String comment) {
    super(id, type);
    this.start = start;
    this.end = end;
    this.text = text;
    this.conceptIds = conceptIds;
    this.comment = comment;
  }

  public String getText() {
    return text;
  }

  public int getEnd() {
    return end;
  }

  public int getStart() {
    return start;
  }

  public Set<String> getConceptIds() {
    return conceptIds;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public String toPubTatorString() {
    return Joiner.on("\t").skipNulls().join(getId(), start, end, text, getType(),
        Joiner.on('|').join(getConceptIds().stream().sorted().toArray()), comment);
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
        .append(this.text, rhs.text)
        .append(this.start, rhs.start)
        .append(this.end, rhs.end)
        .append(this.conceptIds, rhs.conceptIds)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .appendSuper(super.hashCode())
        .append(text)
        .append(start)
        .append(end)
        .append(conceptIds)
        .toHashCode();
  }
}
