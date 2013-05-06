import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;


public class AudioRecorder {
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	
	public File outputWav;
	public File outputFLAC;
	
	public AudioRecorder (){
		outputWav = new File ("recordings/audio.wav");
		outputFLAC = new File ("recordings/audio.flac");
	}
	
	public void captureAudio(){
		try{
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
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
		return new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
	}

	class CaptureThread extends Thread{
		public void run(){
			AudioFileFormat.Type fileType = null;
			fileType = AudioFileFormat.Type.WAVE;
			try{
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				AudioSystem.write(new AudioInputStream(targetDataLine), fileType, outputWav);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void stop () {
		targetDataLine.stop();
		targetDataLine.close();
	}

}
