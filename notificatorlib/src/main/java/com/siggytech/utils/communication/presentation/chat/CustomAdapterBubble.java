package com.siggytech.utils.communication.presentation.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.siggytech.utils.communication.R;
import com.siggytech.utils.communication.databinding.ChatCellOutBinding;
import com.siggytech.utils.communication.databinding.ItemProgressBinding;
import com.siggytech.utils.communication.model.ChatModel;
import com.siggytech.utils.communication.presentation.ImageActivity;
import com.siggytech.utils.communication.presentation.VideoActivity;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.Utils;

import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.siggytech.utils.communication.util.UriUtil.base64ToUri;

public class CustomAdapterBubble extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatModel> lstChat;

    private MediaPlayer mPlayer;
    private Context context;

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false;

    public CustomAdapterBubble(List<ChatModel> lstChat, Context context) {
        this.lstChat = lstChat;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                ItemProgressBinding progressBinding = DataBindingUtil.inflate(inflater,R.layout.item_progress, parent, false);
                viewHolder = new LoadingViewHolder(progressBinding.getRoot());
                break;
        }

        assert viewHolder != null;
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        ChatCellOutBinding binding = DataBindingUtil.inflate(inflater,R.layout.chat_cell_out, parent, false);
        viewHolder = new MessageViewHolder(binding.getRoot(),binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ChatModel model = lstChat.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                MessageViewHolder messageViewHolder = (MessageViewHolder) holder;

                if(model.isMine()){
                    if (Build.VERSION.SDK_INT > 23){
                        messageViewHolder.chatCellOutBinding.lnMessage.setBackgroundResource(R.drawable.bubble_nine_pach);
                    }else messageViewHolder.chatCellOutBinding.lnMessage.setBackgroundResource(R.drawable.mine_bubble);

                    messageViewHolder.chatCellOutBinding.lnLayout.setGravity(Gravity.END);
                }else{
                    if (Build.VERSION.SDK_INT > 23){
                        messageViewHolder.chatCellOutBinding.lnMessage.setBackgroundResource(R.drawable.bubble_nine_pach_agent);
                    }else messageViewHolder.chatCellOutBinding.lnMessage.setBackgroundResource(R.drawable.other_bubble);

                    messageViewHolder.chatCellOutBinding.lnLayout.setGravity(Gravity.START);
                }

                messageViewHolder.chatCellOutBinding.chatOutName.setText(model.getFromMessage());
                messageViewHolder.chatCellOutBinding.chatTextDatetime.setText(model.getDateTimeMessage());

                if(Utils.MESSAGE_TYPE.MESSAGE.equals(model.getMessageModel().getType())) {
                    setAudioVisibility(messageViewHolder,View.GONE);
                    messageViewHolder.chatCellOutBinding.ivPreviewImage.setVisibility(View.GONE);
                    messageViewHolder.chatCellOutBinding.chatOutText.setVisibility(View.VISIBLE);
                    messageViewHolder.chatCellOutBinding.chatOutText.setText(model.getMessageModel().getMessage());

                }
                else if(Utils.MESSAGE_TYPE.AUDIO.equals(model.getMessageModel().getType())) {
                    setAudioVisibility(messageViewHolder,View.VISIBLE);
                    messageViewHolder.chatCellOutBinding.ivPreviewImage.setVisibility(View.GONE);
                    messageViewHolder.chatCellOutBinding.chatOutText.setVisibility(View.GONE);
                    messageViewHolder.chatCellOutBinding.tvAudioDuration.setText(getDurationString(model.getMessageModel().getDuration()));
                    messageViewHolder.chatCellOutBinding.tvAudioDuration.setTag(getDurationString(model.getMessageModel().getDuration()));
                    try {
                        if(model.isMine()) {
                            messageViewHolder.audioUri = base64ToUri(model.getMessageModel().getMessage(), model.getIdMessage()+".3gp");
                        }else messageViewHolder.audioUri = Uri.parse(getDownloadUrl(model.getMessageModel().getMessage()));
                    } catch(Exception e) { e.printStackTrace(); }

                }
                else if(Utils.MESSAGE_TYPE.VIDEO.equals(model.getMessageModel().getType())) {
                    messageViewHolder.chatCellOutBinding.chatOutText.setVisibility(View.GONE);
                    setAudioVisibility(messageViewHolder,View.GONE);
                    messageViewHolder.chatCellOutBinding.ivPreviewImage.setVisibility(View.VISIBLE);
                    try{
                        if(model.isMine()){
                            try {
                                messageViewHolder.uri = base64ToUri(model.getMessageModel().getMessage(),model.getIdMessage()+".mp4");
                                Bitmap decodedByte = ThumbnailUtils.createVideoThumbnail(messageViewHolder.uri.toString(),MINI_KIND);
                                messageViewHolder.chatCellOutBinding.ivPreviewImage.setRoundImage(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                                        decodedByte.getHeight(), false));
                                messageViewHolder.chatCellOutBinding.ivPreviewImage.getProgressBar().hide();
                            } catch(Exception e) { e.printStackTrace(); }

                            messageViewHolder.chatCellOutBinding.ivPreviewImage.setOnClickListener(v -> goVideoView(messageViewHolder.uri));
                        }else{
                            String videoUrl = getDownloadUrl(model.getMessageModel().getMessage());

                            long thumb = 5000 * 1000;
                            RequestOptions options = new RequestOptions().frame(thumb);
                            Glide.with(context).load(videoUrl).apply(options).into(messageViewHolder.chatCellOutBinding.ivPreviewImage.getImageView());

                            messageViewHolder.chatCellOutBinding.ivPreviewImage.getProgressBar().hide();
                            messageViewHolder.chatCellOutBinding.ivPreviewImage.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(context, VideoActivity.class);
                                    intent.putExtra("VideoUri",videoUrl);
                                    context.startActivity(intent);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (Exception e){e.printStackTrace();}

                }
                else if(Utils.MESSAGE_TYPE.PHOTO.equals(model.getMessageModel().getType())) {
                    setAudioVisibility(messageViewHolder,View.GONE);
                    messageViewHolder.chatCellOutBinding.ivPreviewImage.setVisibility(View.VISIBLE);
                    messageViewHolder.chatCellOutBinding.chatOutText.setVisibility(View.GONE);

                    try{
                        if(model.isMine()){
                            byte[] decodedString = Base64.decode(model.getMessageModel().getMessage(), Base64.DEFAULT);
                            final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            messageViewHolder.chatCellOutBinding.ivPreviewImage.setRoundImage(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                                    decodedByte.getHeight(), false));
                            messageViewHolder.chatCellOutBinding.ivPreviewImage.getProgressBar().hide();
                            try {
                                messageViewHolder.uri = base64ToUri(model.getMessageModel().getMessage(),model.getIdMessage()+".bmp");
                            } catch(Exception e) { e.printStackTrace(); }

                            messageViewHolder.chatCellOutBinding.ivPreviewImage.setOnClickListener(v -> goImageView(messageViewHolder.uri));
                        }else{
                            String url = getDownloadUrl(model.getMessageModel().getMessage());

                            long thumb = 5000 * 1000;
                            RequestOptions options = new RequestOptions().frame(thumb);
                            Glide.with(context).load(url).apply(options).into(messageViewHolder.chatCellOutBinding.ivPreviewImage.getImageView());

                            messageViewHolder.chatCellOutBinding.ivPreviewImage.getProgressBar().hide();
                            messageViewHolder.chatCellOutBinding.ivPreviewImage.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(context, ImageActivity.class);
                                    intent.putExtra("ImageUri",url);
                                    context.startActivity(intent);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (Exception e){e.printStackTrace();}
                }

                messageViewHolder.chatCellOutBinding.chatOutName.setTextColor(Conf.CHAT_COLOR_FROM);
                messageViewHolder.chatCellOutBinding.chatOutText.setTextColor(Conf.CHAT_COLOR_TEXT);
                messageViewHolder.chatCellOutBinding.chatTextDatetime.setTextColor(Conf.CHAT_COLOR_DATE);
                messageViewHolder.chatCellOutBinding.tvAudioDuration.setTextColor(Conf.CHAT_COLOR_DATE);
                //messageViewHolder.image.setImageResource(R.drawable.ic_launcher_round);

                messageViewHolder.chatCellOutBinding.ivPlay.setTag(false);
                messageViewHolder.chatCellOutBinding.ivPlay.setOnClickListener(v -> {
                    try {
                        if (!((boolean) v.getTag())) {
                            // If media player another instance already running then stop it first
                            stopPlaying(messageViewHolder.chatCellOutBinding.sbPlay,messageViewHolder.chatCellOutBinding.ivPlay);

                            v.setTag(true);
                            ((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_pause,null));

                            // Initialize media player
                            mPlayer = MediaPlayer.create(context, messageViewHolder.audioUri);

                            // Start the media player
                            mPlayer.start();

                            // Initialize the seek bar
                            initializeSeekBar(messageViewHolder.chatCellOutBinding.sbPlay
                                    ,messageViewHolder.chatCellOutBinding.tvAudioDuration
                                    ,messageViewHolder.chatCellOutBinding.ivPlay
                                    );
                        } else {
                            stopPlaying(messageViewHolder.chatCellOutBinding.sbPlay
                                    ,messageViewHolder.chatCellOutBinding.ivPlay);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });

                messageViewHolder.chatCellOutBinding.sbPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                        try {
                            if(fromTouch) mPlayer.seekTo(progress);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                });
                break;
            case LOADING:
//                Do nothing
                break;
        }
    }

    private void setAudioVisibility(MessageViewHolder messageViewHolder, int visibility) {
        messageViewHolder.chatCellOutBinding.ivPlay.setVisibility(visibility);
        messageViewHolder.chatCellOutBinding.sbPlay.setVisibility(visibility);
        messageViewHolder.chatCellOutBinding.tvAudioDuration.setVisibility(visibility);
    }

    @Override
    public int getItemCount() {
        return lstChat == null ? 0 : lstChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && isLoadingAdded) ? LOADING : ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

/*
   Helpers
   _________________________________________________________________________________________________
    */

    //for pagination add items only
    public void add(ChatModel chatModel) {
        lstChat.add(0,chatModel);
        notifyItemInserted(0);

    }

    public void addAll(List<ChatModel> mcList) {
        for (ChatModel mc : mcList) {
            add(mc);
        }
    }

    public void remove(ChatModel chatModel) {
        int position = lstChat.indexOf(chatModel);
        if (position > -1) {
            lstChat.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingHeader() {
        isLoadingAdded = true;
        lstChat.add(0,new ChatModel());
        notifyItemInserted(0);
    }

    public void removeLoadingHeader() {
        isLoadingAdded = false;

        int position = 0;
        ChatModel item = getItem(position);

        if (item != null) {
            lstChat.remove(position);
            notifyItemRemoved(position);
        }
    }

    public ChatModel getItem(int position) {
        return lstChat.get(position);
    }

/*
    Functions
    ________________________________________________________________________________________________
 */

    private void stopPlaying(final SeekBar mSeekBar, final ImageView mImage){
        try {
            ((Activity)context).runOnUiThread(() -> {
                mImage.setTag(false);
                mImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_play_arrow,null));
                mSeekBar.setProgress(0);
            });
            try {
                // If media player is not null then try to stop it
                if (mPlayer != null) {
                    mPlayer.stop();
                    mPlayer.reset();
                    mPlayer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeSeekBar(final SeekBar mSeekBar, final TextView tvDuration, final ImageView mImage){

        mSeekBar.setProgress(mPlayer.getCurrentPosition());
        mSeekBar.setMax(mPlayer.getDuration());

        new Thread(() -> {
            try {
                while (mPlayer != null && mPlayer.isPlaying() && (mPlayer.getCurrentPosition() ) < (mPlayer.getDuration() )) {
                    int mCurrentPosition = mPlayer.getCurrentPosition();
                    mSeekBar.setProgress(mCurrentPosition);
                    mSeekBar.setMax(mPlayer.getDuration());
                    ((Activity)context).runOnUiThread(() -> {
                        if(mPlayer!=null) {
                            try {
                                if(mPlayer.isPlaying()) tvDuration.setText(getDurationString(mPlayer.getCurrentPosition() / 1000));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else tvDuration.setText((String)tvDuration.getTag());
                    });

                }

                if (mPlayer != null && mPlayer.getDuration() == mPlayer.getCurrentPosition()) {
                    stopPlaying(mSeekBar,mImage);
                }
                if(mPlayer != null &&  !mPlayer.isPlaying()){
                    stopPlaying(mSeekBar,mImage);
                }
            }catch (Exception e){
                e.printStackTrace();
                stopPlaying(mSeekBar,mImage);
            }
        }).start();
    }

    private String getDurationString(int seconds){
        int minutes = seconds / 60;
        seconds     = seconds % 60;
        return String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
    }

    private void goImageView(Uri uri) {
        try {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("ImageUri",uri.getPath());
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void goVideoView(Uri uri) {
        try {
            Intent intent = new Intent(context, VideoActivity.class);
            intent.putExtra("VideoUri",uri.toString());
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getDownloadUrl(String name){
        return Conf.HTTP+""+Conf.SERVER_IP+":"+Conf.SERVER_IMAGE_PORT+"/"+name;
    }


/*
   View Holders
   _________________________________________________________________________________________________
    */


    protected static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected static class MessageViewHolder extends RecyclerView.ViewHolder{

        Uri audioUri;
        Uri uri;

        ChatCellOutBinding chatCellOutBinding;

        public MessageViewHolder(View itemView,ChatCellOutBinding binding) {
            super(itemView);
            chatCellOutBinding = binding;
        }
    }


}
