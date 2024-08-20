package cz.sengycraft.marketplace.gui;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.transactions.TransactionData;
import cz.sengycraft.marketplace.transactions.TransactionsManager;
import cz.sengycraft.marketplace.utils.*;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class TransactionsGUI {

    private final TransactionsManager transactionsManager;
    private final YamlDocument config;

    public TransactionsGUI() {
        this.transactionsManager = TransactionsManager.getInstance();
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        this.config = configurationManager.getConfiguration("config");
    }

    public PaginatedGui getTransactionsGUI(Player player) {
        PaginatedGui transactionsGUI = createGui();
        setGuiControls(transactionsGUI, player);
        populateTransactions(transactionsGUI, player.getName());

        return transactionsGUI;
    }

    private PaginatedGui createGui() {
        return Gui.paginated()
                .title(ComponentUtils.deserialize(config.getString("gui.transactions.title")))
                .rows(config.getInt("gui.transactions.rows"))
                .pageSize((config.getInt("gui.transactions.rows") - 1) * 9)
                .disableAllInteractions()
                .create();
    }

    private void setGuiControls(PaginatedGui gui, Player player) {
        addGuiItem(gui, "gui.transactions.fill-item", null);
        addGuiItem(gui, "gui.transactions.previous-page", event -> gui.previous());
        addGuiItem(gui, "gui.transactions.refresh", event -> refreshGUI(player));
        addGuiItem(gui, "gui.transactions.next-page", event -> gui.next());
    }

    private void addGuiItem(PaginatedGui gui, String configPath, GuiAction<InventoryClickEvent> action) {
        Pair<HashSet<Integer>, ItemStack> slotAndItemPair = ItemStackUtils.getItemFromConfiguration(config.getSection(configPath));

        List<Integer> slots = new ArrayList<>(slotAndItemPair.getLeft());

        ItemBuilder itemBuilder = ItemBuilder.from(slotAndItemPair.getRight());
        GuiItem guiItem = (action == null) ? itemBuilder.asGuiItem() : itemBuilder.asGuiItem(action);

        gui.setItem(slots, guiItem);
    }

    private void populateTransactions(PaginatedGui gui, String player) {
        List<Pair<TransactionData, Boolean>> transactionData = transactionsManager.getTransactions(player);

        for (Pair<TransactionData, Boolean> data : transactionData) {
            ItemStack transaction = ItemStack.deserializeBytes(data.getLeft().getItem());
            setItemMeta(transaction, data);

            gui.addItem(ItemBuilder.from(transaction).asGuiItem());
        }
    }

    private void setItemMeta(ItemStack transaction, Pair<TransactionData, Boolean> data) {
        ItemMeta itemMeta = transaction.getItemMeta();

        TransactionData transactionData = data.getLeft();
        boolean seller = data.getRight();

        String path = seller ? "sold" : "bought";

        String itemTitle = MessageUtils.replacePlaceholders(
                config.getString("gui.transactions.transaction." + path + ".title"),
                new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(transaction)))
        );
        itemMeta.displayName(ComponentUtils.deserialize(itemTitle));

        String place = transactionData.isBlackmarket() ? "blackmarket" : "marketplace";

        List<Component> lore = Optional.ofNullable(itemMeta.lore()).orElse(new ArrayList<>());
        lore.addAll(ComponentUtils.deserialize(MessageUtils.replacePlaceholders(
                config.getStringList("gui.transactions.transaction." + path + ".lore"),
                new Pair<>("{place}", config.getString("gui.transactions.transaction." + place)),
                new Pair<>(seller ? "{buyer}" : "{seller}", seller ? transactionData.getBuyer() : transactionData.getSeller()),
                new Pair<>("{price}", seller ? String.valueOf(transactionData.getPriceSeller()) : NumberUtils.getDoubleFormatted(transactionData.getPriceBuyer())),
                new Pair<>("{date}", transactionData.getDate().format(DateTimeFormatter.ofPattern(config.getString("gui.transactions.transaction.date-format"))))
        )));
        itemMeta.lore(lore);

        transaction.setItemMeta(itemMeta);
    }

    public void refreshGUI(Player player) {
        getTransactionsGUI(player).open(player);
    }

}
