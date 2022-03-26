package com.serversocket;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FileService {
    private final int BUFFER_SIZE = 1024;

    private String fetchedFilePath;
    private String contentType;
    private String contentDisposition;

    private int fileLength;
    private byte[] fileData;

    public boolean fileExist;

    public FileService(String domain, int port, String root, String path, String defaultPath, boolean fileExist) throws IOException {
        this.fileExist = fileExist;
        this.fileData = null;
        String fullPath = root + path;

        // If the given path is a file, then fetch the given file.
        if (!isDirectory(fullPath)) {
            this.initializeByFetchedFilePath(fullPath);
            return;
        }

        // If the given path is a directory and has the default file inside it, then fetch the default file.
        if (fileExist(fullPath + "/" + defaultPath)) {
            this.initializeByFetchedFilePath(fullPath + "/" + defaultPath);
            return;
        }

        // List all contents in the given directory & generate the list in html.
        ArrayList<HashMap<String, String>> files = getAllDirectoryContents(root, path);
        ListBuilder listBuilder = new ListBuilder(domain, port, root, files, (path.equals(defaultPath)) ? "" : path);

        this.contentType = "text/html";
        this.contentDisposition = "inline";
        this.fileData = listBuilder.getHtml().getBytes("UTF-8");
        this.fileLength = this.fileData.length;
    }

    private ArrayList<HashMap<String, String>> getAllDirectoryContents(String root, String path) {
        ArrayList<HashMap<String, String>> files = new ArrayList<>();

        File folder = new File(root + path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return files;
        }

        String rootPath = "/" + path + (path.equals("") ? "" : "/");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // Get each file/folder meta data
        for (File file : listOfFiles) {
            HashMap<String, String> data = new HashMap<>();
            long sizeInByte = (file.isFile()) ? file.length() : 0;

            data.put("name", file.getName());
            data.put("path", rootPath + file.getName());
            data.put("lastModified", sdf.format(file.lastModified()));
            data.put("type", (file.isFile()) ? "file" : "folder");
            data.put("size", Long.toString(sizeInByte)); // in byte

            files.add(data);
        }
        return files;
    }

    public static boolean fileExist(String path) {
        return (new File(path)).exists();
    }

    public static boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    public static long getDirectorySize(File dir) {
        long length = 0;
        File[] files = dir.listFiles();
        if (files == null) {
            return length;
        }

        for (File file : files) {
            long adder = (file.isFile()) ? file.length() : getDirectorySize(file);
            length += adder;
        }
        return length;
    }

    private void initializeByFetchedFilePath(String path) throws IOException {
        this.fetchedFilePath = path;

        this.setFileLength();
        this.setContentType();
        this.setContentDisposition();
    }

    private void setFileLength() throws IOException {
        this.fileLength = (int) Files.size(Path.of(this.fetchedFilePath));
    }

    private void setContentType() throws IOException {
        String type = Files.probeContentType(Path.of(this.fetchedFilePath));

        // Handle Javascript type and set default type to text/plain if mime type isn't found.
        if (type == null || type.equals("")) {
            File file = new File(this.fetchedFilePath);
            String filename = file.getName();

            int idx = filename.lastIndexOf(".");
            type = (filename.substring(idx + 1).equals("js")) ? "application/javascript" : "text/plain";
        }
        this.contentType = type;
    }

    public void writeFileData(BufferedOutputStream bos) throws IOException {
        if (this.fileData != null) {
            bos.write(this.fileData, 0, this.fileLength);
            bos.flush();
            return;
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.fetchedFilePath));
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while (bis.available() > 0) {
            bytesRead = bis.read(buffer);
            bos.write(buffer, 0, bytesRead);
        }
        bos.flush();
    }

    public void setContentDisposition() {
        this.contentDisposition = (this.contentType.split("/")[0].equals("text")) ? "inline" : "attachment";
    }

    public String getContentDisposition() {
        return this.contentDisposition;
    }

    public String getContentType() {
        return this.contentType;
    }

    public int getFileLength() {
        return this.fileLength;
    }
}
