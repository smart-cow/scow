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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.web;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author FITZPATRICK
 * @author PREMA
 */
@Controller
public class BpmServiceController {
    
    static Logger log = Logger.getLogger(BpmServiceController.class);
    
    Service service = null; 
    Map<String, Object> varsMap = new HashMap<String, Object>();
    String wsdl = "";
    String method = "";

    /**
     * Used to test the server and make sure it is running
     * @return 
     */
    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello";
    }
    
    @RequestMapping("/soap")
    @ResponseBody
    public String soapClient() {       
    //public String soapClient(@RequestParam("wsdl") String wsdl, @RequestParam("method") String method, @RequestParam("varsMap") Map<String, Object> varsMap) {       
        
        wsdl = "http://www.webservicex.net/whois.asmx?WSDL";
        method = "GetWhoIS";         
        varsMap.put("HostName", "mitre.org");
        try{
        return usePayload(wsdl, method, varsMap );        
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return "";
    }
    
    /** Create a service and add port to it. **/
    public void setUp() throws Exception {
       
      /** proxy setting, only if you need **/
        System.setProperty("http.proxyHost", "gatekeeper.mitre.org");
        System.setProperty("http.proxyPort", "80");
        System.setProperty("sun.net.client.defaultConnectTimeout", "" + 2000);
        System.setProperty("sun.net.client.defaultReadTimeout", "" + 2000);     
    } 
    
    /** Create a Dispatch instance using PAYLOAD mode **/
    public String usePayload(String wsdl, String method, Map<String, Object> varsMap){        
       
       String xmlResult = "";
       try{         
        
        /** proxy setting **/
        setUp();
        
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdl);        
        
        String targetNameSpace = d.getDocumentElement().getAttribute("targetNamespace");        
        
        NodeList elem = d.getElementsByTagName("wsdl:service");
        String serviceName = elem.item(0).getAttributes().getNamedItem("name").getNodeValue();
        
        QName serviceQName = new QName(targetNameSpace, serviceName);
        URL wsdlLocation = new URL(wsdl); 
        service = Service.create(wsdlLocation, serviceQName);        
        
        Iterator<QName> al = service.getPorts();
        String portName = al.next().getLocalPart();                
        QName qPortQName = new QName(targetNameSpace, portName);  
        
        String soapAction = targetNameSpace + "/" + method;        
        Dispatch<Source> sourceDispatch = service.createDispatch(qPortQName, Source.class, Service.Mode.PAYLOAD);
        // The soapActionUri is set here. otherwise we get a error on .net based services.
        sourceDispatch.getRequestContext().put(Dispatch.SOAPACTION_USE_PROPERTY, new Boolean(true));
        sourceDispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY, soapAction); 
        
        //build request string
        NodeList se = d.getElementsByTagName("s:schema");
        String tns = se.item(0).getAttributes().getNamedItem("targetNamespace").getNodeValue(); 
        String varlist = "";       
        for (Map.Entry<String, Object> entry : varsMap.entrySet()) {            
            varlist = varlist + "<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">";           
            }       
              
        String requestStr = "<"+method+ " xmlns=\"" + tns + "\">" + varlist + "</" + method + ">";        
        //"<GetWhoIS xmlns=\"http://www.webservicex.net\"><HostName>"+in_str+"</HostName></GetWhoIS>";        
        
        log.info("\nInvoking xml request:\n " + requestStr);
        Source result = sourceDispatch.invoke(new StreamSource(new StringReader(requestStr)));        
        xmlResult = sourceToXMLString(result);
        log.info("\nReceived xml response:\n " + xmlResult);  
       } catch (Exception e) {
           e.printStackTrace();
           xmlResult = "Error invoking webservice: " + e.getMessage();
       } 
       return xmlResult;
   }
    
    /** Convert PAYLOAD response Source to String **/
   private String sourceToXMLString(Source result) {
      String xmlResult = null;
      try {
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer();
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         OutputStream out = new ByteArrayOutputStream();
         StreamResult streamResult = new StreamResult();
         streamResult.setOutputStream(out);
         transformer.transform(result, streamResult);
         xmlResult = streamResult.getOutputStream().toString();
      } catch (TransformerException e) {
         e.printStackTrace();
      }
      return xmlResult;
   }
    
}
