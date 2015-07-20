package com.pengyifan.pubtator.io;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

class PubTatorLoader {

  // private static final String newline = System.getProperty("line.separator");

  public List<PubTatorDocument> load(Reader reader)
      throws IOException {
    // read all
    StringBuilder sb = new StringBuilder();
    char[] cbuf = new char[1024];
    int size = 0;
    while ((size = reader.read(cbuf, 0, 1024)) != -1) {
      sb.append(cbuf, 0, size);
    }
    List<PubTatorDocument> pDocuments = Lists.newArrayList();

    for (String block : Splitter.onPattern("(\\r?\\n){2,}").split(sb.toString().trim())) {
      block = block.trim();
      PubTatorDocument pDoc = new PubTatorDocument();
      for (String line : Splitter.onPattern("(\\r?\\n)").split(block)) {
        if (line.contains("|t|") || line.contains("|T|")
            || line.contains("|a|") || line.contains("|A|")) {
          String[] fields = parseText(line);
          if (pDoc.getId() == null) {
            pDoc.setId(fields[0]);
          } else {
            checkArgument(pDoc.getId().equals(fields[0]),
                "Different doc id: %s, %s", pDoc.getId(), fields[0]);
          }
          if (fields[1].equalsIgnoreCase("a")) {
            pDoc.setAbstract(fields[2]);
          }
          if (fields[1].equalsIgnoreCase("t")) {
            pDoc.setTitle(fields[2]);
          }
        } else {
          String[] fields = line.split("[\\t]");
          if (fields.length == 4 || fields.length == 5) {
            // Relation
            parseRelation(fields, pDoc);
          } else if (fields.length == 6 || fields.length == 7) {
            // Mention
            parseMention(fields, pDoc);
          } else {
            // Unknown
            throw new IllegalArgumentException(
                String.format("Line does not match known format: %s", line));
          }
        }
      }
      pDocuments.add(pDoc);
    }

    return pDocuments;
  }

  private void parseMention(String[] fields, PubTatorDocument pDoc) {
    checkArgument(pDoc.getId().equals(fields[0]),
        "Different doc id: %s, %s", pDoc.getId(), fields[0]);

    int start = Integer.parseInt(fields[1]);
    int end = Integer.parseInt(fields[2]);

    String text = fields[3];
    String type = fields[4];
    Set<String> conceptIds = Sets.newHashSet();
    for(String conceptId: Splitter.on("|").split(fields[5])) {
      conceptIds.add(finalizeConceptId(conceptId));
    }
    String comment = fields.length == 7 ? fields[6] : null;

    pDoc.addAnnotation(new PubTatorMentionAnnotation(
        pDoc.getId(), type, start, end, text, conceptIds, comment));
  }

  private void parseRelation(String[] fields, PubTatorDocument pDoc) {
    String id = fields[0];
    checkArgument(pDoc.getId().equals(id),
        "Different doc id: %s, %s", pDoc.getId(), fields[0]);

    String type = fields[1];
    String conceptId1 = finalizeConceptId(fields[2]);
    String conceptId2 = finalizeConceptId(fields[3]);
    pDoc.addAnnotation(
        new PubTatorRelationAnnotation(pDoc.getId(), type, conceptId1, conceptId2));
  }

  private String finalizeConceptId(String conceptId) {
    if (conceptId == null || conceptId.length() == 0 || conceptId.equals("-1")) {
      return null;
    }
    int col = conceptId.indexOf(':');
    if (col != -1) {
      conceptId = conceptId.substring(col + 1);
    }
    return conceptId;
  }

  private String[] parseText(String line) {
    String[] fields = new String[3];
    int from = 0;
    int to = line.indexOf('|');
    fields[0] = line.substring(from, to);

    from = to + 1;
    to = line.indexOf('|', from);
    fields[1] = line.substring(from, to);

    from = to + 1;
    fields[2] = line.substring(from);
    return fields;
  }
}
