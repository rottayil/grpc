package com.cisco.ss.googlecloud;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.dialogflow.v2beta1.StreamingDetectIntentRequest;
import com.google.protobuf.ByteString;

public class RequestObserverImpl implements RequestObserver {


    private final ApiStreamObserver<StreamingDetectIntentRequest> requestObserver;
    private final ResponseApiStreamingObserver responseObserver;

    public RequestObserverImpl(ApiStreamObserver<StreamingDetectIntentRequest> requestObserver, ResponseApiStreamingObserver responseObserver) {
        this.requestObserver = requestObserver;
        this.responseObserver = responseObserver;
    }

    @Override
    public void sendAudio(ByteString audioBytes) {
        requestObserver.onNext(StreamingDetectIntentRequest.newBuilder()
                .setInputAudio(audioBytes)
                .build());

    }

    @Override
    public void onCompleted() {
        System.out.println("RequestObserverImpl:completed");
        requestObserver.onCompleted();
    }
}

