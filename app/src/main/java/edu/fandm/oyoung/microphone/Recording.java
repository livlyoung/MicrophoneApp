package edu.fandm.oyoung.microphone;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;



//Source: https://www.youtube.com/watch?v=z3Gx4whgWcY with my own edits.
public class Recording {

    private String name;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private boolean isPlaying = false;
    private Date timestamp;
    private File file;

    public Recording(String name) {
        this.name = name;
        this.timestamp = new Date();
    }

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void startRecording() throws IOException {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(getFilePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.prepare();
        recorder.start();
    }

    public void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            writeToFile();
            recorder = null;
        }
    }

    public void play() throws IOException {
        player = new MediaPlayer();
        player.setDataSource(this.getFilePath());
        player.setOnCompletionListener(mp -> {
            isPlaying = false;
            player.release();
            player = null;
        });
        player.prepare();
        player.start();
        isPlaying = true;
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            isPlaying = false;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    protected String getFilePath() {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + name + ".3gp";
        }
    }

    private void writeToFile() {
        try {
            FileInputStream inputStream = new FileInputStream(getFilePath());
            FileOutputStream outputStream = new FileOutputStream(getFilePath() + ".txt");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
