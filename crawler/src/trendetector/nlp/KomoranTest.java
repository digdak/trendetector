package trendetector.nlp;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;
import kr.co.shineware.util.common.model.Pair;


public class KomoranTest {

	public static void main(String[] args) throws IOException {
		Komoran komoran = new Komoran("crawler/models-full/");
		
		Scanner scan = new Scanner(System.in);
		for (;;) {
			String str = scan.nextLine();
			System.out.println("[NLP] " + str);
			str = str.replaceAll("<", " <").replace(">", "> ");
			str = str.replaceAll("[\\n|\\r|(|)|\\[|\\]|{|}|'|“|”|\"|`|.]", " @ ");
			
			List<List<Pair<String, String>>> result = komoran.analyze(str);
			
			for (List<Pair<String, String>> eojeolResult : result) {
				System.out.println(eojeolResult);
				
				Pair<List<Pair<String, String>>, List<Pair<String, String>>> keys
					= ExtractingIndexTerms.getKeywords(eojeolResult);
				
				System.out.println("\tkeywords:");
				if (!keys.getFirst().isEmpty()) {
					System.out.println("\t" + keys.getFirst());
				}
				if (!keys.getSecond().isEmpty()) {
					System.out.println("\t\t" + keys.getSecond());
				}
	//			System.out.println();
			}
		}
	}
}
