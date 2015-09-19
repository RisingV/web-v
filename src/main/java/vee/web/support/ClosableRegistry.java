package vee.web.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-10  <br/>
 */
public class ClosableRegistry {

    private static AtomicInteger nameGenerator = new AtomicInteger( 0 );
    static Map<String, AutoCloseable> toCloses = new ConcurrentHashMap<>();

    public static void registerCloseable( AutoCloseable ac ) {
        registerClosable( "anonymous-closable-" + nameGenerator.getAndIncrement(), ac );
    }

    public static void registerClosable( String name, AutoCloseable ac ) {
        toCloses.put( name, ac );
    }

}
