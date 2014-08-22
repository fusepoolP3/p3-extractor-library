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
package eu.fusepool.p3.transformer;

import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.vocab.TRANSFORMER;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 *
 * @author reto
 */
class AsyncTransformerHandler extends TransformerHandler implements AsyncTransformer.CallBackHandler {

    private final AsyncTransformer transformer;
    private final Map<String, RequestResult> requests = new HashMap<String, RequestResult>();
    private static final String JOB_URI_PREFIX = "/job/";

    AsyncTransformerHandler(AsyncTransformer transformer) {
        super(transformer);
        this.transformer = transformer;
        transformer.activate(this);
    }

    @Override
    protected void handlePost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        final String requestId = JOB_URI_PREFIX + UUID.randomUUID().toString();
        transformer.transform(new HttpRequestEntity(request), requestId);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        response.setHeader("Location", requestId);
        response.getOutputStream().flush();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestUri = request.getRequestURI();
        if (requestUri.startsWith(JOB_URI_PREFIX)) {
            final RequestResult requestResult = requests.get(requestUri);
            if (requestResult == null) {
                if (transformer.isActive(requestUri)) {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                    GraphNode responseNode = getServiceNode(request);
                    responseNode.addProperty(TRANSFORMER.status,
                            TRANSFORMER.Processing);
                    respondFromNode(response, responseNode);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }               
                return;
            }

            if (requestResult.exception != null) {
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
        } else {
            super.handleGet(request, response);
        }
    }

    @Override
    public void responseAvailable(String requestId, Entity response
    ) {
        requests.put(requestId, new RequestResult(response));
    }

    @Override
    public void reportException(String requestId, Exception ex
    ) {
        requests.put(requestId, new RequestResult(ex));
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
