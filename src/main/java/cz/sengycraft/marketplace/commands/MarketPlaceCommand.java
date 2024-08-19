package cz.sengycraft.marketplace.commands;

import cz.sengycraft.marketplace.common.Permissions;
import cz.sengycraft.marketplace.marketplace.gui.MarketPlaceGUI;
import cz.sengycraft.marketplace.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MarketPlaceCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender,"commands.player-only");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(Permissions.VIEW.permission())) {
            MessageUtils.sendMessage(sender,"commands.no-permission");
            return false;
        }

        new MarketPlaceGUI().getMarketPlaceGUI(player).open(player);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

}
