package com.example.mymusicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int GET_FOLDER_CODE = 147;
    private static int pos;
    public static MediaPlayer mediaPlayer;
    Button play, pause,selectFolder;
    SeekBar seekBar;
    ListView listView;
    String[] List;
    TextView txtStart, txtEnd;
    String folderPath;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = findViewById(R.id.btnPlay);
        pause = findViewById(R.id.btnPause);
        seekBar = findViewById(R.id.seekBar);
        listView = (ListView) findViewById(R.id.listView);
        mediaPlayer = new MediaPlayer();
        txtStart = findViewById(R.id.textViewStart);
        txtEnd = findViewById(R.id.textViewEnd);
        selectFolder = findViewById(R.id.btnSelectFolder);

        folderPath = Environment.getExternalStorageDirectory().toString() + "/ADM";

        if (checkPermission()) {


            checkFiles();

            //On click on List View
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Uri uri = Uri.parse(folderPath+"/" + List[i]);
                    mediaPlayer.reset();
                    mediaPlayer = MediaPlayer.create(MainActivity.this, uri);

                    //TODO 1.play song seq
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            play();
                        }
                    });



                    Toast.makeText(MainActivity.this, List[i], Toast.LENGTH_SHORT).show();

                }
            });


        } else {
            Toast.makeText(this, "Give permission", Toast.LENGTH_SHORT).show();
        }

//        Toast.makeText(this, Environment.getExternalStorageDirectory().toString(),Toast.LENGTH_SHORT).show();

        //Select FOlder
        selectFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent,GET_FOLDER_CODE);
            }
        });


    }

    @Override
    //Fetch Result from Open Dir Intent
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == GET_FOLDER_CODE){
            Uri uri = data.getData();

            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getTreeDocumentId(uri));
            String path = getPath(this, docUri);

            folderPath = path;
                checkFiles();
//            Log.i("Get Folder act", path);
        }

    }
// Get Dir path from intent uri
    private String getPath(MainActivity mainActivity, Uri docUri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(mainActivity, docUri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(docUri)) {
                final String docId = DocumentsContract.getDocumentId(docUri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
        }
        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    public void play(View view) {
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());
    }


    public void play() {

        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());
        seekBarCustomize();

    }


    public void pause(View view) {
        mediaPlayer.pause();
    }

    public void stop(View view) {
        stopPlaying();
    }

    public void stopPlaying() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            seekBar = null;
        }


    }

    public void showList(String[] ListArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ListArray);
        listView.setAdapter(adapter);
    }

    public void seekBarCustomize() {

//        Log.i("Duration",String.valueOf((mediaPlayer.getDuration() / 60000 )));

        txtEnd.setText(String.valueOf(new DecimalFormat("##.##").format((mediaPlayer.getDuration() / 60000.0))));

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
//                    txtStart.setText(String.valueOf( new DecimalFormat("##.##").format((mediaPlayer.getCurrentPosition() )))  );

                }

            }
        }, 0, 1000);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pos = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(pos);
            }
        });

    }

    public boolean checkPermission() {
        //Check And Grant Permission
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        123);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

    ArrayList<HashMap<String, String>> getPlayList(String rootPath) {
        ArrayList<HashMap<String, String>> fileList = new ArrayList<>();


        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null) {
                        fileList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    song.put("file_name", file.getName());
                    fileList.add(song);
                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }

    public void checkFiles() {

        ArrayList<HashMap<String, String>> songList = getPlayList(folderPath);
        if (songList != null) {
            Toast.makeText(this, String.valueOf(songList.size()), Toast.LENGTH_SHORT).show();
            List = new String[songList.size()];
            for (int i = 0; i < songList.size(); i++) {
                String fileName = songList.get(i).get("file_name");


                List[i] = fileName;


//                String filePath=songList.get(i).get("file_path");
                //here you will get list of file name and file path that present in your device
//                Log.e("file details "," name ="+fileName +" path = "+filePath);
            }
            Log.i("Total files", String.valueOf(songList.size()));

            //Fill List view
            showList(List);

        } else
            Toast.makeText(this, "Null", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}
