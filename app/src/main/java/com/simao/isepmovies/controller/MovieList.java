package com.simao.isepmovies.controller;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.MovieDB;
import com.simao.isepmovies.R;
import com.simao.isepmovies.adapter.MovieAdapter;
import com.simao.isepmovies.helper.Scrollable;
import com.simao.isepmovies.model.MovieModel;

/**
 * Cette classe charge la liste des films.
 */
public class MovieList extends Fragment implements AdapterView.OnItemClickListener {

    private MainActivity activity;
    private View rootView;
    private ArrayList<MovieModel> moviesList;
    private int checkLoadMore = 0;
    private int totalPages;
    private MovieAdapter movieAdapter;
    private String currentList = "";
    private int backState;
    private String title;
    private MovieModel movieModel;
    private AbsListView listView;
    private EndlessScrollListener endlessScrollListener;
    private ProgressBar spinner;
    private Toast toastLoadingMore;
    private HttpURLConnection conn;
    private boolean isLoading;
    private Bundle save;
    private boolean fragmentActive;
    private int lastVisitedMovie;
    private MovieDetails movieDetails;
    private boolean genresList;
    private float scale;
    private boolean phone;
    private int minThreshold;

    public MovieList() {
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
    @SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        movieModel = new MovieModel();

        if (this.getArguments().getString("currentList") != null) {
            switch (this.getArguments().getString("currentList")) {
                case "upcoming":
                    rootView = inflater.inflate(R.layout.movieslist, container, false);
                    break;
                case "nowPlaying":
                    rootView = inflater.inflate(R.layout.nowplaying, container, false);
                    break;
                case "popular":
                    rootView = inflater.inflate(R.layout.popular, container, false);
                    break;
                case "topRated":
                    rootView = inflater.inflate(R.layout.toprated, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.movieslist, container, false);
            }
        } else
            rootView = inflater.inflate(R.layout.movieslist, container, false);


        activity = ((MainActivity) getActivity());
        toastLoadingMore = Toast.makeText(activity, R.string.loadingMore, Toast.LENGTH_SHORT);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        if (phone)
            minThreshold = (int) (-49 * scale);
        else
            minThreshold = (int) (-42 * scale);
        return rootView;
    }

