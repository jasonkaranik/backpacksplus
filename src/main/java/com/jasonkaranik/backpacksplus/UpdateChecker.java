package com.jasonkaranik.backpacksplus;

import com.jasonkaranik.json.simple.extended.JSONObject;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;

import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateChecker {
    public static void check() {
        BackpacksPlus plugin = BackpacksPlus.getPlugin();
        new BukkitRunnable() {
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URI("https://nexus.jasonkaranik.com/service/rest/v1/search?repository=maven-releases&group=com.jasonkaranik&name=backpacksplus").toURL().openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    if (connection.getResponseCode() == 200) {
                        JSONObject response = new JSONObject(new String(connection.getInputStream().readAllBytes()));
                        if (response.containsKey("items")) {
                            JSONArray arr = (JSONArray) response.get("items");
                            if (arr.size() > 0) {
                                JSONObject item = new JSONObject(arr.getFirst().toString());
                                if (item.containsKey("version")) {
                                    String remoteVersion = (String) item.get("version");

                                    if (!remoteVersion.equals(plugin.getDescription().getVersion())) {
                                        plugin.getLogger().info("There's an available update!");
                                    } else {
                                        plugin.getLogger().info("You are running the latest update!");
                                    }
                                    return;
                                }
                            }
                        }
                    }

                    plugin.getLogger().warning("Could not check for updates");
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not check for updates (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                }
            }
        }.runTaskLater(plugin, 1L);
    }
}
