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
import java.io.IOException;

/**
 * Synchronous Java API for transformer services. Even services implementing this 
 * interface will be exposed using the asynchronous REST API if they are marked
 * as long-running.
 * 
 * @author reto
 */
public interface SyncTransformer extends Transformer {


    /**
     * Transforms an Entity. Note that even though this gets a HttpRequestEntity
     * the HttpServletRequest might not be usable if the HTTP request already
     * returned, which is the case for long running transformers.
     * 
     * @param entity the entity to be transformer
     * @return the transformed entity
     * @throws IOException thrown when problems occur reading of writing the entity
     * @throws TransformerException when a transformation error occurs 
     */
    Entity transform(HttpRequestEntity entity) throws IOException;
    
    /**
     * Indicates if the transform method performs a long running task. In this
     * case the server will exposes the service using the asynchronous protocol.
     * 
     * @return true if this is a long running task, false otherwise
     */
    boolean isLongRunning();

}
