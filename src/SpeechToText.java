import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class SpeechToText {
	
	public Utterance getUtterance (File file){
		InputStream is;
		try {
			is = new FileInputStream (file);
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

	      InputStream is = req.getInputStream();
	      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	      String responseString = reader.readLine ();
	      reader.close ();
	      if (!responseString.contains ("utterance")){
	    	  return null;
	      }
	      String utterance = responseString.substring (responseString.indexOf ("utterance")+12, responseString.indexOf ("confidence")-3);
	      String confidence = responseString.substring (responseString.indexOf ("confidence")+12, responseString.indexOf ("}]}")-1);
	      double conf = Double.parseDouble (confidence);
	      return (new Utterance (utterance, conf));
	}

}
