/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.Deployment;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.api.service.ResourceNames;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20NewProcessBuilder;
import org.wiredwidgets.cow.server.transform.graph.builder.GraphBuilder;
import org.wiredwidgets.rem2.schema.Node;
import org.wiredwidgets.rem2.schema.Property;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
@Component
public class ProcessServiceImpl extends AbstractCowServiceImpl implements ProcessService {

    private static Logger log = Logger.getLogger(ProcessServiceImpl.class);
    
    @Autowired
    Bpmn20NewProcessBuilder bpmn20ProcessBuilder;
    
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    ProcessDefinitionsService processDefsService;
    
    @Autowired
    GraphBuilder graphBuilder;
    
    @Value("${rem2.url}")
    String REM2_URL;
    
    public String getRem2WorkflowLocation(){
        return this.REM2_URL +"/cms/workflows";        
    }
    
    @Override
    public Deployment createDeployment(Definitions definitions) {
    	loadWorkflow(definitions);
    	Deployment d = new Deployment();      
    	d.setId(definitions.getRootElements().get(0).getValue().getId());
    	d.setName(definitions.getName());
    	d.setState("active");
    	return d;
    }    
    
    @Override
    public void deleteDeployment(String id) {
        kBase.removeProcess(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Deployment> findAllDeployments() throws Exception {
        Collection<org.drools.definition.process.Process> procs = kBase.getProcesses();
        List<Deployment> deploys = new ArrayList<Deployment>();
        for (org.drools.definition.process.Process p : procs) {
            Deployment d = new Deployment();
            d.setId(p.getId());
            d.setName(p.getName());
            deploys.add(d);
        }
        return deploys;
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceNames getResourceNames(String deploymentId) {
        return new ResourceNames();//throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getNativeProcessAsStream(String key) {
        InputStream in = null;
        return in;//throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public Deployment getDeployment(String id) {
        org.drools.definition.process.Process p = kBase.getProcess(id);
        Deployment d = new Deployment();
        d.setId(p.getId());
        d.setName(p.getName());
        return d;
    }

	@Override
    public Deployment saveV2Process(Process v2Process) {
       Definitions d = bpmn20ProcessBuilder.build(v2Process);
       log.debug("built bpmn20 process");
       saveInRem2(v2Process);
       return createDeployment(d);
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getResourceAsStream(String key, String extension) {
    	return getProcessFromRem2(key).getInputStream();
    }

    @Transactional(readOnly = true)
    @Override
    public Process getV2Process(String key) {
    	return (Process) marshaller.unmarshal(getProcessFromRem2(key));
    }
    
    @Override
    public Map<String, Object> getProcessGraph(String key) {
    	return getProcessGraph(getV2Process(key));
    }
    
    @Override
	public Map<String, Object> getProcessGraph(Process process) {
    	ActivityGraph graph = graphBuilder.buildGraph(process);
    	
    	// put into a List so we have a defined order
    	List<Activity> activities = new ArrayList<Activity>();
    	activities.addAll(graph.vertexSet());
    	
    	List<Map<String, Object>> edgeList = new ArrayList<Map<String, Object>>();
    	int i = 0;
    	for (ActivityEdge activityEdge : graph.edgeSet()) {
    		Map<String, Object> edge = new HashMap<String, Object>();
    		edge.put("source", activities.indexOf(graph.getEdgeSource(activityEdge)));
    		edge.put("target", activities.indexOf(graph.getEdgeTarget(activityEdge)));
    		edge.put("left", false);
    		edge.put("right", true);
    		edgeList.add(edge);
    		i++;
    	}
    	
    	// create list of Nodes
    	List<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
    	for (Activity activity : activities) {
    		Map<String, Object> node = new HashMap<String, Object>();
    		node.put("type", activity.getClass().getSimpleName());
    		node.put("details", activity);
    		nodeList.add(node);
    	}
    	
    	
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	resultMap.put("Nodes", nodeList);
    	resultMap.put("Edges", edgeList);
    	
    	return resultMap;
    	
    }
    
    @Override
	public Definitions getBpmn20Process(String key) {
    	return bpmn20ProcessBuilder.build(getV2Process(key));
    }
    
    @Override
	public void loadAllProcesses() {
    	List<ProcessDefinition> defs = processDefsService.findLatestVersionProcessDefinitions();
    	for (ProcessDefinition def : defs) {
    		try {
	    		Definitions d = getBpmn20Process(def.getKey());
	    		loadWorkflow(d);		
    		}
    		catch (Exception e) {
    			log.error(e);
    			e.printStackTrace();
    		}
    	}
    }
    
    private void addProperty(Node node, String name, String value) {
        Property p = new Property();
        p.setName(name);
        p.setValue(value);
        node.getProperties().add(p);
    }    
    
    @Override
	public void saveInRem2(org.wiredwidgets.cow.server.api.model.v2.Process process) {
        Node node = new Node();
        node.setType("rem:marketplace");
        node.setName(process.getName());
        
        addProperty(node, "rem:name", process.getName());
        addProperty(node, "rem:description", process.getName());
        addProperty(node, "rem:type", "workflow");
        
        Node content = new Node();
        content.setName("jcr:content");
        content.setType("nt:resource");
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(process, new StreamResult(sw));
        
        addProperty(content, "jcr:data", sw.toString()); 
        addProperty(content, "jcr:mimeType", "application/xml");
        node.getNodes().add(content);
             
        log.debug("calling rem2");
        RestTemplate restTemplate = new RestTemplate();
        //URI location = restTemplate.postForLocation("http://scout.mitre.org:8080/rem2/cms/workflows", node);
        
        log.info("\nrem2.url = " + this.getRem2WorkflowLocation());
        URI location = restTemplate.postForLocation(this.getRem2WorkflowLocation(), node);
    } 
    
    private InputStream marshalToInputStream(Object source) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
        	marshaller.marshal(source, new StreamResult(out));  
        }
        catch (Exception e) {
        	log.error("Marshalling exception for object " + source.getClass().getName());
        	log.error(e);
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
        byte[] bytes = out.toByteArray();
        String test = new String(bytes);
        return new ByteArrayInputStream(bytes);
    }
    
    private StreamSource getProcessFromRem2(String processName) {
    	String url = REM2_URL + "/cms/workflows/" + processName;
    	return restTemplate.getForObject(url, StreamSource.class);
    }
    
    @Override
	public void loadWorkflow(Definitions defs) {
    	try {
    		log.info("Loading process into knowledge base: " + defs.getName());
	        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
	        InputStream stream = marshalToInputStream(defs);
	        Resource resource = ResourceFactory.newInputStreamResource(stream);
	        kBuilder.add(resource, ResourceType.BPMN2);
	        if (kBuilder.hasErrors()) {
	        	log.error("Errors found in process " + defs.getName());
	        	for (KnowledgeBuilderError error : kBuilder.getErrors()) {
	        		String lines = "";
	        		for (int line : error.getLines()) {
	        			lines += (line + " ");
	        		}
	        		log.error("Lines: " + lines.trim());
	        		log.error("Message: " + error.getMessage());
	        	}
	        }
	        else {
	        	kBase.addKnowledgePackages(kBuilder.getKnowledgePackages()); 
		        // verify, just in case.
	        	// this may be unnecessary if we assume lack of errors always means successful load
		        org.drools.definition.process.Process process = kBase.getProcess(defs.getName());
		        if (process == null) {
		        	log.error("Process failed to load: " + defs.getName());
		        }
	        }
	        

    	}
    	catch  (Exception e) {
    		log.error("Error loading process: " + defs.getName());
    		log.error(e);
    	}
    }
   
}
