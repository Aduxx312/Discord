package dev.aduxx.aDUXXDCREWARD;

import chatutil.ChatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class RewardManager implements Listener {

    private static final Set<String> claimed = new HashSet<>();

    public void handleReward(MessageReceivedEvent event, String playerName) {
        String playerNameLower = playerName.toLowerCase();

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String ping = event.getAuthor().getAsMention();


        if (!offlinePlayer.hasPlayedBefore()) {
            sendEmbed(event, "player-not-found", playerName, ping);
            return;
        }


        if (!offlinePlayer.isOnline()) {
            sendEmbed(event, "player-not-online", playerName, ping);
            return;
        }


        if (claimed.contains(playerNameLower)) {
            sendEmbed(event, "reward-picked", playerName, ping);
            return;
        }


        String cmd = DiscordRewardPlugin.getInstance().getConfig().getString("reward-command")
                .replace("[PLAYER]", playerName);

        Bukkit.getScheduler().runTask(DiscordRewardPlugin.getInstance(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));

        claimed.add(playerNameLower);
        sendEmbed(event, "reward-succes", playerName, ping);

        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) {
            String msg = DiscordRewardPlugin.getInstance().getConfig().getString("messages.picked-info")
                    .replace("[PLAYER]", playerName);
            p.sendMessage(ChatUtil.color(msg));
        }
    }

    private void sendEmbed(MessageReceivedEvent event, String section, String player, String ping) {
        var cfg = DiscordRewardPlugin.getInstance().getConfig();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(cfg.getString(section + ".title"));
        embed.setDescription(ChatUtil.replaceDiscordPlaceholders(
                cfg.getString(section + ".description"), player, ping));
        embed.setColor(Color.decode(cfg.getString(section + ".embed-color")));
        embed.setThumbnail(cfg.getString(section + ".embed-thumbnail-url"));

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
