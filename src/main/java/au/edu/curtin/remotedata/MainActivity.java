package au.edu.curtin.remotedata;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String URL_STRING = "https://134.7.234.97:8000";
    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;
    private TextView textArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get UI references
        textArea = findViewById(R.id.textArea);
        progressBar = findViewById(R.id.progressBar);
        Button downloadBtn = findViewById(R.id.downloadBtn);

        progressBar.setVisibility(View.INVISIBLE);

        // Set on click listener
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Download button pushed");
                new MyTask().execute();
            }
        });
    }

    private class MyTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "Do in background started");

            String result = "";

            try {
                // Open connection
                HttpsURLConnection connection = openConnection();

                // Check status
                if (connection == null) {
                    throw new IllegalStateException("Connection not initialised");
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IllegalStateException("Error connecting to server: " + responseCode);
                }

                // Download data
                result = download(connection);
            }
            catch (IOException ioEx) {
                String message = "Error getting response code";
                Log.e(TAG, message, ioEx);
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            catch (NetworkOnMainThreadException netEx) {
                String message = "Cannot run network operations on main thread";
                Log.e(TAG, message, netEx);
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textArea.setText(s);
        }

        // Functions
        private HttpsURLConnection openConnection() {
            Log.d(TAG, "Open connection started");

            HttpsURLConnection connection = null;

            try {
                URL url = new URL(URL_STRING);
                connection = (HttpsURLConnection) url.openConnection();
                DownloadUtils.addCertificate(MainActivity.this, connection);
            }
            catch (MalformedURLException urlEx) {
                String message = "Error creating URL object";
                Log.e(TAG, message, urlEx);
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            catch (IOException ioEx) {
                String message = "Error creating Connection";
                Log.e(TAG, message, ioEx);
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
            catch (GeneralSecurityException secEx) {
                String message = "Error adding certificate";
                Log.e(TAG, message, secEx);
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            return connection;
        }

        private String download(HttpsURLConnection connection) {
            Log.d(TAG, "Download started");

            String result = "";

            try {
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesRead = inputStream.read(buffer);
                }

                outputStream.close();
                result = new String(outputStream.toByteArray());
            }
            catch (IOException ioEx) {
                String message = "Error initialising input stream";
                Log.e(TAG, message, ioEx);
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            return result;
        }
    }
}