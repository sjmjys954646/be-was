package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

public class Response {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String ROOT_DIRECTORY = System.getProperty("user.dir");
    String httpVersion;
    int statusCode;
    String statusText;
    HashMap<String, String> responseHeader;
    byte[] responseBody;

    public Response(Request request) {
        this.httpVersion = request.httpVersion;
        this.responseHeader = new HashMap<>();
        setStatusCode(request);
        setBody(request);
        setHeader(responseBody.length);
    }

    private void setHeader(int bodyLength) {
        responseHeader.put("Date",ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)));
        responseHeader.put("Server", "MyServer/1.0" );
        responseHeader.put("Content-Length", Integer.toString(bodyLength));
        responseHeader.put("Content-Type", "text/html; charset=UTF-8");
    }

    public void setStatusCode(Request request) {
        this.statusCode = StatusCode.OK.getCode();
        this.statusText = StatusCode.OK.name();
    }


    public String getHttpVersion() {
        return httpVersion;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public HashMap<String, String> getResponseHeader() {
        return responseHeader;
    }

    void setBody(Request request){
        try{
            if(request.mimeType.getMimeType().equals("text/html")){
                responseBody = Files.readAllBytes(new File(ROOT_DIRECTORY + "/src/main/resources/templates" + request.getRequestTarget()).toPath());
            }
            else{
                responseBody = new byte[0];
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    public byte[] getResponseBody() {
        return responseBody;
    }
}
