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
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;

public class ConfirmationGUI {

    private final DatabaseManager databaseManager;
    private final YamlDocument config;

    private static final String ITEM_NAME_PLACEHOLDER = "{itemName}";
    private static final String ITEM_COUNT_PLACEHOLDER = "{itemCount}";
    private static final String PRICE_PLACEHOLDER = "{price}";

    public ConfirmationGUI() {
        this.databaseManager = DatabaseManager.getInstance();
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        this.config = configurationManager.getConfiguration("config");
    }

    public Gui getConfirmationGUI(ItemData itemData, Player buyer, ItemStack modifiedItem) {
        Gui gui = createGui(itemData);
        configureGuiActions(gui, itemData, buyer, modifiedItem);
        return gui;
    }

    private Gui createGui(ItemData itemData) {
        String title = MessageUtils.replacePlaceholders(
                config.getString("gui.confirmation.title"),
                new Pair<>(ITEM_NAME_PLACEHOLDER, ComponentUtils.serialize(ItemStackUtils.getItemName(ItemStack.deserializeBytes(itemData.getItem()))))
        );

        return Gui.gui()
                .title(ComponentUtils.deserialize(title))
                .rows(config.getInt("gui.confirmation.rows"))
                .disableAllInteractions()
                .create();
    }

    private void configureGuiActions(Gui gui, ItemData itemData, Player buyer, ItemStack modifiedItem) {
        gui.setCloseGuiAction(event -> handleGuiClose(event, buyer));

        setGuiItem(gui);
        setGuiItemWithAction(gui, "gui.confirmation.confirm", event -> handleConfirmAction(itemData, buyer, modifiedItem));
        setGuiItemWithAction(gui, "gui.confirmation.cancel", event -> handleCancelAction(buyer));
        setItemForSale(gui, modifiedItem);
    }

    private void handleGuiClose(InventoryCloseEvent event, Player buyer) {
        if (event.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
            MessageUtils.sendMessage(buyer, "commands.marketplace.cancel");
        }
    }

    private void setGuiItem(Gui gui) {
        Pair<HashSet<Integer>, ItemStack> itemData = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.confirmation.fill-item"));
        gui.setItem(
                new ArrayList<>(itemData.getLeft()),
                ItemBuilder.from(itemData.getRight()).asGuiItem()
        );
    }

    private void setGuiItemWithAction(Gui gui, String configPath, GuiAction<InventoryClickEvent> action) {
        Pair<HashSet<Integer>, ItemStack> itemData = ItemStackUtils.getItemFromConfiguration(config.getSection(configPath));
        gui.setItem(
                new ArrayList<>(itemData.getLeft()),
                ItemBuilder.from(itemData.getRight()).asGuiItem(action)
        );
    }

    private void setItemForSale(Gui gui, ItemStack item) {
        HashSet<Integer> slots = ItemStackUtils.getSlots(config.getString("gui.confirmation.item-for-sale.slot"));
        gui.setItem(new ArrayList<>(slots), ItemBuilder.from(item).asGuiItem());
    }

    private void handleConfirmAction(ItemData itemData, Player buyer, ItemStack modifiedItem) {
        String itemId = itemData.getObjectId();
        if (databaseManager.findDocument(config.getString("database.items-collection-name"), "_id", new ObjectId(itemId)) != null) {
            databaseManager.deleteDocument(config.getString("database.items-collection-name"), "_id", new ObjectId(itemId));
            buyer.getInventory().addItem(ItemStack.deserializeBytes(itemData.getItem()));
            openMarketPlaceGui(buyer);
            sendMessage(
                    buyer,
                    "commands.marketplace.success",
                    new Pair<>(ITEM_COUNT_PLACEHOLDER, String.valueOf(modifiedItem.getAmount())),
                    new Pair<>(ITEM_NAME_PLACEHOLDER, ComponentUtils.serialize(ItemStackUtils.getItemName(modifiedItem))),
                    new Pair<>(PRICE_PLACEHOLDER, String.valueOf(itemData.getPrice()))
            );
        } else {
            sendMessage(buyer, "commands.marketplace.item-not-available");
            openMarketPlaceGui(buyer);
        }
    }

    private void handleCancelAction(Player buyer) {
        openMarketPlaceGui(buyer);
        sendMessage(buyer, "commands.marketplace.cancel");
    }

    private void openMarketPlaceGui(Player buyer) {
        new MarketPlaceGUI().getMarketPlaceGUI(buyer).open(buyer);
    }

    @SafeVarargs
    private final void sendMessage(Player player, String messageKey, Pair<String, String>... placeholders) {
        MessageUtils.sendMessage(player, messageKey, placeholders);
    }
}
