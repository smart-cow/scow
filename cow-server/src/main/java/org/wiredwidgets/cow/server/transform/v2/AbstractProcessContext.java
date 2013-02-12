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
 * Abstract base class for ProcessContext subclasses.
 * Each BPMN notation system, e.g. BPMN20 and JPDL, will have its own ProcessContext implementation
 */

package org.wiredwidgets.cow.server.transform.v2;

import java.util.HashMap;
import java.util.Map;

import org.wiredwidgets.cow.server.api.model.v2.Process;

/**
 * T = base class of all nodes for this notation system
 * T2 = class for the target process being created
 * @author JKRANES
 */
public abstract class AbstractProcessContext<T extends Object, T2 extends Object> implements ProcessContext<T> {

    private Map<String, Integer> nodeCounter = new HashMap<String, Integer>();
    private Process source;
    private T2 target;

    protected AbstractProcessContext(Process source, T2 target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Returns the COW Process that is being rendered into a specific BPMN system
     * @return 
     */
    @Override
    public Process getSource() {
        return source;
    }

    /**
     * Returns the root of the object graph that is being constructed
     * @return 
     */
    public T2 getTarget() {
        return target;
    }
    
    /**
     * Generates an ID using a simple incrementing system.
     * @param key the root string for the incrementing system.  Allows for multiple incrementing values, 
     * for example task1, task2, task2 as well as fork1, fork2 and fork3.  To create a single incrementing system, use ""
     * as the key, this will return a simple sequence of 1, 2, 3, etc.
     * Note: this is not threadsafe -- we assume the graph is being built by a single thread.
     * @return 
     */
    @Override
    public String generateId(String key) {
        Integer count = nodeCounter.get(key);
        if (count == null) {
            count = new Integer(0);
            nodeCounter.put(key, count);
        }
        count = new Integer(count.intValue() + 1);
        nodeCounter.put(key, count);
        return (key + count.toString());
    }

}
