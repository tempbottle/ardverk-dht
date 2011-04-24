/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.storage;

import java.net.URI;

import org.ardverk.dht.KUID;

public class DefaultResourceIdFactory implements ResourceIdFactory {

    public static final ResourceIdFactory FACTORY 
        = new DefaultResourceIdFactory();
    
    @Override
    public <T extends Resource> ResourceId<T> createResourceId(URI uri) {
        return valueOf(uri);
    }
    
    public static <T extends Resource> ResourceId<T> valueOf(URI uri) {
        return new DefaultResourceId<T>(parse(uri), uri);
    }
    
    public static <T extends Resource> ResourceId<T> valueOf(KUID valueId) {
        return new DefaultResourceId<T>(valueId, create(valueId));
    }
    
    private static URI create(KUID valueId) {
        return URI.create("ardverk:kuid:" + valueId.toHexString());
    }
    
    private static KUID parse(URI uri) {
        String scheme = uri.getScheme();
        if (!scheme.equals("ardverk")) {
            throw new IllegalArgumentException();
        }
        
        String ssp = uri.getSchemeSpecificPart();
        if (!ssp.startsWith("kuid:")) {
            throw new IllegalArgumentException();
        }
        
        String valueId = ssp.substring("kuid:".length());
        return KUID.create(valueId, 16);
    }
    
    private static class DefaultResourceId<T extends Resource> extends AbstractResourceId<T> {

        private final KUID valueId;

        private final URI uri;
        
        private DefaultResourceId(KUID valueId, URI uri) {
            this.valueId = valueId;
            this.uri = uri;
        }
        
        @Override
        public KUID getId() {
            return valueId;
        }

        @Override
        public URI getURI() {
            return uri;
        }
    }
}
