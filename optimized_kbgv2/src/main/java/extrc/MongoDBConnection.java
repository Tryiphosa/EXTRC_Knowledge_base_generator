package extrc;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


public class MongoDBConnection {
    private static MongoClient mongoClient = null;
    
    public static MongoDatabase getDatabase(String dbName) {
        if (mongoClient == null) {
            
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            
        }
        return mongoClient.getDatabase(dbName);
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
