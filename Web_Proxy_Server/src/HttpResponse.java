import java.io.*;
import java.net.*;
import java.util.*;

public class HttpResponse {
    final static String CRLF = "\r\n";
    /*用於讀取對象的緩衝區大小*/
    final static int BUF_SIZE = 8192;
    /*proxy可以處理的對象的最大大小，目前設定為100KB*/
    final static int MAX_OBJECT_SIZE = 100000;
    /*Reply status and headers*/
    String version;
    int status;
    String statusLine = "";
    String headers = "";
    /* Body of reply */
    byte[] body = new byte[MAX_OBJECT_SIZE];

    /*讀取伺服器的response */
    public HttpResponse(DataInputStream fromServer) {
        /*object的長度*/
        int length = -1;
        boolean gotStatusLine = false;

        /*先讀取狀態行與response headers*/
        try {
            String line = fromServer.readLine();
            while (line.length() != 0) {
                if (!gotStatusLine) {
                    statusLine = line;
                    gotStatusLine = true;
                } else {
                    headers += line + CRLF;
                }

                /*獲取由 Content-Length header指示的內容長度。
                 但，並非每個response都有所展示。有一些servers返
                 回的header為“Content-Length”，其他server返回
                 “Content-length”，所以需要檢查這兩者
                 */
                if (line.startsWith("Content-length:") ||
                        line.startsWith("Content-Length:")) {
                    String[] tmp = line.split(" ");
                    length = Integer.parseInt(tmp[1]);
                }
                line = fromServer.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading headers from server: " + e);
            return;
        }

        try {
            int bytesRead = 0;
            byte buf[] = new byte[BUF_SIZE];
            boolean loop = false;

            /*如果沒有get 到 Content-Length header，就重覆迴圈直到關閉連接*/
            if (length == -1) {
                loop = true;
            }

            /* 讀取 BUF_SIZE 主體裡的區塊的BUF_SIZE並將區塊複製到主體中，
               通常用比BUF_SIZE更小的區塊返回回覆。
               當我們讀取Content-Length bytes，
               或當response裡沒有 Connection-Length 而連接關閉的情況下，while迴圈則結束*/
            while (bytesRead < length || loop) {
                /* Read it in as binary data */
                int res = fromServer.read(buf, 0, BUF_SIZE);
                if (res == -1) {
                    break;
                }
                /*將bytes複製到body中，確保不會超過最大的object size*/
                for (int i = 0;
                     i < res && (i + bytesRead) < MAX_OBJECT_SIZE;
                     i++) {
                    body[i + bytesRead] = buf[i];
                }
                bytesRead += res;
            }
        } catch (IOException e) {
            System.out.println("Error reading response body: " + e);
            return;
        }


    }

    /*將response轉換為字串以便於重新發送。 只轉換response headers，body不轉換為字串*/
    public String toString() {
        String res = "";

        res = statusLine + CRLF;
        res += headers;
        res += CRLF;

        return res;
    }
}