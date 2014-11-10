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

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.commons.Entity;
import static eu.fusepool.p3.transformer.server.handler.TransformerServlet.writeResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author reto
 */
class SyncTransformerServlet extends TransformerServlet {

    private SyncTransformer transformer;

    SyncTransformerServlet(SyncTransformer transformer) {
        super(transformer);
        this.transformer = transformer;
    }

    @Override
    protected void handlePost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            Entity responseEntity = transformer.transform(new HttpRequestEntity(request));
            writeResponse(responseEntity, response); 
        } catch (TransformerException e) {
            response.setStatus(e.getStatusCode());
            writeResponse(e.getResponseEntity(), response);
        }        
    }

}
