import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javaFlacEncoder.FLAC_FileEncoder;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class SpeechToTextInterface {
	SpeechToText stt = new SpeechToText ();
	SentimentAnalyser analyser = new SentimentAnalyser ();
	private AudioRecorder audio = new AudioRecorder ();

	JEditorPane results;

	JPanel p;

	ArrayList<String> brandStrings = new ArrayList<String>();
	JTextField brandInput;

	private JButton stopRecording;
	private JButton record;
	private JButton audioFolder;
	private JTextArea resultArea;

	private JTextField before_input;
	private JTextField after_input;

	private JLabel status;

	JFrame frame;

	public static void main(String[] args){
		File audioDir = new File ("recordings");
		if (!audioDir.exists ())
			audioDir.mkdirs ();
		new SpeechToTextInterface ().buildGUI ();
	}

	private void buildGUI () {
		frame = new JFrame ();
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setSize (600, 800);
		record = new JButton ("Record");
		record.setIcon (getRecordingIcon ());
		record.addActionListener (new ActionListener (){

			@Override
			public void actionPerformed (ActionEvent e) {
				startRecording ();
			}

		});
		stopRecording = new JButton ("Stop recording");
		stopRecording.addActionListener (new ActionListener (){

			@Override
			public void actionPerformed (ActionEvent e) {
				stopRecording ();
			}
		});
		audioFolder = new JButton ("Analyse folder");
		audioFolder.addActionListener (new ActionListener (){
			@Override
			public void actionPerformed (ActionEvent e) {
				new Thread (new FolderAnalyser ()).start ();
			}

		});
		resultArea = new JTextArea ();
		resultArea.setLineWrap (true);
		resultArea.setWrapStyleWord (true);
		JScrollPane scrollPane = new JScrollPane (resultArea);
		scrollPane.setPreferredSize (new Dimension (400,400));
		brandInput = new JTextField("");


	

		JButton analyse_button = new JButton("Analyse");
		analyse_button.addActionListener (new ActionListener (){
			@Override
			public void actionPerformed (ActionEvent e) {
				
				Thread thread = new Thread(new Runnable()
				{
				public void run()
				{
					analyse();
				}
				});
				
				thread.start();
			}
		});
		p = new JPanel (new GridBagLayout ());
		GridBagConstraints c = new GridBagConstraints ();
		c.gridx = 0;
		c.gridy = 0;
		p.add (stopRecording, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		p.add(record, c);
		c.gridx++;
		p.add (audioFolder,c);

		c.gridx=0;
		c.gridy++;
		c.gridwidth =  3;
		p.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		c.gridy++;



		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;


		JLabel l = new JLabel("<html> Brands to analyse <br> (not case sensitive):");
		l.setHorizontalAlignment( JLabel.CENTER );
		p.add(l,c);


		c.gridwidth = 2;
		c.gridx++;
		p.add(brandInput,c);


		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		p.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		c.gridy++;
		JPanel p2 = new JPanel();
		before_input = new JTextField(2);
		before_input.setText("3");
		after_input = new JTextField(2);
		after_input.setText("3");
		p2.add(new JLabel("Use "));
		p2.add(before_input);
		p2.add(new JLabel("words before the mention and "));
		p2.add(after_input);
		p2.add(new JLabel(" words after."));
		p.add(p2,c);

		c.gridy++;
		p.add (analyse_button, c);

		c.gridy++;


		c.gridwidth = 2;
		status = new JLabel(" ");
		Border paddingBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		status.setHorizontalAlignment( JLabel.CENTER );
		status.setBorder(paddingBorder);
		p.add (status, c);
		c.gridwidth = 1;
		c.gridx=2;

		JButton clear = new JButton("Clear");
		clear.addActionListener (new ActionListener (){
			@Override
			public void actionPerformed (ActionEvent e) {
				resultArea.setText("");
			}
		});

		p.add(clear,c);
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 3;
		p.add (scrollPane, c);


		results = new JEditorPane();
		results.setContentType("text/html");
		results.setEditable(false);
		JScrollPane js = new JScrollPane(results);

		frame.setLayout(new GridLayout(1,2));
		frame.add(p);
		frame.add(js);
		frame.pack ();
		frame.setVisible (true);
	}
	
	static ArrayList<BrandMentions> search2(String corpus, ArrayList<String> searchNameList, int before, int after){
		String[] words = corpus.split(" ");
		ArrayList<BrandMentions> brandMentions = new ArrayList<BrandMentions> ();
		for (String brand : searchNameList){
			brand = brand.trim();
			BrandMentions brandMention= new BrandMentions(brand);
			int index = 0;
			for (String word : words){
				if (word.toLowerCase ().equals (brand.toLowerCase ()))
					brandMention.addSentence (getContext (words, index, before, after));
				index++;
			}
			brandMentions.add (brandMention);
		}
		return brandMentions;
		
	}

	private static String getContext (String[] words, int index, int before, int after) {
		StringBuilder s = new StringBuilder ();
		int wordIndex = (index-before);
		if (wordIndex < 0)
			wordIndex = 0;
		for (int i=0;i<after+before+1;i++,wordIndex++){
			s.append (words[wordIndex] + " ");
		}
		return s.toString ();
	}

	static ArrayList<BrandMentions> search(String corpus, ArrayList<String> searchNameList, int before, int after){
		ArrayList<BrandMentions> brand_mentions_list  = new ArrayList<BrandMentions>();
		String[] wordCorpusVector = corpus.split(" ");
		for (String searchName : searchNameList){
			
			searchName = searchName.trim();
			BrandMentions brandmention= new BrandMentions(searchName);
			String[] searchNameVector = searchName.split(" ");
			nextPosition: for(int i = 0 ; i< wordCorpusVector.length-searchNameVector.length+1; i++){
				for(int j = 0 ; j<searchNameVector.length; j++){
					//System.out.println("i:" +i+ " j:"+j + " SearchName: " + searchNameVector[j] + " Wordcorpus: " + wordCorpusVector[j+i]);
					if(searchNameVector[j].toLowerCase().equals(wordCorpusVector[j+i].toLowerCase())){
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



	private class FolderAnalyser implements Runnable {
		@Override
		public void run () {
			JFileChooser fc = new JFileChooser ();
			fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
			int option = fc.showOpenDialog (audioFolder);
			if(option != JFileChooser.APPROVE_OPTION)
				return;
			File dir = fc.getSelectedFile();
			for (File f : dir.listFiles ()){
				if (f.isFile ()){
					if (f.getName ().contains ("flac"))
						processFLACAudioFile (f);
				}
			}
		}
	}

	public void addBrandsToList(){
		String input = brandInput.getText();
		//input = input.toLowerCase();
		brandStrings.clear();
		for(String b : input.split(",")){
			if(!b.equals(""))
				b = b.trim();
				brandStrings.add(b);
		}
	}

	public void analyse(){
		p.revalidate();
		status.revalidate();
		status.repaint();
		p.repaint();
		
		addBrandsToList();
		String corpus = resultArea.getText();
		
		if(corpus.equals("")){
			JOptionPane.showMessageDialog(frame, "No input. Record voice, select a sound-file or paste text in the resultArea below the buttons.");
			return;
		}
		else if(brandStrings.isEmpty()){
			JOptionPane.showMessageDialog(frame, "No brands added. Add them in the 'Brands to Analyse:'-field separated with commas like this: Coca Cola,Apple,Ford,Sony");
			return;
		}
		int bef;
		int aft;
		try{
			bef = Integer.parseInt(before_input.getText());
			aft = Integer.parseInt(after_input.getText());
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(frame, "Words before and after must be numbers.");
			return;
		}
		if(bef<0 || aft <0){
			JOptionPane.showMessageDialog(frame, "Words before and after must be positive numbers.");
			return;
		}
		corpus = corpus.replaceAll ("\\.|\\,|\\'|\\!", "");
		ArrayList<BrandMentions> bmlist =  search(corpus, brandStrings, bef , aft);
		String text = "<html><font face=\"arial\" size=\"4\">";
		

		int counter = 0;
		for(BrandMentions b: bmlist){
			double[] meanVec = new double[analyser.sentimentNames.length];
			double numberOfmentions = b.getSentenceList().size();
			text += "<table border=\"1\" ><tr><td> Brand name </td><td>" + b.getBrand() + "</td></tr><tr>";
			text += "<td>" + "Mentions" + "</td>";
			for (int i=0;i< analyser.sentimentNames.length;i++){
				text += "<td>" + analyser.sentimentNames[i] + "</td>"; 
			}
			text += "</tr>";
			for (String s :b.getSentenceList()){
				text+= "<tr><td>" + s + "</td>";
				System.out.println(s);
				

				
				s = s.trim();
				
				//s = s.replaceAll("[^a-zA-Z0-9 ]", "");
				counter++;
				status.setText("Analysing " + b.getBrand() + "... " + counter + "/" + b.getSentenceList().size());
				Utterance u = new Utterance(s,1);
				double[] sentiments = analyser.getSentiment (u);


				try {
					Thread.sleep(5000);
				} catch(InterruptedException e) {
				}



				for (int i=0;i< analyser.sentimentNames.length;i++){
					text += "<td>" +sentiments[i]+"</td>";
					meanVec[i] += sentiments[i];
				}
				text += "</tr>";
			}

			if(b.getSentenceList().isEmpty()){
				text+="<tr><td>No mentions</td>";
			}
			else{
				text+="<tr><td>Total average</td>";
				for(int i=0;i< analyser.sentimentNames.length;i++){
					if(numberOfmentions!=0){
						meanVec[i] = meanVec[i]/numberOfmentions;
					}
					text+= "<td>"+meanVec[i]+"</td>";
				}
			}
			text += "</tr></table><br><br>";
		}
		text += "</html>";
		results.setText(text);
		
		status.setText("Analysing finished!");
	}

	public void stopRecording () {
		audio.stop ();
		new Thread (new PostProcesser ()).start ();
	}

	public void startRecording () {
		record.setEnabled(false);
		stopRecording.setEnabled(true);
		audio.captureAudio();
	}

	private void processFLACAudioFile (File f){
		String analysing = "Analysing file "+f.getName ()+"...";
		status.setText(analysing);


		//Old:
		//resultArea.append (analysing);


		Utterance u = stt.getUtterance (f);
		if (u == null){
			System.err.println ("Did not get an utterance");
			status.setText("Did not get an utterance");
			return;
		}
		else{
			status.setText("Utterance found! Confidence " + u.confidence);
		}


		//New-----

		resultArea.append("\n----------------------------\n" + u.text);


		//Utterance totalUtterance = new Utterance(resultArea.getText(), )
		//double[] totalSentiments = analyser.getSentiment (status.getText());


		//Old----

		/*double[] sentiments = analyser.getSentiment (u);
		if (sentiments == null){
			resultArea.append (analyser.ERROR+"\n");
			return;
		}
		resultArea.setText (resultArea.getText ().substring (0, resultArea.getText ().length ()-analysing.length ()));
		StringBuilder text = new StringBuilder (u.text+"\n-----------------\nSENTIMENTS:\n");
		for (int i=0;i< analyser.sentimentNames.length;i++){
			text.append (analyser.sentimentNames[i]+": "+sentiments[i]+"\n"); 
		}
		text.append ("----------------------------\n");
		resultArea.append (text.toString ());*/

	}




	private class PostProcesser implements Runnable{
		@Override
		public void run () {
			stopRecording.setEnabled(false);
			FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
			flacEncoder.encode(audio.outputWav, audio.outputFLAC);

			processFLACAudioFile (audio.outputFLAC);
			record.setEnabled(true);
			audio.outputWav.deleteOnExit ();
		}
	}

	private Icon getRecordingIcon (){
		BufferedImage img = new BufferedImage (16,16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics ();
		g.setColor (Color.RED);
		g.fillOval (2, 2, 13, 13);
		g.setColor (Color.BLACK);
		g.drawOval (2, 2, 13, 13);
		g.dispose ();
		ImageIcon icon = new ImageIcon (img);
		img.flush ();
		return icon;
	}
}
