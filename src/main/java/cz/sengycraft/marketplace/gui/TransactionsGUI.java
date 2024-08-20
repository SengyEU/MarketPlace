package cz.sengycraft.marketplace.gui;

import cz.sengycraft.marketplace.transactions.TransactionData;
import cz.sengycraft.marketplace.transactions.TransactionsManager;
import cz.sengycraft.marketplace.utils.*;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionsGUI extends BaseGUI {

    private final TransactionsManager transactionsManager;

    public TransactionsGUI() {
        super("config");
        this.transactionsManager = TransactionsManager.getInstance();
    }

    public PaginatedGui getTransactionsGUI(Player player) {
        PaginatedGui transactionsGUI = createPaginatedGui("gui.transactions.title", "gui.transactions.rows");
        setGuiControls(transactionsGUI, player, "gui.transactions");
        populateTransactions(transactionsGUI, player.getName());
        return transactionsGUI;
    }

    private void populateTransactions(PaginatedGui gui, String playerName) {
        List<Pair<TransactionData, Boolean>> transactionData = transactionsManager.getTransactions(playerName);
        for (Pair<TransactionData, Boolean> data : transactionData) {
            ItemStack transaction = ItemStack.deserializeBytes(data.getLeft().getItem());
            setItemMeta(transaction, data);
            gui.addItem(ItemBuilder.from(transaction).asGuiItem());
        }
    }

    private void setItemMeta(ItemStack transaction, Pair<TransactionData, Boolean> data) {
        ItemMeta itemMeta = transaction.getItemMeta();
        TransactionData transactionData = data.getLeft();
        boolean isSeller = data.getRight();
        String path = isSeller ? "sold" : "bought";
        String itemTitle = MessageUtils.replacePlaceholders(
                config.getString("gui.transactions.transaction." + path + ".title"),
                new Pair<>("{itemName}", ComponentUtils.serialize(ItemStackUtils.getItemName(transaction)))
        );
        itemMeta.displayName(ComponentUtils.deserialize(itemTitle));

        List<Component> lore = Optional.ofNullable(itemMeta.lore()).orElse(new ArrayList<>());
        lore.addAll(ComponentUtils.deserialize(MessageUtils.replacePlaceholders(
                config.getStringList("gui.transactions.transaction." + path + ".lore"),
                new Pair<>("{place}", transactionData.isBlackmarket() ? "blackmarket" : "marketplace"),
                new Pair<>(isSeller ? "{buyer}" : "{seller}", isSeller ? transactionData.getBuyer() : transactionData.getSeller()),
                new Pair<>("{price}", isSeller ? String.valueOf(transactionData.getPriceSeller()) : NumberUtils.getDoubleFormatted(transactionData.getPriceBuyer())),
                new Pair<>("{date}", transactionData.getDate().format(DateTimeFormatter.ofPattern(config.getString("gui.transactions.transaction.date-format"))))
        )));
        itemMeta.lore(lore);
        transaction.setItemMeta(itemMeta);
    }

    @Override
    protected void refreshGUI(Player player) {
        getTransactionsGUI(player).open(player);
    }
}
