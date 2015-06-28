package com.pengyifan.pubtator.utils;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.brat.BratAnnotation;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTatorMerge {
  private PubTatorDocument newDoc;

  public PubTatorMerge() {
  }

  public void addDocument(PubTatorDocument doc) {
    if (newDoc == null) {
      newDoc = new PubTatorDocument(doc.getBioCDocument());
    } else {
      checkArgument(newDoc.getId().equals(doc.getId()), "Not same document [new:%s] [old:%s]",
          newDoc.getId(), doc.getId());
    }

    BioCPassage newTitle = newDoc.getBioCDocument().getPassage(0);
    BioCPassage title = doc.getBioCDocument().getPassage(0);
    for(BioCAnnotation ann: title.getAnnotations()) {
      if (!hasAnnotation(ann, newTitle.getAnnotations())) {
        newTitle.addAnnotation(ann);
      }
    }

    BioCPassage newAbstract = newDoc.getBioCDocument().getPassage(1);
    BioCPassage ab = doc.getBioCDocument().getPassage(1);
    for(BioCAnnotation ann: ab.getAnnotations()) {
      if (!hasAnnotation(ann, newAbstract.getAnnotations())) {
        newAbstract.addAnnotation(ann);
      }
    }

    for(BioCAnnotation ann: doc.getBioCDocument().getAnnotations()) {
      if (!hasAnnotation(ann, newDoc.getBioCDocument().getAnnotations())) {
        newDoc.getBioCDocument().addAnnotation(ann);
      }
    }
  }

  private boolean hasAnnotation(BioCAnnotation ann, Collection<BioCAnnotation> annotations) {
    for(BioCAnnotation a: annotations) {
      if (isEqual(a, ann)) {
        return true;
      }
    }
    return false;
  }

  public PubTatorDocument getDoc() {
    return newDoc;
  }

  private boolean isEqual(BioCAnnotation ann1, BioCAnnotation ann2) {
    return Objects.equals(ann1.getText(), ann2.getText())
        && Objects.equals(ann1.getInfons(), ann2.getInfons())
        && Objects.equals(ann1.getLocations(), ann2.getLocations());
  }
}
