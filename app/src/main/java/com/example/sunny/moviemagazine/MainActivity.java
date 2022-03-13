package com.example.sunny.moviemagazine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Response.Listener<JSONObject> {

    private static final String api = BuildConfig.api_key;
    public ArrayList<Movie> ismovie;
    Cursor cursor;
    boolean retrystate = false;
    private ArrayList<Movie> moviecursor = new ArrayList<>();
    private LinearLayout lsnak;
    private RecyclerView myrv;
    private ProgressDialog pd;
    private RecyclerViewAdapter myrevad;
    private String geted_data, data = "popular", pointer = geted_data, message, action;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lsnak = findViewById(R.id.id_LinerSank);
        pd = new ProgressDialog(MainActivity.this);
        geted_data = "http://api.themoviedb.org/3/movie/popular?api_key=" + api;
        retrystate = retry();
        if (retrystate == false) {
            if (savedInstanceState != null) {
                if (savedInstanceState.getString(pointer) == "popular") {
                    data = savedInstanceState.getString(pointer);
                    pjson(geted_data);
                    setTitle("\uD83C\uDFC5  Popular Movies");
                } else if (savedInstanceState.getString(pointer) == "top") {
                    geted_data = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + api;
                    data = savedInstanceState.getString(pointer);
                    setTitle("\uD83D\uDD25  Top_rated Movies");
                    pjson(geted_data);
                } else if (savedInstanceState.getString(pointer) == "favourites") {
                    setTitle("❤  Favourite Movies");
                    data = savedInstanceState.getString(pointer);
                    getFav();
                } else {
                    pjson(geted_data);
                }
            } else {
                pjson(geted_data);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(pointer, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (data == "favourites") {
            getFav();
        }
    }

    private boolean retry() {
        if (!Connection(MainActivity.this)) {
            displaydialog(MainActivity.this);
            return true;
        } else {

            myrv = findViewById(R.id.id_recyclerviwe);
            myrv.setHasFixedSize(true);
            ismovie = new ArrayList<Movie>();
            requestQueue = Volley.newRequestQueue(this);
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                GridLayoutManager gm = new GridLayoutManager(this, 2);
                gm.setOrientation(LinearLayoutManager.HORIZONTAL);
                myrv.setLayoutManager(gm);
            } else {
                GridLayoutManager gm = new GridLayoutManager(this, 1);
                gm.setOrientation(LinearLayoutManager.HORIZONTAL);
                myrv.setLayoutManager(gm);
            }
            message = "Swipe Left To Scroll";
            action = "";
            messg(message, action);
            return false;
        }
    }

    private void displaydialog(MainActivity mainActivity) {
        message = "No Network Connection";
        action = "Retry";
        messg(message, action);
    }

    public boolean Connection(MainActivity mainActivity) {
        ConnectivityManager ce = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = ce.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            android.net.NetworkInfo wcont = ce.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mcont = ce.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return (mcont != null && ni.isConnectedOrConnecting()) || (wcont != null && ni.isConnectedOrConnecting());
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.id_popular) {
            setTitle("\uD83C\uDFC5  Popular Movies");
            data = "popular";
            pd.setMessage("Loading....");
            pd.show();
            geted_data = "http://api.themoviedb.org/3/movie/popular?api_key=" + api;
            ismovie = new ArrayList<Movie>();
            requestQueue = Volley.newRequestQueue(this);
            retry();
            if (retry() == false)
                pjson(geted_data);
            else
                messg(message, action);
        }
        if (item.getItemId() == R.id.id_top) {
            setTitle("\uD83D\uDD25  Top_rated Movies");
            data = "top";
            pd.setMessage("Loading....");
            pd.show();
            geted_data = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + api;
            ismovie = new ArrayList<Movie>();
            requestQueue = Volley.newRequestQueue(this);
            retry();
            if (retry() == false)
                pjson(geted_data);
            else
                messg(message, action);
        }
        if (item.getItemId() == R.id.id_farvoritmenu) {
            data = "favourites";
            getFav();
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Movie> getFavoriteMoviesDataFromCursor(Cursor cursor) {
        ArrayList<Movie> results = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movie movie = new Movie(cursor);
                results.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return results;
    }

    private void pjson(String geted_data) {
        pd.setMessage("Loading....");
        pd.show();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, geted_data, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject results = jsonArray.getJSONObject(i);
                        String imagurl = results.getString("poster_path");
                        String imagurl2 = results.getString("backdrop_path");
                        String title_path = results.getString("original_title");
                        String rate = results.getString("vote_average");
                        String relese_date = results.getString("release_date");
                        String descrip = results.getString("overview");
                        String id = results.getString("id");
                        ismovie.add(new Movie(imagurl, imagurl2, title_path, rate, relese_date, descrip, id));
                    }
                    myrevad = new RecyclerViewAdapter(MainActivity.this, ismovie);
                    myrv.setAdapter(myrevad);
                    pd.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void messg(final String message, final String action) {

        Snackbar snackbar = Snackbar.make(lsnak, message, Snackbar.LENGTH_LONG).setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retry();
                if (retry() == false) {
                    pjson(geted_data);
                } else {
                    messg(message, action);
                }
            }
        });
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.GREEN);
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    @Override
    public void onResponse(JSONObject response) {

    }

    private void getFav() {
        cursor = getContentResolver()
                .query(MovieAttributes.dataAttributes.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
        moviecursor = getFavoriteMoviesDataFromCursor(cursor);
        if (!moviecursor.isEmpty()) {
            myrevad = new RecyclerViewAdapter(MainActivity.this, moviecursor);
            myrv.setAdapter(myrevad);
            setTitle("❤  Favourite Movies");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Movie Magazine");
            builder.setMessage("There is No Favourites");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (!Connection(MainActivity.this)) {
                        displaydialog(MainActivity.this);
                    } else {
                        pjson(geted_data);
                        setTitle("Movie Magazine");
                    }
                }
            });
            builder.show();
        }
    }
}
