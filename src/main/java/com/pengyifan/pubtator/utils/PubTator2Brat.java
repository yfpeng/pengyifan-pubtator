package com.pengyifan.pubtator.utils;

import com.google.common.base.Joiner;
import com.pengyifan.brat.BratDocument;
import com.pengyifan.brat.BratEntity;
import com.pengyifan.brat.BratNote;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTator2Brat {
  public BratDocument convert(PubTatorDocument pubTatorDocument) {
    BratDocument bratDocument = new BratDocument();
    bratDocument.setDocId(pubTatorDocument.getId());
    bratDocument.setText(Joiner.on("\n").join(
        pubTatorDocument.getTitle(), pubTatorDocument.getAbstract()));
    // mention
    int entityId = 0;
    for(PubTatorMentionAnnotation mention: pubTatorDocument.getMentions()) {
      // test text
      String actual = mention.getText();
      String expected = bratDocument.getText().substring(mention.getStart(), mention.getEnd());
      checkArgument(actual.equals(expected),
          "Not equal. actual[%s %s %s] : expected[%s]", actual, mention.getStart(),
          mention.getEnd(), expected);
      BratEntity entity = new BratEntity();
      entity.setId("T" + entityId++);
      entity.setText(mention.getText());
      entity.setType(mention.getType());
      entity.addSpan(mention.getStart(), mention.getEnd());
      bratDocument.addAnnotation(entity);
      BratNote note = new BratNote();
      note.setId("#" + entity.getId());
      note.setRefId(entity.getId());
      note.setType("ConceptId");
      note.setText(Joiner.on('|').join(mention.getConceptIds()));
      bratDocument.addAnnotation(note);
    }
    return bratDocument;
  }
}
