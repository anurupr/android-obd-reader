package br.com.pirus.obd2.io;

import com.github.pires.obd.commands.ObdCommand;

public interface ObdProgressListener {

    void stateUpdate(final ObdCommand cmd);

}