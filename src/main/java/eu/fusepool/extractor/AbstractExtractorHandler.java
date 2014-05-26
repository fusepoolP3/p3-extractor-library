/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepool.extractor;

import java.io.IOException;
import java.util.Set;
import javax.activation.MimeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public abstract class AbstractExtractorHandler extends AbstractHandler {

    protected abstract Set<MimeType> getSupportedInputFormats();
    protected abstract Set<MimeType> getSupportedOutputFormats();
    
    public void handle(String target, Request baseRequest, 
            HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        if (request.getMethod().equals("GET")) {
            handleGet(request, response);
            return;
        }
        if (request.getMethod().equals("POST")) {
            handlePost(request, response);
            return;
        }
        //TODO support at least HEAD
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private String getFullRequestUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        if (request.getQueryString() != null) {
            requestURL.append("?").append(request.getQueryString());
        }
        return requestURL.toString();
    }

    protected void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final UriRef serviceUri = new UriRef(getFullRequestUrl(request));
        final MGraph resultGraph = new SimpleMGraph();
        final GraphNode node = new GraphNode(serviceUri, resultGraph);
        for (MimeType mimeType : getSupportedInputFormats()) {
            node.addPropertyValue(new UriRef("http://fusepool.eu/ontology/p3#supportedInputFormat"), mimeType.toString());
        }
        for (MimeType mimeType : getSupportedOutputFormats()) {
            node.addPropertyValue(new UriRef("http://fusepool.eu/ontology/p3#supportedOutputFormat"), mimeType.toString());
        }
        //TODO content negotiation
        final String responseFormat = SupportedFormat.TURTLE;
        response.setContentType(responseFormat);
        response.setStatus(HttpServletResponse.SC_OK);
        //baseRequest.setHandled(true);
        Serializer.getInstance().serialize(response.getOutputStream(), resultGraph, responseFormat);
    }

    protected abstract void handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}
