package com.twba.ddd.core;

import com.twba.kernel.fwk.security.SecurityProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class ApplicationProperties {

    private String uri;
    private String context;
    private String labels;
    private List<String> allowedOrigins;
    private List<String> whitelistedUrls;
    private Set<String> ignoredFirstAccessPages;
    private SecurityProperties security;
    private String templateFileName;
    private String serviceName;
    private Toggles toggles;
    private String newUserEmailTemplate;
    private String forgetPasswordTemplate;
    private boolean profiling;
    private Services services;
    private String defaultCertificateTemplateFile;
    private String protocol;

    public String protocol() {
        return (Objects.isNull(protocol) || protocol.isEmpty())?"http://":protocol;
    }

    @Getter
    @Setter
    public static class Toggles {
        private boolean sendRealEmails;
    }

    @Getter
    @Setter
    public static class Services {
        private String adminService;
        private String certificateService;
        private String collaborationService;
        private String courseExecutionService;
        private String courseRatingService;
        private String identityAccessService;
        private String messagingService;
        private String notificationService;
        private String studentService;
        private String teacherService;
    }
}
