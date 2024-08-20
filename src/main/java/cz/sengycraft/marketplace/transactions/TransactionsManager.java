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

public class TransactionsManager {

    private static TransactionsManager instance;

    public static TransactionsManager getInstance() {
        if (instance == null) instance = new TransactionsManager();

        return instance;
    }

    DatabaseManager databaseManager = DatabaseManager.getInstance();
    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    YamlDocument config = configurationManager.getConfiguration("config");
    String transactionsCollectionName = config.getString("database.transactions-collection-name");

    public void storeTransaction(TransactionData transactionData) {
        Document document = new Document("item", transactionData.getItem());
        document.append("blackmarket", transactionData.isBlackmarket());
        document.append("priceBuyer", transactionData.getPriceBuyer());
        document.append("priceSeller", transactionData.getPriceSeller());
        document.append("buyer", transactionData.getBuyer());
        document.append("seller", transactionData.getSeller());
        document.append("date", transactionData.getDate());

        databaseManager.insertDocument(transactionsCollectionName, document);
    }

    public List<Pair<TransactionData, Boolean>> getTransactions(String playerName) {
        List<Pair<TransactionData, Boolean>> data = new ArrayList<>();

        databaseManager.getAllDocuments(transactionsCollectionName, "buyer", playerName).stream()
                .map(document -> new TransactionData(
                        document.get("item", org.bson.types.Binary.class).getData(),
                        document.getBoolean("blackmarket"),
                        document.getDouble("priceBuyer"),
                        document.getInteger("priceSeller"),
                        document.getString("buyer"),
                        document.getString("seller"),
                        Instant.ofEpochMilli(document.getDate("date").getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .forEach(transactionData -> data.add(new Pair<>(transactionData, false)));

        databaseManager.getAllDocuments(transactionsCollectionName, "seller", playerName).stream()
                .map(document -> new TransactionData(
                        document.get("item", org.bson.types.Binary.class).getData(),
                        document.getBoolean("blackmarket"),
                        document.getDouble("priceBuyer"),
                        document.getInteger("priceSeller"),
                        document.getString("buyer"),
                        document.getString("seller"),
                        Instant.ofEpochMilli(document.getDate("date").getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .forEach(transactionData -> data.add(new Pair<>(transactionData, true)));

        data.sort((pair1, pair2) -> pair2.getLeft().getDate().compareTo(pair1.getLeft().getDate()));

        return data;
    }

}
