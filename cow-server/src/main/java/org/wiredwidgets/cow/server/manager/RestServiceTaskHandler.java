package org.wiredwidgets.cow.server.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class RestServiceTaskHandler implements WorkItemHandler {
	
	private static Logger log = Logger.getLogger(RestServiceTaskHandler.class);
        @Autowired
        RestTemplate restTemplate; 
        @Autowired
        StatefulKnowledgeSession kSession;        
        
                
	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {	
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
		log.info("Work item: " + item.getName());
		
		for (Entry<String, Object> entry : item.getParameters().entrySet()) {
			log.info(entry.getKey() + ":" + entry.getValue());
		}
		
                String content = (String)item.getParameter("content");
                String var = (String)item.getParameter("var");
                String url = (String)item.getParameter("url");
                String method = (String)item.getParameter("method"); 
                
                //RestTemplate restTemplate = new RestTemplate();
                String result = null;
                if (method.equalsIgnoreCase(HttpMethod.GET.name())) {                     
                    result = restTemplate.getForObject(evaluateExpression(url,item), String.class); 
                    log.info("GET result: " + result);
                } else if (method.equalsIgnoreCase(HttpMethod.POST.name())) {            
                    try {
                        // this method expects XML content in the response.  if none if found an exception is thrown
                        result = restTemplate.postForObject(evaluateExpression(url,item), evaluateExpression(content,item), String.class);
                        log.info("POST result: " + result);
                    }
                    catch (RestClientException e) {
                        // in this case, just log the error and move on.  The result will be null.
                        log.error(e);
                    }
                }            
                
                 
                // update the result variable, if specified
                Map<String, Object> varsMap = new HashMap<String, Object>();
                if (var != null && !var.trim().equals("") && result != null && !result.trim().equals("")){   
                  varsMap.put(var, result);
                  log.info("varsMap: " + varsMap);    
                }

                
		manager.completeWorkItem(item.getId(), varsMap);		
		
	}
       
            
        private String evaluateExpression(String expression, WorkItem wi) {
        //private String evaluateExpression(String expression) {
        // change ${xxx} to #{#xxx} 
        expression = expression.replaceAll("\\$\\{(.*)\\}", "#\\{#$1\\}");

        StandardEvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();    
             
        //context.setVariables((executionService.getVariables(execution.getId(), executionService.getVariableNames(execution.getId()))));
        context.setVariables(wi.getParameters());
        log.info(parser.parseExpression(expression, new TemplateParserContext()).getValue(context, String.class));
        return parser.parseExpression(expression, new TemplateParserContext()).getValue(context, String.class);
    }

}
