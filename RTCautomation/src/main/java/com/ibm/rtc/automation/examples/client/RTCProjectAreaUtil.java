package com.ibm.rtc.automation.examples.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.team.process.client.IClientProcess;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.client.workingcopies.IProjectAreaWorkingCopy;
import com.ibm.team.process.client.workingcopies.IWorkingCopyManager;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProcessDefinition;
import com.ibm.team.process.common.IProcessItem;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IRole;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.common.model.ItemProfile;

public class RTCProjectAreaUtil {
    private static Log logger = LogFactory.getLog(RTCProjectAreaUtil.class);
    
    public static void listProjectAreas(ITeamRepository repo, IProgressMonitor monitor) throws TeamRepositoryException  {
        IProcessItemService client = (IProcessItemService) repo.getClientLibrary(IProcessItemService.class);

        List<?> projectAreas = client.findAllProjectAreas(IProcessClientService.ALL_PROPERTIES, monitor);

        Collections.sort(projectAreas, new PANameComparator());
        
        int projectAreasSize = projectAreas.size(); 
        for(int i = 0; i < projectAreasSize; i++){
            IProjectArea projectArea = (IProjectArea)projectAreas.get(i);
            
            System.out.println(projectArea.getName());
        }
    }
    
    public static IProjectArea getProjectArea(ITeamRepository repo, IProgressMonitor monitor, String projectAreaName) throws TeamRepositoryException  {
    	IProcessClientService processClient = (IProcessClientService) repo
    			.getClientLibrary(IProcessClientService.class);

    		URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
    		IProjectArea projectArea = (IProjectArea) processClient.findProcessArea(uri, null, null);
    		if (projectArea == null) {
    			System.out.println("Project area not found.");
    		}
    		return projectArea;
    }
    
    public static void addMembersToPA(ITeamRepository repo, IProgressMonitor monitor, Map<String, String> replacementMap) 
    		throws TeamRepositoryException {
    	String projectArea = replacementMap.get("projectArea");
    	String userIds = replacementMap.get("userId");
        
    	if (userIds == null) {
    		logger.error("Error: invalid userIds");
    		return;
    	}
        String[] userIdArray = userIds.split(",");
        for(int i=0; i<userIdArray.length; i++){
        	String userId = userIdArray[i];
        	String roleIds = replacementMap.get(userId);
        	String[] roleIdArray = roleIds.split(",");
        	addMemberToPA(repo, monitor, projectArea, userId, roleIdArray);
        }
    }
    /**
	 * Iterate over the contributors of the process area and print them sorted
	 * as admins and as team members
	 * 
	 * @param teamRepository
	 * @param processArea
	 * @throws TeamRepositoryException
	 */
	public static void addMemberToPA(ITeamRepository teamRepository,
			IProgressMonitor monitor, String projectAreaName, String userId, String[] roleIds)
			throws TeamRepositoryException {

		IProcessItemService ipis = (IProcessItemService) teamRepository
				.getClientLibrary(IProcessItemService.class);
		IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);

		URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
		IProcessArea processArea = (IProcessArea) processClient
				.findProcessArea(uri, null, monitor);

		if (processArea == null) {
			logger.error("Error: invalid project area [" + projectAreaName
					+ "]");
			throw new RuntimeException();
		}
		logger.info("Process Area: " + processArea.getName());

		//System.out.println("Team Members");
		//IContributorHandle[] members = processArea.getMembers();
		/*for (int i = 0; i < members.length; i++) {
			IContributorHandle handle = (IContributorHandle) members[i];
			IContributor contributor = (IContributor) teamRepository
					.itemManager().fetchCompleteItem(handle,
							IItemManager.DEFAULT, null);
			System.out.println(": " + contributor.getUserId() + "\t"
					+ contributor.getName() + "\t"
					+ contributor.getEmailAddress() + "\t");
			IProcessItemService processService = (IProcessItemService) teamRepository
					.getClientLibrary(IProcessItemService.class);
			IClientProcess process = processService.getClientProcess(
					processArea, null);
			IRole[] contributorRoles = process.getContributorRoles(contributor,
					processArea, null);
			for (int j = 0; j < contributorRoles.length; j++) {
				IRole role = (IRole) contributorRoles[j];
				System.out.println(role.getId() + " ");
			}
		}*/
		IProcessItemService processService = (IProcessItemService) teamRepository
		.getClientLibrary(IProcessItemService.class);
		IClientProcess process = processService.getClientProcess(
				processArea, monitor);
		
