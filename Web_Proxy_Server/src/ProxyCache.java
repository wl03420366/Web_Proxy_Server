import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyCache {
    /*proxy的port*/
    private static int port;
    /*用於連接client端的socket*/
    private static ServerSocket socket;

    /*創建ProxyCache object 和 socket*/
    public static void init(int p) {
        port = p;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error creating socket: " + e);
            System.exit(-1);
        }
    }

    public static void handle(Socket client) {
        Socket server = null;
        HttpRequest request = null;
        HttpResponse response = null;

        /* 處理 request，如果有產生例外情況，則顯示錯誤訊息及結束request
           同時client端會閒置，直到時間結束*/

        /*讀取request*/
        try {
            BufferedReader fromClient =  new BufferedReader(new InputStreamReader(client.getInputStream()));
            request = new HttpRequest(fromClient);
        } catch (IOException e) {
            System.out.println("Error reading request from client: " + e);
            return;
        }
        /*將訊息送給server request*/
        try {
            /*打開socket並對socket寫入request */
            server = new Socket(request.getHost(), request.getPort());
            DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
            toServer.writeBytes(request.toString());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + request.getHost());
            System.out.println(e);
            return;
        } catch (IOException e) {
            System.out.println("Error writing request to server: " + e);
            return;
        }
        /* 讀取server的response並傳送給client */
        try {
            DataInputStream fromServer = new DataInputStream(server.getInputStream());
            response = new HttpResponse(fromServer);
            DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
            toClient.writeBytes(response.toString());
            toClient.write(response.body);
            client.close();
            server.close();
            /* Insert object into the cache */
            /* Fill in (optional exercise only) */
        } catch (IOException e) {
            System.out.println("Error writing response to client: " + e);
        }
    }


    /*讀取命令行參數並執行proxy*/
    public static void main(String args[]) {
        int myPort = 0;

        try {
            myPort = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Need port number as argument");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("Please give port number as integer.");
            System.exit(-1);
        }

        init(myPort);

        /*主迴圈。
        傾聽傳入的連接並生成一個新thread來處理它們*/
        Socket client = null;

        while (true) {
            try {
                client = socket.accept();
                handle(client);
            } catch (IOException e) {
                System.out.println("Error reading request from client: " + e);
                /*確定不能繼續執行這個request，所以跳往while迴圈的下一次迭代*/
                continue;
            }
        }

    }
}