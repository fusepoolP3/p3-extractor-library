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
import eu.fusepool.p3.transformer.TransformerFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author reto
 */
public class TransformerFactoryHandler extends AbstractHandler {
    private final TransformerFactory factory;

    public TransformerFactoryHandler(TransformerFactory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(String target, Request baseRequest, 
            HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        final Transformer transformer = factory.getTransformer(request);
        if (transformer == null) {
            response.sendError(404);
        } else  {
            final Handler handler = TransformerHandlerFactory.getTransformerHandler(transformer);
            handler.handle(target, baseRequest, request, response);
        }
    }

   
}
