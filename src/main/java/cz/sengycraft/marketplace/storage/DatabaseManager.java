package cz.sengycraft.marketplace.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static DatabaseManager instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void init(String uri, String databaseName) {
        Logger logger = Logger.getLogger("org.mongodb.driver");
        logger.setLevel(Level.SEVERE);
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(databaseName);
    }

    public CompletableFuture<Void> insertDocument(String collectionName, Document document) {
        return CompletableFuture.runAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.insertOne(document);
        });
    }

    public CompletableFuture<Document> findDocument(String collectionName, String key, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            return collection.find(Filters.eq(key, value)).first();
        });
    }

    public CompletableFuture<List<Document>> getAllDocuments(String collectionName) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            List<Document> documents = new ArrayList<>();
            for (Document document : collection.find()) {
                documents.add(document);
            }
            return documents;
        });
    }

    public CompletableFuture<List<Document>> getAllDocuments(String collectionName, String key, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            List<Document> documents = new ArrayList<>();
            for (Document document : collection.find(Filters.eq(key, value))) {
                documents.add(document);
            }
            return documents;
        });
    }

    public CompletableFuture<Void> deleteDocument(String collectionName, String key, Object value) {
        return CompletableFuture.runAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.deleteOne(Filters.eq(key, value));
        });
    }

    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            mongoClient.close();
        });
    }
}
