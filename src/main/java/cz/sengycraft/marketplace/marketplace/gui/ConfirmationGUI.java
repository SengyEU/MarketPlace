package cz.sengycraft.marketplace.marketplace.gui;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.ComponentUtils;
import cz.sengycraft.marketplace.utils.ItemStackUtils;
import cz.sengycraft.marketplace.utils.MessageUtils;
import cz.sengycraft.marketplace.utils.Pair;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;

public class ConfirmationGUI {

    static DatabaseManager databaseManager = DatabaseManager.getInstance();
    static ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    static YamlDocument config = configurationManager.getConfiguration("config");
    static String itemsCollectionName = config.getString("database.items-collection-name");

    public static Gui getConfirmationGUI(ItemData itemData, Player buyer, ItemStack modifiedItem) {
        Gui gui = Gui.gui()
                .title(ComponentUtils.deserialize(
                                MessageUtils.replacePlaceholders(
                                        config.getString("gui.confirmation.title"),
                                        new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(ItemStack.deserializeBytes(itemData.getItem())))))
                        )
                )
                .rows(config.getInt("gui.confirmation.rows"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(inventoryCloseEvent -> {
            if (inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.PLAYER))
                MessageUtils.sendMessage(buyer, "commands.marketplace.cancel");
        });

        Pair<HashSet<Integer>, ItemStack> fillItem = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.confirmation.fill-item"));
        gui.setItem(
                new ArrayList<>(fillItem.getLeft()),
                ItemBuilder.from(fillItem.getRight()).asGuiItem()
        );

        Pair<HashSet<Integer>, ItemStack> confirm = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.confirmation.confirm"));
        gui.setItem(
                new ArrayList<>(confirm.getLeft()),
                ItemBuilder.from(confirm.getRight()).asGuiItem(inventoryClickEvent -> {
                    if (databaseManager.findDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId())) != null) {
                        databaseManager.deleteDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId()));
                        buyer.getInventory().addItem(ItemStack.deserializeBytes(itemData.getItem()));
                        MarketPlaceGUI.getMarketPlaceGUI(buyer).open(buyer);
                        MessageUtils.sendMessage(
                                buyer,
                                "commands.marketplace.success",
                                new Pair<>("{itemCount}", String.valueOf(modifiedItem.getAmount())),
                                new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(modifiedItem))),
                                new Pair<>("{price}", String.valueOf(itemData.getPrice()))
                        );
                    } else {
                        MessageUtils.sendMessage(buyer, "commands.marketplace.item-not-available");
                        MarketPlaceGUI.refreshGUI(buyer);
                    }
                })
        );

        Pair<HashSet<Integer>, ItemStack> cancel = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.confirmation.cancel"));
        gui.setItem(
                new ArrayList<>(cancel.getLeft()),
                ItemBuilder.from(cancel.getRight()).asGuiItem(inventoryClickEvent -> {
                    MarketPlaceGUI.getMarketPlaceGUI(buyer).open(buyer);
                    MessageUtils.sendMessage(buyer, "commands.marketplace.cancel");
                })
        );

        gui.setItem(
                new ArrayList<>(ItemStackUtils.getSlots(config.getString("gui.confirmation.item-for-sale.slot"))),
                ItemBuilder.from(modifiedItem).asGuiItem()
        );

        return gui;
    }

}
