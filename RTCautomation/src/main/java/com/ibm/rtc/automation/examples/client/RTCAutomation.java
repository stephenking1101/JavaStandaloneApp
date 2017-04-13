package com.ibm.rtc.automation.examples.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.common.TeamRepositoryException;

public class RTCAutomation {
    private static Log logger = LogFactory.getLog(RTCAutomation.class);

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        RTCAutomationArgs actionArgs = new RTCAutomationArgs();
        try {
            actionArgs.parseArguments(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            actionArgs.printHelp();
            System.exit(1);
        }

        TeamPlatform.startup();
        try {
            Configuration configuration = ConfigurationManager.getConfiguration();
            // logger.debug(configuration);
            IProgressMonitor monitor = new LoggerProgressMonitor();
            logger.debug("Logging in...");
            String url = actionArgs.getOptionValue("url");
            ITeamRepository repo = login(monitor, configuration, url);
            String automationAction = actionArgs.getOptionValue("action");
            logger.debug("Action: " + automationAction);
            if ("updateWIOwner".equalsIgnoreCase(automationAction)) {
                if ("true".equals(actionArgs.getOptionValue("updateChildren"))) {
                    List<Integer> workItemsList = RTCWorkItemUtil.getChildrenIds(repo, monitor, new Integer(actionArgs
                            .getOptionValue("workItemId")));
                    workItemsList.add(new Integer(actionArgs.getOptionValue("workItemId")));
                    RTCWorkItemUtil.changeWorkItemsOwner(repo, monitor, workItemsList, actionArgs
                            .getOptionValue("newOwnerUserId"));
                    System.out.print("done");
                } else {
                    RTCWorkItemUtil.changeWorkItemOwner(repo, monitor, new Integer(actionArgs
                            .getOptionValue("workItemId")), actionArgs.getOptionValue("newOwnerUserId"));
                }
            } else if ("replaceWIVariables".equalsIgnoreCase(automationAction)) {
                if ("true".equals(actionArgs.getOptionValue("updateChildren"))) {
                    String workItemId = actionArgs.getOptionValue("workItemId");
                    String replacePropsPath = actionArgs.getOptionValue("replaceProps");
                    logger.debug("workItemId: " + workItemId + ", replaceProps: " + replacePropsPath
                            + ", update children");
                    Map<String, String> replacementMap = loadPropertiesToMap(replacePropsPath);

                    List<Integer> workItemsList = RTCWorkItemUtil.getChildrenIds(repo, monitor, new Integer(actionArgs
                            .getOptionValue("workItemId")));
                    workItemsList.add(new Integer(actionArgs.getOptionValue("workItemId")));

                    RTCWorkItemUtil.replaceWorkItemsVariables(repo, monitor, workItemsList, replacementMap);
                } else {
                    String workItemId = actionArgs.getOptionValue("workItemId");
                    String replacePropsPath = actionArgs.getOptionValue("replaceProps");
                    logger.debug("workItemId: " + workItemId + ", replaceProps: " + replacePropsPath);
                    Map<String, String> replacementMap = loadPropertiesToMap(replacePropsPath);
                    RTCWorkItemUtil.replaceWorkItemVariables(repo, monitor, new Integer(workItemId), replacementMap);
                }
            } else if ("createWI".equalsIgnoreCase(automationAction)) {
            	Map<String, Object> propFiles = new HashMap<String, Object>(); 
                String workItemPropsPath = actionArgs.getOptionValue("workItemProps");
                String[] paths = workItemPropsPath.split(",");
                for(int i=0; i<paths.length; i++){
                	Map<String, String> propMap = loadPropertiesToMap(paths[i]);
                	logger.debug("workItemProps: " + paths[i]);
                	propFiles.put(new File(paths[i]).getName(), propMap);                	
                }
                RTCWorkItemUtil.createWorkItems(repo, monitor, propFiles, propFiles);
            } else if ("updateWorkItem".equalsIgnoreCase(automationAction)) {
            	String workItemId = actionArgs.getOptionValue("workItemId");
            	List<Integer> workItemIdList = new ArrayList<Integer>();
            	if(workItemId.equals("fromQuery")){
            		workItemIdList = RTCWorkItemUtil.runPersonalQuery(repo, monitor, 
    						actionArgs.getOptionValue("projectArea"),
    						actionArgs.getOptionValue("queryName"));
            	}else{
            		String[] ids = workItemId.split(",");
                    for(int i=0; i<ids.length; i++){
                    	workItemIdList.add(Integer.valueOf(ids[i]));
                    }
            	}
            	if(workItemIdList == null || workItemIdList.isEmpty()) return;
                String workItemPropsPath = actionArgs.getOptionValue("workItemProps");
                logger.debug("workItemProps: " + workItemPropsPath);
                Map<String, String> replacementMap = loadPropertiesToMap(workItemPropsPath);
                if ("true".equals(actionArgs.getOptionValue("updateChildren"))) {
                	RTCWorkItemUtil.updateWorkItems(repo, monitor, 
                    		workItemIdList, 
                    		replacementMap,
                    		true);
                } else {
                	RTCWorkItemUtil.updateWorkItems(repo, monitor, 
                    		workItemIdList, 
                    		replacementMap,
                    		false);
                }
                
            } else if ("listProjectAreas".equalsIgnoreCase(automationAction)) {
                logger.debug("listProjectAreas is triggered");
                logger.debug("It can also be extracted from jazz/process/project-areas");
                
                RTCProjectAreaUtil.listProjectAreas(repo, monitor);
            } else if ("listUsers".equalsIgnoreCase(automationAction)) {
                logger.debug("listUsers is triggered");
         
                RTCUserUtil.listUsers(repo);
            } else if ("listUsersEmail".equalsIgnoreCase(automationAction)) {
                logger.debug("listUsersEmail is triggered");
         
                RTCUserUtil.listUsersEmail(repo);
            } else if ("createProject".equalsIgnoreCase(automationAction)) {
                logger.debug("createProject is triggered");
         
                RTCProjectAreaUtil.createProject(repo, monitor,
                		actionArgs.getOptionValue("projectName"),
                		actionArgs.getOptionValue("processId"));
            } else if ("addMembersToPA".equalsIgnoreCase(automationAction)) {
                logger.debug("addMembersToPA is triggered");
                String userProps = actionArgs.getOptionValue("userProps");
                Map<String, String> userPropsMap = loadPropertiesToMap(userProps);
                userPropsMap.put("adminName", configuration.getProperty("userId",null,"LoginInfo"));
                userPropsMap.put("adminPassword", configuration.getProperty("password",null,"LoginInfo"));
                RTCUserUtil.importUsers(repo, monitor, userPropsMap);
                RTCProjectAreaUtil.addMembersToPA(repo, monitor, userPropsMap);
            } else if ("importUsers".equalsIgnoreCase(automationAction)) {
                logger.debug("importUsers is triggered");
                String userProps = actionArgs.getOptionValue("userProps");
                Map<String, String> userPropsMap = loadPropertiesToMap(userProps);
                userPropsMap.put("adminName", configuration.getProperty("userId",null,"LoginInfo"));
                userPropsMap.put("adminPassword", configuration.getProperty("password",null,"LoginInfo"));
                RTCUserUtil.importUsers(repo, monitor, userPropsMap);
            } else if ("createRTCUser".equalsIgnoreCase(automationAction)) {
                logger.debug("createRTCUser is triggered");
         
                RTCUserUtil.createRTCUser(repo,monitor, actionArgs.getOptionValue("userName")
                        , actionArgs.getOptionValue("emailAddress")
                        , actionArgs.getOptionValue("userId"));
            } else if ("changeWorkItemState".equalsIgnoreCase(automationAction)) {
            	String workItemId = actionArgs.getOptionValue("workItemId");
            	List<Integer> workItemIdList = new ArrayList<Integer>();
            	if(workItemId.equals("fromQuery")){
            		workItemIdList = RTCWorkItemUtil.runPersonalQuery(repo, monitor, 
    						actionArgs.getOptionValue("projectArea"),
    						actionArgs.getOptionValue("queryName"));
            	}else{
            		String[] ids = workItemId.split(",");
                    for(int i=0; i<ids.length; i++){
                    	workItemIdList.add(Integer.valueOf(ids[i]));
                    }
            	}
            	if(workItemIdList == null || workItemIdList.isEmpty()) return;
                if ("true".equals(actionArgs.getOptionValue("updateChildren"))) {
                    //List<Integer> workItemsList = RTCWorkItemUtil.getChildrenIds(repo, monitor, new Integer(actionArgs
                    //        .getOptionValue("workItemId")));
                    //workItemsList.add(new Integer(actionArgs.getOptionValue("workItemId")));
                    //RTCWorkItemUtil.changeWorkItemsState(repo, monitor, workItemsList
                   // 		, actionArgs.getOptionValue("workFlowAction")
                    //        , actionArgs.getOptionValue("targetState"));
                    //System.out.print("done");
                    RTCWorkItemUtil.changeWorkItemsState(repo, monitor, workItemIdList
                    , actionArgs.getOptionValue("workFlowAction")
                    , actionArgs.getOptionValue("targetState")
                    , true);
                } else {
                    RTCWorkItemUtil.changeWorkItemsState(repo, monitor, workItemIdList
                    		, actionArgs.getOptionValue("workFlowAction")
                            , actionArgs.getOptionValue("targetState")
                            , false);
                }
            } else if ("runSharedQuery".equalsIgnoreCase(automationAction)) {
				logger.debug("runSharedQuery action is triggered");
				RTCWorkItemUtil.runSharedQuery(repo, monitor, 
						actionArgs.getOptionValue("projectArea"),
						actionArgs.getOptionValue("queryName"));	
			} else if ("runPersonalQuery".equalsIgnoreCase(automationAction)) {
				logger.debug("runPersonalQuery action is triggered");
				RTCWorkItemUtil.runPersonalQuery(repo, monitor, 
						actionArgs.getOptionValue("projectArea"),
						actionArgs.getOptionValue("queryName"));	
			} else if ("removeBuildEnginePermissions".equalsIgnoreCase(automationAction)) {
				logger.debug("removeBuildEnginePermissions action is triggered");
				String projectArea = actionArgs.getOptionValue("projectArea");
				logger.debug("Project Area: " + projectArea);
				RTCProjectAreaUtil.removeBuildEnginePermissions(repo, monitor, projectArea);	
			} else if ("setCurrent9092Timeline".equalsIgnoreCase(automationAction)) {
                logger.debug("setCurrent9092Timeline is triggered");
                String projectArea = actionArgs.getOptionValue("projectArea");
				logger.debug("Project Area: " + projectArea);
                RTCProjectAreaUtil.setCurrent9092Timeline(repo,monitor, projectArea);
            } else if ("setCurrent9094Timeline".equalsIgnoreCase(automationAction)) {
                logger.debug("setCurrent9094Timeline is triggered");
                String projectArea = actionArgs.getOptionValue("projectArea");
				logger.debug("Project Area: " + projectArea);
                RTCProjectAreaUtil.setCurrent9094Timeline(repo,monitor, projectArea);
            } else if ("closeG3WorkItem".equalsIgnoreCase(automationAction)) {
                    List<Integer> workItemsList = RTCWorkItemUtil.getChildrenIds(repo, monitor, new Integer(actionArgs
                            .getOptionValue("workItemId")));
                    String workItemPropsPath = actionArgs.getOptionValue("workItemProps");
                    logger.debug("workItemProps: " + workItemPropsPath);
                    Map<String, String> replacementMap = loadPropertiesToMap(workItemPropsPath);
                    RTCWorkItemUtil.updateWorkItems(repo, monitor, 
                    		workItemsList, 
                    		replacementMap,
                    		false);
                    RTCWorkItemUtil.changeWorkItemsState(repo, monitor, workItemsList
                    		, actionArgs.getOptionValue("workFlowAction")
                            , actionArgs.getOptionValue("targetState")
                            , false);
                    System.out.print("done");
                    RTCWorkItemUtil.changeG3ParentState(repo, monitor, new Integer(actionArgs.getOptionValue("workItemId"))
            		, "Completed"
                    , "Completed"
                    , actionArgs.getOptionValue("userId"));
            } else if ("showWorkitemHistory".equalsIgnoreCase(automationAction)) {
				logger.debug("showWorkitemHistory action is triggered");
				RTCWorkItemUtil.showWorkItemHistory(repo, monitor, 
						actionArgs.getOptionValue("workItemId"));	
			} else if ("syncWorkitemAttr".equalsIgnoreCase(automationAction)) {
                logger.debug("syncWorkitemAttr is triggered");
                
                RTCWorkItemUtil.synchWorkItem(repo, monitor, actionArgs.getOptionValue("workItemId"));
            } else {
                logger.debug("Unsupported action");
            }

            repo.logout();
        } catch (TeamRepositoryException e) {
        	e.printStackTrace();
            logger.error("TeamRepositoryException: " + e.getMessage());
        } finally {
            TeamPlatform.shutdown();
        }
    }

    public static ITeamRepository login(IProgressMonitor monitor, Configuration configuration, String url)
            throws TeamRepositoryException {
        String rtcUrl = configuration.getProperty(url, null, "RTC");
        logger.debug("RTC URL: " + rtcUrl);
        ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository(rtcUrl);
        ILoginHandler iLoginHandler = new UserLoginHandler(configuration);
        repository.registerLoginHandler(iLoginHandler);
        monitor.subTask("Contacting " + repository.getRepositoryURI() + "...");
        repository.login(monitor);
        monitor.subTask("Connected");
        return repository;
    }

    public static Map<String, String> loadPropertiesToMap(String propsPath) throws FileNotFoundException, IOException {
        Properties replaceProps = new Properties();
        replaceProps.load(new FileInputStream((propsPath)));

        Map<String, String> resultMap = TokenReplacer.propertiesToMap(replaceProps);
        return resultMap;
    }

}
