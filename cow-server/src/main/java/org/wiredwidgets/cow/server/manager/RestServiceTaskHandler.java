package org.wiredwidgets.cow.server.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
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

	@Autowired
	ConversionService converter;



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

		String contentPattern = (String)item.getParameter("content");
		String var = (String)item.getParameter("var");
		String urlPattern = (String)item.getParameter("url");
		String method = (String)item.getParameter("method"); 

		String result = "";
		String url = evaluateExpression(urlPattern, item);
		
		if (method.equalsIgnoreCase(HttpMethod.GET.name())) {                     
			result = restTemplate.getForObject(url, String.class); 
			log.info("GET result: " + result);
		} 
		else if (method.equalsIgnoreCase(HttpMethod.POST.name())) {            
			try {
				// this method expects XML content in the response.  if none if found an exception is thrown
				String content = evaluateExpression(contentPattern, item);
				result = restTemplate.postForObject(url, content, String.class);
				log.info("POST result: " + result);
			}
			catch (RestClientException e) {
				log.error(e);
			}
		}            

		Map<String, Object> outputMap = new HashMap<String, Object>();
		// update the result variable, if specified
		
		if (var != null && !var.trim().equals("")){  
			Map<String, Object> varsMap = new HashMap<String, Object>();
			varsMap.put(var, result);
			outputMap.put("Variables", varsMap);
		}   
	
		manager.completeWorkItem(item.getId(), outputMap);		
	}


	@SuppressWarnings("unchecked")
	private String evaluateExpression(String expression, WorkItem wi) {
		//private String evaluateExpression(String expression) {
		// change ${xxx} to #{#xxx} 
		expression = expression.replaceAll("\\$\\{(.*)\\}", "#\\{#$1\\}");

		StandardEvaluationContext context = new StandardEvaluationContext();
		ExpressionParser parser = new SpelExpressionParser();    

		//context.setVariables((executionService.getVariables(execution.getId(), executionService.getVariableNames(execution.getId()))));
		Object variables = wi.getParameter("Variables");
		if (variables instanceof Map) {
			context.setVariables((Map<String, Object>) variables);
		}
		String exprResult = parser.parseExpression(expression, new TemplateParserContext())
				.getValue(context, String.class);
		log.info("ParsedExpr: " + exprResult);
		return exprResult;
	}


}
