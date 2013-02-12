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

package org.wiredwidgets.cow.webapp.server;

import java.io.IOException;

import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * Expose Spring services to GWT app
 * From http://pgt.de/2009/07/17/non-invasive-gwt-and-spring-integration-reloaded/
 */
public abstract class AutoinjectingRemoteServiceServlet extends RemoteServiceServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        AutowireCapableBeanFactory beanFactory = ctx.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
        if (this.getBeanName() != null) {
            beanFactory.applyBeanPropertyValues(this, this.getBeanName());
        }
    }

    /*
     * Override to return the configured Spring bean name if property values need to be set.
     */
    protected String getBeanName() {
        return null;
    }
    
    @Override
    protected String readContent(HttpServletRequest request)
        throws ServletException, IOException {
      return RPCServletUtils.readContentAsUtf8(request, false);
    }
}
