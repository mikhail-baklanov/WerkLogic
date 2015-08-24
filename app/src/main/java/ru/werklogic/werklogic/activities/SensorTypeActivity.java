package ru.werklogic.werklogic.activities;

import android.app.Activity;
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
import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.dm.SensorType;

public class SensorTypeActivity extends Activity {

    public static final String SENSOR_TYPE_NUMBER_PARAM = "sensor_type_number";
    public static final String ACTION1_PARAM = "action1";
    public static final String ACTION2_PARAM = "action2";
    public static final String ACTION3_PARAM = "action3";
    public static final String ACTION4_PARAM = "action4";
    private List<SensorViewInfo> sensorViewInfos = new ArrayList<>();
    private List<ActionViewInfo> actionViewInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_type);

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
        int sensorTypeNumber = getIntent().getIntExtra(SENSOR_TYPE_NUMBER_PARAM, 0);
        selectSensorType(sensorTypeNumber);
    }

    private void initListeners() {
        for (int i = 0; i < sensorViewInfos.size(); i++) {
            final int sensorTypeNumber = i + 1;
            sensorViewInfos.get(i).radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectSensorType(sensorTypeNumber);
                }
            });
        }
    }

    private void selectSensorType(int sensorTypeNumber) {
        for (int i = 0; i < sensorViewInfos.size(); i++) {
            sensorViewInfos.get(i).radioButton.setChecked(sensorTypeNumber == i + 1);

        }
        SensorType st = SensorType.getSensorTypeByNumber(sensorTypeNumber);

        for (int i = 0; i < actionViewInfos.size(); i++) {
            if (st.getButtonsCount() > i) {
                actionViewInfos.get(i).label.setVisibility(View.VISIBLE);
                actionViewInfos.get(i).spinner.setVisibility(View.VISIBLE);
                actionViewInfos.get(i).label.setText(st.getDefaultActionsInfo().get(i).getMessageResId());
                actionViewInfos.get(i).spinner.setSelection(st.getDefaultActionsInfo().get(i).getActionType().getPosition());
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class SensorViewInfo {
        RadioButton radioButton;

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
