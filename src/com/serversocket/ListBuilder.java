package com.serversocket;

import java.util.ArrayList;
import java.util.HashMap;

public class ListBuilder {
    private final ArrayList<HashMap<String, String>> files;
    private final String urn;
    private final String iconUrn;

    private final String domain;
    private final String root;
    private final int port;

    private StringBuilder Html;

    private static final String[] SIZE_SYMBOL_ORDER = {"B", "KB", "MB", "GB"};
    public static final String ICON_DIR = "list-icons";

    public ListBuilder(String domain, int port, String root, ArrayList<HashMap<String, String>> files, String urn) {
        this.domain = domain;
        this.port = port;
        this.root = root;

        this.files = files;
        this.urn = "/" + urn;
        this.iconUrn = ClientServer.SERVER_ASSETS_DIR + '/' + ICON_DIR;

        this.generateHtml();
    }

    private void generateHtml() {
        Html = new StringBuilder(String.format(
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "   <title>Index of %s</title>\n" +
            "<head>\n" +
            "\n" +
            "<body>\n" +
            "   <h1>Index of %s</h1>\n" +
            "   <table>\n" +
            "       <tbody>\n"
        , this.urn, this.urn));

        Html.append(getTableRows());

        Html.append(String.format(
            "       </tbody>\n" +
            "   </table>\n" +
            "   <address>Server at %s in folder %s on port %d</address>\n" +
            "</body>\n" +
            "</html>\n"
        , domain, root, port));
    }

    private String getTableRows() {
        StringBuilder tableRow = new StringBuilder(String.format(
            "           <tr>\n" +
            "               <th valign=\"top\"><img src=\"/%s/blank.gif\" alt=\"[ICO]\"></th>\n" +
            "               <th>Name</th>\n" +
            "               <th style=\"padding: 0 10px;\">Last modified</th>\n" +
            "               <th>Size</th>\n" +
            "           </tr>\n" +
            "           <tr>\n" +
            "               <th colspan=\"5\"><hr></th>\n" +
            "           </tr>\n"
        , iconUrn));

        // Append option to redirect parent directory.
        if (!urn.equals("/")) {
            String parentUrn = urn.substring(0, urn.lastIndexOf("/"));
            parentUrn = (parentUrn.equals("")) ? "/" : parentUrn;
            tableRow.append(String.format(
                "           <tr>\n" +
                "               <td valign=\"top\"><img src=\"/%s/back.gif\" alt=\"[PARENTDIR]\"></td>\n" +
                "               <td><a href=\"%s\">Parent Directory</a></td>\n" +
                "               <td style=\"padding: 0 10px;\"></td>\n" +
                "               <td align=\"right\">-</td>\n" +
                "           </tr>\n"
            , iconUrn, parentUrn));
        }

        for (HashMap<String, String> file : this.files) {
            boolean isFile = file.get("type").equals("file");
            String alt = (isFile) ? "TXT" : "DIR";
            String iconName = (isFile) ? "text.gif" : "folder.gif";
            String iconPath = String.format("/%s/%s", iconUrn, iconName);

            // Get displayed size.
            String size = "-";
            if (!file.get("size").equals("0")) {
                size = getSize(file.get("size"));
            }

            tableRow.append(String.format(
                "           <tr>\n" +
                "               <td valign=\"top\"><img src=\"%s\" alt=\"[%s]\"></td>\n" +
                "               <td><a href=\"%s\">%s</a></td>\n" +
                "               <td style=\"padding: 0 10px;\">%s</td>\n" +
                "               <td align=\"right\">%s</td>\n" +
                "           </tr>\n"
            , iconPath, alt, file.get("path"), file.get("name"), file.get("lastModified"), size));
        }

        tableRow.append(
            "           <tr>\n" +
            "               <th colspan=\"5\"><hr></th>\n" +
            "           </tr>\n"
        );
        return tableRow.toString();
    }

    private String getSize(String sizeStr) {
        int symbolIdx = 0;
        long size = Long.parseLong(sizeStr);

        while (symbolIdx < SIZE_SYMBOL_ORDER.length && ((size / 1024) > 0)) {
            size /= 1024;
            symbolIdx++;
        }
        return size + " " + SIZE_SYMBOL_ORDER[symbolIdx];
    }

    public String getHtml() {
        return this.Html.toString();
    }
}
