package com.kricom.mosquitto.mock;

import com.kricom.mosquitto.internal.connection.MosquittoConnectionProviderMock;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SourceCallBackMock<I, M> implements SourceCallback<InputStream, Map<String, Object>> {
    private final Logger LOGGER = LoggerFactory.getLogger(SourceCallBackMock.class);

    private Result<InputStream, Map<String, Object>> result;

    public Result<InputStream, Map<String, Object>> getResult() {
        return result;
    }


    @Override
    public void handle(Result<InputStream, Map<String, Object>> result) {
        this.result = result;
    }

    @Override
    public void handle(Result<InputStream, Map<String, Object>> result, SourceCallbackContext sourceCallbackContext) {
        this.result = result;
    }

    @Override
    public void onConnectionException(ConnectionException e) {
        LOGGER.error("Exception: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public SourceCallbackContext createContext() {
        return null;
    }
}