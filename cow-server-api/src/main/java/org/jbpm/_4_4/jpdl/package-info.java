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
 * CUSTOM package-info to replace the generated default in order to eliminate the namespace prefix
 * for the JPDL XML output, in order to work around JBPM bug https://issues.jboss.org/browse/JBPM-3392
 * See http://hwellmann.blogspot.com/2011/03/jaxb-marshalling-with-custom-namespace.html
 * 
 */

@XmlSchema(namespace = "http://jbpm.org/4.4/jpdl", 
        xmlns = {@XmlNs(namespaceURI = "http://jbpm.org/4.4/jpdl", prefix = "")},
        elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)

package org.jbpm._4_4.jpdl;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;

