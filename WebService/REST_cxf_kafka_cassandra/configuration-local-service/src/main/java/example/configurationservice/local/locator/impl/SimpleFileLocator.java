package example.configurationservice.local.locator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;

/**
 *
 */
public class SimpleFileLocator implements FileLocator {

    private static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(SimpleFileLocator.class));

    private List<String> urls = new ArrayList<String>();

    public SimpleFileLocator(List<String> urls) {
        for (String url: urls){
            addNonBlankUrl(url);
        }
    }

    private void addNonBlankUrl(String url){
        if (StringUtils.isNotBlank(url)){
            urls.add(url);
        }
    }

    public SimpleFileLocator(String urls) {
        if (StringUtils.isNotBlank(urls)) {
            String[] urlArrary = urls.split(",");
            for (String url: urlArrary){
                addNonBlankUrl(url);
            }
        } else {
            throw new IllegalArgumentException("urls should not be null or empty : " + urls);
        }
    }

    @Override
    public List<String> locateFileUrl() {
        return this.urls;
    }

}
