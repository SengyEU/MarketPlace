package cz.sengycraft.marketplace.gui;

import cz.sengycraft.marketplace.discord.Webhook;
import cz.sengycraft.marketplace.items.ItemData;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.transactions.TransactionData;
import cz.sengycraft.marketplace.transactions.TransactionsManager;
import cz.sengycraft.marketplace.utils.*;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

public class ConfirmationGUI extends BaseGUI {

    private final DatabaseManager databaseManager;
    private final boolean blackMarket;
    private final String configPath;

    public ConfirmationGUI(boolean blackMarket) {
        super("config");
        this.blackMarket = blackMarket;
        this.configPath = blackMarket ? "blackmarket" : "marketplace";
        this.databaseManager = DatabaseManager.getInstance();
    }

    public Gui getConfirmationGUI(ItemData itemData, Player buyer, ItemStack modifiedItem) {
        Gui gui = createGui(ComponentUtils.serialize(ItemStackUtils.getItemName(ItemStack.deserializeBytes(itemData.getItem()))));
        configureGuiActions(gui, itemData, buyer, modifiedItem);
        return gui;
    }

    private void configureGuiActions(Gui gui, ItemData itemData, Player buyer, ItemStack modifiedItem) {
        gui.setCloseGuiAction(event -> handleGuiClose(event, buyer));
        setGuiControls(gui, buyer, "gui.confirmation");
        addGuiItem(gui, "gui.confirmation.confirm", event -> handleConfirmAction(itemData, buyer, modifiedItem));
        addGuiItem(gui, "gui.confirmation.cancel", event -> handleCancelAction(buyer));

        setItemForSale(gui, modifiedItem);
    }

    private void handleGuiClose(InventoryCloseEvent event, Player buyer) {
        if (event.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
            sendMessage(buyer, "commands.marketplace.cancel");
        }
    }

    private void setItemForSale(Gui gui, ItemStack item) {
        HashSet<Integer> slots = ItemStackUtils.getSlots(config.getString("gui.confirmation.item-for-sale.slot"));
        gui.setItem(new ArrayList<>(slots), ItemBuilder.from(item).asGuiItem());
    }

    private void handleConfirmAction(ItemData itemData, Player buyer, ItemStack modifiedItem) {
        String itemId = itemData.getObjectId();
        if (databaseManager.findDocument(config.getString("database.items-collection-name"), "_id", new ObjectId(itemId)) == null) {
            sendMessage(buyer, "commands.marketplace.item-not-available");
            openMarketPlaceGui(buyer);
            return;
        }

        double price = blackMarket ? itemData.getHalfPrice() : itemData.getPrice();

        if (!VaultIntegration.hasMoney(buyer, price)) {
            sendMessage(buyer, "commands.marketplace.not-enough-money");
            openMarketPlaceGui(buyer);
            return;
        }

        if (hasFullInventory(buyer)) {
            sendMessage(buyer, "commands.marketplace.no-inventory-space");
            openMarketPlaceGui(buyer);
            return;
        }

        databaseManager.deleteDocument(config.getString("database.items-collection-name"), "_id", new ObjectId(itemId));
        buyer.getInventory().addItem(ItemStack.deserializeBytes(itemData.getItem()));
        openMarketPlaceGui(buyer);
        sendMessage(
                buyer,
                "commands." + configPath + ".success",
                new Pair<>("{itemCount}", String.valueOf(modifiedItem.getAmount())),
                new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(modifiedItem))),
                new Pair<>("{price}", NumberUtils.getDoubleFormatted(price))
        );

        Player seller = Bukkit.getPlayer(itemData.getSeller());
        int sellerPrice = blackMarket ? itemData.getDoublePrice() : itemData.getPrice();

        if (seller != null) {
            sendMessage(
                    seller,
                    "commands." + configPath + ".success-seller",
                    new Pair<>("{player}", buyer.getName()),
                    new Pair<>("{itemCount}", String.valueOf(modifiedItem.getAmount())),
                    new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(modifiedItem))),
                    new Pair<>("{price}", String.valueOf(sellerPrice))
            );
        }

        VaultIntegration.changeBalance(Bukkit.getOfflinePlayer(itemData.getSeller()), sellerPrice);
        VaultIntegration.changeBalance(buyer, -price);

        TransactionsManager.getInstance().storeTransaction(new TransactionData(
                itemData.getItem(),
                blackMarket,
                price,
                sellerPrice,
                buyer.getName(),
                itemData.getSeller(),
                LocalDateTime.now()
        ));

        String message = config.getString("discord-webhook.message")
                .replace("{itemName}", ComponentUtils.serializePlain(ItemStackUtils.getItemName(ItemStack.deserializeBytes(itemData.getItem()))))
                .replace("{place}", blackMarket ? config.getString("discord-webhook.blackmarket") : config.getString("discord-webhook.marketplace"))
                .replace("{seller}", itemData.getSeller())
                .replace("{buyer}", buyer.getName())
                .replace("{price}", String.valueOf(itemData.getPrice()))
                .replace("{date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getString("gui.transactions.transaction.date-format"))));

        Webhook.sendWebhook(message);
    }

    private void handleCancelAction(Player buyer) {
        openMarketPlaceGui(buyer);
        sendMessage(buyer, "commands.marketplace.cancel");
    }

    private void openMarketPlaceGui(Player buyer) {
        new MarketPlaceGUI(blackMarket).getMarketPlaceGUI(buyer).open(buyer);
    }

    @Override
    protected void refreshGUI(Player player) {
    }
}
