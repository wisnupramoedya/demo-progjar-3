package com.serversocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Header {
    private String requestStatus;
    private final Hashtable<String, String> requestHeader;
    private final BufferedReader bufferedReader;

    public Header(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
        this.requestHeader = new Hashtable<>();
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus() throws IOException {
        do {
            requestStatus = bufferedReader.readLine();
        } while (requestStatus == null || Objects.equals(requestStatus, ""));
    }

    public String getRequestedFile() {
        String[] parsedRequestStatus = requestStatus.split(" ");
        return (parsedRequestStatus[1].equals("/"))
                ? ""
                : parsedRequestStatus[1].substring(1).replaceAll("%20", " ");
    }

    public void setAllRequestHeaders() throws IOException {
        String request;
        do {
            request = bufferedReader.readLine();
        } while (appendToHash(request));
    }

    private boolean appendToHash(String line) {
        int colonIndex = line.indexOf(":");
        if (colonIndex != -1) {
            requestHeader.put(line.substring(0, colonIndex), line.substring(colonIndex + 2));
            return true;
        }
        return false;
    }

    public String getHeaderWithKey(String key) {
        return requestHeader.get(key);
    }

    public boolean doesHeaderHaveKey(String key) {
        return requestHeader.containsKey(key);
    }
}
