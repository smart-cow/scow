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

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    private static Map<String, String> users = new HashMap<String, String>();
    
    static {
        users.put("mhowansky", "matt");
        users.put("matt", "matt");
    }

    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        
        String username = (String) authentication.getPrincipal();
        String password = (String)authentication.getCredentials();
        
        if (users.get(username)==null)
            throw new UsernameNotFoundException("User not found");
        
        String storedPass = users.get(username);
        
        if (!storedPass.equals(password))
            throw new BadCredentialsException("Invalid password");
        
        Authentication customAuthentication = 
            new CustomUserAuthentication("ROLE_USER", authentication);
        customAuthentication.setAuthenticated(true);
        
        return customAuthentication;
        
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}