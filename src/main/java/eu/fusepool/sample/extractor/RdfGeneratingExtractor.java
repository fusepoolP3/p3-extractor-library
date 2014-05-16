/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.fusepool.sample.extractor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;


public abstract class RdfGeneratingExtractor extends AbstractExtractorHandler {

    @Override
    protected Set<MimeType> getSupportedOutputFormats() {
        final Set<String> supportedFormats = Serializer.getInstance().getSupportedFormats();
        final Set<MimeType> result = new HashSet<MimeType>();
        for (String string : supportedFormats) {
            try {
                result.add(new MimeType(string));
            } catch (MimeTypeParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
    
    protected void handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //TODO check content type matches supportedInputFormat
        TripleCollection generatedRdf = generateRdf(request);
        //TODO content negotiation
        final String responseFormat = SupportedFormat.TURTLE;
        response.setContentType(responseFormat);
        response.setStatus(HttpServletResponse.SC_OK);
        //baseRequest.setHandled(true);
        Serializer.getInstance().serialize(response.getOutputStream(), generatedRdf, responseFormat);
    }

    protected abstract TripleCollection generateRdf(HttpServletRequest request) throws IOException, ServletException;
    
}
