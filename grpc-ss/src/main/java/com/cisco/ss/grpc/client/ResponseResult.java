package com.cisco.ss.grpc.client;

public class ResponseResult {
    public byte[] getAudioBytes() {
        return audioBytes;
    }

    public String getResponseText() {
        return responseText;
    }

    byte[] audioBytes;
    String responseText;

    public ResponseResult(byte[] audioBytes, String responseText) {
        this.audioBytes = audioBytes;
        this.responseText = responseText;
    }
}
