package com.siggytech.utils.communication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;


public class CustomAdapterBubble extends BaseAdapter {
    private static final String TAG = CustomAdapterBubble.class.getSimpleName();
    private List<ChatModel> lstChat;
    private Context context;
    private LayoutInflater inflater;
    private MediaPlayer mPlayer;


    public CustomAdapterBubble(List<ChatModel> lstChat, Context context) {
        this.lstChat = lstChat;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return lstChat.size();
    }

    @Override
    public Object getItem(int position) {
        return lstChat.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        final ViewHolder holder;

        if (convertView == null) {

            vi = inflater.inflate(R.layout.chat_cell_out, null);

            holder = new ViewHolder();

            holder.lnLayout = vi.findViewById(R.id.lnLayout);
            holder.lnMessage = vi.findViewById(R.id.lnMessage);
            holder.chat_out_from = vi.findViewById(R.id.chat_out_name);
            holder.chat_out_text = vi.findViewById(R.id.chat_out_text);
            holder.chat_text_datetime = vi.findViewById(R.id.chat_text_datetime);
            holder.lnAudio = vi.findViewById(R.id.lnAudio);
            holder.ivPlay = vi.findViewById(R.id.ivPlay);
            holder.sbPlay = vi.findViewById(R.id.sbPlay);
            holder.ivPreviewImage = vi.findViewById(R.id.ivPreviewImage);
            //holder.image = vi.findViewById(R.id.cell_icon);

            vi.setTag(holder);

        } else holder = (ViewHolder)vi.getTag();

        if(lstChat.get(position).isMine()){
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

        holder.chat_out_from.setText(lstChat.get(position).getFromMessage());
        holder.chat_text_datetime.setText(lstChat.get(position).getDateTimeMessage());

        if(Utils.MESSAGE_TYPE.MESSAGE.equals(lstChat.get(position).getMessageModel().getType())) {
            holder.lnAudio.setVisibility(View.GONE);
            holder.ivPreviewImage.setVisibility(View.GONE);
            holder.chat_out_text.setVisibility(View.VISIBLE);
            holder.chat_out_text.setText(lstChat.get(position).getMessageModel().getMessage());

        }
        if(Utils.MESSAGE_TYPE.AUDIO.equals(lstChat.get(position).getMessageModel().getType())) {
            holder.lnAudio.setVisibility(View.VISIBLE);
            holder.ivPreviewImage.setVisibility(View.GONE);
            holder.chat_out_text.setVisibility(View.GONE);
            try {
                holder.audioUri = Utils.Base64ToUrl(lstChat.get(position).getMessageModel().getMessage(),Utils.GetDateName()+".3gp");
            } catch(Exception e) { e.printStackTrace(); }

        }else if(Utils.MESSAGE_TYPE.VIDEO.equals(lstChat.get(position).getMessageModel().getType())) {
            //TODO do staff
        } else if(Utils.MESSAGE_TYPE.PHOTO.equals(lstChat.get(position).getMessageModel().getType())) {
            holder.lnAudio.setVisibility(View.GONE);
            holder.ivPreviewImage.setVisibility(View.VISIBLE);
            holder.chat_out_text.setVisibility(View.GONE);

            try{
                byte[] decodedString = Base64.decode(lstChat.get(position).getMessageModel().getMessage(), Base64.DEFAULT);
                final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                holder.ivPreviewImage.setImageBitmap(Bitmap.createScaledBitmap(decodedByte, decodedByte.getWidth(),
                        decodedByte.getHeight(), false));

                try {
                    holder.uri = Utils.Base64ToUrl(lstChat.get(position).getMessageModel().getMessage(),Utils.GetDateName()+".bmp");
                } catch(Exception e) { e.printStackTrace(); }

                holder.ivPreviewImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        transition(holder.uri);
                    }
                });
            } catch (Exception e){e.printStackTrace();}
        }
        holder.chat_out_from.setTextColor(Conf.CHAT_COLOR_FROM);
        holder.chat_out_text.setTextColor(Conf.CHAT_COLOR_TEXT);
        holder.chat_text_datetime.setTextColor(Conf.CHAT_COLOR_DATE);
        //holder.image.setImageResource(R.drawable.ic_launcher_round);

        holder.ivPlay.setTag(false);
        holder.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        initializeSeekBar(holder.sbPlay,holder.ivPlay,holder.factor);
                    } else {
                        stopPlaying(holder.sbPlay,holder.ivPlay);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
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
        
        return vi;
    }

    class ViewHolder {
        LinearLayout lnLayout;
        LinearLayout lnMessage;
        TextView chat_out_from;
        TextView chat_out_text;
        TextView chat_text_datetime;
        LinearLayout lnAudio;
        ImageView ivPlay;
        SeekBar sbPlay;
        Uri audioUri;
        Uri uri;
        int factor = 1; //Esto es por si es necesario dividir el factor de duracion
        ImageView ivPreviewImage;
    }

    private void stopPlaying(final SeekBar mSeekBar, final ImageView mImage){
        mImage.setTag(false);
        mImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow));
        mSeekBar.setProgress(0);

        try {
            // If media player is not null then try to stop it
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void initializeSeekBar(final SeekBar mSeekBar, final ImageView mImage, final int factor){

        Log.d(TAG,"TOTAL "+mPlayer.getDuration()/factor);
        mSeekBar.setMax(mPlayer.getDuration()/factor);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mPlayer != null && mPlayer.isPlaying() && (mPlayer.getCurrentPosition() / factor) < (mPlayer.getDuration() / factor)) {
                        int mCurrentPosition = mPlayer.getCurrentPosition() / factor;
                        Log.d(TAG,"Posicion actual "+mPlayer.getCurrentPosition());
                        mSeekBar.setProgress(mCurrentPosition);
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
            }
        }).start();
    }


    private void transition(Uri uri) {
        try {
            Intent intent = new Intent(context, TransitionToActivity.class);
            intent.putExtra("ImageUri",uri.toString());

            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
