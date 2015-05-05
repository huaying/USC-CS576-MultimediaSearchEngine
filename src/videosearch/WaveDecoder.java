package videosearch;



import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class WaveDecoder {


	public List<Integer> extractAudioFeature(String audioPath, int audiolength) {
		File fileIn = new File(audioPath);
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(fileIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		AudioFormat format = audioInputStream.getFormat();
		float SampleRate = format.getSampleRate();

        long FrameSize = audioInputStream.getFrameLength();
//    	System.out.println( "frame size = " + FrameSize );
		AudioDecoder decoder = null;
		try {
			decoder = new AudioDecoder( new FileInputStream( fileIn ) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		FFT fft = new FFT(1024, SampleRate);
		//System.out.println( "frame " + decoder + " samples" );
		float[] samples = new float[1024];
		float[] spectrum = new float[1024/2+1];
		ArrayList<Integer> finalresult = new ArrayList();
		int size=(int) (FrameSize/audiolength);
		int readSamples = 0;
		int sum = 0;

		while( ( readSamples = decoder.readSamples( samples, size ) ) >0 && sum<audiolength) {
//			System.out.println( "read " + readSamples + " samples" );
//			System.out.println(Arrays.toString(samples));

			fft.forward(samples);

			System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);
//	        System.out.println("Spectrum:"+Arrays.toString(spectrum));
			float max = 0;
			int fq = 0;
			for (int i = 0; i < spectrum.length; i++) {
				if (spectrum[i] > max) {
					max = spectrum[i];
					fq = i;
				}
			}
//
//			System.out.println("String array length is: " + samples.length);
//			System.out.println("max frequency = " + fq);
			finalresult.add(fq);
			sum++;

		}

		return finalresult;
	}
}