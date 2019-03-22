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
import java.util.concurrent.CountDownLatch;

public class RecognizeClient {
    private final static int PORT =55555;
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
                Value responseText = value.getResult().getFieldsMap().get("response");
                System.out.println("got response text:" + responseText);
                responseResult = new ResponseResult(value.getAudio().toByteArray(),responseText.getStringValue());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("wait is over, server says completed");
                latch.countDown();

            }
        };
       requestObserver = stub.recognize(responseObserver);

    }

    private Channel getChannel(String host, int port) {
        if (null == channel) {
            channel =  ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext().build();
        }
        return channel;
    }


}
