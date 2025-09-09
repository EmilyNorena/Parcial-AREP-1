package edu.eci.escuelaing.facade;

import java.net.*;
import java.io.*;

public class HttpServerFacade {
    private static boolean running = true;
    private static final String BACKEND_URL = "http://localhost:35000";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(36000);
        System.out.println("Fachada escuchando en puerto 36000...");

        while (running) {
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            String requestLine = in.readLine();
            if (requestLine == null) continue;

            System.out.println("Solicitud recibida: " + requestLine);
            String requestPath = requestLine.split(" ")[1];

            if (requestPath.startsWith("/setkv") || requestPath.startsWith("/getkv")) {
                String backendResponse = forwardToBackend(requestPath);

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println(backendResponse);
            } else if (("/".equals(requestPath))) {
                out.println("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n\r\n" +
                        getHtmlPage());
            } else {
                out.println("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n\r\n" +
                        "<html><body><h1>404 No se encontro</h1></body></html>");
            }

            out.close();
            in.close();
            clientSocket.close();
        }

        serverSocket.close();
    }

    private static String forwardToBackend(String requestPath) {
        try {
            URL backendURL = new URL(BACKEND_URL + requestPath);
            HttpURLConnection con = (HttpURLConnection) backendURL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Java-HttpClient");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                return content.toString();
            } else {
                return "{ \"error\": \"El backend respondio: " + responseCode + "\" }";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{ \"error\": \"Fallo al conectar con el backend.\" }";
        }
    }

    private static String getHtmlPage() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\" name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>Key-Value Store</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form>\n" +
                "<label for=\"key\">Key:</label><br>\n" +
                "<input type=\"text\" id=\"key\" name=\"key\" value=\"John\"><br><br>\n" +
                "<label for=\"value\">Value:</label><br>\n" +
                "<input type=\"text\" id=\"value\" name=\"value\" value=\"Doe\"><br><br>\n" +
                "<input type=\"button\" value=\"Set Value\" onclick=\"loadSetMsg()\">\n" +
                "<input type=\"button\" value=\"Get Value\" onclick=\"loadGetMsg()\">\n" +
                "</form>\n" +
                "<div id=\"getrespmsg\"></div>\n" +
                "<script>\n" +
                "function loadSetMsg() {\n" +
                "    let keyVar = document.getElementById(\"key\").value;\n" +
                "    let valueVar = document.getElementById(\"value\").value;\n" +
                "    const xhttp = new XMLHttpRequest();\n" +
                "    xhttp.onload = function() {\n" +
                "        document.getElementById(\"getrespmsg\").innerHTML = this.responseText;\n" +
                "    }\n" +
                "    xhttp.open(\"GET\", \"/setkv?key=\" + keyVar + \"&value=\" + valueVar);\n" +
                "    xhttp.send();\n" +
                "}\n" +
                "function loadGetMsg() {\n" +
                "    let keyVar = document.getElementById(\"key\").value;\n" +
                "    const xhttp = new XMLHttpRequest();\n" +
                "    xhttp.onload = function() {\n" +
                "        document.getElementById(\"getrespmsg\").innerHTML = this.responseText;\n" +
                "    }\n" +
                "    xhttp.open(\"GET\", \"/getkv?key=\" + keyVar);\n" +
                "    xhttp.send();\n" +
                "}\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }
}
