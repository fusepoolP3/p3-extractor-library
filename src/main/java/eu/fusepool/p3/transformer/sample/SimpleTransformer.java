/*
 * Copyright 2014 Bern University of Applied Sciences.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fusepool.p3.transformer.sample;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.RdfGeneratingTransformer;
import eu.fusepool.p3.transformer.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.commons.io.IOUtils;


public class SimpleTransformer extends RdfGeneratingTransformer {
    
    public static final IRI TEXUAL_CONTENT = new IRI("http://example.org/ontology#TextualContent");
    private static final List<String> forbiddenStrings = Arrays.asList(new String[] {"Democracy for Hong Kong"});

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        try {
            MimeType mimeType = new MimeType("text/plain;charset=UTF-8");
            return Collections.singleton(mimeType);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Graph generateRdf(HttpRequestEntity entity) throws IOException {
        final String text = IOUtils.toString(entity.getData(), "UTF-8");
        if (forbiddenStrings.contains(text)) {
            throw new TransformerException(403, "fobidden!");
        }
        final Graph result = new SimpleGraph();
        final RDFTerm resource = entity.getContentLocation() == null?
                new BlankNode() :
                new IRI(entity.getContentLocation().toString());
        final GraphNode node = new GraphNode(resource, result);
        node.addProperty(RDF.type, TEXUAL_CONTENT);
        node.addPropertyValue(SIOC.content, text);
        node.addPropertyValue(new IRI("http://example.org/ontology#textLength"), text.length());
        return result;
    }

    @Override
    public boolean isLongRunning() {
        return false;
    }



}
