/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wiredwidgets.cow.server.manager.MinaTaskServerManager;

/**
 *
 * @author FITZPATRICK
 */
@Controller
public class BpmServiceController {
    
    static Logger log = Logger.getLogger(BpmServiceController.class);

    /**
     * Used to test the server and make sure it is running
     * @return 
     */
    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello";
    }
}
