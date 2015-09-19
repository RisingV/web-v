package vee.web.action.io.impl;

import vee.web.action.io.FileDownload;
import vee.web.action.reflect.result.ResultGetter;
import vee.web.action.reflect.result.ResultWriter;
import vee.web.servlet.RequestContext;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public class FileResultWriter implements ResultWriter {

    @Override
    public void write( ResultGetter getter, RequestContext context ) {

        try {
            FileDownload fd = (FileDownload) getter.get();
            HttpServletResponse resp = context.getResponse();
            resp.setContentType( "application/octet-stream" );
            resp.setHeader( "Content-disposition", "attachment; filename=" + fd.getFileName() );
            OutputStream outputStream = resp.getOutputStream();
            writeBinary( fd.getFileObject(), outputStream, fd.getBufferSize() );
            fd.onFinish().accept( null );
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }

    }

    private void writeBinary( Object result, OutputStream out, int bufferSize ) throws IOException {
        result = null != result ? result : "";
        InputStream in;
        if ( result instanceof InputStream ) {
            in = (InputStream) result;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject( result );

            oos.flush();
            oos.close();

            in = new ByteArrayInputStream( baos.toByteArray() );
        }

        byte[] buffer = new byte[bufferSize];
        int length;
        while ( ( length = in.read( buffer ) ) > 0 ) {
            out.write( buffer, 0, length );
        }
        in.close();
        out.flush();
    }

}
