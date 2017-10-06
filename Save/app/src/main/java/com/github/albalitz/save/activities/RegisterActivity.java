package com.github.albalitz.save.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.albalitz.save.R;
import com.github.albalitz.save.persistence.api.Api;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity
        implements ApiActivity, SnackbarActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonRegister;
    private Button buttonCancel;

    private Api api;

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(this.toString(), "onTextChanged triggered.");
            setRegisterButtonState(filledBothInputs());
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this.toString(), "Creating RegisterActivity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        api = new Api(this);

        // assign content
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);

        // prepare stuff
        Log.v(this.toString(), "Adding button click listener ...");
        setRegisterButtonState(filledBothInputs());
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        Log.v(this.toString(), "Adding TextWatcher ...");
        editTextUsername.addTextChangedListener(watcher);
        editTextPassword.addTextChangedListener(watcher);
    }

    protected boolean filledBothInputs() {
        for (String input : Arrays.asList(
                editTextUsername.getText().toString().trim(),
                editTextPassword.getText().toString().trim()
        )) {
            if (input.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected void setRegisterButtonState(boolean state) {
        Log.v(this.toString(), "Register button state is now " + state + ".");
        buttonRegister.setEnabled(state);
    }

    private void registerUser() {
        Utils.showSnackbar(this, "Registering...");

        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        try {
            api.registerUser(username, password);
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegistrationError(String message) {
        // todo
    }

    @Override
    public void onRegistrationSuccess() {
        finish();
    }

    @Override
    public void onSavedLinksUpdate(ArrayList<Link> savedLinks) {}

    @Override
    public void onSaveDialogDone() {}

    @Override
    public View viewFromActivity() {
        return findViewById(R.id.buttonRegister);
    }
}
