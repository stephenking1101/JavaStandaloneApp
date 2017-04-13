package com.ibm.rtc.automation.examples.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RTCAutomationArgs {
	protected Options options;
	protected CommandLine cmdline = null;
	
	public String getOptionValue(String optionName) {
		return cmdline.getOptionValue(optionName);
	}

	public RTCAutomationArgs() {
		options = new Options();
		options.addOption(getOption("help", "print this help page"));
		options.addOption(getOption("action", "RTC Automation Action", 1, "updateWIOwner|replaceWIVariables|createWI|listProjectAreas|listUsers|listUsersEmail|removeBuildEnginePermissions|addMembersToPA|importUsers|changeWorkItemState|showWorkitemHistory", true));
		options.addOption(getOption("url", "RTC Instance URL alias in config.xml", 1, "e.g. URL", true));
	}

	public void parseArguments(String[] args) throws ParseException {
		CommandLineParser parser = new ExtendedGnuParser(true);
		// First parse is to get action
		cmdline = parser.parse(options, args);
		String automationAction = cmdline.getOptionValue("action");
		if ("updateWIOwner".equalsIgnoreCase(automationAction)){
			options.addOption(getOption("workItemId", "Work Item ID", 1, "workItemId", true));
			options.addOption(getOption("newOwnerUserId", "New Owner user ID", 1, "newOwnerUserId", true));
			options.addOption(getOption("updateChildren", "Update Children", 1, "true|false", false));
		} else if ("replaceWIVariables".equalsIgnoreCase(automationAction)) {
			options.addOption(getOption("workItemId", "Work Item ID", 1, "workItemId", true));
			options.addOption(getOption("updateChildren", "Update Children", 1, "true|false", false));
			options.addOption(getOption("replaceProps", "Properties file containing key value pair", 1, "your_replace_work_items.properties", true));
		} else if ("createWI".equalsIgnoreCase(automationAction)) {
			options.addOption(getOption("workItemProps", "Properties file containing key value pair", 1, "new_work_items.properties", true));
		} else if ("updateWorkItem".equalsIgnoreCase(automationAction)) {
			options.addOption(getOption("workItemProps", "Properties file containing key value pair", 1, "work_items.properties", true));
			options.addOption(getOption("workItemId", "workItem Id", 1, "workItemId", true));
			options.addOption(getOption("updateChildren", "Update Children", 1, "true|false", false));
			cmdline = parser.parse(options, args);
			if(cmdline.getOptionValue("workItemId").equals("fromQuery")){
				options.addOption(getOption("projectArea", "Project Area name", 1, "projectArea", true));
	        	options.addOption(getOption("queryName", "Query name", 1, "queryName", true));
			}
		} else if ("listProjectAreas".equalsIgnoreCase(automationAction)) {
            System.out.println("No additonal parameters required");
        } else if ("removeBuildEnginePermissions".equalsIgnoreCase(automationAction)) {
            options.addOption(getOption("projectArea", "Project Area name", 1, "projectArea", true));
        } else if ("listUsers".equalsIgnoreCase(automationAction)) {
            System.out.println("No additonal parameters required");
        } else if ("listUsersEmail".equalsIgnoreCase(automationAction)) {
            System.out.println("No additonal parameters required");
        } else if ("createRTCUser".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("userName", "userName", 1, "userName", true));
        	options.addOption(getOption("emailAddress", "emailAddress", 1, "emailAddress", true));
        	options.addOption(getOption("userId", "userId", 1, "userId", true));
        } else if ("createProject".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("projectName", "Project Area Name", 1, "projectName", true));
        	options.addOption(getOption("processId", "Process ID", 1, "processId", true));
        } else if ("addMembersToPA".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("userProps", "userProps", 1, "userProps", true));
        } else if ("setCurrent9092Timeline".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("projectArea", "Project Area Name", 1, "projectName", true));
        } else if ("setCurrent9094Timeline".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("projectArea", "Project Area Name", 1, "projectName", true));
        } else if ("importUsers".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("userProps", "userProps", 1, "userProps", true));
        } else if ("runSharedQuery".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("projectArea", "Project Area name", 1, "projectArea", true));
        	options.addOption(getOption("queryName", "Query name", 1, "queryName", true));
        } else if ("runPersonalQuery".equalsIgnoreCase(automationAction)) {
        	options.addOption(getOption("projectArea", "Project Area name", 1, "projectArea", true));
        	options.addOption(getOption("queryName", "Query name", 1, "queryName", true));
        } else if ("changeWorkItemState".equalsIgnoreCase(automationAction)){
			options.addOption(getOption("workItemId", "Work Item ID", 1, "workItemId", true));
			options.addOption(getOption("workFlowAction", "Action to change the Work Item Status", 1, "action name - e.g. Close", true));
			options.addOption(getOption("targetState", "Target Status Name", 1, "Target Status Name - e.g. Closed", true));
			options.addOption(getOption("updateChildren", "Update Children", 1, "true|false", false));
			cmdline = parser.parse(options, args);
			if(cmdline.getOptionValue("workItemId").equals("fromQuery")){
				options.addOption(getOption("projectArea", "Project Area name", 1, "projectArea", true));
	        	options.addOption(getOption("queryName", "Query name", 1, "queryName", true));
			}
		} else if ("closeG3WorkItem".equalsIgnoreCase(automationAction)){
			options.addOption(getOption("workItemId", "Work Item ID", 1, "workItemId", true));
			options.addOption(getOption("workFlowAction", "Action to change the Work Item Status", 1, "action name - e.g. Close", true));
			options.addOption(getOption("targetState", "Target Status Name", 1, "Target Status Name - e.g. Closed", true));
			options.addOption(getOption("userId", "userId", 1, "userId", true));
			options.addOption(getOption("workItemProps", "Properties file containing key value pair", 1, "work_items.properties", true));
		} else if ("showWorkitemHistory".equalsIgnoreCase(automationAction)){
			options.addOption(getOption("workItemId", "Work Item ID", 1, "workItemId", true));
		} else if ("syncWorkitemAttr".equalsIgnoreCase(automationAction)){
			options.addOption(getOption("workItemId", "Work Item ID", 1, "workItemId", true));
		} else {
			System.out.println("action must be " + options.getOption("action").getArgName());
		}
		// Second parse is to get everything else
		cmdline = parser.parse(options, args);
	}

	protected Option getOption(String name, String usage) {
		Option option = new Option(name, usage);
		return option;
	}

	protected Option getOption(String name, String usage, int numArgs,
			String argName, boolean reqd) {
		Option option = new Option(name, usage);
		option.setArgs(numArgs);
		option.setArgName(argName);
		option.setRequired(reqd);
		return option;
	}


	public void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("arguments:", options);
	}
}
