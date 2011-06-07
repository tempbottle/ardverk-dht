package org.ardverk.dht.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectDatabase2 extends AbstractObjectDatabase {

    private static final Logger LOG 
        = LoggerFactory.getLogger(ObjectDatabase2.class);

    private static final String LIST = "list";
    
    private static final String VTAG = "vtag";
    
    private final File directory;
    
    public ObjectDatabase2(File directory) {
        this.directory = directory;
    }
    
    private File toFile(Key key) {
        return new File(directory, key.getPath());
    }
    
    @Override
    protected Response handlePut(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        return null;
    }

    @Override
    protected Response handleDelete(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        File file = toFile(key);
        if (!file.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        FileInputStream fis = new FileInputStream(file);
        Context context = null;
        try {
            context = Context.valueOf(fis);
        } finally {
            if (context == null) {
                IoUtils.close(fis);
            }
        }
        
        if (context.containsHeader(Constants.TOMBSTONE)) {
            IoUtils.close(fis);
            return ResponseFactory.createNotFound();
        }
        
        context.addHeader(Constants.tombstone());
        
        
        return null;
    }
    
    @Override
    protected Response handleHead(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Response handleGet(Contact src, Key key, boolean store) throws IOException {
        Map<String, String> query = key.getQueryString();
        if (query != null && !query.isEmpty()) {
            if (query.containsKey(LIST)) {
                return list(src, key, query);
            } else if (query.containsKey(VTAG)) {
                return vtag(src, key, query);
            }
        }
        
        File file = toFile(key);
        if (!file.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Context context = null;
        
        FileInputStream fis = new FileInputStream(file);
        try {
            context = Context.valueOf(fis);
        } finally {
            if (context == null) {
                IoUtils.close(fis);
            }
        }
        
        if (context.containsHeader(Constants.TOMBSTONE)) {
            IoUtils.close(fis);
            return ResponseFactory.createNotFound();
        }
        
        return null;
    }
    
    protected Response list(Contact src, Key key, Map<String, String> query) {
        return null;
    }
    
    protected Response vtag(Contact src, Key key, Map<String, String> query) {
        return null;
    }
}
