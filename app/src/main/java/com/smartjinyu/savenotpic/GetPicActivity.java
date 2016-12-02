package com.smartjinyu.savenotpic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetPicActivity extends Activity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 233;
    final private String dirError = "dir error";
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int hasPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            handlePic();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void handlePic() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultDir = getDefaultDir();
        if (!dirError.equals(defaultDir)) {
            String saveDir = sharedPreferences.getString("saveDir", getDefaultDir());
            Boolean shareLater = sharedPreferences.getBoolean("pref_share", true);
            //read settings
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            //getIntent
            if (Intent.ACTION_SEND.equals(action) && "image/png".equals(type)) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    String sUri= uri.toString();
                    String realPath= sUri;
                    Log.d("gggggggg",sUri.substring(sUri.lastIndexOf(".")));
                    if(!sUri.substring(sUri.lastIndexOf(".")).equals(".png") && !sUri.substring(sUri.lastIndexOf(".")).equals(".PNG")){
                        realPath = getRealPathFromURI(this, uri);
                    }
                    Log.d("Geppppppp",realPath);
                    String picName = realPath.substring(realPath.lastIndexOf("/"), realPath.lastIndexOf("."));
                    try {
                        Bitmap bitmap = getBitmapFromUri(uri);
                        fileName = saveDir + "/" + picName + ".png";
                        File picFile = new File(fileName);
                        int i = 1;
                        while (picFile.exists()) {
                            fileName = saveDir + "/" + picName + "(" + i + ").png";
                            picFile = new File(fileName);
                            i++;
                        }
                        //avoid conflicts
                        if(!picFile.getParentFile().exists()){
                            picFile.getParentFile().mkdirs();
                        }
                        //avoid directory not exists, which will cause FileOutPutStream throw an exception
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(picFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));//refresh the gallery after saving
                            Toast.makeText(this, getString(R.string.shareSucceed), Toast.LENGTH_SHORT).show();
                            if (shareLater) {
                                Intent shareIntent = new Intent();
                                Uri shareuri = FileProvider.getUriForFile(getApplicationContext(),"com.smartjinyu.savenotpic.imageProvider",picFile);
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM,shareuri);
                                shareIntent.setType("image/png");
                                startActivity(shareIntent);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.d("GetPic001", e.toString());
                            Toast.makeText(this, getString(R.string.shareFail001), Toast.LENGTH_LONG).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("GetPic002", e.toString());
                            Toast.makeText(this, getString(R.string.shareFail002), Toast.LENGTH_LONG).show();

                        }
                    } catch (IOException e) {
                        Log.d("GetPic003", e.toString());
                        Toast.makeText(this, getString(R.string.shareFail003), Toast.LENGTH_LONG).show();

                    }
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.shareFail004), Toast.LENGTH_LONG).show();

        }
        finish();
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

private String getRealPathFromURI(Context mContext,Uri contentUri) {
    String[] proj = { MediaStore.Images.Media.DATA };
    CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
    Cursor cursor = loader.loadInBackground();
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    String result = cursor.getString(column_index);
    cursor.close();
    return result;
}

    private String getDefaultDir() {
        if (hasSDCardMounted()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Screenshots";
        } else {
            return dirError;
        }
    }

    private static boolean hasSDCardMounted() {
        String state = Environment.getExternalStorageState();
        return state != null && state.equals(Environment.MEDIA_MOUNTED);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, getString(R.string.needPer), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    handlePic();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
