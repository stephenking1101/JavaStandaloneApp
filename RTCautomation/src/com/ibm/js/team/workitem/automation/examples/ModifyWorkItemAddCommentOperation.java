/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2013. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.js.team.workitem.automation.examples;

import java.rmi.RemoteException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.ItemNotFoundException;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.IComment;
import com.ibm.team.workitem.common.model.IComments;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * Modifies a work item, creating a comment. The user who is associated with the
 * comment can be different from the modifying user if the optional user ID is
 * provided.
 * 
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class ModifyWorkItemAddCommentOperation {

	public ModifyWorkItemAddCommentOperation() throws RemoteException {
		super();
	}

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

	private static class WorkItemAddComment extends WorkItemOperation {

		private String fComment;
		private IContributorHandle fCommenter;

		public WorkItemAddComment(IContributorHandle commenter, String comment) {
			super(
					"Add Comment to Work Item",
					IWorkItem.SMALL_PROFILE
							.createExtension(Arrays
									.asList(new String[] { IWorkItem.COMMENTS_PROPERTY })));
			fComment = comment;
			fCommenter = commenter;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();

			IComments comments = workItem.getComments();
			IComment newComment = comments.createComment(fCommenter,
					XMLString.createFromPlainText(fComment));
			comments.append(newComment);
		}
	}

	public static void main(String[] args) {

		boolean result;
		if (!TeamPlatform.isStarted()) {
			System.out.println("Starting");
			TeamPlatform.startup();
		}
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

		boolean result = false;
		if (args.length < 5 || args.length > 6) {
			System.out
					.println("Usage: AddComment [repositoryURI] [userId] [password] [workItemID] [commentString] {[commenterID]}");
			return result;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String idString = args[3];
		String commentText = args[4];
		String commenterID = null;
		if (args.length == 6) {
			commenterID = args[5];
		}

		IProgressMonitor monitor = new NullProgressMonitor();

		ITeamRepository teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURI);
		teamRepository.registerLoginHandler(new LoginHandler(userId, password));
		teamRepository.login(monitor);

		IContributor commentUser = null;
		if (null != commenterID) {
			try {
				commentUser = teamRepository.contributorManager()
						.fetchContributorByUserId(commenterID, monitor);
			} catch (ItemNotFoundException e) {
			}
		}
		if (commentUser == null) {
			commentUser = teamRepository.loggedInContributor();
		}

		// Use IWorkItemClient or IWorkItemCommon
		IWorkItemCommon workItemCommon = (IWorkItemCommon) teamRepository
				.getClientLibrary(IWorkItemCommon.class);
		int id = new Integer(idString).intValue();
		IWorkItem workItem = workItemCommon.findWorkItemById(id,
				IWorkItem.SMALL_PROFILE, monitor);

		if (null != workItem) {
			WorkItemAddComment operation = new WorkItemAddComment(commentUser,
					commentText);
			operation.run(workItem, monitor);
			System.out.println("Modified work item " + workItem.getId() + ".");
			result = true;

		} else {
			System.out.println("Can not find work Item " + idString + ".");
		}
		teamRepository.logout();
		return result;
	}
}
