package com.cisco.ss.grpc.client;

import com.google.protobuf.ByteString;
import org.apache.commons.io.FileUtils;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class ExampleClient {
    private final static int PORT =55555;
    private final static String AUDIO_FILE="book_a_room.wav";



    public static void main(String[] args) throws InterruptedException, IOException, UnsupportedAudioFileException {
        String host = "localhost";
        if (args.length > 0){
            host = args[0];
        }
        host = "10.232.21.42";
        process(host, "out1.wav");
        System.out.println("===========================second time");
        process(host, "out2.wav");
        System.out.println("===========================third time");
        process(host, "out3.wav");
    }

    private static byte[] getBytesFromInputStream(InputStream iStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for (int len = iStream.read(buffer); len != -1; len = iStream.read(buffer)) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }


    private static void process(String host, String outFileName) throws IOException, InterruptedException {
        RecognizeClient client = RecognizeClient.newInstance(host, PORT);
        sendFile(client, AUDIO_FILE);
        ResponseResult result = client.completed();
        File file = new File(outFileName);
        try {
            FileUtils.writeByteArrayToFile(file, result.getAudioBytes());
            System.out.println("output audio in " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendFile(RecognizeClient client, String audioFilePath) throws IOException {
        // Following messages: audio chunks. We just read the file in fixed-size chunks. In reality
        // you would split the user input by time.
        byte[] buffer = new byte[4096];
        int bytes;
        InputStream audioStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(AUDIO_FILE);


        while ((bytes = audioStream.read(buffer)) != -1) {
           client.sendAudioChunk(ByteString.copyFrom(buffer, 0, bytes).toByteArray());
        }
    }


}
