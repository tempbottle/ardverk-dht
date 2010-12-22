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

package com.ardverk.dht;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkProcess;
import com.ardverk.dht.config.GetConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.NodeResponseHandler;
import com.ardverk.dht.io.ValueResponseHandler;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

/**
 * The {@link LookupManager} manages FIND_NODE and FIND_VALUE lookups.
 */
public class LookupManager {

    private final FutureService futureService;
    
    private final MessageDispatcher messageDispatcher;
    
    private final RouteTable routeTable;
    
    LookupManager(FutureService futureService, 
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable) {
        this.futureService = futureService;
        this.messageDispatcher = messageDispatcher;
        this.routeTable = routeTable;
    }
    
    public ArdverkFuture<NodeEntity> lookup(KUID lookupId, LookupConfig config) {
        Contact[] contacts = routeTable.select(lookupId);
        return lookup(contacts, lookupId, config);
    }
    
    public ArdverkFuture<NodeEntity> lookup(Contact[] contacts, 
            KUID lookupId, LookupConfig config) {
        
        ArdverkProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, 
                    contacts, routeTable, lookupId, config);
        return futureService.submit(process, config);
    }
    
    public ArdverkFuture<ValueEntity> get(KUID key, GetConfig config) {
        Contact[] contacts = routeTable.select(key);
        return get(contacts, key, config);
    }
    
    public ArdverkFuture<ValueEntity> get(Contact[] contacts, 
            KUID key, GetConfig config) {
        ArdverkProcess<ValueEntity> process
            = new ValueResponseHandler(messageDispatcher, contacts, 
                    routeTable, key, config);
        return futureService.submit(process, config);
    }
}