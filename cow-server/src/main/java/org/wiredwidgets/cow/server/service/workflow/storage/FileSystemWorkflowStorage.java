package org.wiredwidgets.cow.server.service.workflow.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;

public class FileSystemWorkflowStorage implements IWorkflowStorage {

    @Value("${workflow.fs.dir}")
    String WORKFLOW_DIR;
    
    @Autowired
    protected Jaxb2Marshaller marshaller;
	
	
	@Override
	public Process get(String key) {
		try {
			InputStream is = getFileAsInputStream(key);
			StreamSource source = new StreamSource(is);
			Process process = (Process) marshaller.unmarshal(source);
			return process;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	@Override
	public List<ProcessDefinition> getAll() {
		List<ProcessDefinition> procDefs = new ArrayList<ProcessDefinition>();
		
		File wFlowDir = new File(WORKFLOW_DIR);
		for (String filePath : wFlowDir.list()) {
			int lastSlash = filePath.lastIndexOf('/');
			int lastBackslash = filePath.lastIndexOf("\\");
			int fNameStart = lastSlash > lastBackslash ? lastSlash : lastBackslash;
			
			int lastDot = filePath.lastIndexOf(".xml");
			if (lastDot == -1) {
				lastDot = filePath.length();
			}
			String workflowKey = filePath.substring(fNameStart + 1, lastDot);
			
			ProcessDefinition pd = new ProcessDefinition();
			pd.setName(workflowKey);
			pd.setKey(workflowKey);
			procDefs.add(pd);
		}
		
		return procDefs;
	}

	
	@Override
	public URI save(Process process) {
		File file = getFile(process.getKey());
		StreamResult result = new StreamResult(file);
		marshaller.marshal(process, result);
		return file.toURI();
	}

	
	@Override
	public boolean delete(String key) {
		File f = getFile(key);	
		return f.delete();
	}

	private File getFile(String key) {
		File f = new File(WORKFLOW_DIR + '/' + key + ".xml");
		return f;
	}
	
	/**
	 * Easiest way to convert file to UTF-16 for marshaller
	 * @param key
	 * @return
	 */
	private InputStream getFileAsInputStream(String key) throws IOException{
		BufferedReader reader = null;
		try {
			StringBuilder sb = new StringBuilder();
			reader = new BufferedReader(new FileReader(getFile(key)));
			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
			    sb.append(line);
			}
			return new ByteArrayInputStream(sb.toString().getBytes());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
		
	
}
