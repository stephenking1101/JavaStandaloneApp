/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.js.team.workitem.automation.examples;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IApproval;
import com.ibm.team.workitem.common.model.IApprovalDescriptor;
import com.ibm.team.workitem.common.model.IApprovals;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.WorkItemApprovals;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class ModifyAddWorkItemApproval {

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
	 * 
	 */
	private static class WorkItemCreateApproval extends WorkItemOperation {

		private String fContributorUserID;
		private String fApprovalName;

		public WorkItemCreateApproval(String contributorUserID,
				String approvalName) {
			super("Add Approval", IWorkItem.FULL_PROFILE);
			fContributorUserID = contributorUserID;
			fApprovalName = approvalName;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {

			// Get the work item to get the team repository.
			IWorkItem workItem = workingCopy.getWorkItem();
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
				// Approve @See https://jazz.net/library/article/1118/
				// approval.setStateIdentifier(WorkItemApprovals.APPROVED_STATE.getDisplayName(null));
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

		if (args.length != 6) {
			System.out
					.println("Usage: ModifyWorkItemAddApproval <repositoryURI> <userId> <password> <workItemID> <approverUserID> <approvalName>");
			System.out.println(" <approverUserID> for example 'bob' ");
			System.out.println(" <approvelName> for example 'Code Review' ");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String idString = args[3];
		String approverUserID = args[4];
		String approvelName = args[5];

		ITeamRepository teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURI);
		teamRepository.registerLoginHandler(new LoginHandler(userId, password));
		teamRepository.login(null);

		IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
				.getClientLibrary(IWorkItemClient.class);

		int id = new Integer(idString).intValue();
		IWorkItem workItem = workItemClient.findWorkItemById(id,
				IWorkItem.FULL_PROFILE, null);

		WorkItemCreateApproval operation = new WorkItemCreateApproval(
				approverUserID, approvelName);
		operation.run(workItem, null);

		System.out.println("Modified work item " + workItem.getId() + ".");

		teamRepository.logout();

		return true;
	}
}
