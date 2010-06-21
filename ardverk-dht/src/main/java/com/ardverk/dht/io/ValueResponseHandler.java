package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.FixedSizeArrayList;
import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Key;
import com.ardverk.dht.storage.ValueTuple;

public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private final FixedSizeArrayList<ValueTuple> tuples;
    
    private final Key key;
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, Key key) {
        super(messageDispatcher, routeTable, key.getPrimaryKey());
        
        tuples = new FixedSizeArrayList<ValueTuple>(settings.getR());
        this.key = Arguments.notNull(key, "key");
    }

    @Override
    protected synchronized void processResponse0(RequestEntity request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        if (response instanceof NodeResponse) {
            processNodeResponse((NodeResponse)response, time, unit);
        } else {
            processValueResponse((ValueResponse)response, time, unit);
        }
    }
    
    private synchronized void processNodeResponse(NodeResponse response, 
            long time, TimeUnit unit) throws IOException {
        
        Contact src = response.getContact();
        Contact[] contacts = response.getContacts();
        processContacts(src, contacts, time, unit);
    }
    
    private synchronized void processValueResponse(ValueResponse response, 
            long time, TimeUnit unit) throws IOException {
        
        ValueTuple tuple = response.getValueTuple();
        tuples.add(tuple);
        
        if (tuples.isFull()) {
            State state = getState();
            setValue(new DefaultValueEntity(state, tuple));
        }
    }
    
    @Override
    protected void lookup(Contact dst, KUID lookupId, 
            long timeout, TimeUnit unit) throws IOException {
        
        assert (lookupId.equals(key.getPrimaryKey()));
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ValueRequest message = factory.createValueRequest(dst, key);
        
        long adaptiveTimeout = dst.getAdaptiveTimeout(timeout, unit);
        send(dst, message, adaptiveTimeout, unit);
    }
    
    @Override
    protected void complete(State state) {
        
        if (tuples.isEmpty()) {
            setException(new NoSuchValueException(state));
        } else {
            setValue(new DefaultValueEntity(state, tuples.get(0)));
        }
    }
    
    public static class NoSuchValueException extends IOException {
        
        private static final long serialVersionUID = -2753236114164880872L;

        private final State state;
        
        private NoSuchValueException(State state) {
            this.state = state;
        }

        public Contact[] getContacts() {
            return state.getContacts();
        }

        public int getHop() {
            return state.getHop();
        }

        public long getTime(TimeUnit unit) {
            return state.getTime(unit);
        }
        
        public long getTimeInMillis() {
            return state.getTimeInMillis();
        }
    }
}
