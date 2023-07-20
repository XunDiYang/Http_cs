package com.socket.http_cs.model;

import java.util.Map;

public class HttpResponse {
    /**
     * 请求方法 GET/POST/PUT/DELETE/OPTION...
     */
    private String method;
    /**
     * 请求的uri
     */
    private String uri;
    /**
     * http版本
     */
    private String version;

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 请求参数相关
     */
    private String message;

    public HttpResponse(String method, String uri, String version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }
}
