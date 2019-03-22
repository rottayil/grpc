package com.cisco.ss.googlecloud;

import com.google.protobuf.ByteString;

public interface RequestObserver {
    void sendAudio(ByteString audioBytes);
    void onCompleted();
}
