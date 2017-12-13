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
import com.simao.isepmovies.adapter.SearchAdapter;
import com.simao.isepmovies.model.MovieModel;
import com.simao.isepmovies.model.SearchModel;

/**
  * Cette classe gère la recherche.
  */
public class SearchList extends Fragment implements AdapterView.OnItemClickListener {

    private MainActivity activity;

    private ArrayList<SearchModel> searchList;
    private int totalPages;
    private SearchAdapter searchAdapter;
    private String query = "";
    private String title;

    private MovieDetails movieDetails;
    private CastDetails castDetails;
    private EndlessScrollListener endlessScrollListener;
    private Toast toastLoadingMore;
    private HttpURLConnection conn;
    private AbsListView listView;
    private int backState;
    private Bundle save;
    private int lastVisitedId;


    public SearchList() {
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
        if (getActivity() != null)
            ((MainActivity) getActivity()).setSearchViewCount(true);
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


        View rootView = inflater.inflate(R.layout.searchlist, container, false);
        listView = (AbsListView) rootView.findViewById(R.id.movieslist);
        listView.setOnItemClickListener(this);
        toastLoadingMore = Toast.makeText(getActivity(), R.string.loadingMore, Toast.LENGTH_SHORT);
        activity = ((MainActivity) getActivity());
        return rootView;
    }

    /**
       * @param savedInstanceState si le fragment est en cours de recréation à partir d'un état précédemment enregistré, c'est l'état.
       */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (save != null) {
            totalPages = save.getInt("totalPages");
            setTitle(save.getString("title"));
            query = save.getString("query");
            if (save.getInt("backState") == 1) {
                backState = 1;
                searchList = save.getParcelableArrayList("listData");
                searchAdapter = new SearchAdapter(getActivity(), R.layout.row, searchList);
                endlessScrollListener = new EndlessScrollListener();
                endlessScrollListener.setCurrentPage(save.getInt("currentPage"));
                endlessScrollListener.setOldCount(save.getInt("oldCount"));
                endlessScrollListener.setLoading(save.getBoolean("loading"));
            } else {
                backState = 0;
                if (!query.isEmpty())
                    search();
            }
        }
        activity.setTitle(getTitle());
        activity.clearSearchCount();

