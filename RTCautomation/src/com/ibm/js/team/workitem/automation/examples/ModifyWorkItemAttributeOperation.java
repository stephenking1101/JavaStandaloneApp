/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2014. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.js.team.workitem.automation.examples;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class ModifyWorkItemAttributeOperation {

	private static class LoginHandler implements ILoginHandler, ILoginInfo {

		private String fUserId;
		private String fPassword;

		private LoginHandler(String userId, String password) {
			fUserId = userId;
			fPassword = password;
		}

		public String getUserId() {
			return fUserId;
		}

		public String getPassword() {
			return fPassword;
		}

		public ILoginInfo challenge(ITeamRepository repository) {
			return this;
		}
	}

	private static class WorkItemSummaryModification extends WorkItemOperation {

		private String fAttributeId;
		private String fAttributeValueString;

		public WorkItemSummaryModification(String attributeId,
				String attributeValueString) {
			super("Modifying Work Item", IWorkItem.FULL_PROFILE);
			fAttributeId = attributeId;
			fAttributeValueString = attributeValueString;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();
			ITeamRepository teamRepository = (ITeamRepository) workItem
					.getOrigin();
			IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
					.getClientLibrary(IWorkItemClient.class);
			// workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
			if (null != fAttributeId) {
				IAttribute customString = workItemClient.findAttribute(
						workItem.getProjectArea(), fAttributeId, monitor);
				if (workItem.hasCustomAttribute(customString))
					workItem.setValue(customString, fAttributeValueString);
			}
		}
	}

	public static void main(String[] args) {

		boolean result;
		TeamPlatform.startup();
		try {
			result = run(args);
		} catch (TeamRepositoryException x) {
			x.printStackTrace();
			result = false;
		} finally {
			TeamPlatform.shutdown();
		}

		if (!result)
			System.exit(1);
	}

	private static boolean run(String[] args) throws TeamRepositoryException {

		if (args.length != 7) {
			System.out
					.println("Usage: ModifyWorkItem [repositoryURI] [userId] [password] [projectArea] [workItemID] [attributeID] [attributeStringValue]");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String projectAreaName = args[3];
		String idString = args[4];
		String attributeId = args[5];
		String attributeValue = args[6];

		ITeamRepository teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURI);
		teamRepository.registerLoginHandler(new LoginHandler(userId, password));
		teamRepository.login(null);

		IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);
		IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
				.getClientLibrary(IWorkItemClient.class);

		URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
		IProjectArea projectArea = (IProjectArea) processClient
				.findProcessArea(uri, null, null);
		if (projectArea == null) {
			System.out.println("Project area not found.");
			return false;
		}

		int id = new Integer(idString).intValue();
		IWorkItem workItem = workItemClient.findWorkItemById(id,
				IWorkItem.FULL_PROFILE, null);

		WorkItemSummaryModification operation = new WorkItemSummaryModification(
				attributeId, attributeValue);
		operation.run(workItem, null);
		System.out.println("Modified work item " + workItem.getId() + ".");
		teamRepository.logout();

		return true;
	}
}
