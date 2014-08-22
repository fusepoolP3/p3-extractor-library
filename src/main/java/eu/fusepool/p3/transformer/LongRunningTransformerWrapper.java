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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.activation.MimeType;

/**
 *
 * @author reto
 */
class LongRunningTransformerWrapper implements AsyncTransformer {
    private CallBackHandler callBackHandler;
    private final SyncTransformer wrapped;
    private final Set<String> activeRequests = Collections.synchronizedSet(new HashSet<String>());

    public LongRunningTransformerWrapper(SyncTransformer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void activate(CallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
    }

    @Override
    public void transform(final HttpRequestEntity requestEntity, final String requestId) throws IOException {
        activeRequests.add(requestId);
        final PreReadEntity preReadEntity = new PreReadEntity(requestEntity);
        (new Thread() {

            @Override
            public void run() {
                try {
                    callBackHandler.responseAvailable(requestId, wrapped.transform(preReadEntity));
                } catch (Exception ex) {
                    callBackHandler.reportException(requestId, ex);
                } 
                activeRequests.remove(requestId);
            }
            
        }).start();
        
    }

    @Override
    public boolean isActive(String requestId) {
        return activeRequests.contains(requestId);
    }

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        return wrapped.getSupportedInputFormats();
    }

    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        return wrapped.getSupportedOutputFormats();
    }
    
}
