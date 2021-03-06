package com.pengyifan.pubtator.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.BioCRelation;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;
import com.pengyifan.pubtator.io.PubTatorIO;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

public class BioC2PubTator implements Function<BioCDocument, PubTatorDocument> {

  @Override
  public PubTatorDocument apply(BioCDocument bioCDocument) {
    String documentId = bioCDocument.getID();
    PubTatorDocument pDoc = new PubTatorDocument();
    pDoc.setId(documentId);

    for (BioCPassage passage : bioCDocument.getPassages()) {
      Optional<String> type = passage.getInfon("type");
      checkArgument(type.isPresent(), "No type found");

      if (type.get().equals("abstract")) {
        pDoc.setAbstract(passage.getText().get());
      } else if (type.get().equals("title")) {
        pDoc.setTitle(passage.getText().get());
      } else {
        throw new IllegalArgumentException(
            String.format("Cannot handle passage type: ", type.get()));
      }

      for (BioCAnnotation annotation : passage.getAnnotations()) {
        convertMentioin(annotation, pDoc);
      }
    }

    for (BioCAnnotation annotation : bioCDocument.getAnnotations()) {
      convertRelation(annotation, pDoc);
    }
    for (BioCRelation relation : bioCDocument.getRelations()) {
      convertRelation(relation, pDoc);
    }
    return pDoc;
  }

  private void convertRelation(BioCRelation relation, PubTatorDocument pDoc) {
    Optional<String> type = relation.getInfon("relation");
    checkArgument(type.isPresent(), "No type found");

    String conceptId1 = PubTatorIO.finalizeConceptId(relation.getInfon("Chemical").get());
    String conceptId2 = PubTatorIO.finalizeConceptId(relation.getInfon("Disease").get());
    if (conceptId1.compareTo(conceptId2) > 0) {
      String tmp = conceptId1;
      conceptId1 = conceptId2;
      conceptId2 = tmp;
    }
    pDoc.addAnnotation(
        new PubTatorRelationAnnotation(pDoc.getId(), type.get(), conceptId1, conceptId2));
  }

  private void convertRelation(BioCAnnotation annotation, PubTatorDocument pDoc) {
    Optional<String> type = annotation.getInfon("relation");
    checkArgument(type.isPresent(), "No type found");

    String conceptId1 = PubTatorIO.finalizeConceptId(annotation.getInfon("Chemical").get());
    String conceptId2 = PubTatorIO.finalizeConceptId(annotation.getInfon("Disease").get());
    pDoc.addAnnotation(
        new PubTatorRelationAnnotation(pDoc.getId(), type.get(), conceptId1, conceptId2));
  }

  private void convertMentioin(BioCAnnotation annotation, PubTatorDocument pDoc) {
    Optional<String> type = annotation.getInfon("type");
    checkArgument(type.isPresent(), "No type found");

    Optional<String> compositeRole = annotation.getInfon("CompositeRole");
    if (compositeRole.isPresent() && !compositeRole.get().equals("CompositeMention")) {
      return;
    }

    BioCLocation location = annotation.getTotalLocation();
    int start = location.getOffset();
    int end = start + location.getLength();
    Optional<String> conceptIdList = annotation.getInfon("MESH");
    if (!conceptIdList.isPresent()) {
      conceptIdList = annotation.getInfon("OMIM");
    }

    Set<String> conceptIds = Sets.newHashSet();
    if (conceptIdList.isPresent()) {
      for (String c : Splitter.on("|").split(conceptIdList.get().trim())) {
        String normalizedConceptId = PubTatorIO.finalizeConceptId(c);
        if (normalizedConceptId != null) {
          conceptIds.add(normalizedConceptId);
        }
      }
    }

    Optional<String> comment = annotation.getInfon("comment");

    if (comment.isPresent()) {
      pDoc.addAnnotation(new PubTatorMentionAnnotation(
          pDoc.getId(), type.get(), start, end, annotation.getText().get(), conceptIds, comment.get()));
    } else {
      pDoc.addAnnotation(new PubTatorMentionAnnotation(
          pDoc.getId(), type.get(), start, end, annotation.getText().get(), conceptIds));
    }
  }
}
