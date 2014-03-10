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

package org.wiredwidgets.cow.webapp.server;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.webapp.client.AuthService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


public class AuthServiceImpl extends AutoinjectingRemoteServiceServlet implements AuthService {

    private static final long serialVersionUID = 3091448562758356129L;

    @Override
    public String retrieveUsername() {
        
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication==null){
            System.out.println("Not logged in");
            return null;
        }
        else {
            //return (String) authentication.getPrincipal();
        	return (String) authentication.getName();
        }
        
    }   
    

    
}
