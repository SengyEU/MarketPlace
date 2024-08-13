package cz.sengycraft.marketplace.configuration;

import cz.sengycraft.marketplace.MarketPlacePlugin;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ConfigurationManager {

    private MarketPlacePlugin plugin;

    public void setPlugin(MarketPlacePlugin plugin) {
        this.plugin = plugin;
    }

    private static ConfigurationManager instance;

    public static ConfigurationManager getInstance() {
        if (instance == null) instance = new ConfigurationManager();

        return instance;
    }

    private final HashMap<String, YamlDocument> configurations = new HashMap<>();

    public void initializeConfigurations(String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            YamlDocument yamlDocument = YamlDocument.create(new File(plugin.getDataFolder(), fileName + ".yml"),
                    Objects.requireNonNull(plugin.getResource(fileName + ".yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            yamlDocument.update();
            yamlDocument.save();

            configurations.put(fileName, yamlDocument);
        }
    }

    public YamlDocument getConfiguration(String name) {
        return configurations.get(name);
    }

    public void reloadConfigurations() throws IOException {
        for (YamlDocument configuration : configurations.values()) {
            configuration.reload();
        }
    }

}
