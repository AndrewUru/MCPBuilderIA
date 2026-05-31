package com.mcpbuilder.ia;

import android.content.SharedPreferences;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class ProjectStorage {
    private static final String PREFIX = "project:";

    private ProjectStorage() {
    }

    static void save(SharedPreferences preferences, ProjectInput input) throws Exception {
        JSONObject data = new JSONObject();
        data.put("name", input.projectName);
        data.put("connector", input.connector);
        data.put("intent", input.intent);
        data.put("siteUrl", input.siteUrl);
        data.put("username", input.username);
        data.put("secret", input.secret);
        data.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(new Date()));

        preferences.edit().putString(PREFIX + input.projectName, data.toString()).apply();
    }

    static ProjectInput load(SharedPreferences preferences, String projectName) throws Exception {
        String raw = preferences.getString(PREFIX + projectName, null);
        if (raw == null) return null;

        JSONObject data = new JSONObject(raw);
        return new ProjectInput(
                data.optString("name", projectName),
                data.optString("connector", "WooCommerce"),
                data.optString("intent", ""),
                data.optString("siteUrl", ""),
                data.optString("username", ""),
                data.optString("secret", "")
        );
    }

    static List<String> names(SharedPreferences preferences) {
        List<String> names = new ArrayList<>();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith(PREFIX)) {
                names.add(key.substring(PREFIX.length()));
            }
        }
        Collections.sort(names);
        return names;
    }
}
