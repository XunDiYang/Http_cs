package com.socket.http_cs.model;

import java.util.Map;

public class HttpRequest {
    public static class StartLine{
        HTTP_METHOD method;
        String uri;
        int port;
        HTTP_VERSION version;

        public StartLine(HTTP_METHOD method, String uri, int port, HTTP_VERSION version) {
            this.method = method;
            this.uri = uri;
            this.port = port;
            this.version = version;
        }
    }

    private StartLine startLine;
    private Map<String,String> headers;
    private String messageBody;

    public HttpRequest() {
        this.startLine = null;
        this.headers = null;
        this.messageBody = null;
    }

    public HttpRequest(StartLine startLine, Map<String, String> headers, String messageBody) {
        this.startLine = startLine;
        this.headers = headers;
        this.messageBody = messageBody;
    }

}
