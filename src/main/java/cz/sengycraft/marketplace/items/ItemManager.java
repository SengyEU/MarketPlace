package cz.sengycraft.marketplace.items;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ItemManager {

    private static ItemManager instance;

    public static ItemManager getInstance() {
        if (instance == null) instance = new ItemManager();

        return instance;
    }

    private final DatabaseManager databaseManager = DatabaseManager.getInstance();
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private final YamlDocument config = configurationManager.getConfiguration("config");
    private final String itemsCollectionName = config.getString("database.items-collection-name");

    public CompletableFuture<Void> storeItem(ItemData itemData) {
        Document document = new Document("item", itemData.getItem())
                .append("price", itemData.getPrice())
                .append("seller", itemData.getSeller());

        // Insert the document asynchronously
        return databaseManager.insertDocument(itemsCollectionName, document);
    }

    public CompletableFuture<List<ItemData>> getItems() {
        return databaseManager.getAllDocuments(itemsCollectionName)
                .thenApply(documents -> documents.stream()
                        .map(document -> new ItemData(
                                document.getObjectId("_id").toHexString(),
                                document.get("item", org.bson.types.Binary.class).getData(),
                                document.getInteger("price"),
                                document.getString("seller")))
                        .collect(Collectors.toList()));
    }
}
