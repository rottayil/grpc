package com.cisco.ss.googlecloud;

public class ResponseResult {
    public String getFulfillmentText() {
        return fulfillmentText;
    }

    public byte[] getAudio() {
        return audio;
    }

    String fulfillmentText;
    byte[] audio;

    public ResponseResult(String fulfillmentText, byte[] audio) {
        this.fulfillmentText = fulfillmentText;
        this.audio = audio;
    }
}
