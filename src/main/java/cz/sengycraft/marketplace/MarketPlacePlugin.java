package cz.sengycraft.marketplace;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MarketPlacePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigurationManager.getInstance().setPlugin(this);
        try {
            ConfigurationManager.getInstance().initializeConfigurations("config.yml", "marketplace.yml", "blackmarket.yml");
        } catch (IOException e) {
            getLogger().severe("Couldn't load configuration files! The plugin will now disable!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }



}
