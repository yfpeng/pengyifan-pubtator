package com.pengyifan.pubtator.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.pengyifan.bioc.*;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

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

    String conceptId1 = finalizeConceptId(relation.getInfon("Chemical").get());
    String conceptId2 = finalizeConceptId(relation.getInfon("Disease").get());
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

    String conceptId1 = finalizeConceptId(annotation.getInfon("Chemical").get());
    String conceptId2 = finalizeConceptId(annotation.getInfon("Disease").get());
    if (conceptId1.compareTo(conceptId2) > 0) {
      String tmp = conceptId1;
      conceptId1 = conceptId2;
      conceptId2 = tmp;
    }
    pDoc.addAnnotation(
        new PubTatorRelationAnnotation(pDoc.getId(), type.get(), conceptId1, conceptId2));
  }

  private String finalizeConceptId(String conceptId) {
    checkArgument(conceptId != null && conceptId.length() != 0, "Cannot apply conceptId: %s",
        conceptId);
    if (conceptId == null || conceptId.length() == 0 || conceptId.equals("-1")) {
      return null;
    }
    int col = conceptId.indexOf(':');
    if (col != -1) {
      conceptId = conceptId.substring(col + 1);
    }
    return conceptId;
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
        String normalizedConceptId = finalizeConceptId(c);
        if (normalizedConceptId != null) {
          conceptIds.add(normalizedConceptId);
        }
      }
    }

    pDoc.addAnnotation(new PubTatorMentionAnnotation(
        pDoc.getId(), type.get(), start, end, annotation.getText().get(), conceptIds));
  }
}
