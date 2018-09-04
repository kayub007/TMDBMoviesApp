package com.example.kamol.tmdbmoviesapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    /*
     * Fields
     */

    private ImageView moviePosterView;
    private TextView movieVoteAverageView;
    private TextView movieReleaseView;
    private TextView moviePlotView;
    private TextView movieTitleView;

    /*
     * Constants
     */

    private static final String NOT_AVAILABLE = "Not available";

    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Setting toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentThatStartedThisActivity = getIntent();

        //Assign the views that will be populated with the movie's data
        moviePosterView = (ImageView) findViewById(R.id.movie_details_poster_view);
        movieTitleView = (TextView) findViewById(R.id.movie_title_view);
        movieVoteAverageView = (TextView) findViewById(R.id.movie_details_vote_view);
        movieReleaseView = (TextView) findViewById(R.id.movie_details_release_view);
        moviePlotView = (TextView) findViewById(R.id.movie_details_plot_view);

        // Make plot view scrollable
        moviePlotView.setMovementMethod(new ScrollingMovementMethod());

        // Get movie from intent and populate views with data
        if (intentThatStartedThisActivity.hasExtra("movieObject")) {
            Movie movie = intentThatStartedThisActivity.getExtras().getParcelable("movieObject");
            fillMovieData(movie);
        }
    }

    /**
     * Updates the Details Activity UI by setting the text
     * and resources for the movie selected by the user
     *
     * @param movie The movie selected by the user
     */
    private void fillMovieData(Movie movie) {

        // Movie data
        String movieTitle = movie.getMovieTitle();
        String posterPath = movie.getMoviePosterPath();
        Double voteAverage = movie.getMovieVoteAverage();
        String releaseDate = extractReleaseYear(movie.getMovieReleaseDate());
        String moviePlot = movie.getMoviePlot();

        // Change activity label to be movie title
        setTitle(movieTitle);

        // Update views
        loadMoviePoster(posterPath);
        setViewData(movieVoteAverageView, voteAverage.toString());
        setViewData(movieReleaseView, releaseDate);
        setViewData(moviePlotView, moviePlot);
        setViewData(movieTitleView, movieTitle);
    }

    /**
     * Load Movie poster into the corresponding view
     * using the Picasso library.
     *
     * @param posterPath URL to fetch the movie poster
     */
    private void loadMoviePoster(String posterPath) {
        if(posterPath != null) {
            Picasso.with(this)
                    .load(posterPath)
                    .placeholder(R.drawable.placeholder)
                    .resize(200,300)
                    .error(R.drawable.movie_details_error)
                    .into(moviePosterView);
        }
    }

    /**
     * Extract the year the movie was released.
     *
     * @param releaseDate A String that represents
     *                    a date with the format YYYY-MM-DD
     * @return A four-digit year in String format
     */
    private String extractReleaseYear(String releaseDate) {
        return releaseDate.split("-")[0];
    }

    /**
     * Sets the view's text to be the value provided
     *
     * @param view A TextView
     * @param value A String that will be the text
     *              for the view provided
     */
    private void setViewData(TextView view, String value) {
        if(value != null) {
            view.setText(value);
        } else {
            view.setText(NOT_AVAILABLE);
        }
    }
}