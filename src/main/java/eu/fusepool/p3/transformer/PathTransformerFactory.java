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

package eu.fusepool.p3.transformer;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * An implementation of TransfomerFactory allowing the registration of different 
 * transformers at different paths. Currently this only supports SyncTransformers,
 * as request to the task page cannot be forwarded.
 * 
 * @author reto
 */
public class PathTransformerFactory implements TransformerFactory {
    
    final private Map<String,Transformer> pathTransformerMap =new HashMap<>();

    @Override
    public Transformer getTransformer(HttpServletRequest request) {
        final String requestedPath = request.getRequestURI();
        return pathTransformerMap.get(requestedPath);
    }
    
    public void registerTransformer(String path, Transformer transformer) {
        pathTransformerMap.put(path, transformer);
    }
    
    public Transformer remove(String path) {
        return pathTransformerMap.remove(path);
    }
    
}
