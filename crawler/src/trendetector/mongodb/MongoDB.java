package trendetector.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDB {
	
	@SuppressWarnings("resource")
	public static MongoDatabase create(String host, int port, String database){
		return  new MongoClient(host, port).getDatabase(database);
	}
}
