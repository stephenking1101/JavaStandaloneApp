/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.rtc.automation.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.links.common.IItemReference;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContent;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IAttachment;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.WorkItemEndPoints;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class ModifyWorkItemUploadAttachmentOperation {

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

	private static class WorkItemUploadAttachmentModification extends
			WorkItemOperation {

		private String fFileName;
		private String fContentType;
		private String fEncoding;

		public WorkItemUploadAttachmentModification(String fileName,
				String contentType, String encoding) {
			super("Initializing Work Item", IWorkItem.FULL_PROFILE);
			fFileName = fileName;
			fContentType = contentType;
			fEncoding = encoding;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			try {
				attachFile(workingCopy, fFileName, fContentType, fEncoding,
						monitor);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private static void attachFile(WorkItemWorkingCopy workingCopy,
				String name, String contentType, String encoding,
				IProgressMonitor monitor) throws TeamRepositoryException,
				IOException {
			File attachmentFile = new File(name);
			FileInputStream fis = new FileInputStream(attachmentFile);
			IWorkItem workItem = workingCopy.getWorkItem();

			IWorkItemClient workItemClient = (IWorkItemClient) ((ITeamRepository) workItem
					.getOrigin()).getClientLibrary(IWorkItemClient.class);

			try {
				IAttachment newAttachment = workItemClient
						.createAttachment(workItem.getProjectArea(),
								attachmentFile.getName(), "Some Description",
								contentType, encoding, fis, monitor);

				newAttachment = (IAttachment) newAttachment.getWorkingCopy();

				newAttachment = workItemClient.saveAttachment(newAttachment,
						monitor);
				IItemReference reference = WorkItemLinkTypes
						.createAttachmentReference(newAttachment);

				workingCopy.getReferences().add(WorkItemEndPoints.ATTACHMENT,
						reference);
			} finally {
				if (fis != null) {
					fis.close();
				}
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

		if (args.length != 5) {
			System.out
					.println("Usage: ModifyWorkItemUploadAttachment [repositoryURI] [userId] [password] [workItemID] [FileName]");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String idString = args[3];
		String fileName = args[4];

		ITeamRepository teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURI);
		teamRepository.registerLoginHandler(new LoginHandler(userId, password));
		teamRepository.login(null);

		IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
				.getClientLibrary(IWorkItemClient.class);

		int id = new Integer(idString).intValue();
		IWorkItem workItem = workItemClient.findWorkItemById(id,
				IWorkItem.FULL_PROFILE, null);

		WorkItemUploadAttachmentModification operation = new WorkItemUploadAttachmentModification(
				fileName, IContent.CONTENT_TYPE_TEXT,
				IContent.ENCODING_UTF_8);
		operation.run(workItem, null);

//		WorkItemUploadAttachmentModification operation1 = new WorkItemUploadAttachmentModification(
//				fileName, IContent.CONTENT_TYPE_UNKNOWN,
//				IContent.ENCODING_UTF_8);
//		operation1.run(workItem, null);

		System.out.println("Modified work item " + workItem.getId() + ".");
		teamRepository.logout();

		return true;
	}
}
