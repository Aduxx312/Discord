package Discord;

import dev.aduxx.aDUXXDCREWARD.DiscordRewardPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.Color;

public class DiscordListener extends ListenerAdapter {

    private final DiscordRewardPlugin plugin;

    public DiscordListener(DiscordRewardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String channelId = plugin.getConfig().getString("discord.channel-id");
        if (channelId == null || !event.getChannel().getId().equals(channelId)) return;

        String playerName = event.getMessage().getContentRaw().trim();

        if (playerName.isEmpty()) return;

        String discordMention = event.getAuthor().getAsMention();

        if (playerName.length() < 3 || playerName.length() > 16) {
            sendEmbed(event, "player-not-found", playerName, discordMention);
            return;
        }

        Player player = plugin.getServer().getPlayerExact(playerName);
        if (player == null) {
            sendEmbed(event, "player-not-online", playerName, discordMention);
            return;
        }

        if (plugin.getRewardStorage().isRewarded(playerName)) {
            sendEmbed(event, "reward-picked", playerName, discordMention);
            return;
        }


        if (!plugin.getRewardStorage().isEligible(playerName)) {
            plugin.getRewardStorage().setEligible(playerName);
        }


        String infoMsg = plugin.getConfig().getString("info", "&aMasz do odebrania nagrodę za połączenie konta z discordem!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', infoMsg));


        sendEmbed(event, "reward-success", playerName, discordMention);
    }

    private void sendEmbed(MessageReceivedEvent event, String configPath, String playerName, String discordMention) {
        var cfg = plugin.getConfig().getConfigurationSection(configPath);
        if (cfg == null) return;

        EmbedBuilder embed = new EmbedBuilder();

        String title = cfg.getString("title");
        if (title != null) embed.setTitle(title);

        String desc = cfg.getString("description");
        if (desc != null) {
            desc = desc.replace("[PLAYER]", playerName).replace("[DISCORD_PING_NICK]", discordMention);
            embed.setDescription(desc);
        }

        try {
            String colorStr = cfg.getString("embed-color");
            if (colorStr != null) {
                embed.setColor(Color.decode(colorStr));
            } else {
                embed.setColor(Color.WHITE);
            }
        } catch (Exception e) {
            embed.setColor(Color.WHITE);
        }

        String thumbUrl = cfg.getString("embed-thumbnail-url");
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            embed.setThumbnail(thumbUrl);
        }

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
