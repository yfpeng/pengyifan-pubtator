package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class PubTatorDocument {

  private CoreMap map;

  public PubTatorDocument() {
    map = new ArrayCoreMap();
    map.set(MentionsAnnotation.class, Lists.newArrayList());
    map.set(RelationsAnnotation.class, Lists.newArrayList());
  }

  public void addAnnotation(PubTatorAnnotation annotation) {
    if (annotation instanceof PubTatorMentionAnnotation) {
      getMentions().add((PubTatorMentionAnnotation) annotation);
    } else if (annotation instanceof PubTatorRelationAnnotation) {
      getRelations().add((PubTatorRelationAnnotation) annotation);
    } else {
      throw new IllegalArgumentException(String.format("Cannot handle annotation type: %s",
          annotation.getClass()));
    }
  }

  public List<PubTatorMentionAnnotation> getMentions(String conceptId) {
    return getMentions().stream()
        .filter(m -> m.getConceptIds().contains(conceptId))
        .collect(Collectors.toList());
  }

  public String getText() {
    return getTitle() + "\n" + getAbstract();
  }

  public String toPubTatorString() {
    StringJoiner sj = new StringJoiner("\n");
    sj.add(Joiner.on("|").join(getId(), "t", getTitle()));
    if (getAbstract() != null) {
      sj.add(Joiner.on("|").join(getId(), "a", getAbstract()));
    }
    getMentions().stream()
        .sorted((m1, m2) -> Integer.compare(m1.getStart(), m2.getStart()))
        .forEach(m -> sj.add(m.toPubTatorString()));

    getRelations().stream()
        .sorted((r1, r2) -> r1.getId().compareTo(r2.getId()))
        .forEach(r -> sj.add(r.toPubTatorString()));
    return sj.toString();
  }

  @Override
  public String toString() {
    return toPubTatorString();
  }

  public void setId(String id) {
    map.set(IdAnnotation.class, id);
  }

  public String getId() {
    return map.get(IdAnnotation.class);
  }

  public void setTitle(String title) {
    map.set(TitleAnnotation.class, title);
  }

  public void setAbstract(String a) {
    map.set(AbstractAnnotation.class, a);
  }

  public String getTitle() {
    return map.get(TitleAnnotation.class);
  }

  public String getAbstract() {
    return map.get(AbstractAnnotation.class);
  }

  public List<PubTatorMentionAnnotation> getMentions() {
    return map.get(MentionsAnnotation.class);
  }

  public List<PubTatorRelationAnnotation> getRelations() {
    return map.get(RelationsAnnotation.class);
  }

  public void checkValidation() {
    checkNotNull(getId(), "ID is null");
    checkNotNull(getAbstract(), "abstract is null");
    checkNotNull(getTitle(), "title is null");

    for(PubTatorMentionAnnotation mention: getMentions()) {
      checkArgument(mention.getId().equals(getId()),
          "Mention ID[%s] is different from doc ID[%s]: %s, %s", mention.getId(), getId());
      checkNotNull(mention.getType(), "Mention type is null: %s", mention);

      String actualText = mention.getText();
      String expectedText = getText().substring(mention.getStart(), mention.getEnd());
      checkArgument(expectedText.equals(actualText), "Text mismatch. Expected[%s], actual[%s]",
          expectedText, actualText);
    }

    for(PubTatorRelationAnnotation relation: getRelations()) {
      checkArgument(relation.getId().equals(getId()),
          "Relation ID[%s] is different from doc ID[%s]: %s, %s", relation.getId(), getId());
      checkNotNull(relation.getType(), "Relation type is null: %s", relation);

      String conceptId1 = relation.getConceptId1();
      checkArgument(!getMentions(conceptId1).isEmpty(),
          "Cannot find concept [%s] in the document.", conceptId1);
      String conceptId2 = relation.getConceptId2();
      checkArgument(!getMentions(conceptId2).isEmpty(),
          "Cannot find concept [%s] in the document.", conceptId2);
    }
  }

  private class AbstractAnnotation implements CoreAnnotation<String> {

    @Override
    public Class<String> getType() {
      return String.class;
    }
  }

  private class TitleAnnotation implements CoreAnnotation<String> {

    @Override
    public Class<String> getType() {
      return String.class;
    }
  }

  private class MentionsAnnotation implements CoreAnnotation<List<PubTatorMentionAnnotation>> {

    @Override
    public Class<List<PubTatorMentionAnnotation>> getType() {
      return ErasureUtils.<Class<List<PubTatorMentionAnnotation>>>uncheckedCast(List.class);
    }
  }

  private class RelationsAnnotation implements CoreAnnotation<List<PubTatorRelationAnnotation>> {

    @Override
    public Class<List<PubTatorRelationAnnotation>> getType() {
      return ErasureUtils.<Class<List<PubTatorRelationAnnotation>>>uncheckedCast(List.class);
    }
  }

  private class IdAnnotation implements CoreAnnotation<String> {

    @Override
    public Class<String> getType() {
      return String.class;
    }
  }
}
