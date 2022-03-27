package com.serversocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class RequestHeader {
    private String requestStatus;
    private final HashMap<String, String> requestHeader;
    private final BufferedReader bufferedReader;

    public RequestHeader(BufferedReader bufferedReader) throws IOException {
        this.bufferedReader = bufferedReader;
        this.requestHeader = new HashMap<>();
        this.setRequestStatus();
        this.setAllRequestHeaders();
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

    public HashMap<String, String> getRangeValues() {
        if (!doesHeaderHaveKey("Range")) {
            return null;
        }
        HashMap<String, String> rangeValues = new HashMap<>();

        // Get unit
        String rangeHeader = getHeaderWithKey("Range");
        String[] parsedRange = rangeHeader.split("=");
        rangeValues.put("unit", parsedRange[0]);

        // Get start range and end range
        String range = parsedRange[1].split(",")[0];
        int separatorIdx = range.indexOf("-");
        rangeValues.put("startIndex", range.substring(0, separatorIdx));
        rangeValues.put("endIndex", range.substring(separatorIdx + 1));
        return rangeValues;
    }

    public boolean validRangeValues(long fileLength) {
        if (!doesHeaderHaveKey("Range")) {
            return true;
        }
        return validRangeValues(getRangeValues(), fileLength);
    }

    public boolean validRangeValues(HashMap<String, String> rangeValues, long fileLength) {
        if (!Objects.equals(rangeValues.get("unit"), "bytes")
                || !rangeValues.containsKey("startIndex")
                || !rangeValues.containsKey("endIndex")
        ) {
            return false;
        }
        long startIndex = 0;
        if (!rangeValues.get("startIndex").equals("")) {
            startIndex = Long.parseLong(rangeValues.get("startIndex"));
        }
        long endIndex = fileLength - 1;
        if (!rangeValues.get("endIndex").equals("")) {
            endIndex = Long.parseLong(rangeValues.get("endIndex"));
        }
        if (startIndex < 0 || startIndex > endIndex || endIndex >= fileLength) {
            return false;
        }
        return true;
    }
}
