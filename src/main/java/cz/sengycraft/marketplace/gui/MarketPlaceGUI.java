
package cz.sengycraft.marketplace.gui;

import cz.sengycraft.marketplace.items.ItemData;
import cz.sengycraft.marketplace.items.ItemManager;
import cz.sengycraft.marketplace.utils.*;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MarketPlaceGUI extends BaseGUI {

    private final ItemManager itemManager;
    private final boolean blackMarket;
    private final String configPath;

    public MarketPlaceGUI(boolean blackMarket) {
        super("config");
        this.blackMarket = blackMarket;
        this.configPath = blackMarket ? "blackmarket" : "marketplace";
        this.itemManager = ItemManager.getInstance();
    }

    public PaginatedGui getMarketPlaceGUI(Player buyer) {
        PaginatedGui marketPlaceGUI = createPaginatedGui("gui." + configPath + ".title", "gui." + configPath + ".rows");
        setGuiControls(marketPlaceGUI, buyer, "gui." + configPath);
        populateItemsForSale(marketPlaceGUI, buyer);
        return marketPlaceGUI;
    }

    private void populateItemsForSale(PaginatedGui gui, Player buyer) {
        int maxItems = blackMarket ? config.getInt("gui.blackmarket.max-items") : Integer.MAX_VALUE;
        int itemCount = 0;

        List<ItemData> items = itemManager.getItems();
        if (blackMarket) {
            List<ItemData> itemsClone = new ArrayList<>(items);
            Collections.shuffle(itemsClone);
            items = itemsClone;
        }

        for (ItemData itemData : items) {
            if (itemCount >= maxItems) break;

            ItemStack itemForSale = ItemStack.deserializeBytes(itemData.getItem());
            setItemMeta(itemForSale, itemData);

            gui.addItem(ItemBuilder.from(itemForSale).asGuiItem(event -> handleItemClick(event, itemData, buyer, itemForSale)));
            itemCount++;
        }
    }

    private void setItemMeta(ItemStack itemForSale, ItemData itemData) {
        ItemMeta itemMeta = itemForSale.getItemMeta();

        String itemTitle = MessageUtils.replacePlaceholders(
                config.getString("gui." + configPath + ".items-for-sale.title"),
                new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(itemForSale)))
        );
        itemMeta.displayName(ComponentUtils.deserialize(itemTitle));

        String price = blackMarket ? itemData.getHalfPriceFormatted() : String.valueOf(itemData.getPrice());

        List<Component> lore = Optional.ofNullable(itemMeta.lore()).orElse(new ArrayList<>());
        lore.addAll(ComponentUtils.deserialize(MessageUtils.replacePlaceholders(
                config.getStringList("gui." + configPath + ".items-for-sale.lore"),
                new Pair<>("{seller}", itemData.getSeller()),
                new Pair<>("{price}", price)
        )));
        itemMeta.lore(lore);

        itemForSale.setItemMeta(itemMeta);
    }

    private void handleItemClick(InventoryClickEvent event, ItemData itemData, Player buyer, ItemStack itemForSale) {
        if (!event.isLeftClick()) return;

        if (!isItemAvailable(itemData)) {
            sendMessage(buyer, "commands.marketplace.item-not-available");
            refreshGUI(buyer);
            return;
        }

        if (itemData.getSeller().equalsIgnoreCase(buyer.getName())) {
            sendMessage(buyer, "commands.marketplace.your-item");
            refreshGUI(buyer);
            return;
        }

        double price = blackMarket ? itemData.getHalfPrice() : itemData.getPrice();

        if (!VaultIntegration.hasMoney(buyer, price)) {
            sendMessage(buyer, "commands.marketplace.not-enough-money");
            refreshGUI(buyer);
            return;
        }

        if (hasFullInventory(buyer)) {
            sendMessage(buyer, "commands.marketplace.no-inventory-space");
            refreshGUI(buyer);
            return;
        }

        new ConfirmationGUI(blackMarket).getConfirmationGUI(itemData, buyer, itemForSale).open(buyer);
    }

    @Override
    protected void refreshGUI(Player player) {
        getMarketPlaceGUI(player).open(player);
    }
}
