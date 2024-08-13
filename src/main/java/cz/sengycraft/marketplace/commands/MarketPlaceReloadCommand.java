package cz.sengycraft.marketplace.commands;

import cz.sengycraft.marketplace.MarketPlacePlugin;
import cz.sengycraft.marketplace.common.Permissions;
import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.utils.ComponentUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarketPlaceReloadCommand implements TabExecutor {

    private final MarketPlacePlugin plugin;

    public MarketPlaceReloadCommand(MarketPlacePlugin plugin) {
        this.plugin = plugin;
    }

    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    YamlDocument messages = configurationManager.getConfiguration("messages");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(deserialize(messages.getString("commands.player-only")));
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(Permissions.RELOAD.permission())) {
            player.sendMessage(deserialize(messages.getString("commands.no-permission")));
            return false;
        }

        try {
            configurationManager.reloadConfigurations();
            player.sendMessage(deserialize(messages.getString("commands.reload.success")));
        } catch (IOException e) {
            plugin.getLogger().severe(e.getMessage());
            player.sendMessage(deserialize(messages.getString("commands.reload.error")));
            return false;

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private Component deserialize(String message) {
        return ComponentUtils.deserialize(message);
    }
}
