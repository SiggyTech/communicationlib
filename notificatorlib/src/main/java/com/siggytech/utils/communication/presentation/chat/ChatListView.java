package com.siggytech.utils.communication.presentation.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.siggytech.utils.communication.R;
import com.siggytech.utils.communication.databinding.ChatRecyclerViewBinding;
import com.siggytech.utils.communication.model.ChatModel;
import com.siggytech.utils.communication.model.EventMessageModel;
import com.siggytech.utils.communication.model.GroupModel;
import com.siggytech.utils.communication.model.MessageModel;
import com.siggytech.utils.communication.model.QueueRequestModel;
import com.siggytech.utils.communication.model.async.ApiAsyncTask;
import com.siggytech.utils.communication.model.async.ApiEnum;
import com.siggytech.utils.communication.model.async.ApiListener;
import com.siggytech.utils.communication.model.async.AsyncTaskCompleteListener;
import com.siggytech.utils.communication.model.async.CallTask;
import com.siggytech.utils.communication.model.async.TaskMessage;
import com.siggytech.utils.communication.model.repo.DbHelper;
import com.siggytech.utils.communication.model.repo.MessageRaw;
import com.siggytech.utils.communication.presentation.MessengerHelper;
import com.siggytech.utils.communication.presentation.PaginationScrollListener;
import com.siggytech.utils.communication.presentation.register.Siggy;
import com.siggytech.utils.communication.presentation.service.MessengerReceiver;
import com.siggytech.utils.communication.presentation.service.Socket;
import com.siggytech.utils.communication.util.AESUtils;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.DateUtil;
import com.siggytech.utils.communication.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.siggytech.utils.communication.presentation.service.MessengerReceiver.MESSAGE_CHAT;
import static com.siggytech.utils.communication.presentation.service.MessengerReceiver.MESSAGE_CHAT_ID;


public class ChatListView extends FrameLayout implements AsyncTaskCompleteListener<TaskMessage> {
    private static final String TAG = ChatListView.class.getSimpleName();
    private List<ChatModel> lsChat = new ArrayList<>();
    private EventMessageModel eventMessageModel;
    private MessageModel model;

    private Socket socketSend;
    private final String deviceToken;
    private Gson gson;

    private ChatRecyclerViewBinding mBinding;
    private LayoutInflater inflater;
    private long idGroup;
    private DbHelper dbHelper;
    private String apiKey;
    private int limitCount = 10;
    private StrictMode.ThreadPolicy policy = null;

    //helpers pagination
    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    private long TOTAL_PAGES = 1;

    //listener socket
    private OkHttpClient messengerClient;
    private Request requestSocketListener;
    private WebSocketListener webSocketListenerMessenger;
    private MessageRaw messageRaw;
    private final ApiListener<TaskMessage> apiListener;

    private Lifecycle.Event lifecycleEvent;
    private InputMethodManager inputMethodManager;
    private HeaderListener headerListener;


    public ChatListView (Context context, long idGroup, String API_KEY,String deviceToken, HeaderListener headerListener){
        super(context);
        this.gson = Utils.getGson();
        this.idGroup = idGroup;
        this.dbHelper = new DbHelper(getContext());
        this.apiKey = API_KEY;
        this.deviceToken = deviceToken;
        this.apiListener = getApiListener();
        this.headerListener = headerListener;

        initLayout();
        addLastMessages();
        setAdapter();
    }

    private void initLayout() {
        try {
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mBinding = DataBindingUtil.inflate(inflater, R.layout.chat_recycler_view, null, false);

            if(headerListener!=null)
               headerListener.onTitleChanged(getNameGroup());

            this.addView(mBinding.getRoot());
        }catch (Exception e){
            e.printStackTrace();
            Utils.traces("Error on chatListView init layout: "+Utils.exceptionToString(e));
        }

    }

