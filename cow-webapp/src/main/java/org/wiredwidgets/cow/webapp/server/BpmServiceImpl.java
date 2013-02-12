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

package org.wiredwidgets.cow.webapp.server;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.webapp.client.BpmService;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 *
 * @author JKRANES
 */
public class BpmServiceImpl extends AutoinjectingRemoteServiceServlet implements BpmService {

    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(BpmServiceImpl.class);
    private String baseURL;
    @Autowired
    RestTemplate restTemplate;

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBaseURL() {
        return this.baseURL;
    }

    protected String getBeanName() {
        return "bpmService";
    }

    public String getProcess(String key) {
        return restTemplate.getForObject(baseURL + "/processes/{key}?format=sc2", String.class, key);
    }

    public String getNativeProcess(String key) {
        return restTemplate.getForObject(baseURL + "/processes/{key}?format=native", String.class, key);
    }

    public String createNativeDeployment(String processXml) {
        return restTemplate.postForObject(baseURL + "/deployments/native", processXml, String.class);
    }

    public String createDeployment(String processXml) {
        StreamSource source = new StreamSource();
        source.setReader(new StringReader(processXml));
        return restTemplate.postForObject(baseURL + "/deployments/sc2?name=test", source, String.class);
    }

    public String getForObject(String url, String[] args ) {
        return restTemplate.getForObject(baseURL + url, String.class, (Object[])args);
    }

    public String postForObject(String url, String request, String[] args) {
        // Casting the request object as as Source causes Spring to use the
        // SourceHttpMessageConverter, which will cause the request body
        // to be marked as application/xml, as required by the REST service.
        StreamSource source = new StreamSource(new StringReader(request));
        return restTemplate.postForObject(baseURL + url, source, String.class, (Object[])args);
    }

    public String postForLocation(String url, String request, String[] args) {
        // see comment above regarding Source
        /*StreamSource source = new StreamSource(new StringReader(request));
        return restTemplate.postForLocation(url, source, (Object[])args).toString();*/
    	Object source = null;
        if (request != null && !request.equals("")) {
        	source = new StreamSource(new StringReader(request));
        }
        return restTemplate.postForLocation(baseURL + url, source, (Object[]) args).toString();

    }

    public void delete(String url, String[] args) {
        restTemplate.delete(baseURL + url, (Object[])args);
    }
    
    public void delete(String url) {
    	URI uri;
		try {
			uri = new URI(baseURL + url);
			restTemplate.delete(uri);
		} catch (URISyntaxException e) {
			//logger.error("Error parsing DELETE URL", e);
		}
    }

    public void postForNoContent(String url, String[] args) {
        restTemplate.execute(baseURL + url, HttpMethod.POST, null, null, (Object[])args);
    }
}
