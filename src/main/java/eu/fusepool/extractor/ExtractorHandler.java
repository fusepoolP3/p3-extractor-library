/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepool.extractor;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.util.InputStreamEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author reto
 */
public class ExtractorHandler extends AbstractExtractorHandler {

    private Extractor extractor;

    public ExtractorHandler(Extractor extractor) {
        this.extractor = extractor;
    }

    @Override
    protected Set<MimeType> getSupportedInputFormats() {
        return extractor.getSupportedInputFormats();
    }

    @Override
    protected Set<MimeType> getSupportedOutputFormats() {
        return extractor.getSupportedOutputFormats();
    }

    @Override
    protected void handlePost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        Entity responseEntity = extractor.extract(new HttpRequestEntity(request));
        response.setContentType(responseEntity.getType().toString());
        responseEntity.writeData(response.getOutputStream());
        response.getOutputStream().flush();       
    }

}
