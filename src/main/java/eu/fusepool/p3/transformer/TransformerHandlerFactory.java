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

/**
 *
 * @author reto
 */
public class TransformerHandlerFactory {
    public static AbstractTransformingHandler getTransformerHandler(SyncTransformer transformer) {
       if (transformer.isLongRunning()) {
            return new AsyncTransformerHandler(new LongRunningTransformerWrapper(transformer));
        } else {
            return new SyncTransformerHandler(transformer);
        }
    }
    
    public static AbstractTransformingHandler getTransformerHandler(AsyncTransformer transformer) {
        return new AsyncTransformerHandler(transformer);
    }
    
    public static AbstractTransformingHandler getTransformerHandler(Transformer transformer) {
        if (transformer instanceof SyncTransformer) {
            return TransformerHandlerFactory.getTransformerHandler((SyncTransformer)transformer);
        } else {
            return TransformerHandlerFactory.getTransformerHandler((AsyncTransformer)transformer);
        }
        
    }
}