        if (searchList == null || searchList.size() == 0) {
            searchList = new ArrayList<>();
            searchAdapter = new SearchAdapter(getActivity(), R.layout.row, searchList);
            ArrayList<MovieModel> movieList = new ArrayList<>();
            int navDrawPos = activity.getOldPos();
            if (navDrawPos == 1) {
                MovieSlideTab movieSlideTab = activity.getMovieSlideTab();
                MovieList movieListView = (MovieList) getFragmentManager().findFragmentByTag(movieSlideTab.getFragmentTag(movieSlideTab.getCurrPos()));
                if (movieListView != null)
                    movieList = movieListView.getMoviesList();
            }
            if (navDrawPos == 3) {
                GenresList genres = activity.getGenresList();
                movieList = genres.getMovieListView().getMoviesList();
                if (movieList == null || movieList.size() == 0) {
                    MovieSlideTab movieSlideTab = activity.getMovieSlideTab();
                    MovieList movieListView = (MovieList) getFragmentManager().findFragmentByTag(movieSlideTab.getFragmentTag(movieSlideTab.getCurrPos()));
                    if (movieListView != null)
                        movieList = movieListView.getMoviesList();
                }

            }

            for (int i = 0; i < movieList.size(); i++) {
                MovieModel movie = movieList.get(i);
                SearchModel search = new SearchModel();

                search.setId(movie.getId());
                search.setTitle(movie.getTitle());
                if (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty())
                    search.setReleaseDate(movie.getReleaseDate());

                if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty())
                    search.setPosterPath(movie.getPosterPath());

                if (navDrawPos == 1 || navDrawPos == 3)
                    search.setMediaType("movie");
                else search.setMediaType("tv");

                searchList.add(search);
            }

        }

        listView.setAdapter(searchAdapter);
        listView.setOnScrollListener(endlessScrollListener);

        if (activity.getOldPos() == 1)
            activity.getMovieSlideTab().showInstantToolbar();
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
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        boolean result;
        Bundle args;
        switch (searchList.get(position).getMediaType()) {
            case "movie":
                activity.setRestoreMovieDetailsAdapterState(true);
                activity.setRestoreMovieDetailsState(false);
                if (movieDetails != null && lastVisitedId == searchList.get(position).getId() && movieDetails.getTimeOut() == 0) {
                    // Old movie details retrieve info and re-init component else crash
                    movieDetails.onSaveInstanceState(new Bundle());
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", searchList.get(position).getId());
                    Bundle save = movieDetails.getSave();
                    movieDetails = new MovieDetails();
                    movieDetails.setTimeOut(0);
                    movieDetails.setSave(save);
                    movieDetails.setArguments(bundle);
                } else movieDetails = new MovieDetails();


                args = new Bundle();
                args.putInt("id", searchList.get(position).getId());
                movieDetails.setArguments(args);

                movieDetails.setTitle(searchList.get(position).getTitle());
                transaction.replace(R.id.frame_container, movieDetails);
                result = true;
                break;

            case "person":
                activity.setRestoreMovieDetailsAdapterState(true);
                activity.setRestoreMovieDetailsState(false);
                if (castDetails != null && lastVisitedId == searchList.get(position).getId() && castDetails.getTimeOut() == 0) {
                    // Les anciens détails du film récupèrent les informations et le composant de réinitialisation
                    castDetails.onSaveInstanceState(new Bundle());
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", searchList.get(position).getId());
                    Bundle save = castDetails.getSave();
                    castDetails = new CastDetails();
                    castDetails.setTimeOut(0);
                    castDetails.setSave(save);
                    castDetails.setArguments(bundle);
                } else castDetails = new CastDetails();


                args = new Bundle();
                args.putInt("id", searchList.get(position).getId());
                castDetails.setArguments(args);

                castDetails.setTitle(searchList.get(position).getTitle());
                transaction.replace(R.id.frame_container, castDetails);
                result = true;
                break;
            default:
                result = false;
                break;
        }

        lastVisitedId = searchList.get(position).getId();

        if (result) {
            // ajoute la transaction en cours à la pile arrière:
            transaction.addToBackStack("movieList");
            transaction.commit();
            // réduire la recherche Voir
            ((MainActivity) getActivity()).collapseSearchView();
        }

    }


    public void onSuggestionClick(int id, String mediaType, String title) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        boolean result;
        Bundle args;
        switch (mediaType) {
            case "movie":
                activity.setRestoreMovieDetailsAdapterState(true);
                activity.setRestoreMovieDetailsState(false);
                if (movieDetails != null && lastVisitedId == id && movieDetails.getTimeOut() == 0) {
                    // Les anciens détails du film récupèrent les informations et le composant de réinitialisation
                    movieDetails.onSaveInstanceState(new Bundle());
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id);
                    Bundle save = movieDetails.getSave();
                    movieDetails = new MovieDetails();
                    movieDetails.setTimeOut(0);
                    movieDetails.setSave(save);
                    movieDetails.setArguments(bundle);
                } else movieDetails = new MovieDetails();


                args = new Bundle();
                args.putInt("id", id);
                movieDetails.setArguments(args);

                movieDetails.setTitle(title);
                transaction.replace(R.id.frame_container, movieDetails);
                result = true;
                break;

            case "person":
                activity.setRestoreMovieDetailsAdapterState(true);
                activity.setRestoreMovieDetailsState(false);
                if (castDetails != null && lastVisitedId == id && castDetails.getTimeOut() == 0) {
                    // Les anciens détails du film récupèrent les informations et le composant de réinitialisation
                    castDetails.onSaveInstanceState(new Bundle());
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id);
                    Bundle save = castDetails.getSave();
                    castDetails = new CastDetails();
                    castDetails.setTimeOut(0);
                    castDetails.setSave(save);
                    castDetails.setArguments(bundle);
                } else castDetails = new CastDetails();


                args = new Bundle();
                args.putInt("id", id);
                castDetails.setArguments(args);

                castDetails.setTitle(title);
                transaction.replace(R.id.frame_container, castDetails);
                result = true;
                break;
            default:
                result = false;
                break;
        }

        lastVisitedId = id;

        if (result) {
            // ajoute la transaction en cours à la pile arrière:
            transaction.addToBackStack("movieList");
            transaction.commit();
            // réduire la recherche Voir
            ((MainActivity) getActivity()).collapseSearchView();
        }

    }

    /**
      * Cette classe gère la connexion à notre serveur principal.
      * Si la connexion est réussie, nous établissons les résultats de la liste.
      */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    toastLoadingMore.show();
                }
            });
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

                    JSONObject searchData = new JSONObject(sb.toString());
                    totalPages = searchData.getInt("total_pages");
                    JSONArray searchResultsArray = searchData.getJSONArray("results");

                    for (int i = 0; i < searchResultsArray.length(); i++) {
                        JSONObject object = searchResultsArray.getJSONObject(i);

                        SearchModel movie = new SearchModel();
                        if (object.has("id") && object.getInt("id") != 0)
                            movie.setId(object.getInt("id"));

                        if (object.has("title"))
                            movie.setTitle(object.getString("title"));

                        if (object.has("name")) {
                            if (object.has("media_type") && object.getString("media_type").equals("tv"))
                                movie.setTitle(object.getString("name"));
                            else
                                movie.setTitle(object.getString("name"));
                        }
                        if (object.has("release_date") && !object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                            movie.setReleaseDate(object.getString("release_date"));

                        if (object.has("first_air_date") && !object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                            movie.setReleaseDate(object.getString("first_air_date"));

                        // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
                        if (isAdded()) {
                            if (object.has("poster_path") && !object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                        }

                        if (isAdded()) {
                            if (object.has("profile_path") && !object.getString("profile_path").equals("null") && !object.getString("profile_path").isEmpty())
                                movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("profile_path"));
                        }

                        if (object.has("media_type") && !object.getString("media_type").isEmpty())
                            movie.setMediaType(object.getString("media_type"));

                        searchList.add(movie);
                    }

                    return true;
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

        protected void onPostExecute(Boolean result) {
            // est ajouté vérifie si nous sommes toujours sur la même vue, si nous ne faisons pas cette vérification, le programme va planter
            if (isAdded()) {
                if (!result) {
                    Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    backState = 0;
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            toastLoadingMore.cancel();
                        }
                    });
                    backState = 1;
                    if (!searchList.isEmpty())
                        searchAdapter.notifyDataSetChanged();
                    else
                        Toast.makeText(getActivity(), R.string.noResults, Toast.LENGTH_LONG).show();

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
                    loading = false;
                    final JSONAsyncTask request = new JSONAsyncTask();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute(MovieDB.url + "search/multi?query=" + getQuery() + "?&api_key=" + MovieDB.key + "&page=" + currentPage).get(10000, TimeUnit.MILLISECONDS);
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

    /**
      * Lancé lorsque l'utilisateur appuie sur le bouton Envoyer.
      *
      * @param query la requête à rechercher
      */
    public void setQuery(String query) {
        this.query = query.replaceAll("[\\s%\"^#<>{}\\\\|`]", "%20");
        search();
    }

    public String getQuery() {
        return query;
    }

    /**
      * Fait une nouvelle requête au serveur avec la requête donnée.
      */
    public void search() {
        if (getActivity() != null) {
            listView = (AbsListView) getActivity().findViewById(R.id.movieslist);
            searchList = new ArrayList<>();
            searchAdapter = new SearchAdapter(getActivity(), R.layout.row, searchList);
            listView.setAdapter(searchAdapter);
            endlessScrollListener = new EndlessScrollListener();
            listView.setOnScrollListener(endlessScrollListener);
            final JSONAsyncTask request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        request.execute(MovieDB.url + "search/multi?query=" + getQuery() + "?&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        request.cancel(true);
                        // nous abandonnons la requête http, sinon cela causera des problèmes et ralentira la connexion plus tard
                        if (conn != null)
                            conn.disconnect();
                        toastLoadingMore.cancel();
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
            send.putInt("totalPages", totalPages);
            send.putString("title", getTitle());
            if (backState == 1) {
                send.putInt("backState", 1);
                send.putParcelableArrayList("listData", searchList);
                // utilisé pour restaurer les variables de l'écouteur de défilement
                send.putInt("currentPage", endlessScrollListener.getCurrentPage());
                send.putInt("oldCount", endlessScrollListener.getOldCount());
                send.putBoolean("loading", endlessScrollListener.getLoading());
                // Enregistrer la position de défilement
                if (listView != null) {
                    Parcelable listState = listView.onSaveInstanceState();
                    send.putParcelable("listViewScroll", listState);
                }
            } else {
                send.putInt("backState", 0);
                send.putString("query", query);
            }
            outState.putBundle("save", send);
        }
    }

    /**
       * Lancé lorsque le fragment est détruit.
       */
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null)
            ((MainActivity) getActivity()).setSearchViewCount(false);
    }

}