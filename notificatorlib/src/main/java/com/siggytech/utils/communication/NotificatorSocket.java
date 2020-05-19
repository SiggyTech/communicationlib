package com.siggytech.utils.communication;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class NotificatorSocket extends AppCompatActivity {
    public static final String BROADCAST = "com.siggy.notificator.MessengerBroadcastReceiver";
    private UDPThread SocketRecv;
    private MessengerService messengerService;
    Intent mServiceIntent;
    Context context;
    public String packageName;
    private int localport = 1984;

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
        super.onStop();
        stopService(new Intent(this, MessengerService.class));
    }

    public NotificatorSocket(Context context, String packageName, String messageTittle, String messageText){

        this.packageName = packageName;
        this.context = context;

        Intent broadcastedIntent = new Intent(context, MyScheduleReceiver.class);
        broadcastedIntent.putExtra("FREC", 10);//en segundos
        broadcastedIntent.putExtra("packageName", packageName);
        broadcastedIntent.putExtra("messageTittle", messageTittle);
        broadcastedIntent.putExtra("messageText", messageText);


        context.sendBroadcast(broadcastedIntent);

        //register();


        if(localport <= 1024){
            Log.e("UDP", "UDPSocket:1024 ");
        }
    }



    private class UDPThread extends Thread{
        private DatagramSocket mm_socket;
        private byte[] mm_data;
        private boolean Send_Flag = false;
        private boolean Recv_Flag = false;
        private String des_Address;
        private int desPort;

        UDPThread( ){
            if(mm_socket == null){
                try {
                    mm_socket = new DatagramSocket(null);
                    mm_socket.setReuseAddress(true);
                    mm_socket.bind(new InetSocketAddress(localport));
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}