		if (roleIds==null) roleIds = new String[] {""};
		IRole[] contributorRoles = process.getRoles(processArea, monitor);
		IRole[] rolesToAssign = new IRole[roleIds.length];
		for (int j = 0; j < contributorRoles.length; j++) {
			IRole role = (IRole) contributorRoles[j];
			for(int k=0; k<roleIds.length; k++){
				if(roleIds[k].equalsIgnoreCase(role.getId())){
					rolesToAssign[k] = role;
					logger.debug("Role Assigned: " + role.getId() + " ");
				}
			}
		}
		
		for(int i=0; i<roleIds.length; i++){
			if(rolesToAssign[i] == null){
				logger.error("Role id: " +roleIds[i] + " not found");
				return;
			}
		}
		
		IProjectArea area = (IProjectArea) getProjectArea(teamRepository,monitor,projectAreaName);
		IContributor user = teamRepository.contributorManager().fetchContributorByUserId(userId, monitor);
		if (user == null) {
    		logger.error("User " + userId + " does not exist in repository. Please import it first."); 
    		return;
    	}
		logger.info("User Added: " + user.getName() + " ");
		
		area = (IProjectArea) ipis.getMutableCopy(area);
		//area.addAdministrator(user);
		area.addMember(user);
		area.addRoleAssignments(user, rolesToAssign);

		ipis.save(new IProcessItem[] { area },
				monitor);

