package it.polito.dp2.WF.sol2;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.File;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXParseException;

import it.polito.dp2.WF.*;
import it.polito.dp2.WF.sol2.jaxb.*;
public class Parser {
	private String xmlFileName, schemaFileName;
	private HashMap<String, MyWorkFlowReader> workFlows = new HashMap<String, MyWorkFlowReader>();
	private HashSet<MyProcessReader> allProcess = new HashSet<MyProcessReader>();
	
	public Parser(String xmlFileName, String schemaFileName, HashMap<String, MyWorkFlowReader> workFlows, HashSet<MyProcessReader> allProcess){
		this.xmlFileName = xmlFileName;
		this.schemaFileName = schemaFileName;
		this.workFlows = workFlows;
		this.allProcess = allProcess;
	}
	
	public void parse() throws WorkflowMonitorException{
	 Object rootObj;
		try {
			rootObj = unmarshal(xmlFileName, schemaFileName, "it.polito.dp2.WF.sol2.jaxb");
		} catch (Exception e) {
			throw new WorkflowMonitorException(e);
		}
			
		if (!(rootObj instanceof JAXBElement<?>))
			throw new WorkflowMonitorException("The root element is not of type JAXBElement<>");
			
		Object rootObjValue = ((JAXBElement<?>)rootObj).getValue();
			
		if (rootObjValue == null)
			throw new WorkflowMonitorException("The value of the root element is null");
			
		if (!(rootObjValue instanceof WorkFlowSystemType))
			throw new WorkflowMonitorException("The root element is not of type JAXBElement<WorkFlowSystemType>");
			
		WorkFlowSystemType workFlowSystemNode = (WorkFlowSystemType)rootObjValue;
			
		WorkFlowsType workFlowsNode = workFlowSystemNode.getWorkFlows();
		parseWorkFlowsNode(workFlowsNode);

	}

	private void parseWorkFlowsNode(WorkFlowsType workFlowsNode) throws WorkflowMonitorException {
		for(WorkFlowType workFlow : workFlowsNode.getWorkFlow()){
			parseWorkFlowNode(workFlow);			
		}
	}

	private void parseWorkFlowNode(WorkFlowType workFlowNode) throws WorkflowMonitorException{
		String flowName = workFlowNode.getFlowName();
		
		HashMap<String, MyActionReader> actions = new HashMap<String, MyActionReader>();
		HashSet<MyProcessReader> processes = new HashSet<MyProcessReader>();
		MyWorkFlowReader wfs = new MyWorkFlowReader(actions, flowName, processes);
		
		ActionsType actionsNode = workFlowNode.getActions();
		for(ActionType actionNode : actionsNode.getAction()){
			String actionName = actionNode.getActionName();
			if(!MyActionReader.isNameValid(actionName))
				throw new WorkflowMonitorException("action name is not correct");

			String actionRole =actionNode.getActionRole();
			if(!MyActionReader.isRoleValid(actionRole))
				throw new WorkflowMonitorException("action role is not correct");
			boolean automaticallyInstantiated = actionNode.isAutomaticallyInstantiated();
			MyActionReader action = new MyActionReader(wfs, actionName, actionRole, automaticallyInstantiated);
			if(actionNode.getSimpleAct()!=null){
				SimpleActType simpleActNode = actionNode.getSimpleAct();
					HashSet<MyActionReader> nextActions = new HashSet<MyActionReader>();
					if(simpleActNode.getNextAction()!=null){
						for(NextActionType nextActionNode:simpleActNode.getNextAction()){
								String nextPossibleAction = nextActionNode.getNextPossibleAction();
								nextActions.add(actions.get(nextPossibleAction));
								}
						}
					action = new MySimpleActionReader(wfs, actionName, actionRole, automaticallyInstantiated, nextActions);
						
						}else if(actionNode.getProcessAct()!=null) {
							ProcessActType processActNode = actionNode.getProcessAct();
							String relatedWorkFlow = processActNode.getRelatedWorkFlow();
							MyWorkFlowReader work = null;
							for(MyWorkFlowReader wf:workFlows.values()){
								if(relatedWorkFlow.equals(wf.getName())){
									work = wf;
								}
								}
								action = new MyProcessActionReader(work,wfs, actionName, actionRole, automaticallyInstantiated);
							}
					actions.put(actionName, action);
		}
		ProcessesType processesNode = workFlowNode.getProcesses();
		Calendar startTime = null;
		for(ProcessType processNode:processesNode.getProcess()){
			startTime = processNode.getStartTime().toGregorianCalendar();
			ArrayList<MyActionStatusReader> actionStatuses= new ArrayList<MyActionStatusReader>();
			ActionStatusesType actionStatusesNode = processNode.getActionStatuses();
			if(actionStatusesNode!=null){
			for(ActionStatusType actionStatusNode:actionStatusesNode.getActionStatus()){
				String actionStatName = actionStatusNode.getActionStatName();	
				boolean terminated = actionStatusNode.isTerminated();
				Calendar terminationTime = null;
				if(terminated)
					terminationTime = actionStatusNode.getTerminationTime().toGregorianCalendar();
				boolean takenInCharge = actionStatusNode.isTakenInCharge();
				Actor actor = null;
				if(takenInCharge){
					ActorType actorNode = actionStatusNode.getActor();
					if(actorNode!=null){
					String actorName = actorNode.getActorName();
					String actorRole = actorNode.getActorRole();
					actor = new Actor(actorName, actorRole);
					}
				}
				
				MyActionStatusReader actionStatus = new MyActionStatusReader(actionStatName, actor, terminationTime, takenInCharge, terminated);
				actionStatuses.add(actionStatus);
			}
		}	
			MyProcessReader process = new MyProcessReader(startTime,actionStatuses, wfs);
			processes.add(process);
			}	
		allProcess.addAll(processes);
		MyWorkFlowReader wf = new MyWorkFlowReader(actions, flowName, processes);
		workFlows.put(flowName, wf);
	}
		
			
			private static Calendar parseDate(String string) throws ParseException {
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
				Calendar cal  = Calendar.getInstance();
				dateFormat.setTimeZone(TimeZone.getTimeZone("CEST"));
				cal.setTime(dateFormat.parse(string));
				return cal;
			}
			
			private static String formatDate(Calendar calendar) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
				dateFormat.setTimeZone(calendar.getTimeZone());
				return dateFormat.format(calendar.getTime());
			}
			
			private static Object unmarshal(String xmlFileName, String xsdFileName, String contextPath) throws Exception {
				Unmarshaller u = null;
				try {
					JAXBContext jc = JAXBContext.newInstance(contextPath);
			        u = jc.createUnmarshaller();
				} catch (JAXBException e) {
					System.err.println("JAXBException");
					e.printStackTrace();
					throw e;
				}
		        
		        SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
		        
		        Schema schema = null;
		        try {
		            schema = sf.newSchema(new File(xsdFileName));
		        } catch (org.xml.sax.SAXException se) {
		            System.err.println("Unable to validate due to following error");
		            se.printStackTrace();
		            throw se;
		        }
		        
		        u.setSchema(schema);
		        
		        Object rootObj = null;
				try {
					rootObj = u.unmarshal( new File( xmlFileName ) );
				} catch (JAXBException e) {
					System.err.println("JAXBException");
					e.printStackTrace();
					throw e;
				}
				
				if (rootObj == null)
					throw new Exception("The root object is null");
				
				return rootObj;
			}
			
}


