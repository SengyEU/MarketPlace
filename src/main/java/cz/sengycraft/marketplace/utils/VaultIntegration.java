package cz.sengycraft.marketplace.utils;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    private static Economy econ = null;

    public static void setup() {
        setupEconomy();
    }


    private static void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public static double getMoney(Player player) {
        return econ.getBalance(player);
    }

    public static void changeBalance(Player player, double amount) {
        if(amount == 0) return;

        if(amount < 0) {
            amount = amount * -1;
            econ.withdrawPlayer(player, amount);
        } else {
            econ.depositPlayer(player, amount);
        }
    }

    public static boolean hasMoney(Player player, double money) {
        return econ.has(player, money);
    }

}