    /**
       * @param savedInstanceState si le fragment est en cours de recréation à partir d'un état précédemment enregistré, c'est l'état.
       */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.getArguments().getString("currentList") != null) {
            switch (this.getArguments().getString("currentList")) {

                case "upcoming":
                    listView = (AbsListView) rootView.findViewById(R.id.movieslist);
                    break;
                case "nowPlaying":
                    listView = (AbsListView) rootView.findViewById(R.id.nowplaying);
                    break;
                case "popular":
                    listView = (AbsListView) rootView.findViewById(R.id.popular);
                    break;
                case "topRated":
                    listView = (AbsListView) rootView.findViewById(R.id.toprated);
                    break;
                default:
                    // utilisé dans les genres
                    activity.getMovieSlideTab().showInstantToolbar();
                    listView = (AbsListView) rootView.findViewById(R.id.movieslist);
                    listView.setPadding(0, activity.getToolbar().getHeight(), 0, 0);
                    genresList = true;
            }
        } else {
            // utilisé lors de la recherche de tous les crédits pour une personne spécifique
            listView = (AbsListView) rootView.findViewById(R.id.movieslist);
            moviesList = new ArrayList<>();
            moviesList = this.getArguments().getParcelableArrayList("credits");
            movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
            movieAdapter.sort(movieModel);
            listView.setAdapter(movieAdapter);

        }

        //Handle orientation change starts
        if (save != null) {
            checkLoadMore = save.getInt("checkLoadMore");
            totalPages = save.getInt("totalPages");
            setCurrentList(save.getString("currentListURL"));
            setTitle(save.getString("title"));
            isLoading = save.getBoolean("isLoading");
            lastVisitedMovie = save.getInt("lastVisitedMovie");
            if (save.getInt("backState") == 1) {
                backState = 1;
                moviesList = save.getParcelableArrayList("listData");
                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
                endlessScrollListener = new EndlessScrollListener();
                endlessScrollListener.setCurrentPage(save.getInt("currentPage"));
                endlessScrollListener.setOldCount(save.getInt("oldCount"));
                endlessScrollListener.setLoading(save.getBoolean("loading"));
            } else {
                backState = 0;
            }
        }


        if (listView != null) {

            if (!genresList)
                ((Scrollable) listView).setScrollViewCallbacks(activity.getMovieSlideTab());

            getActivity().setTitle(getTitle());
            listView.setOnItemClickListener(this);


            // vérifie si nous avons défini des arguments pour charger des films pour une personne spécifique.
            if (this.getArguments().getString("currentList") != null) {
                // vérifie si nous étions sur les détails du film et nous le pressons, pour éviter le rechargement de la liste
                if (backState == 0) {
                    if (this.getArguments().getString("currentList").equals("upcoming") && activity.getCurrentMovViewPagerPos() == 0)
                        updateList();

                    if (this.getArguments().getString("currentList").equals("genresList"))
                        updateList();

                    if (isLoading)
                        spinner.setVisibility(View.VISIBLE);
                } else {
                    // Si le segment de mémoire est supérieur à 20 Mo, nous utilisons la logique par défaut de viewpager hors écran
                    // Sinon, nous supprimons les éléments de la liste invisible pour économiser plus de RAM
                    if (MainActivity.getMaxMem() / 1048576 > 20)
                        fragmentActive = true;

                    if (fragmentActive) {
                        listView.setAdapter(movieAdapter);
                        listView.setOnScrollListener(endlessScrollListener);
                    }
                    // Maintain scroll position
                    if (save != null && save.getParcelable("listViewScroll") != null) {
                        listView.onRestoreInstanceState(save.getParcelable("listViewScroll"));
                    }
                }
            }

        }
    }

    /**
      * Méthode de rappel à appeler lorsqu'un élément de ce AdapterView a été cliqué.
      *
      * @param parent AdapterView où le clic est arrivé.
      * Vue @param La vue dans laquelle AdapterView a été cliqué (ce sera une vue fournie par l'adaptateur)
      * @param position La position de la vue dans l'adaptateur.
      * @param id Identificateur de ligne de l'élément sur lequel vous avez cliqué.
      */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        activity.setLastVisitedSimMovie(0);
        activity.resetMovieDetailsBundle();
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        activity.setOrientationChanged(false);
        activity.resetCastDetailsBundle();
        if (movieDetails != null && lastVisitedMovie == moviesList.get(position).getId() && movieDetails.getTimeOut() == 0) {
            // Les anciens détails du film récupèrent les informations et le composant de réinitialisation
            movieDetails.onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", moviesList.get(position).getId());
            Bundle save = movieDetails.getSave();
            movieDetails = new MovieDetails();
            movieDetails.setTimeOut(0);
            movieDetails.setSave(save);
            movieDetails.setArguments(bundle);
        } else movieDetails = new MovieDetails();

        lastVisitedMovie = moviesList.get(position).getId();
        movieDetails.setTitle(moviesList.get(position).getTitle());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", moviesList.get(position).getId());
        movieDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, movieDetails);
        // ajoute la transaction en cours à la pile arrière:
        transaction.addToBackStack("movieList");
        transaction.commit();
        fragmentActive = true;
        activity.getMovieSlideTab().showInstantToolbar();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
      * Cette classe gère la connexion à notre serveur principal.
      * Si la connexion est réussie, nous établissons nos données de liste.
      */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (checkLoadMore == 0) {
                activity.showView(spinner);
                isLoading = true;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        toastLoadingMore.show();
                    }
                });
            }

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

                    JSONObject movieData = new JSONObject(sb.toString());
                    totalPages = movieData.getInt("total_pages");
                    JSONArray movieArray = movieData.getJSONArray("results");

                    // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
                    if (isAdded()) {
                        for (int i = 0; i < movieArray.length(); i++) {
                            JSONObject object = movieArray.getJSONObject(i);

                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));
                            movie.setTitle(object.getString("title"));
                            if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                movie.setReleaseDate(object.getString("release_date"));


                            if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));


                            moviesList.add(movie);
                        }

                        return true;
                    }

                }


            } catch (ParseException | IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
            if (isAdded()) {
                if (checkLoadMore == 0) {
                    activity.hideView(spinner);
                    isLoading = false;
                }

                if (!result) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.noConnection), Toast.LENGTH_LONG).show();
                    backState = 0;
                } else {
                    movieAdapter.notifyDataSetChanged();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            toastLoadingMore.cancel();
                        }
                    });
                    final View toolbarView = activity.findViewById(R.id.toolbar);
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (toolbarView.getTranslationY() == -toolbarView.getHeight() && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                if (phone)
                                    listView.smoothScrollBy((int) (56 * scale), 0);
                                else
                                    listView.smoothScrollBy((int) (59 * scale), 0);
                            }
                        }
                    });

                    backState = 1;
                    save = null;
                }
            }
        }

    }

    /**
       * Cette classe est à l'écoute des événements de défilement dans la liste.
       */
    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int currentPage = 1;
        private boolean loading = false;
        private int oldCount = 0;

        public EndlessScrollListener() {
        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (oldCount != totalItemCount && firstVisibleItem + visibleItemCount >= totalItemCount) {
                loading = true;
                oldCount = totalItemCount;
            }
            if (loading) {
                if (currentPage != totalPages) {
                    currentPage++;
                    checkLoadMore = 1;
                    loading = false;
                    final JSONAsyncTask request = new JSONAsyncTask();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key + "&page=" + currentPage).get(10000, TimeUnit.MILLISECONDS);
                            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                                request.cancel(true);
                                // nous abandonnons la requête http, sinon cela causera des problèmes et ralentira la connexion plus tard
                                if (conn != null)
                                    conn.disconnect();
                                toastLoadingMore.cancel();
                                currentPage--;
                                loading = true;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                } else {
                    if (totalPages != 1) {
                        Toast.makeText(getActivity(), R.string.nomoreresults, Toast.LENGTH_SHORT).show();
                    }
                    loading = false;

                }
            }


        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getOldCount() {
            return oldCount;
        }

        public void setOldCount(int oldCount) {
            this.oldCount = oldCount;
        }

        public boolean getLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }

    }

    public void setCurrentList(String currentList) {
        this.currentList = currentList;
    }

    public String getCurrentList() {
        return currentList;
    }

    public void setBackState(int backState) {
        this.backState = backState;
    }

    public int getBackState() {
        return backState;
    }

    /**
      * Lancé lorsque la liste est vide et nous devrions le mettre à jour.
      */
    public void updateList() {
        if (listView != null) {
            moviesList = new ArrayList<>();
            movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
            listView.setAdapter(movieAdapter);
            endlessScrollListener = new EndlessScrollListener();
            listView.setOnScrollListener(endlessScrollListener);
            checkLoadMore = 0;
            final JSONAsyncTask request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (!isLoading) {
                            request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                        }
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        request.cancel(true);
                        toastLoadingMore.cancel();
                        if (spinner != null)
                            activity.hideView(spinner);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        isLoading = false;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }
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
        if (save != null)
            outState.putBundle("save", save);
        else {
            Bundle send = new Bundle();
            send.putInt("checkLoadMore", checkLoadMore);
            send.putInt("totalPages", totalPages);
            send.putString("currentListURL", getCurrentList());
            send.putString("title", getTitle());
            send.putBoolean("isLoading", isLoading);
            send.putInt("lastVisitedMovie", lastVisitedMovie);
            if (backState == 1) {
                send.putInt("backState", 1);
                send.putParcelableArrayList("listData", moviesList);
                // utilisé pour restaurer les variables de l'écouteur de défilement
                send.putInt("currentPage", endlessScrollListener.getCurrentPage());
                send.putInt("oldCount", endlessScrollListener.getOldCount());
                send.putBoolean("loading", endlessScrollListener.getLoading());
                // Enregistrer la position de défilement
                if (listView != null) {
                    Parcelable listState = listView.onSaveInstanceState();
                    send.putParcelable("listViewScroll", listState);
                }
            } else
                send.putInt("backState", 0);

            outState.putBundle("save", send);
        }
    }

    /**
      * Cette méthode est utilisée si l'appareil a une taille de segment de mémoire <= 20 Mo.
      * En utilisant cette méthode, nous arrivons à n'avoir qu'une seule vue de liste active à la fois.
      */
    public void cleanUp() {
        // WE clean unused lists to save up RAM if the heap size is less or equal to 20MB
        if (MainActivity.getMaxMem() / 1048576 <= 20) {
            if (moviesList != null) {
                listView.setAdapter(null);

            }
        }
    }

    public void setFragmentActive(boolean fragmentActive) {
        this.fragmentActive = fragmentActive;
    }

    /**
      * Utilisé si l'appareil a une taille de segment de mémoire faible <= 20 Mo.
      */
    public void setAdapter() {
        if (listView != null && listView.getAdapter() == null)
            listView.setAdapter(movieAdapter);
    }

    /**
       * Renvoie la liste avec des données.
       * Utilisé lorsque vous cliquez sur l'icône de recherche pour obtenir cette liste et la définir si la liste de recherche est vide.
       */
    public ArrayList<MovieModel> getMoviesList() {
        return moviesList;
    }

    public AbsListView getListView() {
        return listView;
    }

    /**
     * Lancé lorsque le fragment est détruit.
     */
    public void onDestroyView() {
        super.onDestroyView();
    }
}