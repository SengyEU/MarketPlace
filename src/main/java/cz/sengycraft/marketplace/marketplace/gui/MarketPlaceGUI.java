package cz.sengycraft.marketplace.marketplace.gui;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.marketplace.items.ItemManager;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.*;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketPlaceGUI {

    static ItemManager itemManager = ItemManager.getInstance();

    static DatabaseManager databaseManager = DatabaseManager.getInstance();
    static ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    static YamlDocument config = configurationManager.getConfiguration("config");
    static String itemsCollectionName = config.getString("database.items-collection-name");

    public static PaginatedGui getMarketPlaceGUI(Player buyer) {
        PaginatedGui marketPlaceGUI = Gui.paginated()
                .title(ComponentUtils.deserialize(config.getString("gui.marketplace.title")))
                .rows(config.getInt("gui.marketplace.rows"))
                .pageSize((config.getInt("gui.marketplace.rows") - 1) * 9)
                .disableAllInteractions().create();

        Pair<HashSet<Integer>, ItemStack> fillItem = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.marketplace.fill-item"));
        marketPlaceGUI.setItem(
                new ArrayList<>(fillItem.getLeft()),
                ItemBuilder.from(fillItem.getRight()).asGuiItem()
        );

        Pair<HashSet<Integer>, ItemStack> previousPage = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.marketplace.previous-page"));
        marketPlaceGUI.setItem(
                new ArrayList<>(previousPage.getLeft()),
                ItemBuilder.from(previousPage.getRight()).asGuiItem(inventoryClickEvent -> marketPlaceGUI.previous())
        );

        Pair<HashSet<Integer>, ItemStack> refresh = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.marketplace.refresh"));
        marketPlaceGUI.setItem(
                new ArrayList<>(refresh.getLeft()),
                ItemBuilder.from(refresh.getRight()).asGuiItem(inventoryClickEvent -> refreshGUI(buyer))
        );

        Pair<HashSet<Integer>, ItemStack> nextPage = ItemStackUtils.getItemFromConfiguration(config.getSection("gui.marketplace.next-page"));
        marketPlaceGUI.setItem(
                new ArrayList<>(nextPage.getLeft()),
                ItemBuilder.from(nextPage.getRight()).asGuiItem(inventoryClickEvent -> marketPlaceGUI.next())
        );

        for (ItemData itemData : itemManager.getItems()) {

            ItemStack itemForSale = ItemStack.deserializeBytes(itemData.getItem());

            ItemMeta itemForSaleMeta = itemForSale.getItemMeta();
            itemForSaleMeta.displayName(ComponentUtils.deserialize(
                    MessageUtils.replacePlaceholders(
                            config.getString("gui.marketplace.items-for-sale.title"),
                            new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(itemForSale)))
                    )
            ));
            List<Component> originalLore = itemForSaleMeta.lore();
            if (originalLore == null) originalLore = new ArrayList<>();

            originalLore.addAll(ComponentUtils.deserialize(MessageUtils.replacePlaceholders(
                    config.getStringList("gui.marketplace.items-for-sale.lore"),
                    new Pair<>("{seller}", itemData.getSeller()),
                    new Pair<>("{price}", String.valueOf(itemData.getPrice()))
            )));

            itemForSaleMeta.lore(originalLore);

            itemForSale.setItemMeta(itemForSaleMeta);

            marketPlaceGUI.addItem(ItemBuilder.from(itemForSale).asGuiItem(inventoryClickEvent -> {
                if (inventoryClickEvent.isLeftClick()) {
                    if (databaseManager.findDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId())) != null) {
                        if (!itemData.getSeller().equalsIgnoreCase(buyer.getName())) {
                            if (VaultIntegration.hasMoney(buyer, itemData.getPrice())) {
                                if((int) Arrays.stream(buyer.getInventory().getStorageContents()).filter(Objects::isNull).count() > 0) {
                                    ConfirmationGUI.getConfirmationGUI(itemData, buyer, itemForSale).open(buyer);
                                } else {
                                    MessageUtils.sendMessage(buyer, "commands.marketplace.no-inventory-space");
                                    refreshGUI(buyer);
                                }
                            } else {
                                MessageUtils.sendMessage(buyer, "commands.marketplace.not-enough-money");
                                refreshGUI(buyer);
                            }
                        } else {
                            MessageUtils.sendMessage(buyer, "commands.marketplace.your-item");
                            refreshGUI(buyer);
                        }
                    } else {
                        MessageUtils.sendMessage(buyer, "commands.marketplace.item-not-available");
                        refreshGUI(buyer);
                    }
                }
            }));
        }

        return marketPlaceGUI;
    }

    // TODO: Better implementation. For now, this is the best the Triumph GUI library can do.
    public static void refreshGUI(Player player) {
        MarketPlaceGUI.getMarketPlaceGUI(player).open(player);
    }

}
