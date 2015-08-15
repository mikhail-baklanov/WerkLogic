package ru.werklogic.werklogic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.werklogic.werklogic.R;


public class LoginActivity extends Activity {
    public static final String EXTRA_PARAM_PASSWORD = "password";
    public static final String EXTRA_PARAM_MESSAGE = "message";
    private static final int MAX_PASSWORD_LEN = 5;

    private String userPassword = "";
    private String rightPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rightPassword = getIntent().getStringExtra(EXTRA_PARAM_PASSWORD);
        setContentView(R.layout.activity_login);
        ((TextView)findViewById(R.id.input_login_text)).setText(getIntent().getStringExtra(EXTRA_PARAM_MESSAGE));
        setButtonClickListeners();
    }

    private void setButtonClickListeners() {
        setButtonListener(R.id.btn0, "0");
        setButtonListener(R.id.btn1, "1");
        setButtonListener(R.id.btn2, "2");
        setButtonListener(R.id.btn3, "3");
        setButtonListener(R.id.btn4, "4");
        setButtonListener(R.id.btn5, "5");
        setButtonListener(R.id.btn6, "6");
        setButtonListener(R.id.btn7, "7");
        setButtonListener(R.id.btn8, "8");
        setButtonListener(R.id.btn9, "9");
        setButtonListener(R.id.btnStar, "*");
        setButtonListener(R.id.btnSharp, "#");
    }

    private void setButtonListener(int resId, final String s) {
        findViewById(resId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPassword += s;
                if (userPassword.length() >= MAX_PASSWORD_LEN) {
                    if (rightPassword == null || userPassword.equals(rightPassword)) {
                        Intent intent = new Intent().putExtra(EXTRA_PARAM_PASSWORD, userPassword);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        userPassword = "";
                    }
                }
                refreshStars();
            }
        });
    }

    private void refreshStars() {
        int[] stars = new int[]{R.id.c1, R.id.c2, R.id.c3, R.id.c4, R.id.c5};
        for (int i = 0; i < MAX_PASSWORD_LEN; i++) {
            ImageView v = (ImageView) findViewById(stars[i]);
            if (i >= userPassword.length())
                v.setImageResource(R.drawable.nostar_psw);
            else
                v.setImageResource(R.drawable.star_psw);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
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
}
