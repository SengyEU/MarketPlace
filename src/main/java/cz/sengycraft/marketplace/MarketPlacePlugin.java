package cz.sengycraft.marketplace;

import cz.sengycraft.marketplace.commands.*;
import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.listeners.MacroListener;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.VaultIntegration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MarketPlacePlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        ConfigurationManager configurationManager = ConfigurationManager.getInstance();

        configurationManager.setPlugin(this);
        try {
            configurationManager.initializeConfigurations("config", "messages");
        } catch (IOException e) {
            getLogger().severe("Couldn't load configuration files! The plugin will now disable!");
            getServer().getPluginManager().disablePlugin(this);
        }

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.init(
                configurationManager.getConfiguration("config").getString("database.mongo-client-uri"),
                configurationManager.getConfiguration("config").getString("database.database-name")
        );

        VaultIntegration.setup();

        getServer().getPluginManager().registerEvents(new MacroListener(), this);

        getCommand("sell").setExecutor(new SellCommand());
        getCommand("marketplacereload").setExecutor(new MarketPlaceReloadCommand(this));
        getCommand("marketplace").setExecutor(new MarketPlaceCommand());
        getCommand("blackmarket").setExecutor(new BlackmarketCommand());
        getCommand("transactions").setExecutor(new TransactionsCommand());
    }

    @Override
    public void onDisable() {
        DatabaseManager.getInstance().close();
    }

}
