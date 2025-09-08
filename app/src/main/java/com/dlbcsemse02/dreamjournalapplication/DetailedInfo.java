package com.dlbcsemse02.dreamjournalapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetailedInfo extends AppCompatActivity {

    private TextView detailedInfoText;

    //This activity shows detailed information about a specific journal entry,
    //including creation date, last edited date, number of media attachments, and
    //total entry size. It retrieves the entry ID from the intent extras,
    //reads the journal entries from a JSON file, and displays the relevant information.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_info);

        detailedInfoText = findViewById(R.id.detailedInfoText);
        int entryID = getIntent().getIntExtra("ENTRY_ID", -1);
        loadDetailedInfo(entryID);

        //Back button that returns the user back to the main activity.
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //This method loads detailed information about a journal entry from a JSON file.
    //It reads the file, parses the JSON content and performs basic formatting of the data,
    //and then displays the information in a TextView.
    private void loadDetailedInfo(int entryID) {

        File file = new File(this.getFilesDir(), "journal_entries.json");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            //Here, a loop is used to find the journal entry with a specified ID
            //and extracts the relevant information to display. It should be noted that
            //the data containing dates and times are formatted to be more readable.
            //The loop also breaks after the output has been displayed.
            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject journalEntry = journalFileContent.getJSONObject(i);
                if (journalEntry.getInt("ID") == entryID) {
                    String dateAndTimeCreated = journalEntry.optString("DateAndTimeCreated", "N/A");
                    String lastEdited = journalEntry.optString("LastEdited", "N/A");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    String formattedDateAndTimeCreated = LocalDateTime.parse(dateAndTimeCreated).format(formatter);
                    String formattedLastEdited = LocalDateTime.parse(lastEdited).format(formatter);

                    //All media attachments within a journal entry are counted, with their file sizes being
                    //summed to provide a total size. This is then combined with the total size of all
                    //the text data within the entry to give an overall size of the entry.
                    JSONArray allMediaInText = journalEntry.optJSONArray("AllMediaInText");
                    int mediaCount = 0;

                    //The size of the text data is calculated and then added to the
                    //total size of the media attachments within the journal entry.
                    long totalEntrySize = journalEntry.toString().getBytes(StandardCharsets.UTF_8).length;

                    if (allMediaInText != null) {
                        mediaCount = allMediaInText.length();

                        for (int j = 0; j < allMediaInText.length(); j++) {
                            String mediaPath = allMediaInText.optString(j, "");
                            if ((mediaPath != null) && (!mediaPath.isEmpty())) {
                                File mediaFile = new File(mediaPath);
                                if (mediaFile.exists()) {
                                    totalEntrySize += mediaFile.length();
                                }
                            }
                        }
                    }

                    //The total size of the entry is formatted into either bytes, kilobytes,
                    //or megabytes before being displayed to the user.
                    String entrySize = "";
                    if (totalEntrySize < 1024) {
                        entrySize = (totalEntrySize) + " B";
                    } else if (totalEntrySize < (1024 * 1024)) {
                        entrySize = (totalEntrySize / 1024) + " KB";
                    } else {
                        entrySize = (totalEntrySize / (1024 * 1024)) + " MB";
                    }


                    //The detailed information is then formatted into a readable string and
                    //displayed in the TextView.
                    String detailedInfo = "Date and Time Created: " + formattedDateAndTimeCreated + "\n" + "Last Edited: " + formattedLastEdited + "\n" + "Number of Media Attachments: " + mediaCount + "\n" + "Total Entry Size: " + entrySize + "\n";

                    detailedInfoText.setText(detailedInfo);
                    break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
