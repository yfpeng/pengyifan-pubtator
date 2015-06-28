package com.pengyifan.pubtator.io;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    List<PubTatorDocument> pubTatorDocuments = Lists.newArrayList();

    for (String block : Splitter.onPattern("(\\r?\\n){2,}").split(sb.toString().trim())) {
      block = block.trim();
      BioCDocument bioCDocument = new BioCDocument();
      boolean first = true;
      PubTatorDocument pubTatorDocument = new PubTatorDocument(bioCDocument);
      pubTatorDocuments.add(pubTatorDocument);

      for (String line : Splitter.onPattern("(\\r?\\n)").split(block)) {
        if (line.contains("|t|") || line.contains("|T|")
            || line.contains("|a|") || line.contains("|A|")) {
          String[] fields = parseText(line);
          if (first) {
            first = false;
            bioCDocument.setID(fields[0]);
          } else {
            checkArgument(bioCDocument.getID().equals(fields[0]),
                "Different doc id: %s, %s", bioCDocument.getID(), fields[0]);
          }
          if (fields[1].equalsIgnoreCase("a")) {
            Optional<BioCPassage> opt = pubTatorDocument.find("title");
            checkArgument(opt.isPresent(), "No  title found: %s", pubTatorDocument.getId());
            BioCPassage passage = new BioCPassage();
            passage.setOffset(opt.get().getText().get().length() + 1);
            passage.putInfon("type", "abstract");
            passage.setText(fields[2]);
          }
          if (fields[1].equalsIgnoreCase("t")) {

            BioCPassage passage = new BioCPassage();
            passage.setOffset(0);
            passage.putInfon("type", "title");
            passage.setText(fields[2]);
            bioCDocument.addPassage(passage);
          }
        } else {
          String[] fields = line.split("[\\t]");
          if (fields.length == 4 || fields.length == 5) {
            // Relation
            parseRelation(fields, bioCDocument);
          } else if (fields.length == 6 || fields.length == 7) {
            // Mention
            parseMention(fields, bioCDocument);
          } else {
            // Unknown
            throw new IllegalArgumentException(
                String.format("Line does not match known format: %s", line));
          }
        }
      }
      pubTatorDocuments.add(pubTatorDocument);
    }

    return pubTatorDocuments;
  }

  private void parseMention(String[] fields, BioCDocument bioCDocument) {
    String id = fields[0];
    checkArgument(bioCDocument.getID().equals(id),
        "Different doc id: %s, %s", bioCDocument.getID(), fields[0]);

    int start = Integer.parseInt(fields[1]);
    int end = Integer.parseInt(fields[2]);

    BioCAnnotation bioCAnnotation = new BioCAnnotation();
    bioCAnnotation.setID(bioCDocument.getID());
    bioCAnnotation.setText(fields[3]);
    bioCAnnotation.putInfon("type", fields[4]);
    if (fields[5].contains(":")) {
      bioCAnnotation.putInfon(fields[5].split(":")[0], fields[5].split(":")[1]);
    } else {
      bioCAnnotation.putInfon("MESH", fields[5]);
    }
    bioCAnnotation.addLocation(new BioCLocation(start, end - start));
    if (fields.length == 7) {
      bioCAnnotation.putInfon("comment", fields[6]);
    }

    for(BioCPassage passage: bioCDocument.getPassages()) {
      if (passage.getOffset() <= start
          && end <= passage.getOffset() + passage.getText().get().length()) {
        passage.addAnnotation(bioCAnnotation);
        return;
      }
    }
    throw new IllegalArgumentException("should not reach here");
  }

  private void parseRelation(String[] fields, BioCDocument bioCDocument) {
    String id = fields[0];
    checkArgument(bioCDocument.getID().equals(id),
        "Different doc id: %s, %s", bioCDocument.getID(), fields[0]);

    BioCAnnotation bioCAnnotation = new BioCAnnotation();
    bioCAnnotation.setID(bioCDocument.getID());
    bioCAnnotation.putInfon("relation", fields[1]);
    bioCAnnotation.putInfon("conceptId1", finalizeConceptId(fields[2]));
    bioCAnnotation.putInfon("conceptId2", finalizeConceptId(fields[3]));
    bioCDocument.addAnnotation(bioCAnnotation);
  }

  private String finalizeConceptId(String conceptId) {
    if (conceptId == null || conceptId.length() == 0 || conceptId.equals("-1")) {
      return null;
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
