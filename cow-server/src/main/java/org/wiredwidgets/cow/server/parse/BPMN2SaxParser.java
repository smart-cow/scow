/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.parse;

import org.wiredwidgets.cow.server.api.service.Deployment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author FITZPATRICK
 */
public class BPMN2SaxParser extends DefaultHandler{
    private Deployment deploy;
    
    public BPMN2SaxParser(){
        deploy = new Deployment();
    }
    
    
    public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException{
        if (qName.equalsIgnoreCase("process")){
            deploy.setId(attributes.getValue("id"));
            deploy.setName(attributes.getValue("name"));
            deploy.setState("active");
        }
    }
    
    public Deployment getDeployment(){
        return deploy;
    }

}
