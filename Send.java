package iro2.attack;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.net.*;
import java.util.*;

final class StdOut{
    public final static String RED = "\u001b[00;31m";
    public final static String GREEN  = "\u001b[00;32m";
    public final static String YELLOW = "\u001b[00;33m";
    public final static String PURPLE = "\u001b[00;34m";
    public final static String PINK   = "\u001b[00;35m";
    public final static String CYAN   = "\u001b[00;36m";   
    public final static String END    = "\u001b[00m";
}

public class Send {
    private String hostName;
    private int port;
    private byte[] buffer;
    private String option;
    private String optionPath;

    // getterは不必要？ とりあえず残す
    public String getOptionPath(){
        return this.optionPath;
    }

    public String getOption(){
        return this.option;
    }

    public byte[] getBuffer(){
        return this.buffer;
    }

    public String getHostName(){
        return this.hostName;
    }

    public int getPort(){
        return this.port;
    }

    public void send(){
        System.out.println(StdOut.GREEN + "URL" + StdOut.END + "(プロトコルタイプ://ドメイン の形式)と" + StdOut.CYAN + "ポート番号" + StdOut.END + "を半角区切りで入力してください。");
        System.out.println("Option:");
        System.out.println("-sオプションは、送信したデータを任意のパスに任意の名前で保存できます。");
        System.out.println("また、-sdオプションは、保存したデータファイルに書き込み時刻を書き込みます。");
        System.out.println("保存するには、以下の形式で操作を完了できます。");
        System.out.println("URL ポート番号 -s/-sd ディレクトリパス/ファイルパス");
        System.out.println("例: C:\\Users\\adminminzemi\\heDesktop\\buffa-deta-.txt");
        try {
            Scanner scn = new Scanner(System.in);
            String input = scn.nextLine();
            String[] cmd = input.split(" ");
            this.hostName = cmd[0];
            this.port = Integer.parseInt(cmd[1]);
            this.option = cmd[2];
            this.optionPath = cmd[3];
            Path savePath = Paths.get(this.optionPath);
            if(Files.isDirectory(savePath)){
                System.out.println("指定されたパスはディレクトリです。 -sオプションでは、ディレクトリパスの後にバッファデータ保存用のファイルパス(ファイル名.拡張子) をつける必要があります。");
                System.out.println("例: C:\\Users\\User\\Desktop\\buf_data.txt");
                System.exit(1);
            }
            Random r = new Random();
            this.buffer = new byte[60000];
            for(int i = 0; i < this.buffer.length; i++){
                this.buffer[i] = (byte)r.nextInt(128);
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
            DatagramPacket dp = new DatagramPacket(this.buffer, this.buffer.length - 1, socketAdd);
            DatagramSocket ds = new DatagramSocket();
            boolean isBound = ds.isBound();
            System.out.print("\nBind: ");
            if (isBound)
                System.out.println(StdOut.GREEN + "OK" + StdOut.END);
                else {
                    System.out.println(StdOut.RED + "failed");
                    System.out.println("バインドに失敗しました。" + StdOut.END);
                    System.exit(1);
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
                System.exit(1);
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
                System.exit(1);
            }
            scn.close();
            if(this.option.equals("-s") || this.option.equals("-sd")){ 
                File f = new File(this.optionPath);
                FileOutputStream fos = new FileOutputStream(f);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                String byteToString;
                if (this.option.equals("-sd")){
                    LocalDateTime ldt = LocalDateTime.now();
                    int year = ldt.getYear();
                    int month = ldt.getMonthValue();
                    int date = ldt.getDayOfMonth();
                    int hour = ldt.getHour();
                    int minute = ldt.getMinute();
                    osw.write("書き込み時刻: " + year + "年" + month + "月" + date + "日" + hour + "時" + minute + "分\n\n");
                }
                for(byte byteBuffer : this.buffer){
                    byteToString = byteBuffer + "";
                    osw.write(byteToString + " ");
                }
                osw.close();
                System.out.println("\n送信したデータをテキストファイルに保存しました: " + f.toString());
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 
    }

    public static void main(String[] args) {
        Send s = new Send();
        s.send();
    }
}
