package com.mcpbuilder.ia;

final class PreviewRenderer {
    private PreviewRenderer() {
    }

    static String render(String section, String projectName, String connector, GeneratedProject project) {
        StringBuilder preview = new StringBuilder();
        preview.append("Proyecto: ").append(projectName).append("\n");
        preview.append("Conector: ").append(connector).append("\n");
        preview.append("Vista: ").append(section).append("\n\n");

        if (section.equals("Tools")) {
            preview.append("Tools MCP generadas\n");
            for (String tool : project.tools) preview.append("  - ").append(tool).append("\n");
            preview.append("\nArchivos incluidos\n");
            for (String path : project.files.keySet()) preview.append("  ").append(path).append("\n");
        } else if (section.equals("Variables")) {
            preview.append(project.files.get(".env.example"));
        } else if (section.equals("Codigo")) {
            preview.append("src/server.js\n\n").append(project.files.get("src/server.js"));
            preview.append("\n\nsrc/client.js\n\n").append(project.files.get("src/client.js"));
        } else if (section.equals("Docs")) {
            preview.append(project.files.get("docs/README.md"));
        } else if (section.equals("Prompts")) {
            preview.append(project.files.get("prompts/usage.md"));
        }

        return preview.toString();
    }
}
