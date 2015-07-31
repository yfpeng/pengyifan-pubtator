package com.pengyifan.pubtator.io;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.BioCRelation;
import com.pengyifan.pubtator.PubTatorDocument;

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
  private BioCDocument currentDocument;

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
    int annId = 0;
    int relId = 0;
    int state = 0;
    while (currentLine != null) {
      switch (state) {
      case 0:
        if (currentLine.isEmpty()) {
          if (currentDocument != null) {
            pDocuments.add(new PubTatorDocument(currentDocument));
            currentDocument = null;
          }
        } else if (currentLine.contains("|t|")) {
          // title
          currentDocument = new BioCDocument();
          annId = 0;
          relId = 0;
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
          if (parseMention("T" + annId, fields)) {
            annId++;
          } else if (parseRelation("R" + relId, fields)) {
            relId++;
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
      pDocuments.add(new PubTatorDocument(currentDocument));
      currentDocument = null;
    }
    return pDocuments;
  }

  private void parseTitle() {
    try {
      String[] fields = parseText(currentLine);
      checkNotNull(fields[0], "ID is null");
      checkNotNull(fields[2], "text is null");

      currentDocument.setID(fields[0]);

      BioCPassage passage = new BioCPassage();
      passage.setOffset(0);
      passage.setText(fields[2]);
      passage.putInfon("type", "title");
      currentDocument.addPassage(passage);
    } catch (Exception e) {
      appendErrorMessage(e);
    }
  }

  private void parseAbstract() {
    try {
      String[] fields = parseText(currentLine);
      checkNotNull(fields[0], "ID is null");
      checkNotNull(fields[2], "text is null");
      checkArgument(fields[0].equals(currentDocument.getID()),
          "Different doc id: %s, %s", currentDocument.getID(), fields[0]);
      checkArgument(currentDocument.getPassageCount() == 1, "No title");

      BioCPassage titlePassage = currentDocument.getPassage(0);

      BioCPassage passage = new BioCPassage();
      passage.setOffset(titlePassage.getText().get().length() + 1);
      passage.setText(fields[2]);
      passage.putInfon("type", "abstract");
      currentDocument.addPassage(passage);
    } catch (Exception e) {
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

  private boolean parseMention(String annId, String[] fields) {
    try {
      checkArgument(fields[0].equals(currentDocument.getID()),
          "Different doc id: %s, %s", currentDocument.getID(), fields[0]);

      int start = Integer.parseInt(fields[1]);
      int end = Integer.parseInt(fields[2]);

      String actualText = fields[3];

      String text = currentDocument.getPassage(0).getText().get()
          + "\n"
          + currentDocument.getPassage(1).getText().get();

      String expectedText = text.substring(start, end);
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

      BioCAnnotation annotation = new BioCAnnotation();
      annotation.setID(annId);
      BioCLocation location = new BioCLocation(start, end - start);
      annotation.addLocation(location);
      annotation.putInfon("type", type);
      annotation.putInfon("MESH", Joiner.on('|').join(conceptIds));
      annotation.setText(actualText);
      if (comment != null) {
        annotation.putInfon("comment", comment);
      }

      BioCPassage passage = findPassage(location);
      passage.addAnnotation(annotation);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private BioCPassage findPassage(BioCLocation location) {
    for (BioCPassage passage : currentDocument.getPassages()) {
      if (passage.getOffset() <= location.getOffset()
          && location.getOffset() + location.getLength()
          <= passage.getOffset() + passage.getText().get().length()) {
        return passage;
      }
    }
    throw new IllegalArgumentException(
        String.format("Cannot find passage with loc [%s]", location));
  }

  private boolean parseRelation(String relId, String[] fields) {
    try {
      checkArgument(fields[0].equals(currentDocument.getID()),
          "Different doc id: %s, %s", currentDocument.getID(), fields[0]);

      String type = fields[1];
      String conceptId1 = finalizeConceptId(fields[2]);
      String conceptId2 = finalizeConceptId(fields[3]);

      checkArgument(hasId(conceptId1), "Cannot find concept [%s] in the document.", conceptId1);
      checkArgument(hasId(conceptId2), "Cannot find concept [%s] in the document.", conceptId2);

      BioCRelation relation = new BioCRelation();
      relation.setID(relId);
      relation.putInfon("relation", type);
      relation.putInfon("Chemical", conceptId1);
      relation.putInfon("Disease", conceptId2);

      currentDocument.addRelation(relation);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean hasId(String id) {
    for (BioCPassage passage : currentDocument.getPassages()) {
      for (BioCAnnotation annotation : passage.getAnnotations()) {
        String mesh = annotation.getInfon("MESH").orElse("");
        if (mesh.contains(id)) {
          return true;
        }
      }
    }
    return false;
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
