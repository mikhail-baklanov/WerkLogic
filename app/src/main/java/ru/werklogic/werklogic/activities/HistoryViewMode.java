package ru.werklogic.werklogic.activities;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import ru.werklogic.werklogic.R;

/**
 * Created by bmw on 22.03.14.
 */
public class HistoryViewMode {
    private TextView tvOneSensor;
    private TextView tvAll;
    private boolean showOne;
    private Context context;

    public void reset(Context context, View view, final Runnable action) {
        this.context = context;
        tvAll = (TextView) view.findViewById(R.id.tvAll);
        tvOneSensor = (TextView) view.findViewById(R.id.tvImportant);
        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportantSwitch(false);
                    action.run();
            }
        });
        tvOneSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportantSwitch(true);
                action.run();
            }
        });
        showImportantSwitch(true);
    }
    private void showImportantSwitch(boolean showImportant) {
        android.content.res.Resources resources = context.getResources();
        this.showOne = showImportant;
        if (showImportant) {
            tvOneSensor.setBackgroundColor(resources.getColor(R.color.historySwitchSelectedColor));
            tvOneSensor.setTextColor(resources.getColor(R.color.historySwitchSelectedTextColor));
            tvAll.setBackgroundColor(resources.getColor(R.color.historySwitchUnselectedColor));
            tvAll.setTextColor(resources.getColor(R.color.historySwitchUnselectedTextColor));
        } else {
            tvAll.setBackgroundColor(resources.getColor(R.color.historySwitchSelectedColor));
            tvAll.setTextColor(resources.getColor(R.color.historySwitchSelectedTextColor));
            tvOneSensor.setBackgroundColor(resources.getColor(R.color.historySwitchUnselectedColor));
            tvOneSensor.setTextColor(resources.getColor(R.color.historySwitchUnselectedTextColor));
        }
    }


    public boolean oneSensorView() {
        return showOne;
    }
}
