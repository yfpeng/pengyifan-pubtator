# Java implementation of Pubtator

Data structures and code to read/write Pubtator file format.

### Pubtator

[Pubtator](http://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/tmTools/Format.html) format can be used to share text documents and annotations.

### Getting started

```XML
<repositories>
    <repository>
        <id>oss-sonatype</id>
        <name>oss-sonatype</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
...
<dependency>
  <groupId>com.pengyifan.pubtator</groupId>
  <artifactId>pengyifan-pubtator</artifactId>
  <version>0.0.3-SNAPSHOT</version>
</dependency>
```

### Convert from Pubtator to BioC

```java
import com.pengyifan.pubtator.io.PubTatorIO;
import com.pengyifan.pubtator.PubTatorDocument;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.Writer;
import java.util.List;

// read from pubtator
List<PubTatorDocument> documents = new ArrayList();
documents.addAll(PubTatorIO.readPubTatorFormat(new FileReader(file)));

// write to bioc
PubTatorIO.write(new FileWriter(outfile), documents);
```

### Developers

* Yifan Peng (yfpeng@udel.edu)

