package com.cisco.ss.googlecloud;



import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class provides methods for u-law, A-law and linear PCM
 * conversions.
 */
public class TranscoderUtil {

    private static final int samplingRate = 8000;
    private static final int sampleSizeInBits = 8;
    private static final int frameRate = 8000;
    /**
     * Method to perform transcoding using java util
     *
     * @param encoding
     * @param wavBytes
     * @return
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public static byte[] convert(AudioFormat.Encoding encoding, byte[] wavBytes) throws UnsupportedAudioFileException, IOException{
        ByteArrayInputStream inByteStream = new ByteArrayInputStream(wavBytes);
        AudioInputStream inAudioStream = AudioSystem.getAudioInputStream(inByteStream);
        AudioFormat outFormat = inAudioStream.getFormat();

        System.out.println("Converting to format" + outFormat.isBigEndian());
        AudioFormat newFormat = new AudioFormat(
                encoding, samplingRate, sampleSizeInBits, 1, 1, frameRate, false
        );
        System.out.println("isCoversion Supported " + AudioSystem.isConversionSupported(newFormat, outFormat));
        AudioInputStream outAudioStream = AudioSystem.getAudioInputStream(newFormat, inAudioStream);

        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        AudioSystem.write(outAudioStream, AudioFileFormat.Type.WAVE, outByteStream);
        return outByteStream.toByteArray();
    }
}

