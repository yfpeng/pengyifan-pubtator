package com.pengyifan.pubtator.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pengyifan.brat.BratDocument;
import com.pengyifan.brat.BratEntity;
import com.pengyifan.brat.BratNote;
import com.pengyifan.brat.BratRelation;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class PubTator2Brat {

  public BratDocument convert(PubTatorDocument pubTatorDocument) {
    BratDocument bratDocument = new BratDocument();
    bratDocument.setDocId(pubTatorDocument.getId());
    bratDocument.setText(Joiner.on("\n").join(
        pubTatorDocument.getTitle(), pubTatorDocument.getAbstract()));

    Map<String, List<BratEntity>> map = Maps.newHashMap();

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

      for(String conceptId: mention.getConceptIds()) {
        if (!map.containsKey(conceptId)) {
          map.put(conceptId, Lists.newArrayList());
        }
        map.get(conceptId).add(entity);
      }
    }
    // relation
//    int relationId = 0;
//    for(PubTatorRelationAnnotation relation: pubTatorDocument.getRelations()) {
//      List<BratEntity> e1s = map.get(relation.getConceptId1());
//      List<BratEntity> e2s = map.get(relation.getConceptId2());
//      for(BratEntity e1: e1s) {
//        for (BratEntity e2: e2s) {
//          BratRelation bratRelation = new BratRelation();
//          bratRelation.setId("R" + relationId++);
//          bratRelation.setType(relation.getType());
//          bratRelation.putArgument(e1.getType(), e1.getId());
//          bratRelation.putArgument(e2.getType(), e2.getId());
//          bratDocument.addAnnotation(bratRelation);
//        }
//      }
//    }
    return bratDocument;
  }
}
