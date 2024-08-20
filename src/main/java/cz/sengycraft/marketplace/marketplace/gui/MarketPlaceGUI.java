package cz.sengycraft.marketplace.marketplace.gui;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.marketplace.items.ItemManager;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.*;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketPlaceGUI {

    private final ItemManager itemManager;
    private final DatabaseManager databaseManager;
    private final YamlDocument config;
    private final String itemsCollectionName;

    private final boolean blackMarket;

    String configPath;

    public MarketPlaceGUI(boolean blackMarket) {
        this.blackMarket = blackMarket;
        configPath = blackMarket ? "blackmarket" : "marketplace";
        this.itemManager = ItemManager.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        this.config = configurationManager.getConfiguration("config");
        this.itemsCollectionName = config.getString("database.items-collection-name");
    }

    public PaginatedGui getMarketPlaceGUI(Player buyer) {
        PaginatedGui marketPlaceGUI = createGui();
        setGuiControls(marketPlaceGUI, buyer);
        populateItemsForSale(marketPlaceGUI, buyer);
        return marketPlaceGUI;
    }

    private PaginatedGui createGui() {
        return Gui.paginated()
                .title(ComponentUtils.deserialize(config.getString("gui." + configPath + ".title")))
                .rows(config.getInt("gui." + configPath + ".rows"))
                .pageSize((config.getInt("gui." + configPath + ".rows") - 1) * 9)
                .disableAllInteractions()
                .create();
    }

    private void setGuiControls(PaginatedGui gui, Player buyer) {
        addGuiItem(gui, "gui." + configPath + ".fill-item", null);
        addGuiItem(gui, "gui." + configPath + ".previous-page", event -> gui.previous());
        addGuiItem(gui, "gui." + configPath + ".refresh", event -> refreshGUI(buyer));
        addGuiItem(gui, "gui." + configPath + ".next-page", event -> gui.next());
    }

    private void addGuiItem(PaginatedGui gui, String configPath, GuiAction<InventoryClickEvent> action) {
        Pair<HashSet<Integer>, ItemStack> slotAndItemPair = ItemStackUtils.getItemFromConfiguration(config.getSection(configPath));

        List<Integer> slots = new ArrayList<>(slotAndItemPair.getLeft());

        ItemBuilder itemBuilder = ItemBuilder.from(slotAndItemPair.getRight());
        GuiItem guiItem = (action == null) ? itemBuilder.asGuiItem() : itemBuilder.asGuiItem(action);

        gui.setItem(slots, guiItem);
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
            MessageUtils.sendMessage(buyer, "commands.marketplace.item-not-available");
            refreshGUI(buyer);
            return;
        }

        if (itemData.getSeller().equalsIgnoreCase(buyer.getName())) {
            MessageUtils.sendMessage(buyer, "commands.marketplace.your-item");
            refreshGUI(buyer);
            return;
        }

        double price = blackMarket ? itemData.getHalfPrice() : itemData.getPrice();

        if (!VaultIntegration.hasMoney(buyer, price)) {
            MessageUtils.sendMessage(buyer, "commands.marketplace.not-enough-money");
            refreshGUI(buyer);
            return;
        }

        if (!hasInventorySpace(buyer)) {
            MessageUtils.sendMessage(buyer, "commands.marketplace.no-inventory-space");
            refreshGUI(buyer);
            return;
        }

        new ConfirmationGUI(blackMarket).getConfirmationGUI(itemData, buyer, itemForSale).open(buyer);
    }

    private boolean isItemAvailable(ItemData itemData) {
        return databaseManager.findDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId())) != null;
    }

    private boolean hasInventorySpace(Player player) {
        return Arrays.stream(player.getInventory().getStorageContents()).anyMatch(Objects::isNull);
    }

    public void refreshGUI(Player player) {
        getMarketPlaceGUI(player).open(player);
    }
}
