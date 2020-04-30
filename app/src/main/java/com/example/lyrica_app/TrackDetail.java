package com.example.lyrica_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class TrackDetail extends AppCompatActivity {
    private ImageButton backBtn, playlistBtn, favoriteBtn;
    private TextView trackTitle, trackArtist, trackLyric;
    private ProgressDialog pd;
    private int trackId;
    private String url;
    private final String API_KEY = "apikey=b259ba906c6a3d625a51558589a92cc4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);
        backBtn = (ImageButton) findViewById(R.id.backBtn2);
        playlistBtn = (ImageButton) findViewById(R.id.playlistBtn);
        favoriteBtn = (ImageButton) findViewById(R.id.favoriteBtn);
        trackTitle = (TextView) findViewById(R.id.titleText);
        trackArtist = (TextView) findViewById(R.id.artistText);
        trackLyric = (TextView) findViewById(R.id.lyricText);

        Bundle bundle = getIntent().getExtras();
        trackId = bundle.getInt("trackId");
        Log.d("Track ID: ", String.format("%d",trackId));
        trackTitle.setText(bundle.getString("trackTitle"));
        trackArtist.setText(bundle.getString("trackArtist"));

        fetch();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private void fetch(){
        url = String.format("https://api.musixmatch.com/ws/1.1/track.lyrics.get?format=jsonp&callback=callback&track_id=%d&%s",trackId,API_KEY);
        new FetchData().execute(url);
    }

    private class FetchData extends AsyncTask<String, String, JSONObject> {

        protected void onPreExecute() {
            super.onPreExecute();
            TrackDetail.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd = new ProgressDialog(TrackDetail.this);
                    pd.setMessage("Loading track...");
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setCancelable(false);
                    pd.show();
                }
            });
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer data = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    data.append(line + "\n");
                    Log.d("Response: ", "> " + line);
                }
                String json = data.substring(data.indexOf("(") + 1, data.lastIndexOf(")"));

                return new JSONObject(json);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            JSONObject trackDetail;
            try {
                trackDetail = response.getJSONObject("message").getJSONObject("body").getJSONObject("lyrics");
                String lyrics = trackDetail.getString("lyrics_body");
                String[] lyric = lyrics.split(Pattern.quote("*"));
                trackLyric.setText(lyric[0]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            TrackDetail.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pd.isShowing())pd.dismiss();
                }
            });

        }

    }
}
