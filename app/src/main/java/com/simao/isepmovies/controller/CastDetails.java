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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.MovieDB;
import com.simao.isepmovies.R;
import com.simao.isepmovies.adapter.CastDetailsSlideAdapter;
import com.simao.isepmovies.helper.ObservableScrollViewCallbacks;
import com.simao.isepmovies.helper.ScrollState;
import com.simao.isepmovies.helper.Scrollable;
import com.simao.isepmovies.model.MovieModel;
import com.simao.isepmovies.model.SimilarModel;
import com.simao.isepmovies.view.MovieDetailsSlidingTabLayout;
import com.simao.isepmovies.view.ObservableParallaxScrollView;

/**
 * Afficher les détails
 */
public class CastDetails extends Fragment implements ObservableScrollViewCallbacks {

    private MainActivity activity;
    private View rootView;
    private int currentId;
    private int timeOut;
    private HttpURLConnection conn;
    private String title;
    private Bundle save;


    private ArrayList<MovieModel> moviesList;
    private MovieModel movieModel;

    private ProgressBar spinner;
    private int moreIconCheck;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private int homeIconCheck;
    private int galleryIconCheck;
    private CircledImageView galleryIcon;
    private ArrayList<String> galleryList;

    private onGalleryIconClick onGalleryIconClick;
    private onMoreIconClick onMoreIconClick;
    private onHomeIconClick onHomeIconClick;

