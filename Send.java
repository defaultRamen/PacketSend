import java.io.IOException;
import java.net.*;
import java.util.*;

public class Send {
    private String hostName;
    private int port;
    private byte[] b;

    public byte[] getBuffer(){
        return this.b;
    }

    public String getHostName(){
        return this.hostName;
    }

    public int getPort(){
        return this.port;
    }

    public void send(){
        System.out.println(StdOut.GREEN + "URL" + StdOut.END + "(プロトコルタイプ://ドメイン の形式)と" + StdOut.CYAN + "ポート番号" + StdOut.END + "を半角区切りで入力してください。");
        try {
            Scanner scn = new Scanner(System.in);
            String input = scn.nextLine();
            String[] cmd = input.split(" ");
            this.hostName = cmd[0];
            this.port = Integer.parseInt(cmd[1]);
            Random r = new Random();
            byte[] b = new byte[60000];
            for(int i = 0; i < b.length; i++){
                b[i] = (byte)r.nextInt(128);
            }

            URL url = new URL(this.hostName);
            URLConnection urlc = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)urlc;
            http.connect();
            String responceMessage = http.getResponseMessage();
            int responceCode = http.getResponseCode();
            String strCode = responceCode + "";
            boolean is4xx = strCode.matches("4.*");
            boolean is5xx = strCode.matches("5.*");
            System.out.print("Receive responce message: ");
            System.out.println(responceMessage);
            if(is4xx){
                System.out.println("\nクライアント側で問題が発生しました。");
                System.out.println("処理を終了します。");
                System.exit(1);
            } else if(is5xx){
                System.out.println("\n接続先サーバーで問題が発生しています。");
                System.out.println("処理を終了します。");
                System.exit(1);
            }
            System.out.println("responce code: " + StdOut.GREEN + responceCode + StdOut.END);
            System.out.println("続行しますか？" + StdOut.GREEN + "true" + StdOut.END + "/" + StdOut.RED + "false" + StdOut.END);
            boolean isNext = scn.nextBoolean();
            if (!isNext){
                System.out.println("処理が中断されました。");
                System.exit(1);
            }
            String[] host = this.hostName.split("//");
            InetAddress ia = InetAddress.getByName(host[1]);
            InetSocketAddress socketAdd = new InetSocketAddress(ia, this.port);
            DatagramPacket dp = new DatagramPacket(b, b.length - 1, socketAdd);
            DatagramSocket ds = new DatagramSocket();
            boolean isBound = ds.isBound();
            System.out.print("\nBind: ");
            if (isBound)
                System.out.println(StdOut.GREEN + "OK" + StdOut.END);
                else {
                    System.out.println(StdOut.RED + "failed");
                    System.out.println("バインドに失敗しました。" + StdOut.END);
                }
            ds.connect(socketAdd);
            boolean isConnected = ds.isConnected();
            System.out.print("Connect: ");
            if (isConnected)
                System.out.println(StdOut.GREEN + "OK" + StdOut.END);
                else {
                System.out.println(StdOut.RED + "failed");
                System.out.println("ソケット接続に失敗しました。" + StdOut.END);
                ds.close();
            }
            
            System.out.print("Send: ");
            ds.send(dp);
            System.out.println(StdOut.GREEN + "OK" + StdOut.END);
            System.out.println("Sent to: " + StdOut.GREEN + socketAdd + StdOut.END);
        
            System.out.print("Closed Socket: ");
            ds.close();
            boolean isClosed = ds.isClosed();
            if(isClosed)
                System.out.println(StdOut.GREEN + "OK" + StdOut.END);
                else {
                System.out.println(StdOut.RED + "failed" + StdOut.END);
                System.out.println("ソケットを閉じる処理で問題が発生しました。" + StdOut.END);
            }
            scn.close();
        } catch (IOException e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 
    }
}