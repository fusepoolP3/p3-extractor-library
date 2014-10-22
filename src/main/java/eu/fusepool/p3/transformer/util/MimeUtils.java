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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * {@link MimeUtils} contains utility methods and constants useful when
 * dealing with {@link javax.activation.MimeType}s.
 *
 * @author Giuliano Mega
 */
public class MimeUtils {

    public static final MimeType WILDCARD_TYPE = mimeType("*/*");

    public static final String MIME_TYPE_WILDCARD = "*";

    /**
     * Creates a new {@link javax.activation.MimeType} from a string, but issues a
     * {@link RuntimeException} instead of a checked {@link MimeTypeParseException}
     * in case the string cannot be parsed. Useful when creating constants for which
     * we are certain that no exception should ensue. E.g.:
     * <code>
     *     public class MyClass {
     *         public final MimeType MY_MIME = mimeType("application/my-mime");
     *
     *         ...
     *     }
     * </code>
     *
     * @param type a MIME type as a {@link String} (e.g. "text/html").
     *
     * @return the corresponding {@link MimeType}.
     *
     * @throws java.lang.RuntimeException if the {@link MimeType} constructor
     *         throws {@link MimeTypeParseException}.
     */
    public static MimeType mimeType(String type) {
        try {
            return new MimeType(type);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

}
