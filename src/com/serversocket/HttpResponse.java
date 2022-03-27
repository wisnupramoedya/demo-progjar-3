package com.serversocket;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpResponse {
    private final RequestHeader requestHeader;
    private final FileService fileService;
    private final SimpleDateFormat sdfGMT;

    private final BufferedWriter bufferedWriter;
    private final BufferedOutputStream bos;

    private HashMap<String, String> responseHeader;

    private long startIndex;
    private long endIndex;

    public HttpResponse(
            RequestHeader requestHeader,
            FileService fileService,
            BufferedWriter bufferedWriter,
            BufferedOutputStream bos
    ) {
        this.requestHeader = requestHeader;
        this.fileService = fileService;
        this.bufferedWriter = bufferedWriter;
        this.bos = bos;
        this.sdfGMT = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
        this.sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.setResponseHeader();
    }

    private void setResponseHeader() {
        this.responseHeader = new HashMap<>();
        HashMap<String, String> rangeValues = requestHeader.getRangeValues();

        responseHeader.put("Date", sdfGMT.format(new Date()));
        responseHeader.put("Content-Type", fileService.getContentType());
        responseHeader.put("Content-Length", Long.toString(fileService.getFileLength()));
        responseHeader.put("Content-Disposition", fileService.getContentDisposition());
        responseHeader.put("Connection", "close");
        responseHeader.put("Server", "WW Server Pro");

        if (Objects.equals(requestHeader.getHeaderWithKey("Connection"), "keep-alive")) {
            responseHeader.replace("Connection", "keep-alive");
            responseHeader.put("Keep-Alive", "timeout=" + ClientServer.TIMEOUT + "s, max=1000");
        }
        if (rangeValues != null) {
            String startIndexStr = rangeValues.get("startIndex");
            String endIndexStr = rangeValues.get("endIndex");

            startIndex = (startIndexStr.equals("")) ? 0 : Long.parseLong(startIndexStr);
            endIndex = (endIndexStr.equals("")) ? fileService.getFileLength() : Long.parseLong(endIndexStr);
            responseHeader.put("Content-Range", String.format("%s %d-%d/%d",
                    rangeValues.get("unit"), startIndex, endIndex, fileService.getFileLength()
            ));
            responseHeader.replace("Content-Length", String.valueOf(endIndex - startIndex + 1));
        }
    }

    public HashMap<String, String> getResponseHeader() {
        return this.responseHeader;
    }

    private void writeResponseStatus() throws IOException {
        String responseStatus = (fileService.fileExists) ? "200 OK" : "500 Internal Server Error";
        if (fileService.fileExists && requestHeader.doesHeaderHaveKey("Range")) {
            responseStatus = "206 Partial Content";
        }
        bufferedWriter.write("HTTP/1.1 " + responseStatus + "\r\n");
    }

    public void writeResponseHeader() throws IOException {
        writeResponseStatus();
        for (Map.Entry<String, String> header : responseHeader.entrySet()) {
            bufferedWriter.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
        }
        bufferedWriter.write("\r\n");
        bufferedWriter.flush();
    }

    public void writeResponseBody() throws IOException {
        if (requestHeader.doesHeaderHaveKey("Range")) {
            fileService.writeFileData(bos, startIndex, endIndex);
        }
        fileService.writeFileData(bos);
    }
}
