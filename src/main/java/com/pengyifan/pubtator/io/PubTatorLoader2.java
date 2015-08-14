package com.pengyifan.pubtator.io;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.truth.StringUtil;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.PubTatorMentionAnnotation;
import com.pengyifan.pubtator.PubTatorRelationAnnotation;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class PubTatorLoader2 implements Closeable {

  private LineNumberReader reader;
  private StringBuilder errorMessage;
  private String currentLine;
  private PubTatorDocument currentDocument;

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

  public List<PubTatorDocument> read() throws IOException {
    List<PubTatorDocument> pDocuments = Lists.newArrayList();

    currentLine = reader.readLine();
    currentDocument = null;
    int state = 0;
    while (currentLine != null) {
      switch (state) {
      case 0:
        if (currentLine.isEmpty()) {
          if (currentDocument != null) {
            pDocuments.add(currentDocument);
            currentDocument = null;
          }
        } else if (currentLine.contains("|t|")) {
          // title
          currentDocument = new PubTatorDocument();
          parseTitle();
          state = 1;
        } else {
          // error
          appendErrorMessage();
        }
        currentLine = reader.readLine();
        break;
      case 1:
        if (currentLine.isEmpty()) {
          state = 0;
        } else if (currentLine.contains("|a|")) {
          // abstract
          parseAbstract();
          state = 2;
          currentLine = reader.readLine();
        } else {
          // error
          appendErrorMessage();
          state = 0;
          currentLine = reader.readLine();
        }
        break;
      case 2:
        if (currentLine.isEmpty()) {
          state = 0;
        } else {
          String[] fields = currentLine.split("[\\t]");
          if (parseMention(fields)) {
            ;
          } else if (parseRelation(fields)) {
            ;
          } else {
            // error
            appendErrorMessage();
            state = 0;
          }
          currentLine = reader.readLine();
        }
        break;
      }
    }
    if (currentDocument != null) {
      pDocuments.add(currentDocument);
      currentDocument = null;
    }
    return pDocuments;
  }

  private void parseTitle() {
    try {
      String[] fields = parseText(currentLine);
      checkNotNull(fields[0], "ID is null");
      checkNotNull(fields[2], "text is null");

      currentDocument.setId(fields[0]);
      currentDocument.setTitle(fields[2]);
    } catch(Exception e) {
      appendErrorMessage(e);
    }
  }

  private void parseAbstract() {
    try {
      String[] fields = parseText(currentLine);
      checkNotNull(fields[0], "ID is null");
      checkNotNull(fields[2], "text is null");
      checkArgument(fields[0].equals(currentDocument.getId()),
          "Different doc id: %s, %s", currentDocument.getId(), fields[0]);
      currentDocument.setAbstract(fields[2]);
    } catch(Exception e) {
      appendErrorMessage(e);
    }
  }

  private void appendErrorMessage() {
    errorMessage.append(String.format("Cannot parse line %d: %s\n",
        reader.getLineNumber(), currentLine));
  }

  private void appendErrorMessage(Exception e) {
    errorMessage.append(e).append('\n');
    appendErrorMessage();
  }

  private boolean parseMention(String[] fields) {
    try {
      checkArgument(fields[0].equals(currentDocument.getId()),
          "Different doc id: %s, %s", currentDocument.getId(), fields[0]);

      int start = Integer.parseInt(fields[1]);
      int end = Integer.parseInt(fields[2]);

      String actualText = fields[3];
      String expectedText = currentDocument.getText().substring(start, end);
      checkArgument(expectedText.equals(actualText), "Text mismatch. Expected[%s], actual[%s]",
          expectedText, actualText);

      String type = fields[4];
      Set<String> conceptIds = Sets.newHashSet();
      if (fields.length >= 6) {
        for (String conceptId : Splitter.on("|").split(fields[5])) {
          String normalizedConceptId = finalizeConceptId(conceptId);
          if (normalizedConceptId != null) {
            conceptIds.add(normalizedConceptId);
          }
        }
      }
      String comment = null;
      if (fields.length >= 7) {
        comment = fields[6];
      }

      currentDocument.addAnnotation(new PubTatorMentionAnnotation(
          currentDocument.getId(), type, start, end, actualText, conceptIds, comment));
      return true;
    } catch(Exception e) {
//      errorMessage.append(String.format("Error: %s\n", e.getMessage()));
      return false;
    }
  }

  private boolean parseRelation(String[] fields) {
    try {
      checkArgument(fields[0].equals(currentDocument.getId()),
          "Different doc id: %s, %s", currentDocument.getId(), fields[0]);

      String type = fields[1];
      String conceptId1 = finalizeConceptId(fields[2]);
      String conceptId2 = finalizeConceptId(fields[3]);

      checkArgument(!currentDocument.getMentions(conceptId1).isEmpty(),
          "Cannot find concept [%s] in the document.", conceptId1);
      checkArgument(!currentDocument.getMentions(conceptId2).isEmpty(),
          "Cannot find concept [%s] in the document.", conceptId2);

      currentDocument.addAnnotation(
          new PubTatorRelationAnnotation(currentDocument.getId(), type, conceptId1, conceptId2));
      return true;
    } catch(Exception e) {
//      errorMessage.append(String.format("Error: %s\n", e.getMessage()));
      return false;
    }
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
    checkArgument(to != -1, "Cannot find first '|'");

    fields[0] = line.substring(from, to);

    from = to + 1;
    to = line.indexOf('|', from);
    checkArgument(to != -1, "Cannot find second '|'");

    fields[1] = line.substring(from, to);

    from = to + 1;
    fields[2] = line.substring(from);
    return fields;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
