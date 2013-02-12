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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *
 * @author JKRANES
 */
@RemoteServiceRelativePath("service")
public interface BpmService extends RemoteService {

    public String getProcess(String key);

    public String getNativeProcess(String key);

    public String createNativeDeployment(String processXml);

    public String createDeployment(String processXml);

    public String getBaseURL();

    /**
     * Calls a REST service that returns an XML object representation, using the Spring
     * Framework RestTemplate method of the same name.
     * @param url the URL of the REST service.  Can include URL template variables using
     * {variable} syntax, to be filled in by the arguments
     * @param args Arguments to fill in URL template placeholders
     * @return XML content returned by the REST service
     */
    public String getForObject(String url, String[] args);

    /**
     * Calls a REST service that posts an XML object representation, and returns
     * a location header containing the new object's URL.  Calls RestTemplate method
     * of the same name
     * @param url the URL of the REST service.
     * @param request the XML representation of the object being posted
     * @param args arguments to be used to fill in any URL placeholders
     * @return the URL at which the POSTed object can be found
     */
    public String postForLocation(String url, String request, String[] args);

    /**
     * Calls a REST service that POSTSs an XML object representation and receives an
     * XML object representation in the response
     * @param url the URL of the REST service
     * @param request the XML object representation being posted
     * @param args arguments to be used to fill in any URL placeholders
     * @return the XML representation returned by the REST service
     */
    public String postForObject(String url, String request, String[] args);

    /**
     * Calls a REST service that DELETEs an object.  Expects no content in the response
     * @param url the URL of the resource to be DELETEd
     * @param args arguments to be used to fill in any URL placeholders
     */
    public void delete(String url, String[] args);
    
    /**
     * Calls a REST service that DELETEs an object, converting the URL to a URI object.
     * Expects no content in the response
     * @param url the encoded URL of the resource to be DELETEd
     */
    public void delete(String url);

    /**
     * Calls a REST service to POST to a URL.  Expects no content in the response
     * (204 No Content status in case of success)
     * @param url
     * @param args
     */
    void postForNoContent(String url, String[] args);

}
