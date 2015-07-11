package com.pengyifan.pubtator.io;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;

public class PubTatorLoader2 implements Closeable {

  private StringBuilder errorMessage;
  private LineNumberReader reader;

  public PubTatorLoader2(Reader reader) {
    this.reader = new LineNumberReader(reader);
    errorMessage = new StringBuilder();
  }

  public boolean hasErrors() {
    return errorMessage.length() != 0;
  }

  public String getErrorMessage() {
    return errorMessage.toString();
  }

  public List<PubTatorDocument> read()
      throws IOException {
    List<PubTatorDocument> pDocuments = Lists.newArrayList();

    LineNumberReader lineNumberReader = new LineNumberReader(reader);

    String line = null;
    PubTatorDocument pDocument = null;
    while ((line = lineNumberReader.readLine()) != null) {
      if (line.isEmpty()) {
        if (pDocument != null) {
          pDocuments.add(pDocument);
          pDocument = null;
        }
      } else if (line.contains("|t|") || line.contains("|T|")) {
        // title
        pDocument = new PubTatorDocument();
        parseTitle(line, pDocument);
      } else if (line.contains("|a|") || line.contains("|A|")) {
        // abstract
        parseAbstract(line, pDocument);
      } else {
        String[] fields = line.split("[\\t]");
        if (fields.length == 4 || fields.length == 5) {
          // Relation
          try {
            parseRelation(fields, line, pDocument);
          } catch (Exception e) {
            errorMessage.append(e.getMessage()).append('\n');
            appendErrorLine(line);
          }
        } else if (fields.length == 6 || fields.length == 7) {
          // Mention
          try {
            parseMention(fields, line, pDocument);
          } catch (Exception e) {
            errorMessage.append(e.getMessage()).append('\n');
            appendErrorLine(line);
          }
        } else {
          // Unknown
          appendErrorLine(line);
        }
      }
    }
    if (pDocument != null) {
      pDocuments.add(pDocument);
    }
    return pDocuments;
  }

  private void parseAbstract(String line, PubTatorDocument pDoc) {
    if (pDoc == null) {
      errorMessage.append(String.format("No title is given\n"));
      appendErrorLine(line);
      return;
    }
    Pair<String, String> p = parseText(line);
    if (p == null) {
      appendErrorLine(line);
      return;
    }
    if (!p.getKey().equals(pDoc.getId())) {
      errorMessage.append(String.format(
          "Different doc id: %s, %s\n", pDoc.getId(), p.getKey()));
      appendErrorLine(line);
    }
    pDoc.setAbstract(p.getValue());
  }

  private void parseTitle(String line, PubTatorDocument pDoc) {
    if (pDoc == null) {
      errorMessage.append(String.format("No title is given\n"));
      appendErrorLine(line);
      return;
    }
    Pair<String, String> p = parseText(line);
    if (p == null) {
      appendErrorLine(line);
      return;
    }
    pDoc.setId(p.getKey());
    pDoc.setTitle(p.getValue());
  }

  private void parseMention(String[] fields, String line, PubTatorDocument pDoc) {
    if (pDoc == null) {
      errorMessage.append(String.format("No title is given\n"));
      appendErrorLine(line);
      return;
    }
    String id = fields[0];
    if (!id.equals(pDoc.getId())) {
      errorMessage.append(String.format(
          "Different doc id: %s, %s\n", pDoc.getId(), fields[0]));
      appendErrorLine(line);
    }

    int start = Integer.parseInt(fields[1]);
    int end = Integer.parseInt(fields[2]);

    String text = fields[3];
    String type = fields[4];
    Set<String> conceptIds = Sets.newHashSet(Splitter.on("|").split(fields[5]));

    pDoc.addAnnotation(new PubTatorMentionAnnotation(
        pDoc.getId(), type, start, end, text, conceptIds));
  }

  private void parseRelation(String[] fields, String line, PubTatorDocument pDoc) {
    if (pDoc == null) {
      errorMessage.append(String.format("No title is given\n"));
      appendErrorLine(line);
      return;
    }
    String id = fields[0];
    if (!id.equals(pDoc.getId())) {
      errorMessage.append(String.format(
          "Different doc id: %s, %s\n", pDoc.getId(), fields[0]));
      appendErrorLine(line);
    }

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
    if (conceptId.contains(":")) {
      return conceptId;
    }
    return conceptId;
  }

  private Pair<String, String> parseText(String line) {

    int from = 0;
    int to = line.indexOf('|');
    if (to == -1) {
      return null;
    }
    String id = line.substring(from, to);

    from = to + 1;
    to = line.indexOf('|', from);
    if (to == -1) {
      return null;
    }

    from = to + 1;
    String text = line.substring(from);
    return Pair.of(id, text);
  }


  private void appendErrorLine(String line) {
    errorMessage.append(String.format(
        "Cannot parse at %d: %s\n", reader.getLineNumber(), line));
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