    private MovieDetailsSlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private CastDetailsSlideAdapter castDetailsSlideAdapter;
    private onPageChangeSelected onPageChangeSelected;
    private TranslateAnimation downAnimation;
    private DownAnimationListener downAnimationListener;
    private TranslateAnimation upAnimation;
    private UpAnimationListener upAnimationListener;
    private TranslateAnimation iconUpAnimation;
    private IconUpAnimationListener iconUpAnimationListener;
    private TranslateAnimation iconDownAnimation;
    private IconDownAnimationListener iconDownAnimationListener;
    private CastDetailsInfo castDetailsInfo;
    private CastDetailsCredits castDetailsCredits;
    private CastDetailsBiography castDetailsBiography;
    private int castDetailsInfoScrollY;
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
    private int twoIcons;
    private int twoIconsToolbar;
    private int oneIcon;
    private int oneIconToolbar;
    private float scale;
    private boolean phone;
    private int hideThreshold;
    private int minThreshold;
    private int iconDirection;
    private boolean noCredits;

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
        movieModel = new MovieModel();
        onGalleryIconClick = new onGalleryIconClick();
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
            rootView = inflater.inflate(R.layout.castdetails, container, false);
            spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);

            homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
            homeIcon.bringToFront();
            homeIcon.setVisibility(View.INVISIBLE);

            galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
            galleryIcon.bringToFront();
            galleryIcon.setVisibility(View.INVISIBLE);

            moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
            moreIcon.bringToFront();
        }
        moreIcon.setOnClickListener(onMoreIconClick);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activity.getCastDetailsBundle().size() > 0 && activity.getRestoreMovieDetailsState()) {
            save = activity.getCastDetailsBundle().get(activity.getCastDetailsBundle().size() - 1);
            activity.removeCastDetailsBundle(activity.getCastDetailsBundle().size() - 1);
            if (activity.getSearchViewCount())
                activity.decSearchCastDetails();
            activity.setRestoreMovieDetailsState(false);
        }
        if (save != null && save.getInt("timeOut") == 1)
            activity.setRestoreMovieDetailsAdapterState(true);
        // Récupère le ViewPager et positionne le PagerAdapter pour qu'il puisse afficher les éléments
        castDetailsSlideAdapter = new CastDetailsSlideAdapter(getChildFragmentManager(), getResources(), activity);
        if (mViewPager != null)
            currPos = mViewPager.getCurrentItem();
        mViewPager = (ViewPager) rootView.findViewById(R.id.castDetailsPager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(castDetailsSlideAdapter);
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
                        request.execute(MovieDB.url + "person/" + currentId + "?append_to_response=combined_credits%2Cimages&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException | CancellationException e) {
                        request.cancel(true);
                        // nous abandonnons la requête http, sinon cela causera des problèmes et ralentira la connexion plus tard
                        if (conn != null)
                            conn.disconnect();
                        if (spinner != null)
                            activity.hideView(spinner);
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
        activity.setCastDetailsFragment(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                castDetailsInfo = (CastDetailsInfo) castDetailsSlideAdapter.getRegisteredFragment(0);
                castDetailsCredits = (CastDetailsCredits) castDetailsSlideAdapter.getRegisteredFragment(1);
                castDetailsBiography = (CastDetailsBiography) castDetailsSlideAdapter.getRegisteredFragment(2);
            }
        });

        showInstantToolbar();

        iconMarginConstant = activity.getIconMarginConstant();
        iconMarginLandscape = activity.getIconMarginLandscape();
        iconConstantSpecialCase = activity.getIconConstantSpecialCase();
        twoIcons = activity.getTwoIcons();
        twoIconsToolbar = activity.getTwoIconsToolbar();
        oneIcon = activity.getOneIcon();
        oneIconToolbar = activity.getOneIconToolbar();

        /*Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("CastDetails - " + getTitle());
        t.send(new HitBuilders.ScreenViewBuilder().build());*/
    }

    /**
      * Cette classe gère la connexion à notre serveur principal.
      * Si la connexion est réussie, nous donnons des informations sur nos vues.
      */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        /**
         * Appelé avant doInBackground ()
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

       /**
          * Ici, nous établissons la connexion. Si tout va bien, nous mettons à jour les vues.
          *
          * @param urls l'URL à charger.
          * @return vrai si succès, faux pour échec.
          */
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
                    while (castDetailsInfo == null) {
                        Thread.sleep(200);
                    }
                    if (isAdded() && castDetailsInfo != null) {
                        // Nom
                        activity.setText(castDetailsInfo.getName(), jsonData.getString("name"));
                        // Photo de profil
                        if (!jsonData.getString("profile_path").equals("null") && !jsonData.getString("profile_path").isEmpty()) {
                            activity.setBackDropImage(castDetailsInfo.getProfilePath(), jsonData.getString("profile_path"));
                            activity.setImageTag(castDetailsInfo.getProfilePath(), jsonData.getString("profile_path"));
                        }
                        // L'information sur la naissance commence ici.
                        String birthInfoData = "";
                        if (!jsonData.getString("place_of_birth").equals("null") && !jsonData.getString("place_of_birth").isEmpty())
                            birthInfoData += jsonData.getString("place_of_birth");

                        if (!jsonData.getString("birthday").equals("null") && !jsonData.getString("birthday").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("birthday"));
                                String formattedDate = activity.getDateFormat().format(date);
                                birthInfoData += " " + formattedDate;
                            } catch (java.text.ParseException e) {
                            }
                        }


                        if (!jsonData.getString("deathday").equals("null") && !jsonData.getString("deathday").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("deathday"));
                                String formattedDate = activity.getDateFormat().format(date);
                                birthInfoData += " - " + formattedDate;
                            } catch (java.text.ParseException e) {
                            }
                        }

                        if (!birthInfoData.equals("null") && !birthInfoData.isEmpty())
                            activity.setText(castDetailsInfo.getBirthInfo(), birthInfoData);
                        else
                            activity.hideTextView(castDetailsInfo.getBirthInfo());
                        // fin.

                        // Aussi connu sous le nom
                        JSONArray alsoKnownAsArray = jsonData.getJSONArray("also_known_as");
                        String alsoKnownAsData = "";
                        for (int i = 0; i < alsoKnownAsArray.length(); i++) {
                            if (i + 1 == alsoKnownAsArray.length())
                                alsoKnownAsData += alsoKnownAsArray.get(i);
                            else
                                alsoKnownAsData += alsoKnownAsArray.get(i) + ", ";
                        }
                        if (!alsoKnownAsData.equals("null") && !alsoKnownAsData.isEmpty())
                            activity.setText(castDetailsInfo.getAlsoKnownAs(), getResources().getString(R.string.alsoKnownAs) + " " + alsoKnownAsData);
                        else
                            activity.hideTextView(castDetailsInfo.getAlsoKnownAs());

                        //Icône de la page d'accueil
                        if (!jsonData.getString("homepage").isEmpty() && !jsonData.getString("homepage").equals("null")) {
                            homeIconUrl = jsonData.getString("homepage");
                            homeIconCheck = 0;
                        } else {
                            activity.invisibleView(homeIcon);
                            homeIconCheck = 1;
                        }

                        // Gallery
                        JSONObject galleryObject = jsonData.getJSONObject("images");
                        JSONArray galleryProfilesArray = galleryObject.getJSONArray("profiles");
                        galleryList = new ArrayList<>();
                        if (galleryProfilesArray.length() > 0) {
                            for (int i = 0; i < galleryProfilesArray.length(); i++) {
                                JSONObject object = galleryProfilesArray.getJSONObject(i);
                                galleryList.add(object.getString("file_path"));
                            }
                            galleryIconCheck = 0;
                        } else {
                            activity.invisibleView(galleryIcon);
                            galleryIconCheck = 1;
                        }
                        // Crédits, ici on charge la distribution
                        JSONObject credits = jsonData.getJSONObject("combined_credits");
                        JSONArray creditsArray = credits.getJSONArray("cast");
                        moviesList = new ArrayList<>();
                        final ArrayList<SimilarModel> similarList = new ArrayList<>();

                        for (int i = 0; i < creditsArray.length(); i++) {
                            JSONObject object = creditsArray.getJSONObject(i);

                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));

                            if (object.getString("media_type").equals("movie")) {
                                movie.setTitle(object.getString("title"));
                                if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                    movie.setReleaseDate(object.getString("release_date"));
                            }

                            if (object.getString("media_type").equals("tv")) {
                                movie.setTitle(object.getString("name"));
                                if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                    movie.setReleaseDate(object.getString("first_air_date"));
                            }

                            movie.setCharacter(object.getString("character"));
                            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
                            if (isAdded()) {
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                            }

                            movie.setMediaType(object.getString("media_type"));

                            moviesList.add(movie);
                        }

                        // Équipage
                        JSONArray crewArray = credits.getJSONArray("crew");
                        for (int i = 0; i < crewArray.length(); i++) {
                            JSONObject object = crewArray.getJSONObject(i);

                            String departmentAndJob = "";
                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));

                            if (object.getString("media_type").equals("movie")) {
                                movie.setTitle(object.getString("title"));
                                if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                    movie.setReleaseDate(object.getString("release_date"));
                            }

                            if (object.getString("media_type").equals("tv")) {
                                movie.setTitle(object.getString("name"));
                                if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                    movie.setReleaseDate(object.getString("first_air_date"));
                            }
                            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
                            if (isAdded()) {
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                            }


                            if (!object.getString("department").equals("null") && !object.getString("department").isEmpty())
                                departmentAndJob += object.getString("department");

                            if (!object.getString("job").equals("null") && !object.getString("job").isEmpty())
                                departmentAndJob += " / " + object.getString("job");

                            movie.setDepartmentAndJob(departmentAndJob);

                            movie.setMediaType(object.getString("media_type"));

                            moviesList.add(movie);
                        }

                        Collections.sort(moviesList, movieModel);
                        ArrayList<MovieModel> movieListNoDuplicates = removeDuplicates(moviesList);
                        int simLen = 6;
                        if (movieListNoDuplicates.size() < 6)
                            simLen = movieListNoDuplicates.size();
                        for (int i = 0; i < simLen; i++) {
                            SimilarModel simMov = new SimilarModel();
                            simMov.setId(movieListNoDuplicates.get(i).getId());
                            simMov.setMediaType(movieListNoDuplicates.get(i).getMediaType());
                            simMov.setTitle(movieListNoDuplicates.get(i).getTitle());

                            if (movieListNoDuplicates.get(i).getReleaseDate() != null)
                                simMov.setReleaseDate(movieListNoDuplicates.get(i).getReleaseDate());
                            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
                            if (isAdded()) {
                                if (movieListNoDuplicates.get(i).getPosterPath() != null)
                                    simMov.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + movieListNoDuplicates.get(i).getPosterPath());
                            }

                            similarList.add(simMov);
                        }

                        if (similarList.size() == 0)
                            activity.hideView(castDetailsInfo.getKnownHolder());
                        if (movieListNoDuplicates.size() < 7)
                            activity.hideView(castDetailsInfo.getShowMoreButton());

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (moviesList.size() == 0) {
                                    noCredits = true;
                                    mSlidingTabLayout.disableTabClickListener(1);
                                }

                                // Cast
                                if (isAdded()) {
                                    castDetailsInfo.setKnownList(similarList);
                                    castDetailsCredits.setAdapter(moviesList);
                                }
                            }
                        });

                        // Biographie
                        final String biography = jsonData.getString("biography");

                        if (!biography.equals("null") && !biography.isEmpty())
                            activity.setText(castDetailsBiography.getBiography(), biography);
                        else
                            activity.setText(castDetailsBiography.getBiography(), getResources().getString(R.string.noBiography));


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
            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme
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
                    if (homeIconCheck == 1 && galleryIconCheck == 1) {
                        moreIconCheck = 1;
                        activity.hideView(moreIcon);
                        activity.hideView(castDetailsInfo.getMoreIcon());
                    } else {
                        moreIconCheck = 0;
                        activity.showView(castDetailsInfo.getMoreIcon());
                        // set listener on backdrop click to open gallery
                        if (galleryIconCheck == 0)
                            castDetailsInfo.getProfilePath().setOnClickListener(onGalleryIconClick);
                        adjustIconsPos(homeIcon, galleryIcon);
                        adjustIconsPos(castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
                    }
                }
            } else setTimeOut(1);
        }
    }

    /**
      * Nous utilisons cette clé pour savoir si l'utilisateur a essayé d'ouvrir ce film et que la connexion a échoué.
      * Donc, s'il essaie de charger à nouveau le même film, nous savons que la connexion a échoué et nous devons faire une nouvelle demande.
      */
    public int getTimeOut() {
        return timeOut;
    }

    /**
      * Met à jour la valeur timeOut.
      */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
      * Mettre à jour le titre. Nous utilisons cette méthode pour enregistrer notre titre et ensuite le définir dans la barre d'outils.
      */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
      * Obtient le titre.
      */
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
            outState.putBundle("save", save);
            if (addToBackStack) {
                activity.addCastDetailsBundle(save);
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

                // Icône de la galerie
                send.putInt("galleryIconCheck", galleryIconCheck);
                if (galleryIconCheck == 0)
                    send.putStringArrayList("galleryList", galleryList);

                // Plus d'Icônes
                send.putInt("moreIconCheck", moreIconCheck);

                // Cast details info commence ici
                if (castDetailsInfo != null) {
                    // Nom
                    send.putString("name", castDetailsInfo.getName().getText().toString());

                    // URL du chemin de l'affiche
                    if (castDetailsInfo.getProfilePath().getTag() != null)
                        send.putString("profilePathURL", castDetailsInfo.getProfilePath().getTag().toString());

                    // Informations sur la naissance
                    send.putString("birthInfo", castDetailsInfo.getBirthInfo().getText().toString());

                    // Aussi connu sous le nom
                    send.putString("alsoKnownAs", castDetailsInfo.getAlsoKnownAs().getText().toString());

                    // liste connue
                    if (castDetailsInfo.getKnownList() != null && castDetailsInfo.getKnownList().size() > 0)
                        send.putParcelableArrayList("knownList", castDetailsInfo.getKnownList());

                }
                // fin

                // Crédits commence ici
                if (castDetailsCredits != null)
                    send.putParcelableArrayList("moviesList", moviesList);

                // fin

                // Aperçu
                if (castDetailsBiography != null)
                    send.putString("biography", castDetailsBiography.getBiography().getText().toString());

            }

            outState.putBundle("save", send);
            save = send;
            if (addToBackStack) {
                activity.addCastDetailsBundle(send);
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
        // Page d'accueil
        homeIconCheck = args.getInt("homeIconCheck");
        if (homeIconCheck == 0)
            homeIconUrl = args.getString("homepage");

        // Galerie
        galleryIconCheck = args.getInt("galleryIconCheck");
        if (galleryIconCheck == 0) {
            galleryList = new ArrayList<>();
            galleryList = args.getStringArrayList("galleryList");
            if (galleryList.size() == 0)
                activity.hideView(galleryIcon);
        }

        // Plus d'icône
        moreIconCheck = args.getInt("moreIconCheck");

        if (homeIconCheck == 1 && galleryIconCheck == 1) {
            moreIconCheck = 1;
            moreIcon.setVisibility(View.GONE);
        } else moreIconCheck = 0;


        mSlidingTabLayout.setOnPageChangeListener(onPageChangeSelected);
        activity.setCastDetailsInfoBundle(save);
        activity.setCastDetailsCreditsBundle(save);
        activity.setCastDetailsBiographyBundle(save);

        moviesList = save.getParcelableArrayList("moviesList");
        if (moviesList != null && moviesList.size() == 0) {
            noCredits = true;
            mSlidingTabLayout.disableTabClickListener(1);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                castDetailsInfo = (CastDetailsInfo) castDetailsSlideAdapter.getRegisteredFragment(0);
                castDetailsCredits = (CastDetailsCredits) castDetailsSlideAdapter.getRegisteredFragment(1);
                if (currPos == 0) {
                    moreIcon.setVisibility(View.INVISIBLE);
                } else if (moreIconCheck == 0) {
                    castDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
                    updateDownPos();
                }
                if (moreIconCheck == 1)
                    castDetailsInfo.getMoreIcon().setVisibility(View.GONE);
                else {
                    // définir l'écouteur en arrière-plan cliquez pour ouvrir la galerie
                    if (galleryIconCheck == 0 && galleryList.size() > 0)
                        castDetailsInfo.getProfilePath().setOnClickListener(onGalleryIconClick);
                    adjustIconsPos(homeIcon, galleryIcon);
                    adjustIconsPos(castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
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

                if (castDetailsCredits.getMoviesList().size() < 7)
                    activity.hideView(castDetailsInfo.getShowMoreButton());

            }
        });

    }

    /**
      * Classe qui écoute lorsque l'utilisateur a appuyé sur le bouton Icône Plus.
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
            items = homeIconCheck + galleryIconCheck;
            toolbarHidden = toolbarView.getTranslationY() == -toolbarView.getHeight();
            currScroll = castDetailsInfo.getRootView().getScrollY();

            if (!key) {
                iconDirection = 1;
                if (currPos == 0) {
                    // 2 icons
                    if (items == 0) {
                        if (toolbarHidden && currScroll / scale > twoIcons) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > twoIconsToolbar) {
                            iconDirection = -1;
                        }
                    }
                    // 1 icône
                    if (items == 1) {
                        if (toolbarHidden && currScroll / scale > oneIcon) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > oneIconToolbar) {
                            iconDirection = -1;
                        }
                    }
                }
                if (currPos == 0) {
                    castDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, homeIcon, galleryIcon);
                }
                key = true;
            } else {
                if (currPos == 0) {
                    castDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, homeIcon, galleryIcon);
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
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(homeIcon.getTag().toString()));
            startActivity(i);
        }
    }

    /**
      * Classe qui écoute lorsque l'utilisateur a appuyé sur le bouton icône Galerie.
      */
    public class onGalleryIconClick implements View.OnClickListener {
        public onGalleryIconClick() {
            // conserve des références pour votre logique onClick
        }


        @Override
        public void onClick(View v) {
            if (activity.getSearchViewCount())
                activity.incSearchCastDetails();
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
                    // add the current transaction to the back stack:
                    transaction.addToBackStack("movieDetails");
                    transaction.commit();
                }
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
            if (noCredits) {
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
                if (castDetailsInfo != null) {
                    if (119 * scale <= (castDetailsInfo.getScrollView().getChildAt(0).getHeight() - (567 * scale)) && castDetailsInfo.canScroll()) {
                        if (toolbarHidden) {
                            final ObservableParallaxScrollView scrollView = castDetailsInfo.getScrollView();
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


            if (position == 1 && castDetailsCredits != null) {
                if (castDetailsCredits.getMoviesList() != null && castDetailsCredits.getMoviesList().size() > 0) {
                    final AbsListView listView = castDetailsCredits.getListView();
                    if (castDetailsCredits.canScroll()) {
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

            if (position == 2 && castDetailsBiography != null) {
                castDetailsBiography.getScrollView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (toolbarHidden)
                            castDetailsBiography.getScrollView().scrollTo(0, (int) (56 * scale));
                        else
                            castDetailsBiography.getScrollView().scrollTo(0, 0);
                    }
                });
            }

            if (moreIconCheck == 0) {
                if (castDetailsInfo != null) {
                    castDetailsInfoScrollY = castDetailsInfo.getRootView().getScrollY();

                    galleryIcon.clearAnimation();
                    homeIcon.clearAnimation();

                    homeIcon.setVisibility(View.INVISIBLE);
                    castDetailsInfo.getHomeIcon().setVisibility(View.INVISIBLE);

                    galleryIcon.setVisibility(View.INVISIBLE);
                    castDetailsInfo.getGalleryIcon().setVisibility(View.INVISIBLE);

                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));

                    if (position == 0) {
                        castDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                        moreIcon.setVisibility(View.INVISIBLE);
                    } else {
                        castDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
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
                            createUpAnimation((119 * scale) - castDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                    if (currPos == 2 && position == 0) {
                        updateDownPos();
                        if (infoTabScrollPosUpdated) {
                            infoTabScrollPosUpdated = false;
                            createUpAnimation((119 * scale) - castDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                }
            }

            if (!noCredits || position != 1)
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
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp.setMargins(0, (int) (scale * (496 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 15 + 0.5f), 0);
        moreIcon.setLayoutParams(lp);
        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp2);
    }

    /**
      * Écouteur qui met à jour la position des icônes après la fin de l'animation.
      */
    private class UpAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            castDetailsInfo.getMoreIcon().clearAnimation();
            castDetailsInfo.getMoreIcon().setVisibility(View.VISIBLE);
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
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp.setMargins(0, (int) (scale * (346 + iconMarginConstant) + 0.5f - castDetailsInfoScrollY), (int) (scale * 15 + 0.5f), 0);
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
            downAnimation = new TranslateAnimation(0, 0, 0, -scale * 150 + 0.5f + castDetailsInfoScrollY);
        else
            downAnimation = new TranslateAnimation(0, 0, 0, scale * (150 + iconConstantSpecialCase) + 0.5f + castDetailsInfoScrollY);
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
            upAnimation = new TranslateAnimation(0, 0, 0, scale * 150 + 0.5f - castDetailsInfoScrollY - dy);
        else
            upAnimation = new TranslateAnimation(0, 0, 0, -scale * (150 + iconConstantSpecialCase) + 0.5f - castDetailsInfoScrollY - dy);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(false);
        upAnimation.setAnimationListener(upAnimationListener);
    }

    public CastDetails.onMoreIconClick getOnMoreIconClick() {
        return onMoreIconClick;
    }

    public void setAddToBackStack(boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
    }

    public Bundle getSave() {
        return save;
    }

    public void setSave(Bundle save) {
        this.save = save;
    }

    /**
    * Cette méthode calcule quelles icônes avons-nous?
    *
    * @param homeIcon la première icône
    * @param galleryIcon la deuxième icône
    */
    public void adjustIconsPos(CircledImageView homeIcon, CircledImageView galleryIcon) {
        int iconCount[] = {homeIconCheck, galleryIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(homeIcon);
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
    public void showHideImages(int visibility, CircledImageView homeIcon, CircledImageView galleryIcon) {
        float dy[] = {0.7f, 56.7f};
        float infoTabDy[] = {-2.4f, 53.5f};
        int currDy = 0;
        int delay = 100;
        int iconCount[] = {homeIconCheck, galleryIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(homeIcon);
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
      * Crée une animation pour la galerie et les icônes de page d'accueil avec la direction vers le haut.
      */
    public void createIconUpAnimation(float dy, int delay) {
        iconUpAnimation = new TranslateAnimation(0, 0, 0, (-(scale * 67.3f) + 0.5f - (dy * scale)) * iconDirection);
        iconUpAnimation.setDuration(250);
        iconUpAnimation.setFillAfter(false);
        iconUpAnimation.setStartOffset(delay);
        iconUpAnimation.setAnimationListener(iconUpAnimationListener);
    }

    /**
      * Crée une animation pour la galerie et les icônes homePage avec la direction vers le bas.
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
                galleryIcon.clearAnimation();
            } else {
                updateIconUpPosInInfoTab();
                castDetailsInfo.getHomeIcon().clearAnimation();
                castDetailsInfo.getGalleryIcon().clearAnimation();
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
                galleryIcon.clearAnimation();
            } else {
                updateIconDownPosInInfoTab();
                castDetailsInfo.getHomeIcon().clearAnimation();
                castDetailsInfo.getGalleryIcon().clearAnimation();

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
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp3.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

    /**
      * Met à jour la position des icônes lors de l'appel.
      */
    public void updateIconUpPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp3.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

   /**
     * Met à jour la position des icônes dans l'info du film appuyez sur lorsqu'il est appelé.
     */
    public void updateIconDownPosInInfoTab() {
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) castDetailsInfo.getGalleryIcon().getLayoutParams();
        p.removeRule(RelativeLayout.ABOVE);
        p.removeRule(RelativeLayout.BELOW);
        p.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
        p.setMargins(0, 0, (int) (23 * scale), (int) (-20 * scale));
        castDetailsInfo.getGalleryIcon().setLayoutParams(p);
        castDetailsInfo.getHomeIcon().setLayoutParams(p);
    }

    /**
      * Met à jour la position des icônes dans l'info du film appuyez sur lorsqu'il est appelé.
      */
    public void updateIconUpPosInInfoTab() {
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        p3.addRule(RelativeLayout.ALIGN_PARENT_END);
        p.addRule(RelativeLayout.ALIGN_PARENT_END);
        if (iconDirection == 1) {
            p3.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
            p3.setMargins(0, 0, (int) (23 * scale), (int) (44 * scale));
            p.addRule(RelativeLayout.ABOVE, R.id.homeIcon);
            p.setMargins(0, 0, (int) (23 * scale), (int) (15.5f * scale));
        } else {
            p3.addRule(RelativeLayout.BELOW, R.id.moreIcon);
            p3.setMargins(0, (int) (16 * scale), (int) (23 * scale), 0);
            p.addRule(RelativeLayout.BELOW, R.id.homeIcon);
            p.setMargins(0, (int) (15.5f * scale), (int) (23 * scale), 0);
        }

        castDetailsInfo.getHomeIcon().setLayoutParams(p3);
        castDetailsInfo.getGalleryIcon().setLayoutParams(p);
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
                return (Scrollable) view.findViewById(R.id.castdetailsinfo);
            case 1:
                return (Scrollable) view.findViewById(R.id.castdetailscredits);
            case 2:
                return (Scrollable) view.findViewById(R.id.castdetailsbiography);
            default:
                return (Scrollable) view.findViewById(R.id.castdetailsinfo);
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
        return castDetailsSlideAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
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

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    private ArrayList<MovieModel> removeDuplicates(List<MovieModel> l) {

        Set<MovieModel> set = new TreeSet<>(new Comparator<MovieModel>() {

            @Override
            public int compare(MovieModel o1, MovieModel o2) {
                if (o1.getId() == o2.getId())
                    return 0;
                else return 1;
            }
        });
        set.addAll(l);
        ArrayList<MovieModel> movieListWithoutDuplicates = new ArrayList<>();
        movieListWithoutDuplicates.addAll(set);
        return movieListWithoutDuplicates;
    }

    /**
      * Lancé lorsque le fragment est détruit.
      */
    public void onDestroyView() {
        super.onDestroyView();
        onPageChangeSelected = null;
        onGalleryIconClick = null;
        onMoreIconClick = null;
        onHomeIconClick = null;
        onPageChangeSelected = null;
        downAnimationListener = null;
        upAnimationListener = null;
        iconUpAnimationListener = null;
        iconDownAnimationListener = null;
        movieModel = null;
    }

}
