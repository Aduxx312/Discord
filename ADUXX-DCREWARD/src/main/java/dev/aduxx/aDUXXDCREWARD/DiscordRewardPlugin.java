package dev.aduxx.aDUXXDCREWARD;

import Discord.DiscordListener;
import commands.RewardCommand;
import listeners.GuiHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public class DiscordRewardPlugin extends JavaPlugin {

    private static DiscordRewardPlugin instance;

    private RewardStorage rewardStorage;
    private JDA jda;
    private GuiHandler guiHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        rewardStorage = new RewardStorage(this);
        rewardStorage.load();

        guiHandler = new GuiHandler(this);
        Bukkit.getPluginManager().registerEvents(guiHandler, this);

        getCommand("nagroda").setExecutor(new RewardCommand(this));

        String token = getConfig().getString("discord.Token");

        if (token == null || token.isEmpty()) {

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordListener(this))
                .setActivity(getBotActivity())
                .build();
    }

    @Override
    public void onDisable() {
        if (jda != null) {
            jda.shutdownNow();
        }

        rewardStorage.save();
    }

    private Activity getBotActivity() {
        String type = getConfig().getString("discord.activity-type", "PLAYING").toUpperCase();
        String text = getConfig().getString("discord.bot-activity", "Twojserwer.pl");

        try {
            return Activity.of(Activity.ActivityType.valueOf(type), text);
        } catch (IllegalArgumentException e) {
            return Activity.playing(text);
        }
    }

    public static DiscordRewardPlugin getInstance() {
        return instance;
    }

    public RewardStorage getRewardStorage() {
        return rewardStorage;
    }

    public GuiHandler getGuiHandler() {
        return guiHandler;
    }
}
