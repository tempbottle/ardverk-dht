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

package org.ardverk.dht.routing;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.dht.KUID;
import org.ardverk.lang.Precoditions;
import org.ardverk.lang.TimeStamp;
import org.ardverk.net.NetworkUtils;

/**
 * A default implementation of {@link Contact}.
 */
public class DefaultContact extends AbstractContact {
  
  private static final long serialVersionUID = 298059770472298142L;
  
  private final Type type;
  
  private final TimeStamp creationTime;
  
  private final TimeStamp timeStamp;
  
  private final int instanceId;
  
  private final boolean hidden;
  
  private final SocketAddress socketAddress;
  
  private final SocketAddress contactAddress;
  
  private final SocketAddress remoteAddress;
  
  /**
   * Creates a {@link DefaultContact}
   */
  public DefaultContact(KUID contactId, SocketAddress address) {
    this(Type.UNKNOWN, contactId, 0, false, address);
  }
  
  /**
   * Creates a {@link DefaultContact}
   */
  public DefaultContact(Type type, KUID contactId, 
      int instanceId, boolean hidden, 
      SocketAddress address) {
    this(type, contactId, instanceId, hidden, address, address);
  }
  
  /**
   * Creates a {@link DefaultContact}
   */
  public DefaultContact(Type type, KUID contactId, 
      int instanceId, boolean hidden, 
      SocketAddress socketAddress, SocketAddress contactAddress) {
    this(type, contactId, instanceId, hidden, 
        socketAddress, contactAddress, -1L, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Creates a {@link DefaultContact}
   */
  public DefaultContact(Type type, 
      KUID contactId, 
      int instanceId, 
      boolean hidden,
      SocketAddress socketAddress, 
      SocketAddress contactAddress,
      long rtt, TimeUnit unit) {
    super(contactId, rtt, unit);
    
    if (contactAddress == null) {
      contactAddress = socketAddress;
    }
    
    this.type = Precoditions.notNull(type, "type");
    this.creationTime = TimeStamp.now();
    this.timeStamp = creationTime;
    
    this.instanceId = instanceId;
    this.hidden = hidden;
    this.socketAddress = socketAddress;
    this.contactAddress = contactAddress;
    this.remoteAddress = fixAddress(socketAddress, contactAddress);
  }
  
  /**
   * 
   */
  private DefaultContact(DefaultContact existing, Contact other) {
    super(existing, pickRTT(existing, other), TimeUnit.MILLISECONDS);
    
    this.creationTime = existing.getCreationTime();
    
    if (other.isActive()) {
      this.timeStamp = other.getTimeStamp();
    } else {
      this.timeStamp = existing.getTimeStamp();
    }
    
    if (existing.isBetter(other)) {
      this.instanceId = existing.getInstanceId();
      this.hidden = existing.isHidden();
      this.socketAddress = existing.getSocketAddress();
      this.contactAddress = existing.getContactAddress();
      this.remoteAddress = existing.getRemoteAddress();
      this.type = existing.getType();
    } else {
      this.instanceId = other.getInstanceId();
      this.hidden = other.isHidden();
      this.socketAddress = other.getSocketAddress();
      this.contactAddress = other.getContactAddress();
      this.remoteAddress = other.getRemoteAddress();
      this.type = other.getType();
    }
  }
  
  /**
   * Returns {@code true} if this is a better {@link Contact} than
   * the other given {@link Contact}.
   */
  private boolean isBetter(Contact other) {
    // Everything is a better than an *UNKNOWN* Contact even
    // if the other Contact is *UNKNOWN* too.
    return type != Type.UNKNOWN && isBetterOrEqual(other);
  }
  
  /**
   * Returns {@code true} if this is a better or a equally good 
   * {@link Contact} than the other given {@link Contact}.
   */
  private boolean isBetterOrEqual(Contact other) {
    return type.isBetterOrEqual(other.getType());
  }
  
  @Override
  public TimeStamp getCreationTime() {
    return creationTime;
  }
  
  @Override
  public TimeStamp getTimeStamp() {
    return timeStamp;
  }
  
  @Override
  public int getInstanceId() {
    return instanceId;
  }
  
  @Override
  public boolean isHidden() {
    return hidden;
  }
  
  @Override
  public SocketAddress getSocketAddress() {
    return socketAddress;
  }
  
  @Override
  public SocketAddress getContactAddress() {
    return contactAddress;
  }
  
  @Override
  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public Type getType() {
    return type;
  }
  
  @Override
  public Contact merge(Contact other) {
    if (!equals(other) || other.isHidden()) {
      throw new IllegalArgumentException("other=" + other);
    }
    
    return other != this ? new DefaultContact(this, other) : this;
  }
  
  /**
   * Combines the socket addresses {@link InetAddress} and the
   * contact addresses port number.
   */
  private static SocketAddress fixAddress(SocketAddress socketAddress, 
      SocketAddress contactAddress) {
    
    if (NetworkUtils.isAnyLocalAddress(contactAddress)) {
      return NetworkUtils.create(socketAddress, contactAddress);
    }
    
    return contactAddress;
  }
  
  /**
   * Picks and returns the RTT for the given two {@link Contact}s.
   */
  private static long pickRTT(Contact existing, Contact other) {
    long otherRTT = other.getRoundTripTimeInMillis();
    return otherRTT > 0L ? otherRTT : existing.getRoundTripTimeInMillis();
  }
}