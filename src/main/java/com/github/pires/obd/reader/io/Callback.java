package com.github.pires.obd.reader.io;

public interface Callback {

    int ERROR = -1;
    int FAILURE = 0;
    int SUCCESS = 1;

    void onResult(int codeResult);
}
