package com.siggytech.utils.notificatorlib;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by fsoto on 9/13/19.
 */

public class NotificatorSocket extends AppCompatActivity {
    public static final String BROADCAST = "com.siggy.notificator.MessengerBroadcastReceiver";
    private int LocalPort = 80;
    private final Handler mHandler;
    private UDPThread SocketRecv;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private MessengerService messengerService;
    Intent mServiceIntent;
    Context context;
    public String packageName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
    private void register()
    {

        //setContentView(R.layout.activity_main);
        messengerService = new MessengerService(context);
        //messengerService.LocalPort = 1984;

        mServiceIntent = new Intent(context, messengerService.getClass());
        if (!isMyServiceRunning(messengerService.getClass())) {
            //startService(mServiceIntent);
            mServiceIntent.putExtra("packageName", packageName);
            context.startService(mServiceIntent);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    public void onStop() {
        stopService(new Intent(this, MessengerService.class));
    }

    public NotificatorSocket(Context context ,Handler handler, int localport, String packageName){

        this.packageName = packageName;
        this.context = context;

        Intent broadcastedIntent = new Intent(context, MyScheduleReceiver.class);
        broadcastedIntent.putExtra("FREC", 10);//en segundos
        broadcastedIntent.putExtra("packageName", packageName);
        context.sendBroadcast(broadcastedIntent);

        //register();

        LocalPort = localport;
        mHandler = handler;
        if(localport <= 1024){
            Log.e("UDP", "UDPSocket:端口号小于1024 ");
        }
    }

    public void startRecv(){      // 监听端口
        SocketRecv = new UDPThread();
        SocketRecv.setRecv_Flag(true);
        SocketRecv.start();
    }

    public void Send(String message,String address,int port){
        SocketRecv.setRecv_Flag(false);
        UDPThread SocketSend = new UDPThread();
        SocketSend.write(message,address,port);
        SocketSend.start();
    }

    private class UDPThread extends Thread{
        private DatagramSocket mm_socket;
        private byte[] mm_data;
        private boolean Send_Flag = false;
        private boolean Recv_Flag = false;
        private String des_Address;
        private int desPort;

        UDPThread( ){   // 本地端口
            if(mm_socket == null){
                try {
                    mm_socket = new DatagramSocket(null);
                    mm_socket.setReuseAddress(true);
                    mm_socket.bind(new InetSocketAddress(LocalPort));
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override public void run() {
            if(Send_Flag){
                SendDataToServer();
                Send_Flag = false;
            }
            while (Recv_Flag) {
                ReceiveServerSocketData();
            }
        }

        private   void setRecv_Flag(boolean recv_Flag){
            Recv_Flag = recv_Flag;
        }

        private void write(String command,String address,int port){
            des_Address = address;
            desPort     = port;
            mm_data     = command.getBytes();
            Send_Flag   = true;
        }

        private void SendDataToServer() {
            try{
                InetAddress serverAddress = InetAddress.getByName(des_Address);
                DatagramPacket packet = new DatagramPacket(mm_data,mm_data.length,
                        serverAddress,desPort);
                mm_socket.send(packet);//Enviar los datos al servidor. 
                mHandler.obtainMessage(MESSAGE_WRITE,packet.getLength(),
                        -1,packet.getData()).sendToTarget();
            }catch(SocketException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        private void ReceiveServerSocketData() {
            try {
                //实例化的端口号要和发送时的socket一致，否则收不到data  
                byte data[]=new byte[4*1024];
                //参数一:要接受的data 参数二：data的长度  
                DatagramPacket packet = new DatagramPacket(data,data.length);
                mm_socket.receive(packet);
                mHandler.obtainMessage(MESSAGE_READ,packet.getLength(),
                        -1,packet.getData()).sendToTarget();
            }catch(SocketException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public void addNotification(String title, String text, final Class<? extends Activity> ActivityToOpen) {
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(text)
                ;

        // Creates the intent needed to show the notification
        Intent notificationIntent = new Intent(context, ActivityToOpen);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on...........", ""+isScreenOn);
        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
    }
    // 使用TCP协议通信
    protected void connectServerWithTCPSocket(String msg,String addrServer,int localPort) {
        Socket socket;
        try {
            socket = new Socket(addrServer, localPort);
            // 获取Socket的OutputStream对象用于发送数据。  
            OutputStream outputStream = socket.getOutputStream();
            byte buffer[] = msg.getBytes();
            // 发送读取的数据到服务端  
            outputStream.flush();
        }  catch (UnknownHostException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // TCP服务器
    public void ServerReceviedByTcp() {
        // 声明一个ServerSocket对象  
        ServerSocket serverSocket = null;
        try{
            // 创建一个ServerSocket对象，并让这个Socket在1989端口监听  
            serverSocket = new ServerSocket(1989);
            // 调用ServerSocket的accept()方法，接受客户端所发送的请求，  
            // 如果客户端没有发送数据，那么该线程就停滞不继续  
            Socket socket = serverSocket.accept();
            // 从Socket当中得到InputStream对象  
            InputStream inputStream = socket.getInputStream();
            byte buffer[] = new byte[1024 * 4];
            int temp = 0;
            // 从InputStream当中读取客户端所发送的数据  
            while ((temp = inputStream.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, temp));
            }
            serverSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}