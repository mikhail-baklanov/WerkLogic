package ru.werklogic.werklogic.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import ru.werklogic.werklogic.R;


public class HistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (savedInstanceState == null) {
            HistoryFragment historyFragment = new HistoryFragment();
            historyFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.container, historyFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

}
