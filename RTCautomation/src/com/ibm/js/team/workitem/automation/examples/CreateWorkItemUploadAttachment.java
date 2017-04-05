/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.js.team.workitem.automation.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.common.IItemReference;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.IAttachment;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.model.WorkItemEndPoints;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class CreateWorkItemUploadAttachment {

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

	private static class WorkItemInitialization extends WorkItemOperation {

		private String fSummary;
		private ICategoryHandle fCategory;
		private ITeamRepository fTeamRepository;
		private IProjectArea fProjectArea;
		private String fFileName;

		public WorkItemInitialization(String summary, ICategoryHandle category,
				ITeamRepository teamRepository, IProjectArea projectArea,
				String fileName) {
			super("Initializing Work Item");
			fSummary = summary;
			fCategory = category;
			fTeamRepository = teamRepository;
			fProjectArea = projectArea;
			fFileName = fileName;
		}

		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();

			workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));

			workItem.setCategory(fCategory);

			IWorkItemClient workItemClient = (IWorkItemClient) fTeamRepository
					.getClientLibrary(IWorkItemClient.class);

			IAttribute priority = workItemClient.findAttribute(fProjectArea,
					"com.ibm.team.workitem.attribute.priority", null);

			if (null != priority) {
				workItem.setValue(
						priority,
						getLiteralEqualsString("Medium", workItemClient,
								priority));
			}

			try {
				attachFile(fFileName, workingCopy, fProjectArea, workItemClient);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private static Identifier<? extends ILiteral> getLiteralStartsWithString(
			String name, IWorkItemCommon workItemCommon, IAttributeHandle ia)
			throws TeamRepositoryException {
		Identifier<? extends ILiteral> literalID = null;
		IEnumeration<? extends ILiteral> enumeration = workItemCommon
				.resolveEnumeration(ia, null);

		List<? extends ILiteral> literals = enumeration
				.getEnumerationLiterals();
		for (Iterator<? extends ILiteral> iterator = literals.iterator(); iterator
				.hasNext();) {
			ILiteral iLiteral = (ILiteral) iterator.next();
			if (iLiteral.getName().startsWith(name)) {
				literalID = iLiteral.getIdentifier2();
				break;
			}
		}
		return literalID;
	}

	private static Identifier<? extends ILiteral> getLiteralEqualsString(
			String name, IWorkItemCommon workItemCommon, IAttributeHandle ia)
			throws TeamRepositoryException {
		Identifier<? extends ILiteral> literalID = null;
		IEnumeration<? extends ILiteral> enumeration = workItemCommon
				.resolveEnumeration(ia, null);

		List<? extends ILiteral> literals = enumeration
				.getEnumerationLiterals();
		for (Iterator<? extends ILiteral> iterator = literals.iterator(); iterator
				.hasNext();) {
			ILiteral iLiteral = (ILiteral) iterator.next();
			if (iLiteral.getName().equals(name)) {
				literalID = iLiteral.getIdentifier2();
				break;
			}
		}
		return literalID;
	}

	private static void attachFile(String name, WorkItemWorkingCopy workItem,
			IProjectArea fProjectArea, IWorkItemClient workItemClient)
			throws TeamRepositoryException, IOException {
		File attachmentFile = new File(name);
		FileInputStream fis = new FileInputStream(attachmentFile);
		try {
			IAttachment newAttachment = workItemClient.createAttachment(
					fProjectArea, attachmentFile.getName(), "", "text",
					"UTF-8", fis, null);

			newAttachment = (IAttachment) newAttachment.getWorkingCopy();
			// Using an internal interface to set the owner e.g. in synchronizer tools?
			// ((com.ibm.team.workitem.common.internal.model.Attachment)newAttachment).setCreator(value);
			newAttachment = workItemClient.saveAttachment(newAttachment, null);
			IItemReference reference = WorkItemLinkTypes
					.createAttachmentReference(newAttachment);
			workItem.getReferences().add(WorkItemEndPoints.ATTACHMENT,
					reference);
		} finally {
			if (fis != null) {
				fis.close();
				// if (attachmentFile != null) {
				// attachmentFile.delete();
				// }
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

	/**
	 * @param args
	 * @return
	 * @throws TeamRepositoryException
	 */
	private static boolean run(String[] args) throws TeamRepositoryException {

		if (args.length != 8) {
			System.out
					.println("Usage: CreateWorkItem [repositoryURI] [userId] [password] [projectArea] [workItemType] [summary] [category] [fileName");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String projectAreaName = args[3];
		String typeIdentifier = args[4];
		String summary = args[5];
		String categoryName = args[6];
		String fileName = args[7];

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

		WorkItemInitialization operation = new WorkItemInitialization(summary,
				category, teamRepository, projectArea, fileName);
		IWorkItemHandle handle = operation.run(workItemType, null);
		IWorkItem workItem = auditableClient.resolveAuditable(handle,
				IWorkItem.FULL_PROFILE, null);

		System.out.println("Created work item " + workItem.getId() + ".");
		teamRepository.logout();

		return true;
	}
}
