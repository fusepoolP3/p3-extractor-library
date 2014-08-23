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
import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.sample.SimpleAsyncTransformer;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.net.ServerSocket;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Asynhronous Handlers and Factory is a bit tricky as the task page request
 * need to b sent to the right handler. As long as exactly one ASyncTransformer
 * is returned, things work.
 * 
 * @author Reto
 */
public class FactoryASyncTest {

    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        RestAssured.baseURI = "http://localhost:"+port+"/";
        TransformerServer server = new TransformerServer(port);
        final Transformer transfomer = new SimpleAsyncTransformer();
        server.start(new TransformerFactory() {

            @Override
            public Transformer getTransformer(HttpServletRequest request) {
                return transfomer;
            }
        });
    }

    @Test
    public void turtleOnGet() {
        //Nothing Async-Specvific here
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .expect().statusCode(HttpStatus.SC_OK).header("Content-Type", "text/turtle").when()
                .get();
    }
    
    @Test
    public void turtlePost() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .content("hello")
                .expect().statusCode(HttpStatus.SC_ACCEPTED).when()
                .post();
        String location = response.getHeader("location");
        Assert.assertNotNull("No location header in ACCEPTED- response", location);
        //we assume the next request is perfomed before the task finished
        Response response2 = RestAssured.given().header("Accept", "text/turtle")
                .expect().statusCode(HttpStatus.SC_ACCEPTED)
                .header("Content-Type", "text/turtle").when()
                .get(location);
        int count = 0;
        while (response2.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            response2 = RestAssured.given().header("Accept", "text/turtle")
                .expect()
                .when()
                .get(location);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (count++ > 65) {
                throw new RuntimeException("Async job not ending");
            }
        }
        Assert.assertEquals("Didn't get a 200 response eventually", HttpStatus.SC_OK, response2.getStatusCode());
        Assert.assertTrue("Result doesn't contain originally posted text", response2.getBody().asString().contains("hello"));
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
