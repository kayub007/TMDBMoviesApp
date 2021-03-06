package com.example.kamol.tmdbmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, AdapterView.OnItemSelectedListener {

    /*
     * Fields
     */

    private String mSearchCriteria = "Most Popular"; // Default sort criteria
    private ArrayList<Movie> mMoviesArray = null;
    private GridView mMainGridView;
    private ProgressBar mProgressBar;

    /*
     * Constants
     */

    // Tag for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    // Constants to form the movie poster URL
    private static final String MOVIEDB_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE = "w185";


    /*
     * Methods
     */

    // Methods that request data and update ========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Check if there is a previous state to be restored
        if (savedInstanceState == null
                || !savedInstanceState.containsKey("movies")
                || !savedInstanceState.containsKey("criteria")
                || !savedInstanceState.containsKey("gridScroll")) {

            makeSearchQuery(mSearchCriteria);

        } else {
            //Retrieve data
            mMoviesArray = savedInstanceState.getParcelableArrayList("movies");
            mSearchCriteria = savedInstanceState.getString("criteria");

            // Prevent cases where there was no internet connection,
            // no data was loaded previously but the user rotates device
            if (mMoviesArray != null) {
                setAdapter();
                restoreScrollPosition(savedInstanceState);
            }
        }
    }

    /*
     * Getters
     */

    public ArrayList<Movie> getMovieArray() {
        return mMoviesArray;
    }

    /*
     * Setters
     */

    public void setMovieArray(ArrayList<Movie> moviesArray) {
        mMoviesArray = moviesArray;
    }

    /**
     * Saves the current moviesArray, searchCriteria and scroll position
     * to avoid fetching data from API when the device is rotated
     *
     * @param outState The state that will be passed to onCreate
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", mMoviesArray);
        outState.putString("criteria", mSearchCriteria);

        // If the view was loaded correctly
        if (mMainGridView != null) {
            outState.putInt("gridScroll", mMainGridView.getFirstVisiblePosition());
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Makes a query to the MoviesDB API if there is internet connection.
     * Otherwise, it shows an dialog to alert the user and sets
     * the movie array to null
     *
     * @param searchCriteria The criteria the user chose to fetch movies data.
     *                       Either "Most Popular" or "Top Rated"
     */
    private void makeSearchQuery(String searchCriteria) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            URL searchURL = NetworkUtils.buildURL(searchCriteria);
            new QueryTask().execute(searchURL);
        } else {
            NetworkUtils.createNoConnectionDialog(this);
            mMoviesArray = null;
        }
    }

    /**
     * Restores scroll for the main GridView when the device is rotated
     *
     * @param savedInstanceState The previous state to be restored that contains
     *                           a "gridScroll" key with the previous scroll position
     */
    private void restoreScrollPosition(Bundle savedInstanceState) {
        int position = savedInstanceState.getInt("gridScroll");
        mMainGridView.smoothScrollToPosition(position);
    }

    /**
     * An AsyncTask to handle network requests to MovieDB API
     * and updates the data received to update the UI
     */
    private class QueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String searchResults = null;

            try {
                // Make query and store the results
                searchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return searchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            createMovieObjects(s);
            setAdapter();
        }
    }

    // Methods that process data after API request ============================================

    /**
     * Set the mMoviesArray to be an array of Movie objects
     * created from the data received from the API request
     *
     * @param JSONString JSON response in String format
     *                   that contains data to make
     *                   the Movie objects
     */
    private void createMovieObjects(String JSONString) {

        try {
            JSONObject JSONObject = new JSONObject(JSONString);
            JSONArray resultsArray = JSONObject.optJSONArray("results");

            ArrayList<Movie> movieArray = createMoviesArrayFromJSONArray(resultsArray);

            if (movieArray.size() > 0) {
                setMovieArray(movieArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an ArrayList of Movie objects from a JSONArray
     *
     * @param resultsArray JSONArray that contains JSON objects for each movie
     *                     fetched from the API request to movieDB
     * @return an ArrayList of Movie objects
     */
    private ArrayList<Movie> createMoviesArrayFromJSONArray(JSONArray resultsArray) {

        ArrayList<Movie> movieArray = new ArrayList<Movie>();

        int i;
        for (i = 0; i < resultsArray.length(); i++) {

            try {
                JSONObject movie = resultsArray.getJSONObject(i);
                Movie movieObject = createMovie(movie);

                if (movieObject != null) {
                    movieArray.add(createMovie(movie));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return movieArray;
    }

    /**
     * Creates a Movie object from a JSON Object fetched from the API request
     * by filling its title, poster URL (adding a base path), plot, release date
     * and vote average.
     *
     * @param movie a JSONObject containing the data for a movie
     * @return Movie object
     */
    private Movie createMovie(JSONObject movie) {
        try {
            // Movie data
            String title = movie.getString("title");
            String posterPath = MOVIEDB_POSTER_BASE_URL + IMAGE_SIZE + movie.getString("poster_path");
            String plot = movie.getString("overview");
            String releaseDate = movie.getString("release_date");
            Double voteAverage = movie.getDouble("vote_average");

            return new Movie(title, releaseDate, posterPath, voteAverage, plot);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Methods for UI =======================================================================

    /**
     * Sets the Movie Adapter for the main layout that will contain movie posters
     */
    private void setAdapter() {
        MovieAdapter mMovieAdapter = new MovieAdapter(MainActivity.this, mMoviesArray, this);

        mMainGridView = (GridView) findViewById(R.id.root_grid_view);
        mMainGridView.invalidateViews();
        mMainGridView.setAdapter(mMovieAdapter);
    }

    // Listeners =======================================================================

    /**
     * Implementation of the onClick method in the MovieAdapter class.
     * It launches an activity passing the corresponding Movie object
     * through an intent
     *
     * @param movie A Movie instance that corresponds to the item clicked
     */
    @Override
    public void onClick(Movie movie) {
        Context context = this;
        Class destinationActivity = DetailsActivity.class;

        // Intent
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra("movieObject", movie);
        startActivity(intent);
    }

    // Menu  =======================================================================

    /**
     * Creates the options menu and spinner
     *
     * @param menu menu to be created
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        createSpinner(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        String searchCriteria = parent.getItemAtPosition(pos).toString();

        /*
         If the query is not searching the same criteria already selected,
         set this criteria as the new selection and make a new API request
          */
        if (!mSearchCriteria.equals(searchCriteria)) {

            mSearchCriteria = searchCriteria;

            switch (searchCriteria) {
                case "Top Rated":
                    makeSearchQuery(searchCriteria);
                    break;
                case "Most Popular":
                    makeSearchQuery(searchCriteria);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //
    }

    /**
     * Creates a Spinner feature in the menu bar with custom layout
     * and format that displays the corresponding selection
     *
     * @param menu The menu being created
     */
    private void createSpinner(Menu menu) {

        // Get spinner and spinner view
        MenuItem spinner = menu.findItem(R.id.sort_spinner);
        Spinner spinnerView = (Spinner) spinner.getActionView();

        // Set listener
        spinnerView.setOnItemSelectedListener(this);

        // Create spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sorting_options_array, R.layout.spinner_item);

        // Custom dropdown layout
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerView.setAdapter(spinnerAdapter);

        // To make sure that the previous selection is kept on device rotation
        spinnerView.setSelection(spinnerAdapter.getPosition(mSearchCriteria));
    }

    // Activity lifecycle methods ========================================================

    /**
     * Lifecycle method to handle cases where the user was initially
     * offline and no data was fetched and then the user reconnects
     * and restarts the app. To handle fetching automatically without
     * user intervention.
     */
    @Override
    public void onRestart() {
        super.onRestart();
        if (mMoviesArray == null) {
            makeSearchQuery(mSearchCriteria);
        }
    }
}