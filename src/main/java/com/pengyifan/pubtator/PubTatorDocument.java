package com.pengyifan.pubtator;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCPassage;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class PubTatorDocument {

  private BioCDocument bioCDocument;

  public PubTatorDocument(BioCDocument bioCDocument) {
    this.bioCDocument = bioCDocument;
  }

  public BioCDocument getBioCDocument() {
    return bioCDocument;
  }

  public List<PubTatorMentionAnnotation> getMentions(String conceptId) {
    return getMentions().stream()
        .filter(m -> m.getConceptIds().contains(conceptId))
        .collect(Collectors.toList());
  }

  public String toPubTatorString() {
    StringJoiner sj = new StringJoiner("\n");
    sj.add(Joiner.on("|").join(getId(), "t", getTitle()));
    sj.add(Joiner.on("|").join(getId(), "a", getAbstract()));

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

  public String getId() {
    return bioCDocument.getID();
  }

  public String getTitle() {
    return find("title").get().getText().get();
  }

  public Optional<BioCPassage> find(String type) {
    return bioCDocument.getPassages().stream()
        .filter(p -> p.getInfon("type").get().equals(type))
        .findAny();
  }

  public String getAbstract() {
    return find("abstract").get().getText().get();
  }

  public List<PubTatorMentionAnnotation> getMentions() {
    List<PubTatorMentionAnnotation> annotatons = Lists.newArrayList();
    for (BioCPassage passage : bioCDocument.getPassages()) {
      for (BioCAnnotation annotation : passage.getAnnotations()) {
        annotatons.add(new PubTatorMentionAnnotation(bioCDocument, annotation));
      }
    }
    return annotatons;
  }

  public List<PubTatorRelationAnnotation> getRelations() {
    return bioCDocument.getRelations().stream()
        .map(r -> new PubTatorRelationAnnotation(bioCDocument, r))
        .collect(Collectors.toList());
  }
}
