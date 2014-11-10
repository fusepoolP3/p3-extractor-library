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
package eu.fusepool.p3.transformer.server;

import com.thetransactioncompany.cors.CORSFilter;
import eu.fusepool.p3.transformer.server.handler.TransformerFactoryServlet;
import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.server.handler.TransformerHandlerFactory;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * @author reto
 */
public class TransformerServer {

    private final Server server;
    final ServletHandler handler = new ServletHandler();
    /**
     * 
     * @param port the port the server shall listen too
     * @param cors if CORS support should should be enables
     */
    public TransformerServer(int port, boolean cors) {
        server = new Server(port);
        server.setHandler(handler);
        if (cors) {
            handler.addFilterWithMapping(new FilterHolder(new CORSFilter()), "/", 
                    EnumSet.of(DispatcherType.FORWARD, DispatcherType.INCLUDE,DispatcherType.REQUEST));
        }
    }
    
    /**
     * 
     * @param transformer
     * @throws Exception ugly, but so does the underlying Jetty Server
     */
    public void start(Transformer transformer) throws Exception {    
        handler.addServletWithMapping(new ServletHolder(TransformerHandlerFactory.getTransformerHandler(transformer)),"/");  
        server.start();   
    }
    
    public void start(TransformerFactory factory) throws Exception {
        handler.addServletWithMapping(new ServletHolder(new TransformerFactoryServlet(factory)),"/");
        server.start();   
    }
    
    public void join() throws InterruptedException {
        server.join();
    }
}
