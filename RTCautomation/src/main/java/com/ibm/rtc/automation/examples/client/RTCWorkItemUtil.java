package com.ibm.rtc.automation.examples.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.factory.IReferenceFactory;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.links.common.registry.ILinkTypeRegistry;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.IContributorManager;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IAuditableHandle;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IDetailedStatus;
import com.ibm.team.workitem.client.IQueryClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.IWorkItemWorkingCopyManager;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemReferences;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.common.model.WorkItemEndPoints;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;
import com.ibm.team.workitem.common.query.IQueryDescriptor;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResult;
import com.ibm.team.workitem.common.query.QueryTypes;
import com.ibm.team.workitem.common.workflow.IWorkflowAction;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;

public class RTCWorkItemUtil {
    private static Log logger = LogFactory.getLog(RTCWorkItemUtil.class);
    
    public static List<Integer> runSharedQuery(ITeamRepository repo, IProgressMonitor monitor, String projectAreaName, String queryName) throws TeamRepositoryException{
    	List<Integer> result = new ArrayList<Integer>();
    	IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
    	IQueryClient queryClient = workItemClient.getQueryClient();
    	URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
    	IProcessClientService processClient = (IProcessClientService) repo
                .getClientLibrary(IProcessClientService.class);
    	IProjectArea projectArea = (IProjectArea) processClient.findProcessArea(uri,
                IProcessClientService.ALL_PROPERTIES, monitor);
        if (projectArea == null) {
            logger.error("Project area '" + projectAreaName + "'not found.");
            return result;
        }
        List<IProjectArea> sharingTargets = new ArrayList<IProjectArea>();
        // Add desired sharing targets
        sharingTargets.add(projectArea);
    	IQueryDescriptor query = findSharedQuery(projectArea,sharingTargets, queryName,monitor);
    	IQueryResult<?> unresolvedResults = queryClient.getQueryResults(query);
    	//((QueryResultIterator) unresolvedResults).setLimit(Integer.MAX_VALUE);
    	// Set the load profile
    	ItemProfile<IWorkItem> loadProfile = IWorkItem.FULL_PROFILE;
    	result = processUnresolvedResults(projectArea, unresolvedResults, loadProfile, monitor);
    	return result;
    }
    
    public static List<Integer> runPersonalQuery(ITeamRepository repo, IProgressMonitor monitor, String projectAreaName, String queryName) throws TeamRepositoryException{
    	List<Integer> result = new ArrayList<Integer>();
    	IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
    	IQueryClient queryClient = workItemClient.getQueryClient();
    	URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
    	IProcessClientService processClient = (IProcessClientService) repo
                .getClientLibrary(IProcessClientService.class);
    	IProjectArea projectArea = (IProjectArea) processClient.findProcessArea(uri,
                IProcessClientService.ALL_PROPERTIES, monitor);
        if (projectArea == null) {
            logger.error("Project area '" + projectAreaName + "'not found.");
            return result;
        }
        
    	IQueryDescriptor query = findPersonalQuery(projectArea, queryName,monitor);
    	IQueryResult<?> unresolvedResults = queryClient.getQueryResults(query);
    	//((QueryResultIterator) unresolvedResults).setLimit(Integer.MAX_VALUE);
    	// Set the load profile
    	ItemProfile<IWorkItem> loadProfile = IWorkItem.FULL_PROFILE;
    	result = processUnresolvedResults(projectArea, unresolvedResults, loadProfile, monitor);
    	return result;
    }
    
    private static List<Integer> processUnresolvedResults(IProjectArea projectArea , IQueryResult<?> results,
    		ItemProfile<IWorkItem> profile, IProgressMonitor monitor)
    		throws TeamRepositoryException {
    	List<Integer> list = new ArrayList<Integer>();
    	// Get the required client libraries
    	ITeamRepository teamRepository = (ITeamRepository)projectArea.getOrigin();
    	IWorkItemClient workItemClient = (IWorkItemClient) teamRepository.getClientLibrary(IWorkItemClient.class);
    	IAuditableCommon auditableCommon = (IAuditableCommon) teamRepository.getClientLibrary(IAuditableCommon.class);
    	//long processed = 0;
    	//long pending = 0;
    	//long inProgress = 0;
    	//long complete = 0;
    	while (results.hasNext(monitor)) {
    		IResult result = (IResult) results.next(monitor);
    		IWorkItem workItem = (IWorkItem) auditableCommon.resolveAuditable(
    			(IAuditableHandle) result.getItem(), profile, monitor);
    		IWorkflowInfo workflowInfo = workItemClient.findWorkflowInfo(workItem, monitor);
    		logger.debug("ID:" + workItem.getId() + " "+ workItem.getHTMLSummary().getPlainText() + " Status: " +  workflowInfo.getStateName(workItem.getState2()));
    		/*if(workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase("Requested")){
    			pending++;
    		}
		    if(workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase("Completed")){
		    	complete++;
		    }
		    processed++;*/
    		list.add(workItem.getId());
    	}
    	//inProgress=processed-pending-complete;
    	//System.out.println("Total request: " + processed);
    	//System.out.println("Pending request: " + pending);
    	//System.out.println("Inprogress request: " + inProgress);
    	//System.out.println("Completed request: " + complete);
    	return list;
    }
    
    private static IQueryDescriptor findPersonalQuery(IProjectArea projectArea, 
    		String queryName, IProgressMonitor monitor)
    		throws TeamRepositoryException {
    	// Get the required client libraries
    	ITeamRepository teamRepository = (ITeamRepository)projectArea.getOrigin();
    	IWorkItemClient workItemClient = (IWorkItemClient) teamRepository.getClientLibrary(
    		IWorkItemClient.class);
    	IQueryClient queryClient = workItemClient.getQueryClient();
    	// Get the current user.
    	IContributor loggedIn = teamRepository.loggedInContributor();
    	IQueryDescriptor queryToRun = null;
    	// Get all queries of the user in this project area.
    	List<?> queries = queryClient.findPersonalQueries(
    		projectArea.getProjectArea(), loggedIn,
    		QueryTypes.WORK_ITEM_QUERY,
    		IQueryDescriptor.FULL_PROFILE, monitor);
    	// Find a query with a matching name
    	for (Iterator<?> iterator = queries.iterator(); iterator.hasNext();) {
    		IQueryDescriptor iQueryDescriptor = (IQueryDescriptor) iterator.next();
    		if (iQueryDescriptor.getName().equals(queryName)) {
    			queryToRun = iQueryDescriptor;
    			break;
    		}
    	}
    	return queryToRun;
    }
    
