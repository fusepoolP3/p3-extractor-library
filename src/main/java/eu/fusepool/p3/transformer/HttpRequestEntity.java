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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import eu.fusepool.p3.transformer.util.AcceptHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An entity allowing access to the {@link HttpServletRequest} it originates from.
 *
 * @author reto
 */
public class HttpRequestEntity extends InputStreamEntity {

    private final HttpServletRequest request;
    private final List<AcceptHeader> acceptHeaders;
    private static final Logger log = LoggerFactory.getLogger(HttpRequestEntity.class);

    public HttpRequestEntity(HttpServletRequest request) {
        this.request = request;
        this.acceptHeaders = Collections.unmodifiableList(AcceptHeader.fromRequest(request));
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
     * @return the first {@link AcceptHeader} associated to the request originating
     * this {@link HttpRequestEntity}. If the request lacks an accept header,
     * this will be {@link AcceptHeader#NULL_HEADER}.
     */
    public AcceptHeader getAcceptHeader() {
        return acceptHeaders.get(0);
    }

    /**
     * @return a readonly list with all of the {@link AcceptHeader}s associated to the
     * request originating this {@link HttpRequestEntity}. If the request lacks and accept
     * header, the list will contain a single element -- {@link AcceptHeader#NULL_HEADER}.
     */
    public List<AcceptHeader> getAcceptHeaders() {
        return acceptHeaders;
    }

    /**
     * Utility method.
     *
     * @return a single {@link AcceptHeader} that is semantically equivalent (as per RFC2616)
     * to the set of headers passed to this request. Generated with
     * {@link AcceptHeader#fromHeaders(java.util.List)}.
     */
    public AcceptHeader getMergedAcceptHeader() {
        return AcceptHeader.fromHeaders(acceptHeaders);
    }

}
