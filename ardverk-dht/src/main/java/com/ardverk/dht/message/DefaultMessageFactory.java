package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultMessageFactory extends AbstractMessageFactory {

    private final Contact localhost;
    
    public DefaultMessageFactory(int length, Contact localhost) {
        super(length);
        
        this.localhost = Arguments.notNull(localhost, "localhost");
    }
    
    @Override
    public PingRequest createPingRequest(Contact dst) {
        return createPingRequest(dst.getRemoteAddress());
    }

    @Override
    public PingRequest createPingRequest(SocketAddress dst) {
        MessageId messageId = createMessageId(dst);
        return new DefaultPingRequest(messageId, localhost, dst);
    }

    @Override
    public PingResponse createPingResponse(PingRequest request) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultPingResponse(messageId, localhost, address);
    }

    @Override
    public NodeRequest createNodeRequest(Contact dst, KUID key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultNodeRequest(messageId, localhost, address, key);
    }

    @Override
    public NodeResponse createNodeResponse(LookupRequest request, Contact[] contacts) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultNodeResponse(messageId, localhost, address, contacts);
    }

    @Override
    public ValueRequest createValueRequest(Contact dst, KUID key) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultValueRequest(messageId, localhost, address, key);
    }

    @Override
    public ValueResponse createValueResponse(LookupRequest request, ValueTuple tuple) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        
        return new DefaultValueResponse(messageId, localhost, address, tuple);
    }

    @Override
    public StoreRequest createStoreRequest(Contact dst, ValueTuple tuple) {
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = createMessageId(address);
        return new DefaultStoreRequest(messageId, localhost, address, tuple);
    }

    @Override
    public StoreResponse createStoreResponse(StoreRequest request, Condition status) {
        Contact dst = request.getContact();
        SocketAddress address = dst.getRemoteAddress();
        MessageId messageId = request.getMessageId();
        return new DefaultStoreResponse(messageId, localhost, address, status);
    }
}
