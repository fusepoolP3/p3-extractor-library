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
import java.util.Set;
import javax.activation.MimeType;

/**
 *
 * @author reto
 */
abstract class TransformerHandler extends AbstractTransformingHandler {
    private final Transformer transformer;

    public TransformerHandler(Transformer transformer) {
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
    
}
