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
package eu.fusepool.p3.transformer.server.handler;

import eu.fusepool.p3.transformer.AsyncTransformer;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.vocab.TRANSFORMER;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 *
 * @author Reto
 */
public class ASyncResponsesManager implements AsyncTransformer.CallBackHandler {

    static final String JOB_URI_PREFIX = "/job/";

    private final Map<String, RequestResult> requestResults = new HashMap<String, RequestResult>();

    @Override
    public void responseAvailable(String requestId, Entity response) {
        requestResults.put(requestId, new RequestResult(response));
    }

    @Override
    public void reportException(String requestId, Exception ex) {
        requestResults.put(requestId, new RequestResult(ex));
    }

    boolean handleJobRequest(HttpServletRequest request, HttpServletResponse response, AsyncTransformer transformer) throws IOException {
        final String requestUri = request.getRequestURI();
        final RequestResult requestResult = requestResults.get(requestUri);
        if (requestResult == null) {
            if (transformer.isActive(requestUri)) {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                GraphNode responseNode = AbstractTransformingServlet.getServiceNode(request);
                responseNode.addProperty(TRANSFORMER.status, TRANSFORMER.Processing);
                AbstractTransformingServlet.respondFromNode(response, responseNode);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
            return true;
        }
        if (requestResult.exception != null) {
            if (requestResult.exception instanceof TransformerException) {
                TransformerException te = (TransformerException) requestResult.exception;
                response.setStatus(te.getStatusCode());
                TransformerServlet.writeResponse(te.getResponseEntity(), response);
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            requestResult.exception.printStackTrace(out);
            out.flush();
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(requestResult.entity.getType().toString());
            ServletOutputStream out = response.getOutputStream();
            requestResult.entity.writeData(out);
            out.flush();
        }
        return true;
    }

    String handlePost(HttpServletRequest request, HttpServletResponse response, AsyncTransformer transformer) throws IOException {
        final String requestId = ASyncResponsesManager.JOB_URI_PREFIX + UUID.randomUUID().toString();
        transformer.transform(new HttpRequestEntity(request), requestId);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        response.setHeader("Location", requestId);
        response.getOutputStream().flush();
        return requestId;
    }

    static class RequestResult {

        public RequestResult(Exception exception) {
            this.exception = exception;
            entity = null;
        }

        public RequestResult(Entity entity) {
            this.entity = entity;
            exception = null;
        }

        final Entity entity;
        final Exception exception;
    }

}
