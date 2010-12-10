package com.ardverk.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.utils.ArrayUtils;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkValueFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.config.SyncConfig;
import com.ardverk.dht.entity.AbstractEntity;
import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.ValueTuple;
import com.ardverk.dht.utils.ContactKey;

public class SyncManager {
    
    private final DHT dht;
    
    private final StoreManager storeManager;
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    SyncManager(DHT dht, StoreManager storeManager, 
            RouteTable routeTable, Database database) {
        this.dht = dht;
        this.storeManager = storeManager;
        this.routeTable = routeTable;
        this.database = database;
    }
        
    public ArdverkFuture<SyncEntity> sync(final SyncConfig syncConfig) {
        
        final Object lock = new Object();
        
        synchronized (lock) {
            
            final long startTime = System.currentTimeMillis();
            
            final Map<ContactKey, ArdverkFuture<PingEntity>> futures 
                = new HashMap<ContactKey, ArdverkFuture<PingEntity>>();
            
            final List<ArdverkFuture<StoreEntity>> storeFutures 
                = new ArrayList<ArdverkFuture<StoreEntity>>();
            
            Contact localhost = routeTable.getLocalhost();
            PingConfig pingConfig = syncConfig.getPingConfig();
            
            final ArdverkFuture<SyncEntity> userFuture 
                = new ArdverkValueFuture<SyncEntity>();
            
            // The number of PINGs we've sent
            final AtomicInteger pingCounter = new AtomicInteger();
            
            // The number of STOREs we've sent
            final AtomicInteger storeCounter = new AtomicInteger();
            
            for (final ValueTuple tuple : database.values()) {
                KUID valueId = tuple.getId();
                Contact[] contacts = routeTable.select(valueId);
                
                // 
                int index = ArrayUtils.indexOf(localhost, contacts);
                if (index == -1) {
                    continue;
                }
                
                PingFuture pingFuture = ping(futures, contacts, index, pingConfig);
                pingCounter.incrementAndGet();
                
                pingFuture.addAsyncFutureListener(new AsyncFutureListener<Boolean>() {
                    @Override
                    public void operationComplete(AsyncFuture<Boolean> future) {
                        synchronized (lock) {
                            try {
                                process(future);
                            } catch (Throwable impossible) {
                                handleException(impossible);
                            }
                        }
                    }
                    
                    private void process(AsyncFuture<Boolean> future) 
                            throws InterruptedException, ExecutionException {
                        try {
                            if (!future.isCancelled()) {
                                handleValue(future.get());
                            }
                        } finally {
                            postProcess(pingCounter);
                        }
                    }
                    
                    private void handleValue(boolean store) {
                        if (!store) {
                            return;
                        }
                        
                        ArdverkFuture<StoreEntity> storeFuture 
                            = store(tuple, syncConfig.getStoreConfig());
                        storeCounter.incrementAndGet();
                        storeFuture.addAsyncFutureListener(
                                new AsyncFutureListener<StoreEntity>() {
                            @Override
                            public void operationComplete(AsyncFuture<StoreEntity> future) {
                                synchronized (lock) {
                                    try {
                                        storeFutures.add((ArdverkFuture<StoreEntity>)future);
                                    } finally {
                                        postProcess(storeCounter);
                                    }
                                }
                            }
                        });
                    }
                    
                    private void handleException(Throwable t) {
                        userFuture.setException(t);
                    }
                    
                    private void postProcess(AtomicInteger counter) {
                        assert (Thread.holdsLock(lock));
                        counter.decrementAndGet();
                        
                        if (pingCounter.get() == 0 
                                && storeCounter.get() == 0) {
                            complete();
                        }
                    }
                    
                    private void complete() {
                        long time = System.currentTimeMillis() - startTime;
                        
                        @SuppressWarnings("unchecked")
                        ArdverkFuture<StoreEntity>[] futures 
                            = storeFutures.toArray(new ArdverkFuture[0]);
                        userFuture.setValue(new DefaultSyncEntity(
                                futures, time, TimeUnit.MILLISECONDS));
                    }
                });
            }
            
            userFuture.addAsyncFutureListener(new AsyncFutureListener<SyncEntity>() {
                @Override
                public void operationComplete(AsyncFuture<SyncEntity> future) {
                    synchronized (lock) {
                        FutureUtils.cancelAll(futures.values(), true);
                    }
                }
            });
            
            if (pingCounter.get() == 0 && storeCounter.get() == 0) {
                long time = System.currentTimeMillis() - startTime;
                userFuture.setValue(new DefaultSyncEntity(time, TimeUnit.MILLISECONDS));
            }
            
            return userFuture;
        }
    }
    
