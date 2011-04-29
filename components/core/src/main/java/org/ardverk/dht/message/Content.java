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

package org.ardverk.dht.message;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.NopFuture;

/**
 * The {@link Message} content.
 * 
 * @see Message#getContent()
 */
public interface Content {
    
    /**
     * A default {@link DHTFuture} that may be returned by {@link #getContentFuture()}
     */
    public static final NopFuture<Void> DEFAULT_FUTURE 
        = NopFuture.withValue(null);
    
    /**
     * Returns the {@link Content}'s {@link DHTFuture}.
     */
    public DHTFuture<Void> getContentFuture();
    
    /**
     * Returns the length of the {@link Content}.
     */
    public long getContentLength();
    
    /**
     * Returns the {@link Content}'s data as an {@link InputStream}.
     * 
     * NOTE: The caller (you) is responsible for closing the {@link InputStream}.
     */
    public InputStream getContent() throws IOException;
    
    /**
     * Returns the {@link Content}'s data as a {@code byte[]}.
     */
    public byte[] getContentAsBytes() throws IOException;
    
    /**
     * Returns {@code true} if the {@link Content} capable of producing 
     * its data more than once.
     */
    public boolean isRepeatable();
    
    /**
     * Returns {@code true} if the {@link Content} is streaming.
     */
    public boolean isStreaming();
}