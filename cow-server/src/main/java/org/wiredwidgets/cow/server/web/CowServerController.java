/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Abstract base class with utility methods for use by controllers
 * @author JKRANES
 */
public abstract class CowServerController {
    
    private static Logger log = Logger.getLogger(CowServerController.class);
    
    /*
     * Note: process keys passed as path variables must be doubly URL encoded, because Spring
     * applies decoding before it does path matching. 
     */
    protected String decode(String value) {
        String result = value;
        try {
            result = URLDecoder.decode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            log.error("Value: " + value);
            // do nothing -- value is unchanged
        }
        return result;
    }    
    
    
    /**
     * Construct a 201 Created response with location header
     * @param uriPattern for example /tasks/{id}
     * @param id the value to substitute in the URI pattern
     * @param uriBuilder If a UriComponentsBuilder is specified in the controller method's
     * 					 parameters, Spring will automatically pass it in
     * @param body The object to be included in the HTTP response's body
     * @return 
     */
    protected static <T> ResponseEntity<T> getCreatedResponse(
    		String uriPattern, Object id, UriComponentsBuilder uriBuilder, T body) {

    	HttpHeaders headers = getHeadersWithLocation(uriPattern, id, uriBuilder); 	
    	return new ResponseEntity<T>(body, headers, HttpStatus.CREATED);
    }
    
    
    /**
     * Construct an HttpHeaders object with the location header set
     * @param uriPattern for example /tasks/{id}
     * @param id the value to substitute in the URI pattern
     * @param uriBuilder If a UriComponentsBuilder is specified in the controller method's
     * 					 parameters, Spring will automatically pass it in
     * @return
     */
    protected static HttpHeaders getHeadersWithLocation(String uriPattern, Object id, 
    		UriComponentsBuilder uriBuilder) {
    	URI uri = uriBuilder
    				.path(uriPattern)
    				.buildAndExpand(id)
    				.toUri();
    	HttpHeaders headers = new HttpHeaders();
    	headers.setLocation(uri);
    	return headers;
    }
    
    
    /**
     * Construct a default response to an HTTP GET request. If body is not null
     * returns 200, else 404
     * @param body The object to be included in the HTTP response's body
     * @return 
     */
    protected static <T> ResponseEntity<T> createGetResponse(T body) {
    	if (body == null) {
    		return notFound();
    	}
    	return ok(body);
    }
    
    
    /**
     * Construct a 200 ok response
     * @param body
     * @return
     */
    protected static <T> ResponseEntity<T> ok(T body) {
    	return new ResponseEntity<T>(body, HttpStatus.OK);
    }
    
    
    /**
     * Construct a 404 not found response
     * @return
     */
    protected static <T> ResponseEntity<T> notFound() {
    	return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
    }
    
    
    /**
     * Construct a 201 no content response
     * @return
     */
    protected static <T> ResponseEntity<T> noContent() {
    	return new ResponseEntity<T>(HttpStatus.NO_CONTENT);
    }
    
    
    /**
     * Construct a 409 conflict response
     * @param body
     * @return
     */
    protected static <T> ResponseEntity<T> conflict(T body) {
    	return new ResponseEntity<T>(body, HttpStatus.CONFLICT);
    }
    
    
    /**
     * Construct a 501 not implemented response
     * @return
     */
    protected static <T> ResponseEntity<T> notImplemented() {
    	return new ResponseEntity<T>(HttpStatus.NOT_IMPLEMENTED);
    }
    
    
    /**
     * Construct a 403 forbidden response
     * @return
     */
    protected static <T> ResponseEntity<T> forbidden() {
    	return new ResponseEntity<T>(HttpStatus.FORBIDDEN);
    }
    
    
    /**
     * Construct a 500 internal server response
     * @return
     */
    protected static <T> ResponseEntity<T> internalError() {
    	return new ResponseEntity<T>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    

    protected static Long convertProcessInstanceKeyToId(String processInstanceKey) {
        int dotPos = processInstanceKey.indexOf('.');
        processInstanceKey = processInstanceKey.substring(dotPos + 1);
        return Long.valueOf(processInstanceKey);
    }
    
    
    
}
