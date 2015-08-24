package ru.werklogic.werklogic.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.commands.AddSmsCommand;
import ru.werklogic.werklogic.commands.DeleteSmsCommand;
import ru.werklogic.werklogic.commands.UpdateSensorActivityCommand;
import ru.werklogic.werklogic.commands.UpdateSensorCommand;
import ru.werklogic.werklogic.commands.UpdateSmsCommand;
import ru.werklogic.werklogic.dm.DataModel;
import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.dm.SmsItem;
import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.protocol.facade.Rec31Facade;
import ru.werklogic.werklogic.service.AutoUnbindServiceConnection;
import ru.werklogic.werklogic.service.IWerkLogicService;
import ru.werklogic.werklogic.service.RunnableWithParameter;
import ru.werklogic.werklogic.service.WerkLogicService;
import ru.werklogic.werklogic.utils.Utils;

import static ru.werklogic.werklogic.utils.Utils.log;

/**
 * Created by bmw on 09.02.14.
 */
public class SettingsEditActivity extends Activity {
    private final static String TAG = SettingsEditActivity.class.getName();
    private static final int ENTER_OLD_PASSWORD = 1;
    private static final int ENTER_PASSWORD = 2;
    private static final int REENTER_PASSWORD = 3;
    private static final int ENTER_OLD_PASSWORD_FOR_RESET = 4;
    private static final int SELECT_SENSOR_TYPE = 5;
    private DataModel dm;
    private String newPassword;
    private BroadcastReceiver broadcastReceiver;
    private android.content.IntentFilter broadcastFilter;

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,
                broadcastFilter);
        refreshSettings();
    }

    private void refreshSettings() {
        refreshSmsList();
        refreshSensors();
        refreshPassword();
    }

    private void refreshPassword() {
        TextView tv = (TextView) findViewById(R.id.psw_info);
        tv.setText(dm.needAuth() ? R.string.psw_present : R.string.psw_absent);
        findViewById(R.id.reset_psw_button).setEnabled(dm.needAuth());
    }

    private void refreshSmsList() {
        LinearLayout layout;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (LinearLayout) findViewById(R.id.smsContainer);
        layout.removeAllViews();
        final List<SmsItem> list = dm.getSmsList();
        if (list != null) {
            for (final SmsItem item : list) {
                View v = inflater.inflate(R.layout.sms_edit_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item.getNumber());
                v.findViewById(R.id.btnChangeActive).setBackgroundResource(item.isActive() ? R.drawable.swi_on : R.drawable.swi_off);
                v.findViewById(R.id.message).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextEditDialogFragment dialog = new TextEditDialogFragment(
                                getString(R.string.updateSms), getString(R.string.labelSms), item.getNumber(), getString(R.string.btnUpdate), getString(R.string.btnCancel), new TextEditDialogFragment.Listener() {
                            @Override
                            public void onYesClick(String oldValue, String value, Dialog dialog) {
                                updateSmsNumber(item, value);
                                dialog.cancel();
                            }
                        });
                        dialog.show(getFragmentManager(), null);

                    }
                });
                v.findViewById(R.id.btnChangeActive).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateSmsActive(item, !item.isActive());
                    }
                });
                v.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.showConfirmDialog(SettingsEditActivity.this, null, getString(R.string.confirmSmsDelete), getString(R.string.btnYes), getString(R.string.btnCancel), new Runnable() {
                            @Override
                            public void run() {
                                deleteSms(item);
                            }
                        });

                    }
                });
                layout.addView(v);
            }
        }
    }

    private void updateSmsActive(SmsItem item, boolean b) {
        dm.updateSmsActive(item, b);
        refreshSmsList();
    }

    private void refreshSensors() {
        LinearLayout layout;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (LinearLayout) findViewById(R.id.sensorsContainer);
        layout.removeAllViews();
        final List<SensorState> list = dm.getSensorsStates();
        if (list != null) {
            for (final SensorState item : list) {
                View v = inflater.inflate(R.layout.sensor_edit_one, layout, false);
                ((TextView) v.findViewById(R.id.sensor_name)).setText(item.getValidName(this));
                Utils.updateSensorStateView(v, item.getState());
                v.findViewById(R.id.sensor_name).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextEditDialogFragment dialog = new TextEditDialogFragment(
                                getString(R.string.updateSensor), getString(R.string.labelSensor), item.getName(), getString(R.string.btnUpdate), getString(R.string.btnCancel), new TextEditDialogFragment.Listener() {
                            @Override
                            public void onYesClick(String oldValue, String value, Dialog dialog) {
                                updateSensor(list, item, value);
                                dialog.cancel();
                            }
                        });
                        dialog.show(getFragmentManager(), null);

                    }
                });
                v.findViewById(R.id.sensor_state).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SensorState.State.NOT_INIT.equals(item.getState())) {
                            forceInit(item.getHardwareSensorInfo());
                        }
                    }
                });
                v.findViewById(R.id.btnChangeActive).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateSensorActivity(item, !item.isActive());
                    }
                });
                v.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.showConfirmDialog(SettingsEditActivity.this, null, getString(R.string.confirmSensorDelete), getString(R.string.btnYes), getString(R.string.btnCancel), new Runnable() {
                            @Override
                            public void run() {
                                deleteSensor(list, item);
                            }
                        });

                    }
                });
                layout.addView(v);
            }
        }
    }

    private void deleteSensor(List<SensorState> list, SensorState item) {
        final HardwareSensorInfo hwi = item.getHardwareSensorInfo();
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                boolean result = service.deleteSensor(hwi.getSensorNumber());
                if (result)
                    dm.removeSensor(hwi);
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }


    private void updateSensorActivity(final SensorState item, final boolean b) {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.processCommand(new UpdateSensorActivityCommand(item.getHardwareSensorInfo(), b));
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }

    private void updateSensor(List<SensorState> list, final SensorState item, final String value) {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.processCommand(new UpdateSensorCommand(item.getHardwareSensorInfo(), value));
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dm = Utils.getApplication(this).getDataModel();
        setContentView(R.layout.activity_settings_edit);

        ((TextView) findViewById(R.id.title)).setText(getString(R.string.settings));

        findViewById(R.id.btnAddSms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextEditDialogFragment dialog = new TextEditDialogFragment(
                        getString(R.string.addSms), getString(R.string.labelSms), "", getString(R.string.btnCreate), getString(R.string.btnCancel), new TextEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String oldValue, String value, Dialog dialog) {
                        if (value != null && value.trim().length() > 0) {
                            addSms(value);
                        }
                        dialog.cancel();
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });
        findViewById(R.id.btnAddSensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dm.isConfigInternal()) {
                    Intent intent = new Intent(SettingsEditActivity.this,
                            SensorTypeActivity.class);
                    intent.putExtra(SensorTypeActivity.SENSOR_TYPE_NUMBER_PARAM, 0);
                    startActivityForResult(intent, SELECT_SENSOR_TYPE);
                }
            }
        });
        findViewById(R.id.change_psw_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (dm.needAuth()) {
                    enterOldPassword();
                } else {
                    enterNewPassword();
                }
            }
        });
        findViewById(R.id.reset_psw_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterOldPasswordForReset();
            }
        });

        findViewById(R.id.show_cloud_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCloud();
            }
        });

        findViewById(R.id.scan_cloud_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCloud();
            }
        });

        refreshSettings();

        broadcastReceiver = new LocalBroadcastReceiver();
        broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(DataModel.DATA_REFRESH_ACTION);
