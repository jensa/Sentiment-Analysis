import java.util.ArrayList;


public class BrandMentions {

	private String brand;
	
	private ArrayList<String> sentence_list;
	
	BrandMentions(String brand){
		this.brand = brand;
		sentence_list = new ArrayList<String>();
	}
	
	public void addSentence(String sentence){
		sentence_list.add(sentence);
	}
	
	public ArrayList<String> getSentenceList(){
		return sentence_list;
	}
	
	public String getBrand(){
		return brand;
	}

}
