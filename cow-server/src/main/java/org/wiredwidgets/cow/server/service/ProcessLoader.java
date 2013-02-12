package org.wiredwidgets.cow.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Loads all processes at system startup.
 * Note we cannot just use an init method in ProcessServiceImpl, as this would
 * bypass the transactional proxy.  Since we invoke the method here using
 * the interface, it will be properly transactional.
 * @author JKRANES
 *
 */
@Component
public class ProcessLoader implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	ProcessService service;
	
	boolean loaded=false;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		// only do this once
		if (!loaded) {
			service.loadAllProcesses();	
			loaded = true;
		}
	}

}
