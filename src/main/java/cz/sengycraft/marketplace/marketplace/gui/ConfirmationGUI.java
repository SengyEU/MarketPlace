package cz.sengycraft.marketplace.marketplace.gui;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.MessageUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bson.types.ObjectId;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmationGUI {

    static DatabaseManager databaseManager = DatabaseManager.getInstance();
    static ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    static YamlDocument config = configurationManager.getConfiguration("config");
    static String itemsCollectionName = config.getString("database.items-collection-name");

    public static Gui getConfirmationGUI(ItemData itemData, Player buyer) {
        Gui gui = Gui.gui()
                .title(Component.text("Do you really want to buy {itemName} ?"))
                .rows(3)
                .disableAllInteractions()
                .create();

        gui.setItem(11, ItemBuilder.from(Material.GREEN_WOOL).asGuiItem(inventoryClickEvent -> {
            if (databaseManager.findDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId())) != null) {
                databaseManager.deleteDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId()));
                buyer.getInventory().addItem(ItemStack.deserializeBytes(itemData.getItem()));
                MarketPlaceGUI.getMarketPlaceGUI(buyer).open(buyer);
            } else {
                MessageUtils.sendMessage(buyer, "commands.marketplace.item-not-available");
                MarketPlaceGUI.refreshGUI(buyer);
            }
        }));
        gui.setItem(15, ItemBuilder.from(Material.RED_WOOL).asGuiItem(inventoryClickEvent -> MarketPlaceGUI.getMarketPlaceGUI(buyer).open(buyer)));

        return gui;
    }

}
