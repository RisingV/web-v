package vee.web.action.io;

import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public interface FileDownload {

    String getFileName();

    //best to be an inputStream
    Object getFileObject();

    //default 32k
    default int getBufferSize() {
        return 32 * 1024;
    }

    default Consumer<Void> onFinish() {
        return v -> {
        };
    }

}
