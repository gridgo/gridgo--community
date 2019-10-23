package io.gridgo.socket.nanomsg;

import org.nanomsg.NanoLibrary;

import io.gridgo.socket.Socket;
import io.gridgo.socket.impl.BaseSocketFactory;
import lombok.Getter;
import lombok.NonNull;

public class NNSocketFactory extends BaseSocketFactory {

    private final NanoLibrary nanomsg = new NanoLibrary();

    @Getter
    private final String type = "nanomsg";

    protected Socket createCustomSocket(@NonNull String type) {
        switch (type.toLowerCase()) {
        case "bus":
            return createNanoSocket(nanomsg.NN_BUS);
        case "surveyor":
            return createNanoSocket(nanomsg.NN_SURVEYOR);
        case "respondent":
            return createNanoSocket(nanomsg.NN_RESPONDENT);
        default:
        }
        return null;
    }

    private Socket createNanoSocket(int type) {
        return new NNSocket(nanomsg.nn_socket(nanomsg.AF_SP, type), nanomsg);
    }

    protected Socket createPairSocket() {
        return createNanoSocket(nanomsg.NN_PAIR);
    }

    protected Socket createPubSocket() {
        return createNanoSocket(nanomsg.NN_PUB);
    }

    protected Socket createPullSocket() {
        return createNanoSocket(nanomsg.NN_PULL);
    }

    protected Socket createPushSocket() {
        return createNanoSocket(nanomsg.NN_PUSH);
    }

    protected Socket createRepSocket() {
        return createNanoSocket(nanomsg.NN_REP);
    }

    protected Socket createReqSocket() {
        return createNanoSocket(nanomsg.NN_REQ);
    }

    protected Socket createSubSocket() {
        return createNanoSocket(nanomsg.NN_SUB);
    }
}
