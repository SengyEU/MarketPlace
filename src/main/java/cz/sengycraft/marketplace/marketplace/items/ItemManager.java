package cz.sengycraft.marketplace.marketplace.items;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import cz.sengycraft.marketplace.storage.DatabaseManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bson.Document;

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

    public void storeItem(ItemData itemData){
        Document document = new Document("item", itemData.getItem());
        document.append("price", itemData.getPrice());
        document.append("seller", itemData.getSeller());

        databaseManager.insertDocument(itemsCollectionName, document);
    }

}
