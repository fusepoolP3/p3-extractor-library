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
package eu.fusepool.p3.transformer.server.handler;

import eu.fusepool.p3.transformer.AsyncTransformer;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author reto
 */
class AsyncTransformerHandler extends TransformerHandler  {

    private final AsyncTransformer transformer;
    private final ASyncResponsesManager aSyncResponsesManager = new ASyncResponsesManager();

    AsyncTransformerHandler(AsyncTransformer transformer) {
        super(transformer);
        this.transformer = transformer;
        transformer.activate(aSyncResponsesManager);
    }

    @Override
    protected void handlePost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        aSyncResponsesManager.handlePost(request, response, transformer);
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestUri = request.getRequestURI();
        if (requestUri.startsWith(ASyncResponsesManager.JOB_URI_PREFIX)) {
            aSyncResponsesManager.handleJobRequest(request, response, transformer);
        } else {
            super.handleGet(request, response);
        }
    }

    
    

}
