/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.rtc.automation.examples;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IDetailedStatus;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.IWorkItemWorkingCopyManager;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class ModifyWorkItem {

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

		if (args.length != 6) {
			System.out
					.println("Usage: ModifyWorkItem [repositoryURI] [userId] [password] [projectArea] [workItemID] [summary]");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String projectAreaName = args[3];
		String idString = args[4];
		String summary = args[5];

		ITeamRepository teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURI);
		teamRepository.registerLoginHandler(new LoginHandler(userId, password));
		teamRepository.login(null);

		IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);
		IAuditableClient auditableClient = (IAuditableClient) teamRepository
				.getClientLibrary(IAuditableClient.class);
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
				IWorkItem.SMALL_PROFILE, null);
		IWorkItemWorkingCopyManager wcm = workItemClient
				.getWorkItemWorkingCopyManager();

		wcm.connect(workItem, IWorkItem.FULL_PROFILE, null);

		try {
			WorkItemWorkingCopy wc = wcm.getWorkingCopy(workItem);

			wc.getWorkItem().setHTMLSummary(
					XMLString.createFromPlainText(summary));
			IWorkItem wi = wc.getWorkItem();
			List<IAttributeHandle> attribs = wi.getCustomAttributes();
			for (@SuppressWarnings("rawtypes")
			Iterator iterator = attribs.iterator(); iterator.hasNext();) {
				IAttributeHandle iAttributeHandle = (IAttributeHandle) iterator
						.next();
				IAttribute anAttribute = (IAttribute) auditableClient
						.resolveAuditable(iAttributeHandle,
								IAttribute.FULL_PROFILE, null);
				System.out.println("AttName:" + anAttribute.getDisplayName());
			}
			IDetailedStatus s = wc.save(null);
			if (!s.isOK()) {
				throw new TeamRepositoryException("Error saving work item",
						s.getException());
			}
		} finally {
			wcm.disconnect(workItem);
		}

		System.out.println("Modified work item: " + workItem.getId() + ".");

		teamRepository.logout();

		return true;
	}
}
