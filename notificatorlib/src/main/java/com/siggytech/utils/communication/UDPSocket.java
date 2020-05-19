package com.siggytech.utils.communication;

import android.os.Handler;
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

public class UDPSocket{
    private int LocalPort = 80;
    private final Handler mHandler;
    private UDPThread SocketRecv;


    UDPSocket(Handler handler, int localport){
        LocalPort = localport;
        mHandler = handler;
        if(localport <= 1024){
            Log.e("UDP", "UDPSocket");
        }
    }

    public void startRecv(){      //
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

        UDPThread( ){
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
                mm_socket.send(packet);//Enviar los datos al servidor
                mHandler.obtainMessage(PTTButton.MESSAGE_WRITE,packet.getLength(),
                        -1,packet.getData()).sendToTarget();
            }catch(SocketException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        private void ReceiveServerSocketData() {
            try {
                //byte data[]=new byte[4 * 1024];
                byte data[]=new byte[3584];

                DatagramPacket packet = new DatagramPacket(data,data.length);
                mm_socket.receive(packet);
                mHandler.obtainMessage(PTTButton.MESSAGE_READ,packet.getLength(),
                        -1,packet.getData()).sendToTarget();
            }catch(SocketException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    protected void connectServerWithTCPSocket(String msg,String addrServer,int localPort) {
        Socket socket;
        try {
            socket = new Socket(addrServer, localPort);

            OutputStream outputStream = socket.getOutputStream();
            byte buffer[] = msg.getBytes();

            outputStream.flush();
        }  catch (UnknownHostException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void ServerReceviedByTcp() {

        ServerSocket serverSocket = null;
        try{

            serverSocket = new ServerSocket(1989);

            Socket socket = serverSocket.accept();

            InputStream inputStream = socket.getInputStream();
            byte buffer[] = new byte[1024 * 4];
            int temp = 0;
            while ((temp = inputStream.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, temp));
            }
            serverSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