//        broadcastFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    }

    private void scanCloud() {
        IntentIntegrator.initiateScan(this, IntentIntegrator.QR_CODE_TYPES, "Сканирование идентификатора облака");
    }

    private void showCloud() {
        Intent intent = new Intent(this, QRActivity.class);
        intent.putExtra(QRActivity.QR_PARAM, dm.getCloudId());
        startActivity(intent);
    }

    private void addSensor(String value, int sensorTypeNumber, int action1, int action2, int action3, int action4) {
        byte sensorAddress = dm.getFreeSensorAddress();
        Utils.log("Получен свободный адрес для датчика: " + sensorAddress);
        if (sensorAddress < 0)
            return;
        final HardwareSensorInfo hwi = new HardwareSensorInfo(sensorAddress, 0, false);
        dm.addSensor(hwi);
        dm.setSensorName(hwi, value);
        dm.setSensorType(hwi, sensorTypeNumber, action1, action2, action3, action4);
        forceInit(hwi);
    }

    private void forceInit(final HardwareSensorInfo hwi) {
        AsyncTask<Void, Integer, Void> initSensorTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                initSensor(hwi);

                return null;
            }
        };
        initSensorTask.execute();
    }

    private void initSensor(final HardwareSensorInfo hwi) {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.setupSensor(hwi.getSensorNumber(), new Rec31Facade.ISetupDetectorListener() {

                    @Override
                    public void onFirstClick() {
                        Utils.log("Шаг 1 настройки датчика пройден");
                        dm.setSensorState(hwi, SensorState.State.STEP1);
                    }

                    @Override
                    public void onSecondClick() {
                        dm.setSensorState(hwi, SensorState.State.STEP2);
                        Utils.log("Шаг 2 настройки датчика пройден");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        dm.resetSensorState(hwi);
                        Utils.log("Ошибка настройки датчика: " + Utils.getStackTrace(e));
                    }

                    @Override
                    public void onSuccess(HardwareSensorInfo sensorInfo) {
                        Utils.log("Шаг 3 настройки датчика пройден. Датчик зарегистрирован: " + sensorInfo);
                        dm.setSensorInitedState(hwi, sensorInfo.getButton(), sensorInfo.isBatteryHigh());
                    }
                });
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }


    private void enterOldPassword() {
        Intent intent = new Intent(this,
                LoginActivity.class);
        String password = dm.getPassword();
        intent.putExtra(LoginActivity.EXTRA_PARAM_PASSWORD, password);
        intent.putExtra(LoginActivity.EXTRA_PARAM_MESSAGE, getString(R.string.enter_old_password));
        startActivityForResult(intent, ENTER_OLD_PASSWORD);
    }

    private void enterOldPasswordForReset() {
        Intent intent = new Intent(this,
                LoginActivity.class);
        String password = dm.getPassword();
        intent.putExtra(LoginActivity.EXTRA_PARAM_PASSWORD, password);
        intent.putExtra(LoginActivity.EXTRA_PARAM_MESSAGE, getString(R.string.enter_old_password));
        startActivityForResult(intent, ENTER_OLD_PASSWORD_FOR_RESET);
    }

    private void updateSmsNumber(final SmsItem item, final String value) {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.processCommand(new UpdateSmsCommand(item, value));
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }

    private void deleteSms(final SmsItem item) {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.processCommand(new DeleteSmsCommand(item));
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }

    private void addSms(final String value) {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.processCommand(new AddSmsCommand(new SmsItem(value, true)));
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, final Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case ENTER_OLD_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    // вводим новый пароль
                    enterNewPassword();
                }
                break;
            case ENTER_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    // повторяем новый пароль
                    newPassword = data.getStringExtra(LoginActivity.EXTRA_PARAM_PASSWORD);
                    Intent intent = new Intent(this,
                            LoginActivity.class);
                    intent.putExtra(LoginActivity.EXTRA_PARAM_PASSWORD, newPassword);
                    intent.putExtra(LoginActivity.EXTRA_PARAM_MESSAGE, getString(R.string.reenter_new_password));
                    startActivityForResult(intent, REENTER_PASSWORD);
                }
                break;
            case ENTER_OLD_PASSWORD_FOR_RESET:
                if (resultCode == Activity.RESULT_OK) {
                    // если ввели пароль правильно, то сбрасываем его
                    dm.setPassword(null);
                }
                break;
            case REENTER_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    dm.setPassword(newPassword);
                    Toast.makeText(this, R.string.psw_is_changed, Toast.LENGTH_LONG).show();
                }
                break;

            case IntentIntegrator.REQUEST_CODE:
                IntentResult result =
                        IntentIntegrator.parseActivityResult(reqCode, resultCode, data);
                if (result != null) {
                    String contents = result.getContents();
                    if (contents != null) {
                        dm.setCloudId(contents);
                    }
                }
                break;
            case SELECT_SENSOR_TYPE:
                if (resultCode == Activity.RESULT_OK) {
                    TextEditDialogFragment dialog = new TextEditDialogFragment(
                            getString(R.string.addSensor), getString(R.string.labelSensor), "",
                            getString(R.string.btnCreate), getString(R.string.btnCancel), new TextEditDialogFragment.Listener() {
                        @Override
                        public void onYesClick(String oldValue, String value, Dialog dialog) {
                            addSensor(value, data.getIntExtra(SensorTypeActivity.SENSOR_TYPE_NUMBER_PARAM, 0),
                                    data.getIntExtra(SensorTypeActivity.ACTION1_PARAM, 0),
                                    data.getIntExtra(SensorTypeActivity.ACTION2_PARAM, 0),
                                    data.getIntExtra(SensorTypeActivity.ACTION3_PARAM, 0),
                                    data.getIntExtra(SensorTypeActivity.ACTION4_PARAM, 0)
                                    );
                            dialog.cancel();
                        }
                    });
                    dialog.show(getFragmentManager(), null);
                }
                break;
        }
    }

    private void enterNewPassword() {
        Intent intent = new Intent(this,
                LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PARAM_MESSAGE, getString(R.string.enter_new_password));
        startActivityForResult(intent, ENTER_PASSWORD);
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DataModel.DATA_REFRESH_ACTION.equals(action)) {
                log(SettingsEditActivity.class.getSimpleName() + ": Получено событие обновления списка датчиков");
                refreshSettings();
            }
        }
    }
}