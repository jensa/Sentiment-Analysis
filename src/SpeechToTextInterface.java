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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class SpeechToTextInterface {
	SpeechToText stt = new SpeechToText ();
	SentimentAnalyser analyser = new SentimentAnalyser ();
	private AudioRecorder audio = new AudioRecorder ();

	private JButton stopRecording;
	private JButton record;
	private JTextArea resultArea;

	public static void main(String[] args){
		File audioDir = new File ("recordings");
		if (!audioDir.exists ())
			audioDir.mkdirs ();
		new SpeechToTextInterface ().buildGUI ();
	}
	
	private void buildGUI () {
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
		audio.stop ();
		new Thread (new PostProcesser ()).start ();
	}

	public void startRecording () {
		record.setEnabled(false);
		stopRecording.setEnabled(true);
		audio.captureAudio();
	}

	class PostProcesser implements Runnable{
		@Override
		public void run () {
			stopRecording.setEnabled(false);
			String analysing = "Analysing...";
			resultArea.append (analysing);
		
			FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
			flacEncoder.encode(audio.outputWav, audio.outputFLAC);

			Utterance u = stt.getUtterance (audio.outputFLAC);
			audio.outputWav.delete ();
			if (u == null)
				return;
			double[] sentiments = analyser.getSentiment (u);
			if (sentiments == null){
				resultArea.setText (resultArea.getText ()+analyser.ERROR+"\n");
				return;
			}
			resultArea.setText (resultArea.getText ().substring (0, resultArea.getText ().length ()-analysing.length ()));
			StringBuilder text = new StringBuilder (u.text+"\n-----------------\nSENTIMENTS:\n");
			for (int i=0;i< analyser.sentimentNames.length;i++){
				text.append (analyser.sentimentNames[i]+": "+sentiments[i]+"\n"); 
			}
			text.append ("----------------------------\n");
			resultArea.append (text.toString ());
			record.setEnabled(true);
			
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
