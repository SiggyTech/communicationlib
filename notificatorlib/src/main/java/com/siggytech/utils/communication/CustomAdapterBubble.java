package com.siggytech.utils.communication;

import android.content.Context;
import android.media.MediaPlayer;
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

    private List<ChatModel> lstChat;
    private Context context;
    private LayoutInflater inflater;
    private MediaPlayer mediaPlayer = new MediaPlayer();

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

            holder.chat_out_from = vi.findViewById(R.id.chat_out_name);
            holder.chat_out_text = vi.findViewById(R.id.chat_out_text);
            holder.chat_text_datetime = vi.findViewById(R.id.chat_text_datetime);
            holder.lnAudio = vi.findViewById(R.id.lnAudio);
            holder.ivPlay = vi.findViewById(R.id.ivPlay);
            holder.sbPlay = vi.findViewById(R.id.sbPlay);
            //holder.image = vi.findViewById(R.id.cell_icon);

            vi.setTag(holder);

        } else holder = (ViewHolder)vi.getTag();

        holder.chat_out_from.setText(lstChat.get(position).getFromMessage());
        holder.chat_text_datetime.setText(lstChat.get(position).getDateTimeMessage());
        holder.chat_out_text.setText(lstChat.get(position).getTextMessage());
        if(Utils.MESSAGE_TYPE.MESSAGE.equals(lstChat.get(position).getMessageType())) {
            holder.lnAudio.setVisibility(View.GONE);
            holder.chat_out_text.setVisibility(View.VISIBLE);
        }
        if(Utils.MESSAGE_TYPE.AUDIO.equals(lstChat.get(position).getMessageType())) {
            holder.lnAudio.setVisibility(View.VISIBLE);
            holder.chat_out_text.setVisibility(View.GONE);

            try {
                mediaPlayer = MediaPlayer.create(context, Utils.Base64ToUrl(lstChat.get(position).getTextMessage(),Utils.GetDateName()+".3gp"));
                mediaPlayer.prepare();
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setLooping(false);
                holder.sbPlay.setMax(mediaPlayer.getDuration());
            } catch(Exception e) { e.printStackTrace(); }

        }else if(Utils.MESSAGE_TYPE.VIDEO.equals(lstChat.get(position).getMessageType())) {
            //TODO do staff
        } else if(Utils.MESSAGE_TYPE.PHOTO.equals(lstChat.get(position).getMessageType())) {
            //TODO do staff
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
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                } catch(Exception e) { e.printStackTrace(); }

                if(!((boolean)v.getTag())){
                    v.setTag(true);
                    ((ImageView)v).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));
                    try {
                        mediaPlayer.start();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                int total = mediaPlayer.getDuration();

                                while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
                                    try {
                                        Thread.sleep(1000);
                                        currentPosition = mediaPlayer.getCurrentPosition();
                                    } catch (InterruptedException e) {
                                        return;
                                    } catch (Exception e) {
                                        return;
                                    }

                                    holder.sbPlay.setProgress(currentPosition);
                                }
                            }
                        }).start();
                    } catch(Exception e) { e.printStackTrace(); }
                }else {
                    v.setTag(false);
                    ((ImageView) v).setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow));
                }
            }
        });

        holder.sbPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                
                int x = (int) Math.ceil(progress / 1000f);

                double percent = progress / (double) seekBar.getMax();
                int offset = seekBar.getThumbOffset();
                int seekWidth = seekBar.getWidth();
                int val = (int) Math.round(percent * (seekWidth - 2 * offset));
              
               
                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    seekBar.setProgress(0);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
        
        return vi;
    }



    class ViewHolder {
        TextView chat_out_from;
        TextView chat_out_text;
        TextView chat_text_datetime;
        LinearLayout lnAudio;
        ImageView ivPlay;
        SeekBar sbPlay;
    }

}
