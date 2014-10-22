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

    private static final String RFC2616_HEADER = "Accept";

    private static final String RFC2616_MEDIA_SEPARATOR = ",";

    /**
     * Constant representing a <code>null</code> accept header, which amounts to an accept header
     * containing only the wildcard mime type (*), and thus matching everything.
     */
    public static final AcceptHeader NULL_HEADER = new AcceptHeader(null);

    static class AcceptHeaderEntry implements Comparable<AcceptHeaderEntry> {

        private MimeTypeComparator mediaTypeComparator = new MimeTypeComparator();
        MimeType mediaType;
        int quality; //from 0 to 1000

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
     * Constructs an {@link AcceptHeader} from an {@link javax.servlet.http.HttpServletRequest}.
     *
     * @param request the request to extract the {@link AcceptHeader} from.
     * @return the request's {@link AcceptHeader}, {@link AcceptHeader#NULL_HEADER} if none is found.
     */
    public static AcceptHeader fromRequest(HttpServletRequest request) {
        String header = request.getHeader(RFC2616_HEADER);
        if (header == null) {
            return NULL_HEADER;
        }

        List<String> entries = new ArrayList<String>();
        for (String entry : header.split(RFC2616_MEDIA_SEPARATOR)) {
            entries.add(entry);
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
        for(AcceptHeaderEntry clientSupported : entries) {
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