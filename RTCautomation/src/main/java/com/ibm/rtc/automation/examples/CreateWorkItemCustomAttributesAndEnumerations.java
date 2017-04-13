/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.rtc.automation.examples;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class CreateWorkItemCustomAttributesAndEnumerations {

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
		private String fCustomStringAttributeID;
		private String fCustomString;
		private String fCustomIntegerAttributeID;
		private Integer fCustomInteger;
		private String fCustomContributorUserIDAttributeID;
		private String fCustomContributorUserID;
		private String fPriorityEnumerationString;
		private boolean fCreateAttributesIfMissing;

		public WorkItemInitialization(String summary, ICategoryHandle category,
				ITeamRepository teamRepository, IProjectArea projectArea,
				String priorityString, String customStringAttributeID,
				String customString, String customIntegerAttributeID,
				Integer customInteger, String customUserIDAttributeID,
				String customUserID, Boolean createAttributes) {
			super("Initializing Work Item");
			fSummary = summary;
			fCategory = category;
			fTeamRepository = teamRepository;
			fProjectArea = projectArea;
			fCustomStringAttributeID = customStringAttributeID;
			fCustomString = customString;
			fCustomIntegerAttributeID = customIntegerAttributeID;
			fCustomInteger = customInteger;
			fCustomContributorUserIDAttributeID = customUserIDAttributeID;
			fCustomContributorUserID = customUserID;
			fPriorityEnumerationString = priorityString;
			fCreateAttributesIfMissing = createAttributes;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.ibm.team.workitem.client.WorkItemOperation#execute(com.ibm.team
		 * .workitem.client.WorkItemWorkingCopy,
		 * org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected void execute(WorkItemWorkingCopy workingCopy,
				IProgressMonitor monitor) throws TeamRepositoryException {
			IWorkItem workItem = workingCopy.getWorkItem();
			workItem.setHTMLSummary(XMLString.createFromPlainText(fSummary));
			workItem.setCategory(fCategory);

			IWorkItemClient workItemClient = (IWorkItemClient) fTeamRepository
					.getClientLibrary(IWorkItemClient.class);

			IAttribute customString = workItemClient.findAttribute(
					fProjectArea, fCustomStringAttributeID, monitor);
			// IAttribute built_in = workItemClient.findAttribute(
			// fProjectArea, IWorkItem.DURATION_PROPERTY, monitor);

			if (null == customString && fCreateAttributesIfMissing) {
				customString = workItemClient.createNewAttribute(fProjectArea,
						fCustomStringAttributeID, AttributeTypes.SMALL_STRING,
						IAttribute.FULL_TEXT_KIND_DEFAULT, monitor);
				customString.setDisplayName("Created String ("
						+ fCustomStringAttributeID + ")");
				customString = workItemClient.saveAttribute(customString,
						monitor);
			}

			if (null != customString) {
				if (!workItem.hasCustomAttribute(customString)
						&& fCreateAttributesIfMissing) {
					// If not defined in the process template it has to be
					// manually created
					workItem.addCustomAttribute(customString);
				}
				if (workItem.hasCustomAttribute(customString))
					workItem.setValue(customString, fCustomString);
			}

			IAttribute customInteger = workItemClient.findAttribute(
					fProjectArea, fCustomIntegerAttributeID, monitor);

			if (null == customInteger && fCreateAttributesIfMissing) {
				customInteger = workItemClient.createNewAttribute(fProjectArea,
						fCustomIntegerAttributeID, AttributeTypes.INTEGER,
						IAttribute.FULL_TEXT_KIND_DEFAULT, monitor);
				customInteger.setDisplayName("Created Integer ("
						+ fCustomIntegerAttributeID + ")");
				customInteger = workItemClient.saveAttribute(customInteger,
						monitor);
			}

			if (null != customInteger) {
				if (!workItem.hasCustomAttribute(customInteger)
						&& fCreateAttributesIfMissing) {
					// If not defined in the process template it has to be
					// manually created
					workItem.addCustomAttribute(customInteger);
				}
				if (workItem.hasCustomAttribute(customInteger))
					workItem.setValue(customInteger, fCustomInteger);
			}

			IAttribute customContributor = workItemClient.findAttribute(
					fProjectArea, fCustomContributorUserIDAttributeID, monitor);

			if (null == customContributor && fCreateAttributesIfMissing) {
				customContributor = workItemClient.createNewAttribute(
						fProjectArea, fCustomContributorUserIDAttributeID,
						AttributeTypes.CONTRIBUTOR,
						IAttribute.FULL_TEXT_KIND_DEFAULT, monitor);
				customContributor.setDisplayName("Created Contributor ("
						+ fCustomContributorUserIDAttributeID + ")");
				customContributor = workItemClient.saveAttribute(
						customContributor, monitor);
			}

			if (null != customContributor) {
				if (!workItem.hasCustomAttribute(customContributor)
						&& fCreateAttributesIfMissing) {
					// If not defined in the process template it has to be
					// manually created
					workItem.addCustomAttribute(customContributor);
				}
				IContributor aUser = fTeamRepository.contributorManager()
						.fetchContributorByUserId(fCustomContributorUserID,
								null);
				if (workItem.hasCustomAttribute(customContributor))
					workItem.setValue(customContributor, aUser);
			}

			// Please note: In 3.0.1 unable to find the external ID:
			// com.ibm.team.workitem.attribute.priority
			IAttribute priority = workItemClient.findAttribute(fProjectArea,
					"internalPriority", null);

			if (null != priority && workItem.hasAttribute(priority)) {
				workItem.setValue(
						priority,
						getLiteralEqualsString(fPriorityEnumerationString,
								workItemClient, priority));
				Object prio = workItem.getValue(priority);
				if (prio instanceof Identifier<?>) {
					Identifier<?> identifier = (Identifier<?>) prio;
					System.out.print("\tIdentifier: "
							+ identifier.getStringIdentifier());
				}
			}

			System.out.println("Attributes: ");
			List<IAttributeHandle> built_in_attribs = workItemClient
					.findBuiltInAttributes(fProjectArea, monitor);
			for (Iterator<IAttributeHandle> iterator = built_in_attribs
					.iterator(); iterator.hasNext();) {
				IAttributeHandle iAttributeHandle = (IAttributeHandle) iterator
						.next();
				IAttribute iAttribute = (IAttribute) fTeamRepository
						.itemManager().fetchCompleteItem(iAttributeHandle,
								IItemManager.DEFAULT, null);
				System.out.print("built in: " + iAttribute.getIdentifier()
						+ " \t" + iAttribute.getDisplayName() + " \t"
						+ iAttribute.getAttributeType());
				Object value = workItem.getValue(iAttribute);
				if (value != null && value instanceof String) {
					String sValue = (String) value;
					System.out.print("\tValue: " + sValue);
				}
				System.out.println();
			}

			// List Custom Attributes
			List<IAttributeHandle> custAttribs = workItem.getCustomAttributes();
			for (Iterator<IAttributeHandle> iterator = custAttribs.iterator(); iterator
					.hasNext();) {
				IAttributeHandle iAttributeHandle = (IAttributeHandle) iterator
						.next();
				IAttribute iAttribute = (IAttribute) fTeamRepository
						.itemManager().fetchCompleteItem(iAttributeHandle,
								IItemManager.DEFAULT, null);
				System.out.print("Custom: " + iAttribute.getIdentifier()
						+ " \t" + iAttribute.getDisplayName() + " \t"
						+ iAttribute.getAttributeType());
				Object value = workItem.getValue(iAttribute);
				if (value != null && value instanceof String) {
					String sValue = (String) value;
					System.out.print("\tValue: " + sValue);
				}
				System.out.println();
			}
		}

		@SuppressWarnings("unused")
		// Used if no exact match possible
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

		if (args.length != 15) {
			System.out
					.println("Usage: CreateWorkItemCustomAttributesAndEnumerations [repositoryURI] [userId] [password] [projectArea] [workItemType] [summary] [category] [priorityString] [customStringAttributeID][customString> <customIntegerAttributeID] [customInteger] [customContributorAttributeID] [customUserID] [createCustomAttributes]");
			System.out
					.println("[priorityString] [customString] [customInteger] [customUserID]");
			System.out.println(" [priorityString] for example 'Medium' ");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String projectAreaName = args[3];
		String typeIdentifier = args[4];
		String summary = args[5];
		String categoryName = args[6];
		String priorityString = args[7];
		String customStringAttributeID = args[8];
		String customString = args[9];
		String customIntegerAttributeID = args[10];
		String customInteger = args[11];
		String customContributorAttributeID = args[12];
		String customUserID = args[13];
		String createCustomAttributesIfUnavailable = args[14];

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
				category, teamRepository, projectArea, priorityString,
				customStringAttributeID, customString,
				customIntegerAttributeID, new Integer(customInteger),
				customContributorAttributeID, customUserID, new Boolean(
						createCustomAttributesIfUnavailable));
		IWorkItemHandle handle = operation.run(workItemType, null);

		IWorkItem workItem = auditableClient.resolveAuditable(handle,
				IWorkItem.FULL_PROFILE, null);

		System.out.println("Created work item " + workItem.getId() + ".");
		teamRepository.logout();

		return true;
	}
}
