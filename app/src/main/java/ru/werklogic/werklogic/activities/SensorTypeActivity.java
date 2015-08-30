package ru.werklogic.werklogic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.SensorType;

public class SensorTypeActivity extends Activity {

    public static final String SENSOR_TYPE_NUMBER_PARAM = "sensor_type_number";
    public static final String ACTION1_PARAM = "action1";
    public static final String ACTION2_PARAM = "action2";
    public static final String ACTION3_PARAM = "action3";
    public static final String ACTION4_PARAM = "action4";
    public static final String ACTION_PARAM = "action";
    private List<SensorViewInfo> sensorViewInfos = new ArrayList<>();
    private List<ActionViewInfo> actionViewInfos = new ArrayList<>();
    private int prevSensorTypeNumber, sensorTypeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_type);

        prevSensorTypeNumber = 0;

        sensorViewInfos.add(new SensorViewInfo(null)); // type = NONE
        sensorViewInfos.add(new SensorViewInfo(
                (RadioButton) findViewById(R.id.sensor_type1)));
        sensorViewInfos.add(new SensorViewInfo(
                (RadioButton) findViewById(R.id.sensor_type2)));
        sensorViewInfos.add(new SensorViewInfo(
                (RadioButton) findViewById(R.id.sensor_type3)));
        sensorViewInfos.add(new SensorViewInfo(
                (RadioButton) findViewById(R.id.sensor_type4)));

        actionViewInfos.add(new ActionViewInfo(
                (TextView) findViewById(R.id.action1_text),
                (Spinner) findViewById(R.id.action1_spinner)));
        actionViewInfos.add(new ActionViewInfo(
                (TextView) findViewById(R.id.action2_text),
                (Spinner) findViewById(R.id.action2_spinner)));
        actionViewInfos.add(new ActionViewInfo(
                (TextView) findViewById(R.id.action3_text),
                (Spinner) findViewById(R.id.action3_spinner)));
        actionViewInfos.add(new ActionViewInfo(
                (TextView) findViewById(R.id.action4_text),
                (Spinner) findViewById(R.id.action4_spinner)));

        initListeners();
        this.sensorTypeNumber = getIntent().getIntExtra(SENSOR_TYPE_NUMBER_PARAM, 0);

        initDefaultActions();
        int[] actions = new int[actionViewInfos.size()];
        actions[0] = getIntent().getIntExtra(ACTION1_PARAM, 0);
        actions[1] = getIntent().getIntExtra(ACTION2_PARAM, 0);
        actions[2] = getIntent().getIntExtra(ACTION3_PARAM, 0);
        actions[3] = getIntent().getIntExtra(ACTION4_PARAM, 0);
        sensorViewInfos.get(sensorTypeNumber).setActions(actions);

        selectSensorType(sensorTypeNumber);
    }

    private void initListeners() {
        for (int i = 1; i < sensorViewInfos.size(); i++) {
            final int sensorTypeNumber = i;
            sensorViewInfos.get(i).radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectSensorType(sensorTypeNumber);
                }
            });
        }
        findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult();
            }
        });
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(SENSOR_TYPE_NUMBER_PARAM, sensorTypeNumber);
        for (int i = 0; i < SensorType.values()[sensorTypeNumber].getButtonsCount(); i++) {
            intent.putExtra(ACTION_PARAM + (i + 1), actionViewInfos.get(i).spinner.getSelectedItemPosition());
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void initDefaultActions() {
        for (int i = 0; i < sensorViewInfos.size(); i++) {
            int[] actions = new int[SensorType.values()[i].getButtonsCount()];
            for (int j = 0; j < SensorType.values()[i].getButtonsCount(); j++) {
                actions[j] = SensorType.values()[i].getDefaultActionsInfo().get(j).getActionType().ordinal();
            }
            sensorViewInfos.get(i).setActions(actions);
        }
    }

    private void selectSensorType(int sensorTypeNumber) {
        this.sensorTypeNumber = sensorTypeNumber;
        // выбор радиокнопки текущего сенсора
        for (int i = 0; i < sensorViewInfos.size(); i++) {
            if (sensorViewInfos.get(i).radioButton != null)
                sensorViewInfos.get(i).radioButton.setChecked(sensorTypeNumber == i);
        }
        SensorType st = SensorType.values()[sensorTypeNumber];

        // сохранение действий для выбранного ранее сенсора
        if (prevSensorTypeNumber != sensorTypeNumber) {
            for (int i = 0; i < SensorType.values()[prevSensorTypeNumber].getButtonsCount(); i++) {
                sensorViewInfos.get(prevSensorTypeNumber).getActions()[i] = actionViewInfos.get(i).spinner.getSelectedItemPosition();
            }
            prevSensorTypeNumber = sensorTypeNumber;
        }

        // выставление действий для всех каналов текущего сенсора
        for (int i = 0; i < actionViewInfos.size(); i++) {
            if (st.getButtonsCount() > i) {
                actionViewInfos.get(i).label.setVisibility(View.VISIBLE);
                actionViewInfos.get(i).spinner.setVisibility(View.VISIBLE);
                actionViewInfos.get(i).label.setText(st.getDefaultActionsInfo().get(i).getMessageResId());
                actionViewInfos.get(i).spinner.setSelection(sensorViewInfos.get(sensorTypeNumber).getActions()[i]);
            } else {
                actionViewInfos.get(i).label.setVisibility(View.GONE);
                actionViewInfos.get(i).spinner.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private static class SensorViewInfo {
        RadioButton radioButton;
        private int[] actions;

        public int[] getActions() {
            return actions;
        }

        public void setActions(int[] actions) {
            this.actions = actions;
        }

        private SensorViewInfo(RadioButton radioButton) {
            this.radioButton = radioButton;
        }
    }

    private static class ActionViewInfo {
        TextView label;
        Spinner spinner;

        private ActionViewInfo(TextView label, Spinner spinner) {
            this.label = label;
            this.spinner = spinner;
        }
    }
}
