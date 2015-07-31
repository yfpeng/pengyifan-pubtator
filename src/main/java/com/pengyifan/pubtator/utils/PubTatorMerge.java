package com.pengyifan.pubtator.utils;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.BioCRelation;
import com.pengyifan.pubtator.PubTatorDocument;

import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTatorMerge {
  private PubTatorDocument newDoc;
  private int annId;
  private int relId;

  public PubTatorMerge() {
  }

  public void addDocument(PubTatorDocument doc) {
    if (newDoc == null) {
      newDoc = new PubTatorDocument(doc.getBioCDocument());
      annId = newDoc.getMentions().size();
      relId = newDoc.getRelations().size();
    } else {
      checkArgument(newDoc.getId().equals(doc.getId()), "Not same document [new:%s] [old:%s]",
          newDoc.getId(), doc.getId());
    }

    BioCPassage newTitle = newDoc.getBioCDocument().getPassage(0);
    BioCPassage title = doc.getBioCDocument().getPassage(0);
    for (BioCAnnotation ann : title.getAnnotations()) {
      if (!hasAnnotation(ann, newTitle.getAnnotations())) {
        ann.setID("T" + annId++);
        newTitle.addAnnotation(ann);
      }
    }

    BioCPassage newAbstract = newDoc.getBioCDocument().getPassage(1);
    BioCPassage ab = doc.getBioCDocument().getPassage(1);
    for (BioCAnnotation ann : ab.getAnnotations()) {
      if (!hasAnnotation(ann, newAbstract.getAnnotations())) {
        ann.setID("T" + annId++);
        newAbstract.addAnnotation(ann);
      }
    }

    for (BioCAnnotation ann : doc.getBioCDocument().getAnnotations()) {
      if (!hasAnnotation(ann, newDoc.getBioCDocument().getAnnotations())) {
        ann.setID("T" + annId++);
        newDoc.getBioCDocument().addAnnotation(ann);
      }
    }
    for (BioCRelation rel : doc.getBioCDocument().getRelations()) {
      if (!hasRelation(rel, newDoc.getBioCDocument().getRelations())) {
        rel.setID("R" + relId++);
        newDoc.getBioCDocument().addRelation(rel);
      }
    }
  }

  private boolean hasRelation(BioCRelation rel, Collection<BioCRelation> relations) {
    for (BioCRelation r : relations) {
      if (isEqual(r, rel)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasAnnotation(BioCAnnotation ann, Collection<BioCAnnotation> annotations) {
    for (BioCAnnotation a : annotations) {
      if (isEqual(a, ann)) {
        return true;
      }
    }
    return false;
  }

  public PubTatorDocument getDoc() {
    return newDoc;
  }

  private boolean isEqual(BioCRelation rel1, BioCRelation rel2) {
    return Objects.equals(rel1.getInfons(), rel2.getInfons());
  }

  private boolean isEqual(BioCAnnotation ann1, BioCAnnotation ann2) {
    return Objects.equals(ann1.getText(), ann2.getText())
        && Objects.equals(ann1.getInfons(), ann2.getInfons())
        && Objects.equals(ann1.getLocations(), ann2.getLocations());
  }
}
