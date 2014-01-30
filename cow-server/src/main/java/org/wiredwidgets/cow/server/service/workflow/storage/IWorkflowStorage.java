package org.wiredwidgets.cow.server.service.workflow.storage;

import java.net.URI;
import java.util.List;

import org.wiredwidgets.cow.server.api.service.ProcessDefinition;

public interface IWorkflowStorage {

	public org.wiredwidgets.cow.server.api.model.v2.Process get(String key);
	
	public List<ProcessDefinition> getAll();
	
	public URI save(org.wiredwidgets.cow.server.api.model.v2.Process process);
	
	public boolean delete(String key);
}
