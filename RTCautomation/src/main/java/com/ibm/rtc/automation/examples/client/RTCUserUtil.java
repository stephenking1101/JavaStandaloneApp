package com.ibm.rtc.automation.examples.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.process.client.IClientProcess;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IRole;
import com.ibm.team.process.common.IRole2;
import com.ibm.team.repository.client.IContributorManager;
import com.ibm.team.repository.client.IExternalUserRegistryManager;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.util.IClientLibraryContext;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.IContributorLicenseType;
import com.ibm.team.repository.common.IExternalUser;
import com.ibm.team.repository.common.ILicenseAdminService;
import com.ibm.team.repository.common.TeamRepositoryException;

public class RTCUserUtil {
    private static Log logger = LogFactory.getLog(RTCUserUtil.class);

    public static void archiveUser(ITeamRepository repo, IProgressMonitor monitor, String userId)
            throws TeamRepositoryException {

        IContributor user = repo.contributorManager().fetchContributorByUserId(userId, monitor);
        IContributor archiveIDWorkingCopy = (IContributor) user.getWorkingCopy();

        archiveIDWorkingCopy.setArchived(true);
        repo.contributorManager().saveContributor(archiveIDWorkingCopy, monitor);

        System.out.println("Archived user: " + userId + "." + archiveIDWorkingCopy.isArchived());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void listUsers(ITeamRepository repo) throws TeamRepositoryException {
        IContributorManager icm = repo.contributorManager();
        List allContribs = icm.fetchAllContributors(null);
        //System.out.println(allContribs.size());
        ArrayList clone = new ArrayList();
        clone.addAll(allContribs);
        Collections.sort(clone, new UserComparator());
        
        int allContribSize = clone.size(); 
        for(int i = 0; i < allContribSize; i++){
            IContributor user = (IContributor)clone.get(i);
            
            System.out.println(user.getName());
        }
        System.out.println("There are " + allContribSize + " total users in the repository");
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void listUsersEmail(ITeamRepository repo) throws TeamRepositoryException {
        IContributorManager icm = repo.contributorManager();
        List allContribs = icm.fetchAllContributors(null);
        //System.out.println(allContribs.size());
        ArrayList clone = new ArrayList();
        clone.addAll(allContribs);
        Collections.sort(clone, new UserComparator());
        
        int allContribSize = clone.size(); 
        for(int i = 0; i < allContribSize; i++){
            IContributor user = (IContributor)clone.get(i);
            if (!user.isArchived()){
            	System.out.println(user.getUserId() + " ," + user.getEmailAddress());
            }
        }
        System.out.println("There are " + allContribSize + " total users in the repository");
    }
    
    public static IContributor createRTCUser(ITeamRepository repo,IProgressMonitor monitor,String userName,String 
    		emailAddress,String userId) throws TeamRepositoryException{ 
    		//Create Item Type Contributor and set its properties 
    		IContributor i1 = (IContributor) IContributor.ITEM_TYPE.createItem(); 
    		i1.setName(userName); 
    		i1.setUserId(userId); 
    		i1.setEmailAddress(emailAddress); 
    		i1.setArchived(false); 
    		return repo.contributorManager().saveContributor(i1, null); 
    }
    
    public static void importUsers(ITeamRepository repo, IProgressMonitor monitor, Map<String, String> replacementMap) 
    		throws TeamRepositoryException {
    	String userIds = replacementMap.get("userId");
        String licenseIds = replacementMap.get("licenseId");
//        String adminName = replacementMap.get("adminName");
//        String adminPassword = replacementMap.get("adminPassword");
//        String groupName = replacementMap.get("ldapGroupName");
//        String keystore = replacementMap.get("keystore");
        String[] userIdArray = userIds.split(",");
        String[] licenseIdArray = licenseIds.split(",");
        for(int i=0; i<userIdArray.length; i++){
        	String userId = userIdArray[i];
        	//LDAPUtil.addUser(adminName, adminPassword, userId, groupName, keystore);
        	importUserToRTC(repo, monitor, userId, licenseIdArray);
        }
    }
    
    public static boolean importUserToRTC(ITeamRepository repo, IProgressMonitor monitor, String userId, String[] licenseIdArray) throws 
    TeamRepositoryException{ 
    	IContributor cont=null;
    	try{
    		cont = repo.contributorManager().fetchContributorByUserId(userId, null) ;
    	} catch (TeamRepositoryException e){
            //logger.warn("TeamRepositoryException: " + e.getMessage());
    	}
    	
    	if (cont != null) {
    		logger.warn("User " + cont.getUserId() + " already exists."); 
    		try {
    			//System.out.println(cont.getUserId() + " " + cont.getName());
				// Add license for user 
				//IClientLibraryContext clientLibraryContext = (IClientLibraryContext) repo
				//		.getClientLibrary(IClientLibraryContext.class);
				ILicenseAdminService licenseService = (ILicenseAdminService) ((IClientLibraryContext)repo)
						.getServiceInterface(ILicenseAdminService.class);
				
				String[] currentLicenses = licenseService.getAssignedLicenses(cont);
				
				for(int i=0 ; i < currentLicenses.length; i++){
					logger.debug("license already granted before :" + currentLicenses[i]);
				}
				
				if(currentLicenses.length == 0) {
					for(int j=0; j<licenseIdArray.length; j++){
						String licenseId = licenseIdArray[j];
						assignUserLicense(repo, cont, licenseId);
					}
				}
			} catch (Exception licenseException) {
				logger.error(licenseException.getMessage());
				return false;
			}
    		return false;
    	}
    	
    	cont = (IContributor) IContributor.ITEM_TYPE.createItem();

	    //Get the ExternalUser Registry and retrieve the required user 
	    IExternalUserRegistryManager externalUserRegMgr = repo.externalUserRegistryManager(); 
	    //IExternalUser exUser = externalUserRegMgr.fetchUser("test1@in.ibm.com", null); 
	    IExternalUser exUser = externalUserRegMgr.fetchUser(userId, monitor);
	    if (exUser!=null) { 
	        //Once User is found in ExternalUser Registry import it 
	        cont = (IContributor) cont.getWorkingCopy(); 
	        cont.setEmailAddress(exUser.getEmailAddresses().get(0)); 
	        cont.setName(exUser.getFullNames().get(0)); 
	        cont.setUserId(exUser.getUserId()); 
	        cont.setArchived(false); 
	        logger.info("Import User - Email: " + exUser.getEmailAddresses().get(0) + " Name: " + exUser.getFullNames().get(0) + " ID: " + exUser.getUserId()); 
	        cont = repo.contributorManager().saveContributor(cont, monitor); 
			
	        try {
    			//System.out.println(cont.getUserId() + " " + cont.getName());
				// Add license for user 
				//IClientLibraryContext clientLibraryContext = (IClientLibraryContext) repo
				//		.getClientLibrary(IClientLibraryContext.class);
				ILicenseAdminService licenseService = (ILicenseAdminService) ((IClientLibraryContext)repo)
						.getServiceInterface(ILicenseAdminService.class);
				
				String[] currentLicenses = licenseService.getAssignedLicenses(cont);
				
				for(int i=0 ; i < currentLicenses.length; i++){
					logger.debug("license already granted before :" + currentLicenses[i]);
				}
				
				if(currentLicenses.length == 0) {
					for(int j=0; j<licenseIdArray.length; j++){
						String licenseId = licenseIdArray[j];
						assignUserLicense(repo, cont, licenseId);
					}
				}
			} catch (Exception licenseException) {
				logger.error(licenseException.getMessage());
				return false;
			}
	        
	        return true; 
	    } 
	    else{ 
	    	logger.error("User " + userId + " doesn't exist in ExternalUser Registry"); 
	        return false;
	    } 
    }
    
    private static void assignUserLicense(ITeamRepository repo,IContributor user, String licenseId) throws 
    TeamRepositoryException{ 
    	ILicenseAdminService licenseService = (ILicenseAdminService) ((IClientLibraryContext)repo)
				.getServiceInterface(ILicenseAdminService.class);
    	IContributorLicenseType license_types[] = licenseService
				.getLicenseTypes();
		
		// Assign new user as a developer, test box has no floating
		// licenses
		for (IContributorLicenseType license_type : license_types) {
			String id = license_type.getId();
			//logger.debug(license_type.getId() + " " + license_type.getName());
			if (id.equals(licenseId)) {
				licenseService.assignLicense(user, id);
				logger.debug("license granted:" + license_type.getId() + " " + license_type.getName());
			}
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
    public static void listProjectAreaMembers(ITeamRepository teamRepository,
    		IProcessArea processArea) throws TeamRepositoryException {
    	System.out.println("Process Area: " + processArea.getName());
    	System.out.println("Administrators");
    	dumpContributors(teamRepository, processArea, processArea.getAdministrators());
    	System.out.println("Team Members");
    	dumpContributors(teamRepository, processArea, processArea.getMembers());
    }
    
    private static void dumpContributors(ITeamRepository teamRepository,
    		IProcessArea processArea, IContributorHandle[] contributors)
    		throws TeamRepositoryException {

    	for (int i = 0; i < contributors.length; i++) {
    		IContributorHandle handle = (IContributorHandle) contributors[i];
    		dumpContributor(teamRepository, processArea, handle);
    	}
    }
    
    private static void dumpContributor(ITeamRepository teamRepository,
    		IProcessArea processArea, IContributorHandle handle)
    		throws TeamRepositoryException {
    	IContributor contributor = (IContributor) teamRepository.itemManager()
    		.fetchCompleteItem(handle, IItemManager.DEFAULT, null);
    	System.out.print(": " + contributor.getUserId() + "\t"
    		+ contributor.getName() + "\t" + contributor.getEmailAddress()
    		+ "\t");
    	IProcessItemService processService = (IProcessItemService) teamRepository
    		.getClientLibrary(IProcessItemService.class);
    	IClientProcess process = processService.getClientProcess(processArea, null);
    	IRole[] contributorRoles = process.getContributorRoles(contributor, processArea, null);
    	for (int j = 0; j < contributorRoles.length; j++) {
    		IRole role = (IRole) contributorRoles[j];
    		System.out.print(role.getId() + " ");
    	}
    	//processArea.addMember(contributorHandle) 
    	//processArea.setRoleAssignments 
    	for (int j = 0; j < contributorRoles.length; j++) {
    		IRole role = (IRole) contributorRoles[j];
    		IRole2 role2 = (IRole2) role;	
    		System.out.print(role.getId() + "[" + role2.getRoleName() +" " + role2.getRoleLabel() + "] ");
    	}
    	System.out.println();
    }

}

class UserComparator implements Comparator<Object>{

    public int compare(Object arg0, Object arg1) {

        return ((IContributor)arg0).getName().compareTo(((IContributor)arg1).getName());
    }
}
