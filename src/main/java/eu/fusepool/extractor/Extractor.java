/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepool.extractor;

import java.io.IOException;
import java.util.Set;
import javax.activation.MimeType;

public interface Extractor {

    Set<MimeType> getSupportedInputFormats();

    Set<MimeType> getSupportedOutputFormats();

    Entity extract(HttpRequestEntity entity) throws IOException;

}
