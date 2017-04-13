package com.ibm.rtc.automation.examples.client;

/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2012.  
 * 
 * DevelopmentLineHelper
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.common.model.ItemProfile;

/**
 * Tries to find a development line and enclosed iteration for a project area.
 */
public class TimelineHelper {

	private ITeamRepository fTeamRepository;
	private IProgressMonitor fMonitor;
	private IIterationHandle listitehandle; 
	private IProjectArea fprojectArea;
	private IAuditableClient fAuditableClient;

	public TimelineHelper(ITeamRepository teamRepository,
			IProgressMonitor monitor, IProjectArea projectArea) {
		fTeamRepository = teamRepository;
		fMonitor = monitor;
		fprojectArea = projectArea;
		fAuditableClient = (IAuditableClient) fTeamRepository
				.getClientLibrary(IAuditableClient.class);
	}

	public IIterationHandle getIterationLiteral(String val)
			throws TeamRepositoryException {
		listitehandle = null;
		
		IDevelopmentLineHandle[] developmentHandles = fprojectArea
				.getDevelopmentLines();

		IDevelopmentLine line = null;
		if (developmentHandles != null) {
			List<?> developmentLines = fTeamRepository.itemManager()
					.fetchCompleteItems(Arrays.asList(developmentHandles),
							IItemManager.DEFAULT, null);

			for (Iterator<?> e = developmentLines.iterator(); e.hasNext();) {
				line = (IDevelopmentLine) e.next();
				IIterationHandle[] iterationHandles = line.getIterations();
				IterationRecursiveLiteral(iterationHandles, val);
			}

		}

		return listitehandle;
	}

	public void IterationRecursiveLiteral(IIterationHandle[] iterationHandles,
			String val) {

		//String interation_name = null;
		IIteration iteration = null;
		//IIteration iteration_r = null;
		//int i = 0;
		if (iterationHandles != null) {
			List<?> iterationlines = null;

			try {
				iterationlines = fTeamRepository.itemManager()
						.fetchCompleteItems(Arrays.asList(iterationHandles),
								IItemManager.DEFAULT, null);
			} catch (TeamRepositoryException e) {

				e.printStackTrace();
			}
			for (Iterator<?> e1 = iterationlines.iterator(); e1.hasNext();) {
				iteration = (IIteration) e1.next();
				//if (iteration != null)System.out.println("iteration name: " + iteration.getName());
				if (iteration != null && iteration.getName().equalsIgnoreCase(val)) {
					listitehandle = iteration;
					return;
				}
				if (iteration.getChildren() != null) {
					IterationRecursiveLiteral(iteration.getChildren(), val);
				}

			}
		}

	}
	
	/**
	 * Find an iteration based on the path provided.
	 * 
	 * @param iProjectAreaHandle
	 * @param path
	 * @param byId
	 * @return an iteration if one can be found or null otherwise
	 * 
	 * @throws TeamRepositoryException
	 */
	public IIteration findIteration(List<?> path, boolean byId) throws TeamRepositoryException {
		fAuditableClient = (IAuditableClient) fTeamRepository
				.getClientLibrary(IAuditableClient.class);
		IIteration foundIteration = null;
		IDevelopmentLine developmentLine = findDevelopmentLine(path, byId);
		if (developmentLine != null) {
			foundIteration = findIteration(developmentLine.getIterations(),
					path, 1, byId);
		}
		return foundIteration;
	}
	
	/**
	 * Find an Iteration
	 * 
	 * @param iterations
	 * @param path
	 * @param level
	 * @param byId
	 * @return
	 * @throws TeamRepositoryException
	 */
	private IIteration findIteration(IIterationHandle[] iterations,
			List<?> path, int level,  boolean byId)
			throws TeamRepositoryException {
		String lookFor = (String) path.get(level);
		for (IIterationHandle iIterationHandle : iterations) {

			IIteration iteration = fAuditableClient.resolveAuditable(
					iIterationHandle, ItemProfile.ITERATION_DEFAULT, fMonitor);
			String compare = "";
			if (byId) {
				compare = iteration.getId();
			} else {
				compare = iteration.getName();
			}
			if (lookFor.equals(compare)) {
				if (path.size() > level + 1) {
					IIteration found = findIteration(iteration.getChildren(),
							path, level + 1, byId);
					if (found != null) {
						return found;
					}
				} else {
					return iteration;
				}
			}
		}
		return null;
	}

	/**
	 * Find a development line based on the path provided.
	 * 
	 * @param projectArea
	 * @param path
	 * @param byId search by id or name
	 * @return a development line found or null.
	 * @throws TeamRepositoryException
	 */
	public IDevelopmentLine findDevelopmentLine(List<?> path, boolean byId) throws TeamRepositoryException {
		int level = 0;
		String lookFor = (String) path.get(level);
		IDevelopmentLineHandle[] developmentLineHandles = fprojectArea
				.getDevelopmentLines();

		for (IDevelopmentLineHandle developmentLineHandle : developmentLineHandles) {
			IDevelopmentLine developmentLine = fAuditableClient
					.resolveAuditable(developmentLineHandle,
							ItemProfile.DEVELOPMENT_LINE_DEFAULT, fMonitor);
			/*IIteration iteration = fAuditableClient.resolveAuditable(
					developmentLine, ItemProfile.ITERATION_DEFAULT, fMonitor);*/
			String compare = "";
			if (byId) {
				compare = developmentLine.getId();
			} else {
				compare = developmentLine.getName();
			}
			if (lookFor.equals(compare)) return developmentLine;
			/*
			if (lookFor.equals(compare)) {
				if (path.size() > level + 1) {
					IIteration found = findIteration(iteration.getChildren(),
						path, level + 1, byId);
					if (found != null) {
						return (IDevelopmentLine) found;
					}
				} else {
					return (IDevelopmentLine) iteration;
				}
			}*/
		}
		return null;
	}
	
	public IIterationHandle getIterationLiteralbyDevelopmentLine(String val, IDevelopmentLine line)
			throws TeamRepositoryException {
		listitehandle = null;
		
		IIterationHandle[] iterationHandles = line.getIterations();
		IterationRecursiveLiteral(iterationHandles, val);

		return listitehandle;
	}
}
