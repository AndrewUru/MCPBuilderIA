package com.mcpbuilder.ia;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class ConnectionTester {
    private ConnectionTester() {
    }

    static String test(ProjectInput input) {
        try {
            Endpoint endpoint = endpointFor(input);
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint.url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            if (endpoint.bearer) {
                connection.setRequestProperty("Authorization", "Bearer " + endpoint.secret);
            } else {
                String auth = Base64.getEncoder().encodeToString((endpoint.user + ":" + endpoint.secret).getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", "Basic " + auth);
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
            String body = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            connection.disconnect();

            if (code >= 200 && code < 300) {
                return "Conexion correcta (" + code + "). Ya puedes generar y guardar este conector.";
            }
            return "La API respondio " + code + ": " + shorten(body);
        } catch (Exception error) {
            return "No se pudo conectar: " + error.getMessage();
        }
    }

    private static Endpoint endpointFor(ProjectInput input) {
        String baseUrl = trimSlash(input.siteUrl);
        if (input.connector.equals("WordPress")) {
            return new Endpoint(baseUrl + "/wp-json/wp/v2/users/me", input.username, input.secret);
        }
        if (input.connector.equals("WooCommerce")) {
            return new Endpoint(baseUrl + "/wp-json/wc/v3/products?per_page=1", input.username, input.secret);
        }
        if (input.connector.equals("WhatsApp Business")) {
            String version = valueOrFallback(input.siteUrl, "v25.0");
            if (!version.startsWith("v")) version = "v" + version;
            String url = "https://graph.facebook.com/" + version + "/" + input.username
                    + "?fields=display_phone_number,verified_name,quality_rating";
            return new Endpoint(url, "", input.secret, true);
        }
        return new Endpoint(baseUrl, input.username, input.secret);
    }

    private static String trimSlash(String value) {
        String clean = value == null ? "" : value.trim();
        while (clean.endsWith("/")) clean = clean.substring(0, clean.length() - 1);
        return clean;
    }

    private static String shorten(String value) {
        String clean = value == null ? "" : value.replace('\n', ' ').trim();
        return clean.length() > 180 ? clean.substring(0, 180) + "..." : clean;
    }

    private static String valueOrFallback(String value, String fallback) {
        String clean = value == null ? "" : value.trim();
        return clean.isEmpty() ? fallback : clean;
    }

    private static final class Endpoint {
        final String url;
        final String user;
        final String secret;
        final boolean bearer;

        Endpoint(String url, String user, String secret) {
            this(url, user, secret, false);
        }

        Endpoint(String url, String user, String secret, boolean bearer) {
            this.url = url;
            this.user = user;
            this.secret = secret;
            this.bearer = bearer;
        }
    }
}
