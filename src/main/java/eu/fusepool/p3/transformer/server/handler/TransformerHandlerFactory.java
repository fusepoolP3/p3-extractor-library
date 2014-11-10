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
import eu.fusepool.p3.transformer.LongRunningTransformerWrapper;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.Transformer;

/**
 *
 * @author reto
 */
public class TransformerHandlerFactory {
    public static AbstractTransformingServlet getTransformerHandler(SyncTransformer transformer) {
       if (transformer.isLongRunning()) {
            return new AsyncTransformerServlet(new LongRunningTransformerWrapper(transformer));
        } else {
            return new SyncTransformerServlet(transformer);
        }
    }
    
    public static AbstractTransformingServlet getTransformerHandler(AsyncTransformer transformer) {
        return new AsyncTransformerServlet(transformer);
    }
    
    public static AbstractTransformingServlet getTransformerHandler(Transformer transformer) {
        if (transformer instanceof SyncTransformer) {
            return TransformerHandlerFactory.getTransformerHandler((SyncTransformer)transformer);
        } else {
            return TransformerHandlerFactory.getTransformerHandler((AsyncTransformer)transformer);
        }
        
    }
}