    private static IQueryDescriptor findSharedQuery(	IProjectArea projectArea, 
    		List<IProjectArea> sharingTargets, String queryName,  IProgressMonitor monitor)
    		throws TeamRepositoryException {
    	// Get the required client libraries
    	ITeamRepository teamRepository = (ITeamRepository)projectArea.getOrigin();
    	IWorkItemClient workItemClient = (IWorkItemClient) teamRepository.getClientLibrary(IWorkItemClient.class);
    	IQueryClient queryClient = workItemClient.getQueryClient();
    	IQueryDescriptor queryToRun = null;
    	List<?> queries = queryClient.findSharedQueries(projectArea.getProjectArea(),
    		sharingTargets, QueryTypes.WORK_ITEM_QUERY,
    	IQueryDescriptor.FULL_PROFILE, monitor);
    	// Find a query with a matching name
    	for (Iterator<?> iterator = queries.iterator(); iterator.hasNext();) {
    		IQueryDescriptor iQueryDescriptor = (IQueryDescriptor) iterator.next();
    		if (iQueryDescriptor.getName().equals(queryName)) {
    			queryToRun = iQueryDescriptor;
    			break;
    		}
    	}
    	return queryToRun;
    }
    
    public static void changeG3ParentState(ITeamRepository repo, IProgressMonitor monitor, Integer workItemId, String action, String targetState, String userId)
    		throws TeamRepositoryException{
    	IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
    	IAuditableClient auditableClient = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);
    	List<Integer> workItemIdList = new ArrayList<Integer>();
        workItemIdList.add(workItemId);
    	List<IWorkItemHandle> iWorkItemsList = service.findWorkItemsById(workItemIdList, monitor);
    	
