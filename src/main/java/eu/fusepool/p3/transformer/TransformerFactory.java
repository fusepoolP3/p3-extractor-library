/*
 * Copyright (C) 2014 Bern University of Applied Sciences.
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

import javax.servlet.http.HttpServletRequest;

/**
 * A TransformerFactory returns a Transformer based on the HttpServletRequest.
 * Typically it will return different transformers depending on the
 * requested path.
 * 
 * @author reto
 */
public interface TransformerFactory {
    
    /**
     * Returns a transformer for a specific request.
     * 
     * @param request
     * @return 
     * @throws TransformerException when transformation is not possible for this request 
     */
    Transformer getTransformer(HttpServletRequest request);
    
}
