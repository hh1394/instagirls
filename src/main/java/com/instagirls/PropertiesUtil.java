package com.instagirls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    public static final String POSTED_FILE_URL = "posted_file";
    public static final String GIRLS_FILE_URL = "girls_file";
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

    private PropertiesUtil() {
    }

    public static Properties loadPropertiesFile(final String filename) {
        final InputStream resourceAsStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(filename);
        final Properties prop = new Properties();
        try {
            prop.load(resourceAsStream);
        } catch (IOException e) {
            LOGGER.info("Could not load " + filename);
        }
        return prop;
    }

}
