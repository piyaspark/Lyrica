package com.example.lyrica_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

public class TrackDetail extends AppCompatActivity {
    private ImageButton backBtn, favoriteBtn;
    private TextView titleText, artistText, lyricText;
    private ProgressDialog pd;
    private int trackId;
    private String trackTitle, trackArtist, trackLyric;
    private String url;
    private final String API_KEY = "apikey=b259ba906c6a3d625a51558589a92cc4";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference favRef = db.collection("favorites");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);
        backBtn = (ImageButton) findViewById(R.id.backBtn2);
        favoriteBtn = (ImageButton) findViewById(R.id.favoriteBtn);
        titleText = (TextView) findViewById(R.id.titleText);
        artistText = (TextView) findViewById(R.id.artistText);
        lyricText = (TextView) findViewById(R.id.lyricText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            trackId = bundle.getInt("trackId");
            trackTitle = bundle.getString("trackTitle");
            trackArtist = bundle.getString("trackArtist");
        }

        Log.d("Track ID: ", String.format("%d", trackId));
        titleText.setText(trackTitle);
        artistText.setText(trackArtist);

        fetch();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackInfo track = new TrackInfo(trackTitle, trackArtist, trackLyric);
                track.setUserId(mAuth.getUid());
                track.setId(trackId);

                favRef.add(track)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Favorite track", "DocumentSnapshot successfully written!");
                                Toast.makeText(TrackDetail.this, "Song has been added to favorite list.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Favorite track", "Error writing document", e);
                            }
                        });
            }
        });
    }


    @SuppressLint("DefaultLocale")
    private void fetch() {
        url = String.format("https://api.musixmatch.com/ws/1.1/track.lyrics.get?format=jsonp&callback=callback&track_id=%d&%s", trackId, API_KEY);
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            String lyrics;
            int statusCode;

            try {
                statusCode = response.getJSONObject("message").getJSONObject("header").getInt("status_code");

                if (statusCode != 200) {
                    lyricText.setText("\n\n\n\n\nNo lyric found for this song.");
                    lyricText.setGravity(Gravity.CENTER);
                } else {
                    lyrics = response.getJSONObject("message").getJSONObject("body").getJSONObject("lyrics").getString("lyrics_body");
                    String[] lyric = lyrics.split(Pattern.quote("*"));
                    trackLyric = lyric[0];
                    lyricText.setText(trackLyric);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            TrackDetail.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pd.isShowing()) pd.dismiss();
                }
            });
        }
    }
}
