/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.fusepool.p3.transformer.util;

import java.util.*;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.fusepool.p3.transformer.util.MimeUtils.mimeType;

/**
 * @author reto
 * @author Giuliano Mega
 */
public class AcceptHeader {

    @Override
    public String toString() {
        return entries.toString();
    }

    private static final Logger logger = LoggerFactory.getLogger(AcceptHeader.class);

    public static final String RFC2616_HEADER = "Accept";

    public static final String RFC2616_MEDIA_SEPARATOR = ",";

    /**
     * Constant representing a <code>null</code> accept header, which amounts to an accept header
     * containing only the wildcard mime type (*), and thus matching everything.
     */
    public static final AcceptHeader NULL_HEADER = new AcceptHeader(Collections.EMPTY_LIST);

    public static class AcceptHeaderEntry implements Comparable<AcceptHeaderEntry> {

        private final MimeTypeComparator mediaTypeComparator = new MimeTypeComparator();
        final MimeType mediaType;
        final int quality; //from 0 to 1000

        AcceptHeaderEntry(MimeType mediaType) {
            MimeTypeParameterList parametersWithoutQ = mediaType.getParameters();

            String qValue = parametersWithoutQ.get("q");
            parametersWithoutQ.remove("q");

            this.mediaType = mimeType(mediaType.getBaseType() + parametersWithoutQ.toString());

            if (qValue == null) {
                quality = 1000;
            } else {
                quality = (int) (Float.parseFloat(qValue) * 1000);
            }
        }

        @Override
        public int compareTo(AcceptHeaderEntry o) {
            if (equals(o)) {
                return 0;
            }
            if (quality == o.quality) {
                return mediaTypeComparator.compare(mediaType, o.mediaType);
            }
            return (o.quality - quality);
        }

        @Override
        public String toString() {
            return mediaType + " with q=" + quality + ";";
        }

    }

    /**
     * Constructs an {@link AcceptHeader} array from an {@link javax.servlet.http.HttpServletRequest}.
     * The array will contain as many elements as there are accept headers in the request.
     *
     * @param request the request to extract the {@link AcceptHeader} from.
     *
     * @return an array with all of the the request's {@link AcceptHeader}s or, in case the request
     *         contains no accept headers, an array containing {@link AcceptHeader#NULL_HEADER}. This
     *         is consistent with RFC2161 in that if the client omits the accept header, then it should
     *         accept anything.
     */
    public static List<AcceptHeader> fromRequest(HttpServletRequest request) {
        ArrayList<AcceptHeader> headers = new ArrayList<AcceptHeader>();
        Enumeration<String> strHeaders = request.getHeaders(RFC2616_HEADER);
        while (strHeaders.hasMoreElements()) {
            headers.add(fromString(strHeaders.nextElement()));
        }

        if (headers.size() == 0) {
            headers.add(NULL_HEADER);
        }

        return headers;
    }

    /**
     * @return a new {@link AcceptHeader} from a RFC2161 media/quality list. Example:
     * <code>
     *      fromString("image/png;q=1.0,image/*;q=0.7,text/plain;q=0.5");
     * </code>
     */
    public static AcceptHeader fromString(String header) {
        if (header == null) {
            throw new NullPointerException("Header string can't be null.");
        }

        List<String> entries = new ArrayList<String>();
        for (String entry : header.split(RFC2616_MEDIA_SEPARATOR)) {
            entries.add(entry);
        }

        return new AcceptHeader(entries);
    }

    /**
     * Constructs an {@link AcceptHeader} that is equivalent to a set of headers passed
     * as part of a single HTTP request.
     *
     * @param headers a collection of accept headers that are part of single request.
     *
     * @return an {@link AcceptHeader} that corresponds to the merge of all headers in
     * the parameter list. Example:
     *
     * <code>
     *     AcceptHeader merged = fromHeaders(fromString("text/html;q=1.0"),
     *                                       fromString("image/png;q=0.5"));
     * </code>
     *
     * is equivalent to:
     *
     * <code>
     *     AcceptHeader merged = fromString("text/html;q=1.0,image/png;q=0.5");
     * </code>
     */
    public static AcceptHeader fromHeaders(List<AcceptHeader> headers) {
        if (headers.size() == 0) {
            throw new IllegalArgumentException("Header list must contain at least one element.");
        }

        TreeSet<AcceptHeaderEntry> entries = new TreeSet<>();
        for (AcceptHeader header : headers) {
            // It's OK to do this as AcceptHeaderEntry is immutable.
            entries.addAll(header.entries);
        }

        return new AcceptHeader(entries);
    }

