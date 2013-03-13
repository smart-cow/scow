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

package org.wiredwidgets.cow.server.convert;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;

/**
 * Exposes the conversion service to converters.
 * @author JKRANES
 */
public abstract class AbstractConverter<S, T> implements Converter<S, T>, InitializingBean {

	@Autowired
    private GenericConversionService converter;
	
    @Override
    public void afterPropertiesSet() {
    	converter.addConverter(this);
    }
    
    protected <U> U convert(Object source, Class<U> targetType) {
    	return source==null ? null : converter.convert(source, targetType);
    }	
    
    protected XMLGregorianCalendar convert(Date date) {
        return convert(date, XMLGregorianCalendar.class);
    }
    
    protected Date convert(XMLGregorianCalendar gc) {
    	return convert(gc, Date.class);
    }


}
