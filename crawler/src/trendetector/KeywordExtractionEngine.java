package trendetector;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;
import kr.co.shineware.util.common.model.Pair;

import org.bson.Document;
import org.jsoup.Jsoup;

import trendetector.mongodb.MongoDB;
import trendetector.nlp.ExtractingIndexTerms;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;


public class KeywordExtractionEngine {	
	
	private static byte getStatus(boolean subject, boolean mainkey, String form) {
		byte status = 0x00;
		
		if (subject) {
			status |= (1 << 0);
		}
		
		if (mainkey) {
			status |= (1 << 1);
		}
		
		if (form.indexOf("NNP") != -1) {
			status |= (1 << 2);
		}
		
		if (form.charAt(form.length() - 1) == 'O') {
			status |= (1 << 3);
		}
		
		return status;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException {
		Komoran komoran = new Komoran("crawler/models-full/");
		komoran.setUserDic("crawler/models-full/dic.user");
		komoran.setFWDic("crawler/models-full/fwd.user");
		MongoDatabase db = MongoDB.create();
		
		Document where = new Document();
		Document whereContents = new Document();
		whereContents.append("$exists", true);
		whereContents.append("$not", new Document("$eq", false));
		
		where.append("contents", whereContents);
		where.append("keywords", new Document("$exists", false));
		
		Document orderby = new Document("date", 1);

		System.out.println(new Date());
		while (true) {
			try {
				FindIterable<Document> iter = 
						db.getCollection("article").find(where).sort(orderby).limit(100);
				
				if (!iter.iterator().hasNext()) {
					System.out.println(new Date() + "\t[SLEEP] ...5000ms");
					Thread.sleep(5000);
					continue;
				}
				
				iter.forEach( (Document doc) -> {
					String subject = doc.getString("subject");
					String contents = doc.getString("contents");

					HashMap<String, Double> hashMap = new HashMap<String, Double>();
					List<Pair<String, Byte>> words = new ArrayList<Pair<String, Byte>>();
					
					try {
						contents = Jsoup.parse(contents).text();
						contents = contents.replaceAll("[\\n|\\r|(|)|\\[|\\]|{|}|'|“|”|\"|`|.|,]", " @ ");
						subject = subject.replaceAll("[\\n|\\r|(|)|\\[|\\]|{|}|'|“|”|\"|`|.|,]", " @ ");
						List<List<Pair<String, String>>> result = null;
						
						result = komoran.analyze(subject);
						for (List<Pair<String, String>> eojeolResult : result) {
							Pair<List<Pair<String, String>>, List<Pair<String, String>>> keys
								= ExtractingIndexTerms.getKeywords(eojeolResult);
							
							for (Pair<String, String> mainKeyword : keys.getFirst()) {
								words.add(new Pair<String, Byte>(
										mainKeyword.getFirst().trim(), 
										getStatus(true, true, mainKeyword.getSecond())
								));
							}
							
							for (Pair<String, String> subKeyword : keys.getSecond()) {
								words.add(new Pair<String, Byte>(
										subKeyword.getFirst().trim(), 
										getStatus(true, false, subKeyword.getSecond())
								));
							}
						}
						
						if (contents.length() > 30000) {
							contents = contents.substring(0, 30000);
						}
						
						result = komoran.analyze(contents);
						for (List<Pair<String, String>> eojeolResult : result) {
							Pair<List<Pair<String, String>>, List<Pair<String, String>>> keys
								= ExtractingIndexTerms.getKeywords(eojeolResult);
							
							for (Pair<String, String> mainKeyword : keys.getFirst()) {
								words.add(new Pair<String, Byte>(
										mainKeyword.getFirst().trim(), 
										getStatus(false, true, mainKeyword.getSecond())
								));
							}
							
							for (Pair<String, String> subKeyword : keys.getSecond()) {
								words.add(new Pair<String, Byte>(
										subKeyword.getFirst().trim(), 
										getStatus(false, false, subKeyword.getSecond())
								));
							}
						}
						
						double tfmax = 0;
						int totalwordcnt = words.size();
						for (Pair<String, Byte> word : words) {
							double tf = (double)1 / (double)totalwordcnt;
							byte status = word.getSecond();
							byte check = 0x01;
							
							/* check subject */
							if ((status & check) > 0) {
								tf *= 4;
							}
							
							/* check main key */
							check <<= 1;
							if ((status & check) > 0) {
								tf *= 3;
							}
							
							/* check NNP */
							check <<= 1;
							if ((status & check) > 0) {
								tf *= 2;
							}
							
							Double before = hashMap.get(word.getFirst());
							
							if (before != null) {
								tf = tf + before;
							}
							
							if (tfmax < tf) {
								tfmax = tf;
							}
							
							hashMap.put(word.getFirst(), tf);
						}
						
						List<Document> keywords = new ArrayList<Document>();
						Iterator<String> keys = hashMap.keySet().iterator();
						
						while (keys.hasNext()) {
							String key = keys.next();
							Document keyword = new Document();
							keyword.append("keyword", key);
							keyword.append("tf", hashMap.get(key) / tfmax);
							keywords.add(keyword);
						}
						
						db.getCollection("article").updateOne(
							new Document("_id", doc.getObjectId("_id")),
							new Document("$set", new Document("keywords", keywords))
						);
						System.out.println(new Date() + "\t[DONE] " + doc.getObjectId("_id"));
						
					} catch (Exception e) {
						e.printStackTrace();
						db.getCollection("article").updateOne(
								new Document("_id", doc.getObjectId("_id")),
								new Document("$set", new Document("keywords", false))
							);
						System.out.println(new Date() + "\t[FAIL] " + doc.getObjectId("_id"));
					}
				});
					
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(5000);
			}
		}
	}

}
