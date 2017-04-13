### How to use this tool?
1. Unzip the rtcautomationassembly.zip
2. Update your user id and password in ../conf/config.xml
3. Modify the properties file in ../conf/
4. Run the following commands

### Example: 
## To import users to RTC, assign license, add team members and grant roles via command line
rtcautomation.bat -action addMembersToPA -url 9092 -userProps ../conf/add_user_to_PA.properties

## To import users to RTC and assign license via command line
rtcautomation.bat -action importUsers -url 9092 -userProps ../conf/import_user.properties

## To create workitem via command line
rtcautomation.bat -action createWI -url 9092 -workItemProps ../conf/create_WI.properties
rtcautomation.bat -action createWI -url 9092 -workItemProps "../conf/create_WI - parent.properties,../conf/create_WI - children.properties"

## To update workitem via command line
rtcautomation.bat -action updateWorkItem -url 9092 -workItemId 366510,366511 -workItemProps ../conf/update_WI.properties
rtcautomation.bat -action updateWorkItem -url 9092 -workItemId fromQuery -workItemProps ../conf/update_WI.properties -projectArea "_Stephen_Test_PA" -queryName "New Workitems"

## To change workitem status via command line
rtcautomation.bat -action changeWorkItemState -url 9092 -workItemId 366510 -workFlowAction Complete -targetState Completed -updateChildren true
rtcautomation.bat -action changeWorkItemState -url 9092 -workItemId fromQuery -workFlowAction "Start Working"-targetState "In Progress" -updateChildren false -projectArea "_Stephen_Test_PA" -queryName "New Workitems"

## To run a shared query via command line
rtcautomation.bat -action runSharedQuery -url 9092 -projectArea "HSBC Global RTC Deployment" -queryName "AP G3 Projects"

## To list users in repository via command line
rtcautomation.bat -action listUsers -url 9092 

## To list project areas in repository via command line
rtcautomation.bat -action listProjectAreas -url 9092 

## To update work item owner via command line
rtcautomation.bat -action updateWIOwner -url 9092 -workItemId 366510 -newOwnerUserId 43334612
rtcautomation.bat -action updateWIOwner -url 9092 -workItemId 366510 -newOwnerUserId 43334612 -updateChildren true

## To replace variable in work item summary
rtcautomation.bat -action replaceWIVariables -url 9092 -workItemId 373689 -replaceProps ../conf/replace_WI.properties
rtcautomation.bat -action replaceWIVariables -url 9092 -workItemId 373689 -replaceProps ../conf/replace_WI.properties -updateChildren true 

