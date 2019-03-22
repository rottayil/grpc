package com.cisco.ss.googlecloud;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStream;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.AudioFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

// Imports the Google Cloud client library

/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public class RecognizeDialogFlow {

    private static final String GOOGLE_SERVICE_ACCOUNT_FILENAME = "roomreservation.json";
    ApiStreamObserver<StreamingDetectIntentRequest> requestObserver;
    ResponseApiStreamingObserver responseObserver = new ResponseApiStreamingObserver();
    SessionsClient sessionsClient;

    final AudioEncoding audioEncoding = AudioEncoding.AUDIO_ENCODING_MULAW;
    final int sampleRateHertz = 8000;
//    final AudioEncoding audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16;
//    final int sampleRateHertz = 16000;

    public static RecognizeDialogFlow newInstance(String projectId,
                                                  String sessionId,
                                                  Boolean outputAudio,
                                                  String languageCode) throws Throwable {
        return new RecognizeDialogFlow(projectId, sessionId, outputAudio, languageCode);

    }
    private RecognizeDialogFlow(String projectId, String sessionId, Boolean outputAudio, String languageCode) throws Throwable {
        init(projectId, sessionId, outputAudio, languageCode);

    }


    /**
     * Returns the result of detect intent with streaming audio as input.
     *
     * Using the same `session_id` between requests allows continuation of the conversation.
     *
     * @param projectId     Project/Agent Id.
     * @param sessionId     Identifier of the DetectIntent session.
     * @param outputAudio   Whether to return the audio as request output.
     * @param languageCode  Language code of the query.
     * @return The List of StreamingDetectIntentResponses to the input audio inputs.
     */
    // @Override
    public void init(
            String projectId,
            String sessionId,
            Boolean outputAudio,
            String languageCode) throws Throwable {


        // Instantiates a client

        // Set the session name using the sessionId (UUID) and projectID (my-project-id)
       // SessionsSettings sessionSettings = getSessionSettings();
      //  sessionsClient = SessionsClient.create(sessionSettings);
        sessionsClient = SessionsClient.create();

        SessionName session = SessionName.of(projectId, sessionId);
        System.out.println("Session Path: " + session.toString());

        // Instructs the speech recognizer how to process the audio content.
        InputAudioConfig inputAudioConfig = InputAudioConfig.newBuilder()
                .setAudioEncoding(audioEncoding) // audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16
                .setLanguageCode(languageCode) // languageCode = "en-US"
                .setSampleRateHertz(sampleRateHertz) // sampleRateHertz = 16000
                .build();

        // Build the query with the InputAudioConfig
        QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();


        // Response Observer


        // Performs the streaming detect intent callable request
        // TODO bidiStreamingCall is deprecated
//       BidiStream bidiStream = sessionsClient.streamingDetectIntentCallable().call();
//       bidiStream.forEach(new Consumer() {
//           @Override
//           public void accept(Object o) {
//
//           }
//       });
        requestObserver =
                sessionsClient.streamingDetectIntentCallable().bidiStreamingCall(responseObserver);


        try {
            // The first request contains the configuration
            StreamingDetectIntentRequest request;

            if(outputAudio) {
                OutputAudioConfig outputAudioConfig = OutputAudioConfig
                        .newBuilder()
                        .setAudioEncoding(OutputAudioEncoding.OUTPUT_AUDIO_ENCODING_LINEAR_16)
                        .setSampleRateHertz(8000)
                        .build();

                request = StreamingDetectIntentRequest.newBuilder()
                        .setSession(session.toString())
                        .setQueryInput(queryInput)
                        .setOutputAudioConfig(outputAudioConfig)
                        .build();
            } else {
                request = StreamingDetectIntentRequest.newBuilder()
                        .setSession(session.toString())
                        .setQueryInput(queryInput)
                        .build();
            }

            // Make the first request
            requestObserver.onNext(request);
            //System.out.println("loading locally");


        } catch (RuntimeException e) {
            // Cancel stream.
            e.printStackTrace();
            requestObserver.onError(e);
        }


    }
    public ResponseResult detectAudio() throws Throwable {
        responseObserver.awaitCompletion();
        sessionsClient.close();

        // Process errors/responses.
        if (!responseObserver.getResponseThrowables().isEmpty()) {
            throw responseObserver.getResponseThrowables().get(0);
        }
        List<StreamingDetectIntentResponse> responses = responseObserver.getResponses();
        if (responses.isEmpty()) {
            throw new RuntimeException("No response from Dialogflow.");
        }

        for (StreamingDetectIntentResponse response : responses) {
            if (response.hasRecognitionResult()) {
                System.out.format(
                        "Intermediate transcript: '%s'\n", response.getRecognitionResult().getTranscript());
            }
        }

        StreamingDetectIntentResponse finalResponse = responses.get(responses.size() - 1);
        ByteString outputAudio = finalResponse.getOutputAudio();
        byte[] audioOut = null;
        if (null != outputAudio){
            try {
                audioOut = TranscoderUtil.convert(AudioFormat.Encoding.ULAW, outputAudio.toByteArray());
            }catch(Exception e){
                audioOut = outputAudio.toByteArray();
            }

        }
        return new ResponseResult(responses.toString(), audioOut);
    }
    public SessionsSettings getSessionSettings() throws IOException {
        CredentialsProvider credentialsProvider;
                URL url = Thread.currentThread().getContextClassLoader()
                        .getResource(GOOGLE_SERVICE_ACCOUNT_FILENAME);
                credentialsProvider = FixedCredentialsProvider
                        .create(ServiceAccountCredentials.fromStream(new FileInputStream(url.getPath())));

        SessionsSettings sessionSettings = SessionsSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        return sessionSettings;
    }
    public void sendAudio(ByteString audioBytes) {
        requestObserver.onNext(StreamingDetectIntentRequest.newBuilder()
                .setInputAudio(audioBytes)
                .build());

    }

    public void onCompleted() {
        System.out.println("RequestObserverImpl:completed");
        requestObserver.onCompleted();
    }



}

