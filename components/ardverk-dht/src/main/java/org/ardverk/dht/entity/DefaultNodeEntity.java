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

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.io.Outcome;
import org.ardverk.dht.routing.Contact;


public class DefaultNodeEntity extends AbstractLookupEntity implements NodeEntity {
    
    private final Outcome outcome;
    
    public DefaultNodeEntity(Outcome outcome) {
        super(outcome.getId(), outcome.getTimeInMillis(), 
                TimeUnit.MILLISECONDS);
        
        this.outcome = outcome;
    }
    
    @Override
    public Contact[] getClosest() {
        return outcome.getClosest();
    }
    
    @Override
    public Contact[] getContacts() {
        return outcome.getContacts();
    }

    @Override
    public int getHop() {
        return outcome.getHop();
    }
    
    public Outcome getOutcome() {
        return outcome;
    }
}