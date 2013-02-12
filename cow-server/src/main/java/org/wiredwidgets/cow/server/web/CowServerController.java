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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.log4j.Logger;

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
    
}
