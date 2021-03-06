/*
 * Copyright 2009-2012 Roger Kapsi
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

package org.ardverk.dht.codec.bencode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import junit.framework.TestCase;

import org.ardverk.dht.KUID;
import org.ardverk.dht.codec.MessageCodec.Decoder;
import org.ardverk.dht.codec.MessageCodec.Encoder;
import org.ardverk.dht.message.DefaultPingRequest;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.message.MessageId;
import org.ardverk.dht.message.PingRequest;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.Contact.Type;
import org.ardverk.dht.routing.DefaultContact;
import org.junit.Test;


public class BencodeMessageCodecTest {

  @Test
  public void encodeDecode() throws IOException {
    BencodeMessageCodec codec 
      = new BencodeMessageCodec();
    
    MessageId messageId = MessageId.createRandom(20);
    KUID contactId = KUID.createRandom(20);
    
    Contact contact = new DefaultContact(Type.SOLICITED, 
        contactId, 0, false,
        new InetSocketAddress("localhost", 6666));
    
    SocketAddress address = new InetSocketAddress("localhost", 6666);
    PingRequest request = new DefaultPingRequest(messageId, contact, address);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Encoder encoder = codec.createEncoder(baos);
    encoder.write(request);
    encoder.close();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    Decoder decoder = codec.createDecoder(address, bais);
    Message message = decoder.read();
    decoder.close();
    
    TestCase.assertTrue(message instanceof PingRequest);
  }
}