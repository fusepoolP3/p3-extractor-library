/*
 * Copyright 2014 reto.
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
import eu.fusepool.p3.transformer.LongRunningTransformerWrapper;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.commons.Entity;
import static eu.fusepool.p3.transformer.server.handler.TransformerHandler.writeResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author reto
 */
public class TransformerFactoryHandler extends AbstractHandler {

    private final TransformerFactory factory;
    private ASyncResponsesManager aSyncResponsesManager = new ASyncResponsesManager();
    private final Map<String, AsyncTransformer> requestId2Transformer = new HashMap<>();
    private final Set<AsyncTransformer> aSyncTransformerSet = Collections.newSetFromMap(
        new WeakHashMap<AsyncTransformer, Boolean>());
    public TransformerFactoryHandler(TransformerFactory factory) {
        this.factory = factory;
    }
    
    @Override
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

    public void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final Transformer transformer = wrapLongRunning(getTransformer(request, response));
        if (transformer == null) {
            response.sendError(404);
        } else {
            if (transformer instanceof SyncTransformer) {
                new SyncTransformerHandler((SyncTransformer) transformer).handlePost(request, response);
            } else {
                final AsyncTransformer aSyncTransformer = (AsyncTransformer) transformer;
                synchronized (aSyncTransformerSet) {
                    if (!aSyncTransformerSet.contains(aSyncTransformer)) {
                        aSyncTransformerSet.add(aSyncTransformer);
                        aSyncTransformer.activate(aSyncResponsesManager);
                    }
                }
                final String requestId = aSyncResponsesManager.handlePost(request, response, aSyncTransformer);
                requestId2Transformer.put(requestId, aSyncTransformer);
            }
        }
    }

    private void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestUri = request.getRequestURI();
        if (requestUri.startsWith(ASyncResponsesManager.JOB_URI_PREFIX)) {
            final AsyncTransformer transformer = requestId2Transformer.get(requestUri);
            if (transformer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                if (!aSyncResponsesManager.handleJobRequest(request, response, transformer)) {
                    requestId2Transformer.remove(requestUri);
                }
            }
        } else {
            final Transformer transformer = getTransformer(request, response);
            if (transformer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                AbstractTransformingHandler handler = TransformerHandlerFactory.getTransformerHandler(transformer);
                handler.handleGet(request, response);
            }
        }
    }
    
    private Transformer getTransformer(HttpServletRequest request, HttpServletResponse response) throws IOException {
         try {
            return factory.getTransformer(request);
        } catch (TransformerException e) {
            response.setStatus(e.getStatusCode());
            writeResponse(e.getResponseEntity(), response);
            throw e;
        }      
    }

    private Transformer wrapLongRunning(Transformer transformer) {
        if (transformer instanceof SyncTransformer) {
            SyncTransformer syncTransformer = (SyncTransformer) transformer;
            if (syncTransformer.isLongRunning()) {
                return new LongRunningTransformerWrapper(syncTransformer);
            }
        } 
        return transformer;
    }

}
