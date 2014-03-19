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

package org.wiredwidgets.cow.server.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;




/**
 * Adds CORS headers to HTTP Responses. 
 * 
 * Example Javascript CORS request:
 * <pre>
 * {@code
 * $.ajax({
 *   url: "http://scout2.mitre.org:8080/cow-server/tasks",
 *   dataType: "json", //Could also be xml
 *   xhrFields: {
 *   	 // Makes the dialog box asking for credentials to pop up before the first CORS request.
 *       withCredentials: true 
 *   }
 * }).done(function(data) {
 *   console.log(data);
 * });
 * }
 * </pre>
 * @author brosenberg
 *
 */
@Component
public class CorsFilter implements Filter {
	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
				throws IOException, ServletException {
		
		HttpServletRequest httpReq = (HttpServletRequest) request;
		String origin = httpReq.getHeader("Origin");
			
		HttpServletResponse httpResp = (HttpServletResponse) response;
		httpResp.setHeader("Access-Control-Allow-Origin", origin);
		httpResp.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
		httpResp.setHeader("Access-Control-Max-Age", "0");
		httpResp.setHeader("Access-Control-Allow-Headers", "x-requested-with");
		httpResp.setHeader("Access-Control-Allow-Credentials", "true");			
		

		chain.doFilter(request, response);
		
	}
	
	
	@Override
	public void destroy() {
		
	}



	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
