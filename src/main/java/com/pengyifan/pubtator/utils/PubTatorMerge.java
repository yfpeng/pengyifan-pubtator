package com.pengyifan.pubtator.utils;

import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTatorMerge {
  private PubTatorDocument newDoc;

  public PubTatorMerge() {
  }

  public void addDocument(PubTatorDocument doc) {
    if (newDoc == null) {
      newDoc = new PubTatorDocument();
      newDoc.setId(doc.getId());
      newDoc.setTitle(doc.getTitle());
      newDoc.setAbstract(doc.getAbstract());
    } else {
      checkArgument(newDoc.getId().equals(doc.getId()), "Not same document [new:%s] [old:%s]",
          newDoc.getId(), doc.getId());
    }

    doc.getMentions().stream().forEach(ann -> {
      if (!hasMention(ann)) {
        newDoc.addAnnotation(ann);
      }
    });
    doc.getRelations().stream().forEach(ann -> {
      if (!hasRelation(ann)) {
        newDoc.addAnnotation(ann);
      }
    });
  }

  public PubTatorDocument getDoc() {
    newDoc.getMentions().sort((m1, m2) -> Integer.compare(m1.getStart(), m2.getStart()));
    return newDoc;
  }

  private boolean hasRelation(PubTatorRelationAnnotation relation) {
    for (PubTatorRelationAnnotation r : newDoc.getRelations()) {
      if (r.equals(relation)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasMention(PubTatorMentionAnnotation mention) {
    for (PubTatorMentionAnnotation m : newDoc.getMentions()) {
      if (m.equals(mention)) {
        return true;
      }
    }
    return false;
  }
}
