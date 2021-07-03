package com.hoaxify.ws.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final AppConfiguration appConfiguration;

    @Autowired
    public WebConfiguration(final AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:./" + appConfiguration.getUploadPath() + "/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

    }

    @Bean
    CommandLineRunner createStorageDirectories() {
        return (args -> {
            createFolder(appConfiguration.getUploadPath());
            createFolder(appConfiguration.getProfileStoragePath());
            createFolder(appConfiguration.getAttachmentStoragePath());
        });
    }

    private void createFolder(final String path) {
        File folder = new File(path);
        boolean folderExist = folder.exists() && folder.isDirectory();
        if (!folderExist) {
            folder.mkdir();
        }
    }

}
