package org.wiredwidgets.cow.server.service;

import org.apache.log4j.Logger;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.listener.JbpmTaskEventListener;
import org.wiredwidgets.cow.server.manager.TaskServiceSessionManager;

/**
 * Loads all processes at system startup. Note we cannot just use an init method
 * in ProcessServiceImpl, as this would bypass the transactional proxy. Since we
 * invoke the method here using the interface, it will be properly
 * transactional.
 * 
 * @author JKRANES
 * 
 */
@Component
public class AppStartup implements ApplicationListener<ApplicationContextEvent> {

	private static Logger log = Logger.getLogger(AppStartup.class);

	@Autowired
	ProcessService service;

	@Autowired
	TaskServiceSessionManager tssm;

	@Autowired
	org.jbpm.task.service.TaskService taskService;

	@Autowired
	JbpmTaskEventListener taskListener;
	
	@Autowired
	LocalHTWorkItemHandler htHandler;

	boolean loaded = false;

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {

		if (event instanceof ContextRefreshedEvent) {
			// only do this once
			if (!loaded) {
				
				// register the task event listener
				log.info("Loading task event listener: " + taskListener.getClass().getSimpleName());
				taskService.addEventListener(taskListener);
				
				// unclear whether this does anything...
				// htHandler.connect();

				try {
					service.loadAllProcesses();
					tssm.initLdap();
				}
				catch (Exception e) {
					log.error(e);
				}
				finally {
					// only try once 
					loaded = true;
				}
			}
		} else if (event instanceof ContextStartedEvent) {
//			try {
//				PropertyConfigurator.configure(new ClassPathResource(
//						"log4j.properties").getInputStream());
//			} catch (IOException e) {
//				log.error(e);
//			}
		}
	}

}
