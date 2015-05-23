/*
 * Copyright 2014 Reto.
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
package eu.fusepool.p3.transformer.tests;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import eu.fusepool.p3.transformer.sample.SimpleTransformer;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.net.ServerSocket;
import java.util.Iterator;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.http.HttpStatus;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Reto
 */
public class SynRestApiTest {

    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        RestAssured.baseURI = "http://localhost:"+port+"/";
        TransformerServer server = new TransformerServer(port, true);
        server.start(new SimpleTransformer());
    }

    @Test
    public void turtleOnGet() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .header("Origin", "http://www.example-social-network.com")
                .expect().statusCode(HttpStatus.SC_OK).header("Content-Type", "text/turtle")
                .header("Access-Control-Allow-Origin", "http://www.example-social-network.com").when()
                .get();
    }
    
    @Test
    public void turtlePost() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .content("hello")
                .expect().statusCode(HttpStatus.SC_OK).content(new StringContains("hello")).header("Content-Type", "text/turtle").when()
                .post();
        ImmutableGraph graph = Parser.getInstance().parse(response.getBody().asInputStream(), "text/turtle");
        Iterator<Triple> typeTriples = graph.filter(null, RDF.type, 
                SimpleTransformer.TEXUAL_CONTENT);
        Assert.assertTrue("No type triple found", typeTriples.hasNext());
        RDFTerm textDescription = typeTriples.next().getSubject();
        Assert.assertTrue("TextDescription resource is not a BlankNode", textDescription instanceof BlankNode);
    }
    
    @Test
    public void turtlePostWithContentLocation() {
        final String contentUri = "http://exaple.org/content";
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .header("Content-Location", contentUri)
                .content("hello")
                .expect().statusCode(HttpStatus.SC_OK).content(new StringContains("hello")).header("Content-Type", "text/turtle").when()
                .post();
        ImmutableGraph graph = Parser.getInstance().parse(response.getBody().asInputStream(), "text/turtle");
        Iterator<Triple> typeTriples = graph.filter(null, RDF.type, 
                SimpleTransformer.TEXUAL_CONTENT);
        Assert.assertTrue("No type triple found", typeTriples.hasNext());
        RDFTerm textDescription = typeTriples.next().getSubject();
        Assert.assertTrue("TextDescription resource is not a IRI", textDescription instanceof IRI);
        Assert.assertEquals("RDFTerm doesn't have the right URI", contentUri, ((IRI)textDescription).getUnicodeString());
    }
    
    @Test
    public void badRequest() {
        RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .content("Democracy for Hong Kong") 
                .expect().statusCode(HttpStatus.SC_FORBIDDEN).when()
                .post();
        
    }

    public static int findFreePort() {
        int port = 0;
        try (ServerSocket server = new ServerSocket(0);) {
            port = server.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("unable to find a free port");
        }
        return port;
    }

}
