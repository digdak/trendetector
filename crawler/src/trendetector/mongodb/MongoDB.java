package trendetector.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDB {
	public static MongoDatabase db;
	
	static {
		db = MongoDB.create();
	}
	
	@SuppressWarnings("resource")
	public static MongoDatabase create(){
		return  new MongoClient("localhost", 27017).getDatabase("trendetector");
	}
}
