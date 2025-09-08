package com.dlbcsemse02.dreamjournalapplication;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class EditingJournalText extends AppCompatActivity {

    private ActivityResultLauncher<Intent> chooseImageLauncher;
    private ActivityResultLauncher<Intent> chooseVideoLauncher;

    private JSONArray entryMediaArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_journal_text);

        //This sets the title of the toolbar to the name of the journal entry being
        //edited. The value was passed from the previous activity via an Intent.
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        String entryName = getIntent().getStringExtra("ENTRY_NAME");
        toolbarTitle.setText(entryName);

        EditText journalEntryEditText = findViewById(R.id.journalEditText);
        int entryID = getIntent().getIntExtra("ENTRY_ID", -1);

        ImageView backIcon = findViewById(R.id.backToHomeIcon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Here, an image activity result launcher is registered to handle the result of an image selection activity.
        //This will allow the user to choose an image from their device to add to the journal entry.
        chooseImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {

                //Checks if the result is OK and that the data is not null (i.e., an image was selected)
                if ((o.getResultCode() == RESULT_OK) && (o.getData() != null)) {
                    Uri imageUri = o.getData().getData();

                    //Grants persistent read permission for the selected image URI
                    //if an image URI was selected
                    if (imageUri != null) {
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    entryMediaArray.put(imageUri.toString());

                    addImageToLayout(imageUri, entryMediaArray);

                }
            }
        });

        //Here, a video activity result launcher is registered to handle the result of a video selection activity.
        //This will allow the user to choose a video from their device to add to the journal entry.
        chooseVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {

                //Checks if the result is OK and that the data is not null (i.e., a video was selected)
                if ((o.getResultCode() == RESULT_OK) && (o.getData() != null)) {

                    //Grants persistent read permission for the selected video URI
                    //if a video URI was selected
                    Uri videoUri = o.getData().getData();
                    if (videoUri != null) {
                        getContentResolver().takePersistableUriPermission(videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    entryMediaArray.put(videoUri.toString());

                    addVideoToLayout(videoUri, entryMediaArray);
                }
            }
        });


        ImageView addMediaIcon = findViewById(R.id.addMediaIcon);
        addMediaIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creates a popup menu when the add media icon is clicked,
                //showing options to add an image or a video to the journal entry.
                PopupMenu popupMenu = new PopupMenu(EditingJournalText.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.editing_journal_entry_add_media, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        int itemID = item.getItemId();
                        if (itemID == R.id.addImage) {

                            //Creates an intent to open a document picker for selecting an image file,
                            //and launches the image selection activity
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");

                            //Grants persistent read permission for the selected image URI
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                            chooseImageLauncher.launch(intent);

                            return true;

                        } else if (itemID == R.id.addVideo) {

                            //Creates an intent to open a document picker for selecting a video file,
                            //and launches the video selection activity
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("video/*");

                            //Grants persistent read permission for the selected video URI
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                            chooseVideoLauncher.launch(intent);

                            return true;

                        } else {
                            return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        if (entryID != -1) {

            //If an entryID was found, this loads the journal entry text from a JSON file and
            //determines whether the ID from the journal entry received matches the one in the file.
            //If so, it loads the EditText field with the corresponding journal entry text
            //so that the user can continue where they last left off.
            try {
                File file = new File(getFilesDir(), "journal_entries.json");
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    StringBuilder content = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    reader.close();

                    JSONArray journalFileContent = new JSONArray(content.toString());
                    for (int i = 0; i < journalFileContent.length(); i++) {
                        JSONObject entry = journalFileContent.getJSONObject(i);
                        if (entry.getInt("ID") == entryID) {
                            String entryText = entry.optString("EntryText", "");
                            journalEntryEditText.setText(entryText);

                            JSONArray savedMedia = entry.optJSONArray("AllMediaInText");
                            if (savedMedia != null) {

                                entryMediaArray = new JSONArray();
                                //This loop iterates through each media URI stored in the
                                //"AllMediaInText" array of the journal entry. For each URI, it
                                //parses the URI string and determines whether it is an image or a video.
                                for (int j = 0; j < savedMedia.length(); j++) {
                                    String uriString = savedMedia.optString(j);
                                    Uri uri = Uri.parse(uriString);

                                    entryMediaArray.put(uriString);

                                    //Checks if the media is a video or an image based on the file extension or
                                    //URI content. Then, it calls the appropriate method to add the
                                    //media to the layout and the media array.
                                    if (uriString.endsWith(".mp4") || uriString.contains("video")) {
                                        addVideoToLayout(uri, entryMediaArray);
                                    } else {
                                        addImageToLayout(uri, entryMediaArray);
                                    }
                                }
                                break;
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //This saves the journal entry text and all media contained within the
        //journal entry page to the JSON file when the activity is paused.
        int entryID = getIntent().getIntExtra("ENTRY_ID", -1);
        if (entryID != -1) {
            EditText journalEntryEditText = findViewById(R.id.journalEditText);
            String updatedText = journalEntryEditText.getText().toString();

            ManagingJournalEntries managingJournalEntries = new ManagingJournalEntries();
            managingJournalEntries.saveJournalEntryText(this, entryID, updatedText);
            managingJournalEntries.saveJournalEntryMedia(this, entryID, entryMediaArray);
        }
    }

    //This method adds an ImageView to the layout of the journal editing page
    //and includes a remove button to delete the image from the layout and
    //the associated media array.
    private void addImageToLayout(Uri imageUri, JSONArray mediaArray) {
        //Creates layouts to hold the ImageView and remove button. The
        //contentLayout is the main layout where all content is added, while
        //the mediaContainer holds the ImageView and its associated remove button.
        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        final FrameLayout mediaContainer = new FrameLayout(EditingJournalText.this);

        //Sets layout parameters for the media container to ensure proper spacing and alignment
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.bottomMargin = 16;
        mediaContainer.setLayoutParams(containerParams);

        //Creates an ImageView and manages the display of the selected image by setting its URI,
        //adjusting its bounds, and adding padding for better presentation.
        ImageView imageView = new ImageView(EditingJournalText.this);
        imageView.setImageURI(imageUri);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(500);

        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(imageParams);

        //Creates a TextView that acts as a remove button ("X") for the image.
        //When clicked, it removes the image from the content layout and
        //also removes the corresponding URI from the mediaArray to keep the data consistent.
        TextView removeButton = new TextView(EditingJournalText.this);
        removeButton.setText("X");
        removeButton.setTextColor(Color.parseColor("#FF6280"));
        removeButton.setTextSize(20);

        //Positions the remove button at the top-right corner of the ImageView,
        //making it easily accessible for users.
        FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        removeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        removeButton.setLayoutParams(removeParams);

        //Stores the final URI string to be used in the click listener.
        //This variable is final due to its use in the inner class below.
        final String finalImageUri = imageUri.toString();

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Removes the image container from the content layout
                //and the image URI from the media array. This is done to
                //ensure that the UI and data remain in sync.
                contentLayout.removeView(mediaContainer);
                if (mediaArray != null) {
                    for (int i = 0; i < mediaArray.length(); i++) {
                        if (mediaArray.optString(i).equals(finalImageUri)) {
                            mediaArray.remove(i);
                            break;
                        }
                    }

                    //Saves the changes immediately after removing to ensure
                    //data consistency for the journal entry in the future
                    int entryID = getIntent().getIntExtra("ENTRY_ID", -1);
                    if (entryID != -1) {
                        new ManagingJournalEntries().saveJournalEntryMedia(EditingJournalText.this, entryID, entryMediaArray);
                    }
                }
                Toast.makeText(EditingJournalText.this, "Image removed", Toast.LENGTH_SHORT).show();
            }
        });

        //Adds the ImageView and remove button to a horizontal linear layout
        mediaContainer.addView(imageView);
        mediaContainer.addView(removeButton);

        contentLayout.addView(mediaContainer);

    }

    //This method adds a VideoView to the layout of the journal editing page
    //and includes a remove button to delete the video from the layout and
    //the associated media array. It sets up the VideoView with media controls
    //and ensures proper layout and functionality.
    private void addVideoToLayout(Uri videoUri, JSONArray mediaArray) {

        //Creates layouts to hold the VideoView and remove button. The
        //contentLayout is the main layout where all content is added, while
        //the mediaContainer holds the VideoView and its associated remove button.
        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        final FrameLayout mediaContainer = new FrameLayout(EditingJournalText.this);

        //Sets layout parameters for the media container to ensure proper spacing and alignment
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.bottomMargin = 16;
        mediaContainer.setLayoutParams(containerParams);
        mediaContainer.setForegroundGravity(android.view.Gravity.CENTER);

        //Creates a VideoView and manages the display of the selected video by
        //setting its URI and adjusting its bounds. The bounds are handled via
        //layout parameters to ensure the video is displayed correctly within the layout.
        VideoView videoView = new VideoView(EditingJournalText.this);
        videoView.setVideoURI(videoUri);

        FrameLayout.LayoutParams videoLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 650);
        videoView.setLayoutParams(videoLayoutParams);

        //This adds media controls such as play and pause to the VideoView.
        MediaController mediaController = new MediaController(EditingJournalText.this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        //Creates a TextView that acts as a remove button ("X") for the video.
        //When clicked, it removes the video from the content layout and
        //also removes the corresponding URI from the mediaArray to keep the data consistent.
        TextView removeButton = new TextView(EditingJournalText.this);
        removeButton.setText("X");
        removeButton.setTextColor(Color.parseColor("#FF6280"));
        removeButton.setTextSize(20);

        //Positions the remove button at the top-right corner of the VideoView,
        //making it easily accessible for users.
        FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        removeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        removeButton.setLayoutParams(removeParams);

        //Stores the final URI string to be used in the click listener.
        //This variable is final due to its use in the inner class below.
        final String finalVideoUri = videoUri.toString();

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Removes the video container from the content layout
                //and the video URI from the media array. This is done to
                //ensure that the UI and data remain in sync.
                contentLayout.removeView(mediaContainer);
                if (mediaArray != null) {
                    for (int i = 0; i < mediaArray.length(); i++) {
                        if (mediaArray.optString(i).equals(finalVideoUri)) {
                            mediaArray.remove(i);
                            break;
                        }
                    }

                    //Saves the changes immediately after removing to ensure
                    //data consistency for the journal entry in the future
                    int entryID = getIntent().getIntExtra("ENTRY_ID", -1);
                    if (entryID != -1) {
                        new ManagingJournalEntries().saveJournalEntryMedia(EditingJournalText.this, entryID, entryMediaArray);
                    }
                }
                Toast.makeText(EditingJournalText.this, "Video removed", Toast.LENGTH_SHORT).show();
            }
        });

        //Adds the video information view and remove button to a horizontal linear layout
        mediaContainer.addView(videoView);
        mediaContainer.addView(removeButton);

        contentLayout.addView(mediaContainer);

        //Ensures that the media container is properly laid out and displayed
        mediaContainer.post(new Runnable() {
            @Override
            public void run() {
                mediaContainer.requestLayout();
                mediaContainer.invalidate();
            }
        });

        //This prevents the video from auto-playing when added to the layout
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.pause();
            }
        });

        videoView.requestFocus();
    }
}