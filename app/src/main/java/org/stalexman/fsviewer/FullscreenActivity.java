package org.stalexman.fsviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.stalexman.fsviewer.classes.ImageFileFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class FullscreenActivity extends AppCompatActivity {

    private static final int UI_ANIMATION_DELAY = 300;
    public static final String SETTINGS_PAUSE = "mPause";
    public static final String SETTINGS_FOLDER_PATH = "mPath";
    public static final String APP_SETTINGS = "mSettings";
    private SharedPreferences mSettings;

    private int imageRefreshDelay = 3;

    private ImageView mImageView;
    private Button button;
    private ArrayList<File> images;
    private int photoNumber = 0;
    private File dirWithImages;
    private View mControlsView;
    private String LOG = "LOG FullScreenActivity";
    private String folder = "";

    private RefreshAsyncTask refreshAsyncTask;
    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        // Флаги нужны, чтобы при старте Activity по таймеру, она появлялась на экране, даже если он блокирован
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        // Если Activity пересоздается, сохраняем номер фото, чтобы при перевороте экрана, список фото не начинался сначала
        if (savedInstanceState != null) {
            photoNumber = savedInstanceState.getInt("photoNumber");
        }
        // Запускаем поиск папок с фото
        new SearchAsyncTask().execute();
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mImageView = (ImageView)findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.kotenok);
        button = (Button)findViewById(R.id.button);

        // Ищем настройки и выставляем те, что найдем
        mSettings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        // Ищем паузу
        if (mSettings.contains(SETTINGS_PAUSE)) {
            imageRefreshDelay = mSettings.getInt(SETTINGS_PAUSE, 5);
        }
        // Ищем папку с фотографиями
        if (mSettings.contains(SETTINGS_FOLDER_PATH)) {
            folder = mSettings.getString(SETTINGS_FOLDER_PATH, "");
        }
        // Ищем фотографии в folder
        String dirName = folder;
        dirWithImages = new File(dirName);
        images = new ArrayList<>();
        File [] files = dirWithImages.listFiles();
        if (files != null){
             for (File one:files) {
                ImageFileFilter iff = new ImageFileFilter(one);
                if (iff.accept()){
                    images.add(one);
                //               }
                }
            }
        }
        // при нажатии на изображение, должно появиться меню
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        // При нажатии на кнопку "Настройки", запускается Activity  с настройками
        button.setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onStart() {
        Log.i(LOG, "onStart()");
        // Запускаем AsyncTask показа картинок
        refreshAsyncTask = new RefreshAsyncTask();
        // Но если папка не выставлена, ничего не запускаем
        if (!folder.equals("")){
            refreshAsyncTask.execute();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Если теряем Activity из вида, останавливаем AsyncTask
        refreshAsyncTask.cancel(true);
        Log.i(LOG, "onStop()");
        super.onStop();
    }

    // Сохраняем photoNumber при перевороте экрана, чтобы запустить показ слайдов с установленного значения
    // в пересозданной Activity
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("photoNumber", photoNumber);
        super.onSaveInstanceState(outState);
    }
    // Вызывается при возвращении из SettingsActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        String s = data.getStringExtra("folder");
        if (s != null){
            folder = s;
        }
        imageRefreshDelay = data.getIntExtra("pause", imageRefreshDelay);
    }


    // Ниже, до AsyncTask идет обработка работы FullScreenActivity
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i(LOG, "onPostCreate()");
        delayedHide(5000);
    }
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, 1);
            }
            return false;
        }
    };

    private void toggle() {
        Log.i(LOG, "toggle()");
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }
    private void hide() {
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
        Log.i(LOG, "hide()");

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            Log.i(LOG, "mHidePart2Runnable");
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        Log.i(LOG, "show()");
        mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            Log.i(LOG, "mShowPart2Runnable()");
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        Log.i(LOG, "delayedHide()");
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    // AsyncTask. Последовательно показывает фото пользователю. Обновляет фото через метод onProgressUpdate
    public class RefreshAsyncTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPostExecute(Void result) {
        }
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Void doInBackground(Void... params) {
            for (;; photoNumber++){
                if (images.size() == 0){
                    String dirName = folder;
                    dirWithImages = new File(dirName);
                    images = new ArrayList<>();
                    File [] files = dirWithImages.listFiles();
                    if (files != null){
                        for (File one:files) {
                            ImageFileFilter iff = new ImageFileFilter(one);
                            if (iff.accept()){
                                images.add(one);
                            }
                        }
                    }
                }
                publishProgress(photoNumber%images.size());
                if (isCancelled()){
                    return null;
                }
                try {
                    Thread.sleep(imageRefreshDelay * 1000);
                } catch (InterruptedException e){
                    return null;
                }
            }
        }
        @Override
        protected void onProgressUpdate(Integer... number) {
            Log.i(LOG, "Load!");
            mImageView.setImageBitmap(null);
            mImageView.setImageURI(Uri.fromFile(images.get(number[0])));
        }
    }

    // AsyncTask, который ищет папки с фотографиями в файловой системе
    public class SearchAsyncTask extends AsyncTask<Void, Void, String> {
        private final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
        private ArrayList<File> foldersWithImages = new ArrayList<>();
        private String errorText;

        private FileOutputStream fos = null;
        private OutputStreamWriter osw = null;
        private ObjectOutputStream oos = null;
        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("OK")){
                Toast.makeText(getApplicationContext(), "Ошибка: " + errorText, Toast.LENGTH_SHORT);
            }
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected String doInBackground(Void... params) {
            File root = Environment.getExternalStorageDirectory();
            recursiveSearch(root);
            Log.i(LOG, "SearchAsyncTask Directories");
            for (int i = 0; i < foldersWithImages.size(); i++){
                Log.i(LOG, "SearchAsyncTask " + foldersWithImages.get(i).getAbsolutePath());
            }
            try {
                fos = openFileOutput("folders_with_images", MODE_PRIVATE);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(foldersWithImages);
            } catch (IOException e) {
                errorText = e.getMessage();
                Log.i(LOG, "IOException in SearchAsyncTask() doInBackground() with message " + e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
                // looks like some dammit thing - I can't output and I can't close =(
                try {
                    if (osw != null) {
                        oos.flush();
                        oos.close();
                    }
                } catch (IOException e) {
                    errorText = e.getMessage();
                    Log.i(LOG, "IOException in SearchAsyncTask() doInBackground() with message " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return "OK";
        }
        private void recursiveSearch(File dir) {
            File [] fileList = dir.listFiles();
            if (fileList == null){
                return;
            }
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    recursiveSearch(fileList[i]);
                }
            }
            for (int i = 0; i < fileList.length; i++) {
                if (isAnImage(fileList[i])) {
                    foldersWithImages.add(dir);
                    break;
                }
            }
        }
        private boolean isAnImage(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}