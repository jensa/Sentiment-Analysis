import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class SpeechToTextInterface {
	JTextArea resultArea;
	SpeechToText stt;
	
	public static void main(String[] args){
		new SpeechToTextInterface ().buildGUI ();
	}

	private void buildGUI () {
		stt = new SpeechToText ();
		JFrame frame = new JFrame ();
		frame.setSize (600, 800);
		JButton record = new JButton ("Record");
		record.addActionListener (new ActionListener (){

			@Override
			public void actionPerformed (ActionEvent e) {
				startRecording ();
			}
			
		});
		JButton stopRecording = new JButton ("Stop recording");
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
		Utterance u = stt.getUtterance ("C:\\googletest.flac");
		resultArea.setText (resultArea.getText () + u.text+"\n");
		
	}
	
	public void startRecording () {
		// TODO Auto-generated method stub
		
	}

}
