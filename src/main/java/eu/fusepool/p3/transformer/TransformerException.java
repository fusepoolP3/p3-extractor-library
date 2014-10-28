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

import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.InputStreamEntity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * A Transformer can throw a TransformerException when a special status code 
 * should be returned to the client.
 */
public class TransformerException extends RuntimeException {
    private final int statusCode;
    private final Entity responseEntity;

    public TransformerException(int statusCode, final String message) {
        this.statusCode = statusCode;
        this.responseEntity = new InputStreamEntity() {

            @Override
            public MimeType getType() {
                try {
                    return new MimeType("text", "plain");
                } catch (MimeTypeParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(message.getBytes("UTF-8"));
            }
        };
    }
    
    public TransformerException(int statusCode, Entity responseEntity) {
        this.statusCode = statusCode;
        this.responseEntity = responseEntity;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Entity getResponseEntity() {
        return responseEntity;
    }
    
    
    
}
