package com.pengyifan.pubtator.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTator2BioC implements Function<PubTatorDocument, BioCDocument> {
  @Override
  public BioCDocument apply(PubTatorDocument pubTatorDocument) {
    int menId = 0;
    int relId = 0;

    BioCDocument bioCDocument = new BioCDocument();
    bioCDocument.setID(pubTatorDocument.getId());
    // title
    BioCPassage titlePassage = new BioCPassage();
    bioCDocument.addPassage(titlePassage);
    titlePassage.putInfon("type", "title");
    titlePassage.setText(pubTatorDocument.getTitle());
    titlePassage.setOffset(0);
    for (PubTatorMentionAnnotation mention : pubTatorDocument.getMentions()) {
      if (isInPassage(titlePassage, mention)) {
        titlePassage.addAnnotation(convertMention(menId++, mention));
      }
    }
    // abstract
    BioCPassage abstractPassage = new BioCPassage();
    bioCDocument.addPassage(abstractPassage);
    abstractPassage.putInfon("type", "abstract");
    abstractPassage.setText(pubTatorDocument.getAbstract());
    abstractPassage.setOffset(titlePassage.getOffset() + pubTatorDocument.getTitle().length() + 1);
    for (PubTatorMentionAnnotation mention : pubTatorDocument.getMentions()) {
      if (isInPassage(abstractPassage, mention)) {
        abstractPassage.addAnnotation(convertMention(menId++, mention));
      }
    }
    // relation
    for (PubTatorRelationAnnotation relation : pubTatorDocument.getRelations()) {
      try {
        bioCDocument.addAnnotation(convertRelation(relId++, pubTatorDocument, relation));
      } catch (IllegalArgumentException e) {
        System.err.println(e.getMessage());
      }
    }
    return bioCDocument;
  }

  private BioCAnnotation convertRelation(int relId, PubTatorDocument document,
      PubTatorRelationAnnotation relation) {
    BioCAnnotation bioCAnnotation = new BioCAnnotation();
    bioCAnnotation.setID("R" + String.valueOf(relId));
    bioCAnnotation.putInfon("relation", relation.getType());

    List<PubTatorMentionAnnotation> mentions = document.getMentions(relation.getConceptId1());
    checkArgument(!mentions.isEmpty(), "Cannot find concept id: %s", relation.getConceptId1());
    bioCAnnotation.putInfon(mentions.get(0).getType(), relation.getConceptId1());

    mentions = document.getMentions(relation.getConceptId2());
    checkArgument(!mentions.isEmpty(), "Cannot find concept id: %s", relation.getConceptId2());
    bioCAnnotation.putInfon(mentions.get(0).getType(), relation.getConceptId2());

    return bioCAnnotation;
  }

  private BioCAnnotation convertMention(int annId, PubTatorMentionAnnotation mention) {
    BioCAnnotation bioCAnnotation = new BioCAnnotation();
    bioCAnnotation.setID(String.valueOf(annId));
    bioCAnnotation.addLocation(
        new BioCLocation(mention.getStart(), mention.getEnd() - mention.getStart()));
    bioCAnnotation.setText(mention.getText());
    bioCAnnotation.putInfon("type", mention.getType());
    if (mention.getConceptIds().size() == 1) {
      bioCAnnotation.putInfon("MESH", Iterables.getOnlyElement(mention.getConceptIds()));
    } else {
      bioCAnnotation.putInfon("MESH", Joiner.on('|').join(mention.getConceptIds()));
      bioCAnnotation.putInfon("CompositeRole", "CompositeMention");
    }
    return bioCAnnotation;
  }

  private boolean isInPassage(BioCPassage bioCPassage, PubTatorMentionAnnotation mention) {
    Range<Integer> range = Range.closed(
        bioCPassage.getOffset(),
        bioCPassage.getOffset() + bioCPassage.getText().get().length());
    return range.contains(mention.getStart()) && range.contains(mention.getEnd());
  }
}
