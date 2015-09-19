package vee.web.action.reflect.action.async;

import vee.web.action.reflect.action.AccessPoint;
import vee.web.concurrent.NamedThreadFactory;
import vee.web.servlet.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-15  <br/>
 */
public class BackendAccess implements AccessPoint {

    private static Logger logger = LoggerFactory.getLogger( BackendAccess.class );
    private static final ExecutorService exec = Executors.newCachedThreadPool( new NamedThreadFactory( "web-common-backend" ) );

    private final long timeout;
    private final AccessPoint access;
    private final ExecuteType executeType;

    public BackendAccess( AccessPoint access, long timeout, ExecuteType executeType ) {
        this.access = access;
        this.timeout = timeout;
        this.executeType = executeType;
    }

    @Override
    public Object call( final Object action, final RequestContext context ) {
        switch ( executeType ) {
            case DUMMY: {
                submit( () -> access.call( action, context ) );
                return null;
            }
            case BACKEND_TASK: {
                submit( () -> ((BackendTask<?>) access.call( action, context )).execute( timeout ));
                return null;
            }
            case CARRY_ON_TASK: {
                CarryOnTask<?> cot = (CarryOnTask<?>) access.call( action, context );
                submit( () -> cot.execute( timeout ) );
                return cot.get();
            }
            default: {
                throw new IllegalStateException( "shouldn't reach here." );
            }
        }
    }

    private void submit( Callable<?> callable ) {
        final FutureTask<?> task = new FutureTask<>( callable );
        exec.execute( task );
        //monitor
        exec.submit( () -> {
            try {
                task.get( timeout, TimeUnit.MILLISECONDS );
                if ( !task.isDone() ) {
                    task.cancel( true );
                    logger.warn( "backend task timeout." );
                }
            } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                logger.error( "backend task error: {} ", e );
            }
        } );
    }

}
