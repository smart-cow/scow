package org.wiredwidgets.cow.server.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class SynchronizedFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This is a somewhat drastic workaround to the problem of thread safety in the StatefulKnowledgeSession.
	 * This workaround makes the entire app effectively single threaded and thus severely limits its
	 * scalability, so should be considered a temporary patch until a better solution can be found.
	 * 
	 * See https://issues.jboss.org/browse/JBPM-3814
	 */
	@Override
	public synchronized void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
