package com.dlbcsemse02.dreamjournalapplication;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class ManagingJournalEntriesTest {

    private ManagingJournalEntries testManagingJournalEntries;
    private Context context = ApplicationProvider.getApplicationContext();
    private File file;


    //This runs before the start of each test to ensure that the test_journal_entries.json
    //file is deleted before testing a given method. This is to ensure a clean slate for
    //the test. It should be noted that a new instance of ManagingJournalEntries is also created
    //for the purpose of creating a separate json file for testing so as to not interfere with the
    //default journal_entries.json file used in the app.
    @Before
    public void setUp() {

        file = new File(context.getFilesDir(), "test_journal_entries.json");
        testManagingJournalEntries = new ManagingJournalEntries("test_journal_entries.json");

        if (file.exists()) {
            file.delete();
        }
    }


    //Tests whether a journal entry is created and saved correctly. This is done
    //by creating a journal entry, then reading the test_journal_entries.json file
    //and checking that the entry exists and has the correct values. Any
    //field containing a time value is not checked for a match, as it is generated automatically
    //and would be difficult to test.
    @Test
    public void saveJournalEntryCreation() throws Exception {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry");

        assertTrue(file.exists());

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONArray jsonArray = new JSONArray(content);
        JSONObject entry = jsonArray.getJSONObject(0);

        assertEquals(entryID, entry.getInt("ID"));
        assertEquals("Test Entry", entry.getString("EntryName"));
        assertEquals("", entry.getString("EntryText"));
        assertFalse(entry.getBoolean("Pinned"));
        assertNotNull(entry.getString("DateAndTimeCreated"));
        assertNotEquals("", entry.getString("LastEdited"));
    }


    //Tests whether the name of a journal entry is updated correctly. This is done
    //by creating the entry, updating the name, then reading the test_journal_entries.json file
    //and checking the newly updated name.
    @Test
    public void updateJournalEntryName() throws Exception {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry");

        testManagingJournalEntries.updateJournalEntryName(context, entryID, "Updated Entry");

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONArray jsonArray = new JSONArray(content);
        JSONObject entry = jsonArray.getJSONObject(0);

        assertEquals("Updated Entry", entry.getString("EntryName"));
        assertNotEquals("", entry.getString("LastEdited"));
    }


    //Tests whether a journal entry is deleted correctly. This is done by
    //creating two entries, deleting one, then reading the test_journal_entries.json file
    //to see that only the other entry exists. The ID of the remaining entry is also checked
    //to ensure that the correct entry was deleted.
    @Test
    public void deleteJournalEntry() throws Exception {
        int entryID1 = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry 1");
        int entryID2 = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry 2");

        testManagingJournalEntries.deleteJournalEntry(context, entryID1);

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONArray jsonArray = new JSONArray(content);

        assertEquals(1, jsonArray.length());
        assertEquals(entryID2, jsonArray.getJSONObject(0).getInt("ID"));

    }


    //Tests whether the image thumbnail path of a journal entry is updated correctly. This is
    //done by creating an entry, updating the image thumbnail path, then reading the
    //test_journal_entries.json file and checking the newly updated path.
    @Test
    public void updateJournalImageThumbnail() throws Exception {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry with Thumbnail");

        testManagingJournalEntries.updateJournalImageThumbnail(context, entryID, "path/to/SampleThumbnail.png");

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONObject entry = new JSONArray(content).getJSONObject(0);

        assertEquals("path/to/SampleThumbnail.png", entry.getString("ImageThumbnail"));
    }


    //Tests whether the pinned status of a journal entry is updated correctly. This is done
    //by creating an entry, updating the pinned status to true, then reading the test_journal_entries.json
    //file and checking the pinned status.
    @Test
    public void updateJournalEntryPinned() throws Exception {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry to Pin");
        testManagingJournalEntries.updateJournalEntryPinned(context, entryID, true);

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONObject entry = new JSONArray(content).getJSONObject(0);

        assertTrue(entry.getBoolean("Pinned"));
    }


    //Tests whether the isEntryPinned function returns the correct value. This is done
    //by creating an entry, checking that it is not pinned by default, updating the pinned
    //status to true, then checking that it is now pinned.
    @Test
    public void isEntryPinned() {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry to Pin");

        assertFalse(testManagingJournalEntries.isEntryPinned(context, entryID));
        testManagingJournalEntries.updateJournalEntryPinned(context, entryID, true);
        assertTrue(testManagingJournalEntries.isEntryPinned(context, entryID));
    }


    //Tests whether the text of a journal entry is updated correctly. This is done by
    //creating an entry, updating the text, then reading the test_journal_entries.json file
    //and checking whether the text was updated correctly.
    @Test
    public void saveJournalEntryText() throws Exception {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry for Text");
        testManagingJournalEntries.saveJournalEntryText(context, entryID, "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONObject entry = new JSONArray(content).getJSONObject(0);

        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", entry.getString("EntryText"));
        assertNotEquals("", entry.getString("LastEdited"));
    }


    //Tests whether media paths are saved correctly in a journal entry. This is done by
    //creating an entry, saving an array of two media paths (one image and one video), then reading
    //the test_journal_entries.json file and checking whether the media paths were saved correctly.
    @Test
    public void saveJournalEntryMedia() throws Exception {
        int entryID = testManagingJournalEntries.saveJournalEntryCreation(context, "Test Entry with Media");

        JSONArray mediaArray = new JSONArray();
        mediaArray.put("SamplePhotoPath.png");
        mediaArray.put("SampleVideoPath.mp4");

        testManagingJournalEntries.saveJournalEntryMedia(context, entryID, mediaArray);

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONObject entry = new JSONArray(content).getJSONObject(0);

        JSONArray storedMediaArray = entry.getJSONArray("AllMediaInText");
        assertEquals(2, storedMediaArray.length());
        assertEquals("SamplePhotoPath.png", storedMediaArray.getString(0));
        assertEquals("SampleVideoPath.mp4", storedMediaArray.getString(1));
    }


    //Similarly to startup, this runs after each test has been conducted to ensure that
    //the test_journal_entries.json file is deleted after testing a given method. This is to
    //ensure a clean slate for the next test.
    @After
    public void cleanup() {
        if (file.exists()) {
            file.delete();
        }
    }
}