package com.siggytech.utils.communication;

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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.siggytech.view.MyImage;

import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class CustomAdapterBubble extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = CustomAdapterBubble.class.getSimpleName();
    private List<ChatModel> lstChat;
    private Context context;
    private MediaPlayer mPlayer;
    private Activity mActivity;

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false;

    public CustomAdapterBubble(List<ChatModel> lstChat, Context context, Activity activity) {
        this.lstChat = lstChat;
        this.context = context;
        this.mActivity = activity;
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
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingViewHolder(v2);
                break;
        }

        assert viewHolder != null;
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.chat_cell_out, parent, false);
        viewHolder = new MessageViewHolder(v1);
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
                        messageViewHolder.lnMessage.setBackgroundResource(R.drawable.bubble_nine_pach);
                    }else messageViewHolder.lnMessage.setBackgroundResource(R.drawable.mine_bubble);

                    messageViewHolder.lnLayout.setGravity(Gravity.END);
                }else{
                    if (Build.VERSION.SDK_INT > 23){
                        messageViewHolder.lnMessage.setBackgroundResource(R.drawable.bubble_nine_pach_agent);
                    }else messageViewHolder.lnMessage.setBackgroundResource(R.drawable.other_bubble);

                    messageViewHolder.lnLayout.setGravity(Gravity.START);
                }

                messageViewHolder.chat_out_from.setText(model.getFromMessage());
                messageViewHolder.chat_text_datetime.setText(model.getDateTimeMessage());

                if(Utils.MESSAGE_TYPE.MESSAGE.equals(model.getMessageModel().getType())) {
                    messageViewHolder.lnAudio.setVisibility(View.GONE);
                    messageViewHolder.ivPreviewImage.setVisibility(View.GONE);
                    messageViewHolder.chat_out_text.setVisibility(View.VISIBLE);
                    messageViewHolder.chat_out_text.setText(model.getMessageModel().getMessage());

                }
                else if(Utils.MESSAGE_TYPE.AUDIO.equals(model.getMessageModel().getType())) {
                    messageViewHolder.lnAudio.setVisibility(View.VISIBLE);
                    messageViewHolder.ivPreviewImage.setVisibility(View.GONE);
                    messageViewHolder.chat_out_text.setVisibility(View.GONE);
                    messageViewHolder.tvAudioDuration.setText(getDurationString(model.getMessageModel().getDuration()));
                    messageViewHolder.tvAudioDuration.setTag(getDurationString(model.getMessageModel().getDuration()));
                    try {
                        if(model.isMine())
                            messageViewHolder.audioUri = Utils.base64ToUri(model.getMessageModel().getMessage(),Utils.getDateName()+".3gp");
                        else messageViewHolder.audioUri = Uri.parse(getDownloadUrl(model.getMessageModel().getMessage()));
                    } catch(Exception e) { e.printStackTrace(); }

                }
                else if(Utils.MESSAGE_TYPE.VIDEO.equals(model.getMessageModel().getType())) {
                    messageViewHolder.chat_out_text.setVisibility(View.GONE);
                    messageViewHolder.lnAudio.setVisibility(View.GONE);
                    messageViewHolder.ivPreviewImage.setVisibility(View.VISIBLE);
                    try{
                        if(model.isMine()){
                            try {
                                messageViewHolder.uri = Utils.base64ToUri(model.getMessageModel().getMessage(),Utils.getDateName()+".mp4");
                                Bitmap decodedByte = ThumbnailUtils.createVideoThumbnail(messageViewHolder.uri.toString(),MINI_KIND);
                                messageViewHolder.ivPreviewImage.setRoundImage(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                                        decodedByte.getHeight(), false));
                                messageViewHolder.ivPreviewImage.getProgressBar().hide();
                            } catch(Exception e) { e.printStackTrace(); }

                            messageViewHolder.ivPreviewImage.setOnClickListener(v -> goVideoView(messageViewHolder.uri));
                        }else{
                            String videoUrl = getDownloadUrl(model.getMessageModel().getMessage());

                            long thumb = 5000 * 1000;
                            RequestOptions options = new RequestOptions().frame(thumb);
                            Glide.with(context).load(videoUrl).apply(options).into(messageViewHolder.ivPreviewImage.getImageView());

                            messageViewHolder.ivPreviewImage.getProgressBar().hide();
                            messageViewHolder.ivPreviewImage.setOnClickListener(v -> {
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
                    messageViewHolder.lnAudio.setVisibility(View.GONE);
                    messageViewHolder.ivPreviewImage.setVisibility(View.VISIBLE);
                    messageViewHolder.chat_out_text.setVisibility(View.GONE);

                    try{
                        if(model.isMine()){
                            byte[] decodedString = Base64.decode(model.getMessageModel().getMessage(), Base64.DEFAULT);
                            final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            messageViewHolder.ivPreviewImage.setRoundImage(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                                    decodedByte.getHeight(), false));
                            messageViewHolder.ivPreviewImage.getProgressBar().hide();
                            try {
                                messageViewHolder.uri = Utils.base64ToUri(model.getMessageModel().getMessage(),Utils.getDateName()+".bmp");
                            } catch(Exception e) { e.printStackTrace(); }

                            messageViewHolder.ivPreviewImage.setOnClickListener(v -> goImageView(messageViewHolder.uri));
                        }else{
                            String url = getDownloadUrl(model.getMessageModel().getMessage());

                            long thumb = 5000 * 1000;
                            RequestOptions options = new RequestOptions().frame(thumb);
                            Glide.with(context).load(url).apply(options).into(messageViewHolder.ivPreviewImage.getImageView());

                            messageViewHolder.ivPreviewImage.getProgressBar().hide();
                            messageViewHolder.ivPreviewImage.setOnClickListener(v -> {
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

                messageViewHolder.chat_out_from.setTextColor(Conf.CHAT_COLOR_FROM);
                messageViewHolder.chat_out_text.setTextColor(Conf.CHAT_COLOR_TEXT);
                messageViewHolder.chat_text_datetime.setTextColor(Conf.CHAT_COLOR_DATE);
                messageViewHolder.tvAudioDuration.setTextColor(Conf.CHAT_COLOR_DATE);
                //messageViewHolder.image.setImageResource(R.drawable.ic_launcher_round);

                messageViewHolder.ivPlay.setTag(false);
                messageViewHolder.ivPlay.setOnClickListener(v -> {
                    try {
                        if (!((boolean) v.getTag())) {
                            // If media player another instance already running then stop it first
                            stopPlaying(messageViewHolder.sbPlay,messageViewHolder.ivPlay);

                            v.setTag(true);
                            ((ImageView) v).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));

                            // Initialize media player
                            mPlayer = MediaPlayer.create(context, messageViewHolder.audioUri);

                            // Start the media player
                            mPlayer.start();

                            // Initialize the seek bar
                            initializeSeekBar(messageViewHolder.sbPlay
                                    ,messageViewHolder.tvAudioDuration
                                    ,messageViewHolder.ivPlay
                                    ,messageViewHolder.factor);
                        } else {
                            stopPlaying(messageViewHolder.sbPlay
                                    ,messageViewHolder.ivPlay);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });

                messageViewHolder.sbPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                        try {
                            if(fromTouch) mPlayer.seekTo(progress * messageViewHolder.factor);
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
            mActivity.runOnUiThread(() -> {
                mImage.setTag(false);
                mImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow));
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

    private void initializeSeekBar(final SeekBar mSeekBar, final TextView tvDuration, final ImageView mImage, final int factor){
        Log.d(TAG,"TOTAL "+mPlayer.getDuration()/factor);
        mSeekBar.setMax(mPlayer.getDuration()/factor);

        new Thread(() -> {
            try {
                while (mPlayer != null && mPlayer.isPlaying() && (mPlayer.getCurrentPosition() / factor) < (mPlayer.getDuration() / factor)) {
                    int mCurrentPosition = mPlayer.getCurrentPosition() / factor;
                    mSeekBar.setProgress(mCurrentPosition);
                    mActivity.runOnUiThread(() -> {
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
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void goImageView(Uri uri) {
        try {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("ImageUri",uri.toString());
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
        LinearLayout lnLayout;
        LinearLayout lnMessage;
        LinearLayout lnDate;
        TextView chat_out_from;
        TextView chat_out_text;
        TextView chat_text_datetime;
        TextView tvDate;
        LinearLayout lnAudio;
        TextView tvAudioDuration;
        ImageView ivPlay;
        SeekBar sbPlay;
        Uri audioUri;
        Uri uri;
        int factor = 1; //This is in case it is necessary to divide the duration factor
        MyImage ivPreviewImage;


        public MessageViewHolder(View itemView) {
            super(itemView);

            lnLayout = itemView.findViewById(R.id.lnLayout);
            lnDate = itemView.findViewById(R.id.lnDate);
            lnMessage = itemView.findViewById(R.id.lnMessage);
            chat_out_from = itemView.findViewById(R.id.chat_out_name);
            chat_out_text = itemView.findViewById(R.id.chat_out_text);
            chat_text_datetime = itemView.findViewById(R.id.chat_text_datetime);
            tvDate = itemView.findViewById(R.id.tvDate);
            lnAudio = itemView.findViewById(R.id.lnAudio);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            sbPlay = itemView.findViewById(R.id.sbPlay);
            ivPreviewImage = itemView.findViewById(R.id.ivPreviewImage);
            //image = itemView.findViewById(R.id.cell_icon);
        }
    }
}
