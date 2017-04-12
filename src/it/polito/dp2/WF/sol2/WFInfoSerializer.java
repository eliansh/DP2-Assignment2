package it.polito.dp2.WF.sol2;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import it.polito.dp2.WF.sol2.jaxb.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import it.polito.dp2.WF.*;

public class WFInfoSerializer{
	private static final String SCHEMA_FILE = "xsd" + File.separatorChar + "WFInfo.xsd";
	private static final String SCHEMA_LOCATION = "http://www.example.org/WFInfo WFInfo.xsd";
	private WorkflowMonitor monitor;
//	DateFormat dateFormat;

	public WFInfoSerializer() throws WorkflowMonitorException {
		it.polito.dp2.WF.WorkflowMonitorFactory factory = it.polito.dp2.WF.WorkflowMonitorFactory.newInstance();
		monitor = factory.newWorkflowMonitor();
		//dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm ");
	}
	
	private JAXBElement<WorkFlowSystemType> build() throws WorkflowMonitorException {
		
		WorkFlowSystemType workFlowSystemObj = new WorkFlowSystemType();	
		workFlowSystemObj.setWorkFlows(buildWorkFlows());	
		return (new ObjectFactory()).createWorkFlowSystem(workFlowSystemObj);
	}

	private WorkFlowsType buildWorkFlows() {
		WorkFlowsType workFlowsObj = new WorkFlowsType();
		Set<WorkflowReader> workFlows = monitor.getWorkflows();
		for (WorkflowReader workFlow : workFlows){
			WorkFlowType workFlowObj = new WorkFlowType();
			workFlowsObj.getWorkFlow().add(workFlowObj);
			workFlowObj.setFlowName(workFlow.getName());
			
			workFlowObj.setActions(buildActions(workFlow));
			workFlowObj.setProcesses(buildProcesses(workFlow));
		}
		
		return workFlowsObj;
	}
	
	private ActionsType buildActions(WorkflowReader workFlow){
		ActionsType actionsObj = new ActionsType();
		Set<ActionReader> actions = workFlow.getActions();
		for(ActionReader action:actions){
			ActionType actionObj = new ActionType();
			actionsObj.getAction().add(actionObj);
			actionObj.setActionName(action.getName());
			actionObj.setActionRole(action.getRole());
			actionObj.setAutomaticallyInstantiated(action.isAutomaticallyInstantiated());
			if (action instanceof SimpleActionReader){
				SimpleActType simpleActObj = new SimpleActType();
			actionObj.setSimpleAct(buildSimpleAct(simpleActObj, action));	
			}else if (action instanceof ProcessActionReader){
				ProcessActType processActObj = new ProcessActType();
				actionObj.setProcessAct(processActObj);
				processActObj.setRelatedWorkFlow(((ProcessActionReader) action).getActionWorkflow().getName());		
			}
		}		
		return actionsObj;
	}
	private SimpleActType buildSimpleAct(SimpleActType simpleActObj, ActionReader action){	
		Set<ActionReader> nextActions = ((SimpleActionReader)action).getPossibleNextActions();
		if (nextActions != null){
			for (ActionReader nAct: nextActions){	
				NextActionType nextActionObj = new NextActionType();
				simpleActObj.getNextAction().add(nextActionObj);
				nextActionObj.setNextPossibleAction(nAct.getName());
			}
			return simpleActObj;
		}
		return null;	
	}
	private ProcessesType buildProcesses(WorkflowReader workFlow) {
		ProcessesType processesObj = new ProcessesType();
		Set<ProcessReader> processes = workFlow.getProcesses();
		for(ProcessReader process : processes){
			ProcessType processObj = new ProcessType();
			processesObj.getProcess().add(processObj);
			processObj.setStartTime(convertDate(process.getStartTime()));
			processObj.setActionStatuses(buildActionStatus(processObj,process));
		}
		return processesObj;
	}

	private ActionStatusesType buildActionStatus(ProcessType processObj,ProcessReader process) {
		ActionStatusesType actionStatusesObj = new ActionStatusesType();
		List<ActionStatusReader> actionStatuses = process.getStatus();
		for(ActionStatusReader actionStatus : actionStatuses){
			ActionStatusType actionStatusObj = new ActionStatusType();
			actionStatusesObj.getActionStatus().add(actionStatusObj);
			actionStatusObj.setActionStatName(actionStatus.getActionName());
			actionStatusObj.setTerminated(actionStatus.isTerminated());	
			Calendar terminationTime = actionStatus.getTerminationTime();
			if(terminationTime!= null){
				actionStatusObj.setTerminationTime(convertDate(actionStatus.getTerminationTime()));
			}
			actionStatusObj.setTakenInCharge(actionStatus.isTakenInCharge());	
			Actor actor = actionStatus.getActor();	
			if (actor!= null){
				ActorType actorObj = new ActorType();
				actionStatusObj.setActor(actorObj);
				actorObj.setActorName(actor.getName());
				actorObj.setActorRole(actor.getRole());
			}
		}	
		return actionStatusesObj;
	}


	private static void marshal(Object rootElement, String xmlFileName, String xsdFileName, String contextPath, String schemaLocation) throws JAXBException, FileNotFoundException, MalformedURLException, SAXException {
		JAXBContext jc = JAXBContext.newInstance( contextPath );
        Marshaller m = jc.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(xsdFileName));
        m.setSchema(schema);
        
        if (schemaLocation != null)
        	m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
        
        m.marshal(rootElement, new File(xmlFileName));
	}
	
	public static void main(String[] args) throws WorkflowMonitorException, JAXBException, FileNotFoundException, SAXException, MalformedURLException {
		WFInfoSerializer f = null;
		
		try {
			f = new WFInfoSerializer();
		} catch (WorkflowMonitorException e) {
			System.err.println("Could not instantiate workflow monitor object");
			throw e;
		}
			
		Object workFlowSystem = null;
		try {
			workFlowSystem = f.build();
		} catch (WorkflowMonitorException e) {
			System.err.println("Work Flow exception");
			throw e;
		}
		
		try {
			WFInfoSerializer.marshal(workFlowSystem, args[0], SCHEMA_FILE, "it.polito.dp2.WF.sol2.jaxb", SCHEMA_LOCATION);
		} catch (JAXBException e) {
			System.err.println("Serialization error");
			throw e;
		} catch (FileNotFoundException e) {
			System.err.println("File not found exception");
			throw e;
	    } catch (SAXException e) {
	        System.err.println("Invalid schema file, unable to validate");
	        throw e;
	    } catch (MalformedURLException e) {
	    	System.err.println("Invalid schema URL, unable to validate");
	    	throw e;
		}
	}
	private static XMLGregorianCalendar convertDate(Calendar date) {
		try {
		  //  
			GregorianCalendar c=new GregorianCalendar();
			c.setTime(date.getTime());
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
		    throw new Error(e);
		}
	    }

}
