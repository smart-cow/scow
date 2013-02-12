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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.transform.v2;

import org.wiredwidgets.cow.server.api.model.v2.Process;

/**
 * Type T represents the base object type for process nodes
 * @author JKRANES
 */
public interface ProcessContext<T extends Object> {

    /**
     * Generates id values by appending a sequence number to the specified key.
     * This provides the option of generating all ids using a single sequence scheme, or generating
     * separate sequences per node type (e.g. task1, task2, gateway1, gateway2, etc).
     * @param key
     * @return
     */
    public String generateId(String key);

    /**
     * Add a new node to the process
     * @param node
     */
    public void addNode(T node);

    Process getSource();

}
