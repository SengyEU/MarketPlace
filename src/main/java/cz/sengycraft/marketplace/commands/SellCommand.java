package cz.sengycraft.marketplace.commands;

import cz.sengycraft.marketplace.common.Permissions;
import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.marketplace.items.ItemManager;
import cz.sengycraft.marketplace.utils.ComponentUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SellCommand implements TabExecutor {

    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    YamlDocument messages = configurationManager.getConfiguration("messages");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(deserialize(messages.getString("commands.player-only")));
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(Permissions.SELL.permission())) {
            player.sendMessage(deserialize(messages.getString("commands.no-permission")));
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(deserialize(messages.getString("commands.usage").replace("{usage}", messages.getString("commands.sell.usage"))));
            return false;
        }

        ItemManager.getInstance().storeItem(
                new ItemData(
                        player.getInventory().getItemInMainHand().serializeAsBytes(),
                        Integer.parseInt(args[0]),
                        player.getName()
                )
        );
        player.sendMessage("Item put on marketplace");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList("<price>");
        return new ArrayList<>();
    }

    private Component deserialize(String message) {
        return ComponentUtils.deserialize(message);
    }
}