    private ArdverkFuture<StoreEntity> store(ValueTuple tuple, StoreConfig storeConfig) {
        Contact localhost = routeTable.getLocalhost();
        Contact[] contacts = routeTable.select(localhost.getId());
        assert (localhost.equals(contacts[0]));
        
        Contact[] dst = new Contact[contacts.length];
        System.arraycopy(contacts, 1, dst, 0, dst.length);
        
        return storeManager.store(dst, tuple, storeConfig);
    }
    
    private PingFuture ping(Map<ContactKey, ArdverkFuture<PingEntity>> futures, 
            Contact[] contacts, int toIndex, PingConfig pingConfig) {
        
        List<ArdverkFuture<PingEntity>> pingFutures 
            = new ArrayList<ArdverkFuture<PingEntity>>(toIndex);
        
        for (int i = 0; i < toIndex; i++) {
            Contact contact = contacts[i];
            final ContactKey key = new ContactKey(contact);
            
            ArdverkFuture<PingEntity> future = futures.get(key);
            if (future == null) {
                future = dht.ping(contact, pingConfig);
                futures.put(key, future);
            }
            
            pingFutures.add(future);
        }
        
        return new PingFuture(pingFutures);
    }
    
    private static class PingFuture extends ArdverkValueFuture<Boolean> {
        
        private final AtomicInteger countdown = new AtomicInteger();
        
        private final List<ArdverkFuture<PingEntity>> futures;
        
        private PingFuture(List<ArdverkFuture<PingEntity>> futures) {
            this.futures = futures;
            
            countdown.set(futures.size());
            
            // It's possible that countdown is 0!
            if (0 < countdown.get()) {
                AsyncFutureListener<PingEntity> listener 
                        = new AsyncFutureListener<PingEntity>() {
                    @Override
                    public void operationComplete(AsyncFuture<PingEntity> future) {
                        coutdown();
                    }
                };
                
                for (ArdverkFuture<PingEntity> future : futures) {
                    future.addAsyncFutureListener(listener);
                }
            } else {
                complete();
            }
        }
        
        @Override
        protected void done() {
            super.done();
            
            FutureUtils.cancelAll(futures, true);
        }
        
        private void coutdown() {
            if (countdown.decrementAndGet() == 0) {
                complete();
            }
        }
        
        private void complete() {
            boolean doStore = true;
            for (ArdverkFuture<PingEntity> future : futures) {
                if (!future.isCompletedAbnormally()) {
                    doStore = false;
                    break;
                }
            }
            setValue(doStore);
        }
    }
    
    private static interface SyncEntity extends Entity {
        
        public ArdverkFuture<StoreEntity>[] getStoreFutures();
    }
    
    private static class DefaultSyncEntity extends AbstractEntity implements SyncEntity {

        private final ArdverkFuture<StoreEntity>[] futures;
        
        @SuppressWarnings("unchecked")
        public DefaultSyncEntity(long time, TimeUnit unit) {
            this(new ArdverkFuture[0], time, unit);
        }
        
        public DefaultSyncEntity(ArdverkFuture<StoreEntity>[] futures, 
                long time, TimeUnit unit) {
            super(time, unit);
            this.futures = futures;
        }

        @Override
        public ArdverkFuture<StoreEntity>[] getStoreFutures() {
            return futures;
        }
    }
}