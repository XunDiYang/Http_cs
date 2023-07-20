package com.socket.http_cs.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {
    private static final byte SP = 0x20;
    private static final byte LF = 0x0a;
    private static final byte CR = 0x0d;
    private byte[] rawData;

    public void add(byte[] data) {
        byte[] newBytes = new byte[rawData.length + data.length];
        System.arraycopy(rawData, 0, newBytes, 0, rawData.length);
        System.arraycopy(data, 0, newBytes, rawData.length, data.length);
        rawData = newBytes;
    }

    public void clear() {
        rawData = new byte[]{};
    }

    public HttpRequest parse() {
        if (rawData == null || rawData.length == 0) {
            return null;
        }
        String[] lines = (new String(rawData)).split(new String(new byte[]{CR, LF}));
        if (lines.length < 1) {
            return null;
        }
//        解析startline
        HttpRequest.StartLine startLine = parseStartLine(lines[0]);
        if(startLine == null){
            return null;
        }
//        解析requestHeader
        int endHeaderIdx = lines.length;
        for(int i = 0; i < lines.length; i++){
            if(lines[i].isEmpty()){
                endHeaderIdx = i;
                break;
            }
        }
        Map<String, String> headers = parseRequestHeaders(Arrays.copyOfRange(lines,1,endHeaderIdx));
//        解析body
        StringBuilder bodyBuilder = new StringBuilder();
        for(int i = endHeaderIdx+1; i < lines.length; i++){
            bodyBuilder.append(lines[i]);
        }
        return new HttpRequest(startLine,headers,bodyBuilder.toString());
    }


    private HttpRequest.StartLine parseStartLine(String startlineStr){
        String[] startline_strlist = startlineStr.split(new String(new byte[]{SP}));
        if (startline_strlist.length != 4) {
            return null;
        }
        return new HttpRequest.StartLine(
                HTTP_METHOD.valueOf(startline_strlist[0]),
                startline_strlist[1],
                Integer.parseInt(startline_strlist[2]),
                HTTP_VERSION.valueOf(startline_strlist[3]));
    }

    private Map<String, String> parseRequestHeaders(String[] headersList){
        Map<String, String> result = new HashMap<>();
        for(String header:headersList){
            String[] headerKeyValue = header.split(":",2);
            if(headerKeyValue.length!=2){
                continue;
            }
            result.put(headerKeyValue[0],headerKeyValue[1].trim());
        }
        return result;
    }

}
