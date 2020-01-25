package xyz.navinda.cleanurls;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Boolean isUrlGenerated = false;
    EditText txtInput;
    Button btnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAction = findViewById(R.id.btnAction);
        txtInput = findViewById(R.id.txtInput);

        btnAction.setOnClickListener(this);

        txtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showShortUrlBtn();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

    }


    @Override
    public void onClick(View v) {
        if (isUrlGenerated) {
            ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = android.content.ClipData.newPlainText("Shorten Url", txtInput.getText());
            clipboard.setPrimaryClip(clip);
            showShortUrlBtn();
            txtInput.setText("");
        } else {
            String longUrl = txtInput.getText().toString();

            if (longUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your URL first!.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isURL(longUrl)) {
                Toast.makeText(MainActivity.this, "Please enter a valid URL!.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            postRequest(longUrl);
        }
    }


    public void postRequest(String longUrl) {
        String url = "https://cleanuri.com/api/v1/shorten";

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("url", longUrl)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String mMessage = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, mMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                handleResponse(responseStr);
            }
        });
    }

    public void handleResponse(String responseStr) {
        try {
            final JSONObject obj = new JSONObject(responseStr);

            if (responseStr.contains("result_url")) {
                final String shortUrl = obj.getString("result_url");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showCopyUrlBtn(shortUrl);
                    }
                });

            } else if (responseStr.contains("error")) {
                final String errorMsg = obj.getString("error");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, errorMsg,
                                Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Unknown Response!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isURL(String url) {
        String URL_REGEX =
                "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))" +
                        "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" +
                        "([).!';/?:,][[:blank:]])?$";

        Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
        Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }


    public void showCopyUrlBtn(String shortUrl) {
        txtInput.setText(shortUrl);
        btnAction.setText("Copy Shorten Url");
        isUrlGenerated = true;
    }

    public void showShortUrlBtn() {
        isUrlGenerated = false;
        btnAction.setText("Short It!");
    }
}


