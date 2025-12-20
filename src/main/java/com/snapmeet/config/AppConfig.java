package com.snapmeet.config;

import com.snapmeet.utils.StringTools;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Getter
    @Value("${ws.port}")
    private  String wsProt;

    @Value("${project.folder}")
    private String projectFolder;

    @Getter
    @Value("${admin.emails}")
    private String adminEmails;

    public String getProjectFolder(){
        if(!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")){
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }
}
