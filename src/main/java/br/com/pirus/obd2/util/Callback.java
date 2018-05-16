package br.com.pirus.obd2.util;

public interface Callback {

    int ERROR = -1;
    int FAILURE = 0;
    int SUCCESS = 1;

    void onResult(int codeResult);
}
