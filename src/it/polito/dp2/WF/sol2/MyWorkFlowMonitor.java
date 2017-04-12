package it.polito.dp2.WF.sol2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import it.polito.dp2.WF.ProcessReader;
import it.polito.dp2.WF.WorkflowMonitor;
import it.polito.dp2.WF.WorkflowMonitorException;
import it.polito.dp2.WF.WorkflowReader;

public class MyWorkFlowMonitor implements WorkflowMonitor {
	
	private HashMap<String, MyWorkFlowReader> workFlows = new HashMap<String, MyWorkFlowReader>();
	private HashSet<MyProcessReader> allProcess = new HashSet<MyProcessReader>();
	private static final String SCHEMA_FILE = "xsd" + File.separatorChar + "WFInfo.xsd";
	
	public MyWorkFlowMonitor() throws WorkflowMonitorException {
		 String fileName = System.getProperty("it.polito.dp2.WF.sol2.WorkflowInfo.file");
		 Parser  parser = new Parser(fileName, SCHEMA_FILE, workFlows, allProcess);
		 parser.parse();
	 }

	@Override
	public Set<ProcessReader> getProcesses() {
		return new HashSet<ProcessReader>(allProcess);
	}

	@Override
	public WorkflowReader getWorkflow(String name) {
		if(isNameValid(name)){
			return workFlows.get(name);
		}
		else
			return null;
	}

	@Override
	public Set<WorkflowReader> getWorkflows() {
		return new HashSet<WorkflowReader>(workFlows.values());
	}
	
	public static boolean isNameValid(String name) {
		String Regx = "[A-Za-z][A-Za-z0-9]*";
		return (name==null || name.matches(Regx));
	}
	private static String formatDate(Calendar calendar) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
		dateFormat.setTimeZone(calendar.getTimeZone());
		return dateFormat.format(calendar.getTime());
	}
}
