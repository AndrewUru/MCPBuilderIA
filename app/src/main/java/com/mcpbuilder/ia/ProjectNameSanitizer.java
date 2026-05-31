package com.mcpbuilder.ia;

import java.util.Locale;

final class ProjectNameSanitizer {
    private ProjectNameSanitizer() {
    }

    static String clean(String value) {
        String cleaned = value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]+", "-")
                .replaceAll("-+", "-");
        if (cleaned.startsWith("-")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("-")) cleaned = cleaned.substring(0, cleaned.length() - 1);
        return cleaned.isEmpty() ? "mcp-business-agent" : cleaned;
    }
}
