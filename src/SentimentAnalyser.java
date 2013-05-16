import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class SentimentAnalyser {
	public final int[] sentimentPoles = {29, 40, 41, 42, 50 };
	public final String[] sentimentNames = {"Sexy", "Violence", "Positive", "Negative", "Uncertain" };
	public final String ERROR = "Server error!";
	
	private final String APIURL = "https://ethersource.gavagai.se/ethersource/rest/textPolarization?apiKey=zeFAXe8pl0et-MwQ&";
	
	
	public double[] getSentiment (Utterance u){
		String cleanString = u.text.replaceAll ("\\#|\\'|\\.|\\!|\\r|\\n|\\r\\n", "");
		String sentimentJSON = getSentimentJSON (cleanString);
		if (sentimentJSON.equals (ERROR)){
			System.err.println ("ERROR: "+u.text);
			return null;
		}
		return parseJSON (sentimentJSON);
	}
	
	private String getSentimentJSON (String query){
		StringBuilder urlString = new StringBuilder (APIURL);
		String text = query.replaceAll (" ", "%20");
		urlString.append ("textFragment=" + text + "&");
		urlString.append ("wordSpaceId=1&");
		for (int i=0;i<sentimentPoles.length;i++){
			urlString.append ("poleId="+sentimentPoles[i]+"&");
		}
		urlString.deleteCharAt (urlString.length ()-1);
		String response = "";
		try {
			URL URL = new URL (urlString.toString ());
			HttpsURLConnection req = (HttpsURLConnection)URL.openConnection ();
			req.setRequestMethod ("GET");
			Authenticator.setDefault (new Authenticator (){
				protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication ("irkurs", "v4lh4ll4v563N".toCharArray());
			    }
			});
			req.setDoInput (true);

			InputStream is = req.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			response = reader.readLine ();
			reader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR;
		}
		return response;
	}
	
	private double[] parseJSON (String JSON){
		double[] values = new double[sentimentPoles.length];
		for (int i=0;i<sentimentPoles.length;i++){
			//["Violence:0.0","Sexy:2.0"]
			String sent = sentimentNames[i];
			int startIndex = JSON.indexOf (sent)+sent.length ()+1;
			String value = JSON.substring (startIndex, startIndex+3);
			values[i] = Double.parseDouble (value);
		}
		return values;
	}

}
