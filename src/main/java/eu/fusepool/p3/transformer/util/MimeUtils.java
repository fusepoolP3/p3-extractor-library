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
     * {@link RuntimeException} instead of a checked {@link MimeTypeParseException}.
     * Useful when creating constants for which we are certain that no parse
     * exception should ensue. E.g.
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
     *         throws {@link MimeTypeParseException}
     */
    public static MimeType mimeType(String type) {
        try {
            return new MimeType(type);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

}
