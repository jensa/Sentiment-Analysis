import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javaFlacEncoder.FLAC_FileEncoder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class SpeechToTextInterface {
	JTextArea resultArea;
	SpeechToText stt;
	SentimentAnalyser analyser;

	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	private JButton stopRecording;
	private JButton record;
	
	private final String OUTPUT_WAV = "recordings\\audio.wav";
	private final String FLAC_FILE = "recordings\\audio.flac";

	public static void main(String[] args){
		File audioDir = new File ("recordings");
		if (!audioDir.exists ())
			audioDir.mkdirs ();
		new SpeechToTextInterface ().buildGUI ();
	}

	private void buildGUI () {
		stt = new SpeechToText ();
		analyser = new SentimentAnalyser ();
		JFrame frame = new JFrame ();
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
		resultArea = new JTextArea ();
		JScrollPane scrollPane = new JScrollPane (resultArea);
		scrollPane.setPreferredSize (new Dimension (400,400));
		JPanel p = new JPanel (new GridBagLayout ());
		GridBagConstraints c = new GridBagConstraints ();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		p.add (stopRecording, c);

		c.gridx++;
		p.add (record, c);

		c.gridy++;
		c.gridx--;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		p.add (scrollPane, c);

		frame.add (p);
		frame.pack ();
		frame.setVisible (true);


	}

	public void stopRecording () {
		record.setEnabled(true);
		stopRecording.setEnabled(false);
		targetDataLine.stop();
		targetDataLine.close();

		FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
		File inputFile = new File(OUTPUT_WAV);
		File outputFile = new File(FLAC_FILE);
		flacEncoder.encode(inputFile, outputFile);

		Utterance u = stt.getUtterance (FLAC_FILE);
		inputFile.delete ();
		double[] sentiments = analyser.getSentiment (u);
		StringBuilder text = new StringBuilder (resultArea.getText () + u.text+"\n-----------------\nSENTIMENTS:\n");
		for (int i=0;i< analyser.sentimentNames.length;i++){
			text.append (analyser.sentimentNames[i]+": "+sentiments[i]+"\n"); 
		}
		text.append ("----------------------------\n");
		resultArea.setText (text.toString ());

	}

	public void startRecording () {
		record.setEnabled(false);
		stopRecording.setEnabled(true);
		captureAudio();
	}

	private void captureAudio(){
		try{
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo =
					new DataLine.Info(
							TargetDataLine.class,
							audioFormat);
			targetDataLine = (TargetDataLine)
					AudioSystem.getLine(dataLineInfo);
			new CaptureThread().start();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private AudioFormat getAudioFormat(){
		float sampleRate = 44100.0F;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate,
				sampleSizeInBits,
				channels,
				signed,
				bigEndian);
	}

	class CaptureThread extends Thread{
		public void run(){
			AudioFileFormat.Type fileType = null;
			File audioFile = null;
			fileType = AudioFileFormat.Type.WAVE;
			audioFile = new File(OUTPUT_WAV);
			try{
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				AudioSystem.write(
						new AudioInputStream(targetDataLine),
						fileType,
						audioFile);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private Icon getRecordingIcon (){
		BufferedImage img = new BufferedImage (16,16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics ();
		g.setColor (Color.RED);
		g.fillOval (2, 2, 13, 13);
		g.dispose ();
		ImageIcon icon = new ImageIcon (img);
		img.flush ();
		return icon;
	}

}
