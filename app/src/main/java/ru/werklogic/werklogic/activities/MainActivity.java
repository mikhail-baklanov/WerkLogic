package ru.werklogic.werklogic.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.commands.SpyModeSwitchCommand;
import ru.werklogic.werklogic.commands.UpdateSensorCommand;
import ru.werklogic.werklogic.dm.DataModel;
import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.service.AutoUnbindServiceConnection;
import ru.werklogic.werklogic.service.IWerkLogicService;
import ru.werklogic.werklogic.service.RunnableWithParameter;
import ru.werklogic.werklogic.service.WerkLogicService;
import ru.werklogic.werklogic.utils.DateUtils;
import ru.werklogic.werklogic.utils.Utils;

import static ru.werklogic.werklogic.utils.Utils.log;


public class MainActivity extends Activity {

    private static final int INPUT_PASSWORD = 1;
    private BroadcastReceiver broadcastReceiver;
    private android.content.IntentFilter broadcastFilter;
    private DataModel dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.startService(this);

        broadcastReceiver = new LocalBroadcastReceiver();
        broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(DataModel.DATA_REFRESH_ACTION);

        dm = Utils.getApplication(this).getDataModel();

        chooseActivity();
    }

    private void refreshSensors() {

        LinearLayout layout = (LinearLayout) findViewById(R.id.sensorsContainer);
        if (layout == null) return;
        layout.removeAllViews();

        List<SensorState> list = dm.getSensorsStates();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (list != null) {
            for (final SensorState item : list) {
                View v = inflater.inflate(R.layout.sensor_state_one, layout, false);
                ((TextView) v.findViewById(R.id.sensor_name)).setText(item.getValidName(this));
                StringBuilder sb = new StringBuilder();
                Utils.updateSensorStateView(v, item.getState());
                if (item.isActive()) {
                    // отображение нотификаций
                    for (Date d : item.getRecentEvents()) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append(DateUtils.getShortDatePresentation(d));
                    }
                    if (item.hasMoreEvents())
                        sb.append(", ...");

                } else {
                    v.setBackgroundColor(getResources().getColor(R.color.sensor_inactive_color));
                }
                if (item.isAlert())
                    v.findViewById(R.id.sensor_state).setBackgroundResource(R.drawable.alert);
                if (sb.length() == 0)
                    v.findViewById(R.id.sensor_notifications).setVisibility(View.GONE);
                else
                    ((TextView) v.findViewById(R.id.sensor_notifications)).setText(sb.toString());
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                        intent.putExtra(HistoryFragment.SENSOR, (Parcelable)item);
                        startActivity(intent);
                    }
                });
                layout.addView(v);
            }
        }
    }

    private void chooseActivity() {
        if (dm.needAuth()) {
            Intent intent = new Intent(this,
                    LoginActivity.class);
            String password = dm.getPassword();
            intent.putExtra(LoginActivity.EXTRA_PARAM_PASSWORD, password);
            intent.putExtra(LoginActivity.EXTRA_PARAM_MESSAGE, getString(R.string.input_login_text));
            startActivityForResult(intent, INPUT_PASSWORD);
        } else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        setContentView(R.layout.activity_main);

        setSpyButtonListeners();
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity();
            }
        });
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsEditActivity.class);
        startActivity(intent);
    }

    private void setSpyButtonListeners() {
        findViewById(R.id.nospy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(MainActivity.this, new RunnableWithParameter<IWerkLogicService>() {
                    @Override
                    public void run(IWerkLogicService service) {
                        service.processCommand(new SpyModeSwitchCommand(false));
                    }
                });
                bindService(new Intent(MainActivity.this, WerkLogicService.class), connection, 0);
            }
        });
        findViewById(R.id.spy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(MainActivity.this, new RunnableWithParameter<IWerkLogicService>() {
                    @Override
                    public void run(IWerkLogicService service) {
                        service.processCommand(new SpyModeSwitchCommand(true));
                    }
                });
                bindService(new Intent(MainActivity.this, WerkLogicService.class), connection, 0);
            }
        });
    }

    private void refreshAll() {
        refreshSensors();
        refreshSpyButton();
        findViewById(R.id.inet_status).setBackgroundColor(
                getResources().getColor(dm.isWSConnectionStatus() ? R.color.inet_on_color : R.color.inet_off_color));
    }

    private void refreshSpyButton() {
        if (dm.isSpyMode()) {
            findViewById(R.id.nospy_button).setVisibility(View.VISIBLE);
            findViewById(R.id.spy_button).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.spy_button).setVisibility(View.VISIBLE);
            findViewById(R.id.nospy_button).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case INPUT_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    startMainActivity();
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,
                broadcastFilter);
        refreshAll();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DataModel.DATA_REFRESH_ACTION.equals(action)) {
                log(MainActivity.class.getSimpleName() + ": Получено событие обновления списка датчиков");
                // обновить кнопку постановки на охрану и список сенсоров
                refreshAll();
            }
        }
    }
}
