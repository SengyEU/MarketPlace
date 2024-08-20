package cz.sengycraft.marketplace.gui;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.items.ItemData;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.ComponentUtils;
import cz.sengycraft.marketplace.utils.ItemStackUtils;
import cz.sengycraft.marketplace.utils.MessageUtils;
import cz.sengycraft.marketplace.utils.Pair;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class BaseGUI {

    protected final YamlDocument config;
    protected final DatabaseManager databaseManager;
    protected final String itemsCollectionName;

    public BaseGUI(String configName) {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        this.config = configurationManager.getConfiguration(configName);
        this.databaseManager = DatabaseManager.getInstance();
        this.itemsCollectionName = config.getString("database.items-collection-name");
    }

    protected PaginatedGui createPaginatedGui(String titlePath, String rowsPath) {
        return Gui.paginated()
                .title(ComponentUtils.deserialize(config.getString(titlePath)))
                .rows(config.getInt(rowsPath))
                .pageSize((config.getInt(rowsPath) - 1) * 9)
                .disableAllInteractions()
                .create();
    }

    protected Gui createGui(String titleReplacement) {
        return Gui.gui()
                .title(ComponentUtils.deserialize(MessageUtils.replacePlaceholders(config.getString("gui.confirmation.title"), new Pair<>("{itemName}", titleReplacement))))
                .rows(config.getInt("gui.confirmation.rows"))
                .disableAllInteractions()
                .create();
    }

    protected void setGuiControls(BaseGui gui, Player player, String configPathPrefix) {
        addGuiItem(gui, configPathPrefix + ".fill-item", null);
        if (gui instanceof PaginatedGui paginatedGui) {
            addGuiItem(gui, configPathPrefix + ".previous-page", event -> paginatedGui.previous());
            addGuiItem(gui, configPathPrefix + ".refresh", event -> refreshGUI(player));
            addGuiItem(gui, configPathPrefix + ".next-page", event -> paginatedGui.next());
        }
    }

    protected void addGuiItem(BaseGui gui, String configPath, GuiAction<InventoryClickEvent> action) {
        Pair<HashSet<Integer>, ItemStack> slotAndItemPair = ItemStackUtils.getItemFromConfiguration(config.getSection(configPath));
        List<Integer> slots = new ArrayList<>(slotAndItemPair.getLeft());
        ItemBuilder itemBuilder = ItemBuilder.from(slotAndItemPair.getRight());
        GuiItem guiItem = (action == null) ? itemBuilder.asGuiItem() : itemBuilder.asGuiItem(action);
        gui.setItem(slots, guiItem);
    }

    protected boolean hasFullInventory(Player player) {
        return Arrays.stream(player.getInventory().getStorageContents()).noneMatch(Objects::isNull);
    }

    protected boolean isItemAvailable(ItemData itemData) {
        return databaseManager.findDocument(itemsCollectionName, "_id", new ObjectId(itemData.getObjectId())) != null;
    }

    @SafeVarargs
    protected final void sendMessage(Player player, String messageKey, Pair<String, String>... placeholders) {
        MessageUtils.sendMessage(player, messageKey, placeholders);
    }

    protected abstract void refreshGUI(Player player);
}
