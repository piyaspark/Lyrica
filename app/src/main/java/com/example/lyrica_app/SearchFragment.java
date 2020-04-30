package com.example.lyrica_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private ProgressDialog pd;
    private EditText searchText;
    private final String API_KEY = "apikey=b259ba906c6a3d625a51558589a92cc4";
    private String url = "";
    private String artistKeyword = "";
    private RecyclerView recyclerView;
    private List<TrackInfo> trackInfoList;

    private RecyclerView.Adapter<ShowDataViewHolder> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        trackInfoList = new ArrayList<>();

        searchText = (EditText) view.findViewById(R.id.searchText);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            private Timer timer = new Timer();
            private final long DELAY = 1000;

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        trackInfoList.clear();
                        fetch();
                        synchronized (this){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshRecyclerView();
                                }
                            });
                        }
                    }
                },DELAY);
            }
        });

        return view;
    }


    public void refreshRecyclerView() {
        mAdapter = new RecyclerView.Adapter<ShowDataViewHolder>() {
            @NonNull
            @Override
            public ShowDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(R.layout.view_single_item, parent, false);
                return new ShowDataViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ShowDataViewHolder holder, final int position) {
                holder.title_name.setText(trackInfoList.get(position).getTitleName());
                holder.artist_name.setText(trackInfoList.get(position).getArtistName());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), TrackDetail.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("trackId", trackInfoList.get(position).getTrackId());
                        bundle.putString("trackTitle", trackInfoList.get(position).getTitleName());
                        bundle.putString("trackArtist", trackInfoList.get(position).getArtistName());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return trackInfoList.size();
            }
        };
        recyclerView.setAdapter(mAdapter);
    }

    public void fetch(){
        artistKeyword = "";
        String search = searchText.getText().toString();
        if(search.contains(" ")){
            String[] search_sp = search.split(" ");
            for (int i = 0; i<search_sp.length; i++){
                if (i == search_sp.length-1){
                    artistKeyword += search_sp[i];
                    break;
                }else {
                    artistKeyword += search_sp[i] + "%20";
                }
            }
        }

        url = String.format("https://api.musixmatch.com/ws/1.1/track.search?format=jsonp&callback=callback&q_artist=%s&s_artist_rating=asc&s_track_rating=asc&quorum_factor=1&page_size=30&%s", artistKeyword,API_KEY);
        new FetchData().execute(url);
    }

    private class FetchData extends AsyncTask<String, String, JSONObject> {

        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd = new ProgressDialog(getActivity());
                    pd.setMessage("Please wait");
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
            JSONArray trackListData;
            try {
                trackListData = response.getJSONObject("message").getJSONObject("body").getJSONArray("track_list");

                for (int i = 0; i< trackListData.length(); i++){
                    int trackId = trackListData.getJSONObject(i).getJSONObject("track").getInt("track_id");
                    String trackName = trackListData.getJSONObject(i).getJSONObject("track").getString("track_name");
                    String artistName = trackListData.getJSONObject(i).getJSONObject("track").getString("artist_name");

                    trackInfoList.add(new TrackInfo(trackId,trackName,artistName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pd.isShowing())pd.dismiss();
                }
            });

        }
    }

    public static class ShowDataViewHolder extends RecyclerView.ViewHolder {
        private final TextView title_name;
        private final TextView artist_name;

        public ShowDataViewHolder(final View itemView) {
            super(itemView);
            title_name = itemView.findViewById(R.id.titleName);
            artist_name = itemView.findViewById(R.id.artistName);
        }

        private void titleName(String title) {
            title_name.setText(title);
        }

        private void artistName(String artist) {
            artist_name.setText(artist);
        }
    }
}
