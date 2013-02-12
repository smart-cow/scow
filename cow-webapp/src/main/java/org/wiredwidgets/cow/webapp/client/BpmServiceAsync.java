/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author JKRANES
 */
public interface BpmServiceAsync {

    public void getProcess(String input, AsyncCallback<String> callback);

    public void getNativeProcess(String key, AsyncCallback<String> asyncCallback);

    public void createDeployment(String processXml, AsyncCallback<String> asyncCallback);

    public void createNativeDeployment(String processXml, AsyncCallback<String> asyncCallback);

    public void getBaseURL(AsyncCallback<String> asyncCallback);

    public void getForObject(String url, String[] args, AsyncCallback<String> asyncCallback);

    public void postForObject(String url, String request, String[] args, AsyncCallback<String> asyncCallback);

    public void postForLocation(String url, String request, String[] args, AsyncCallback<String> asyncCallback);

    public void delete(String url, String[] args, AsyncCallback<Void> asyncCallback);
    
    public void delete(String url, AsyncCallback<Void> asyncCallback);

    public void postForNoContent(String url, String[] args, AsyncCallback<Void> asyncCallback);

}