    private TreeSet<AcceptHeaderEntry> entries = new TreeSet<AcceptHeaderEntry>();

    public AcceptHeader(List<String> entryStrings) {
        if ((entryStrings == null) || (entryStrings.size() == 0)) {
            entries.add(new AcceptHeaderEntry(MimeUtils.WILDCARD_TYPE));
        } else {
            for (String string : entryStrings) {
                try {
                    entries.add(new AcceptHeaderEntry(new MimeType(string)));
                } catch (MimeTypeParseException ex) {
                    logger.warn("The string \"" + string + "\" is not a valid mediatype", ex);
                }
            }
        }
    }

    protected AcceptHeader(TreeSet<AcceptHeaderEntry> entries) {
        this.entries = entries;
    }

    /**
     * @return a sorted list of the {@link AcceptHeaderEntry} that compose this
     * {@link AcceptHeader}.
     */
    public List<AcceptHeaderEntry> getEntries() {
        List<AcceptHeaderEntry> result = new ArrayList<AcceptHeaderEntry>();
        for (AcceptHeaderEntry entry : entries) {
            result.add(entry);
        }
        return result;
    }

    /**
     * @return the {@link MimeType} with the highest quality parameter amongst the ones
     * specified in this {@link AcceptHeader}.
     */
    public MimeType getPreferredAccept() {
        return entries.pollFirst().mediaType;
    }

    /**
     * Given a set of supported {@link MimeType}s, returns the one that best
     * satisfies this accept header.
     *
     * @param supportedTypes a set of supported {@link MimeType}s.
     * @return the best candidate in the set, or <code>null</code> if the
     * header does not allow any of supported {@link MimeType}s.
     */
    public MimeType getPreferredAccept(Set<MimeType> supportedTypes) {
        // Starts from the highest.
        for (AcceptHeaderEntry clientSupported : entries) {
            for (MimeType serverSupported : supportedTypes) {
                if (isSameOrSubtype(serverSupported, clientSupported.mediaType)) {
                    return serverSupported;
                }
            }
        }

        return null;
    }

    /**
     * @param type
     * @return a value from 0 to 1000 to indicate the quality in which type is accepted
     */
    public int getAcceptedQuality(MimeType type) {
        for (AcceptHeaderEntry acceptHeaderEntry : entries) {
            if (isSameOrSubtype(type, acceptHeaderEntry.mediaType)) {
                return acceptHeaderEntry.quality;
            }
        }

        Object[] reverseEntries = entries.toArray();
        for (int i = entries.size() - 1; i >= 0; i--) {
            AcceptHeaderEntry entry = (AcceptHeaderEntry) reverseEntries[i];
            if (isSameOrSubtype(entry.mediaType, type)) {
                return entry.quality;
            }
        }

        return 0;
    }

    /**
     * @param t1
     * @param t2
     * @return true if t1 is the same or a subtype ot t2 such as when t1 is
     * text/plain and t2 is text/*
     */
    private boolean isSameOrSubtype(MimeType t1, MimeType t2) {
        String type1 = t1.getPrimaryType();
        String subtype1 = t1.getSubType();
        String type2 = t2.getPrimaryType();
        String subtype2 = t2.getSubType();

        if (type2.equals(MimeUtils.MIME_TYPE_WILDCARD) && subtype2.equals(MimeUtils.MIME_TYPE_WILDCARD)) {
            return true;
        } else if (type1.equalsIgnoreCase(type2) && subtype2.equals(MimeUtils.MIME_TYPE_WILDCARD)) {
            return true;
        } else {
            return type1.equalsIgnoreCase(type2) && subtype1.equalsIgnoreCase(subtype2);
        }
    }
}