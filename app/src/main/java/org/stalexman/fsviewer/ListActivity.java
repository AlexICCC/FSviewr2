package org.stalexman.fsviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends Activity {
    private FileInputStream fis = null;
    private ObjectInputStream ois = null;
    private ArrayList<File> foldersWithImages;
    private String [] listContent;
    private static final String LOG = "LOG ListActivity";
    private ListView listView;


    /*
      В методе onCreate() ищем файл "folders_with_images" во внутренней памяти приложения.
      Там сохранен список папок с изображениями, который был создан SearchAsyncTask из FullScreenActivity.
      Если находим, создаем ListView, в который включаем каждый. Если нет, оставляем пустой экран.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        File first = getFilesDir();
        File second = new File(first, "folders_with_images");
        if (second.exists()){
            try {
                fis = new FileInputStream(second);
                ois = new ObjectInputStream(fis);
                listView = (ListView)findViewById(R.id.listView);
                foldersWithImages = (ArrayList<File>) ois.readObject();
                listContent = new String[foldersWithImages.size()];
                for (int i = 0; i < foldersWithImages.size(); i++){
                    listContent[i]= foldersWithImages.get(i).getAbsolutePath();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listContent);
                listView.setAdapter(adapter);
                // При нажатии на элемент списка OnItemClickListener отправляет SettingActivity выбранную строки
                // и закрывает эту Activity
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG, "itemClick: position = " + position + ", id = " + id);
                        Intent intent = new Intent();
                        intent.putExtra("folder", listContent[position]);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            } catch (IOException ioe){
                Log.i(LOG, "IOException in onCreateView()");
                Toast.makeText(this, "Ошибка: " + ioe.getMessage(), Toast.LENGTH_LONG).show();
            } catch (ClassNotFoundException cnfe){
                Log.i(LOG, "ClassNotFoundException in onCreateView()");
                Toast.makeText(this, "Ошибка: " + cnfe.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }
}