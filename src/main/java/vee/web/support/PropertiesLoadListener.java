package vee.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-12  <br/>
 */
public class PropertiesLoadListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger( PropertiesLoadListener.class );

    private static final String PROPS_FILE = "preloadPropertiesFile";

    private static final String CLASSPATH_PROTO = "classpath:";

    private static final String FILE_PROTO = "file:";

    @Override
    public void contextInitialized( ServletContextEvent servletContextEvent ) {
        String protocol = servletContextEvent.getServletContext().getInitParameter( PROPS_FILE );
        if ( null == protocol || protocol.isEmpty() ) return;
        Properties toLoad = new Properties();
        try {
            doLoad( protocol, toLoad );
            if ( !toLoad.isEmpty() ) {
                loadAndPrint( toLoad );
            }
        } catch ( Exception err ) {
            log.error( "preloading properties file: {} failed, detail: {} ", protocol, err );
            //stop starting the app
            throw new RuntimeException( err );
        }
    }

    private void loadAndPrint( Properties loaded ) {
        for ( String name : loaded.stringPropertyNames() ) {
            String value = loaded.getProperty( name );
            //won't overwrite
            if ( null == System.getProperty( name ) ) {
                System.setProperty( name, value );
            }
            log.info( "... loaded property: {} : {} ", name, value );
        }
    }

    private void doLoad( String protocol, Properties toLoad ) throws IOException {
        if ( protocol.startsWith( CLASSPATH_PROTO ) ) {
            toLoad.load( loadFromClasspath( protocol ) );
        } else if ( protocol.startsWith( FILE_PROTO ) ) {
            toLoad.load( loadFromFile( protocol ) );
        } else { //load in classpath as the default way.
            toLoad.load( loadFromFile( CLASSPATH_PROTO + protocol ) );
        }
    }

    private InputStream loadFromClasspath( String proto ) {
        String fileName = getFileName( CLASSPATH_PROTO, proto );
        return this.getClass().getResourceAsStream( '/' + fileName );
    }

    private InputStream loadFromFile( String proto ) throws IOException {
        String fileName = getFileName( FILE_PROTO, proto );
        return Files.newInputStream( Paths.get( fileName ) );
    }

    private String getFileName( String protoPrefix, String totalProto ) {
        return totalProto.substring( protoPrefix.length() );
    }


    @Override
    public void contextDestroyed( ServletContextEvent servletContextEvent ) {
        //do nothing.
    }

}
