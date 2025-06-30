package chatutil;

import org.bukkit.ChatColor;

public class ChatUtil {
    public static String replaceDiscordPlaceholders(String input, String player, String discordPing) {
        return ChatColor.translateAlternateColorCodes('&', input
                .replace("[DISCORD_PING_NICK]", discordPing)
                .replace("[PLAYER]", player));
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}