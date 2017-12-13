package com.simao.isepmovies.controller;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.MovieDB;
import com.simao.isepmovies.R;
import com.simao.isepmovies.adapter.MovieDetailsSlideAdapter;
import com.simao.isepmovies.helper.ObservableScrollViewCallbacks;
import com.simao.isepmovies.helper.ScrollState;
import com.simao.isepmovies.helper.Scrollable;
import com.simao.isepmovies.model.CastModel;
import com.simao.isepmovies.model.SimilarModel;
import com.simao.isepmovies.view.MovieDetailsSlidingTabLayout;
import com.simao.isepmovies.view.ObservableParallaxScrollView;

public class MovieDetails extends Fragment implements ObservableScrollViewCallbacks {

    private MainActivity activity;
    private View rootView;
    private int currentId;
    private int timeOut;
    private HttpURLConnection conn;
    private String title;
    private Bundle save;

    private ProgressBar spinner;
    private int moreIconCheck;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private int homeIconCheck;
    private int galleryIconCheck;
    private CircledImageView galleryIcon;
    private int trailerIconCheck;
    private CircledImageView trailerIcon;
    private ArrayList<String> trailerList;
    private ArrayList<String> galleryList;

    private onGalleryIconClick onGalleryIconClick;
    private onTrailerIconClick onTrailerIconClick;
    private onMoreIconClick onMoreIconClick;
    private onHomeIconClick onHomeIconClick;

    private ArrayList<CastModel> castList;


    private MovieDetailsSlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private MovieDetailsSlideAdapter movieDetailsSlideAdapter;
    private onPageChangeSelected onPageChangeSelected;
    private TranslateAnimation downAnimation;
    private DownAnimationListener downAnimationListener;
    private TranslateAnimation upAnimation;
    private UpAnimationListener upAnimationListener;
    private TranslateAnimation iconUpAnimation;
    private IconUpAnimationListener iconUpAnimationListener;
    private TranslateAnimation iconDownAnimation;
    private IconDownAnimationListener iconDownAnimationListener;
    private MovieDetailsInfo movieDetailsInfo;
    private MovieDetailsCast movieDetailsCast;
    private MovieDetailsOverview movieDetailsOverview;
    private int movieDetailsInfoScrollY;
    private boolean addToBackStack;
    private String homeIconUrl;
    private float oldScrollY;
    private float dy;
    private float upDy;
    private float downDy;
    private float downDyTrans;
    private boolean upDyKey;
    private boolean downDyKey;
    private float scrollSpeed = 2.2F;
    private int currPos;
    private boolean infoTabScrollPosUpdated;
    private JSONAsyncTask request;
    private int iconMarginConstant;
    private int iconMarginLandscape;
    private int iconConstantSpecialCase;
    private int threeIcons;
    private int threeIconsToolbar;
    private int twoIcons;
    private int twoIconsToolbar;
    private int oneIcon;
    private int oneIconToolbar;
    private float scale;
    private boolean phone;
    private int hideThreshold;
    private int minThreshold;
    private int iconDirection;
    private boolean noCast;

    public MovieDetails() {
    }

