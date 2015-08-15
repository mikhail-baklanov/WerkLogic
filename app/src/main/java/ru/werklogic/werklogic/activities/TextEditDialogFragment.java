package ru.werklogic.werklogic.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.werklogic.werklogic.R;

/**
 * Created by bmw on 09.02.14.
 */
public class TextEditDialogFragment extends DialogFragment {

    public interface Listener {
        void onYesClick(String oldValue, String value, Dialog dialog);
    }

    private Listener listener;
    private View view;
    private String title;
    private String label;
    private String value;
    private String yesButtonText;
    private String noButtonText;
    private EditText valueEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_text_edit_dialog, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(title);
        ((TextView) (view.findViewById(R.id.label))).setText(label);
        valueEditText = (EditText) (view.findViewById(R.id.value));
        valueEditText.setText(value);
        ((Button) (view.findViewById(R.id.btnYes))).setText(yesButtonText);

        view.findViewById(R.id.btnYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onYesClick(value, valueEditText.getText().toString(), getDialog());
            }
        });
        view.findViewById(R.id.btnNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });

        ((Button) (view.findViewById(R.id.btnNo))).setText(noButtonText);
    }

    public TextEditDialogFragment(String title, String label, String value, String yesButtonText, String noButtonText, Listener listener) {
        this.title = title;
        this.label = label;
        this.value = value;
        this.yesButtonText = yesButtonText;
        this.noButtonText = noButtonText;
        this.listener = listener;
    }

}