package trendetector.nlp;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.util.common.model.Pair;

public class ExtractingIndexTerms {
	public static Pair<List<Pair<String, String>>, List<Pair<String, String>>> getKeywords(List<Pair<String, String>> eojeolResult) {
		/* 알수없는 단어(NA)와 외국어단어(SL)는 고유명사로 취급 */
		for (Pair<String, String> wordMorph : eojeolResult) {
			if (wordMorph.getSecond().equals("NA") || wordMorph.getSecond().equals("SL")) {
				wordMorph.setSecond("NNP");
			}
		}
		
		/* 중간중간 파생되는 키워드를 저장 */
		List<Pair<String, String>> subkeywords = new ArrayList<Pair<String,String>>();
		
		for (int stage = 1; stage <= 7; stage++) {
			List<Pair<String, String>> combine = new ArrayList<Pair<String,String>>();
			combine.add(eojeolResult.get(0));
			
			for (int i = 1; i < eojeolResult.size(); i++) {
				Pair<String, String> currentElement = eojeolResult.get(i);
				Pair<String, String> lastElement = combine.get(combine.size() - 1);
				
				/* SL + SN combine 
				 * ex) galaxy6, 5s
				 */
				if (stage == 1) {
					if ((lastElement.getSecond().equals("SL") && currentElement.getSecond().equals("SN")) 
						|| (lastElement.getSecond().equals("SN") && currentElement.getSecond().equals("SL"))) {
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNP");
						continue;
					}
				}
				
				/* SN+S->SN, SN+NR->SN, SN+NNB->NNG combine 
				 * ex) 6!, 27%, 6개, 1997년, 1천2백, 팔십팔
				 */ 
				else if (stage == 2) {
					if ((lastElement.getSecond().equals("SN") && currentElement.getSecond().indexOf("S") == 0)
						|| (lastElement.getSecond().equals("SN") && currentElement.getSecond().equals("NR"))) {
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("SN");
						continue;
					}
					else if (lastElement.getSecond().equals("SN") && currentElement.getSecond().equals("NNB")) {
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNG");
						continue;
					}
				}
				
				/* S + N combine 
				 * ex) 486컴퓨터, 아이폰6+
				 */
				else if (stage == 3) {
					if (lastElement.getSecond().indexOf("S") == 0 && currentElement.getSecond().indexOf("NN") == 0) {
						subkeywords.add(new Pair<String, String>(currentElement.getFirst(), currentElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNP");
						continue;
					}
					else if (lastElement.getSecond().indexOf("NN") == 0 && currentElement.getSecond().indexOf("S") == 0) {
						subkeywords.add(new Pair<String, String>(lastElement.getFirst(), lastElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNP");
						continue;
					}
				}
				
				/* N + X combine
				 * ex)  확장(NNG)+성(XSN)->확장성(NNG), 사랑(NNG)+하(XSV)->사랑하(VV)
				 */
				else if (stage == 4) {
					if (lastElement.getSecond().indexOf("NN") == 0 && currentElement.getSecond().equals("XSN")) {
						subkeywords.add(new Pair<String, String>(lastElement.getFirst(), lastElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond(lastElement.getSecond());
						continue;
					}
					else if (lastElement.getSecond().indexOf("NN") == 0 && currentElement.getSecond().equals("XSV")) {
						subkeywords.add(new Pair<String, String>(lastElement.getFirst(), lastElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("VV");
						continue;
					}
				}
				
				/* V + VX, V + E combine
				 * ex) 사랑하(V)+기(ETN)->사랑하기(NNG)
				 * 사랑하(V)+고(EC)->사랑하고(VV)
				 * 사랑하고(VV)+있(VX)->사랑하고있(VV)
				 * 사랑하고있(VV)+는(ETM)->사랑하고있는(VV)
				 */
				else if (stage == 5) {
					if (lastElement.getSecond().equals("VV") && currentElement.getSecond().equals("ETN")) {
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNG");
						continue;
					}
					else if (lastElement.getSecond().equals("VV") && currentElement.getSecond().indexOf("E") == 0) {
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("VV");
						continue;
					}
					else if (lastElement.getSecond().equals("VV") && currentElement.getSecond().equals("VX")) {
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("VV");
						continue;
					}
				}
				
				/* V + N combine
				 * ex) 사랑하고있는(V)+여자(NNG)->사랑하고있는여자(NNP) 
				 */
				else if (stage == 6) {
					if (lastElement.getSecond().equals("VV") && currentElement.getSecond().indexOf("NN") == 0) {
						subkeywords.add(new Pair<String, String>(currentElement.getFirst(), currentElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNP");
						continue;
					}
					else if (lastElement.getSecond().indexOf("NN") == 0 && currentElement.getSecond().equals("VV")) {
						subkeywords.add(new Pair<String, String>(lastElement.getFirst(), lastElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("VV");
						continue;
					}
				}
				
				/* N + N combine
				 */
				else if (stage == 7) {
					if (lastElement.getSecond().indexOf("NN") == 0 && currentElement.getSecond().indexOf("NN") == 0) {
						subkeywords.add(new Pair<String, String>(currentElement.getFirst(), currentElement.getSecond()));
						subkeywords.add(new Pair<String, String>(lastElement.getFirst(), lastElement.getSecond()));
						lastElement.setFirst(lastElement.getFirst() + currentElement.getFirst());
						lastElement.setSecond("NNP");
						continue;
					}
				}
				
				combine.add(currentElement);
			}
			
			eojeolResult = combine;
//			System.out.println("stage" + stage + ": " + eojeolResult);
		}
		
		List<Pair<String, String>> keywords = new ArrayList<Pair<String,String>>();
		for (Pair<String, String> keyword : eojeolResult) {
			if (keyword.getSecond().equals("NNP") 
					|| keyword.getSecond().equals("NNG")) {
				keywords.add(keyword);
			}
		}
		
		if (keywords.size() == eojeolResult.size()) {
			for (Pair<String, String> keyword : keywords) {
				keyword.setSecond(keyword.getSecond() + "O");
			}
		}
		
		return new Pair<List<Pair<String, String>>, List<Pair<String, String>>>(keywords, subkeywords);
	}
}