    /**
        * Appelé à faire la création initiale d'un fragment.
        * Ceci est appelé après onAttach (Activité) et avant onCreateView (LayoutInflater, ViewGroup, Bundle).
        *
        * @param savedInstanceState Si le fragment est recréé à partir d'un état précédemment enregistré, c'est l'état.
        */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            save = savedInstanceState.getBundle("save");

    }

    /**
      * Appelé pour que le fragment instancie sa vue d'interface utilisateur.
      *
      * @param inflater définit la mise en page pour la vue actuelle.
      * @param container le conteneur qui contient la vue en cours.
      * @param savedInstanceState Si non-null, ce fragment est en train d'être reconstruit à partir d'un état précédemment enregistré comme indiqué ici.
      * Renvoie la vue pour l'interface utilisateur du fragment, ou null.
      */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        activity = ((MainActivity) getActivity());
        onGalleryIconClick = new onGalleryIconClick();
        onTrailerIconClick = new onTrailerIconClick();
        onMoreIconClick = new onMoreIconClick();
        onHomeIconClick = new onHomeIconClick();
        onPageChangeSelected = new onPageChangeSelected();
        downAnimationListener = new DownAnimationListener();
        upAnimationListener = new UpAnimationListener();
        iconUpAnimationListener = new IconUpAnimationListener();
        iconDownAnimationListener = new IconDownAnimationListener();
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        if (phone) {
            hideThreshold = (int) (-105 * scale);
            minThreshold = (int) (-49 * scale);
        } else {
            hideThreshold = (int) (-100 * scale);
            minThreshold = (int) (-42 * scale);
        }

        if (currentId != this.getArguments().getInt("id") || this.timeOut == 1) {
            rootView = inflater.inflate(R.layout.moviedetails, container, false);
            spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);

            homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
            homeIcon.bringToFront();
            homeIcon.setVisibility(View.INVISIBLE);
            galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
            galleryIcon.bringToFront();
            galleryIcon.setVisibility(View.INVISIBLE);
            trailerIcon = (CircledImageView) rootView.findViewById(R.id.trailerIcon);
            trailerIcon.bringToFront();
            trailerIcon.setVisibility(View.INVISIBLE);

            // L'indice Z le plus élevé doit être déclaré en dernier
            moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
            moreIcon.bringToFront();
        }
        moreIcon.setOnClickListener(onMoreIconClick);

        return rootView;
    }

    /**
       * Appelé immédiatement après onCreateView (LayoutInflater, ViewGroup, Bundle) est revenu,
       * mais avant que tout état enregistré ait été restauré dans la vue.
       *
       * @param view La vue renvoyée par onCreateView (LayoutInflater, ViewGroup, Bundle).
       * @param savedInstanceState Si non-null, ce fragment est en train d'être reconstruit à partir d'un état précédemment enregistré comme indiqué ici.
      */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity.getMovieDetailsBundle().size() > 0 && activity.getRestoreMovieDetailsState()) {
            save = activity.getMovieDetailsBundle().get(activity.getMovieDetailsBundle().size() - 1);
            activity.removeMovieDetailsBundle(activity.getMovieDetailsBundle().size() - 1);
            if (activity.getSearchViewCount())
                activity.decSearchMovieDetails();
            activity.setRestoreMovieDetailsState(false);
        }
        if (save != null && save.getInt("timeOut") == 1)
            activity.setRestoreMovieDetailsAdapterState(true);
        // Récupère le ViewPager et positionne le PagerAdapter pour qu'il puisse afficher les éléments
        movieDetailsSlideAdapter = new MovieDetailsSlideAdapter(getChildFragmentManager(), getResources(), activity);
        if (mViewPager != null)
            currPos = mViewPager.getCurrentItem();
        mViewPager = (ViewPager) rootView.findViewById(R.id.movieDetailsPager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(movieDetailsSlideAdapter);
        // Donne le SlidingTabLayout au ViewPager, ceci doit être fait APRÈS que le ViewPager ait eu
        // c'est l'ensemble PagerAdapter.
        mSlidingTabLayout = (MovieDetailsSlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(activity, R.color.tabSelected));
        mSlidingTabLayout.bringToFront();
    }

    /**
        * @param savedInstanceState si le fragment est en cours de recréation à partir d'un état précédemment enregistré, c'est l'état.
        */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (save != null) {
            setTitle(save.getString("title"));
            currentId = save.getInt("currentId");
            timeOut = save.getInt("timeOut");
            if (timeOut == 0) {
                spinner.setVisibility(View.GONE);
                onOrientationChange(save);
            }
        }

        if (currentId != this.getArguments().getInt("id") || this.timeOut == 1) {
            currentId = this.getArguments().getInt("id");
            moreIcon.setVisibility(View.INVISIBLE);
            mSlidingTabLayout.setVisibility(View.INVISIBLE);
            mViewPager.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);

            request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        request.execute(MovieDB.url + "movie/" + currentId + "?append_to_response=releases%2Ctrailers%2Ccasts%2Cimages%2Csimilar&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException | CancellationException e) {
                        request.cancel(true);
                        // nous abandonnons la requête http, sinon cela causera des problèmes et ralentira la connexion plus tard
                        if (conn != null)
                            conn.disconnect();
                        if (spinner != null)
                            activity.hideView(spinner);
                        if (mViewPager != null)
                            activity.hideLayout(mViewPager);
                        if (getActivity() != null && !(e instanceof CancellationException)) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        setTimeOut(1);
                    }
                }
            }).start();
        }
        activity.setTitle(getTitle());
        activity.setMovieDetailsFragment(this);
        if (activity.getSaveInMovieDetailsSimFragment()) {
            activity.setSaveInMovieDetailsSimFragment(false);
            activity.setMovieDetailsSimFragment(this);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                movieDetailsInfo = (MovieDetailsInfo) movieDetailsSlideAdapter.getRegisteredFragment(0);
                movieDetailsCast = (MovieDetailsCast) movieDetailsSlideAdapter.getRegisteredFragment(1);
                movieDetailsOverview = (MovieDetailsOverview) movieDetailsSlideAdapter.getRegisteredFragment(2);
            }
        });

        showInstantToolbar();

        iconMarginConstant = activity.getIconMarginConstant();
        iconMarginLandscape = activity.getIconMarginLandscape();
        iconConstantSpecialCase = activity.getIconConstantSpecialCase();
        threeIcons = activity.getThreeIcons();
        threeIconsToolbar = activity.getThreeIconsToolbar();
        twoIcons = activity.getTwoIcons();
        twoIconsToolbar = activity.getTwoIconsToolbar();
        oneIcon = activity.getOneIcon();
        oneIconToolbar = activity.getOneIconToolbar();

        /*Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("MovieDetails - " + getTitle());
        t.send(new HitBuilders.ScreenViewBuilder().build());*/
    }

    /**
      * Cette classe gère la connexion à notre serveur principal.
      * Si la connexion est réussie, nous donnons des informations sur nos vues.
      */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(10000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int status = conn.getResponseCode();

                if (status == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();

                    JSONObject jsonData = new JSONObject(sb.toString());

                    // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
                    while (movieDetailsInfo == null) {
                        Thread.sleep(200);
                    }
                    if (isAdded() && movieDetailsInfo != null) {
                        // Chemin de Backdrop
                        if (!jsonData.getString("backdrop_path").equals("null") && !jsonData.getString("backdrop_path").isEmpty()) {
                            activity.setBackDropImage(movieDetailsInfo.getBackDropPath(), jsonData.getString("backdrop_path"));
                            activity.setImageTag(movieDetailsInfo.getBackDropPath(), jsonData.getString("backdrop_path"));
                        } else if (!jsonData.getString("poster_path").equals("null") && !jsonData.getString("poster_path").isEmpty()) {
                            activity.setBackDropImage(movieDetailsInfo.getBackDropPath(), jsonData.getString("poster_path"));
                            activity.setImageTag(movieDetailsInfo.getBackDropPath(), jsonData.getString("poster_path"));
                        } else
                            movieDetailsInfo.setBackDropCheck(1);

                        // Title
                        activity.setText(movieDetailsInfo.getTitleText(), jsonData.getString("title"));

                        // Release date
                        if (!jsonData.getString("release_date").equals("null") && !jsonData.getString("release_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("release_date"));
                                String formattedDate = activity.getDateFormat().format(date);
                                activity.setText(movieDetailsInfo.getReleaseDate(), "(" + formattedDate + ")");
                            } catch (java.text.ParseException e) {
                                activity.hideTextView(movieDetailsInfo.getReleaseDate());
                            }
                        } else
                            activity.hideTextView(movieDetailsInfo.getReleaseDate());

                        // Chemin de l'affiche
                        if (!jsonData.getString("poster_path").equals("null") && !jsonData.getString("poster_path").isEmpty()) {
                            activity.setImage(movieDetailsInfo.getPosterPath(), jsonData.getString("poster_path"));
                            activity.setImageTag(movieDetailsInfo.getPosterPath(), jsonData.getString("poster_path"));
                        }

                        // Status
                        if (!jsonData.getString("status").equals("null") && !jsonData.getString("status").isEmpty())
                            activity.setText(movieDetailsInfo.getStatusText(), jsonData.getString("status"));
                        else
                            activity.hideTextView(movieDetailsInfo.getStatusText());

                        // Tag line
                        if (!jsonData.getString("tagline").equals("null") && !jsonData.getString("tagline").isEmpty())
                            activity.setText(movieDetailsInfo.getTagline(), "\"" + jsonData.getString("tagline") + "\"");
                        else {
                            activity.hideTextView(movieDetailsInfo.getTagline());
                        }

                        // RunTime
                        try {
                            if (Integer.parseInt(jsonData.getString("runtime")) != 0)
                                activity.setText(movieDetailsInfo.getRuntime(), jsonData.getString("runtime") + " " + getString(R.string.min));
                            else {
                                activity.hideTextView(movieDetailsInfo.getRuntime());
                            }
                        } catch (NumberFormatException e) {
                            activity.hideTextView(movieDetailsInfo.getRuntime());
                        } catch (java.lang.IllegalStateException e1) {

                        }


                        // Genres
                        JSONArray genresArray = jsonData.getJSONArray("genres");
                        String genresData = "";
                        for (int i = 0; i < genresArray.length(); i++) {
                            if (i + 1 == genresArray.length())
                                genresData += genresArray.getJSONObject(i).get("name");
                            else
                                genresData += genresArray.getJSONObject(i).get("name") + ", ";
                        }

                        if (genresData.isEmpty())
                            activity.hideTextView(movieDetailsInfo.getGenres());
                        else {
                            activity.setText(movieDetailsInfo.getGenres(), genresData);
                        }

                        // Pays de production
                        JSONArray countriesArray = jsonData.getJSONArray("production_countries");
                        String countriesData = "";
                        for (int i = 0; i < countriesArray.length(); i++) {
                            if (i + 1 == countriesArray.length())
                                countriesData += countriesArray.getJSONObject(i).get("name");
                            else
                                countriesData += countriesArray.getJSONObject(i).get("name") + "\n";
                        }

                        if (countriesData.isEmpty())
                            activity.hideTextView(movieDetailsInfo.getCountries());
                        else {
                            activity.setText(movieDetailsInfo.getCountries(), countriesData);
                        }

                        // Les sociétés de production
                        JSONArray companiesArray = jsonData.getJSONArray("production_companies");
                        String companiesData = "";
                        for (int i = 0; i < companiesArray.length(); i++) {
                            if (i + 1 == companiesArray.length())
                                companiesData += companiesArray.getJSONObject(i).get("name");
                            else
                                companiesData += companiesArray.getJSONObject(i).get("name") + "\n";
                        }

                        if (companiesData.isEmpty())
                            activity.hideTextView(movieDetailsInfo.getCompanies());
                        else {
                            activity.setText(movieDetailsInfo.getCompanies(), companiesData);
                            // si les pays sont vides, nous devons définir la marge sur les entreprises
                            if (countriesData.isEmpty()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) movieDetailsInfo.getCompanies().getLayoutParams();
                                        lp.setMargins(0, (int) (28 * scale), 0, 0);
                                    }
                                });
                            }
                        }

                        // Rating
                        if (Float.parseFloat(jsonData.getString("vote_average")) == 0.0f) {
                            activity.hideRatingBar(movieDetailsInfo.getRatingBar());
                            activity.hideTextView(movieDetailsInfo.getVoteCount());
                        } else {
                            activity.setRatingBarValue(movieDetailsInfo.getRatingBar(), (Float.parseFloat(jsonData.getString("vote_average")) / 2));
                            activity.setText(movieDetailsInfo.getVoteCount(), jsonData.getString("vote_count") + " " + getString(R.string.voteCount));
                        }


                        // Homepage icon
                        if (!jsonData.getString("homepage").isEmpty() && !jsonData.getString("homepage").equals("null")) {
                            homeIconUrl = jsonData.getString("homepage");
                            homeIconCheck = 0;
                        } else {
                            activity.invisibleView(homeIcon);
                            homeIconCheck = 1;
                        }

                        // Trailers
                        JSONObject trailerObject = jsonData.getJSONObject("trailers");
                        JSONArray trailerArray = trailerObject.getJSONArray("youtube");
                        trailerList = new ArrayList<>();
                        if (trailerArray.length() > 0) {
                            for (int i = 0; i < trailerArray.length(); i++) {
                                JSONObject object = trailerArray.getJSONObject(i);
                                trailerList.add(object.getString("source"));
                            }
                            trailerIconCheck = 0;
                        } else {
                            activity.invisibleView(trailerIcon);
                            trailerIconCheck = 1;
                        }

                        // Gallery
                        JSONObject galleryObject = jsonData.getJSONObject("images");
                        JSONArray galleryBackdropsArray = galleryObject.getJSONArray("backdrops");
                        JSONArray galleryPostersArray = galleryObject.getJSONArray("posters");
                        galleryList = new ArrayList<>();
                        if (galleryPostersArray.length() > 0 || galleryBackdropsArray.length() > 0) {
                            for (int i = 0; i < galleryBackdropsArray.length(); i++) {
                                JSONObject object = galleryBackdropsArray.getJSONObject(i);
                                galleryList.add(object.getString("file_path"));
                            }
                            for (int i = 0; i < galleryPostersArray.length(); i++) {
                                JSONObject object = galleryPostersArray.getJSONObject(i);
                                galleryList.add(object.getString("file_path"));
                            }
                            galleryIconCheck = 0;
                        } else {
                            activity.invisibleView(galleryIcon);
                            galleryIconCheck = 1;
                        }

                        //Cast info
                        JSONObject casts = jsonData.getJSONObject("casts");
                        JSONArray castsArray = casts.getJSONArray("cast");
                        castList = new ArrayList<>();
                        for (int i = 0; i < castsArray.length(); i++) {
                            JSONObject object = castsArray.getJSONObject(i);

                            CastModel cast = new CastModel();
                            cast.setId(object.getInt("id"));
                            cast.setName(object.getString("name"));
                            cast.setCharacter(object.getString("character"));
                            if (!object.getString("profile_path").equals("null") && !object.getString("profile_path").isEmpty())
                                cast.setProfilePath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("profile_path"));

                            castList.add(cast);
                        }


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (castList.size() == 0) {
                                    noCast = true;
                                    mSlidingTabLayout.disableTabClickListener(1);
                                }

                                // Cast
                                if (isAdded())
                                    movieDetailsCast.setAdapter(castList);
                            }
                        });


                        //Aperçu
                        final String overview = jsonData.getString("overview");

                        if (!overview.equals("null") && !overview.isEmpty())
                            activity.setText(movieDetailsOverview.getOverview(), overview);
                        else
                            activity.setText(movieDetailsOverview.getOverview(), getResources().getString(R.string.noOverview));


                        // Similar
                        JSONObject similarObj = jsonData.getJSONObject("similar");
                        JSONArray similarArray = similarObj.getJSONArray("results");
                        int similarLen = similarArray.length();
                        if (similarLen > 6)
                            similarLen = 6;

                        if (similarLen == 0)
                            activity.hideView(movieDetailsInfo.getSimilarHolder());
                        else {
                            final ArrayList<SimilarModel> similarList = new ArrayList<>();

                            for (int i = 0; i < similarLen; i++) {
                                JSONObject object = similarArray.getJSONObject(i);

                                SimilarModel similarModel = new SimilarModel();
                                similarModel.setId(object.getInt("id"));
                                similarModel.setTitle(object.getString("title"));
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    similarModel.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                                similarList.add(similarModel);
                                if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                    similarModel.setReleaseDate(object.getString("release_date"));
                            }

                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (isAdded())
                                        movieDetailsInfo.setSimilarList(similarList);
                                }
                            });
                        }


                        return true;
                    }
                }


            } catch (ParseException | IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } catch (InterruptedException e) {

            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        /**
          * Lancé après la fin de doInBackground ()
          *
          * @param renvoie true si la connexion est réussie, false si la connexion a échoué.
          */

        protected void onPostExecute(Boolean result) {
            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
            if (isAdded()) {
                if (!result) {
                    Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    setTimeOut(1);
                    spinner.setVisibility(View.GONE);
                    mViewPager.setVisibility(View.GONE);
                } else {
                    setTimeOut(0);
                    currPos = 0;
                    mViewPager.setCurrentItem(0);
                    spinner.setVisibility(View.GONE);
                    mSlidingTabLayout.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.VISIBLE);
                    mSlidingTabLayout.setOnPageChangeListener(onPageChangeSelected);
                    if (homeIconCheck == 1 && galleryIconCheck == 1 && trailerIconCheck == 1) {
                        moreIconCheck = 1;
                        activity.hideView(moreIcon);
                        activity.hideView(movieDetailsInfo.getMoreIcon());
                    } else {
                        moreIconCheck = 0;
                        activity.showView(movieDetailsInfo.getMoreIcon());
                        // définir l'écouteur en arrière-plan cliquez pour ouvrir la galerie
                        if (galleryIconCheck == 0) {
                            movieDetailsInfo.getBackDropPath().setOnClickListener(onGalleryIconClick);
                            movieDetailsInfo.getPosterPath().setOnClickListener(onGalleryIconClick);
                        }
                        adjustIconsPos(homeIcon, trailerIcon, galleryIcon);
                        adjustIconsPos(movieDetailsInfo.getHomeIcon(), movieDetailsInfo.getTrailerIcon(), movieDetailsInfo.getGalleryIcon());
                    }

                }
            } else setTimeOut(1);


        }
    }

    /**
      * Nous utilisons cette clé pour savoir si l'utilisateur a essayé d'ouvrir ce film et que la connexion a échoué.
      * Donc, s'il essaie de charger à nouveau le même film, nous savons que la connexion a échoué et nous devons faire une nouvelle demande.
      */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
       * Met à jour la valeur timeOut.
       */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Mettre à jour le titre. Nous utilisons cette méthode pour enregistrer notre titre et ensuite le définir dans la barre d'outils.
     */
    public void setTitle(String title) {
        this.title = title;
    }


    private String getTitle() {
        return this.title;
    }


    /**
        * Appelé pour demander au fragment de sauvegarder son état dynamique actuel,
        * afin qu'il puisse être reconstruit plus tard dans une nouvelle instance de son processus est redémarré.
        *
        * @param outState Bundle dans lequel placer votre état enregistré.
        */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Utilisé pour éviter les bugs lorsque nous ajoutons un élément dans la pile arrière
        // et si nous changeons d'orientation deux fois l'élément de la pile arrière a des valeurs nulles
        if (save != null && save.getInt("timeOut") == 1)
            save = null;

        if (save != null) {
            if (movieDetailsCast != null)
                save.putInt("lastVisitedPerson", movieDetailsCast.getLastVisitedPerson());
            outState.putBundle("save", save);
            if (addToBackStack) {
                activity.addMovieDetailsBundle(save);
                addToBackStack = false;
            }
        } else {
            Bundle send = new Bundle();
            send.putInt("currentId", currentId);
            if (request != null && request.getStatus() == AsyncTask.Status.RUNNING) {
                timeOut = 1;
                request.cancel(true);
            }
            send.putInt("timeOut", timeOut);
            send.putString("title", title);
            if (timeOut == 0) {
                // HomePage
                send.putInt("homeIconCheck", homeIconCheck);
                if (homeIconCheck == 0)
                    send.putString("homepage", homeIconUrl);

                // Gallery icon
                send.putInt("galleryIconCheck", galleryIconCheck);
                if (galleryIconCheck == 0)
                    send.putStringArrayList("galleryList", galleryList);

                // Trailer icon
                send.putInt("trailerIconCheck", trailerIconCheck);
                if (trailerIconCheck == 0)
                    send.putStringArrayList("trailerList", trailerList);

                // More icon
                send.putInt("moreIconCheck", moreIconCheck);

                // L'information sur les détails du film commence ici
                if (movieDetailsInfo != null) {
                    // Backdrop path
                    send.putInt("backDropCheck", movieDetailsInfo.getBackDropCheck());
                    if (movieDetailsInfo.getBackDropCheck() == 0 && movieDetailsInfo.getBackDropPath().getTag() != null)
                        send.putString("backDropUrl", movieDetailsInfo.getBackDropPath().getTag().toString());

                    // Poster path url
                    if (movieDetailsInfo.getPosterPath().getTag() != null)
                        send.putString("posterPathURL", movieDetailsInfo.getPosterPath().getTag().toString());

                    // Rating
                    send.putFloat("rating", movieDetailsInfo.getRatingBar().getRating());
                    send.putString("voteCount", movieDetailsInfo.getVoteCount().getText().toString());

                    // Title
                    send.putString("titleText", movieDetailsInfo.getTitleText().getText().toString());

                    // Release date
                    send.putString("releaseDate", movieDetailsInfo.getReleaseDate().getText().toString());

                    // Status
                    send.putString("status", movieDetailsInfo.getStatusText().getText().toString());

                    // Tag line
                    send.putString("tagline", movieDetailsInfo.getTagline().getText().toString());

                    // RunTime
                    send.putString("runTime", movieDetailsInfo.getRuntime().getText().toString());

                    // Genres
                    send.putString("genres", movieDetailsInfo.getGenres().getText().toString());

                    // Production countries
                    send.putString("productionCountries", movieDetailsInfo.getCountries().getText().toString());

                    // Production companies
                    send.putString("productionCompanies", movieDetailsInfo.getCompanies().getText().toString());

                    // Similar list
                    if (movieDetailsInfo.getSimilarList() != null && movieDetailsInfo.getSimilarList().size() > 0)
                        send.putParcelableArrayList("similarList", movieDetailsInfo.getSimilarList());
                }
                // fin


                // Détails du film cast commence ici
                if (movieDetailsCast != null) {
                    send.putParcelableArrayList("castList", castList);
                    send.putInt("lastVisitedPerson", movieDetailsCast.getLastVisitedPerson());
                }
                // fin

                // Aperçu
                if (movieDetailsOverview != null)
                    send.putString("overview", movieDetailsOverview.getOverview().getText().toString());


            }


            outState.putBundle("save", send);
            save = send;
            if (addToBackStack) {
                activity.addMovieDetailsBundle(send);
                addToBackStack = false;
            }
        }


    }

    /**
       * Lancé lorsque la restauration à partir de backState ou de l'orientation a changé.
       *
       * @param args notre paquet avec l'état sauvé.
       */
    private void onOrientationChange(Bundle args) {
        // Home page
        homeIconCheck = args.getInt("homeIconCheck");
        if (homeIconCheck == 0)
            homeIconUrl = args.getString("homepage");


        // Gallery
        galleryIconCheck = args.getInt("galleryIconCheck");
        if (galleryIconCheck == 0) {
            galleryList = new ArrayList<>();
            galleryList = args.getStringArrayList("galleryList");
            if (galleryList.size() == 0)
                activity.hideView(galleryIcon);
        }

        // Trailers
        trailerIconCheck = args.getInt("trailerIconCheck");
        if (trailerIconCheck == 0) {
            trailerList = new ArrayList<>();
            trailerList = args.getStringArrayList("trailerList");
            if (trailerList.size() == 0)
                activity.hideView(trailerIcon);
        }

        // More icon
        moreIconCheck = args.getInt("moreIconCheck");

        if (homeIconCheck == 1 && galleryIconCheck == 1 && trailerIconCheck == 1) {
            moreIconCheck = 1;
            moreIcon.setVisibility(View.GONE);
        } else moreIconCheck = 0;

        mSlidingTabLayout.setOnPageChangeListener(onPageChangeSelected);
        activity.setMovieDetailsInfoBundle(save);
        activity.setMovieDetailsCastBundle(save);
        activity.setMovieDetailsOverviewBundle(save);

        castList = save.getParcelableArrayList("castList");
        if (castList != null && castList.size() == 0) {
            noCast = true;
            mSlidingTabLayout.disableTabClickListener(1);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                movieDetailsInfo = (MovieDetailsInfo) movieDetailsSlideAdapter.getRegisteredFragment(0);
                if (currPos == 0) {
                    moreIcon.setVisibility(View.INVISIBLE);
                } else if (moreIconCheck == 0) {
                    movieDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
                    updateDownPos();
                }
                if (moreIconCheck == 1)
                    movieDetailsInfo.getMoreIcon().setVisibility(View.GONE);
                else {
                    // Définir l'écouteur sur la toile de fond et le chemin de l'affiche cliquer pour ouvrir la galerie
                    if (galleryIconCheck == 0 && galleryList.size() > 0) {
                        movieDetailsInfo.getBackDropPath().setOnClickListener(onGalleryIconClick);
                        movieDetailsInfo.getPosterPath().setOnClickListener(onGalleryIconClick);
                    }
                    adjustIconsPos(homeIcon, trailerIcon, galleryIcon);
                    adjustIconsPos(movieDetailsInfo.getHomeIcon(), movieDetailsInfo.getTrailerIcon(), movieDetailsInfo.getGalleryIcon());
                }

                // Désactiver le changement d'orientation, activer le glissement du tiroir de navigation, afficher la barre d'outils
                if (galleryIconCheck == 0 && galleryList.size() == 1) {
                    activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, R.color.background_material_light));
                    if (activity.getSupportActionBar() != null)
                        activity.getSupportActionBar().show();
                    activity.getMDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if (Build.VERSION.SDK_INT >= 19)
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    // Vérifie l'orientation et verrouille le portrait si nous sommes au téléphone
                    if (getResources().getBoolean(R.bool.portrait_only))
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

            }
        });


    }

    /**
     * Class which listens when the user has tapped on More icon button.
     */
    public class onMoreIconClick implements View.OnClickListener {
        private boolean key;
        private boolean toolbarHidden;
        private int items;
        private View toolbarView = activity.findViewById(R.id.toolbar);
        private int currScroll;

        public onMoreIconClick() {
            // conserve des références pour votre logique onClick
        }

        public boolean getKey() {
            return key;
        }

        public void setKey(boolean key) {
            this.key = key;
        }

        @Override
        public void onClick(View v) {
            items = homeIconCheck + galleryIconCheck + trailerIconCheck;
            toolbarHidden = toolbarView.getTranslationY() == -toolbarView.getHeight();
            currScroll = movieDetailsInfo.getRootView().getScrollY();
            if (!key) {
                iconDirection = 1;
                if (currPos == 0) {
                    // 3 icons
                    if (items == 0) {
                        if (toolbarHidden && currScroll / scale > threeIcons) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > threeIconsToolbar) {
                            iconDirection = -1;
                        }
                    }
                    // 2 icons
                    if (items == 1) {
                        if (toolbarHidden && currScroll / scale > twoIcons) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > twoIconsToolbar) {
                            iconDirection = -1;
                        }
                    }
                    // 1 icon
                    if (items == 2) {
                        if (toolbarHidden && currScroll / scale > oneIcon) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > oneIconToolbar) {
                            iconDirection = -1;
                        }
                    }
                }
                if (currPos == 0) {
                    movieDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, movieDetailsInfo.getHomeIcon(), movieDetailsInfo.getTrailerIcon(), movieDetailsInfo.getGalleryIcon());

                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, homeIcon, trailerIcon, galleryIcon);

                }
                key = true;
            } else {
                if (currPos == 0) {
                    movieDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, movieDetailsInfo.getHomeIcon(), movieDetailsInfo.getTrailerIcon(), movieDetailsInfo.getGalleryIcon());
                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, homeIcon, trailerIcon, galleryIcon);
                }
                key = false;
            }
        }
    }

    /**
       * Classe qui écoute lorsque l'utilisateur a appuyé sur le bouton d'icône Accueil.
       */
    public class onHomeIconClick implements View.OnClickListener {
        public onHomeIconClick() {
            // conserve des références pour votre logique onClick
        }


        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(homeIcon.getTag().toString()));
            startActivity(i);
        }
    }

    public class onGalleryIconClick implements View.OnClickListener {
        public onGalleryIconClick() {
            // conserve des références pour votre logique onClick
        }


        @Override
        public void onClick(View v) {
            if (activity.getSearchViewCount())
                activity.incSearchMovieDetails();
            if (galleryList.size() == 1) {
                setAddToBackStack(true);
                onSaveInstanceState(new Bundle());
                if (activity.getSupportActionBar() != null)
                    activity.getSupportActionBar().hide();
                activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, R.color.black));
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.frame_container, GalleryPreviewDetail.newInstance(MovieDB.imageUrl + getResources().getString(R.string.galleryPreviewImgSize) + galleryList.get(0)));
                // ajoute la transaction en cours à la pile arrière:
                transaction.addToBackStack("movieDetails");
                transaction.commit();
            } else {
                try {
                    setAddToBackStack(true);
                    onSaveInstanceState(new Bundle());
                    showInstantToolbar();
                    activity.getGalleryListView().setTitle(getTitle());
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    Bundle args = new Bundle();
                    args.putStringArrayList("galleryList", galleryList);
                    activity.getGalleryListView().setArguments(args);
                    transaction.replace(R.id.frame_container, activity.getGalleryListView());
                    // ajoute la transaction en cours à la pile arrière:
                    transaction.addToBackStack("movieDetails");
                    transaction.commit();
                } catch (java.lang.IllegalStateException e) {
                    GalleryList galleryListView = new GalleryList();
                    galleryListView.setTitle(getTitle());
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    Bundle args = new Bundle();
                    args.putStringArrayList("galleryList", galleryList);
                    galleryListView.setArguments(args);
                    transaction.replace(R.id.frame_container, galleryListView);
                    // ajoute la transaction en cours à la pile arrière:
                    transaction.addToBackStack("movieDetails");
                    transaction.commit();
                }
            }
        }
    }

    /**
       * Classe qui écoute lorsque l'utilisateur a appuyé sur le bouton d'icône Trailer.
       */
    public class onTrailerIconClick implements View.OnClickListener {
        public onTrailerIconClick() {
            // conserve des références pour votre logique onClick
        }


        @Override
        public void onClick(View v) {
            if (trailerList.size() == 1)
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MovieDB.youtube + trailerList.get(0))));
            else {
                if (activity.getSearchViewCount())
                    activity.incSearchMovieDetails();
                setAddToBackStack(true);
                onSaveInstanceState(new Bundle());
                showInstantToolbar();
                activity.getTrailerListView().setTitle(getTitle());
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Bundle args = new Bundle();
                args.putStringArrayList("trailerList", trailerList);
                activity.getTrailerListView().setArguments(args);
                transaction.replace(R.id.frame_container, activity.getTrailerListView());
                // add the current transaction to the back stack:
                transaction.addToBackStack("movieDetails");
                transaction.commit();
            }
        }
    }

    /**
       * Classe qui écoute lorsque l'utilisateur a changé le robinet dans les détails de Cast
       */
    public class onPageChangeSelected implements ViewPager.OnPageChangeListener {
        private boolean toolbarHidden;
        private View toolbarView = activity.findViewById(R.id.toolbar);

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }


        @Override
        public void onPageSelected(int position) {
            if (noCast) {
                if (position == 1 && currPos == 2) {
                    mViewPager.setCurrentItem(0);
                    return;
                }
                if (position == 1 && currPos == 0) {
                    mViewPager.setCurrentItem(2);
                    return;
                }
            }
            if (toolbarView != null)
                toolbarHidden = toolbarView.getTranslationY() == -toolbarView.getHeight();

            if (position == 0) {
                scrollSpeed = 2.2F;
                if (movieDetailsInfo != null) {
                    if (movieDetailsInfo.canScroll()) {
                        if (toolbarHidden) {
                            final ObservableParallaxScrollView scrollView = movieDetailsInfo.getScrollView();
                            if (scrollView.getCurrentScrollY() / scale < 119) {
                                infoTabScrollPosUpdated = true;
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.scrollTo(0, (int) (119 * scale));
                                    }
                                });
                            }
                        }
                    } else
                        showInstantToolbar();
                }

            } else scrollSpeed = 1;


            if (position == 1 && movieDetailsCast != null) {
                if (movieDetailsCast.getCastList() != null && movieDetailsCast.getCastList().size() > 0) {
                    final AbsListView listView = movieDetailsCast.getListView();
                    if (movieDetailsCast.canScroll()) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (toolbarHidden && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                    if (phone)
                                        listView.smoothScrollBy((int) (56 * scale), 0);
                                    else
                                        listView.smoothScrollBy((int) (65 * scale), 0);
                                }
                            }
                        });
                    } else {
                        if (toolbarHidden)
                            showInstantToolbar();
                    }
                } else {
                    if (toolbarHidden)
                        showInstantToolbar();
                }
            }


            if (position == 2 && movieDetailsOverview != null) {
                movieDetailsOverview.getScrollView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (toolbarHidden)
                            movieDetailsOverview.getScrollView().scrollTo(0, (int) (56 * scale));
                        else
                            movieDetailsOverview.getScrollView().scrollTo(0, 0);
                    }
                });
            }

            if (moreIconCheck == 0) {
                if (movieDetailsInfo != null) {
                    movieDetailsInfoScrollY = movieDetailsInfo.getRootView().getScrollY();

                    galleryIcon.clearAnimation();
                    trailerIcon.clearAnimation();
                    homeIcon.clearAnimation();

                    homeIcon.setVisibility(View.INVISIBLE);
                    movieDetailsInfo.getHomeIcon().setVisibility(View.INVISIBLE);

                    galleryIcon.setVisibility(View.INVISIBLE);
                    movieDetailsInfo.getGalleryIcon().setVisibility(View.INVISIBLE);

                    trailerIcon.setVisibility(View.INVISIBLE);
                    movieDetailsInfo.getTrailerIcon().setVisibility(View.INVISIBLE);

                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));

                    if (position == 0) {
                        movieDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                        moreIcon.setVisibility(View.INVISIBLE);
                    } else {
                        movieDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
                    }

                    onMoreIconClick.setKey(false);

                    if (currPos == 0 && position == 1) {
                        updateUpPos();
                        createDownAnimation();
                        moreIcon.startAnimation(downAnimation);
                    }
                    if (currPos == 0 && position == 2) {
                        updateUpPos();
                        createDownAnimation();
                        moreIcon.startAnimation(downAnimation);
                    }
                    if (currPos == 1 && position == 0) {
                        updateDownPos();
                        // nous avons un cas spécial ici si cela est vrai, cela signifie que nous avons été dans l'onglet cast
                        // nous avons caché la barre d'outils et nous revenons à l'onglet d'information où le scrollY était 0
                        // donc plus tôt dans cette fonction nous avons mis à jour la valeur scrollY et maintenant nous devons mettre à jour le
                        // valeur d'animation sinon l'icône va "sauter"
                        if (infoTabScrollPosUpdated) {
                            infoTabScrollPosUpdated = false;
                            createUpAnimation((119 * scale) - movieDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                    if (currPos == 2 && position == 0) {
                        updateDownPos();
                        if (infoTabScrollPosUpdated) {
                            infoTabScrollPosUpdated = false;
                            createUpAnimation((119 * scale) - movieDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                }
            }


            if (!noCast || position != 1)
                currPos = position;


        }


    }

    /**
       * Écouteur qui met à jour la position des icônes après la fin de l'animation.
       */
    private class DownAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            moreIcon.clearAnimation();
            updateDownPos();
            moreIcon.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) moreIcon.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            moreIcon.setLayoutParams(layoutParams);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
      * Met à jour la position des icônes lors de l'appel.
      */
    public void updateDownPos() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(moreIcon.getWidth(), moreIcon.getHeight());
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(trailerIcon.getWidth(), trailerIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp.setMargins(0, (int) (scale * (496 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 15 + 0.5f), 0);
        moreIcon.setLayoutParams(lp);
        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        trailerIcon.setLayoutParams(lp2);
        lp3.setMargins(0, (int) (scale * (328.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

    /**
       * Écouteur qui met à jour la position des icônes après la fin de l'animation.
       */
    private class UpAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            movieDetailsInfo.getMoreIcon().clearAnimation();
            movieDetailsInfo.getMoreIcon().setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
       * Met à jour la position des icônes lors de l'appel.
       */
    public void updateUpPos() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(moreIcon.getWidth(), moreIcon.getHeight());
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(trailerIcon.getWidth(), trailerIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp.setMargins(0, (int) (scale * (346 + iconMarginConstant) + 0.5f - movieDetailsInfoScrollY), (int) (scale * 15 + 0.5f), 0);
        moreIcon.setLayoutParams(lp);
    }

    /**
       * Crée une animation pour l'icône Plus avec la direction vers le bas.
       */
    public void createDownAnimation() {
        // si c'est 300 cela signifie que nous sommes sur tablette et l'orientation est paysage
        // nous faisons cela sinon l'animation va sortir de l'écran
        // donc si nous sommes sur le paysage, nous changeons la direction de l'animation
        if (iconMarginLandscape == 300)
            downAnimation = new TranslateAnimation(0, 0, 0, -scale * 150 + 0.5f + movieDetailsInfoScrollY);
        else
            downAnimation = new TranslateAnimation(0, 0, 0, scale * (150 + iconConstantSpecialCase) + 0.5f + movieDetailsInfoScrollY);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(false);
        downAnimation.setAnimationListener(downAnimationListener);
    }

    /**
       * Crée une animation pour l'icône Plus avec la direction vers le haut.
       */
    public void createUpAnimation(float dy) {
        // si c'est 300 cela signifie que nous sommes sur tablette et l'orientation est paysage
        // nous faisons cela sinon l'animation va sortir de l'écran
        // donc si nous sommes sur le paysage, nous changeons la direction de l'animation
        if (iconMarginLandscape == 300)
            upAnimation = new TranslateAnimation(0, 0, 0, scale * 150 + 0.5f - movieDetailsInfoScrollY - dy);
        else
            upAnimation = new TranslateAnimation(0, 0, 0, -scale * (150 + iconConstantSpecialCase) + 0.5f - movieDetailsInfoScrollY - dy);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(false);
        upAnimation.setAnimationListener(upAnimationListener);
    }

    public MovieDetails.onMoreIconClick getOnMoreIconClick() {
        return onMoreIconClick;
    }

    public Bundle getSave() {
        return save;
    }

    public void setSave(Bundle save) {
        this.save = save;
    }

    public void setAddToBackStack(boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
    }

    /**
      * Cette méthode calcule quelles icônes avons-nous?
      *
      * @param homeIcon la première icône
      * @param trailerIcon la deuxième icône
      * @param galleryIcon la troisième icône
      */
    public void adjustIconsPos(CircledImageView homeIcon, CircledImageView trailerIcon, CircledImageView galleryIcon) {
        int iconCount[] = {homeIconCheck, trailerIconCheck, galleryIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(homeIcon);
        circledImageViews.add(trailerIcon);
        circledImageViews.add(galleryIcon);

        for (int i = 0; i < iconCount.length; i++) {
            if (iconCount[i] == 1)
                circledImageViews.get(circledImageViews.size() - 1).setVisibility(View.INVISIBLE);
            else {
                CircledImageView temp = circledImageViews.get(0);
                switch (i) {
                    case 0:
                        temp.setOnClickListener(onHomeIconClick);
                        temp.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_home_white_24dp));
                        temp.setTag(homeIconUrl);
                        break;
                    case 1:
                        temp.setOnClickListener(onTrailerIconClick);
                        temp.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_videocam_white_24dp));
                        break;
                    case 2:
                        temp.setOnClickListener(onGalleryIconClick);
                        temp.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_photo_camera_white_24dp));
                        break;

                }
                circledImageViews.remove(0);
            }

        }

    }

    /**
      * Lancé à partir de l'icône Plus cliquez sur les auditeurs. Met à jour la visibilité de la galerie et de l'icône homePage.
      * Et crée une animation pour eux aussi.
      *
      * @param visibility visibilité valeur
      * @param homeIcon première icône
      * @param galleryIcon deuxième icône
      */
    public void showHideImages(int visibility, CircledImageView homeIcon, CircledImageView trailerIcon, CircledImageView galleryIcon) {
        float dy[] = {0.7f, 56.7f, 112.5f};
        float infoTabDy[] = {-2.4f, 53.5f, 109.25f};
        int currDy = 0;
        int delay = 100;
        int iconCount[] = {homeIconCheck, trailerIconCheck, galleryIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(homeIcon);
        circledImageViews.add(trailerIcon);
        circledImageViews.add(galleryIcon);

        if (visibility == View.VISIBLE) {
            if (currPos != 0)
                updateIconDownPos();
            else
                updateIconDownPosInInfoTab();
        } else {
            if (currPos != 0)
                updateIconUpPos();
            else
                updateIconUpPosInInfoTab();
        }

        for (int i = 0; i < iconCount.length; i++) {
            if (iconCount[i] == 1)
                circledImageViews.get(circledImageViews.size() - 1).setVisibility(View.INVISIBLE);
            else {
                CircledImageView temp = circledImageViews.get(0);
                if (visibility == View.VISIBLE) {
                    if (currPos == 0)
                        createIconUpAnimation(infoTabDy[currDy], delay);
                    else
                        createIconUpAnimation(dy[currDy], delay);
                    temp.startAnimation(iconUpAnimation);
                } else {
                    if (currPos == 0)
                        createIconDownAnimation(infoTabDy[currDy]);
                    else
                        createIconDownAnimation(dy[currDy]);
                    temp.startAnimation(iconDownAnimation);
                }
                currDy++;
                delay -= 50;
                temp.setVisibility(visibility);
                circledImageViews.remove(0);
            }

        }

    }

    /**
      * Crée une animation pour la galerie et la galerie, les icônes homePage et trailer avec la direction vers le haut.
      */
    public void createIconUpAnimation(float dy, int delay) {
        iconUpAnimation = new TranslateAnimation(0, 0, 0, (-(scale * 67.3f) + 0.5f - (dy * scale)) * iconDirection);
        iconUpAnimation.setDuration(250);
        iconUpAnimation.setFillAfter(false);
        iconUpAnimation.setStartOffset(delay);
        iconUpAnimation.setAnimationListener(iconUpAnimationListener);
    }

    /**
     * Crée une animation pour la galerie, homePage et les icônes de la remorque avec la direction du bas.
     */
    public void createIconDownAnimation(float dy) {
        iconDownAnimation = new TranslateAnimation(0, 0, 0, ((scale * 67.3f) + 0.5f + (dy * scale)) * iconDirection);
        iconDownAnimation.setDuration(250);
        iconDownAnimation.setFillAfter(false);
        iconDownAnimation.setAnimationListener(iconDownAnimationListener);
    }

    /**
      * Écouteur qui met à jour la position des icônes après la fin de l'animation.
      */
    private class IconUpAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            if (currPos != 0) {
                updateIconUpPos();
                homeIcon.clearAnimation();
                trailerIcon.clearAnimation();
                galleryIcon.clearAnimation();
            } else {
                updateIconUpPosInInfoTab();
                movieDetailsInfo.getHomeIcon().clearAnimation();
                movieDetailsInfo.getTrailerIcon().clearAnimation();
                movieDetailsInfo.getGalleryIcon().clearAnimation();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
      * Écouteur qui met à jour la position des icônes après la fin de l'animation.
      */
    private class IconDownAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

            if (currPos != 0) {
                updateIconDownPos();
                homeIcon.clearAnimation();
                trailerIcon.clearAnimation();
                galleryIcon.clearAnimation();
            } else {
                updateIconDownPosInInfoTab();
                movieDetailsInfo.getHomeIcon().clearAnimation();
                movieDetailsInfo.getTrailerIcon().clearAnimation();
                movieDetailsInfo.getGalleryIcon().clearAnimation();

            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
       * Met à jour la position des icônes lors de l'appel.
       */
    public void updateIconDownPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(trailerIcon.getWidth(), trailerIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + +0.5f), (int) (scale * 23 + 0.5f), 0);
        trailerIcon.setLayoutParams(lp2);
        lp3.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

    /**
       * Met à jour la position des icônes lors de l'appel.
       */
    public void updateIconUpPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(trailerIcon.getWidth(), trailerIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        trailerIcon.setLayoutParams(lp2);
        lp3.setMargins(0, (int) (scale * (328.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

    /**
      * Met à jour la position des icônes dans l'info du film appuyez sur lorsqu'il est appelé.
      */
    public void updateIconDownPosInInfoTab() {
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) movieDetailsInfo.getGalleryIcon().getLayoutParams();
        p.removeRule(RelativeLayout.ABOVE);
        p.removeRule(RelativeLayout.BELOW);
        p.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
        p.setMargins(0, 0, (int) (23 * scale), (int) (-20 * scale));
        movieDetailsInfo.getGalleryIcon().setLayoutParams(p);
        movieDetailsInfo.getTrailerIcon().setLayoutParams(p);
        movieDetailsInfo.getHomeIcon().setLayoutParams(p);
    }

    /**
      * Met à jour la position des icônes dans l'info du film appuyez sur lorsqu'il est appelé.
      */
    public void updateIconUpPosInInfoTab() {
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(trailerIcon.getWidth(), trailerIcon.getHeight());
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        p3.addRule(RelativeLayout.ALIGN_PARENT_END);
        p2.addRule(RelativeLayout.ALIGN_PARENT_END);
        p.addRule(RelativeLayout.ALIGN_PARENT_END);
        if (iconDirection == 1) {
            p3.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
            p3.setMargins(0, 0, (int) (23 * scale), (int) (44 * scale));
            p2.addRule(RelativeLayout.ABOVE, R.id.homeIcon);
            p2.setMargins(0, 0, (int) (23 * scale), (int) (15.5f * scale));
            p.addRule(RelativeLayout.ABOVE, R.id.trailerIcon);
            p.setMargins(0, 0, (int) (23 * scale), (int) (15 * scale));
        } else {
            p3.addRule(RelativeLayout.BELOW, R.id.moreIcon);
            p3.setMargins(0, (int) (16 * scale), (int) (23 * scale), 0);
            p2.addRule(RelativeLayout.BELOW, R.id.homeIcon);
            p2.setMargins(0, (int) (15.5f * scale), (int) (23 * scale), 0);
            p.addRule(RelativeLayout.BELOW, R.id.trailerIcon);
            p.setMargins(0, (int) (15 * scale), (int) (23 * scale), 0);
        }

        movieDetailsInfo.getHomeIcon().setLayoutParams(p3);
        movieDetailsInfo.getTrailerIcon().setLayoutParams(p2);
        movieDetailsInfo.getGalleryIcon().setLayoutParams(p);

    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        float scroll = scrollY;
        if (mViewPager.getCurrentItem() == 0) {
            scroll = (scroll / scrollSpeed);

            if (scrollY / scale >= 0 && onMoreIconClick.getKey())
                onMoreIconClick.onClick(null);
        }

        if (dragging) {
            View toolbarView = getActivity().findViewById(R.id.toolbar);

            if (scroll > oldScrollY) {

                if (upDyKey) {
                    upDy = scroll;
                    upDyKey = false;
                } else {
                    dy = upDy - scroll;

                    if (dy >= -toolbarView.getHeight()) {
                        toolbarView.setTranslationY(dy);
                        mSlidingTabLayout.setTranslationY(dy);
                    } else {
                        toolbarView.setTranslationY(-toolbarView.getHeight());
                        mSlidingTabLayout.setTranslationY(-toolbarView.getHeight());
                    }

                    downDyKey = true;
                }

            }

            if (scroll < oldScrollY) {

                if (downDyKey) {
                    downDy = scroll;
                    downDyTrans = toolbarView.getTranslationY();
                    downDyKey = false;
                } else {

                    dy = (downDyTrans + (downDy - scroll));
                    if (dy <= 0) {
                        toolbarView.setTranslationY(dy);
                        mSlidingTabLayout.setTranslationY(dy);
                    } else {
                        toolbarView.setTranslationY(0);
                        mSlidingTabLayout.setTranslationY(0);
                    }

                    upDyKey = true;

                }
            }


        }

        oldScrollY = scroll;
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        adjustToolbar(scrollState);
    }


    private Scrollable getCurrentScrollable() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        switch (mViewPager.getCurrentItem()) {
            case 0:
                return (Scrollable) view.findViewById(R.id.moviedetailsinfo);
            case 1:
                return (Scrollable) view.findViewById(R.id.castList);
            case 2:
                return (Scrollable) view.findViewById(R.id.moviedetailsoverview);
            default:
                return (Scrollable) view.findViewById(R.id.moviedetailsinfo);
        }
    }

    /**
      * Fixe la position de la barre d'outils
      *
      * @param scrollState
      */
    private void adjustToolbar(ScrollState scrollState) {
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        int toolbarHeight = toolbarView.getHeight();
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
        int scrollY = scrollable.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            switch (currPos) {
                case 0:
                    if (119 * scale <= scrollY) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                    break;
                case 1:
                    if (toolbarHeight <= scrollY - hideThreshold) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                    break;
                case 2:
                    if (toolbarHeight <= scrollY) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                    break;
            }

        }
    }

    /**
      * Renvoie le fragment actif en cours pour la position donnée
      */
    private Fragment getCurrentFragment() {
        return movieDetailsSlideAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
    }


    private void showToolbar() {
        animateToolbar(0);
    }

    private void hideToolbar() {
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        animateToolbar(-toolbarView.getHeight());
    }

    /**
      * Anime notre barre d'outils dans la direction donnée
      *
      * @param toY notre longueur de traduction.
      */
    private void animateToolbar(final float toY) {
        if (activity != null) {
            View toolbarView = activity.findViewById(R.id.toolbar);

            if (toolbarView != null) {
                toolbarView.animate().translationY(toY).setInterpolator(new DecelerateInterpolator(2)).setDuration(200).start();
                mSlidingTabLayout.animate().translationY(toY).setInterpolator(new DecelerateInterpolator(2)).setDuration(200).start();


                if (toY == 0) {
                    upDyKey = true;
                    downDyKey = false;
                    downDy = 9999999;
                } else {
                    downDyKey = true;
                    upDyKey = false;
                    upDy = -9999999;
                }


            }
        }
    }

    /**
      * Instant montre notre barre d'outils. Utilisé lorsque vous cliquez sur les détails du film de la liste des films et de la barre d'outils.
      */
    public void showInstantToolbar() {
        if (activity != null) {
            View toolbarView = activity.findViewById(R.id.toolbar);

            if (toolbarView != null) {
                toolbarView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(0).start();
                mSlidingTabLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(0).start();


                upDyKey = true;
                downDyKey = false;
                downDy = 9999999;

            }
        }
    }

    /**
      *Lancé lorsque le fragment est détruit.
      */
    public void onDestroyView() {
        super.onDestroyView();
        onPageChangeSelected = null;
        onGalleryIconClick = null;
        onTrailerIconClick = null;
        onMoreIconClick = null;
        onHomeIconClick = null;
        onPageChangeSelected = null;
        downAnimationListener = null;
        upAnimationListener = null;
        iconUpAnimationListener = null;
        iconDownAnimationListener = null;
    }


}