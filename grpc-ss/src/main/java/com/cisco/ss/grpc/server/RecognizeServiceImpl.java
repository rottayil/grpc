package com.cisco.ss.grpc.server;

import com.cisco.speechserver.grpc.RecognizeRequest;
import com.cisco.speechserver.grpc.RecognizeResponse;
import com.cisco.speechserver.grpc.RecognizeServiceGrpc;
import com.cisco.ss.googlecloud.RecognizeDialogFlow;
import com.cisco.ss.googlecloud.ResponseResult;
import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;

import java.util.Map;

public class RecognizeServiceImpl extends RecognizeServiceGrpc.RecognizeServiceImplBase {
    private static String PROJECT_ID = "roomreservation-e7d85";
    private static String SESSION_ID = "fake_session_for_testing";
    private static String LANGUAGE_CODE = "en-US";

    @Override
    public StreamObserver<RecognizeRequest> recognize(final StreamObserver<RecognizeResponse> responseObserver) {

        return  new StreamObserver<RecognizeRequest>() {
            RecognizeDialogFlow dialogFlow;
            Map<String, Value> configMap= null;
            @Override
            public void onNext(RecognizeRequest request) {
                if (dialogFlow == null) {
                    configMap = request.getInputConfig().getFieldsMap();
                    try {
                        System.out.println("initializing with config:" + configMap);
                        dialogFlow = RecognizeDialogFlow.newInstance(PROJECT_ID, SESSION_ID, true, LANGUAGE_CODE);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                }
                ByteString audioByteString = request.getAudio();
                if (null != audioByteString) {
                    dialogFlow.sendAudio(audioByteString);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                dialogFlow.onCompleted();
                try {
                    ResponseResult responseResult = dialogFlow.detectAudio();

                Struct struct = Struct.newBuilder().putFields("response", Value.newBuilder().setStringValue(responseResult.getFulfillmentText()).build()).build();
                responseObserver.onNext(RecognizeResponse.newBuilder().setAudio(ByteString.copyFrom(responseResult.getAudio())).setResult(struct).build());
                responseObserver.onCompleted();
            }
                catch (Throwable throwable) {
                    throwable.printStackTrace();
                    responseObserver.onError(throwable);
                }

            }

        };

    }

}

