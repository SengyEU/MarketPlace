package cz.sengycraft.marketplace.transactions;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import cz.sengycraft.marketplace.utils.Pair;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bson.Document;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TransactionsManager {

    private static TransactionsManager instance;

    public static TransactionsManager getInstance() {
        if (instance == null) instance = new TransactionsManager();
        return instance;
    }

    private final DatabaseManager databaseManager = DatabaseManager.getInstance();
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private final YamlDocument config = configurationManager.getConfiguration("config");
    private final String transactionsCollectionName = config.getString("database.transactions-collection-name");

    public CompletableFuture<Void> storeTransaction(TransactionData transactionData) {
        Document document = new Document("item", transactionData.getItem())
                .append("blackmarket", transactionData.isBlackmarket())
                .append("priceBuyer", transactionData.getPriceBuyer())
                .append("priceSeller", transactionData.getPriceSeller())
                .append("buyer", transactionData.getBuyer())
                .append("seller", transactionData.getSeller())
                .append("date", transactionData.getDate());

        return databaseManager.insertDocument(transactionsCollectionName, document);
    }

    public CompletableFuture<List<Pair<TransactionData, Boolean>>> getTransactions(String playerName) {
        CompletableFuture<List<Pair<TransactionData, Boolean>>> buyerTransactionsFuture = databaseManager
                .getAllDocuments(transactionsCollectionName, "buyer", playerName)
                .thenApply(documents -> documents.stream()
                        .map(document -> new Pair<>(
                                new TransactionData(
                                        document.get("item", org.bson.types.Binary.class).getData(),
                                        document.getBoolean("blackmarket"),
                                        document.getDouble("priceBuyer"),
                                        document.getInteger("priceSeller"),
                                        document.getString("buyer"),
                                        document.getString("seller"),
                                        Instant.ofEpochMilli(document.getDate("date").getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                                ), false))
                        .collect(Collectors.toList())
                );

        CompletableFuture<List<Pair<TransactionData, Boolean>>> sellerTransactionsFuture = databaseManager
                .getAllDocuments(transactionsCollectionName, "seller", playerName)
                .thenApply(documents -> documents.stream()
                        .map(document -> new Pair<>(
                                new TransactionData(
                                        document.get("item", org.bson.types.Binary.class).getData(),
                                        document.getBoolean("blackmarket"),
                                        document.getDouble("priceBuyer"),
                                        document.getInteger("priceSeller"),
                                        document.getString("buyer"),
                                        document.getString("seller"),
                                        Instant.ofEpochMilli(document.getDate("date").getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                                ), true))
                        .collect(Collectors.toList())
                );

        return buyerTransactionsFuture.thenCombine(sellerTransactionsFuture, (buyerTransactions, sellerTransactions) -> {
            List<Pair<TransactionData, Boolean>> allTransactions = new ArrayList<>();
            allTransactions.addAll(buyerTransactions);
            allTransactions.addAll(sellerTransactions);

            allTransactions.sort((pair1, pair2) -> pair2.getLeft().getDate().compareTo(pair1.getLeft().getDate()));

            return allTransactions;
        });
    }
}
