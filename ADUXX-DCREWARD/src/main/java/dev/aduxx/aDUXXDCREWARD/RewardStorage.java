package dev.aduxx.aDUXXDCREWARD;

import dev.aduxx.aDUXXDCREWARD.DiscordRewardPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RewardStorage {

    private final DiscordRewardPlugin plugin;
    private final Set<String> rewardedPlayers = new HashSet<>();
    private final Set<String> eligiblePlayers = new HashSet<>();
    private final File storageFile;
    private final YamlConfiguration yaml;

    public RewardStorage(DiscordRewardPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "rewarded.yml");
        this.yaml = YamlConfiguration.loadConfiguration(storageFile);
    }

    public void load() {
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Nie można utworzyć rewarded.yml!");
                e.printStackTrace();
                return;
            }
        }

        rewardedPlayers.clear();
        eligiblePlayers.clear();

        rewardedPlayers.addAll(yaml.getStringList("rewarded"));
        eligiblePlayers.addAll(yaml.getStringList("eligible"));
    }

    public void save() {
        yaml.set("rewarded", rewardedPlayers.stream().toList());
        yaml.set("eligible", eligiblePlayers.stream().toList());
        try {
            yaml.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nie można zapisać rewarded.yml!");
            e.printStackTrace();
        }
    }

    public boolean isRewarded(String playerName) {
        return rewardedPlayers.contains(playerName.toLowerCase());
    }

    public void setRewarded(String playerName) {
        rewardedPlayers.add(playerName.toLowerCase());
        eligiblePlayers.remove(playerName.toLowerCase());
        save();
    }

    public boolean isEligible(String playerName) {
        return eligiblePlayers.contains(playerName.toLowerCase());
    }

    public void setEligible(String playerName) {
        eligiblePlayers.add(playerName.toLowerCase());
        save();
    }

    public void removeEligible(String playerName) {
        eligiblePlayers.remove(playerName.toLowerCase());
        save();
    }
}
