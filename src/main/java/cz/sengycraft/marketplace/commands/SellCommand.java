package cz.sengycraft.marketplace.commands;

import cz.sengycraft.marketplace.common.Permissions;
import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.marketplace.items.ItemManager;
import cz.sengycraft.marketplace.utils.*;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SellCommand implements TabExecutor {

    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    YamlDocument messages = configurationManager.getConfiguration("messages");
    YamlDocument config = configurationManager.getConfiguration("config");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender,"commands.player-only");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(Permissions.SELL.permission())) {
            sendMessage(sender,"commands.no-permission");
            return false;
        }

        if (args.length != 1) {
            sendMessage(sender,"commands.usage", new Pair<>("{usage}", messages.getString("commands.sell.usage")));
            return false;
        }

        if (!IntegerUtils.isInteger(args[0]) || Integer.parseInt(args[0]) <= 0) {
            sendMessage(sender,"commands.sell.invalid-price");
            return false;
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        if (itemInMainHand.getType().isAir()) {
            sendMessage(sender,"commands.sell.no-item");
            return false;
        }

        ItemManager.getInstance().storeItem(
                new ItemData(
                        itemInMainHand.serializeAsBytes(),
                        Integer.parseInt(args[0]),
                        player.getName()
                )
        );

        player.getInventory().setItemInMainHand(null);

        sendMessage(
                sender,
                "commands.sell.success",
                new Pair<>("{itemCount}", String.valueOf(itemInMainHand.getAmount())),
                new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(itemInMainHand))),
                new Pair<>("{price}", args[0])
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList(config.getString("commands.sell.price-tabcomplete"));
        return new ArrayList<>();
    }

    private void sendMessage(CommandSender sender, String messageKey) {
        MessageUtils.sendMessage(sender, messageKey);
    }

    @SafeVarargs
    private final void sendMessage(CommandSender sender, String messageKey, Pair<String, String>... placeholders) {
        MessageUtils.sendMessage(sender, messageKey, placeholders);
    }

}
