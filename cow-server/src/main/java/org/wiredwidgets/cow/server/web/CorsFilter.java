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


/*
Sample CORS request:

$.ajax({
    url: "http://scout2.mitre.org:8080/cow-server/tasks",
    dataType: "json",
    xhrFields: {
        withCredentials: true
    }
}).done(function(data) {
    console.log(data);
});
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
		httpResp.setHeader("Access-Control-Max-Age", "120");
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
