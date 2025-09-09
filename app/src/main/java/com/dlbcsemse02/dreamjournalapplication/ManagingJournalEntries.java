package com.dlbcsemse02.dreamjournalapplication;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManagingJournalEntries {

    //Assigns the name of the JSON file to a variable
    private String file_name;

    //Default constructor assigns the default name of the JSON file
    //to the variable file_name. The second constructor allows for
    //a custom file name to be assigned. This is primarily for testing
    //purposes to avoid overwriting the actual journal_entries.json file.
    public ManagingJournalEntries() {
        this.file_name = "journal_entries.json";
    }

    public ManagingJournalEntries(String file_name) {
        this.file_name = file_name;
    }

    //This method stores a newly created journal entry within a JSON
    //file should the FAB in the main activity be clicked. The values stored
    //per entry are a unique ID, the title of the journal entry, and any text
    //within the entry. Due to the inability to add text during journal creation,
    //no value is stored. The unique ID is returned by the method as entryID
    //to be reused in other methods.
    public int saveJournalEntryCreation(Context context, String title) {

        //Creates JSON structures that will handle JSON parsing. journalFileContent
        //handles the journal entries directly, while jsonObject represents the new
        //journal entry that will be added.
        JSONArray journalFileContent = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        File file = new File(context.getFilesDir(), file_name);

        //Should journal_entries.json exist, the new journal entry is
        //appended to the existing entries in the JSON file
        if (file.exists()) {
            //This tries to read the contents of the file
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                //Handles the effective concatenation of strings while the
                //reader reads every line of the JSON file
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                //If the JSON file was not empty, all content within the file
                //is converted to a JSONArray object
                if (!content.toString().isEmpty()) {
                    journalFileContent = new JSONArray(content.toString());
                }
                //In the event that there is an error with reading the file or that the contents
                //within the file is not valid JSON, an error will be thrown and printed
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        //Here, the values of the newly created journal are
        //added to a JSONObject in order to be stored within the
        //JSON file. Should this be unsuccessful, an error is thrown
        int entryID = 1;
        try {
            //This loop is initiated to ensure that all IDs within
            //the JSON file remain unique, even in the event of a
            //journal entry being deleted.
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                int currentID = entry.getInt("ID");
                if (currentID >= entryID) {
                    entryID = currentID + 1;
                }
            }
            jsonObject.put("ID", entryID);
            jsonObject.put("EntryName", title);
            jsonObject.put("EntryText", "");
            jsonObject.put("ImageThumbnail", "");
            jsonObject.put("DateAndTimeCreated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            jsonObject.put("LastEdited", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            jsonObject.put("AllMediaInText", new JSONArray());
            jsonObject.put("Pinned", false);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        //The new journal entry is added to the end of the array of
        //JSON data
        journalFileContent.put(jsonObject);

        //The new entry is now written to the JSON file and stored. Indentation
        //is added for the sake of readability should the file be accessed by
        //developers/debuggers in the future
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(journalFileContent.toString(4));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
        return entryID;
    }

    //This method updates the name of an existing journal entry within the JSON file
    //when the user clicks the edit icon in the journal entry display.
    public void updateJournalEntryName(Context context, int ID, String newTitle) {
        //If the JSON file does not exist, the method ends with nothing being changed.
        File file = new File(context.getFilesDir(), file_name);
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

            //The contents of the JSON file are converted to a JSONArray object,
            //then iterated through to find the entry with the matching ID.
            //The title is then updated to the new title provided by the user.
            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == ID) {
                    entry.put("EntryName", newTitle);
                    entry.put("LastEdited", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                }
            }

            //The updated JSONArray is written back to the JSON file with indentation
            //for readability.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(journalFileContent.toString(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    //This method deletes any given journal entry via the
    //submenu on the journal entry's view. It does so by comparing
    //all IDs within the JSON file to the ID that was passed to the method,
    //then deleting the entry in the JSON file.
    public void deleteJournalEntry(Context context, int ID) {
        File file = new File(context.getFilesDir(), file_name);
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

            //The contents of the JSON file are converted to a JSONArray object,
            //then iterated through to find the entry with the matching ID
            //parsed by the submenu. The entry is then removed from the JSONArray.
            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == ID) {
                    journalFileContent.remove(i);
                    break;
                }
            }

            //The updated JSONArray is written back to the JSON file with indentation
            //for readability.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(journalFileContent.toString(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    //This method updates the image thumbnail path of an existing journal entry within
    //the JSON file when the user adds or changes said thumbnail within the entry display.
    //After reading the JSON file to determine the matching journal entry via its unique ID,
    //the image path is updated accordingly.
    public void updateJournalImageThumbnail(Context context, int ID, String imagePath) {
        File file = new File(context.getFilesDir(), file_name);
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

            //The contents of the JSON file are converted to a JSONArray object,
            //then iterated through to find the entry with the matching ID.
            //The image path is then updated to show a new or different thumbnail.
            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == ID) {
                    entry.put("ImageThumbnail", imagePath);
                    entry.put("LastEdited", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                }
            }

            //The updated JSONArray is written back to the JSON file with indentation
            //for readability.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(journalFileContent.toString(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    //This method updates the pinned attribute of a journal entry within
    //the JSON file. It achieves this by reading the JSON file, searching for the
    //entry with the matching ID, updating the pinned attribute, and saving it.
    public void updateJournalEntryPinned(Context context, int entryID, boolean pinned) {
        File file = new File(context.getFilesDir(), file_name);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == entryID) {
                    entry.put("Pinned", pinned);
                    break;
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(journalFileContent.toString(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    //This method checks whether a journal entry is pinned or not by reading
    //the JSON file, searching for the entry with the matching ID, and returning
    //a boolean value representing the pinned status of the entry.
    public boolean isEntryPinned(Context context, int entryID) {
        File file = new File(context.getFilesDir(), file_name);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == entryID) {
                    return entry.getBoolean("Pinned");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }


    //This method saves any text inputted by the user within a journal entry's
    //EditText field. It does so by reading the JSON file, searching for the entry
    //with the matching ID, updating the text value, and saving it. By doing so,
    //the JSON file is also updated with the last edited date and time.
    public void saveJournalEntryText(Context context, int entryID, String newText) {

        File file = new File(context.getFilesDir(), file_name);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == entryID) {
                    entry.put("EntryText", newText);
                    entry.put("LastEdited", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                }
            }

            //The updated JSONArray is written back to the JSON file with indentation
            //for human readability.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(journalFileContent.toString(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    //This method saves any media attachments (i.e., images and videos) uploaded by
    //the user within a journal entry. It does so by reading the JSON file, searching
    //for the entry with the matching ID, updating the media array, and saving it.
    public void saveJournalEntryMedia(Context context, int entryID, JSONArray mediaArray) {
        File file = new File(context.getFilesDir(), file_name);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            JSONArray journalFileContent = new JSONArray(content.toString());
            for (int i = 0; i < journalFileContent.length(); i++) {
                JSONObject entry = journalFileContent.getJSONObject(i);
                if (entry.getInt("ID") == entryID) {
                    entry.put("AllMediaInText", mediaArray);
                    entry.put("LastEdited", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    break;
                }
            }

            //The updated JSONArray is written back to the JSON file with indentation
            //for human readability.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(journalFileContent.toString(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

}
