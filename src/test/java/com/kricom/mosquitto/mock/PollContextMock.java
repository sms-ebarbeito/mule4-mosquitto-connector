package com.kricom.mosquitto.mock;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.runtime.source.PollContext;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class PollContextMock<I, M> implements PollContext<InputStream, Map<String, Object>> {

    private Consumer consumer;

    @Override
    public PollItemStatus accept(Consumer<PollItem<InputStream, Map<String, Object>>> consumer) {
        this.consumer = consumer;
        return PollItemStatus.ACCEPTED;
    }

    @Override
    public Optional<Serializable> getWatermark() {
        return Optional.empty();
    }

    @Override
    public boolean isSourceStopping() {
        return false;
    }

    @Override
    public void setWatermarkComparator(Comparator comparator) {

    }

    @Override
    public void onConnectionException(ConnectionException e) {

    }

    public Consumer getConsumer(){
        return consumer;
    }

}
