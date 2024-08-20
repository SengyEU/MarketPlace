package cz.sengycraft.marketplace.items;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class ItemManager {

    private static ItemManager instance;

    public static ItemManager getInstance() {
        if (instance == null) instance = new ItemManager();

        return instance;
    }

    DatabaseManager databaseManager = DatabaseManager.getInstance();
    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    YamlDocument config = configurationManager.getConfiguration("config");
    String itemsCollectionName = config.getString("database.items-collection-name");

    public void storeItem(ItemData itemData) {
        Document document = new Document("item", itemData.getItem());
        document.append("price", itemData.getPrice());
        document.append("seller", itemData.getSeller());

        databaseManager.insertDocument(itemsCollectionName, document);
    }

    public List<ItemData> getItems() {
        return databaseManager.getAllDocuments(itemsCollectionName).stream()
                .map(document -> new ItemData(
                        document.getObjectId("_id").toHexString(),
                        document.get("item", org.bson.types.Binary.class).getData(),
                        document.getInteger("price"),
                        document.getString("seller")))
                .collect(Collectors.toList());
    }


}
