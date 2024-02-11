package edu.fandm.oyoung.microphone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.IOException;
import java.util.List;

//Source: Chat Gpt and occasionally an edit or two on my end.
public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {
    private List<Recording> recordings;
    private Context context;
    private Recording currentlyPlayingRecording;
    private int currentlyPlayingPosition = -1;

    public RecordingAdapter(List<Recording> recordings, Context context) {
        this.recordings = recordings;
        this.context = context;
    }

    public void addRecording(Recording recording) {
        recordings.add(recording);
        notifyItemInserted(recordings.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recording recording = recordings.get(position);
        holder.recordingName.setText(recording.getName());

        // Play button click listener
        holder.playButton.setOnClickListener(v -> {
            if (currentlyPlayingPosition == holder.getAdapterPosition()) {
                // The same recording is being played or paused.
                if (recording.isPlaying()) {
                    holder.playButton.setText("play");
                    recording.pause();
                } else {
                    holder.playButton.setText("pause");
                    try {
                        recording.play();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                // A different recording is being played.
                if (currentlyPlayingRecording != null) {
                    currentlyPlayingRecording.pause();
                    notifyItemChanged(currentlyPlayingPosition);
                }
                try {
                    holder.playButton.setText("pause");
                    recording.play();
                    currentlyPlayingRecording = recording;
                    currentlyPlayingPosition = holder.getAdapterPosition();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Check if the current recording is playing and update the UI accordingly.
        if (recording.isPlaying()) {
            holder.playButton.setText("pause");
            currentlyPlayingRecording = recording;
            currentlyPlayingPosition = holder.getAdapterPosition();
        } else {
            holder.playButton.setText("play");
        }

        // Long click listener to delete the recording
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Recording")
                        .setMessage("Are you sure you want to delete this recording?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Remove the recording from the list
                                Recording recording = recordings.get(holder.getAdapterPosition());
                                recordings.remove(recording);
                                notifyItemRemoved(holder.getAdapterPosition());

                                // Delete the audio file from external storage
                                File audioFile = new File(recording.getFilePath());
                                audioFile.delete();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recordingName;
        public Button playButton;
        public ViewHolder(View itemView) {
            super(itemView);
            recordingName = itemView.findViewById(R.id.recording_name);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
