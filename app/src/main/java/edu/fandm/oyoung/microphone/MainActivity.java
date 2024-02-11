package edu.fandm.oyoung.microphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private String audioSavePath = null;
    public static boolean canWriteToExternalSharedFolders;
    public static boolean canReadToExternalSharedFolders;
    public static boolean canRecordAudio;
    boolean isRecording = false;
    String fileName;
    Recording newRecording;
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;
    List<Recording> recordings = new ArrayList<>(); //to store recordings for recycler view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Source: Lecture slides and Prof. Novak
        // Request all necessary permissions at once
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int permissionStatus = PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionStatus = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionStatus |= checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            permissionStatus |= checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 0);
            }
        } else {
            canWriteToExternalSharedFolders = true;
            canRecordAudio = true;
            canReadToExternalSharedFolders = true;
        }


        //Adds existing files in external storage to recycler view on app launch
        String audioFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File audioFolder = new File(audioFolderPath);
        File[] audioFiles = audioFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".3gp");
            }
        });

        if(audioFiles != null){
            for(int i = 0; i<audioFiles.length;i++){
                Recording recording = createRecordingFromFile(audioFiles[i]);
                recordings.add(recording);
            }

        }


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordingAdapter(recordings, MainActivity.this);
        recyclerView.setAdapter(adapter);
        Toast.makeText(this, "Tip: Long press recording file to delete.", Toast.LENGTH_LONG).show();


        //Source: https://www.youtube.com/watch?v=z3Gx4whgWcY with my own edits
        ImageButton micButton = (ImageButton) findViewById(R.id.mic_button);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    newRecording.stopRecording();
                    micButton.clearAnimation();
                    isRecording = false;
                    Toast.makeText(MainActivity.this, "Recording stopped.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Audio saved to: " + audioSavePath, Toast.LENGTH_SHORT).show();
                    // Add new recordings to the list and notify the adapter of the change
                    recordings.add(newRecording);
                    adapter.notifyItemInserted(recordings.size() - 1);
                }else {
                    isRecording = true;
                    fileName = "recording_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    newRecording = new Recording(fileName);
                    audioSavePath = newRecording.getFilePath();
                    Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                    try {
                        micButton.startAnimation(a); // Start the animation when recording starts
                        newRecording.startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


           }
        });
    }

    //Source: Lecture slides and chat gpt
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            boolean allPermissionsGranted = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                canWriteToExternalSharedFolders = true;
                canRecordAudio = true;
                canReadToExternalSharedFolders = true;
            } else {
                // Handle permission deny case here
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }


    }

    //Source: Chat gpt.
    /*In order to get audio files from external storage into the recycler view.
    The audio files need to be in the form of the Recording object in order to be added
    to the List<Recording> that is used to create recycler view
     */
    public Recording createRecordingFromFile(File audioFile) {
        // Get the name of the audio file without the file extension
        String name = audioFile.getName().replaceFirst("[.][^.]+$", "");

        // Create a new Recording object with the name and file
        Recording recording = new Recording(name);
        recording.setFile(audioFile);

        return recording;
    }


}