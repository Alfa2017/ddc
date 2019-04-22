package ddc.config;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

// https://github.com/NABEEL-AHMED-JAMIL/eventfire/blob/master/src/main/java/com/admaxim/eventfire/config/ReadHttpServletRequest.java

public class ReadHttpServletRequest extends HttpServletRequestWrapper {

    //private static final Logger logger = Logger.getLogger(ReadHttpServletRequest.class);
    private String body;

    public ReadHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);

        BufferedReader bufferedReader = request.getReader();
        String line;
        StringBuffer stringBuffer = new StringBuffer();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        setBody(stringBuffer.toString());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.body.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
            @Override
            public boolean isFinished() { return false; }
            @Override
            public boolean isReady() { return false; }
            @Override
            public void setReadListener(ReadListener listener) {}

        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }
}