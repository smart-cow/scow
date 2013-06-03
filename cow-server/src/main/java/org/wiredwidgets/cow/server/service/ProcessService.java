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

import java.io.InputStream;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.omg.spec.bpmn._20100524.model.Definitions;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.Deployment;
import org.wiredwidgets.cow.server.api.service.ResourceNames;

/**
 *
 * @author JKRANES
 */
public interface ProcessService {

    public static final String BPMN2_EXTENSION = ".bpmn2";
    public static final String JPDL_EXTENSION = ".jpdl.xml";
    public static final String V2_EXTENSION = ".v2.xml";

    /**
     * Creates a new deployment from a StreamSource.  The StreamSource is presumed
     * to contain a BPMN format process definition xml file.
     * @param source
     * @return
     */
    public Deployment createDeployment(Definitions definitions);

    void deleteDeployment(String id);

    List<Deployment> findAllDeployments() throws Exception;

    ResourceNames getResourceNames(String deploymentId);

    InputStream getNativeProcessAsStream(String key);

    Deployment getDeployment(String id);

    Deployment saveV2Process(Process v2Process);

    InputStream getResourceAsStream(String key, String extension);

    org.wiredwidgets.cow.server.api.model.v2.Process getV2Process(String key);

	public abstract void loadAllProcesses();

	public abstract Definitions getBpmn20Process(String key);
    
}
