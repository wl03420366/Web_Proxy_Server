import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest {
    /*變量設定*/
    final static String CRLF = "\r\n";
    final static int HTTP_PORT = 80;
    /*宣告字串參數*/
    String method;
    String URI;
    String version;
    String headers = "";
    /*宣告host與port*/
    private String host;
    private int port;

    /*透過讀取client端的socket來創建HttpRequest*/
    public HttpRequest(BufferedReader from) {
        String firstLine = "";
        try {
            firstLine = from.readLine();
        } catch (IOException e) {
            System.out.println("Error reading request line: " + e);
        }

        String[] tmp = firstLine.split(" ");
        method = tmp[0];
        URI = tmp[1];
        version = tmp[2];

        System.out.println("URI is: " + URI);

        if (!method.equals("GET")) {
            System.out.println("Error: Method not GET");
        }
        try {
            String line = from.readLine();
            while (line.length() != 0) {
                headers += line + CRLF;
                /*需要找到host header來了解在請求的URI不完整時，該聯繫哪個伺服器 */
                if (line.startsWith("Host:")) {
                    tmp = line.split(" ");
                    if (tmp[1].indexOf(':') > 0) {
                        String[] tmp2 = tmp[1].split(":");
                        host = tmp2[0];
                        port = Integer.parseInt(tmp2[1]);
                    } else {
                        host = tmp[1];
                        port = HTTP_PORT;
                    }
                }
                line = from.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading from socket: " + e);
            return;
        }
        System.out.println("Host to contact is: " + host + " at port " + port);
    }

    /*返回request到所對應的主機*/
    public String getHost() {
        return host;
    }

    /*返回port給server*/
    public int getPort() {
        return port;
    }

    /*將request轉換為字串以便於重新發送*/
    public String toString() {
        String req = "";

        req = method + " " + URI + " " + version + CRLF;
        req += headers;
        /* 這項proxy不支援永久連結*/
        req += "Connection: close" + CRLF;
        req += CRLF;

        return req;
    }
}