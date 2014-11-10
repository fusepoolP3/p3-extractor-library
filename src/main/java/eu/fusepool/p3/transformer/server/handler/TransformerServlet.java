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

import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.commons.Entity;
import java.io.IOException;
import java.util.Set;
import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author reto
 */
abstract class TransformerServlet extends AbstractTransformingServlet {
    private final Transformer transformer;

    public TransformerServlet(Transformer transformer) {
        this.transformer = transformer;
    }

    @Override
    protected Set<MimeType> getSupportedInputFormats() {
        return transformer.getSupportedInputFormats();
    }

    @Override
    protected Set<MimeType> getSupportedOutputFormats() {
        return transformer.getSupportedOutputFormats();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            super.handleGet(request, response); 
        } catch (TransformerException e) {
            response.setStatus(e.getStatusCode());
            writeResponse(e.getResponseEntity(), response);
        }
    }
    
    
    static void writeResponse(Entity responseEntity, HttpServletResponse response) throws IOException {
        response.setContentType(responseEntity.getType().toString());
        responseEntity.writeData(response.getOutputStream());
        response.getOutputStream().flush();
    }
    
}
