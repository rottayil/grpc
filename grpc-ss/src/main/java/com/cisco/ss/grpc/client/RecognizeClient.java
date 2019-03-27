package com.cisco.ss.grpc.client;

import com.cisco.speechserver.grpc.RecognizeRequest;
import com.cisco.speechserver.grpc.RecognizeResponse;
import com.cisco.speechserver.grpc.RecognizeServiceGrpc;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RecognizeClient {
    private final static int PORT =55555;
    public static final String CUSTOM_HEADER_SS_PREFIX = "x-ss-";
    public static final String CUSTOM_HEADER_SESSION_ID = CUSTOM_HEADER_SS_PREFIX + "session-id";
    public static final String CUSTOM_HEADER_PROJECT_ID = CUSTOM_HEADER_SS_PREFIX + "projectid";
    public static final String CUSTOM_HEADER_CODEC = CUSTOM_HEADER_SS_PREFIX + "codec";
    public static final String CUSTOM_HEADER_SAMPLE_RATEHERTZ = CUSTOM_HEADER_SS_PREFIX + "samplerate";
    public static final String CUSTOM_HEADER_LANGUAGE = CUSTOM_HEADER_SS_PREFIX + "language";
    private static String PROJECT_ID = "roomreservation-e7d85";
    private static String SESSION_ID = "fake_session_for_testing";
    private static String LANGUAGE_CODE = "en-US";
    private static String CODEC = "LINEAR16";
    private final static String AUDIO_FILE="resources/book_a_room.wav";
    StreamObserver responseObserver;
    StreamObserver<RecognizeRequest> requestObserver;
    ResponseResult responseResult = null;
    static Channel channel = null;
    final CountDownLatch latch = new CountDownLatch(1);
    public static RecognizeClient newInstance(String host, int port) throws IOException, InterruptedException {
        RecognizeClient client = new RecognizeClient();
        client.initChannel(host, port);
        return client;

    }
   public void sendAudioChunk(byte[] bytes){
        requestObserver.onNext(
                RecognizeRequest.newBuilder()
                        .setAudio(ByteString.copyFrom(bytes))
                        .build());
    }
    public ResponseResult completed() throws InterruptedException {
        Thread.sleep(10000);
        System.out.println(new Date().toString()  + ":client says completed");
        requestObserver.onCompleted();

        latch.await();
        if (responseResult == null){
            throw new IllegalStateException("no result available");
        }
        return responseResult;
    }

    private void initChannel(String host, int port) throws IOException, InterruptedException {
        System.out.println("Opening channel to " + host + ":" + port);

        Channel channel = getChannel(host, port);

        RecognizeServiceGrpc.RecognizeServiceStub stub = RecognizeServiceGrpc.newStub(channel);
        responseObserver = new StreamObserver<RecognizeResponse>() {


            @Override
            public void onNext(RecognizeResponse value) {

                System.out.println("got response audio:" + value.getAudio().toString());
                String responseText = value.getResult();
                System.out.println("got response text:" + responseText);
                responseResult = new ResponseResult(value.getAudio().toByteArray(),responseText);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("server returned error");
                t.printStackTrace();

            }

            @Override
            public void onCompleted() {
                System.out.println("wait is over, server says completed");
                latch.countDown();

            }
        };
       requestObserver = stub.recognize(responseObserver);
       Map<String, String> config = new HashMap<>();
       config.put(CUSTOM_HEADER_SESSION_ID, SESSION_ID);
        config.put(CUSTOM_HEADER_PROJECT_ID, PROJECT_ID);
        config.put(CUSTOM_HEADER_CODEC, "ALAW");
        config.put(CUSTOM_HEADER_LANGUAGE, LANGUAGE_CODE);
     //   config.put(CUSTOM_HEADER_SAMPLE_RATEHERTZ, "16000");

       System.out.println("sending config:" + config);
        requestObserver.onNext(
                RecognizeRequest.newBuilder()
                        .putAllInputConfig(config)
                        .build());

    }

    private Channel getChannel(String host, int port) {
        if (null == channel) {
            channel =  ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext().build();
        }
        return channel;
    }


}
