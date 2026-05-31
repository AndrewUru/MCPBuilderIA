package com.mcpbuilder.ia;

final class ProjectInput {
    final String projectName;
    final String connector;
    final String intent;
    final String siteUrl;
    final String username;
    final String secret;

    ProjectInput(String projectName, String connector, String intent, String siteUrl, String username, String secret) {
        this.projectName = projectName;
        this.connector = connector;
        this.intent = intent;
        this.siteUrl = siteUrl;
        this.username = username;
        this.secret = secret;
    }
}