    private ApiListener<TaskMessage> getApiListener() {
         return new ApiListener<TaskMessage>() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void onPostExecute(TaskMessage result) {
                try {
                    if (!result.isError()
                            && MessengerHelper.getChatQueue() != null
                            && !MessengerHelper.getChatQueue().isEmpty()) {

                        for (EventMessageModel e : MessengerHelper.getChatQueue()) {
                            model = null;
                            model = gson.fromJson(AESUtils.decText(e.getData().getText()),MessageModel.class);
                            lsChat.add(new ChatModel(insertMessage(e), model, e.getData().getFrom(), DateUtil.getStringDate(e.getTimeMark()),false));
                            notifyItemInserted();
                        }

                        MessengerHelper.clearChatQueue();
                    }
                }catch (Exception e){
                    Utils.traces(Utils.exceptionToString(e));
                }
            }

            @Override
            public void onCancelled(TaskMessage result) {

            }
        };
    }

    private void addLastMessages() {
        getTotalPagesCount();
        addRawList(dbHelper.getMessage(idGroup,apiKey, 0, limitCount),false);
    }

    /**
     * To gets total pages count
     */
    private void getTotalPagesCount() {
        try {
            long rowCount = dbHelper.getMessageCount(idGroup, apiKey);
            if(rowCount>0)
                TOTAL_PAGES = (int) Math.ceil((double)rowCount / limitCount);

            if(TOTAL_PAGES==0) TOTAL_PAGES++;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        mBinding.recyclerChat.setLayoutManager(linearLayoutManager);
        mBinding.recyclerChat.setHasFixedSize(true);
        mBinding.recyclerChat.setAdapter(new CustomAdapterBubble(lsChat,getContext()));
        mBinding.recyclerChat.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                ChatListView.this.post(() -> loadNextPage());
            }

            @Override
            public long getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public void hideKeyboard() {
                getInputMethodManager().hideSoftInputFromWindow(getWindowToken(), 0);
                clearFocus();
            }
        });

        Objects.requireNonNull(mBinding.recyclerChat.getLayoutManager()).scrollToPosition(Objects.requireNonNull(mBinding.recyclerChat.getAdapter()).getItemCount()-1);
    }

    private void notifyItemInserted(){
        Objects.requireNonNull(mBinding.recyclerChat.getAdapter()).notifyItemInserted(lsChat.size() - 1);
        Objects.requireNonNull(mBinding.recyclerChat.getLayoutManager()).scrollToPosition(mBinding.recyclerChat.getAdapter().getItemCount()-1);
    }

    /**
     * Returns an {@link InputMethodManager}
     *
     * @return input method manager
     */
    public InputMethodManager getInputMethodManager() {
        if (null == inputMethodManager) {
            inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return inputMethodManager;
    }

    public long getIdGroup() {
        return idGroup;
    }

    public void sendMessage(String from, String encryptedData, String msgPart, String type, long idGroup){
        try{
            Utils.traces(TAG+" sendMessage");
            long dateTime = Calendar.getInstance().getTimeInMillis();
            if(socketSend==null)
                startSendSocketConnection();

            socketSend.sendOnOpen(type, "{\n" +
                    "    \"from\": \"" + from + "\",\n" +
                    "    \"text\": \"" + encryptedData + "\", \n" +
                    "    \"dateTime\": \"" + dateTime + "\", \n" +
                    "    \"msgpart\": \"" + msgPart + "\", \n" +
                    "    \"idGroup\": \"" + idGroup + "\" \n" +
                    "}");

            MessageRaw messageRaw = new MessageRaw();
            messageRaw.setUserKey(apiKey);
            messageRaw.setIdGroup(String.valueOf(idGroup));
            messageRaw.setFrom(from);
            messageRaw.setMessage(encryptedData);
            messageRaw.setTimeMark(dateTime);
            messageRaw.setMine(1);
            messageRaw.setSend(1);

            long id = dbHelper.insertMessage(messageRaw);

            addChat(encryptedData,id,dateTime);

        } catch(Exception e){
            Utils.traces("ChatListView sendMessage : "+Utils.exceptionToString(e));
        }
    }

    private void addChat(String encryptedData,long id,long dateTime){
        try {
            model = null;
            model = gson.fromJson(AESUtils.decText(encryptedData), MessageModel.class);
            lsChat.add(new ChatModel(id, model, Conf.LOCAL_USER, DateUtil.getStringDate(dateTime), true));
            notifyItemInserted();
        }catch (Exception e){
            Utils.traces(Utils.exceptionToString(e));
        }
    }

    public void callToBase64(MessageModel messageModel){
        try{
            new CallTask(getContext(),this).execute(messageModel);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskCompleted(TaskMessage result) {
        try {
            if (result != null && result.getMessageModel() != null) {
                Utils.traces(TAG+" onTaskCompleted sendMessage");
                sendMessage(result.getMessageModel().getFrom()
                        ,AESUtils.encText(gson.toJson(result.getMessageModel()))
                        ,getGenericMessage(result.getMessageModel().getType())
                        ,result.getMessageModel().getType(),
                        idGroup);
            }
        }catch (Exception e){
            Utils.traces("ChatListView onTaskCompleted : "+Utils.exceptionToString(e));
        }
    }

    public void onMessageReceiver(String text,long id){
        try {

            Utils.traces(TAG+" onMessageReceiver");
            if(gson==null) gson = Utils.getGson();

            eventMessageModel = null;
            eventMessageModel = gson.fromJson(text, EventMessageModel.class);

            model = null;
            model = gson.fromJson(AESUtils.decText(eventMessageModel.getData().getText()),MessageModel.class);

            //if message is for the current group info
            if(idGroup == Long.parseLong(eventMessageModel.getData().getIdGroupFrom())){
                lsChat.add(new ChatModel(id, model, eventMessageModel.getData().getFrom(), DateUtil.getStringDate(eventMessageModel.getTimeMark()),false));
                notifyItemInserted();
            }
        } catch(Exception ex){
            Utils.traces("ChatListView onMessageReceiver : "+Utils.exceptionToString(ex));
        }
    }


    private String getGenericMessage(String type){
        switch (type){
            case Utils.MESSAGE_TYPE.AUDIO:
                return getContext().getString(R.string.audio_message);
            case Utils.MESSAGE_TYPE.VIDEO:
                return getContext().getString(R.string.video_message);
            case Utils.MESSAGE_TYPE.PHOTO:
                return getContext().getString(R.string.image_message);
            case Utils.MESSAGE_TYPE.FILE:
            default:
                return getContext().getString(R.string.file_message);

        }
    }


    /**
     * Call this to change chat group
     * @param idGroup id group to see
     * @param limit limit records to retrieve
     */
    public void setGroupView(long idGroup, int limit){
        try{
            int pos = 0;
            for(GroupModel g : MessengerHelper.getGroupList()){
                if(g.idGroup == idGroup){
                    break;
                }
                pos++;
            }

            MessengerHelper.setGroupIndex(pos);

             ((Activity) getContext()).runOnUiThread(() -> {
               if(headerListener!=null)
                   headerListener.onTitleChanged(getNameGroup());
             });


        }catch (Exception e){
            Utils.traces("setGroupView ChatControl ex: "+Utils.exceptionToString(e));
        }

        limitCount = limit;
        getTotalPagesCount();
        if(this.idGroup != idGroup){
            this.idGroup = idGroup;
            addRawList(dbHelper.getMessage(idGroup,apiKey, 0, limit),true);
        }
    }

    private void addRawList(List<MessageRaw> list, boolean notify){
        try{
            Utils.traces(TAG+" addRawList");
            lsChat.clear();
            for(MessageRaw raw : list) {
                lsChat.add(new ChatModel(raw.getId()
                        , gson.fromJson(AESUtils.decText(raw.getMessage()), MessageModel.class)
                        , raw.getFrom()
                        , DateUtil.getStringDate(raw.getTimeMark())
                        , raw.getMine() != 0));
            }
            if(notify) {
                mBinding.recyclerChat.setAdapter(new CustomAdapterBubble(lsChat, getContext()));
                Objects.requireNonNull(mBinding.recyclerChat.getLayoutManager()).scrollToPosition(Objects.requireNonNull(mBinding.recyclerChat.getAdapter()).getItemCount() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNextPage() {
        Utils.traces(TAG+ " loadNextPage: " + currentPage+ "; TOTAL_PAGES: "+TOTAL_PAGES);

        List<MessageRaw> list = dbHelper.getMessage(
                idGroup
                ,apiKey
                , lsChat.size()-1
                , limitCount);

        Collections.reverse(list);

        ((CustomAdapterBubble) Objects.requireNonNull(mBinding.recyclerChat.getAdapter())).removeLoadingHeader();
        isLoading = false;

        try{
            for(MessageRaw raw : list) {
                ((CustomAdapterBubble) Objects.requireNonNull(mBinding.recyclerChat.getAdapter()))
                        .add(new ChatModel(raw.getId()
                                , gson.fromJson(AESUtils.decText(raw.getMessage()), MessageModel.class)
                                , raw.getFrom()
                                , DateUtil.getStringDate(raw.getTimeMark())
                                , raw.getMine() != 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentPage != TOTAL_PAGES)
            ((CustomAdapterBubble) Objects.requireNonNull(mBinding.recyclerChat.getAdapter())).addLoadingHeader();
        else isLastPage = true;
    }

    public void deleteHistory() {
        dbHelper.deleteHistory();
    }


    /**
     * closes socket
     */
    public void onDestroy(){
        Utils.traces("ChatListView OnDestroy ");
        try{
            if (dbHelper!=null) dbHelper.close();
            if (socketSend != null){
                socketSend.checkQueue();
                socketSend.terminate();
            }
            clearInstances();
        }catch (Exception e){
            Utils.traces("onDestroy ChatListView ex: "+Utils.exceptionToString(e));
        }
    }

    public void onStop(){
        if(socketSend!=null) {
            Utils.traces("socketSend is not null onStop");
            socketSend.terminate();
            socketSend = null;
        }
        if(MessengerHelper.getChatListenerSocket()!=null){
            MessengerHelper.getChatListenerSocket().cancel();
        }
    }

    public void onResume(){
        if(socketSend!=null) {
            Utils.traces("socketSend is not null onResume");
            socketSend.terminate();
            socketSend = null;
        }
        startSendSocketConnection();
        Utils.traces("context in onResume is "+(getContext()==null?"null":"not null"));
        startListenerWebSocket();
    }

    public Lifecycle.Event getLifecycleEvent() {
        return lifecycleEvent;
    }

    public void setLifecycleEvent(Lifecycle.Event lifecycleEvent) {
        this.lifecycleEvent = lifecycleEvent;
    }

    private void startSendSocketConnection(){
        Utils.traces("startSendSocketConnection");
        if(policy==null) policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT_IN + "?iddevice=" + Siggy.getDeviceToken() + "&groupId=" + idGroup + "&API_KEY="+ apiKey;

        socketSend = Socket.Builder.with(url).build().connect();

        socketSend.setOnChangeStateListener(new Socket.OnStateChangeListener() {
            @Override
            public void onChange(Socket.State status) {
                socketState(status);
            }
        });
    }

    private void socketState(Socket.State status){
        switch (status){
            case CONNECT_ERROR:
                Utils.traces("SEND - CONNECT_ERROR SOCKET");
                startSendSocketConnection();
            case RECONNECTING:
                Utils.traces("SEND - RECONNECTING SOCKET");
            case CLOSED:
                Utils.traces("SEND - CLOSED SOCKET");
            case CLOSING:
                Utils.traces("SEND - CLOSING SOCKET");
            case OPEN:
                Utils.traces("SEND - OPEN SOCKET");
                if(socketSend!=null)
                    socketSend.checkQueue();

        }
    }

    /**
     * Manages messenger web socket connection
     */
    public void startListenerWebSocket(){
        try {

            if(MessengerHelper.getChatListenerSocket()!=null){
                MessengerHelper.getChatListenerSocket().cancel();
            }

            getQueue();

            messengerClient = null;
            messengerClient = new OkHttpClient();

            String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?iddevice=" + this.deviceToken + "&groupId=9999" + "&API_KEY=" + this.apiKey;

            requestSocketListener = null;
            requestSocketListener = new Request.Builder().url(url).build();

            webSocketListenerMessenger = null;
            webSocketListenerMessenger = new WebSocketListener() {
                @Override
                public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                    Utils.traces("messengerWebSocketConnection onOpen");
                    setHeaderSubtitle(getContext().getString(R.string.connected));
                }

                @Override
                public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                    try {
                        Utils.traces("messengerWebSocketConnection onMessage: "+text);
                        eventMessageModel = null;
                        if(gson==null) gson = Utils.getGson();
                        eventMessageModel = gson.fromJson(text, EventMessageModel.class);
                        long id = insertMessage(eventMessageModel);

                        if(id==-1){
                            Utils.traces("messengerWebSocketConnection onMessage error on bd insert");
                        }

                        Intent intent = new Intent(getContext(), MessengerReceiver.class);
                        intent.putExtra(MESSAGE_CHAT, text);
                        intent.putExtra(MESSAGE_CHAT_ID, id);
                        getContext().sendBroadcast(intent);
                    } catch (Exception ex) {
                        Utils.traces("messengerWebSocketConnection onMessage: "+ Utils.exceptionToString(ex));
                    }
                }

                @Override
                public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                    try {
                        Log.e(TAG, "MESSAGE bytes: " + bytes.hex());
                    } catch (Exception ex) {
                        System.out.print(ex.getMessage());
                    }
                }

                @Override
                public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    webSocket.close(1000, null);
                    webSocket.cancel();
                    Utils.traces("messengerWebSocketConnection onClosing code:"+code+" reason: "+reason);
                    setHeaderSubtitle(getContext().getString(R.string.closing));
                }

                @Override
                public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    Utils.traces("messengerWebSocketConnection onClosed code:"+code+" reason: "+reason);
                    setHeaderSubtitle(getContext().getString(R.string.closed));
                }

                @Override
                public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                    Utils.traces("messengerWebSocketConnection onFailure: "+Utils.exceptionToString((Exception) t));
                    setHeaderSubtitle(t.getMessage());
                }
            };

            MessengerHelper.setChatListenerSocket(messengerClient.newWebSocket(requestSocketListener, webSocketListenerMessenger));
            Utils.traces("messengerWebSocketConnection: set websocket");
            //messengerClient.dispatcher().executorService().shutdown();

        }catch (Exception e){
            Utils.traces("messengerWebSocketConnection catch: "+Utils.exceptionToString(e));
        }
    }

    private void setHeaderSubtitle(String text) {
        try {
           ((Activity) getContext()).runOnUiThread(() -> {
                if (headerListener != null)
                    headerListener.onSubtitleChanged(text);
            });
        }catch (Exception e ){
            Utils.traces("ChatListView subtitle "+Utils.exceptionToString(e));
        }
    }

    private void getQueue(){
        try {
            if (dbHelper == null){
                dbHelper = new DbHelper(getContext());
            }
            long timeMark = dbHelper.getTimeMark(9999,apiKey);
            Utils.traces("Chat TIME MARK: "+timeMark);
            if(timeMark>0) {
                QueueRequestModel model = new QueueRequestModel(deviceToken, apiKey, "9999", String.valueOf(timeMark));
                new ApiAsyncTask(apiListener).execute(ApiEnum.GET_CHAT_QUEUE, model);
            }
        }catch (Exception e){
            Utils.traces("Chat Control getQueue catch: "+Utils.exceptionToString(e));
        }
    }

    private long insertMessage( EventMessageModel eventMessageModel){
        messageRaw = null;
        messageRaw = new MessageRaw();

        messageRaw.setIdGroup(eventMessageModel.getData().getIdGroupFrom());
        messageRaw.setFrom(eventMessageModel.getData().getFrom());
        messageRaw.setMessage(eventMessageModel.getData().getText());
        messageRaw.setTimeMark(eventMessageModel.getTimeMark());
        messageRaw.setMine(0);
        messageRaw.setUserKey(apiKey);
        messageRaw.setSend(1);

        Utils.traces("insertMessage context = "+(getContext()==null?"null":"not null"));

        if(dbHelper==null){
            Utils.traces("dbHelper is null");
            dbHelper = new DbHelper(getContext());
        }

        return dbHelper.insertMessage(messageRaw);
    }

    private void clearInstances() {
        if(lsChat!=null) {
            lsChat.clear();
            lsChat = null;
        }
        eventMessageModel = null;
        model = null;
        socketSend = null;
        gson = null;
        dbHelper = null;
        apiKey = null;
        inflater = null;
        messageRaw = null;
        requestSocketListener = null;
        webSocketListenerMessenger = null;
        messengerClient = null;
    }

    public void setHeaderListener(HeaderListener listener) {
        this.headerListener = listener;
    }

    private String getNameGroup(){
        String name = null;
        for(GroupModel g : MessengerHelper.getGroupList()){
            if(g.idGroup == this.idGroup){
                name = g.name;
                break;
            }
        }
        return name;
    }
}
