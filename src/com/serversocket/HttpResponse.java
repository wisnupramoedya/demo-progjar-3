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
    private BufferedOutputStream bos;

    private HashMap<String, String> responseHeader;

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
        if (requestHeader.getHeaderWithKey("Connection").equals("keep-alive")) {
            responseHeader.replace("Connection", "keep-alive");
            responseHeader.put("Keep-Alive", "timeout=" + ClientServer.TIMEOUT + "s, max=1000");
        }
        responseHeader.put("Server", "WW Server Pro");
    }

    public HashMap<String, String> getResponseHeader() {
        return this.responseHeader;
    }

    public void writeResponseHeader() throws IOException {
        String responseStatus = (fileService.fileExists) ? "200 OK" : "500 Internal Server Error";
        bufferedWriter.write("HTTP/1.1 " + responseStatus + "\r\n");

        for (Map.Entry<String, String> header : responseHeader.entrySet()) {
            bufferedWriter.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
        }
        bufferedWriter.write("\r\n");
        bufferedWriter.flush();
    }

    public void writeResponseBody() throws IOException {
        fileService.writeFileData(bos);
    }
}
