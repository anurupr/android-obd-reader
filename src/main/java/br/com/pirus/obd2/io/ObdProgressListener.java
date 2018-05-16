package br.com.pirus.obd2.io;

public interface ObdProgressListener {

    void stateUpdate(final ObdCommandJob job);

}