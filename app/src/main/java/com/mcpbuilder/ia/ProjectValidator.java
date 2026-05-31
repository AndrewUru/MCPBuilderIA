package com.mcpbuilder.ia;

import java.util.Locale;

final class ProjectValidator {
    private ProjectValidator() {
    }

    static ValidationResult validate(ProjectInput input, boolean requireConnection) {
        ValidationResult result = new ValidationResult();
        String rawProjectName = input.projectName == null ? "" : input.projectName.trim();
        if (!rawProjectName.isEmpty() && ProjectNameSanitizer.clean(rawProjectName).length() < 3) {
            result.projectNameError = "Usa al menos 3 caracteres utiles.";
        }

        if (requireConnection) {
            if (!input.connector.equals("WhatsApp Business") && !isHttpUrl(input.siteUrl)) {
                result.siteUrlError = "Introduce una URL con http:// o https://.";
            }
            if (isBlank(input.username)) {
                result.usernameError = "Este campo es obligatorio para probar la conexion.";
            }
            if (isBlank(input.secret)) {
                result.secretError = "Este campo es obligatorio para probar la conexion.";
            }
        }

        return result;
    }

    private static boolean isHttpUrl(String value) {
        String lower = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static final class ValidationResult {
        String projectNameError;
        String siteUrlError;
        String usernameError;
        String secretError;

        boolean isValid() {
            return projectNameError == null
                    && siteUrlError == null
                    && usernameError == null
                    && secretError == null;
        }
    }
}
