/*
 * Copyright 2014 Bern University of Applied Sciences.
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

import eu.fusepool.p3.transformer.commons.Entity;
import java.io.IOException;


/**
 * For asynchronous transformer the response entity is provided to a CallBackHandler
 (typically) after the transform method returned.
 * 
 * The reason why there is a single callBackHandler per transformer and not one
 CallBackHandler per request (i.e. per invocation of the transform method) is to
 allow transformation tasks to survive restarts of the server. For example when
 a transformation is requested the transform method might send and email, a thread
 checking the mailbox for an email answer might be started by the 
 activate method. The email answer would contain the requestId allowing to map 
 a received answer email to the original request.
 * 
 * @author reto
 */
public interface AsyncTransformer extends Transformer {

    public interface  CallBackHandler {
        abstract void responseAvailable(String requestId, Entity response);

        public void reportException(String requestId, Exception ex);
    }

    void activate(CallBackHandler callBackHandler);
    
    void transform(HttpRequestEntity entity, String requestId) throws IOException;
    
    /**
     * Checks if a requestId is being processed by the Transformer. The Transformer
     * should return true if CallBackHandler.responseAvailable might be called
     * for the given requestId.
     * 
     * @param requestId the requestId
     * @return true if the Transformer is processing a request, false otherwise.
     */
    boolean isActive(String requestId);


}
