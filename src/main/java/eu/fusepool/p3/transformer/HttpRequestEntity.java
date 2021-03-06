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

import eu.fusepool.p3.transformer.commons.util.InputStreamEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import eu.fusepool.p3.accept.util.AcceptPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An entity allowing access to the {@link HttpServletRequest} it originates from.
 *
 * @author reto
 */
public class HttpRequestEntity extends InputStreamEntity {

    private final HttpServletRequest request;
    private static final Logger log = LoggerFactory.getLogger(HttpRequestEntity.class);

    public HttpRequestEntity(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public MimeType getType() {
        String requestCt = request.getContentType();
        try {
            if (requestCt == null) {
                return new MimeType("application/octet-stream");
            } else {
                return new MimeType(requestCt);
            }
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public URI getContentLocation() {
        final String contentLocation = request.getHeader("Content-Location");
        if (contentLocation != null) {
            try {
                return new URI(contentLocation);
            } catch (URISyntaxException ex) {
                log.warn("Content-Location not a URI " + contentLocation, ex);
            }
        }
        return null;
    }

    @Override
    public InputStream getData() throws IOException {
        return request.getInputStream();
    }

    /**
     * @return the underlying Servlet Request, need e.g. for content negotiation
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @return the {@link AcceptPreference} associated to the request originating
     * this {@link HttpRequestEntity}.
     */
    public AcceptPreference getAcceptPreference() {
        return AcceptPreference.fromRequest(request);
    }


    /**
     * @return the {@link MimeType} with the highest quality parameter amongst the ones
     * specified in the {@link AcceptPreference}. Shortcut for <code>getAcceptPreference().getPreferredAccept()</code>
     */
    public MimeType getPreferredAccept() {
        return getAcceptPreference().getPreferredAccept();
    }

}
