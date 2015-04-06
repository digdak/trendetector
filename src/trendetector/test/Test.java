package trendetector.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Test {

	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase  db = mongoClient.getDatabase("trendetector");
		MongoCollection<org.bson.Document> coll = db.getCollection("test");
	}

}
