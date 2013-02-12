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

package org.wiredwidgets.cow.server.service;

import java.util.List;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;

/**
 *
 * @author JKRANES
 */
public interface ProcessDefinitionsService {

    List<ProcessDefinition> findAllProcessDefinitions();

    ProcessDefinition getProcessDefinition(String id);


    /**
     * finds the latest version of each process definition
     *
     * @return
     */
    List<ProcessDefinition> findLatestVersionProcessDefinitions();

    List<ProcessDefinition> findProcessDefinitionsByKey(String key);

    ProcessDefinition findLatestVersionProcessDefinitionByKey(String key);

    /**
     * Delete all deployments for this process def key
     * @param key
     * @return true if at least one found, false if none found
     */
    boolean deleteProcessDefinitionsByKey(String key);

}
