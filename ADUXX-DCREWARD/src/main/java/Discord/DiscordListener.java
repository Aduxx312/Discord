package Discord;

import dev.aduxx.aDUXXDCREWARD.DiscordRewardPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

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


        if (plugin.getServer().getPlayer(playerName) == null) {
            sendEmbed(event, "player-not-online", playerName, discordMention);
            return;
        }


        if (plugin.getRewardStorage().isRewarded(playerName)) {
            sendEmbed(event, "reward-picked", playerName, discordMention);
            return;
        }

        String rewardCommand = plugin.getConfig().getString("reward-command", "give [PLAYER] diamond 5");
        if (rewardCommand == null) {
            event.getChannel().sendMessage("Brak komendy nagrody w konfiguracji!").queue();
            return;
        }

        rewardCommand = rewardCommand.replace("[PLAYER]", playerName);


        String finalRewardCommand = rewardCommand;
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean commandSuccess = plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), finalRewardCommand);

                if (commandSuccess) {
                    plugin.getRewardStorage().setRewarded(playerName);
                    sendEmbed(event, "reward-success", playerName, discordMention);


                    String pickedInfo = plugin.getConfig().getString("picked-info", "&8>> &7Gracz &6[PLAYER] &7odebrał nagrodę za dołączenie do discorda &8>> &7Chcesz też? &7Sprawdź &e/nagroda");
                    if (pickedInfo != null) {
                        pickedInfo = pickedInfo.replace("[PLAYER]", playerName);
                        plugin.getServer().broadcastMessage(colorize(pickedInfo));
                    }

                } else {
                    event.getChannel().sendMessage("Nie udało się przyznać nagrody!").queue();
                }
            }
        }.runTask(plugin);
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


    private String colorize(String msg) {
        return msg.replace("&", "§");
    }
}
