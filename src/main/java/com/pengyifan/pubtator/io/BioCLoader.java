package com.pengyifan.pubtator.io;

import com.pengyifan.bioc.BioCCollection;
import com.pengyifan.bioc.io.BioCCollectionReader;
import com.pengyifan.pubtator.PubTatorDocument;
import com.pengyifan.pubtator.utils.BioC2PubTator;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

class BioCLoader {

  private static final BioC2PubTator converter = new BioC2PubTator();

  public List<PubTatorDocument> load(Reader reader)
      throws XMLStreamException, IOException {

    BioCCollectionReader biocReader = new BioCCollectionReader(reader);
    BioCCollection bioCCollection = biocReader.readCollection();
    biocReader.close();

    return bioCCollection.getDocuments().stream()
        .map(d -> converter.apply(d))
        .collect(Collectors.toList());
  }
}

