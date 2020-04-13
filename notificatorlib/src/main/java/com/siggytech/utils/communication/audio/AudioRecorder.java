package com.siggytech.utils.communication.audio;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 *  For capturing audio, Android provides us MediaRecorder class. We
     can set audio source from which we want to capture sound, we can
     also set encoding to be used. We set path to sd card as a parameter
     to setOutputFile() method and call prepare() method to do the initial
     preparations for recording, this method must be called before calling
     start() which actually starts recording the audio from specified
     source and saves it to a file as specified by the path.

     Now , when you want to stop recording, simply call stop() method
     of MediaRecorder.
     That s it, we are done with our recording,  we can play the recorded
     audio by going to the path and file name specified by our path
     variable.
 */

public class AudioRecorder {
    final MediaRecorder recorder = new MediaRecorder();
    final String path;
    private int duration;

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioRecorder(String path) {
        //this.path = sanitizePath(path);
        this.path = path;
    }

    private String sanitizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.contains(".")) {
            path += ".3gp";
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
    }

    /**
     * Starts a new recording.
     */
    public void start() throws IOException {
        String state = Environment.getExternalStorageState();

        if(!state.equals(Environment.MEDIA_MOUNTED))  {
            throw new IOException("SD Card is not mounted. It is " + state + ".");
        }

        // make sure the directory we plan to store the recording in exists

        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created.");

        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.prepare();
        recorder.start();
        recorder.setOnErrorListener(l);
    }

    /**
     * Stops a recording that has been previously started.
     */
    public void stop() throws IOException {
        recorder.stop();
        //recorder.release();
    }

    public MediaRecorder.OnErrorListener l = new MediaRecorder.OnErrorListener() {

        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e("error in recording",""+what+" ,reason "+extra );
        }
    };

    public void recordAudio() {
        try {
            MediaRecorder newRecorder = new MediaRecorder();
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            newRecorder.setOutputFile("/sdcard/testRecord.3gp");
            newRecorder.prepare();
            newRecorder.start();
        }catch(Exception e) {
            Log.e("exception recordAudio",""+e);
        }

    }

    public int getMaxAmplitude()    {
        return recorder.getMaxAmplitude();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
