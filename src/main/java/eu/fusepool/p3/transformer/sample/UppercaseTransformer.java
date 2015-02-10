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
package eu.fusepool.p3.transformer.sample;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.WritingEntity;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.commons.io.IOUtils;

public class UppercaseTransformer implements SyncTransformer {

    private static final MimeType MIME_TEXT_PLAIN;

    static {
        try {
            MIME_TEXT_PLAIN = new MimeType("text/plain");
        } catch (MimeTypeParseException ex) {
            // Should never happen.
            throw new RuntimeException("Internal error.");
        }
    }

    private static final Set<MimeType> IO_FORMAT
            = Collections.singleton(MIME_TEXT_PLAIN);

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        return IO_FORMAT;
    }

    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        return IO_FORMAT;
    }

    @Override
    public Entity transform(HttpRequestEntity entity) throws IOException {

        // Reads the text to be transformed.
        String original = IOUtils.toString(entity.getData());

        // Transforms.
        final String transformed = original.toUpperCase();

        // Sends back the reply.
        return new WritingEntity() {
            @Override
            public MimeType getType() {
                return MIME_TEXT_PLAIN;
            }

            @Override
            public void writeData(OutputStream out) throws IOException {
                out.write(transformed.getBytes());
            }
        };
    }
    

    @Override
    public boolean isLongRunning() {
        return false;
    }

    public static void main(String[] args) throws Exception {
        TransformerServer server = new TransformerServer(8080, true);
        server.start(new UppercaseTransformer());
        server.join();
    }
}
