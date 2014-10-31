package be.ana.nmct.multimania.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import be.ana.nmct.multimania.R;
import be.ana.nmct.multimania.utils.GoogleCalUtil;

public class SettingsFragment extends Fragment {

    private final static String TAG = SettingsFragment.class.getCanonicalName();

    private static final int REQUEST_CODE_EMAIL = 1;

    private static CheckBox checkbox_notify;
    private static CheckBox checkbox_sync;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        checkbox_notify = (CheckBox) v.findViewById(R.id.checkbox_notify);
        checkbox_sync = (CheckBox) v.findViewById(R.id.checkbox_sync);

        checkbox_notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "checked");
            }
        });

        checkbox_sync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    Toast.makeText(getActivity(), "Please choose your Google Account", Toast.LENGTH_LONG).show();
                    askUserEmail();

                } else {
                    GoogleCalUtil cal = new GoogleCalUtil(getActivity());
                    cal.deleteCalendar();
                }
            }
        });


        return v;
    }

    private void askUserEmail() {
        try {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EMAIL && data != null && resultCode == Activity.RESULT_OK) {

            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if (accountName != "") {
                GoogleCalUtil cal = new GoogleCalUtil(getActivity());
                cal.setAccount(accountName);
                cal.createCalendar();
            } else {
                Toast.makeText(getActivity(), "No valid account selected", Toast.LENGTH_LONG).show();
                checkbox_sync.setChecked(false);
            }
        } else {
            Toast.makeText(getActivity(), "No account selected", Toast.LENGTH_LONG).show();
            checkbox_sync.setChecked(false);
        }
    }



}
