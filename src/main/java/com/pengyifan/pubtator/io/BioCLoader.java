package com.pengyifan.pubtator.io;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCCollection;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.io.BioCCollectionReader;
import com.pengyifan.pubtator.PubTatorDocument;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

class BioCLoader {

  public List<PubTatorDocument> load(Reader reader)
      throws XMLStreamException, IOException {

    BioCCollectionReader biocReader = new BioCCollectionReader(reader);
    BioCCollection bioCCollection = biocReader.readCollection();
    biocReader.close();
    return bioCCollection.getDocuments().stream()
        .map(d -> new PubTatorDocument(d))
        .collect(Collectors.toList());
  }
}