        Iterator<IWorkItemHandle> it = iWorkItemsList.iterator();
        while (it.hasNext()) {
            IWorkItemHandle iwih = it.next();
            IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
	        
	        IWorkItemWorkingCopyManager copyManager = workItemClient.getWorkItemWorkingCopyManager();
	        copyManager.connect(iwih, IWorkItem.FULL_PROFILE, null);
	        WorkItemWorkingCopy workItemCopy = copyManager.getWorkingCopy(iwih);
	        IWorkItem workItem = workItemCopy.getWorkItem();
	        IWorkflowInfo workflowInfo = workItemClient.findWorkflowInfo(workItem, null);
	        if (workflowInfo != null) {
		        logger.debug("Current state id: " + workflowInfo.getStateGroup(workItem.getState2()) + " name:" + workflowInfo.getStateName(workItem.getState2()));
		        if (workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase("Requested")){
		        	 Identifier<IWorkflowAction> actionIds[] = workflowInfo.getAllActionIds();
				      String actionString = null;
				      for(Identifier<IWorkflowAction> actionId : actionIds)
				      {
					        if(workflowInfo.getActionName(actionId).equalsIgnoreCase("In Progress"))
					        {
					        	actionString = actionId.getStringIdentifier();
					        	logger.debug("Action string: " + actionString + " (action:In Progress)");
					        }
				      }
				      workItemCopy.setWorkflowAction(actionString);
				        
				      workItemCopy.save(null);
		        }
		        
		        if (!workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase("Requested") && 
		            !workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase("In Progress") && 
		            !workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase(targetState)){
		        	 Identifier<IWorkflowAction> actionIds[] = workflowInfo.getAllActionIds();
				      String actionString = null;
				      for(Identifier<IWorkflowAction> actionId : actionIds)
				      {
					        if(workflowInfo.getActionName(actionId).equalsIgnoreCase("Resume"))
					        {
					        	actionString = actionId.getStringIdentifier();
					        	logger.debug("Action string: " + actionString + " (action:Resume)");
					        }
				      }
				      workItemCopy.setWorkflowAction(actionString);
				        
//				      IDetailedStatus detailedStatus = workItemCopy.save(null);
		        }
		        
		        if (!workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase(targetState)) {
		        	IProjectArea projectArea = (IProjectArea) repo.itemManager().fetchCompleteItem(workItem.getProjectArea(),
                            IItemManager.DEFAULT, monitor);
		        	
		        	IDevelopmentLineHandle[] developmentLineHandles = projectArea
    						.getDevelopmentLines();

    				for (IDevelopmentLineHandle developmentLineHandle : developmentLineHandles) {
    					IDevelopmentLine developmentLine = auditableClient
    							.resolveAuditable(developmentLineHandle,
    									ItemProfile.DEVELOPMENT_LINE_DEFAULT, monitor);
    					
    					IIterationHandle found = developmentLine.getCurrentIteration();
    		            if (found != null) {
    		            	IAuditableClient fAuditableClient = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);
    		            	IIteration child = fAuditableClient.resolveAuditable(
    		            			found, ItemProfile.ITERATION_DEFAULT, monitor);
    		                if(child.getParent() != null) workItem.setTarget(child.getParent());
    		            	break;
    					} 
    				}
    				/*
		        	ArrayList lines = new ArrayList();
		        	lines.add(iteration);
		        	IIterationHandle found = new TimelineHelper(repo, monitor, projectArea)
                    .findDevelopmentLine(lines, true).getCurrentIteration();
		            if (found != null) {
		            	IAuditableClient fAuditableClient = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);
		            	IIteration child = fAuditableClient.resolveAuditable(
		            			found, ItemProfile.ITERATION_DEFAULT, monitor);
		                if(child.getParent() != null) workItem.setTarget(child.getParent());
		            }*/
		            
		            IContributor owner = repo.contributorManager().fetchContributorByUserId(userId, monitor);
		            if (owner != null) {
		            	workItem.setOwner(owner);
		            }
		            
			        Identifier<IWorkflowAction> actionIds[] = workflowInfo.getAllActionIds();
			        String actionString = null;
			        for(Identifier<IWorkflowAction> actionId : actionIds)
			        {
				        if(workflowInfo.getActionName(actionId).equalsIgnoreCase(action))
				        {
				        	actionString = actionId.getStringIdentifier();
				        	logger.debug("Action string: " + actionString + " (action:" + action + ")");
				        }
			        }
			        workItemCopy.setWorkflowAction(actionString);
			        
			        IDetailedStatus detailedStatus = workItemCopy.save(null);
			        copyManager.disconnect(iwih);
			        if (!detailedStatus.isOK()) {
			        	throw new TeamRepositoryException(detailedStatus.getDetails());
			        } 
		        }
	        }
        }
    }
    
    public static void changeWorkItemsState(ITeamRepository repo, IProgressMonitor monitor, List<Integer> ids, String action, String targetState, boolean updateChildren)
    		throws TeamRepositoryException{
    	IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
    	//IAuditableClient acService = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);
    	List<IWorkItemHandle> iWorkItemsList = service.findWorkItemsById(ids, monitor);
    	
        Iterator<IWorkItemHandle> it = iWorkItemsList.iterator();
        while (it.hasNext()) {
            IWorkItemHandle iwih = it.next();
            //IWorkItem workItem = acService.fetchCurrentAuditable(iwih, IWorkItem.SMALL_PROFILE, monitor);
            IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
	        
	        //"New" Status -> "In Progress" Status by "Start Working" Action 
	        IWorkItemWorkingCopyManager copyManager = workItemClient.getWorkItemWorkingCopyManager();
	        //IWorkItemHandle workItemHandle = workItemClient.findWorkItemById(workItemNumber, IWorkItem.FULL_PROFILE, null);
	        copyManager.connect(iwih, IWorkItem.FULL_PROFILE, null);
	        WorkItemWorkingCopy workItemCopy = copyManager.getWorkingCopy(iwih);
	        IWorkItem workItem = workItemCopy.getWorkItem();
	        if(updateChildren){
	        	List<Integer> workItemsList = getChildrenIdsNonRecursive(repo, monitor, workItem.getId());
		        if(workItemsList!=null && !workItemsList.isEmpty()){
		        	changeWorkItemsState(repo, monitor, workItemsList, action, targetState, true);
		        }	
	        }
	        
	        IWorkflowInfo workflowInfo = workItemClient.findWorkflowInfo(workItem, null);
	        if (workflowInfo != null) {
		        logger.debug("Current state id: " + workflowInfo.getStateGroup(workItem.getState2()) + " name:" + workflowInfo.getStateName(workItem.getState2()));
		        if (!workflowInfo.getStateName(workItem.getState2()).equalsIgnoreCase(targetState)) {
		        	/*IProjectArea projectArea = (IProjectArea) repo.itemManager().fetchCompleteItem(workItem.getProjectArea(),
                            IItemManager.DEFAULT, monitor);*/
		        	
		        	/*
		        	ArrayList lines = new ArrayList();
		        	lines.add(iteration);
		        	IIterationHandle found = new TimelineHelper(repo, monitor, projectArea)
                    .findDevelopmentLine(lines, true).getCurrentIteration();
            
		            if (found != null) {
		                workItem.setTarget(found);
		                //logger.debug("Plan For: " + found);
		            }
		        	
		            IContributor owner = repo.contributorManager().fetchContributorByUserId(userId, monitor);
		            if (owner != null) {
		            	workItem.setOwner(owner);
		            }
		            */
		        	/*List allAttributeHandles = workItemClient.findAttributes(RTCProjectAreaUtil.getProjectArea(repo, monitor, projectAreaName), monitor);
		        	for (Iterator iterator = allAttributeHandles.iterator(); iterator.hasNext();) {
		        	    IAttributeHandle iAttributeHandle = (IAttributeHandle) iterator.next();
		        	    IAttribute test = (IAttribute) repo
		        	        .itemManager().fetchCompleteItem(
		        	        iAttributeHandle, IItemManager.DEFAULT ,monitor);
		        	    System.out.println(test.getIdentifier() + " name:" + test.getDisplayName());
		        	}*/
			        Identifier<IWorkflowAction> actionIds[] = workflowInfo.getAllActionIds();
			        String actionString = null;
			        for(Identifier<IWorkflowAction> actionId : actionIds)
			        {
				        if(workflowInfo.getActionName(actionId).equalsIgnoreCase(action))
				        {
				        	//System.out.println(actionId.getStringIdentifier());
				        	actionString = actionId.getStringIdentifier();
				        	logger.debug("Action string: " + actionString + " (action:" + action + ")");
				        }
			        }
			        workItemCopy.setWorkflowAction(actionString);
			        
			        IDetailedStatus detailedStatus = workItemCopy.save(monitor);
			        copyManager.disconnect(iwih);
			        if (!detailedStatus.isOK()) {
			        	throw new TeamRepositoryException(detailedStatus.getDetails());
			        	//System.out.println("Save Fail = " + detailedStatus.getDetails());
			        } 
		        }
	        }
        }
    }
    
    public static void changeWorkItemState(ITeamRepository repo, IProgressMonitor monitor, Integer workItemId,
            String action, String targetState, boolean updateChildren) throws TeamRepositoryException {
        List<Integer> workItemIdList = new ArrayList<Integer>();
        workItemIdList.add(workItemId);
        changeWorkItemsState(repo, monitor, workItemIdList, action, targetState, updateChildren);
    }
    
    public static void updateWorkItem(ITeamRepository repo, IProgressMonitor monitor, Integer workItemId,
    		Map<String, String> attributeMap, boolean updateChildren) throws TeamRepositoryException {
        List<Integer> workItemIdList = new ArrayList<Integer>();
        workItemIdList.add(workItemId);
        updateWorkItems(repo, monitor, workItemIdList, attributeMap, updateChildren);
    }
    
    public static void updateWorkItems(ITeamRepository repo, IProgressMonitor monitor, List<Integer> ids,
    		Map<String, String> attributeMap, boolean updateChildren) throws TeamRepositoryException {
        IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
    	List<IWorkItemHandle> iWorkItemsList = service.findWorkItemsById(ids, monitor);
    	
        Iterator<IWorkItemHandle> it = iWorkItemsList.iterator();
        while (it.hasNext()) {
        	IWorkItemHandle iwih = it.next();
            IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
            IWorkItemWorkingCopyManager copyManager = workItemClient.getWorkItemWorkingCopyManager();
	        copyManager.connect(iwih, IWorkItem.FULL_PROFILE, null);
	        WorkItemWorkingCopy workItemCopy = copyManager.getWorkingCopy(iwih);
	        IWorkItem workItem = workItemCopy.getWorkItem();
	        if(updateChildren){
	        	List<Integer> workItemsList = getChildrenIdsNonRecursive(repo, monitor, workItem.getId());
		        if(workItemsList!=null && !workItemsList.isEmpty()){
		        	updateWorkItems(repo, monitor, workItemsList, attributeMap, true);
		        }	
	        }
	        List<IAttribute> customAttribs = workItemClient.findAttributes(workItem.getProjectArea(), monitor);

            IProjectArea projectArea = (IProjectArea) repo.itemManager().fetchCompleteItem(workItem.getProjectArea(),
                    IItemManager.DEFAULT, monitor);
            IAuditableClient auditableClient = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);
            
            logger.debug("Update Workitem: " + workItem.getId());
            // set the required attributes
            for (IAttribute ia : customAttribs) {
                String attribute = ia.getDisplayName();
                String value = (String) attributeMap.get(attribute);
                Object currentValue = null;
                if (ia != null && workItem.hasAttribute(ia)) {
	        		//logger.debug("Estimate value: " + value);
                	currentValue = workItem.getValue(ia); 
	        	}
                
                //logger.debug("Attribute name:" + attribute + " Attribute type:" + ia.getAttributeType());              
	        	if (value != null && ia != null) {
	        		if(value.startsWith("%") && value.endsWith("%")){
	        			IAttribute iAttribute = findAttribute(repo, projectArea, value.substring(1, value.length()-1));
	        			if(iAttribute != null) {
	        				workItem.setValue(ia, workItem.getValue(iAttribute));
	        				logger.debug("Set " + attribute + ": " + value);
	        			} else if(value.substring(1, value.length()-1).equals("currentIteration")){
	        				IDevelopmentLineHandle[] developmentLineHandles = projectArea
	        						.getDevelopmentLines();

	        				for (IDevelopmentLineHandle developmentLineHandle : developmentLineHandles) {
	        					IDevelopmentLine developmentLine = auditableClient
	        							.resolveAuditable(developmentLineHandle,
	        									ItemProfile.DEVELOPMENT_LINE_DEFAULT, monitor);
	        					
	        					IIterationHandle found = developmentLine.getCurrentIteration();
	        		            if (found != null) {
	        		            	workItem.setTarget(found);
	        		            	logger.debug("Set " + attribute + ": " + value);
	        		            	break;
	        					} 
	        				}
	        			}
	        		} else if (currentValue instanceof Long) {
	        			workItem.setValue(ia, Long.valueOf(value));
	        			//logger.debug(currentValue instanceof Long);
	        			logger.debug("Set " + attribute + ": " + value);
	        		} else if (currentValue instanceof IContributor || attribute.equals("Owned By")){
	        			//logger.debug(currentValue instanceof IContributor);
	        			IContributor owner = repo.contributorManager().fetchContributorByUserId(value, monitor);
	        	        if (owner != null) {
	        	        	workItem.setOwner(owner);
	        	        	logger.debug("Set " + attribute + ": " + value);
	        	        }
	        		} else if(currentValue instanceof Identifier){
	        			workItem.setValue(ia, getLiteralEqualsString(repo, value, ia));
	        			logger.debug("Set " + attribute + ": " + value);
	        		} else if(currentValue instanceof IIterationHandle){
	                    IIterationHandle found = new TimelineHelper(repo, monitor, projectArea)
                        .findIteration(Arrays.asList(value.split("/")), false);
		                if (found != null) {
		                    workItem.setTarget(found);
		                    logger.debug("Set " + attribute + ": " + value);
		                }
		        	} else {    
	        			workItem.setValue(ia, value);
	        			logger.debug("Set " + attribute + ": " + value);
	        		}
	        	}
            }
            
            if (attributeMap.containsKey("%parent%")){
            	String value = (String) attributeMap.get("%parent%");
	            IWorkItem foundWI = workItemClient.findWorkItemById(new Integer(value), IWorkItem.SMALL_PROFILE, monitor);
	        	// Create a new reference to the opposite item
	    		IItemReference reference = IReferenceFactory.INSTANCE.createReferenceToItem(foundWI);
	    		// Create a new end point
	    		IEndPointDescriptor endpoint = ILinkTypeRegistry.INSTANCE.getLinkType(WorkItemLinkTypes.PARENT_WORK_ITEM).getTargetEndPointDescriptor();
	    		// Add the new reference using a specific work item end point
	    		workItemCopy.getReferences().add(endpoint, reference);	
	    		logger.debug("Set Parent: " + value);
        	}
            
            IDetailedStatus detailedStatus = workItemCopy.save(monitor);
	        copyManager.disconnect(iwih);
	        if (!detailedStatus.isOK()) {
	        	throw new TeamRepositoryException(detailedStatus.getDetails());
	        } 
        }
    }
    
    private static IAttribute findAttribute(ITeamRepository repo, IProjectArea projectArea, String name) throws TeamRepositoryException {
    	IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);

    	List<IAttribute> customAttribs = workItemClient.findAttributes(projectArea, null);
        for (IAttribute ia : customAttribs) {
        	if(ia.getDisplayName().equals(name)){
        		return ia;
        	}
        }
        
    	return null;
    }
    
    private static Identifier<?> getLiteralEqualsString(ITeamRepository repo, String name, IAttributeHandle ia) throws TeamRepositoryException {
    	IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);

    	Identifier<?> literalID = null;
    	IEnumeration<?> enumeration = workItemClient.resolveEnumeration(ia, null); // or IWorkitemCommon
    	List<?> literals = enumeration.getEnumerationLiterals();
    	for (Iterator<?> iterator = literals.iterator(); iterator.hasNext();) {
    		ILiteral iLiteral = (ILiteral) iterator.next();
    		if (iLiteral.getName().equals(name)) {
    			literalID = iLiteral.getIdentifier2();
    			break;
    		}
    	}
    	return literalID;
    }

    public static void changeWorkItemsOwner(ITeamRepository repo, IProgressMonitor monitor, List<Integer> ids,
            String newUserId) throws TeamRepositoryException {

        // get new user
        IContributorManager contributorManager = repo.contributorManager();
        IContributor newUser = contributorManager.fetchContributorByUserId(newUserId, monitor);
        logger.debug("New user id: " + newUser.getUserId() + " (" + newUser.getName() + ")");

        IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
        IAuditableClient acService = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);

        IWorkItemWorkingCopyManager workItemWorkingCopyMgr = service.getWorkItemWorkingCopyManager();

        List<IWorkItemHandle> iWorkItemsList = service.findWorkItemsById(ids, monitor);
        Iterator<IWorkItemHandle> it = iWorkItemsList.iterator();
        while (it.hasNext()) {
            IWorkItemHandle iwih = it.next();
            IWorkItem showWI = acService.fetchCurrentAuditable(iwih, IWorkItem.SMALL_PROFILE, monitor);
            IContributor owner = (IContributor) repo.itemManager().fetchCompleteItem(showWI.getOwner(),
                    IItemManager.DEFAULT, monitor);
            logger.debug("WorkItem id: " + showWI.getId() + " (" + showWI.getHTMLSummary()
                    + ") is currently owned by User Id: " + owner.getUserId() + " (" + owner.getName() + ")");

            try {
                workItemWorkingCopyMgr.connect(iwih, IWorkItem.SMALL_PROFILE, monitor);
                WorkItemWorkingCopy wc = workItemWorkingCopyMgr.getWorkingCopy(iwih);

                IWorkItem workItemToBeUpdated = wc.getWorkItem();

                workItemToBeUpdated.setOwner(newUser);

                IDetailedStatus s = wc.save(monitor);
                if (!s.isOK()) {
                    throw new TeamRepositoryException("Error saving work item", s.getException());
                }
            } finally {
                service.getWorkItemWorkingCopyManager().disconnect(iwih);
            }

        }
    }

    public static void changeWorkItemOwner(ITeamRepository repo, IProgressMonitor monitor, Integer workItemId,
            String newUserId) throws TeamRepositoryException {
        List<Integer> workItemIdList = new ArrayList<Integer>();
        workItemIdList.add(workItemId);
        changeWorkItemsOwner(repo, monitor, workItemIdList, newUserId);
    }

    public static List<Integer> getChildrenIdsNonRecursive(ITeamRepository repo, IProgressMonitor monitor, Integer parentWorkItem)
            throws TeamRepositoryException {

        List<Integer> result = new ArrayList<Integer>();
        IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
        IAuditableClient acService = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);

        IWorkItemWorkingCopyManager workItemWorkingCopyMgr = service.getWorkItemWorkingCopyManager();
        IWorkItem foundWI = service.findWorkItemById(parentWorkItem, IWorkItem.SMALL_PROFILE, monitor);

        if (foundWI == null) {
            logger.error("PROBLEM: Work Item " + parentWorkItem + " not found in the current RTC instance");
            return result;
        }

        workItemWorkingCopyMgr.connect(foundWI, IWorkItem.SMALL_PROFILE, monitor);
        WorkItemWorkingCopy wc = workItemWorkingCopyMgr.getWorkingCopy(foundWI);

        IWorkItemHandle handle = (IWorkItemHandle) wc.getWorkItem().getItemHandle();
        IWorkItemReferences references = service.resolveWorkItemReferences(handle, monitor);

        IEndPointDescriptor endpoint = WorkItemEndPoints.CHILD_WORK_ITEMS;
        List<IReference> referencesList = references.getReferences(endpoint);
        Iterator<IReference> it = referencesList.iterator();
        while (it.hasNext()) {
            IReference ref = it.next();
            if (ref.isItemReference()) {
                IItemReference itemRef = (IItemReference) ref;
                IItemHandle iih = itemRef.getReferencedItem();
                if (handle instanceof IWorkItemHandle) {
                    IWorkItemHandle wiHandle = (IWorkItemHandle) iih;

                    IWorkItem showWI = (IWorkItem) acService.fetchCurrentAuditable(wiHandle, IWorkItem.SMALL_PROFILE,
                            monitor);

                    logger.debug("WorkItem id: " + showWI.getId() + " ("
                            + StringEscapeUtils.unescapeHtml(showWI.getHTMLSummary().getPlainText()) + ")");

                    result.add(new Integer(showWI.getId()));
                }
            }

        }
        return result;
    }
    
    //recursive
    public static List<Integer> getChildrenIds(ITeamRepository repo, IProgressMonitor monitor, Integer parentWorkItem)
            throws TeamRepositoryException {

        List<Integer> result = new ArrayList<Integer>();
        IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
        IAuditableClient acService = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);

        IWorkItemWorkingCopyManager workItemWorkingCopyMgr = service.getWorkItemWorkingCopyManager();
        IWorkItem foundWI = service.findWorkItemById(parentWorkItem, IWorkItem.SMALL_PROFILE, monitor);

        if (foundWI == null) {
            logger.error("PROBLEM: Work Item " + parentWorkItem + " not found in the current RTC instance");
            return result;
        }

        workItemWorkingCopyMgr.connect(foundWI, IWorkItem.SMALL_PROFILE, monitor);
        WorkItemWorkingCopy wc = workItemWorkingCopyMgr.getWorkingCopy(foundWI);

        IWorkItemHandle handle = (IWorkItemHandle) wc.getWorkItem().getItemHandle();
        IWorkItemReferences references = service.resolveWorkItemReferences(handle, monitor);

        IEndPointDescriptor endpoint = WorkItemEndPoints.CHILD_WORK_ITEMS;
        List<IReference> referencesList = references.getReferences(endpoint);
        Iterator<IReference> it = referencesList.iterator();
        while (it.hasNext()) {
            IReference ref = it.next();
            if (ref.isItemReference()) {
                IItemReference itemRef = (IItemReference) ref;
                IItemHandle iih = itemRef.getReferencedItem();
                if (handle instanceof IWorkItemHandle) {
                    IWorkItemHandle wiHandle = (IWorkItemHandle) iih;

                    IWorkItem showWI = (IWorkItem) acService.fetchCurrentAuditable(wiHandle, IWorkItem.SMALL_PROFILE,
                            monitor);

                    logger.debug("WorkItem id: " + showWI.getId() + " ("
                            + StringEscapeUtils.unescapeHtml(showWI.getHTMLSummary().getPlainText()) + ")");

                    result.add(new Integer(showWI.getId()));
                    result.addAll(getChildrenIds(repo, monitor, new Integer(showWI.getId())));
                }
            }

        }
        return result;
    }

    public static void replaceWorkItemsVariables(ITeamRepository repo, IProgressMonitor monitor,
            List<Integer> workItemIds, Map<String, String> replacementMap) throws TeamRepositoryException {

        IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
        // IAuditableClient acService = (IAuditableClient)
        // repo.getClientLibrary(IAuditableClient.class);

        IWorkItemWorkingCopyManager workItemWorkingCopyMgr = service.getWorkItemWorkingCopyManager();

        List<IWorkItemHandle> iWorkItemsList = service.findWorkItemsById(workItemIds, monitor);
        Iterator<IWorkItemHandle> it = iWorkItemsList.iterator();
        while (it.hasNext()) {
            IWorkItemHandle iwih = it.next();

            try {
                workItemWorkingCopyMgr.connect(iwih, IWorkItem.SMALL_PROFILE, monitor);
                WorkItemWorkingCopy wc = workItemWorkingCopyMgr.getWorkingCopy(iwih);

                IWorkItem workItemToBeUpdated = wc.getWorkItem();

                // replaceSummary
                String originalHTMLSummary = workItemToBeUpdated.getHTMLSummary().getPlainText();
                String unescapedSummary = StringEscapeUtils.unescapeHtml(originalHTMLSummary);
                logger.debug("WorkItem id: " + workItemToBeUpdated.getId() + " existing Summary (" + unescapedSummary
                        + ")");

                String replacedUnscappedSummary = TokenReplacer.replaceTokens(unescapedSummary, replacementMap);

                logger.debug("WorkItem id: " + workItemToBeUpdated.getId() + " new Summary ("
                        + replacedUnscappedSummary + ")");
                XMLString newHTMLSummary = XMLString.createFromPlainText(StringEscapeUtils
                        .escapeHtml(replacedUnscappedSummary));

                // maybe there is API bug in getHTMLDescription(). It
                // throws
                // Exception in thread "main" java.lang.IllegalStateException:
                // Attempting to get unset feature: Description

                // String originalHTMLDescription =
                // workItemToBeUpdated.getHTMLDescription().getPlainText();
                // logger.debug("Existing Description: " +
                // originalHTMLDescription);

                workItemToBeUpdated.setHTMLSummary(newHTMLSummary);

                IDetailedStatus s = wc.save(monitor);
                if (!s.isOK()) {
                    throw new TeamRepositoryException("Error saving work item", s.getException());
                }
            } finally {
                service.getWorkItemWorkingCopyManager().disconnect(iwih);
            }

        }

    }

    public static void replaceWorkItemVariables(ITeamRepository repo, IProgressMonitor monitor, Integer workItemId,
            Map<String, String> replacementMap) throws TeamRepositoryException {
        List<Integer> workItemIdList = new ArrayList<Integer>();
        workItemIdList.add(workItemId);
        replaceWorkItemsVariables(repo, monitor, workItemIdList, replacementMap);
    }

    public static void showWorkItemHistory(ITeamRepository repo, IProgressMonitor monitor, String workItemId) throws TeamRepositoryException {
    	IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
    	IWorkItem workItem = service.findWorkItemById(Integer.valueOf(workItemId), IWorkItem.FULL_PROFILE, monitor);
    	logger.debug("Last modified date: "+workItem.modified()+"\n");

        IItemManager itm = repo.itemManager(); 
        List<?> history = itm.fetchAllStateHandles((IAuditableHandle) workItem.getStateHandle(), monitor);
        logger.debug("Record history details:-" + workItem.getId());
        for(int i = history.size() -1; i >= 0; i--){
            IAuditableHandle audit = (IAuditableHandle) history.get(i);
            IWorkItem workItemPrevious = (IWorkItem) repo.itemManager().fetchCompleteState(audit,null);
            Date recordModifiedDate = workItemPrevious.modified();
            logger.debug(i + " Record modification date: "+recordModifiedDate);
            
            List<IAttribute> customAttribs = service.findAttributes(workItem.getProjectArea(), monitor);

            for (IAttribute ia : customAttribs) {
                String attribute = ia.getDisplayName();
                Object currentValue = null;
                if (ia != null && workItemPrevious.hasAttribute(ia)) {
	        		//logger.debug("Estimate value: " + value);
                	currentValue = workItemPrevious.getValue(ia); 
	        	}
                logger.debug(i + " attribute: " + attribute + " with value: " + currentValue);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	public static boolean createWorkItems(ITeamRepository repo, IProgressMonitor monitor,
            Map<String, Object> propFiles, Map<String, Object> parents) throws NumberFormatException, TeamRepositoryException {
    	Set<String> files = propFiles.keySet();
    	Iterator<String> it = files.iterator();
    	Map<String, Object> children = new HashMap<String, Object>();
    	while(it.hasNext()){
    		String key = it.next();
    		Map<String, String> replacementMap = (Map<String, String>) propFiles.get(key);
    		if(replacementMap.containsKey("%parent%")){
    			String parent = replacementMap.get("%parent%");
    			if(parent.startsWith("%") && parent.endsWith("%")){
        			String file = parent.substring(1, parent.length()-1);
        			if(parents.containsKey(file) && !file.equals(key)){
        				Object parentWI = parents.get(file);
        				if(parentWI instanceof Map){
        					children.put(key, replacementMap);
        					continue;
        				} else if(parentWI instanceof Integer){
        					replacementMap.put("%parent%", parentWI.toString());
        				}
        			} else {
        				logger.error("Parent " + parent + "not found..." + key);
        				continue;
        			}
    			}
    		}
    		
    		String projectAreaName = replacementMap.get("ProjectArea");
            String workItemTypeName = replacementMap.get("WorkItemType");
            String categoryName = replacementMap.get("Category");
            String summary = replacementMap.get("Summary");
            //String userId = replacementMap.get("Owner");
            //String parentWorkItem = replacementMap.get("ParentWorkItem");
            //String description = replacementMap.get("Description");
            IProcessClientService processClient = (IProcessClientService) repo
                    .getClientLibrary(IProcessClientService.class);
            IWorkItemClient workItemClient = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);
            IAuditableClient auditableClient = (IAuditableClient) repo.getClientLibrary(IAuditableClient.class);

            logger.debug("Project area '" + projectAreaName + "'");
            logger.debug("Work item type '" + workItemTypeName + "'");
            //logger.debug("Summary '" + summary + "'");
            URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));

            IProjectArea projectArea = (IProjectArea) processClient.findProcessArea(uri,
                    IProcessClientService.ALL_PROPERTIES, monitor);
            if (projectArea == null) {
                logger.error("Project area '" + projectAreaName + "'not found.");
                return false;
            }

            IWorkItemType workItemType = workItemClient.findWorkItemType(projectArea, workItemTypeName, null);
            if (workItemType == null) {
                logger.error("Work item type '" + workItemTypeName + "' not found.");
                return false;
            }

            // List<ICategory> findCategories=
            // workItemClient.findCategories(projectArea, ICategory.FULL_PROFILE,
            // monitor);
            // ICategory category = findCategories.get(5);
            //List<String> categoryPath = Arrays.asList("01.Development".split("/"));
            List<String> categoryPath = Arrays.asList(categoryName.split("/"));
            ICategoryHandle category = workItemClient.findCategoryByNamePath(projectArea, categoryPath, monitor);
            if (category == null) {
                logger.error("Category '" + categoryName + "'not found.");
                return false;
            }

            WorkItemInitialization operation = new WorkItemInitialization(summary, category, repo, replacementMap);
            IWorkItemHandle handle = operation.run(workItemType, monitor);
            IWorkItem workItem = auditableClient.resolveAuditable(handle, IWorkItem.FULL_PROFILE, monitor);

            logger.info("Created work item " + workItem.getId() + ".");
            propFiles.put(key, Integer.valueOf(workItem.getId()));
    	}
    	
    	if(!children.isEmpty()) {
    		return createWorkItems(repo, monitor, children, propFiles);
    	}
        return true;
    }

    // support methods
    /*private static String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        System.out.println(date);
        return dateFormat.format(date);
    }*/
    public static void synchWorkItem(ITeamRepository repo, IProgressMonitor monitor, String workItemId) throws TeamRepositoryException {
    	IWorkItemClient service = (IWorkItemClient) repo.getClientLibrary(IWorkItemClient.class);

        //IWorkItemWorkingCopyManager workItemWorkingCopyMgr = service.getWorkItemWorkingCopyManager();
        IWorkItem foundWI = service.findWorkItemById(Integer.valueOf(workItemId), IWorkItem.FULL_PROFILE, monitor);
        /*
        workItemWorkingCopyMgr.connect(foundWI, IWorkItem.FULL_PROFILE, monitor);
        WorkItemWorkingCopy workingCopy = workItemWorkingCopyMgr.getWorkingCopy(foundWI);
    	IWorkItem workItem = workingCopy.getWorkItem();

		IWorkItemType type = service.findWorkItemType(
				workItem.getProjectArea(), workItem.getWorkItemType(),
				monitor);
		
		if (type != null) {
			//logger.debug("Synchronizing workitem " + workItem.getId() + ".");
			service.updateWorkItemType(workItem, type, type,
					monitor);
			logger.debug("Synchronized workitem: " + workItem.getId());
		}*/
		SynchronizeWorkItemOperation synchronize = new SynchronizeWorkItemOperation(service);
		synchronize.run(foundWI, monitor);
    }

    private static class WorkItemInitialization extends WorkItemOperation {
        private String fSummary;
        private ICategoryHandle fCategory;
        private ITeamRepository frepo;
        private Map<String, String> fAttributeMap;

        public WorkItemInitialization(String summary, ICategoryHandle category, ITeamRepository repo, Map<String, String> attributeMap) {
            super("Initializing Work Item");
            fSummary = summary;
            fCategory = category;
            frepo = repo;
            fAttributeMap = attributeMap;
        }

        protected void execute(WorkItemWorkingCopy workingCopy, IProgressMonitor monitor)
                throws TeamRepositoryException {
            IWorkItem workItem = workingCopy.getWorkItem();
            workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
            workItem.setCategory(fCategory);

            IWorkItemClient workItemClient = (IWorkItemClient) frepo.getClientLibrary(IWorkItemClient.class);
            List<IAttribute> customAttribs = workItemClient.findAttributes(workItem.getProjectArea(), monitor);

            IProjectArea projectArea = (IProjectArea) frepo.itemManager().fetchCompleteItem(workItem.getProjectArea(),
                    IItemManager.DEFAULT, monitor);
            IAuditableClient auditableClient = (IAuditableClient) frepo.getClientLibrary(IAuditableClient.class);
            // set the required attributes
            for (IAttribute ia : customAttribs) {
                String attribute = ia.getDisplayName();
                String value = (String) fAttributeMap.get(attribute);
                Object currentValue = null;
                if (ia != null && workItem.hasAttribute(ia)) {
	        		//logger.debug(attribute + " value: " + value);
                	currentValue = workItem.getValue(ia); 
	        	}
                
                //logger.debug("Attribute name:" + attribute + " Attribute type:" + ia.getAttributeType());
                //if (ia != null && attribute.equals("Planned For")) {
                /*if (ia != null && (currentValue instanceof IIterationHandle)) {	
                    //IWorkItem iWorkItem = workItemClient.findWorkItemById(87406, IWorkItem.FULL_PROFILE, monitor);
                    //logger.debug(iWorkItem.getValue(workItemClient.findAttribute(iWorkItem.getProjectArea(), "target",
                    //        null)));
                    //logger.debug(iWorkItem.getHTMLSummary());
                    //workItem.setValue(ia, iWorkItem.getValue(workItemClient.findAttribute(iWorkItem.getProjectArea(),
                    //        "target", null)));
                    
                    // set the "planed for"
                	//logger.debug(currentValue instanceof IIterationHandle);
                	if(value != null){
	                    IIterationHandle found = new TimelineHelper(frepo, monitor, projectArea)
	                            .getIterationLiteral(value);
	                    
	                    if (found != null) {
	                        workItem.setTarget(found);
	                        logger.debug("Set " + attribute + ": " + value);
	                    }
                	} else {
	                    ArrayList lines = new ArrayList();
	    	        	lines.add("GL");
	    	        	IIterationHandle found = new TimelineHelper(frepo, monitor, projectArea)
	                    .findDevelopmentLine(lines, true).getCurrentIteration();
	            
	    	            if (found != null) {
	    	                workItem.setTarget(found);
	    	                logger.debug("Set " + attribute + ": " + found);
	    	            }
                	}
                	
                	continue;
                }*/
                
	        	if (value != null && ia != null) {
	        		if(value.startsWith("%") && value.endsWith("%")){
	        			if(value.substring(1, value.length()-1).equals("currentIteration")){
	        				IDevelopmentLineHandle[] developmentLineHandles = projectArea
	        						.getDevelopmentLines();

	        				for (IDevelopmentLineHandle developmentLineHandle : developmentLineHandles) {
	        					IDevelopmentLine developmentLine = auditableClient
	        							.resolveAuditable(developmentLineHandle,
	        									ItemProfile.DEVELOPMENT_LINE_DEFAULT, monitor);
	        					
	        					IIterationHandle found = developmentLine.getCurrentIteration();
	        		            if (found != null) {
	        		            	workItem.setTarget(found);
	        		            	logger.debug("Set " + attribute + ": " + value);
	        		            	break;
	        					} 
	        				}
	        			}
	        		} else if (currentValue instanceof Long) {
	        			//attribute.equals("Estimate")
	        			workItem.setValue(ia, Long.valueOf(value));
	        			//logger.debug(currentValue instanceof Long);
	        			logger.debug("Set " + attribute + ": " + value);
	        		} else if (currentValue instanceof IContributor || attribute.equals("Owned By")){
	        			//attribute.equals("Owned By")
	        			//logger.debug(currentValue instanceof IContributor);
	        			IContributor owner = frepo.contributorManager().fetchContributorByUserId(value, monitor);
	        	        if (owner != null) {
	        	        	workItem.setOwner(owner);
	        	        	logger.debug("Set " + attribute + ": " + value);
	        	        }
	        		} else if(currentValue instanceof Identifier){
	        			workItem.setValue(ia, getLiteralEqualsString(value, ia));
	        			logger.debug("Set " + attribute + ": " + value);
	        		} else if(currentValue instanceof IIterationHandle || attribute.equals("Planned For")){
	                    IIterationHandle found = new TimelineHelper(frepo, monitor, projectArea)
                        .findIteration(Arrays.asList(value.split("/")), false);
		                if (found != null) {
		                    workItem.setTarget(found);
		                    logger.debug("Set " + attribute + ": " + value);
		                }
		        	} else {    
	        			workItem.setValue(ia, value);
	        			logger.debug("Set " + attribute + ": " + value);
	        		}
	        	}
            }
            
            if (fAttributeMap.containsKey("%parent%")){
            	String value = (String) fAttributeMap.get("%parent%");
        		IWorkItemClient service = (IWorkItemClient) frepo.getClientLibrary(IWorkItemClient.class);
	            IWorkItem foundWI = service.findWorkItemById(new Integer(value), IWorkItem.SMALL_PROFILE, monitor);
	        	// Create a new reference to the opposite item
	    		IItemReference reference = IReferenceFactory.INSTANCE.createReferenceToItem(foundWI);
	    		// Create a new end point
	    		IEndPointDescriptor endpoint = ILinkTypeRegistry.INSTANCE.getLinkType(WorkItemLinkTypes.PARENT_WORK_ITEM).getTargetEndPointDescriptor();
	    		// Add the new reference using a specific work item end point
	    		workingCopy.getReferences().add(endpoint, reference);	
	    		logger.debug("Set Parent: " + value);
        	}
        }
        
        private Identifier<?> getLiteralEqualsString(String name, IAttributeHandle ia) throws TeamRepositoryException {
        	IWorkItemClient workItemClient = (IWorkItemClient) frepo.getClientLibrary(IWorkItemClient.class);

        	Identifier<?> literalID = null;
        	IEnumeration<?> enumeration = workItemClient.resolveEnumeration(ia, null); // or IWorkitemCommon
        	List<?> literals = enumeration.getEnumerationLiterals();
        	for (Iterator<?> iterator = literals.iterator(); iterator.hasNext();) {
        		ILiteral iLiteral = (ILiteral) iterator.next();
        		if (iLiteral.getName().equals(name)) {
        			literalID = iLiteral.getIdentifier2();
        			break;
        		}
        	}
        	return literalID;
        }
    }
    
    private static class SynchronizeWorkItemOperation extends WorkItemOperation {

		private IWorkItemClient fWorkItemClient = null;

		public SynchronizeWorkItemOperation( IWorkItemClient workItemClient) {
			super("Synchronize Work Items", IWorkItem.FULL_PROFILE);
			fWorkItemClient = workItemClient;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();

			IWorkItemType type = fWorkItemClient.findWorkItemType(
					workItem.getProjectArea(), workItem.getWorkItemType(),
					monitor);
			if (type != null) {
				fWorkItemClient.updateWorkItemType(workItem, type, type,
						monitor);
				logger.debug("Synchronized workitem: " + workItem.getId());
			}
		}
	}
}
