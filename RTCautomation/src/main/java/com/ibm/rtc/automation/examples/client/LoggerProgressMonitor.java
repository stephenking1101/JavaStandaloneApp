package com.ibm.rtc.automation.examples.client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

public class LoggerProgressMonitor implements IProgressMonitor {
	private static Log logger = LogFactory.getLog(LoggerProgressMonitor.class);

    public void beginTask(String name, int totalWork) {
        print(name);
    }

    public void done() {
    }

    public void internalWorked(double work) {
    }

    public boolean isCanceled() {
        return false;
    }

    public void setCanceled(boolean value) {
    }

    public void setTaskName(String name) {
        print(name);
    }

    public void subTask(String name) {
        print(name);
    }

    public void worked(int work) {
    }
    
    private void print(String name) {
        if(name != null && ! "".equals(name))
        	logger.info(name);
    }
}
