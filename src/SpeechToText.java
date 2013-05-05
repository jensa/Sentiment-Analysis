import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class SpeechToText {
	
	static class Utterance{
		String text;
		double confidence;
		
		public Utterance (String t, double c){
			text = t;
			confidence = c;
		}
		
	}
	
	public Utterance getUtterance (String filename){
		InputStream is;
		try {
			is = new FileInputStream (filename);
			return getUtterance (is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Utterance getUtterance (InputStream audioInput) throws IOException{
		URL apiURL = new URL("https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=en-US");
		
		HttpsURLConnection req = (HttpsURLConnection)apiURL.openConnection ();
		req.setRequestMethod ("POST");
		req.setRequestProperty ("Content-type", "audio/x-flac; rate=44100");
		//req.setRequestProperty ("lang", "en");
		req.setDoInput (true);
		req.setDoOutput (true);
		 //Send request
	      OutputStream wr = req.getOutputStream ();
	      byte[] bytes = new byte[256];
	      while ((audioInput.read (bytes, 0, 256)) != -1){
	    	  wr.write (bytes);
	      }
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = req.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      String responseString = response.toString ();
	      String utterance = responseString.substring (responseString.indexOf ("utterance:")+3, responseString.indexOf ("confidence")-3);
	      String confidence = responseString.substring (responseString.indexOf ("confidence:")+2, responseString.indexOf ("}]}"));
	      double conf = Double.parseDouble (confidence);
	      return (new Utterance (utterance, conf));
	}

}
