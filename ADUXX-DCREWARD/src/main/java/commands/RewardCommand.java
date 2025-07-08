package commands;


import dev.aduxx.aDUXXDCREWARD.DiscordRewardPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RewardCommand implements CommandExecutor {
    private final DiscordRewardPlugin plugin;

    public RewardCommand(DiscordRewardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        plugin.getGuiHandler().openGui(player);

        return true;
    }
}
