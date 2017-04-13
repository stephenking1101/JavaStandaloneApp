/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved. 
 *
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.rtc.automation.examples;

import java.net.URI;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IQueryClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.expression.AttributeExpression;
import com.ibm.team.workitem.common.expression.Expression;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.expression.QueryableAttributes;
import com.ibm.team.workitem.common.expression.Term;
import com.ibm.team.workitem.common.model.AttributeOperation;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;
import com.ibm.team.workitem.common.query.IResult;

/**
 * Example code, see
 * https://jazz.net/wiki/bin/view/Main/ProgrammaticWorkItemCreation.
 */
public class SynchronizeAttributes {

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
			}
			System.out.println("Synchronized: " + workItem.getId());
		}
	}

	public static void main(String[] args) {

		boolean result;
		if (!TeamPlatform.isStarted())
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
					.println("Usage: SynchronizeAttribute <repositoryURI> <userId> <password> <projectArea> <workitemTypeID>");
			return false;
		}

		String repositoryURI = args[0];
		String userId = args[1];
		String password = args[2];
		String projectAreaName = args[3];
		String workitemTypeID = args[4];

		IProgressMonitor monitor = new NullProgressMonitor();

		ITeamRepository teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURI);
		teamRepository.registerLoginHandler(new LoginHandler(userId, password));
		teamRepository.login(null);

		IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);

		URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
		IProjectArea projectArea = (IProjectArea) processClient
				.findProcessArea(uri, null, null);
		if (projectArea == null) {
			System.out.println("Project area not found.");
			return false;
		}

		IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
				.getClientLibrary(IWorkItemClient.class);

		IAuditableCommon auditableCommon = (IAuditableCommon) teamRepository
				.getClientLibrary(IAuditableCommon.class);

		IQueryableAttribute attribute = QueryableAttributes.getFactory(
				IWorkItem.ITEM_TYPE).findAttribute(projectArea,
				IWorkItem.PROJECT_AREA_PROPERTY, auditableCommon, monitor);
		IQueryableAttribute type = QueryableAttributes.getFactory(
				IWorkItem.ITEM_TYPE).findAttribute(projectArea,
				IWorkItem.TYPE_PROPERTY, auditableCommon, monitor);
		Expression inProjectArea = new AttributeExpression(attribute,
				AttributeOperation.EQUALS, projectArea);
		Expression isType = new AttributeExpression(type,
				AttributeOperation.EQUALS, workitemTypeID);
		Term typeinProjectArea = new Term(Term.Operator.AND);
		typeinProjectArea.add(inProjectArea);
		typeinProjectArea.add(isType);


		System.out.print(" run unresolved Expression: ");

		IQueryResult<IResult> expression_results = resultsUnresolvedByExpression(
				teamRepository, projectArea, typeinProjectArea);

		expression_results.setLimit(Integer.MAX_VALUE);

		SynchronizeWorkItemOperation synchronize = new SynchronizeWorkItemOperation(workItemClient);

		processUnresolvedResults(projectArea, expression_results, synchronize, monitor);

		teamRepository.logout();
		return true;
	}

	public static IQueryResult<IResolvedResult<IWorkItem>> resultsResolvedByExpression(
			ITeamRepository teamRepository, IProjectArea projectArea,
			Expression expression, ItemProfile<IWorkItem> profile)
			throws TeamRepositoryException {
		IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
				.getClientLibrary(IWorkItemClient.class);
		IQueryClient queryClient = workItemClient.getQueryClient();
		IQueryResult<IResolvedResult<IWorkItem>> results = queryClient
				.getResolvedExpressionResults(projectArea, expression, profile);
		return results;
	}

	public static IQueryResult<IResult> resultsUnresolvedByExpression(
			ITeamRepository teamRepository, IProjectArea projectArea,
			Expression expression) throws TeamRepositoryException {
		IWorkItemClient workItemClient = (IWorkItemClient) teamRepository
				.getClientLibrary(IWorkItemClient.class);
		IQueryClient queryClient = workItemClient.getQueryClient();
		IQueryResult<IResult> results = queryClient.getExpressionResults(
				projectArea, expression);
		return results;
	}

	public static long getTimeStamp() {
		Date time = new Date();
		return time.getTime();
	}


	/**
	 * @param projectArea
	 * @param results
	 * @param profile
	 * @param operation
	 * @param monitor
	 * @throws TeamRepositoryException
	 */
	public static void processUnresolvedResults(IProjectArea projectArea,
			IQueryResult<IResult> results, WorkItemOperation operation, IProgressMonitor monitor)
			throws TeamRepositoryException {
		long processed = 0;
		while (results.hasNext(monitor)) {
			IResult result = (IResult) results.next(monitor);
			// IWorkItem workItem = auditableCommon.resolveAuditable((IAuditableHandle) result.getItem(), profile, monitor);
			operation.run((IWorkItemHandle) result.getItem(), monitor);
			processed++;
		}
		System.out.println("Processed results: " + processed);
	}
}
