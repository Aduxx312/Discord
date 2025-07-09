package listeners;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.aduxx.aDUXXDCREWARD.DiscordRewardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class GuiHandler implements Listener {

    private final DiscordRewardPlugin plugin;
    private Inventory gui;
    private String guiTitle;

    public GuiHandler(DiscordRewardPlugin plugin) {
        this.plugin = plugin;
        createGui();
    }

    private void createGui() {
        int size = plugin.getConfig().getInt("gui.size", 45);
        guiTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.name", "&6&lNagroda discord"));
        gui = Bukkit.createInventory(null, size, guiTitle);

        for (String key : plugin.getConfig().getConfigurationSection("gui.slots").getKeys(false)) {
            int slot = Integer.parseInt(key);
            String itemString = plugin.getConfig().getString("gui.slots." + key + ".item", "WHITE_STAINED_GLASS_PANE");
            Material material = itemString.equalsIgnoreCase("empty") ? Material.AIR : Material.getMaterial(itemString.toUpperCase());
            if (material == null) material = Material.WHITE_STAINED_GLASS_PANE;

            ItemStack item;

            if (material == Material.PLAYER_HEAD && slot == 21) {
                String base64 = plugin.getConfig().getString("gui.slots.21.value");
                item = createSkull(base64);
            } else if (material == Material.AIR) {
                item = new ItemStack(Material.AIR);
            } else {
                item = new ItemStack(material);
            }

            if (item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String displayName = plugin.getConfig().getString("gui.slots." + key + ".name", " ");
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

                    List<String> loreRaw = plugin.getConfig().getStringList("gui.slots." + key + ".lore");
                    List<String> loreColored = new ArrayList<>();
                    for (String line : loreRaw) {
                        loreColored.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                    meta.setLore(loreColored);
                    item.setItemMeta(meta);
                }
            }

            gui.setItem(slot, item);
        }
    }

    private ItemStack createSkull(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64 == null || base64.isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "customSkinName");
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    public void openGui(Player player) {
        player.openInventory(updateGuiForPlayer(player));
    }

    private Inventory updateGuiForPlayer(Player player) {
        Inventory playerGui = Bukkit.createInventory(null, gui.getSize(), guiTitle);
        for (int i = 0; i < gui.getSize(); i++) {
            ItemStack original = gui.getItem(i);
            if (original == null) continue;

            ItemStack copy = original.clone();
            ItemMeta meta = copy.getItemMeta();

            if (i == 21 && meta != null && meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                boolean rewarded = plugin.getRewardStorage().isRewarded(player.getName().toLowerCase());
                String statusKey = rewarded ? "status.claimed" : "status.notclaimed";
                String status = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(statusKey, rewarded ? "&a✓" : "&c𐄂"));

                for (String line : meta.getLore()) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("{status}", status)));
                }

                meta.setLore(lore);
                copy.setItemMeta(meta);
            }

            playerGui.setItem(i, copy);
        }
        return playerGui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();
        String playerKey = player.getName().toLowerCase();

        if (event.getRawSlot() == 21) {
            if (plugin.getRewardStorage().isRewarded(playerKey)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.reward-picked", "&cOdebrałeś już nagrodę!")));
                player.closeInventory();
                return;
            }

            if (!plugin.getRewardStorage().isEligible(playerKey)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("reward-info",
                                "&cMusisz najpierw &6powiązać &cswoje konto z naszym &6discordem!")));
                player.closeInventory();
                return;
            }

            String rewardCommand = plugin.getConfig().getString("reward-command", "give [PLAYER] diamond 5")
                    .replace("[PLAYER]", player.getName());

            boolean success = plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(), rewardCommand);

            if (success) {
                plugin.getRewardStorage().setRewarded(playerKey);
                plugin.getRewardStorage().removeEligible(playerKey);

                String pickedInfo = plugin.getConfig().getString("picked-info",
                        "&8>> &7Gracz &6[PLAYER] &7odebrał nagrodę za dołączenie do discorda &8>> &7Chcesz też? &7Sprawdź &e/nagroda");
                if (pickedInfo != null) {
                    pickedInfo = pickedInfo.replace("[PLAYER]", player.getName());
                    plugin.getServer().broadcastMessage(colorize(pickedInfo));
                }


            }
        }
    }

    private String colorize(String msg) {
        return msg.replace("&", "§");
    }
}
