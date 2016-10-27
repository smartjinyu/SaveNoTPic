package com.smartjinyu.savenotpic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nononsenseapps.filepicker.FilePickerActivity;


/**
 * Created by smart on 2016/7/29.
 */
public class SettingsFragment extends PreferenceFragment {
    final private static int  FILE_CODE = 3;
    private Preference pref_saveDir;
    private Preference pref_version;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getPreferenceManager().setSharedPreferencesName("test");
        addPreferencesFromResource(R.xml.settings);
        //pref_saveDir
        MainActivity activity=(MainActivity) getActivity();
        pref_saveDir=findPreference("pref_saveDir");
        pref_saveDir.setSummary(activity.getDir());
        pref_saveDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i, FILE_CODE);
                return true;
            }
        });

        setVersion();
        Preference pref_feedback=findPreference("pref_feedback");
        pref_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent mail=new Intent(Intent.ACTION_SENDTO);
                mail.setData(Uri.parse("mailto:smartjinyu@gmail.com"));
                mail.putExtra(Intent.EXTRA_SUBJECT,"SaveNoTPic Feedback");
                String content=getEmailContent();
                mail.putExtra(Intent.EXTRA_TEXT,content);
                startActivity(mail);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Uri uri = data.getData();
            pref_saveDir.setSummary(uri.toString().substring(7));//remove file://
            editor.putString("saveDir",uri.toString().substring(7));
            editor.commit();
        }
    }

    private String getEmailContent(){
        String content="\n\n"+"------------------------"+"\n";
        String appName="";
        try {
            appName = getActivity().getPackageManager().getPackageInfo("com.smartjinyu.savenotpic", 0).packageName;
        } catch (Exception e) {
            appName=e.toString();
            Log.d("SettingsFragment",e.toString());
        }
        content+= "Package Name: "+appName+"\n";
        content+= "App Version: "+pref_version.getSummary()+"\n";
        content+= "Device Model: "+Build.MODEL+"\n"+"Device Brand: "+Build.BRAND+"\n"+"SDK Version: "+ Build.VERSION.SDK_INT+"\n"+"------------------------";
        return content;

    }


    private void setVersion(){
        String version = "";
        try {
            version = getActivity().getPackageManager().getPackageInfo("com.smartjinyu.savenotpic", 0).versionName;
        } catch (Exception e) {
            version="Error";
            Log.d("SettingsFragment",e.toString());
        }
        pref_version=findPreference("pref_version");
        pref_version.setSummary(version);
    }

}
