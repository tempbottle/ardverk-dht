package com.ardverk.dht.routing;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact.Type;

public class RouteTableTest {

    private static final int K = 20;
    
    private static final int ID_SIZE = 20;
    
    private static final int DEFAULT_PORT = 2000;
    
    @Test
    public void initialState() {
        Contact localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        TestCase.assertEquals(1, routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        
        TestCase.assertEquals(localhost, routeTable.getLocalhost());
        TestCase.assertSame(localhost, routeTable.getLocalhost());
        
        TestCase.assertEquals(localhost, routeTable.get(localhost.getId()));
        TestCase.assertSame(localhost, routeTable.get(localhost.getId()));
        
        ContactEntity[] active = routeTable.getActiveContacts();
        ContactEntity[] cached = routeTable.getCachedContacts();
        
        TestCase.assertEquals(1, active.length);
        TestCase.assertEquals(0, cached.length);
        
        TestCase.assertEquals(localhost, active[0].getContact());
        TestCase.assertSame(localhost, active[0].getContact());
    }
    
    @Test
    public void addLocalhost() {
        Contact localhost = createLocalhost();
        DefaultRouteTable routeTable = createRouteTable(localhost);
        
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleContactAdded(Bucket bucket, Contact contact) {
                TestCase.fail("Shouldn't have been called.");
            }
        });
        
        // Adding it *AGAIN*
        routeTable.add(localhost);
        
        // Nothing should change.
        initialState();
    }
    
    @Test
    public void addContact() throws InterruptedException {
        DefaultRouteTable routeTable = createRouteTable();
        
        final CountDownLatch latch = new CountDownLatch(1);
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleContactAdded(Bucket bucket, Contact contact) {
                latch.countDown();
            }
        });
        
        Contact contact = createContact();
        routeTable.add(contact);
        
        if (!latch.await(1L, TimeUnit.SECONDS)) {
            TestCase.fail("Shouldn't have failed!");
        }
        
        TestCase.assertEquals(2, routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        TestCase.assertEquals(2, routeTable.getBuckets()[0].getActiveCount());
    }
    
    @Test
    public void split() throws InterruptedException {
        final DefaultRouteTable routeTable = createRouteTable();
        
        while (routeTable.size() < routeTable.getK()) {
            Contact contact = createContact();
            routeTable.add(contact);
        }
        
        TestCase.assertEquals(routeTable.getK(), routeTable.size());
        TestCase.assertEquals(1, routeTable.getBuckets().length);
        
        final CountDownLatch latch = new CountDownLatch(1);
        routeTable.addRouteTableListener(new RouteTableAdapter() {
            @Override
            public void handleBucketSplit(Bucket bucket, Bucket left,
                    Bucket right) {
                latch.countDown();
            }
        });
        
        Contact contact = createContact();
        routeTable.add(contact);
        
        if (!latch.await(1L, TimeUnit.SECONDS)) {
            TestCase.fail("Shouldn't have failed!");
        }
        
        TestCase.assertEquals(routeTable.getK() + 1, routeTable.size());
        TestCase.assertEquals(2, routeTable.getBuckets().length);
    }
    
    private static DefaultRouteTable createRouteTable() {
        Contact localhost = createLocalhost();
        return createRouteTable(localhost);
    }
    
    private static DefaultRouteTable createRouteTable(Contact localhost) {
        return new DefaultRouteTable(K, localhost);
    }
    
    private static Contact createLocalhost() {
        return createLocalhost(DEFAULT_PORT);
    }
    
    private static Contact createLocalhost(int port) {
        return createLocalhost(KUID.createRandom(ID_SIZE), port);
    }
    
    private static Contact createLocalhost(KUID contactId, int port) {
        return Contact.localhost(contactId, 
                new InetSocketAddress("localhost", port));
    }
    
    private static Contact createContact() {
        return createContact(DEFAULT_PORT);
    }
    
    private static Contact createContact(int port) {
        return createContact(KUID.createRandom(ID_SIZE), port);
    }
    
    private static Contact createContact(KUID contactId, int port) {
        return new Contact(Type.SOLICITED, contactId, 
                0, new InetSocketAddress("localhost", port));
    }
}
