package org.stalexman.fsviewer;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.stalexman.fsviewer.receivers.AlarmReceiver;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private CheckBox checkboxOff;
    private CheckBox checkboxOn;
    private TextView stopTV;
    private TextView startTV;
    private TextView pathTV;
    private TextView pauseTV;
    private SharedPreferences mSettings;

    // Константы, под которыми мы будем сохранять настройки
    public static final String APP_SETTINGS = "mSettings";
    public static final String SETTINGS_FOLDER_PATH = "mPath";
    public static final String SETTINGS_PAUSE = "mPause";
    public static final String SETTINGS_START_HOUR = "mStartHour";
    public static final String SETTINGS_START_MIN = "mStartMinute";
    public static final String SETTINGS_STOP_HOUR = "mStopHour";
    public static final String SETTINGS_STOP_MIN = "mStopMinute";
    public static final String SETTINGS_CHECKBOX_ON = "mCheckboxOn";
    public static final String SETTINGS_CHECKBOX_OFF = "mCheckboxOff";

    private static final String LOG = "LOG SettingsActivity";

    private static int startHour = 0;
    private static int startMinute = 0;
    private static int stopHour = 0;
    private static int stopMinute = 0;
    private static int pauseSec = 5;
    private String folder = "";
    private boolean boolCheckboxOn = false;
    private boolean boolCheckboxOff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Ставлю флаги, чтобы Activity стартовала при отключенном экране
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        // Находим все поля...
        checkboxOff = (CheckBox) findViewById(R.id.off_checkbox);
        checkboxOn = (CheckBox) findViewById(R.id.on_checkbox);
        stopTV = (TextView) findViewById(R.id.stop);
        startTV = (TextView) findViewById(R.id.start);
        pathTV = (TextView) findViewById(R.id.path);
        pauseTV = (TextView) findViewById(R.id.pause);

        // Проверяем уже установленные настройки и выставляем их на экране:
        mSettings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        // Путь до папки
        if (mSettings.contains(SETTINGS_FOLDER_PATH)) {
            folder = mSettings.getString(SETTINGS_FOLDER_PATH, "");
            pathTV.setText(folder);
        }
        // Пауза между фото
        if (mSettings.contains(SETTINGS_PAUSE)) {
            pauseSec = mSettings.getInt(SETTINGS_PAUSE, 5);
            pauseTV.setText("" + pauseSec);
            Log.i(LOG, "on Create() pause = " + pauseSec);
        }
        // Время автовключения
        if (mSettings.contains(SETTINGS_START_HOUR)) {
            startHour = mSettings.getInt(SETTINGS_START_HOUR, 0);
            startMinute = mSettings.getInt(SETTINGS_START_MIN, 0);
            startTV.setText(String.format("%02d", startHour) + ":" + String.format("%02d", startMinute));
            Log.i(LOG, "on Create() startHour=" + startHour + " startMinute=" + startMinute);
        }

        // Время автовыключения
        if (mSettings.contains(SETTINGS_STOP_HOUR)) {
            stopHour = mSettings.getInt(SETTINGS_STOP_HOUR, 0);
            stopMinute = mSettings.getInt(SETTINGS_STOP_MIN, 0);
            stopTV.setText(String.format("%02d", stopHour) + ":" + String.format("%02d", stopMinute));
            Log.i(LOG, "on Create() stopHour=" + stopHour + " stopMinute=" + stopMinute);
        }

        // Состояние чекбоксов
        if (mSettings.contains(SETTINGS_CHECKBOX_ON)) {
            boolCheckboxOn = mSettings.getBoolean(SETTINGS_CHECKBOX_ON, false);
            checkboxOn.setChecked(boolCheckboxOn);
        }
        if (mSettings.contains(SETTINGS_CHECKBOX_OFF)) {
            boolCheckboxOff = mSettings.getBoolean(SETTINGS_CHECKBOX_OFF, false);
            checkboxOff.setChecked(boolCheckboxOff);
        }
    }
    // Установка паузы. Методы вызывается при нажатии пользователя на время паузы.

    public void onChangePath(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivityForResult(intent, 1);
        Log.i(LOG, "on onChangePath()");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        folder = data.getStringExtra("folder");
        pathTV.setText(folder);
    }



    // Установка паузы. Методы вызывается при нажатии пользователя на время паузы.
    public void onSetPause(View view){
        final Dialog d = new Dialog(SettingsActivity.this);
        d.setTitle("Пауза");
        // NumberPicker создается из dialog_int_picker layout
        d.setContentView(R.layout.dialog_int_picker);
        Button b1 = (Button) d.findViewById(R.id.button_set);
        Button b2 = (Button) d.findViewById(R.id.button_cancel);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(60);
        np.setMinValue(1);
        np.setValue(pauseSec);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                pauseTV.setText(String.valueOf(np.getValue()));
                pauseSec = np.getValue();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i("value is",""+newVal);

    }


    // Установка таймера автовключения. Привязана к TextView времени автовключения
    public void onSetStart(View view){
        Bundle b = new Bundle();
        b.putInt("hour", startHour);
        b.putInt("minute", startMinute);
        b.putString("name", "start");
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(b);
        newFragment.show(this.getSupportFragmentManager(), "timePicker");
    }
    // Установка таймера автовыключения. Привязана к TextView времени автовыключения
    public void onSetStop(View view){
        Bundle b = new Bundle();
        b.putInt("hour", stopHour);
        b.putInt("minute", stopMinute);
        b.putString("name", "stop");
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(b);
        newFragment.show(this.getSupportFragmentManager(), "timePicker");
    }


    // Метод, вызываемый при нажатии на клавишу "Сохранить"
    public void onSaveSettings(View view){
        long currTime = Calendar.getInstance().getTimeInMillis();
        // Установка таймеров
        // На включение
        if (checkboxOn.isChecked()) {
            AlarmManager amStart = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.set(Calendar.HOUR_OF_DAY, startHour);
            calendarStart.set(Calendar.MINUTE, startMinute);
            if (currTime - calendarStart.getTimeInMillis() > 0){
                calendarStart.add(Calendar.DATE, 1);
            }
            Intent intent1 = new Intent(this, AlarmReceiver.class);
            intent1.setAction("org.stalexman.fsviewer.START");
            PendingIntent sender1 = PendingIntent.getBroadcast(this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            amStart.set(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), sender1);
        }
        // На выключение
        if (checkboxOff.isChecked()) {
            AlarmManager amStop = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendarStop = Calendar.getInstance();
            calendarStop.set(Calendar.HOUR_OF_DAY, stopHour);
            calendarStop.set(Calendar.MINUTE, stopMinute);
            if (currTime - calendarStop.getTimeInMillis() > 0){
                calendarStop.add(Calendar.DATE, 1);
            }
            Intent intent2 = new Intent(this, AlarmReceiver.class);
            intent2.setAction("org.stalexman.fsviewer.STOP");
            PendingIntent sender2 = PendingIntent.getBroadcast(this, 2, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
            amStop.setRepeating(AlarmManager.RTC_WAKEUP, calendarStop.getTimeInMillis(), 1000*60*60*24, sender2);
        }
        // Сохранение настроек
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(SETTINGS_CHECKBOX_ON, checkboxOn.isChecked());
        editor.putBoolean(SETTINGS_CHECKBOX_OFF, checkboxOff.isChecked());
        editor.putString(SETTINGS_FOLDER_PATH, folder);
        editor.putInt(SETTINGS_PAUSE, pauseSec);
        editor.putInt(SETTINGS_START_HOUR, startHour);
        editor.putInt(SETTINGS_START_MIN, startMinute);
        editor.putInt(SETTINGS_STOP_HOUR, stopHour);
        editor.putInt(SETTINGS_STOP_MIN, stopMinute);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            editor.apply();
        } else {
            editor.commit();
        }
        // И возвращение в вызывающую Activity, с вызовом метода ее onActivityResult
        Intent intent = new Intent();
        intent.putExtra("pause", pauseSec);
        intent.putExtra("folder", folder);
        setResult(RESULT_OK, intent);
        finish();
    }
    // Метод, вызываемый при нажатии на клавишу "Отмена"
    public void onCancel(View view){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    public TextView getStopTV() {
        return stopTV;
    }
    public TextView getStartTV() {
        return startTV;
    }
    // DialogFragment для получения времени
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private int hour;
        private int minute;
        private String nameOfView;
        public TimePickerFragment() {
        }
        // Вызывается при нажатии на время запуска или время выключения
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle b = getArguments();
            hour = b.getInt("hour");
            minute = b.getInt("minute");
            nameOfView = b.getString("name");
            Log.i(LOG, "on onCreateDialog() hour = " + hour + " minute" + minute);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        // Вызывается, когда пользователь жмет на клавишу "Установить" в открытом DialogFragment
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (nameOfView.equals("start")){
                ((SettingsActivity)getActivity()).getStartTV().setText(String.format("%02d", hourOfDay) + ":"
                        + String.format("%02d", minute));
                startHour = hourOfDay;
                startMinute = minute;
                Log.i(LOG, "on onCreateDialog() startHour = " + startHour + " startMinute" + startMinute);

            }
            if (nameOfView.equals("stop")){
                ((SettingsActivity)getActivity()).getStopTV().setText(String.format("%02d", hourOfDay) + ":"
                        + String.format("%02d", minute));
                stopHour = hourOfDay;
                stopMinute = minute;
                Log.i(LOG, "on onCreateDialog() stopHour = " + stopHour + " stopMinute" + stopMinute);

            }
        }
    }

}