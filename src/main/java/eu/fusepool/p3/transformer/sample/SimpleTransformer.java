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
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.commons.io.IOUtils;


public class SimpleTransformer extends RdfGeneratingTransformer {
    
    public static final UriRef TEXUAL_CONTENT = new UriRef("http://example.org/ontology#TextualContent");
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
    protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
        final String text = IOUtils.toString(entity.getData(), "UTF-8");
        if (forbiddenStrings.contains(text)) {
            throw new TransformerException(403, "fobidden!");
        }
        final TripleCollection result = new SimpleMGraph();
        final Resource resource = entity.getContentLocation() == null?
                new BNode() :
                new UriRef(entity.getContentLocation().toString());
        final GraphNode node = new GraphNode(resource, result);
        node.addProperty(RDF.type, TEXUAL_CONTENT);
        node.addPropertyValue(SIOC.content, text);
        node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), text.length());
        return result;
    }

    @Override
    public boolean isLongRunning() {
        return false;
    }



}
