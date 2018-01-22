package example.configurationservice.local.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;


/**
 *
 */
public class MonitoredResource {

    protected static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(MonitoredResource.class));

    // TODO [FEATURE] support heterogeneous formats and suffixes.
    private static final String DEFAULT_CONFIG_FILE_FILTER_NAME = "*.*";

    //private static final java.lang.String DEFAULT_CONFIG_FILE_FILTER_NAME = "*.yml";
    //private static final java.lang.String DEFAULT_CONFIG_FILE_FILTER_NAME = "*.properties";
    //private static final java.lang.String DEFAULT_CONFIG_FILE_FILTER_NAME = "*";

    private boolean isDirectory;

    private String url;

    private File fileObject;

    private String fileName;

    private String absolutePath;

    private File folderFileObject;

    private String absolutePathOfFolder;

    private IOFileFilter ioFileFilter;

    public IOFileFilter getIoFileFilter() {
        return ioFileFilter;
    }

    public void setIoFileFilter(IOFileFilter ioFileFilter) {
        this.ioFileFilter = ioFileFilter;
    }

    public String getAbsolutePathOfFolder() {
        return absolutePathOfFolder;
    }

    public void setAbsolutePathOfFolder(String absolutePathOfFolder) {
        this.absolutePathOfFolder = absolutePathOfFolder;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFileObject() {
        return fileObject;
    }

    public void setFileObject(File fileObject) {
        this.fileObject = fileObject;
    }

    public File getFolderFileObject() {
        return folderFileObject;
    }

    public void setFolderFileObject(File folderFileObject) {
        this.folderFileObject = folderFileObject;
    }

    /**
     * Construct the file filter for apache-commons-io monitor.
     */
    public void constuctIOFileFilter() {
        IOFileFilter fileter;
        if (!isDirectory) {
            fileter = new WildcardFileFilter(fileName);//FileFilterUtils.nameFileFilter();
        } else {
            fileter = new WildcardFileFilter(DEFAULT_CONFIG_FILE_FILTER_NAME);
        }
        ioFileFilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), fileter);
    }

    public List<File> getConfigFiles() {
        List<File> allConfigFiles = getConfigFilesInFolder();
        return allConfigFiles;
    }

    private List<File> getConfigFilesInFolder() {
        File configFolder = getFolderFileObject();
        FileFilter filesFilter = getIoFileFilter();

        List<File> allConfigFiles = new ArrayList<File>();
        List<File> folderList = new ArrayList<File>();

        folderList.add(configFolder);
        File[] filteredFileList;
        File currentFolder;

        // TODO improve
        while (!folderList.isEmpty()) {
            currentFolder = folderList.remove(0);
            filteredFileList = currentFolder.listFiles(filesFilter);
            if (filteredFileList == null) {
                logger.debug("no files found in folder:{}", currentFolder);
                continue;
            }
            if (logger.isDebugEnabled()) {
                File[] fileListWithoutFilter = currentFolder.listFiles();
                int countWithoutFilter = fileListWithoutFilter != null ? fileListWithoutFilter.length : 0;
                logger.debug(filteredFileList.length + " files found in folder with filter:" + currentFolder
                        + " Total count without filter:" + countWithoutFilter);

            }
            for (File configFile : filteredFileList) {
                if (configFile.isDirectory()) {
                    folderList.add(configFile);
                } else if (configFile.isFile()) {
                    allConfigFiles.add(configFile);
                }
            }
        }
        return allConfigFiles;
    }

    @Override
    public String toString() {
        return "MonitoredResource{" + " absolutePath='" + absolutePath + '\'' + '}';
    }

    public String toStringWithFilter() {
        return "MonitoredResource{" + " Folder AbsolutePath='" + getAbsolutePathOfFolder() + '\'' + " filter='"
                + ioFileFilter + '\'' + '}';
    }
}
