package com.smartjinyu.savenotpic;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getStoragePerm();
        if(savedInstanceState==null){
            SettingsFragment settingsFragment=new SettingsFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content,settingsFragment).commit();

        }
    }
    public String getDir(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString("pref_saveDir",getSDcardScreenshotDir());
    }

    private String getSDcardScreenshotDir(){
        if(hasSDCardMounted()){
            return Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/Screenshots";
        }else{
            return getString(R.string.sdcardFail);
        }
    }
    private static boolean hasSDCardMounted() {
        String state = Environment.getExternalStorageState();
        return state != null && state.equals(Environment.MEDIA_MOUNTED);
    }

    private void getStoragePerm(){
        int hasPermission=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasPermission!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this,getString(R.string.needPer),Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
