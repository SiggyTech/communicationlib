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

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.RecyclerView;

import com.siggytech.view.MyImage;

import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;


public class CustomAdapterBubble extends RecyclerView.Adapter<CustomAdapterBubble.ViewHolder> {
    private static final String TAG = CustomAdapterBubble.class.getSimpleName();
    private List<ChatModel> lstChat;
    private Context context;
    private MediaPlayer mPlayer;
    private Activity mActivity;

    public CustomAdapterBubble(List<ChatModel> lstChat, Context context, Activity activity) {
        this.lstChat = lstChat;
        this.context = context;
        this.mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_cell_out,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ChatModel model = lstChat.get(position);

        if(model.isMine()){
            if (Build.VERSION.SDK_INT > 23){
                holder.lnMessage.setBackgroundResource(R.drawable.bubble_nine_pach);
            }else holder.lnMessage.setBackgroundResource(R.drawable.mine_bubble);

            holder.lnLayout.setGravity(Gravity.END);
        }else{
            if (Build.VERSION.SDK_INT > 23){
                holder.lnMessage.setBackgroundResource(R.drawable.bubble_nine_pach_agent);
            }else holder.lnMessage.setBackgroundResource(R.drawable.other_bubble);

            holder.lnLayout.setGravity(Gravity.START);
        }

        holder.chat_out_from.setText(model.getFromMessage());
        holder.chat_text_datetime.setText(model.getDateTimeMessage());

        if(Utils.MESSAGE_TYPE.MESSAGE.equals(model.getMessageModel().getType())) {
            holder.lnAudio.setVisibility(View.GONE);
            holder.ivPreviewImage.setVisibility(View.GONE);
            holder.chat_out_text.setVisibility(View.VISIBLE);
            holder.chat_out_text.setText(model.getMessageModel().getMessage());

        }
        if(Utils.MESSAGE_TYPE.AUDIO.equals(model.getMessageModel().getType())) {
            holder.lnAudio.setVisibility(View.VISIBLE);
            holder.ivPreviewImage.setVisibility(View.GONE);
            holder.chat_out_text.setVisibility(View.GONE);
            holder.tvAudioDuration.setText(getDurationString(model.getMessageModel().getDuration()));
            holder.tvAudioDuration.setTag(getDurationString(model.getMessageModel().getDuration()));
            try {
                holder.audioUri = Utils.Base64ToUrl(model.getMessageModel().getMessage(),Utils.GetDateName()+".3gp");
            } catch(Exception e) { e.printStackTrace(); }

        }else if(Utils.MESSAGE_TYPE.VIDEO.equals(model.getMessageModel().getType())) {
            holder.chat_out_text.setVisibility(View.GONE);
            holder.lnAudio.setVisibility(View.GONE);
            holder.ivPreviewImage.setVisibility(View.VISIBLE);
            try{
                if(model.isMine()){
                    try {
                        holder.uri = Utils.Base64ToUrl(model.getMessageModel().getMessage(),Utils.GetDateName()+".mp4");
                        Bitmap decodedByte = ThumbnailUtils.createVideoThumbnail(holder.uri.toString(),MINI_KIND);
                        holder.ivPreviewImage.setRoundImage(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                                decodedByte.getHeight(), false));
                        holder.ivPreviewImage.getProgressBar().hide();
                    } catch(Exception e) { e.printStackTrace(); }

                    holder.ivPreviewImage.setOnClickListener(v -> goVideoView(holder.uri));
                }else{
                    new DownloadTask(context,getDownloadUrl(model.getMessageModel().getMessage()),holder.ivPreviewImage,model.getMessageModel().getType());
                }
            } catch (Exception e){e.printStackTrace();}

        } else if(Utils.MESSAGE_TYPE.PHOTO.equals(model.getMessageModel().getType())) {
            holder.lnAudio.setVisibility(View.GONE);
            holder.ivPreviewImage.setVisibility(View.VISIBLE);
            holder.chat_out_text.setVisibility(View.GONE);

            try{
                if(model.isMine()){
                    byte[] decodedString = Base64.decode(model.getMessageModel().getMessage(), Base64.DEFAULT);
                    final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    holder.ivPreviewImage.setRoundImage(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                            decodedByte.getHeight(), false));
                    holder.ivPreviewImage.getProgressBar().hide();
                    try {
                        holder.uri = Utils.Base64ToUrl(model.getMessageModel().getMessage(),Utils.GetDateName()+".bmp");
                    } catch(Exception e) { e.printStackTrace(); }

                    holder.ivPreviewImage.setOnClickListener(v -> goImageView(holder.uri));
                }else{
                    new DownloadTask(context,getDownloadUrl(model.getMessageModel().getMessage()),holder.ivPreviewImage,model.getMessageModel().getType());
                }
            } catch (Exception e){e.printStackTrace();}
        }
        holder.chat_out_from.setTextColor(Conf.CHAT_COLOR_FROM);
        holder.chat_out_text.setTextColor(Conf.CHAT_COLOR_TEXT);
        holder.chat_text_datetime.setTextColor(Conf.CHAT_COLOR_DATE);
        holder.tvAudioDuration.setTextColor(Conf.CHAT_COLOR_DATE);
        //holder.image.setImageResource(R.drawable.ic_launcher_round);

        holder.ivPlay.setTag(false);
        holder.ivPlay.setOnClickListener(v -> {
            try {
                if (!((boolean) v.getTag())) {
                    // If media player another instance already running then stop it first
                    stopPlaying(holder.sbPlay,holder.ivPlay);

                    v.setTag(true);
                    ((ImageView) v).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));

                    // Initialize media player
                    mPlayer = MediaPlayer.create(context, holder.audioUri);

                    // Start the media player
                    mPlayer.start();

                    // Initialize the seek bar
                    initializeSeekBar(holder.sbPlay,holder.tvAudioDuration,holder.ivPlay,holder.factor);
                } else {
                    stopPlaying(holder.sbPlay,holder.ivPlay);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        holder.sbPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                try {
                    if(fromTouch) mPlayer.seekTo(progress * holder.factor);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return lstChat.size();
    }


    private void stopPlaying(final AppCompatSeekBar mSeekBar, final ImageView mImage){
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
                    mPlayer.release();
                    mPlayer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeSeekBar(final AppCompatSeekBar mSeekBar, final TextView tvDuration, final ImageView mImage, final int factor){
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



    class ViewHolder extends RecyclerView.ViewHolder{
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
        AppCompatSeekBar sbPlay;
        Uri audioUri;
        Uri uri;
        int factor = 1; //Esto es por si es necesario dividir el factor de duracion
        MyImage ivPreviewImage;


        public ViewHolder(View itemView) {
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
