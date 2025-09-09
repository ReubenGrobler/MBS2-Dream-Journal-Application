package com.dlbcsemse02.dreamjournalapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> chooseThumbnailLauncher;
    private View currentJournalEntry;
    private LinearLayout journalEntryContainer;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Here, IDs for various views within activity_main.xml are grabbed.
        journalEntryContainer = findViewById(R.id.journalEntryContainer);
        FloatingActionButton addNewJournalEntry = findViewById(R.id.addNewJournalEntry);
        scrollView = findViewById(R.id.scrollView);

        //Here, the ActivityResultLauncher is defined to handle the result
        //of the image picker intent that is launched when the user wants
        //to add a thumbnail image to their journal entry.
        chooseThumbnailLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            //This method is called when the image picker intent returns a result
            //after the user has selected an image or cancelled the operation.
            @Override
            public void onActivityResult(ActivityResult result) {

                //Checks if the result is OK and that the data is not null (i.e., an image was selected)
                if ((result.getResultCode() == RESULT_OK) && (result.getData() != null)) {
                    Uri imageUri = result.getData().getData();

                    //Grants persistent read permission for the selected image URI
                    //if an image URI was selected
                    if (imageUri != null) {
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        //If a journal entry was selected before launching the image picker,
                        //the image thumbnail is updated with the selected image, and the
                        //image path is saved to the JSON file
                        if (currentJournalEntry != null) {
                            ImageView imageThumbnail = currentJournalEntry.findViewById(R.id.entryThumbnail);
                            imageThumbnail.setVisibility(View.VISIBLE);
                            imageThumbnail.setImageURI(imageUri);

                            int entryID = (int) currentJournalEntry.getTag();
                            ManagingJournalEntries managingJournalEntries = new ManagingJournalEntries();
                            managingJournalEntries.updateJournalImageThumbnail(MainActivity.this, entryID, imageUri.toString());
                        }
                    }
                }
            }
        });

        loadJournalEntries();

        //When clicking on the FAB at the bottom right corner of the screen,
        //the app creates a new journal entry, as well as allowing the
        //app the scroll vertically should the amount of journal entries
        //exceed the screen's space. This is done by calling the selectNewEntryName()
        //method, which prompts the user with a popup.
        addNewJournalEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectNewEntryName();
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //When returning to the main activity from another activity
        //(primarily after editing the text of a journal entry),
        //all journal entries are removed from the home screen and
        //reloaded to ensure that any text that was added gets displayed
        //within the journal entry preview.
        journalEntryContainer.removeAllViews();
        loadJournalEntries();
    }

    //This method creates a new journal entry by using the predefined layout in
    //journal_entry.xml and adding it to the home screen. A boolean value is used
    //to determine whether displaying a given entry on the home screen should
    //also save the entry to the JSON file, since this method is also used when loading
    //all entries to the home screen when the app is opened.
    private void displayJournalEntry(String title, String text, boolean saveToFile, int ID, String imagePath) {

        if (saveToFile) {
            ManagingJournalEntries managingJournalEntries = new ManagingJournalEntries();
            ID = managingJournalEntries.saveJournalEntryCreation(this, title);
        }

        //Creates a view from the layout defined in journal_entry.xml.
        LayoutInflater inflater = LayoutInflater.from(this);
        View journalEntry = inflater.inflate(R.layout.journal_entry, journalEntryContainer, false);

        //Attaches the JSON ID for an entry to a specific journal entry view.
        journalEntry.setTag(ID);

        //Fetches the IDs of the newly created journal entry view.
        TextView titleView = journalEntry.findViewById(R.id.entryTitle);
        TextView textView = journalEntry.findViewById(R.id.entryText);
        ImageView editIcon = journalEntry.findViewById(R.id.editIcon);
        ImageView menuIcon = journalEntry.findViewById(R.id.menuIcon);
        ImageView imageThumbnail = journalEntry.findViewById(R.id.entryThumbnail);

        //Sets the predefined text for the title of the journal entry.
        titleView.setText(title);
        textView.setText(Objects.requireNonNullElse(text, ""));

        //If an image path exists for a journal entry, the image thumbnail
        //is set to visible and the image is loaded. If no image path exists,
        //the image thumbnail doesn't show.
        if (imagePath != null && !imagePath.isEmpty()) {
            imageThumbnail.setVisibility(View.VISIBLE);
            imageThumbnail.setImageURI(Uri.parse(imagePath));
        } else {
            imageThumbnail.setVisibility(View.GONE);
        }

        //When the edit icon is clicked, a popup is shown to the user
        //that allows them to change the name of their journal entry.
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = new EditText(MainActivity.this);

                //This sets the input to automatically be
                //the existing name of the journal entry.
                input.setText(titleView.getText().toString());

                //Creates the popup that prompts the user to edit their journal entry name.
                //By default, the input field contains the existing entry name.
                new AlertDialog.Builder(MainActivity.this).setTitle("Edit Journal Entry Name").setView(input).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override

                            //If the user clicks Save, the name of the journal entry is checked
                            //before it is updated and saved to the JSON file. If the checks fail,
                            //namely to see if the input field is empty or if the length of the
                            //string exceeds 35 characters, a toast message is shown to alert the user.
                            public void onClick(DialogInterface dialog, int which) {
                                String newTitle = input.getText().toString().trim();
                                if (!newTitle.isEmpty()) {
                                    if (newTitle.length() <= 35) {
                                        int entryID = (int) journalEntry.getTag();
                                        titleView.setText(newTitle);
                                        ManagingJournalEntries managingJournalEntries = new ManagingJournalEntries();
                                        managingJournalEntries.updateJournalEntryName(MainActivity.this, entryID, newTitle);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Please enter a shorter name", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Please enter a name for your journal entry", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        //If the user clicks Cancel, the popup closes with no changes being made.
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        //When the menu button is clicked, a submenu of three options is shown to the user,
        //namely the options to delete an entry, to view more detailed information of an entry,
        //and to pin an entry to the top of the main activity.
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creates the submenu containing the three options.
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.journal_entry_submenu, popupMenu.getMenu());

                //This checks if an entry is pinned or not to determine whether
                //the pin entry option should display the default "Pin" or
                //update to "Unpin".
                int entryID = (int) journalEntry.getTag();
                ManagingJournalEntries managingJournalEntries = new ManagingJournalEntries();
                boolean isPinned = managingJournalEntries.isEntryPinned(MainActivity.this, entryID);

                MenuItem pinEntryItem = popupMenu.getMenu().findItem(R.id.pinEntry);
                if (isPinned) {
                    pinEntryItem.setTitle("Unpin");
                }

                //If no image thumbnail exists for a journal entry, the option
                //to remove the image thumbnail is hidden. This ensures that only entries
                //with a thumbnail have the option to remove it.
                ImageView imageThumbnail = journalEntry.findViewById(R.id.entryThumbnail);
                MenuItem removeThumbnailItem = popupMenu.getMenu().findItem(R.id.removeImageThumbnail);
                if (imageThumbnail.getVisibility() != View.VISIBLE) {
                    removeThumbnailItem.setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemID = item.getItemId();
                        if (itemID == R.id.deleteEntry) {

                            //When the delete option is clicked, a popup is shown to prompt
                            //the user to confirm whether they want to delete the journal entry
                            //in question. If they confirm, the journal entry is then
                            //removed from the home screen and deleted from the JSON file.
                            //The user is then notified via a toast that the operation
                            //was successful.
                            new AlertDialog.Builder(MainActivity.this).setTitle("Delete Journal Entry").setMessage("Are you sure you want to delete this entry? This action cannot be undone.").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int entryID = (int) journalEntry.getTag();

                                    journalEntryContainer.removeView(journalEntry);
                                    ManagingJournalEntries managingJournalEntries = new ManagingJournalEntries();
                                    managingJournalEntries.deleteJournalEntry(MainActivity.this, entryID);

                                    Toast.makeText(MainActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();

                            return true;

                            //When the view detailed information option is clicked,
                            //the DetailedInfo activity is launched with the entryID
                            //of the journal entry passed as an extra in the intent,
                            //meaning that the DetailedInfo activity can load
                            //the relevant information for the journal entry in question.
                        } else if (itemID == R.id.viewDetailedInfo) {

                            int entryID = (int) journalEntry.getTag();
                            Intent intent = new Intent(MainActivity.this, DetailedInfo.class);
                            intent.putExtra("ENTRY_ID", entryID);
                            startActivity(intent);

                            return true;

                            //When the pin to top option is clicked, the journal entry
                            //is removed from its current position in the main activity
                            //and added to the top of the list of entries within the main
                            //activity.
                        } else if (itemID == R.id.pinEntry) {

                            //This checks whether an entry is already pinned. If it is,
                            //the entry becomes unpinned, otherwise the entry becomes pinned and
                            //moves to the top of the screen. The user is then notified via
                            //a toast message that the operation was successful.
                            if (isPinned) {
                                managingJournalEntries.updateJournalEntryPinned(MainActivity.this, entryID, false);
                                Toast.makeText(MainActivity.this, "Entry unpinned", Toast.LENGTH_SHORT).show();

                            } else {
                                journalEntryContainer.removeView(journalEntry);
                                journalEntryContainer.addView(journalEntry, 0);

                                managingJournalEntries.updateJournalEntryPinned(MainActivity.this, entryID, true);
                                Toast.makeText(MainActivity.this, "Entry pinned", Toast.LENGTH_SHORT).show();
                            }

                            //After pinning or unpinning an entry, all journal entries
                            //are removed from the main activity and reloaded to ensure
                            //that the order of entries is correct.
                            journalEntryContainer.removeAllViews();
                            loadJournalEntries();

                            return true;

                            //When the image thumbnail option is clicked, the image picker intent
                            //is launched to allow the user to select an image from their device
                            //to be used as the thumbnail for the journal entry in question.
                        } else if (itemID == R.id.imageThumbnail) {
                            //The current journal entry is stored in a variable to be used
                            //when the image picker intent returns a result
                            currentJournalEntry = journalEntry;

                            //Launches the image picker intent to allow the user to select an image
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");

                            //Adds flags to the intent to grant read permission and
                            //persistent URI permission for the selected image
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                            chooseThumbnailLauncher.launch(intent);

                            Toast.makeText(MainActivity.this, "Thumbnail added", Toast.LENGTH_SHORT).show();

                            return true;

                            //When the remove image thumbnail option is clicked, the image
                            //thumbnail is removed from the journal entry and the image path
                            //is deleted from the JSON file. The user is then notified via
                            //a toast message that the operation was successful.
                        } else if (itemID == R.id.removeImageThumbnail) {

                            ImageView imageThumbnail = journalEntry.findViewById(R.id.entryThumbnail);

                            imageThumbnail.setVisibility(View.GONE);
                            imageThumbnail.setImageDrawable(null);

                            managingJournalEntries.updateJournalImageThumbnail(MainActivity.this, entryID, "");

                            Toast.makeText(MainActivity.this, "Thumbnail removed", Toast.LENGTH_SHORT).show();

                            return true;
                        } else {
                            return false;
                        }
                    }

                });
                popupMenu.show();

            }
        });


        //When the journal entry itself is clicked, the EditingJournalText activity
        //is launched with the entryID of the journal entry passed as an extra
        //in the intent, as well as the name of the journal entry. The user is then
        //allowed to enter the text of their journal entry within this new activity.
        journalEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int entryID = (int) journalEntry.getTag();
                String entryName = titleView.getText().toString();
                Intent intent = new Intent(MainActivity.this, EditingJournalText.class);
                intent.putExtra("ENTRY_ID", entryID);
                intent.putExtra("ENTRY_NAME", entryName);
                startActivity(intent);
            }
        });


        journalEntryContainer.addView(journalEntry);
    }

    //This method prompts the user with a popup window
    //to ask them for a name when a new journal entry is created.
    //Once the name has been inputted, the displayJournalEntry()
    // method is called to create and display this new entry.
    private void selectNewEntryName() {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.journal_entry_name_popup, null);

        //Creates the input field for the user.
        EditText input = dialogView.findViewById(R.id.inputEntryName);

        //Creates the popup that prompts the user with their journal entry name
        new AlertDialog.Builder(this).setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {

            //If the user clicks OK, the journal entry is created with the inputted name
            //if the name of the length is less than 35 characters and if the input
            //field is not empty
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    if (title.length() <= 35) {
                        displayJournalEntry(title, null, true, -1, null);
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a shorter name", Toast.LENGTH_SHORT).show();
                    }
                }
                //If no value has been inputted into the field, a toast message is shown
                // to alert the user
                else {
                    Toast.makeText(MainActivity.this, "Please enter a name for your journal entry", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            //If the user clicks Cancel, the popup closes
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        }).create().show();
    }

    //This method checks the JSON file for any existing journal entries,
    //then displays them on the home screen. If the JSON file does not exist,
    //then no action is taken and the method ends.
    private void loadJournalEntries() {
        File file = new File(this.getFilesDir(), "journal_entries.json");
        if (!file.exists()) {
            return;
        }

        //This tries to read the contents of the JSON file and stores it within
        //a StringBuilder object.
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            //If the JSON file was not empty, all content within the file
            //is converted to a JSONArray object and each entry is displayed
            //on the home screen.
            if (!content.toString().isEmpty()) {
                JSONArray journalFileContent = new JSONArray(content.toString());

                //Here, the entries are separated into pinned and normal entries,
                //which will determine the order in which they will be displayed on
                //the home screen.
                java.util.List<JSONObject> pinnedEntries = new java.util.ArrayList<>();
                java.util.List<JSONObject> normalEntries = new java.util.ArrayList<>();
                for (int i = 0; i < journalFileContent.length(); i++) {
                    JSONObject entry = journalFileContent.getJSONObject(i);
                    boolean isPinned = entry.optBoolean("Pinned", false);
                    if (isPinned) {
                        pinnedEntries.add(entry);
                    } else {
                        normalEntries.add(entry);
                    }
                }

                //This loop displays all pinned entries alongside their attributes. Following this
                //loop, another loop displays all normal entries alongside their attributes.
                for (int i = 0; i < pinnedEntries.size(); i++) {
                    JSONObject entry = pinnedEntries.get(i);

                    String title = entry.optString("EntryName", "New Journal Title");
                    String text = entry.optString("EntryText", "");
                    int ID = entry.optInt("ID", -1);
                    String imagePath = entry.optString("ImageThumbnail", "");

                    displayJournalEntry(title, text, false, ID, imagePath);
                }

                for (int i = 0; i < normalEntries.size(); i++) {
                    JSONObject entry = normalEntries.get(i);

                    String title = entry.optString("EntryName", "New Journal Title");
                    String text = entry.optString("EntryText", "");
                    int ID = entry.optInt("ID", -1);
                    String imagePath = entry.optString("ImageThumbnail", "");

                    displayJournalEntry(title, text, false, ID, imagePath);
                }
            }

            //In the event that there is an error with reading the file, an error is thrown.
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

