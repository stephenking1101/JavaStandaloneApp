/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.rtc.automation.examples;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IApproval;
import com.ibm.team.workitem.common.model.IApprovalDescriptor;
import com.ibm.team.workitem.common.model.IApprovals;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.WorkItemApprovals;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class CreateWorkItemApproval {

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

	/**
	 * Add an approval to a work item
	 * 
	 * @author rschoon
	 * 
	 */
	private static class WorkItemCreateApproval extends WorkItemOperation {

		private String fSummary;
		private ICategoryHandle fCategory;
		private String fContributorUserID;
		private String fApprovalName;

		public WorkItemCreateApproval(String summary, ICategoryHandle category,
				String contributorUserID, String approvalName) {
			super("Add Attachment", IWorkItem.FULL_PROFILE);
			fSummary = summary;
			fCategory = category;
			fContributorUserID = contributorUserID;
			fApprovalName = approvalName;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();
			workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
			workItem.setCategory(fCategory);

			ITeamRepository repo = (ITeamRepository) workItem.getOrigin();
			// Find a contributor based on the ID
			IContributor aUser = repo.contributorManager()
					.fetchContributorByUserId(fContributorUserID, null);
			// Find a contributor based on the login information
			IContributor loggedIn = repo.loggedInContributor();
			ArrayList<IContributorHandle> reviewers = new ArrayList<IContributorHandle>();
			reviewers.add(loggedIn);
			reviewers.add(aUser);

			// Create a new approval and add the approvers
			IApprovals approvals = workItem.getApprovals();
			IApprovalDescriptor descriptor = approvals.createDescriptor(
					WorkItemApprovals.REVIEW_TYPE.getIdentifier(),
					fApprovalName);
			for (IContributorHandle reviewer : reviewers) {
				IApproval approval = approvals.createApproval(descriptor,
						reviewer);
				approvals.add(approval);
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

		if (args.length != 9) {
			System.out
					.println("Usage: CreateWorkItemApproval [repositoryURI] [userId] [password] [projectArea] [workItemType] [summary] [category> [approverUserID] [approvalName]");
			System.out.println(" [approverUserID] for example 'bob' ");
			System.out.println(" [approvelName] for example 'Code Review' ");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String projectAreaName = args[3];
		String typeIdentifier = args[4];
		String summary = args[5];
		String categoryName = args[6];
		String approverUserID = args[7];
		String approvelName = args[8];

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

		IWorkItemType workItemType = workItemClient.findWorkItemType(
				projectArea, typeIdentifier, null);
		if (workItemType == null) {
			System.out.println("Work item type not found.");
			return false;
		}

		List<String> path = Arrays.asList(categoryName.split("/"));
		ICategoryHandle category = workItemClient.findCategoryByNamePath(
				projectArea, path, null);
		if (category == null) {
			System.out.println("Category not found.");
			return false;
		}

		WorkItemCreateApproval operation = new WorkItemCreateApproval(summary,
				category, approverUserID, approvelName);
		IWorkItemHandle handle = operation.run(workItemType, null);

		IWorkItem workItem = auditableClient.resolveAuditable(handle,
				IWorkItem.FULL_PROFILE, null);
		System.out.println("Created work item " + workItem.getId() + ".");

		teamRepository.logout();

		return true;
	}
}
