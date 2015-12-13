package lic.ce.audi;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Guillermo on 12/12/2015.
 */
public class ChooseContactActivity
        extends ListActivity
        implements TextWatcher, LoaderManager.LoaderCallbacks<Cursor>
{
    private TextView mFilter;
    private SimpleCursorAdapter mAdapter;
    private Uri mRingtoneUri;

    public ChooseContactActivity() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setTitle(R.string.choose_contact_title);

        Intent intent = getIntent();
        mRingtoneUri = intent.getData();

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.choose_contact);

        try {
            mAdapter = new SimpleCursorAdapter(
                    this,
                    // Use a template that displays a text view
                    R.layout.contact_row,
                    // Set an empty cursor right now. Will be set in onLoadFinished()
                    null,
                    // Map from database columns...
                    new String[] {
                            ContactsContract.Contacts.CUSTOM_RINGTONE,
                            ContactsContract.Contacts.STARRED,
                            ContactsContract.Contacts.DISPLAY_NAME },
                    // To widget ids in the row layout...
                    new int[] {
                            R.id.row_ringtone,
                            R.id.row_starred,
                            R.id.row_display_name },
                    0);

            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                public boolean setViewValue(View view,
                                            Cursor cursor,
                                            int columnIndex) {
                    String name = cursor.getColumnName(columnIndex);
                    String value = cursor.getString(columnIndex);
                    if (name.equals(ContactsContract.Contacts.CUSTOM_RINGTONE)) {
                        if (value != null && value.length() > 0) {
                            view.setVisibility(View.VISIBLE);
                        } else  {
                            view.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    }
                    if (name.equals(ContactsContract.Contacts.STARRED)) {
                        if (value != null && value.equals("1")) {
                            view.setVisibility(View.VISIBLE);
                        } else  {
                            view.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    }

                    return false;
                }
            });

            setListAdapter(mAdapter);

            // On click, assign ringtone to contact
            getListView().setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent,
                                                View view,
                                                int position,
                                                long id) {
                            assignRingtoneToContact();
                        }
                    }
            );

            getLoaderManager().initLoader(0,  null, this);

        } catch (SecurityException e) {
            // No permission to retrieve contacts?
            Log.e("Audi", e.toString());
        }

        mFilter = (TextView) findViewById(R.id.search_filter);
        if (mFilter != null) {
            mFilter.addTextChangedListener(this);
        }
    }

    private void assignRingtoneToContact() {
        Cursor c = mAdapter.getCursor();
        int dataIndex = c.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
        String contactId = c.getString(dataIndex);

        dataIndex = c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
        String displayName = c.getString(dataIndex);

        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, mRingtoneUri.toString());
        getContentResolver().update(uri, values, null, null);

        String message =
                getResources().getText(R.string.success_contact_ringtone) +
                        " " +
                        displayName;

        Toast.makeText(this, message, Toast.LENGTH_SHORT)
                .show();
        finish();
        return;
    }

    /* Implementation of TextWatcher.beforeTextChanged */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /* Implementation of TextWatcher.onTextChanged */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    /* Implementation of TextWatcher.afterTextChanged */
    public void afterTextChanged(Editable s) {
        //String filterStr = mFilter.getText().toString();
        //mAdapter.changeCursor(createCursor(filterStr));
        Bundle args = new Bundle();
        args.putString("filter", mFilter.getText().toString());
        getLoaderManager().restartLoader(0,  args, this);
    }

    /* Implementation of LoaderCallbacks.onCreateLoader */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String filter = args != null ? args.getString("filter") : null;
        if (filter != null && filter.length() > 0) {
            selection = "(DISPLAY_NAME LIKE \"%" + filter + "%\")";
        }
        return new CursorLoader(
                this,
                ContactsContract.Contacts.CONTENT_URI,
                new String[] {
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.CUSTOM_RINGTONE,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.LAST_TIME_CONTACTED,
                        ContactsContract.Contacts.STARRED,
                        ContactsContract.Contacts.TIMES_CONTACTED },
                selection,
                null,
                "STARRED DESC, " +
                        "TIMES_CONTACTED DESC, " +
                        "LAST_TIME_CONTACTED DESC, " +
                        "DISPLAY_NAME ASC"
        );
    }

    /* Implementation of LoaderCallbacks.onLoadFinished */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("Audi", data.getCount() + " contacts");
        mAdapter.swapCursor(data);
    }

    /* Implementation of LoaderCallbacks.onLoaderReset */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
