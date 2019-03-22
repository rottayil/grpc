package com.cisco.ss.googlecloud;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.dialogflow.v2beta1.StreamingDetectIntentResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;


class ResponseApiStreamingObserver implements ApiStreamObserver<StreamingDetectIntentResponse> {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<Throwable> responseThrowables = new ArrayList<>();
    final List<StreamingDetectIntentResponse> responses = new ArrayList<>();


    public void awaitCompletion() throws InterruptedException {
         countDownLatch.await();
    }

    public List<StreamingDetectIntentResponse> getResponses() {
        return responses;
    }

    public List<Throwable> getResponseThrowables() {
        return responseThrowables;
    }

    @Override
    public void onNext(StreamingDetectIntentResponse response) {
        System.out.println(new Date().toString() + ":ResponseApiStreamingObserver:from Google:" + response);
        System.out.println("is Final = " + response.getRecognitionResult().getIsFinal());
        responses.add(response);

    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        responseThrowables.add(t);
    }

    @Override
    public void onCompleted() {
        System.out.println("ResponseApiStreamingObserver:completed");
        countDownLatch.countDown();
    }
}
