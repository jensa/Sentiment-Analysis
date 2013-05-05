import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javaFlacEncoder.FLAC_FileEncoder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class SpeechToTextInterface {
	JTextArea resultArea;
	SpeechToText stt;

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
		JFrame frame = new JFrame ();
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setSize (600, 800);
		record = new JButton ("Record");
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
		resultArea.setPreferredSize (new Dimension (200,300));
		JPanel p = new JPanel (new GridBagLayout ());
		GridBagConstraints c = new GridBagConstraints ();
		c.gridx = 0;
		c.gridy = 0;
		p.add (record, c);

		c.gridx++;
		p.add (stopRecording, c);

		c.gridy++;
		c.gridx--;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		p.add (resultArea, c);

		frame.add (p);
		frame.pack ();
		frame.setVisible (true);


	}

	public void stopRecording () {
		record.setEnabled(true);
		stopRecording.setEnabled(false);
		//Terminate the capturing of input data
		// from the microphone.
		targetDataLine.stop();
		targetDataLine.close();

		FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
		File inputFile = new File(OUTPUT_WAV);
		File outputFile = new File(FLAC_FILE);
		flacEncoder.encode(inputFile, outputFile);

		Utterance u = stt.getUtterance (FLAC_FILE);
		resultArea.setText (resultArea.getText () + u.text+"\n");

	}

	public void startRecording () {
		record.setEnabled(false);
		stopRecording.setEnabled(true);
		//Capture input data from the
		// microphone until the Stop button is
		// clicked.
		captureAudio();

	}

	private void captureAudio(){
		try{
			//Get things set up for capture
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo =
					new DataLine.Info(
							TargetDataLine.class,
							audioFormat);
			targetDataLine = (TargetDataLine)
					AudioSystem.getLine(dataLineInfo);

			//Create a thread to capture the microphone
			// data into an audio file and start the
			// thread running.  It will run until the
			// Stop button is clicked.  This method
			// will return after starting the thread.
			new CaptureThread().start();
		}catch (Exception e) {
			e.printStackTrace();
		}//end catch
	}
	private AudioFormat getAudioFormat(){
		float sampleRate = 44100.0F;
		//8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		//8,16
		int channels = 1;
		//1,2
		boolean signed = true;
		//true,false
		boolean bigEndian = false;
		//true,false
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
			}//end catch

		}//end run
	}

}
