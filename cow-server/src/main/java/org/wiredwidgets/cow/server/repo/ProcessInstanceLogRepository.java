/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.repo;

import java.util.Date;
import java.util.List;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author FITZPATRICK
 */
public interface ProcessInstanceLogRepository extends CrudRepository<ProcessInstanceLog, Long>{
    
    public List<ProcessInstanceLog> findByStatus(int status);
    
    public List<ProcessInstanceLog> findByProcessId(String processId);
	
    public List<ProcessInstanceLog> findByProcessIdAndStatus(String processId, int status);
    
    public List<ProcessInstanceLog> findByStatusAndEndAfter(int status, Date end);
    
    public List<ProcessInstanceLog> findByProcessIdAndStatusAndEndAfter(String processId, int status, Date end);
    
}
