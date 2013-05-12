import java.util.ArrayList;


public class TestingSearch {

	public static void main(String[] args) {

		String c = "coca cola ab om mannatu coca cola ab hej hoj hu ji ni ti coca cola ab la om mannatu hej haj om mannatu coca cola ab";


		String sw = "coca cola ab";
		ArrayList<String> snl = new ArrayList<String> ();
		snl.add(sw);
		ArrayList<BrandMentions> bm = search(c, snl, 3, 3);
		
		for(BrandMentions b: bm){
			System.out.println("Brand name: " + b.getBrand());
			System.out.println("Mentions:");
			for (String s :b.getSentenceList()){
				System.out.println(s);
			}
		}

	}


	static ArrayList<BrandMentions> search(String corpus, ArrayList<String> searchNameList, int before, int after){
		ArrayList<BrandMentions> brand_mentions_list  = new ArrayList<BrandMentions>();
		String[] wordCorpusVector = corpus.split(" ");
		for (String searchName : searchNameList){
			BrandMentions brandmention= new BrandMentions(searchName);
			String[] searchNameVector = searchName.split(" ");
			nextPosition: for(int i = 0 ; i< wordCorpusVector.length-searchNameVector.length+1; i++){
				for(int j = 0 ; j<searchNameVector.length; j++){
					//System.out.println("i:" +i+ " j:"+j + " SearchName: " + searchNameVector[j] + " Wordcorpus: " + wordCorpusVector[j+i]);
					if(searchNameVector[j].equals(wordCorpusVector[j+i])){
						continue;
					}
					else{
						continue nextPosition;
					}
				}
				String sentence = "";
				for(int j = 0-before ; j<searchNameVector.length+after; j++){
					int s = i+j;
					
					if(s<0 || s>=wordCorpusVector.length){
						continue;
					}
					sentence+=wordCorpusVector[s] + " ";
				}
				sentence = sentence.substring(0, sentence.length()-1);
				brandmention.addSentence(sentence);
				
			}
			brand_mentions_list.add(brandmention);
		}
		return brand_mentions_list;
	}

	static void printVec(String[] vec){
		for (String s: vec)
			System.out.println(s);
	}


}
