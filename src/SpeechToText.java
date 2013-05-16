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

	private final int MAX_BYTES_IN_REQUEST = 200000; //256 kb is max, leave some room
	private long fileSize;
	
	public Utterance getUtterance (File file){
		
		return getUtterance (file, AudioFileFinderOuter.figureOutFLACBitrate (file));
	}

	public Utterance getUtterance (File file, long rate){
		InputStream is;
		fileSize = file.length ();
		try {
			is = new FileInputStream (file);
			return getUtterance (is, rate);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public Utterance getUtterance (InputStream audioInput, long rate) throws IOException{
		
		int numChunks = (int) Math.floor ((fileSize / MAX_BYTES_IN_REQUEST))+1;
		double[] confidences = new double[numChunks];
		StringBuilder sb = new StringBuilder ();
		for (int i=0;i<numChunks;i++){
			URL apiURL = new URL("https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=en-US");
			HttpsURLConnection req = (HttpsURLConnection)apiURL.openConnection ();
			req.setRequestMethod ("POST");
			req.setRequestProperty ("Content-type", "audio/x-flac; rate="+rate);
			req.setDoInput (true);
			req.setDoOutput (true);
			OutputStream wr = req.getOutputStream ();
			byte[] bytes = new byte[256];
			int bytesRead = 0;
			while (bytesRead <= MAX_BYTES_IN_REQUEST && (audioInput.read (bytes, 0, 256)) != -1){
				wr.write (bytes);
				bytesRead += 256;
			}
			wr.flush ();
			wr.close ();
			InputStream is = req.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String responseString = reader.readLine ();
			reader.close ();
			if (!responseString.contains ("utterance")){
				continue;
			}
			String utterance = responseString.substring (responseString.indexOf ("utterance")+12, responseString.indexOf ("confidence")-3);
			String confidence = responseString.substring (responseString.indexOf ("confidence")+12, responseString.indexOf ("}]}")-1);
			confidences[i] = Double.parseDouble (confidence);
			sb.append (" "+utterance);
			System.out.println (utterance);
		}
		if (sb.length () == 0)
			return null;
		sb.deleteCharAt (0);
		double confidence = 0;
		for (int i=0;i<numChunks;i++){
			confidence += confidences[i];
		}
		confidence = confidence / numChunks;
		return (new Utterance (sb.toString (), confidence));
	}

}
