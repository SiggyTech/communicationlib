package com.siggytech.utils.notificatorlib;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;


public class MessengerService extends Service {
    public int counter=0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private UDPThread SocketRecv;
    public int LocalPort = 1984;
    public Context context;

    public static String packageName;

    public MessengerService(Context applicationContext) {
        super();
        context = applicationContext;
        Log.i("HERE", "here I am!");
    }

    public MessengerService() {
    }
    @Override
    public void onStart(Intent intent, int startid) {
        context = getApplicationContext();//probando...
        try {

            Bundle extras = intent.getExtras();

            if (extras == null) {
                Log.d("Service", "null");
            } else {
                Log.d("Service", "not null");

                packageName = extras.get("packageName").toString();

            }
        }
        catch(Exception ex)
        {
            Log.d("intent.getExtras", "Error: " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        context = getApplicationContext();//probando...
        try {

            Bundle extras = intent.getExtras();

            if (extras == null) {
                Log.d("Service", "null");
            } else {
                Log.d("Service", "not null");

                packageName = extras.get("packageName").toString();

            }
        }
        catch(Exception ex)
        {
            Log.d("intent.getExtras", "Error: " + ex.getMessage());
        }



        startTimer();
        startRecv();

        //context = getApplicationContext();




        return START_STICKY;
    }
    public void startRecv(){

        Log.i("in startRecv", "in!");
        //Log.i("packageName", packageName);

        SocketRecv = new UDPThread();
        SocketRecv.setRecv_Flag(true);
        SocketRecv.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //

        startRecv();
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //Log.i("in timer", "in timer ++++  "+ (counter++));

            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                    Log.i("UDPThread", "LocalPort "+ LocalPort);
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
    private final Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_WRITE:
                    byte[]writeBuf =(byte[])msg.obj;
                    String writeMessage=new String(writeBuf);
                    startRecv();
                    break;
                case MESSAGE_READ:
                    byte[]readBuf =(byte[])msg.obj;
                    String readMessage=new String(readBuf,0,msg.arg1);
                    //addNotification(); //muestro notificación cuando llega mensaje
                    addNotification("Titulo del Mensaje","Este es el texto del mensaje",packageName);

                    break;
            }
        }
    };
    public void addNotification(String title, String text, String packageName) {


        PackageManager pmg = context.getPackageManager();
        String name = "";
        Intent LaunchIntent = null;

        Log.d("+++packageName", packageName);

        try {
            if (pmg != null) {
                ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageName, 0);
                name = (String) pmg.getApplicationLabel(app);
                LaunchIntent = pmg.getLaunchIntentForPackage(packageName);
            }
            Log.d("intent.getExtras", "Found!: " + name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = LaunchIntent; // new Intent();

        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pIntent)
                .setSound(uri)
                ;

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on.........", ""+isScreenOn);
        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
    }

}
