/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.dht.concurrent.ArdverkFuture;
import org.ardverk.dht.config.GetConfig;
import org.ardverk.dht.config.LookupConfig;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.config.PutConfig;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Value;


/**
 * The {@link DHTService} defines the basic operations of a DHT.
 */
interface DHTService {

    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            String host, int port, PingConfig config);
    
    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            InetAddress address, int port, PingConfig config);
    
    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            SocketAddress address, PingConfig config);
    
    /**
     * Sends a PING to the given host.
     */
    public ArdverkFuture<PingEntity> ping(
            Contact dst, PingConfig config);
    
    /**
     * Performs a FIND_NODE lookup in the DHT.
     */
    public ArdverkFuture<NodeEntity> lookup(
            KUID lookupId, LookupConfig config);
    
    /**
     * Retrieves a key-value from the DHT.
     */
    public ArdverkFuture<ValueEntity> get(
            KUID valueId, GetConfig config);
    
    /**
     * Stores the given key-value in the DHT.
     */
    public ArdverkFuture<PutEntity> put(
            KUID valueId, Value value, PutConfig config);
    
    /**
     * Removes the given {@link KUID} from the DHT.
     */
    public ArdverkFuture<PutEntity> remove(KUID valueId, PutConfig config);
}