		//IContributor contributorWorkingCopy = (IContributor) user.getWorkingCopy();
		//processArea.addMember(contributorWorkingCopy);
		//processArea.setRoleAssignments(contributorWorkingCopy, contributorRoles);
	}
	
	public static Boolean createProject(ITeamRepository repo,
			IProgressMonitor monitor, String name, String processId) throws TeamRepositoryException{ 
        String msg = "Created project " + name + " with process id Name " + processId;             

        IProcessItemService service = (IProcessItemService)repo.getClientLibrary(IProcessItemService.class);
        
        List<?> areas = service.findAllProjectAreas(
    			IProcessClientService.ALL_PROPERTIES, monitor);
    	for (Object anArea : areas) {
    		if (anArea instanceof IProjectArea) {
    			IProjectArea foundArea = (IProjectArea) anArea;
    			if (foundArea.getName().equals(name)) {
    				logger.error("Project Area exists: " + name);
    				return false;
    			}
    		}
    	}
    	
        IProcessDefinition definition = service.findProcessDefinition(processId,IProcessItemService.ALL_PROPERTIES, monitor);          
        if (definition == null) { 
            logger.error("Could not find Predefined Process " + processId);
            return false;
        }

        IProjectArea project = service.createProjectArea(); 
        project.setName(name); 
        project.setProcessDefinition(definition);         
        service.save(project, monitor); 
        service.initialize(project, monitor); 
        logger.info(msg);
        return true; 
    } 
	
	public static void setCurrent9092Timeline(ITeamRepository teamRepository,
			IProgressMonitor monitor, String projectAreaName)
			throws TeamRepositoryException {

		IProcessItemService ipis = (IProcessItemService) teamRepository
				.getClientLibrary(IProcessItemService.class);
		IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);

		URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
		IProcessArea processArea = (IProcessArea) processClient
				.findProcessArea(uri, null, monitor);

		if (processArea == null) {
			logger.error("Error: invalid project area [" + projectAreaName
					+ "]");
			throw new RuntimeException();
		}
		logger.info("Process Area: " + processArea.getName());
		IAuditableClient auditableClient = (IAuditableClient) teamRepository.getClientLibrary(IAuditableClient.class);
		
		IProjectArea area = (IProjectArea) getProjectArea(teamRepository,monitor,projectAreaName);

		IDevelopmentLineHandle[] developmentLineHandles = area
				.getDevelopmentLines();

		IDevelopmentLine developmentLine = null;
		IDevelopmentLine developmentLineCopy = null;
		for (IDevelopmentLineHandle developmentLineHandle : developmentLineHandles) {
			developmentLine = auditableClient
					.resolveAuditable(developmentLineHandle,
							ItemProfile.DEVELOPMENT_LINE_DEFAULT, monitor);
			
			logger.info("DevelopmentLine Found - " + developmentLine.getName() + " : " + developmentLine.getStartDate());
			
			if (new TimelineHelper(teamRepository, monitor, area).
			getIterationLiteralbyDevelopmentLine(new Integer(Calendar.getInstance().get(Calendar.YEAR)).toString(), developmentLine) != null){
				developmentLineCopy = (IDevelopmentLine) developmentLine.getWorkingCopy();
				break;
			}
		}
		
		IIterationHandle iIterationHandle = new TimelineHelper(teamRepository, monitor, area)
        .getIterationLiteralbyDevelopmentLine(new Integer(Calendar.getInstance().get(Calendar.YEAR)).toString(), developmentLineCopy);
		
		IIteration currentItera = null;
		if (iIterationHandle != null){
			IIteration iteration = auditableClient.resolveAuditable(
					iIterationHandle, ItemProfile.ITERATION_DEFAULT, monitor);
			logger.info("Iteration Found - " + iteration.getName() + " : " + iteration.getStartDate());
			IIterationHandle[] childIterations = iteration.getChildren();
			for (IIterationHandle child : childIterations){
				IIteration itera = auditableClient.resolveAuditable(
							 child, ItemProfile.ITERATION_DEFAULT, monitor);
				
				if ( itera.getStartDate() != null && itera.getEndDate() != null
						&& Calendar.getInstance().getTime().compareTo(itera.getStartDate()) >= 0 
						&& Calendar.getInstance().getTime().compareTo(itera.getEndDate()) <=0 ){
					currentItera = itera;
					logger.info("Iteration Found - " + currentItera.getName() + " : " + currentItera.getStartDate());
					break;
				}
			}
		} else {
			logger.warn("No iteration found");
		}
		
		if (currentItera != null){
			IIterationHandle[] currentChildIterations = currentItera.getChildren();
			for (IIterationHandle child : currentChildIterations){
				IIteration itera = auditableClient.resolveAuditable(
							 child, ItemProfile.ITERATION_DEFAULT, monitor);

				if ( itera.getStartDate() != null && itera.getEndDate() != null
						&& Calendar.getInstance().getTime().compareTo(itera.getStartDate()) >= 0 
						&& Calendar.getInstance().getTime().compareTo(itera.getEndDate()) <=0 
						&& developmentLineCopy != null ){
					logger.info("Iteration Found - " + itera.getName() + " : " + itera.getStartDate());
					IIteration iteraWorkingCopy = (IIteration) itera.getWorkingCopy();
					developmentLineCopy.setCurrentIteration(iteraWorkingCopy);
					ipis.save(new IProcessItem[] { developmentLineCopy },
							monitor);
					logger.info("Set CurrentIteration To - " + itera.getName() + " : " + itera.getStartDate());
					break;
				}
			}
		}
	}
	
	public static void setCurrent9094Timeline(ITeamRepository teamRepository,
			IProgressMonitor monitor, String projectAreaName)
			throws TeamRepositoryException {

		IProcessItemService ipis = (IProcessItemService) teamRepository
				.getClientLibrary(IProcessItemService.class);
		IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);

		URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
		IProcessArea processArea = (IProcessArea) processClient
				.findProcessArea(uri, null, monitor);

		if (processArea == null) {
			logger.error("Error: invalid project area [" + projectAreaName
					+ "]");
			throw new RuntimeException();
		}
		logger.info("Process Area: " + processArea.getName());
		IAuditableClient auditableClient = (IAuditableClient) teamRepository.getClientLibrary(IAuditableClient.class);
		
		IProjectArea area = (IProjectArea) getProjectArea(teamRepository,monitor,projectAreaName);

		IDevelopmentLineHandle[] developmentLineHandles = area
				.getDevelopmentLines();

		IDevelopmentLine developmentLine = null;
		IDevelopmentLine developmentLineCopy = null;
		for (IDevelopmentLineHandle developmentLineHandle : developmentLineHandles) {
			developmentLine = auditableClient
					.resolveAuditable(developmentLineHandle,
							ItemProfile.DEVELOPMENT_LINE_DEFAULT, monitor);
			
			logger.info("DevelopmentLine Found - " + developmentLine.getName() + " : " + developmentLine.getStartDate());
			
			if (new TimelineHelper(teamRepository, monitor, area).
			getIterationLiteralbyDevelopmentLine("Project", developmentLine) != null){
				developmentLineCopy = (IDevelopmentLine) developmentLine.getWorkingCopy();
				break;
			}
		}
		
		IIterationHandle iIterationHandle = new TimelineHelper(teamRepository, monitor, area)
        .getIterationLiteralbyDevelopmentLine("Project", developmentLineCopy);
		
		IIteration currentItera = null;
		if (iIterationHandle != null){
			IIteration iteration = auditableClient.resolveAuditable(
					iIterationHandle, ItemProfile.ITERATION_DEFAULT, monitor);
			logger.info("Iteration Found - " + iteration.getName() + " : " + iteration.getStartDate());
			IIterationHandle[] childIterations = iteration.getChildren();
			for (IIterationHandle child : childIterations){
				IIteration itera = auditableClient.resolveAuditable(
							 child, ItemProfile.ITERATION_DEFAULT, monitor);
				
				if ( itera.getStartDate() != null && itera.getEndDate() != null
						&& Calendar.getInstance().getTime().compareTo(itera.getStartDate()) >= 0 
						&& Calendar.getInstance().getTime().compareTo(itera.getEndDate()) <=0 ){
					currentItera = itera;
					logger.info("Iteration Found - " + currentItera.getName() + " : " + currentItera.getStartDate());
					break;
				}
			}
		} else {
			logger.warn("No iteration found");
		}
		
		if (currentItera != null){
			IIterationHandle[] currentChildIterations = currentItera.getChildren();
			for (IIterationHandle child : currentChildIterations){
				IIteration itera = auditableClient.resolveAuditable(
							 child, ItemProfile.ITERATION_DEFAULT, monitor);

				if ( itera.getStartDate() != null && itera.getEndDate() != null
						&& Calendar.getInstance().getTime().compareTo(itera.getStartDate()) >= 0 
						&& Calendar.getInstance().getTime().compareTo(itera.getEndDate()) <=0 
						&& developmentLineCopy != null ){
					logger.info("Iteration Found - " + itera.getName() + " : " + itera.getStartDate());
					IIteration iteraWorkingCopy = (IIteration) itera.getWorkingCopy();
					developmentLineCopy.setCurrentIteration(iteraWorkingCopy);
					ipis.save(new IProcessItem[] { developmentLineCopy },
							monitor);
					logger.info("Set CurrentIteration To - " + itera.getName() + " : " + itera.getStartDate());
					break;
				}
			}
		}
	}
	
	public static void removeBuildEnginePermissions(ITeamRepository repo,
			IProgressMonitor monitor, String projectArea)
			throws TeamRepositoryException, IOException,
			ParserConfigurationException, SAXException,
			TransformerFactoryConfigurationError, TransformerException {

		IProcessItemService ipis = (IProcessItemService) repo
				.getClientLibrary(IProcessItemService.class);
		IProcessClientService processClient = (IProcessClientService) repo
				.getClientLibrary(IProcessClientService.class);

		URI uri = URI.create(projectArea.replaceAll(" ", "%20"));
		IProcessArea processArea = (IProcessArea) processClient
				.findProcessArea(uri, null, null);

		if (processArea == null) {
			logger.error("Error: invalid project area [" + projectArea + "]");
			throw new RuntimeException();
		}

		IWorkingCopyManager workingCopyMgr = ipis.getWorkingCopyManager();
		IProjectAreaWorkingCopy projectAreaCopy = (IProjectAreaWorkingCopy) workingCopyMgr
				.createPrivateWorkingCopy(processArea);
		IDocument doc = projectAreaCopy.getProcessSpecification();
		String configStr = doc.get();

		String rootDirectory;
		if (System.getProperty("os.name").startsWith("Win")) {
			rootDirectory = "c:/processconfigsrc";
		} else {
			rootDirectory = "/var/tmp/processconfigsrc";
		}
		new File(rootDirectory).mkdir();
		String saveFile = saveCurrentConfig(projectArea, configStr,
				rootDirectory);
		// logger.debug(configStr);

		String newXmlfile = removeBuildEnginePermissionsFromXML(saveFile);

		File f = new File(newXmlfile);
		String newConfigStr = FileUtils.readFileToString(f, null);
		doc.set(newConfigStr);
		projectAreaCopy.save(monitor);

	}

	public static String removeBuildEnginePermissionsFromXML(
			String projectAreaConfigSourceFile) throws IOException,
			ParserConfigurationException, SAXException,
			TransformerFactoryConfigurationError, TransformerException {
		File f = new File(projectAreaConfigSourceFile);
		if (!f.exists()) {
			logger.debug("projectAreaConfigSourceFile: "
					+ projectAreaConfigSourceFile + " does not exist");
			throw new IOException();
		}

		Document rtcProcessDocument = DOMUtil
				.parse(projectAreaConfigSourceFile);

		Node root = rtcProcessDocument.getDocumentElement();

		Node teamConfigurationNode = DOMUtil.getChildrenNodeByName(root,
				"team-configuration");
		Node permissionsNode = DOMUtil.getChildrenNodeByName(
				teamConfigurationNode, "permissions");
		loopPermissionNode(permissionsNode);

		String newFile = projectAreaConfigSourceFile.replaceFirst(".xml",
				".new.xml");
		DOMUtil.writeXmlToFile(newFile, rtcProcessDocument);
		return newFile;
	}
	
	// support methods
	private static String getTimestamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		System.out.println();
		return dateFormat.format(date);
	}

	private static Node loopPermissionNode(Node permissionsNode) {
		NodeList permissionsList = permissionsNode.getChildNodes();
		for (int i = 0; i < permissionsList.getLength(); i++) {
			Node pNode = (Node) permissionsList.item(i);
			if (pNode != null && "role".equals(pNode.getNodeName())) {

				// String roleId = pNode.getAttributes().getNamedItem("id")
				// .getTextContent();
				// logger.debug("This is role: " + roleId);
				loopRoleNode(pNode);
			}

		}
		return permissionsNode;
	}

	private static Node loopRoleNode(Node roleNode) {
		NodeList operationList = roleNode.getChildNodes();
		for (int i = 0; i < operationList.getLength(); i++) {
			Node pNode = (Node) operationList.item(i);
			if (pNode != null && "operation".equals(pNode.getNodeName())) {

				String operationId = pNode.getAttributes().getNamedItem("id")
						.getTextContent();
				if ("com.ibm.team.build.server.deleteBuildEngine"
						.equalsIgnoreCase(operationId)) {
					logger.debug("Removing com.ibm.team.build.server.deleteBuildEngine");
					roleNode.removeChild(pNode);
				}
				if ("com.ibm.team.build.server.saveBuildEngine"
						.equalsIgnoreCase(operationId)) {
					logger.debug("Removing com.ibm.team.build.server.saveBuildEngine");
					roleNode.removeChild(pNode);
				}
			}

		}
		return roleNode;
	}
	
	private static String saveCurrentConfig(String projectArea,
			String configStr, String processConfigsRootdir) throws IOException {
		projectArea = projectArea.replaceAll(" ", "_");
		String projectAreaDir = processConfigsRootdir + "/" + projectArea;
		String outputFile = projectAreaDir + "/" + projectArea + "."
				+ getTimestamp() + ".xml";

		// this code seems to be not working yet
		String pattern = ".*encoding=\"UTF-8\".*";
		Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
		boolean isUTF8 = p.matcher(configStr).find();

		new File(projectAreaDir).mkdir();
		Writer w = null;
		if (isUTF8) {
			logger.debug("This is a UTF-8 file");
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					outputFile), "UTF-8"));
		} else {
			logger.debug("This is a standard ANSI file");
			w = new FileWriter(outputFile);
		}
		try {
			w.write(configStr);
		} finally {
			w.close();
		}

		logger.info("Process config saved to: " + outputFile);
		return outputFile;
	}
}

class PANameComparator implements Comparator<Object>{

    public int compare(Object arg0, Object arg1) {

        return ((IProjectArea)arg0).getName().compareTo(((IProjectArea)arg1).getName());
    }
}

