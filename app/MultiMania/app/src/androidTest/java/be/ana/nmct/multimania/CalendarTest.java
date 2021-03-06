package be.ana.nmct.multimania;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.test.ApplicationTestCase;

import java.text.ParseException;

import be.ana.nmct.multimania.model.Talk;
import be.ana.nmct.multimania.utils.GoogleCalUtil;
import be.ana.nmct.multimania.utils.SettingsUtil;
import be.ana.nmct.multimania.utils.Utility;

/**
 * Created by Axel on 12/11/2014.
 */

/**
 * This class tests the Calendar functionality
 */
public class CalendarTest extends ApplicationTestCase<Application> {

    private final String TAG = this.getClass().getSimpleName();

    private static final String CALENDAR_NAME = "Multi-Mania 2015";
    private static final String ACCOUNT_NAME = "ana@gmail.com";

    private static Talk sTestTalk;
    private static GoogleCalUtil sCalUtil;

    private ContentResolver mContentResolver;

    static {
        try {
            sTestTalk = new Talk(1, "Test talk", Utility.convertStringToDate("2014-05-19 10:45:00"), Utility.convertStringToDate("2014-05-19 11:30:00"), "TestDescription", 1, false);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public CalendarTest() {
        super(Application.class);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        mContentResolver = context.getContentResolver();
    }

    /**
     * This method gets the account set in SharedPreferences by the user
     * @throws Exception An unknown exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        //Set shared preferences
        SettingsUtil util = new SettingsUtil(this.getContext(), GoogleCalUtil.PREFERENCE_NAME);
        util.setPreference(GoogleCalUtil.PREFERENCE_ACCOUNTNAME, ACCOUNT_NAME);
    }

    /**
     * Gets the Calendar via CalendarContract
     * @return A Cursor containing the Calendar
     */
    private Cursor getCalendarCursor() {
        ContentResolver cr = mContentResolver;
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[]{ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, ACCOUNT_NAME};
        return cr.query(uri, null, selection, selectionArgs, null);
    }

    /**
     * Gets an event by id
     * @param id the id of the event to get
     * @return A Cursor containing the id
     */
    public Cursor getEventByID(long id) {
        ContentResolver cr = mContentResolver;

        final String[] PROJECTION = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
        };
        final String selection = "(" + CalendarContract.Events.OWNER_ACCOUNT + " = ? AND " + CalendarContract.Events._ID + " = ?)";
        final String[] selectionArgs = new String[]{ACCOUNT_NAME, id + ""};

        return cr.query(sCalUtil.buildCalUri(), PROJECTION, selection, selectionArgs, null);
    }

    /**
     * This method tests if a Calendar was successfully created
     */
    public void testCalendarCreated() {

        //Create the calendar
        sCalUtil = new GoogleCalUtil(this.getContext(), CALENDAR_NAME);
        sCalUtil.createCalendar();

        Cursor c = getCalendarCursor();

        assertNotNull(c);
        int calCount = c.getCount();
        assertTrue(calCount == 1);

        c.moveToFirst();
        int nameIndex = c.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME);
        String accountName = c.getString(nameIndex);

        assertTrue(accountName.equals(ACCOUNT_NAME));
    }

  /*  public void testAddEventToCalendar() {

        //Add talk to calendar
        sCalUtil = new GoogleCalUtil(this.getContext(), CALENDAR_NAME);
        sTestTalk.calEventId = sCalUtil.addTalk(sTestTalk);

        //Check if event was added to the calendar
        Cursor c = getEventByID(sTestTalk.calEventId);
        final int TITLE_INDEX = 1;
        String title;

        c.moveToFirst();
        title = c.getString(TITLE_INDEX);

        assertTrue(title.equals(sTestTalk.title));
    }
*/

    /**
     * This method tests if a Calendar was successfully deleted
     */
    public void testCalendarDeleted() {
        //Delete the calendar
        sCalUtil = new GoogleCalUtil(this.getContext(), CALENDAR_NAME);
        sCalUtil.deleteCalendar();

        //Check if calendar still exists
        Cursor c = getCalendarCursor();
        int calCount = c.getCount();
        assertTrue(calCount == 0);
    }
}
