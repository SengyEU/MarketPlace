package cz.sengycraft.marketplace.storage;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import cz.sengycraft.marketplace.MarketPlacePlugin;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private MarketPlacePlugin plugin;
    private static DatabaseManager instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void setPlugin(MarketPlacePlugin plugin){
        this.plugin = plugin;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();

        return instance;
    }

    public void init(String uri, String databaseName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Logger logger = Logger.getLogger("org.mongodb.driver");
            logger.setLevel(Level.SEVERE);
            ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri)).serverApi(serverApi).build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
        });
    }

    public void insertDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
    }

    public Document findDocument(String collectionName, String key, Object value) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.find(Filters.eq(key, value)).first();
    }

    public List<Document> getAllDocuments(String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<Document> documents = new ArrayList<>();

        for (Document document : collection.find()) {
            documents.add(document);
        }

        return documents;
    }

    public void updateDocument(String collectionName, String key, String value, String updateKey, String updateValue) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Bson filter = Filters.eq(key, value);
        Bson updateOperation = Updates.set(updateKey, updateValue);
        collection.updateOne(filter, updateOperation);
    }

    public void deleteDocument(String collectionName, String key, Object value) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(Filters.eq(key, value));
    }

    public void close() {
        mongoClient.close();
    }
}
