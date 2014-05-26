# Sample Extractor

A minimalistic sample extractor and a testbed for some classes that might become
generic support classes to facilitate the development of extractors.

Compile and run with

    mvn clean install exec:java

Example invocation with curl:


    $ curl -X POST -d @file.txt http://localhost:7100/
    []    a       <http://example.org/ontology#TextDescription> ;
          <http://example.org/ontology#textLength>
                  "5"^^<http://www.w3.org/2001/XMLSchema#int> ;
          <http://rdfs.org/sioc/ns#content>
                  "hallo"^^<http://www.w3.org/2001/XMLSchema#string> .

