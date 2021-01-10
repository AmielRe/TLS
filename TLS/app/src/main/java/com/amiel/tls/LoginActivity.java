package com.amiel.tls;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amiel.tls.db.DBHandler;
import com.amiel.tls.db.entities.Person;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseError;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText _midText;
    private TextInputEditText _passwordText;
    private MaterialButton _loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);

        PreferencesManager.initializeInstance(this);

        _midText = (TextInputEditText) findViewById(R.id.input_mid);
        _passwordText = (TextInputEditText) findViewById(R.id.input_password);
        _loginButton = (MaterialButton) findViewById(R.id.btn_login);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    login();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void login() throws NoSuchAlgorithmException{
        Log.d(TAG, "Login");

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.login_progress_message));
        progressDialog.show();

        final String mid = _midText.getText().toString();
        final String password = _passwordText.getText().toString();

        DBHandler.getPerson(mid, new DBHandler.OnGetPersonDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Map<Integer, Person> data) throws NoSuchAlgorithmException {
                if (data.size() > 0) {
                    final Map.Entry<Integer,Person> entry = data.entrySet().iterator().next();
                    String inputHashPassword = CommonUtils.calculateHash(password);

                    if(inputHashPassword.equals(entry.getValue().passwordHash)) {
                        PreferencesManager.getInstance().setIsAdminValue(entry.getValue().isAdmin);
                        setCredentials(mid, progressDialog);
                    }
                    else {
                        progressDialog.dismiss();
                        onLoginFailed();
                    }
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String mid = _midText.getText().toString();
        String password = _passwordText.getText().toString();

        if (mid.isEmpty()) {
            _midText.setError("enter a valid MID");
            valid = false;
        } else {
            _midText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 20) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void setCredentials(final String mid, final ProgressDialog dialog) {
        DBHandler.getPerson(mid, new DBHandler.OnGetPersonDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Map<Integer, Person> data) throws NoSuchAlgorithmException {
                if (data.size() > 0) {
                    final Map.Entry<Integer,Person> entry = data.entrySet().iterator().next();

                    final TextInputEditText newPassword = new TextInputEditText(LoginActivity.this);
                    LinearLayout container = new LinearLayout(LoginActivity.this);
                    container.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(36, 0, 36, 0);
                    newPassword.setLayoutParams(lp);
                    newPassword.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    newPassword.setGravity(android.view.Gravity.TOP| Gravity.START);
                    newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    newPassword.setLines(1);
                    newPassword.setMaxLines(1);
                    newPassword.addTextChangedListener(new TextValidator(newPassword) {
                        @Override public void validate(TextView textView, String text) {
                            if(text.length() < 6 || text.length() > 20) {
                                newPassword.setError(getString(R.string.error_invalid_password_length));
                            } else {
                                newPassword.setError(null);
                            }
                        }
                    });
                    container.addView(newPassword, lp);

                    if(!entry.getValue().firstLogin) {
                        final androidx.appcompat.app.AlertDialog builder = new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                                .setPositiveButton(LoginActivity.this.getString(R.string.update), null)
                                .setView(container)
                                .setTitle(getString(R.string.change_password_title))
                                .setMessage(getString(R.string.change_password_message))
                                .setCancelable(false)
                                .create();
                        builder.show();

                        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(newPassword.getError() == null) {
                                            try {
                                                String newHashedPasword = CommonUtils.calculateHash(newPassword.getText().toString());
                                                DBHandler.updatePersonFirstLogin(mid, newHashedPasword, entry.getValue().roomID);

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new android.os.Handler().postDelayed(
                                                                new Runnable() {
                                                                    public void run() {
                                                                        // On complete call either onLoginSuccess or onLoginFailed
                                                                        onLoginSuccess();
                                                                        // onLoginFailed();
                                                                        dialog.dismiss();
                                                                    }
                                                                }, 1000);
                                                    }
                                                });
                                            } catch (NoSuchAlgorithmException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                // On complete call either onLoginSuccess or onLoginFailed
                                                onLoginSuccess();
                                                // onLoginFailed();
                                                dialog.dismiss();
                                            }
                                        }, 2000);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }
}
