import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.flac.FlacInfoReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;


public class AudioFileFinderOuter {
//	private static File testFile = new File ("C:/Users/jens/Desktop/testrecordings/32000.flac");
//	
//	public static void main(String[] args){
//		System.out.println (figureOutFLACBitrate (testFile));
//	}
	
	public static long figureOutFLACBitrate (File f){
		try {
			RandomAccessFile raf = new RandomAccessFile (f,"rw");
			return figureOutBitrate (raf);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CannotReadException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static long figureOutBitrate (RandomAccessFile f) throws IOException, CannotReadException{
		FlacInfoReader fir = new FlacInfoReader ();
		GenericAudioHeader hdr = fir.read (f);
		return hdr.getSampleRateAsNumber ();
	}

}
