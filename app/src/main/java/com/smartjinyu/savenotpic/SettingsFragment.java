package com.smartjinyu.savenotpic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;


/**
 * Created by smart on 2016/7/29.
 */
public class SettingsFragment extends PreferenceFragment {
    final private static int  FILE_CODE = 3;
    private Preference pref_saveDir;
    private Preference pref_version;
    private SwitchPreference pref_hide;
    private Preference pref_feedback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        //pref_saveDir
        final  MainActivity activity=(MainActivity) getActivity();

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

        pref_hide = (SwitchPreference) findPreference("pref_hide");


        pref_hide.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if((boolean) o){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.hide_title);
                    builder.setMessage(R.string.hide_warnings);
                    builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pref_hide.setChecked(false);
                        }
                    });
                    builder.setPositiveButton(R.string.goon, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PackageManager packageManager = activity.getPackageManager();
                            ComponentName componentName = new ComponentName(activity, com.smartjinyu.savenotpic.MainActivity.class);
                            packageManager.setComponentEnabledSetting(componentName,
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                            Toast.makeText(activity,R.string.hided,Toast.LENGTH_LONG).show();
                            pref_hide.setChecked(true);
                        }
                    });

                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.create().show();


                }else{
                    PackageManager packageManager = activity.getPackageManager();
                    ComponentName componentName = new ComponentName(activity, com.smartjinyu.savenotpic.MainActivity.class);
                    packageManager.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    Toast.makeText(activity,R.string.display,Toast.LENGTH_LONG).show();
                    pref_hide.setChecked(false);
                }
                return true;
            }
        });

        setVersion();
        pref_feedback=findPreference("pref_feedback");
        pref_feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent mail = new Intent(Intent.ACTION_SENDTO);
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
            editor.apply();
        }
    }

    private String getEmailContent(){
        String content="\n\n"+"------------------------"+"\n";
        String appName;
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
        String version;
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
