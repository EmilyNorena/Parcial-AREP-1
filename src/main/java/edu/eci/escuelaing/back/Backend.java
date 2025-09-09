package edu.eci.escuelaing.back;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
public class Backend {

    private static final Map<String, String> kvStore = new HashMap<>();
    private static boolean running = true;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(35000);
        System.out.println("Listo para recibir ...");

        while (running) {
            try (
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                String requestLine = in.readLine();
                if (requestLine == null) continue;

                System.out.println("Solicitud recibida: " + requestLine);
                String path = requestLine.split(" ")[1];

                String response = handleRequest(path);

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        serverSocket.close();
    }

    private static String handleRequest(String path) {
        if (path.startsWith("/setkv")) {
            try {
                Map<String, String> queryParams = parseQueryParams(path);
                String key = queryParams.get("key");
                String value = queryParams.get("value");
                if (key == null || value == null || key.isEmpty() || value.isEmpty()) {
                    return "{ \"error\": \"Parámetros 'key' y 'value' son requeridos y deben ser strings.\" }";
                }

                boolean replaced = kvStore.containsKey(key);
                kvStore.put(key, value);

                return String.format(
                        "{ \"key\": \"%s\", \"value\": \"%s\", \"status\": \"%s\" }",
                        key, value, replaced ? "replaced" : "created"
                );

            } catch (Exception e) {
                return "{ \"error\": \"Solicitud malformada\" }";
            }
        } else if (path.startsWith("/getkv")) {
            try {
                Map<String, String> queryParams = parseQueryParams(path);
                String key = queryParams.get("key");
                if (key == null || key.isEmpty()) {
                    return "{ \"error\": \"Parámetro 'key' es requerido y debe ser string.\" }";
                }

                String value = kvStore.get(key);
                if (value == null) {
                    return String.format(
                            "{ \"error\": \"Clave '%s' no encontrada\" }", key);
                }

                return String.format(
                        "{ \"key\": \"%s\", \"value\": \"%s\" }",
                        key, value
                );

            } catch (Exception e) {
                return "{ \"error\": \"Solicitud malformada\" }";
            }
        }

        return "{ \"error\": \"Ruta no válida\" }";
    }

    private static Map<String, String> parseQueryParams(String path) throws UnsupportedEncodingException {
        Map<String, String> queryParams = new HashMap<>();
        if (path.contains("?")) {
            String[] parts = path.split("\\?");
            if (parts.length > 1) {
                String[] params = parts[1].split("&");
                for (String param : params) {
                    String[] keyVal = param.split("=");
                    if (keyVal.length == 2) {
                        queryParams.put(URLDecoder.decode(keyVal[0], "UTF-8"),
                                URLDecoder.decode(keyVal[1], "UTF-8"));
                    }
                }
            }
        }
        return queryParams;
    }
    }


