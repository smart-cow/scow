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

package org.wiredwidgets.cow.server.service.workflow.storage;

import java.net.URI;
import java.util.List;

import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.api.model.v2.Process;

public interface IWorkflowStorage {

	/**
	 * 
	 * @param key the workflow name
	 * @return process or null if not found
	 */
	public Process get(String key);
	
	
	public List<ProcessDefinition> getAll();
	
	
	/**
	 * 
	 * @param process
	 * @return Location of saved process
	 */
	public URI save(Process process);
	
	
	
	/**
	 * 
	 * @param key the workflow name
	 * @return true if deleted
	 */
	public boolean delete(String key);
}
