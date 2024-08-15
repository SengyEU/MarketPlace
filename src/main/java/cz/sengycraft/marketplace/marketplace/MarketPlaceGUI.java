package cz.sengycraft.marketplace.marketplace;

import cz.sengycraft.marketplace.marketplace.items.ItemData;
import cz.sengycraft.marketplace.marketplace.items.ItemManager;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class MarketPlaceGUI {

    static ItemManager itemManager = ItemManager.getInstance();
    static int test = 6;

    public static PaginatedGui getMarketPlaceGUI() {
        PaginatedGui marketPlaceGUI = Gui.paginated()
                .title(Component.text("marketplace"))
                .rows(test)
                .pageSize((test - 1) * 9)
                .create();

        for (ItemData itemData : itemManager.getItems()) {
            marketPlaceGUI.addItem(ItemBuilder.from(ItemStack.deserializeBytes(itemData.getItem())).asGuiItem(inventoryClickEvent -> inventoryClickEvent.setCancelled(true)));
        }

        return marketPlaceGUI;
    }

}
