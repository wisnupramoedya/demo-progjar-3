package com.serversocket;

import javax.naming.ConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class ClientServer implements Runnable {
    public static final String SERVER_ROOT = "./src/com/serversocket/";
    public static final String SERVER_ASSETS_DIR = "server-assets";
    public static final float TIMEOUT = 2.5F; // in seconds

    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "500.html";

    private final Socket client;
    private final ConfigService configService;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private BufferedOutputStream bos;

    public ClientServer(Socket client, ConfigService configService) {
        this.client = client;
        this.configService = configService;
    }

    /**
     * Server user request.
     */
    public void run() {
        try {
            System.out.format("[%s] Accepted\n", new Date());

            // Create buffer
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            bos = new BufferedOutputStream(client.getOutputStream());
            String connectionFromRequest;

            // Loop if user does not ask to close
            do {
                RequestHeader requestHeader = new RequestHeader(bufferedReader);

                System.out.format("[%s] %s\n", new Date(), requestHeader.getRequestStatus());
                connectionFromRequest = requestHeader.getHeaderWithKey("Connection");

                // Adjust client socket if client request has keep alive connection header.
                if (connectionFromRequest.equals("keep-alive")) {
                    client.setKeepAlive(true);
                    client.setTcpNoDelay(true);
                    client.setSoTimeout((int) (TIMEOUT * 1000));
                }
                FileService fileService = getRequestedFile(requestHeader);

                // Throw exception on invalid range header
                if (!requestHeader.validRangeValues(fileService.getFileLength())) {
                    throw new Exception("Invalid range request headers");
                }

                HttpResponse httpResponse = new HttpResponse(requestHeader, fileService, bufferedWriter, bos);
                httpResponse.writeResponseHeader();
                httpResponse.writeResponseBody();

            } while (!connectionFromRequest.equals("close"));
        }
        catch (SocketTimeoutException e) {}
        catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            }
            System.out.format("[%s] Closed\n", new Date());
        }
    }

    /**
     * Create file service based on request header.
     *
     * @param requestHeader
     * @return FileService
     * @throws ConfigurationException
     * @throws IOException
     */
    private FileService getRequestedFile(RequestHeader requestHeader) throws ConfigurationException, IOException {
        String requestedFile = requestHeader.getRequestedFile();
        String hostFromRequest = requestHeader.getHeaderWithKey("Host");
        String documentRoot = getDocumentRoot(hostFromRequest, requestedFile);

        boolean fileExists = FileService.fileExist(documentRoot + requestedFile);
        String fetchedFile = (fileExists) ? requestedFile : FILE_NOT_FOUND;
        documentRoot = (fileExists) ? documentRoot : (SERVER_ROOT + SERVER_ASSETS_DIR + '\\');

        return new FileService(
                hostFromRequest, configService.getPort(), documentRoot, fetchedFile, DEFAULT_FILE, fileExists
        );
    }

    private String getDocumentRoot(String domain, String requestedFile) throws ConfigurationException {
        if (getFirstDirFromPath(requestedFile).equals(SERVER_ASSETS_DIR)) {
            return SERVER_ROOT;
        }
        String documentRoot = configService.getSettingsWithKey(domain);
        return (documentRoot != null && documentRoot.equals(".")) ? "./" : documentRoot;
    }

    private String getFirstDirFromPath(String path) {
        if (path.equals("")) {
            return "";
        }
        return path.split("/")[0];
    }
}
