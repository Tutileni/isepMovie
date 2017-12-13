
package com.simao.isepmovies;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CancellationException;

import com.simao.isepmovies.adapter.SearchDB;
import com.simao.isepmovies.controller.CastDetails;
import com.simao.isepmovies.controller.GalleryList;
import com.simao.isepmovies.controller.GenresList;
import com.simao.isepmovies.controller.MovieDetails;
import com.simao.isepmovies.controller.MovieSlideTab;
import com.simao.isepmovies.controller.SearchList;
import com.simao.isepmovies.controller.TrailerList;

/**
  * Le programme de la classe principale commence à partir d'ici.
  */
public class MainActivity extends AppCompatActivity {
    private final int CacheSize = 52428800; // 50MB
    private final int MinFreeSpace = 2048; // 2MB
    private static final long maxMem = Runtime.getRuntime().maxMemory();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // Titre de nav
    private CharSequence mDrawerTitle;

    // utilisé pour stocker le titre de l'application
    private CharSequence mTitle;

    // éléments de menu de diapositives
    private String[] navMenuTitles;

    private MovieSlideTab movieSlideTab = new MovieSlideTab();
    private About about = new About();
    private GenresList genresList = new GenresList();
    private SearchList searchList = new SearchList();

    private SearchView searchView;
    private MenuItem searchViewItem;
    private ImageLoader imageLoader;

    // Créer une recherche Voir les auditeurs
    private SearchViewOnQueryTextListener searchViewOnQueryTextListener = new SearchViewOnQueryTextListener();
    private onSearchViewItemExpand onSearchViewItemExpand = new onSearchViewItemExpand();
    private SearchSuggestionListener searchSuggestionListener = new SearchSuggestionListener();
    private SearchDB searchDB;
    private Toolbar toolbar;
    private int oldPos = -1;
    private boolean isDrawerOpen = false;
    private DisplayImageOptions optionsWithFade;
    private DisplayImageOptions optionsWithoutFade;
    private DisplayImageOptions backdropOptionsWithFade;
    private DisplayImageOptions backdropOptionsWithoutFade;
    private int currentMovViewPagerPos;
    private boolean reAttachMovieFragments;
    private TrailerList trailerListView;
    private GalleryList galleryListView;
    private MovieDetails movieDetailsFragment;
    private MovieDetails movieDetailsSimFragment;
    private boolean saveInMovieDetailsSimFragment;
    private CastDetails castDetailsFragment;
    private OnDrawerBackButton onDrawerBackButton = new OnDrawerBackButton();
    private Bundle movieDetailsInfoBundle;
    private Bundle movieDetailsCastBundle;
    private Bundle movieDetailsOverviewBundle;
    private Bundle castDetailsInfoBundle;
    private Bundle castDetailsCreditsBundle;
    private Bundle castDetailsBiographyBundle;
    private ArrayList<Bundle> movieDetailsBundle = new ArrayList<>();
    private ArrayList<Bundle> castDetailsBundle = new ArrayList<>();
    private boolean restoreMovieDetailsAdapterState;
    private boolean restoreMovieDetailsState;
    private int currOrientation;
    private boolean orientationChanged;
    private boolean searchViewTap;
    private boolean searchViewCount;
    private static int searchMovieDetails;
    private static int searchCastDetails;
    private int lastVisitedSimMovie;
    private int lastVisitedMovieInCredits;
    private HttpURLConnection conn;
    private SimpleCursorAdapter searchAdapter;
    private String query;
    private JSONAsyncTask request;
    private SearchImgLoadingListener searchImgLoadingListener;
    private int iconMarginConstant;
    private int iconMarginLandscape;
    private int iconConstantSpecialCase;
    private int threeIcons;
    private int threeIconsToolbar;
    private int twoIcons;
    private int twoIconsToolbar;
    private int oneIcon;
    private int oneIconToolbar;
    private boolean phone;
    private DateFormat dateFormat;

    /**
      * Configurez d'abord le Universal Image Downloader,
      * puis nous définissons la mise en page principale comme étant activity_main.xml
      * et nous ajoutons les éléments du menu slide.
      *
      * @param savedInstanceState Si cette valeur n'est pas nulle, cette activité est en cours de reconstruction à partir d'un état précédemment enregistré comme indiqué ici.
      */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();

        // charger les éléments de menu de diapositives
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.drawer_header, null, false);
        ImageView drawerBackButton = (ImageView) header.findViewById(R.id.drawerBackButton);
        drawerBackButton.setOnClickListener(onDrawerBackButton);
        mDrawerList.addHeaderView(header);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        // configuration de l'adaptateur de liste de nav
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, R.id.title, navMenuTitles));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.bringToFront();
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this,  /* host Activity */
                mDrawerLayout,  /*Objet DrawerLayout */
                toolbar,
                R.string.app_name, //navigation ouvert - description pour l'accessibilité
                R.string.app_name // fermeture de navigation - description pour l'accessibilité
        ) {
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // appelle onPrepareOptionsMenu () pour afficher la vue de recherche
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // appelle onPrepareOptionsMenu () pour masquer la vue de recherche
                invalidateOptionsMenu();
                syncState();
            }

            // met à jour le titre, la transparence de la barre d'outils et la vue de recherche
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > .55 && !isDrawerOpen) {
                    // mDrawerTitle est le titre de l'application
                    getSupportActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu();
                    isDrawerOpen = true;
                } else if (slideOffset < .45 && isDrawerOpen) {
                    // mTitre est le titre de la vue actuelle, peut être des films, des émissions de télévision ou des titres de films
                    getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu();
                    isDrawerOpen = false;
                }
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Obtenir le titre de la barre d'action pour définir le remplissage
        TextView titleTextView = null;

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        if (titleTextView != null) {
            float scale = getResources().getDisplayMetrics().density;
            titleTextView.setPadding((int) scale * 15, 0, 0, 0);
        }

        phone = getResources().getBoolean(R.bool.portrait_only);

        searchDB = new SearchDB(getApplicationContext());

        if (savedInstanceState == null) {
            // Vérifie l'orientation et verrouille le portrait si nous sommes au téléphone
            if (phone) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            // sur la première vue d'affichage pour le premier élément de nav
            displayView(1);

            // Options et configuration Universal Loader.
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheInMemory(false)
                    .showImageOnLoading(R.drawable.placeholder_default)
                    .showImageForEmptyUri(R.drawable.placeholder_default)
                    .showImageOnFail(R.drawable.placeholder_default)
                    .cacheOnDisk(true)
                    .build();
            Context context = this;
            File cacheDir = StorageUtils.getCacheDirectory(context);
            // Crée une configuration globale et initialise ImageLoader avec cette configuration
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                    .defaultDisplayImageOptions(options)
                    .build();
            ImageLoader.getInstance().init(config);


            // Vérifier la taille du cache
            long size = 0;
            File[] filesCache = cacheDir.listFiles();
            for (File file : filesCache) {
                size += file.length();
            }
            if (cacheDir.getUsableSpace() < MinFreeSpace || size > CacheSize) {
                ImageLoader.getInstance().getDiskCache().clear();
                searchDB.cleanSuggestionRecords();
            }


        } else {
            oldPos = savedInstanceState.getInt("oldPos");
            currentMovViewPagerPos = savedInstanceState.getInt("currentMovViewPagerPos");
            restoreMovieDetailsState = savedInstanceState.getBoolean("restoreMovieDetailsState");
            restoreMovieDetailsAdapterState = savedInstanceState.getBoolean("restoreMovieDetailsAdapterState");
            movieDetailsBundle = savedInstanceState.getParcelableArrayList("movieDetailsBundle");
            castDetailsBundle = savedInstanceState.getParcelableArrayList("castDetailsBundle");
            currOrientation = savedInstanceState.getInt("currOrientation");
            lastVisitedSimMovie = savedInstanceState.getInt("lastVisitedSimMovie");
            lastVisitedMovieInCredits = savedInstanceState.getInt("lastVisitedMovieInCredits");
            saveInMovieDetailsSimFragment = savedInstanceState.getBoolean("saveInMovieDetailsSimFragment");


            FragmentManager fm = getFragmentManager();
            // empêche le bogue suivant: aller à l'aperçu de la galerie -> orientation swap ->
            // aller à la liste des films -> orientation swap -> barre d'action buggée
            // donc si nous ne sommes pas sur l'aperçu de la galerie, nous montrons la barre d'outils
            if (fm.getBackStackEntryCount() == 0 || !fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName().equals("galleryList")) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (getSupportActionBar() != null && !getSupportActionBar().isShowing())
                            getSupportActionBar().show();
                    }
                });
            }
        }

        // Obtenir une référence pour l'imageLoader
        imageLoader = ImageLoader.getInstance();

        // Options utilisées pour l'image de fond dans les détails du film et dans la galerie
        optionsWithFade = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(500))
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.color.black)
                .showImageForEmptyUri(R.color.black)
                .showImageOnFail(R.color.black)
                .cacheOnDisk(true)
                .build();
        optionsWithoutFade = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.color.black)
                .showImageForEmptyUri(R.color.black)
                .showImageOnFail(R.color.black)
                .cacheOnDisk(true)
                .build();

        backdropOptionsWithFade = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(500))
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.drawable.placeholder_backdrop)
                .showImageForEmptyUri(R.drawable.placeholder_backdrop)
                .showImageOnFail(R.drawable.placeholder_backdrop)
                .cacheOnDisk(true)
                .build();
        backdropOptionsWithoutFade = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.drawable.placeholder_backdrop)
                .showImageForEmptyUri(R.drawable.placeholder_backdrop)
                .showImageOnFail(R.drawable.placeholder_backdrop)
                .cacheOnDisk(true)
                .build();

        trailerListView = new TrailerList();
        galleryListView = new GalleryList();

        if (currOrientation != getResources().getConfiguration().orientation)
            orientationChanged = true;

        currOrientation = getResources().getConfiguration().orientation;

        iconConstantSpecialCase = 0;
        if (phone) {
            iconMarginConstant = 0;
            iconMarginLandscape = 0;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            if (width <= 480 && height <= 800)
                iconConstantSpecialCase = -70;

            // utilisé dans MovieDetails, CastDetails, onMoreIconClick
            // pour vérifier si l'animation doit être en haut ou en bas
            threeIcons = 128;
            threeIconsToolbar = 72;
            twoIcons = 183;
            twoIconsToolbar = 127;
            oneIcon = 238;
            oneIconToolbar = 182;
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                iconMarginConstant = 232;
                iconMarginLandscape = 300;

                threeIcons = 361;
                threeIconsToolbar = 295;
                twoIcons = 416;
                twoIconsToolbar = 351;
                oneIcon = 469;
                oneIconToolbar = 407;
            } else {
                iconMarginConstant = 82;
                iconMarginLandscape = 0;

                threeIcons = 209;
                threeIconsToolbar = 146;
                twoIcons = 264;
                twoIconsToolbar = 200;
                oneIcon = 319;
                oneIconToolbar = 256;
            }

        }

        dateFormat = android.text.format.DateFormat.getDateFormat(this);
    }

    /**
      * Pops le dernier élément de la pile arrière.
      * Si searchView est ouvert, il le cache.
      * reAttachMovieFragments recrée nos fragments en raison d'un bug dans le viewPager
      * restoreMovieDetailsState -> lorsque nous appuyons sur le bouton retour, nous voulons restaurer notre précédent (le cas échéant)
      * Etat sauvegardé pour le fragment actuel. Nous utilisons backStack personnalisé car l'original n'enregistre pas l'état du fragment.
      */
    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();

        if (mDrawerLayout.isDrawerOpen(mDrawerList))
            mDrawerLayout.closeDrawer(mDrawerList);
        else {
            if (searchViewItem.isActionViewExpanded())
                searchViewItem.collapseActionView();

            else if (fm.getBackStackEntryCount() > 0) {
                String backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
                if (backStackEntry.equals("movieList"))
                    reAttachMovieFragments = true;

                if (backStackEntry.equals("searchList:1"))
                    reAttachMovieFragments = true;

                restoreMovieDetailsState = true;
                restoreMovieDetailsAdapterState = false;
                if (orientationChanged)
                    restoreMovieDetailsAdapterState = true;

                fm.popBackStack();
            } else {
                super.onBackPressed();
            }
        }

    }

    /**
      * Cette méthode est déclenchée lorsque l'activité est en pause.
      * Par exemple si nous minimisons notre application.
      */
    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    /**
      * Cette méthode est déclenchée lorsque l'activité est reprise.
      */
    @Override
    protected void onResume() {
        super.onResume();
    }




    /**
      * Faites glisser l'élément de menu cliquez sur l'écouteur.
      * Lancé lorsque vous cliquez sur l'élément dans le menu Diapositive.
      */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // affichage de l'élément de tiroir de nav sélectionné
            displayView(position);
        }
    }

    /**
      * Initialiser le contenu du menu des options standard de l'activité.
      * Ceci n'est appelé qu'une seule fois, la première fois que le menu des options est affiché.
      *
      * @param menu le menu d'options dans lequel nous plaçons nos articles.
      * @return Vous devez retourner vrai pour que le menu soit affiché; Si vous retournez faux, il ne sera pas affiché.
      */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchViewItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(searchViewOnQueryTextListener);
        searchView.setOnSuggestionListener(searchSuggestionListener);

        // Associer une configuration interrogeable avec SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchViewItemC =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchViewItemC.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        String[] from = {SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2};
        int[] to = {R.id.posterPath, R.id.title, R.id.info};
        searchAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.suggestionrow, null, from, to, 0) {
            @Override
            public void changeCursor(Cursor cursor) {
                super.swapCursor(cursor);
            }
        };
        searchViewItemC.setSuggestionsAdapter(searchAdapter);

        MenuItemCompat.setOnActionExpandListener(searchViewItem, onSearchViewItemExpand);


        return true;
    }

    /**
      * Ce crochet est appelé chaque fois qu'un élément de notre menu d'options est sélectionné.
      *
      * @param item L'élément de menu sélectionné.
      * @return Retourne false pour permettre le traitement normal du menu, true pour le consommer ici.
      */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // bascule le tiroir de navigation en sélectionnant l'icône / le titre de l'application barre d'action
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Gérer les actions de la barre d'actions, cliquez sur
        switch (item.getItemId()) {
            case R.id.search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
      * Appelé lorsque invalidateOptionsMenu () est déclenché
      * Préparez le menu des options standard de l'écran à afficher.
      * Ceci est appelé juste avant que le menu soit affiché, chaque fois qu'il est affiché.
      * Vous pouvez utiliser cette méthode pour activer / désactiver efficacement des éléments ou autrement modifier dynamiquement le contenu.
      *
      * @param menu Le menu options affiché en dernier ou initialisé par onCreateOptionsMenu ().
      * @return Vous devez retourner vrai pour que le menu soit affiché; Si vous retournez faux, il ne sera pas affiché.
      * Si le tiroir de navigation est ouvert, nous cachons la vue de recherche.
      * Si nous avons
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isDrawerOpen)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 4)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 3)
            menu.findItem(R.id.search).setVisible(false);
        else menu.findItem(R.id.search).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }


    /**
      * Affiche la vue de fragment pour l'élément sélectionné dans le menu de diapositives.
      *
      * La position @param est la position que nous avons sélectionnée.
      */
    private void displayView(int position) {
        if (position != 0) {
            FragmentManager fm = getFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment fragment = null;
            searchMovieDetails = 0;
            searchCastDetails = 0;
            searchViewCount = false;
            resetMovieDetailsBundle();
            resetCastDetailsBundle();

            switch (position) {
                // Case 0 est l'en-tête, nous ne voulons rien faire avec.
                case 1:
                    reAttachMovieFragments = true;
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    }
                    fragment = movieSlideTab;
                    break;
                case 2:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    }
                    fragment = getFragmentManager().findFragmentByTag("genres");
                    if (fragment == null)
                        fragment = genresList;
                    if (genresList.getBackState() == 0)
                        genresList.updateList();
                    break;

                case 3:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    }
                    fragment = about;
                    break;

                default:
                    break;
            }
            oldPos = position;
            if (fragment != null) {
                fm.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .commit();
                // met à jour l'élément et le titre sélectionnés, puis ferme
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(navMenuTitles[position - 1]);
                mDrawerLayout.closeDrawer(mDrawerList);
                try {
                    movieSlideTab.showInstantToolbar();
                } catch (NullPointerException e) {
                }
                System.gc();
            }
        } else {
            mDrawerList.setItemChecked(oldPos, true);
        }
    }

    /**
      * Nous utilisons cette méthode pour mettre à jour le titre de la barre d'action.
      *
      * @param titre le nouveau titre.
      * Si le searchView est ouvert, nous n'appelons pas invalidateOptionsMenu () sinon cache searchView.
      */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(mTitle);

        if (!searchViewTap) {
            invalidateOptionsMenu();
        } else searchViewTap = false;
    }

    /**
      * Appelé lorsque le démarrage de l'activité est terminé
      * Lorsque vous utilisez ActionBarDrawerToggle, vous devez l'appeler pendant
      * onPostCreate () et onConfigurationChanged () ...
      *
      * @param savedInstanceState Si l'activité est en cours de réinitialisation après avoir été fermée
      * cet ensemble contient les données qu'il a récemment fournies dans onSaveInstanceState (Bundle). Note: Sinon, c'est null.
      */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
      * Appelée par le système lorsque la configuration de l'appareil change pendant que votre activité est en cours.
      * Notez que ceci ne sera appelé que si vous avez sélectionné les configurations que vous souhaitez gérer
      * avec l'attribut configChanges dans votre manifeste.
      *
      * @param newConfig La nouvelle configuration de l'appareil.
      */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
      * Classe qui écoute les événements SearchView.
      */
    private class SearchViewOnQueryTextListener implements SearchView.OnQueryTextListener {

        /**
          * Appelé lorsque le texte de la requête est modifié par l'utilisateur.
          *
          * @param newText le nouveau contenu du champ de texte de la requête.
          * @return false si le SearchView doit effectuer l'action par défaut de montrer des suggestions si disponibles,
          * true si l'action a été gérée par l'écouteur.
          */
        @Override
        public boolean onQueryTextChange(String newText) {
            query = newText;
            query = query.replaceAll("[\\s%\"^#<>{}\\\\|`]", "%20");

            if (query.length() > 1) {
                new Thread(new Runnable() {
                    public void run() {
                        try {

                            if (request != null)
                                request.cancel(true);

                            if (conn != null)
                                conn.disconnect();

                            request = new JSONAsyncTask();
                            request.execute(MovieDB.url + "search/multi?query=" + query + "?&api_key=" + MovieDB.key);
                            request.setQuery(query);
                        } catch (CancellationException e) {
                            if (request != null)
                                request.cancel(true);
                            // nous abandonnons la requête http, sinon cela causera des problèmes et ralentira la connexion plus tard
                            if (conn != null)
                                conn.disconnect();
                        }
                    }
                }).start();
            } else {
                String[] selArgs = {query};
                searchDB.cleanAutoCompleteRecords();
                Cursor c = searchDB.getSuggestions(selArgs);
                searchAdapter.changeCursor(c);
            }
            return true;
        }

        /**
          * Appelé lorsque l'utilisateur soumet la requête.
          * Cela peut être dû à une pression sur le clavier ou à l'appui sur un bouton d'envoi.
          *
          * @param interroge le texte de la requête à soumettre
          * @return true si la requête a été traitée par l'écouteur, false pour permettre à SearchView d'effectuer l'action par défaut.
          * Nous mettons à jour la requête, cachons le clavier et ajoutons la requête aux suggestions.
          */
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchList.setQuery(query);
            searchView.clearFocus();
            return true;
        }
    }

    /**
      * Classe qui écoute lorsque nous tapons sur le SearchView.
      */
    public class onSearchViewItemExpand implements MenuItemCompat.OnActionExpandListener {
        FragmentManager fm = getFragmentManager();

        /**
          * Appelé lorsque l'icône searchView est sélectionnée.
          *
          * @param item notre rechercheView;
          * @return vrai si l'item doit se développer, false si l'expansion doit être supprimée.
          * Si nous avons navigué parmi les éléments de la liste des résultats de la vue de recherche et que nous tapons de nouveau sur,
          * nous nettoyons notre backStack.
          */
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {

            // recherche la clé de vue
            searchViewTap = true;

            if (searchMovieDetails > 0)
                clearMovieDetailsBackStack();

            if (searchCastDetails > 0)
                clearCastDetailsBackStack();


            // vérifie si nous sommes déjà dans la vue de recherche pour éviter d'ajouter deux fois dans la pile arrière
            if (fm.getBackStackEntryCount() == 0 || !fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName().startsWith("searchList")) {
                // vérifie si la vue de recherche a été créée, si elle est créée, cette méthode la fait sortir de la pile arrière
                // aussi cela efface l'historique de la pile jusqu'à la liste de recherche
                boolean fragmentPopped = false;
                if (fm.popBackStackImmediate("searchList:1", 0))
                    fragmentPopped = true;
                if (fm.popBackStackImmediate("searchList:2", 0))
                    fragmentPopped = true;
                if (fm.popBackStackImmediate("searchList:3", 0))
                    fragmentPopped = true;

                // si getId == 0 cela signifie que le fragment a été détaché
                // nous avons seulement besoin du fragment actif actuel pour sauvegarder son état
                if (!fragmentPopped) {
                    if (movieDetailsFragment != null && movieDetailsFragment.getId() != 0) {
                        // check if the movie is already in our backStack
                        if (movieDetailsBundle.size() > 0) {
                            if (!getSupportActionBar().getTitle().equals(movieDetailsBundle.get(movieDetailsBundle.size() - 1).getString("title"))) {
                                movieDetailsFragment.setAddToBackStack(true);
                                movieDetailsFragment.onSaveInstanceState(new Bundle());
                            }
                        } else {
                            movieDetailsFragment.setAddToBackStack(true);
                            movieDetailsFragment.onSaveInstanceState(new Bundle());
                        }
                    }
                    if (castDetailsFragment != null && castDetailsFragment.getId() != 0) {
                        // Vérifie si l'acteur est déjà dans notre backStack
                        if (castDetailsBundle.size() > 0) {
                            if (!getSupportActionBar().getTitle().equals(castDetailsBundle.get(castDetailsBundle.size() - 1).getString("title"))) {
                                castDetailsFragment.setAddToBackStack(true);
                                castDetailsFragment.onSaveInstanceState(new Bundle());
                            }
                        } else {
                            castDetailsFragment.setAddToBackStack(true);
                            castDetailsFragment.onSaveInstanceState(new Bundle());
                        }
                    }

                    FragmentTransaction transaction = fm.beginTransaction();
                    searchList.setTitle(getResources().getString(R.string.search_title));
                    transaction.replace(R.id.frame_container, searchList);
                    // ajoute la transaction en cours à la pile arrière:
                    transaction.addToBackStack("searchList:" + oldPos);
                    transaction.commit();
                }
            }
            return true;
        }

        /**
          * Appelé lorsque vous cliquez sur le bouton Précédent ou par la méthode CollapseSearchView ().
          *
          * @param item notre rechercheView;
          * @return true si l'élément doit être réduit, false si l'effondrement doit être supprimé.
          */
        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            return true;
        }
    }

    /**
      * Lancé depuis le contrôleur SearchList lorsque vous appuyez sur un film.
      * Masque la zone de texte searchView.
      */
    public void collapseSearchView() {
        searchViewItem.collapseActionView();
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Nous mettons à jour la valeur de texte d'un TextView, à partir de runOnUiThread () car nous ne pouvons pas le mettre à jour à partir d'une tâche asynchrone.
      *
      * @param text le TextView à mettre à jour.
      * @param value la nouvelle valeur de texte.
      */
    public void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Nous mettons à jour la valeur de texte d'un TextView, à partir de runOnUiThread () car nous ne pouvons pas le mettre à jour à partir d'une tâche asynchrone.
      *
      * @param text le TextView à mettre à jour.
      * @param value la nouvelle valeur de texte.
      */
    public void setTextFromHtml(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(Html.fromHtml(value));
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Nous utilisons notre imageLoader pour afficher l'image sur la vue de l'image donnée.
      * runOnUiThread () est appelé car nous ne pouvons pas le mettre à jour à partir d'une tâche asynchrone.
      *
      * @param img ImageView nous affichons l'image.
      * @param url l'URL à partir de laquelle nous affichons l'image.
      * R.string.imageSize définit la taille de nos images.
      */
    public void setImage(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + url, img);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Nous avons mis une balise url sur l'imageView. Donc, lorsque nous tapons dessus, nous savons quelle URL charger.
      * runOnUiThread () est appelé car nous ne pouvons pas le mettre à jour à partir d'une tâche asynchrone.
      *
      * @param img ImageView nous affichons l'image.
      * @param url l'url pour mettre la balise.
      * R.string.imageSize définit la taille de nos images.
      */
    public void setImageTag(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img.setTag(url);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Nous utilisons notre imageLoader pour afficher l'image sur la vue de l'image donnée.
      * runOnUiThread () est appelé car nous ne pouvons pas le mettre à jour à partir d'une tâche asynchrone.
      *
      * @param img ImageView nous affichons l'image.
      * @param url l'url pour mettre la balise.
      * R.string.backDropImgSize définit la taille de nos images backDrop.
      * Si nous chargeons l'image pour la première fois, nous montrons un effet de fondu, sinon aucun effet.
      */
    public void setBackDropImage(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imageLoader.getDiskCache().get(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url).exists())
                    imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url, img, backdropOptionsWithoutFade);
                else
                    imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url, img, backdropOptionsWithFade);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Nous utilisons notre imageLoader pour afficher l'image sur la vue de l'image donnée.
      * runOnUiThread () est appelé car nous ne pouvons pas le mettre à jour à partir d'une tâche asynchrone.
      *
      * @param img ImageView nous affichons l'image.
      * @param url l'url pour mettre la balise.
      * R.string.backDropImgSize définit la taille de nos images backDrop.
      * Si nous chargeons l'image pour la première fois, nous montrons un effet de fondu, sinon aucun effet.
      */
    public void setRatingBarValue(final RatingBar ratingBar, final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setRating(value);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      *
      * @param met en page la disposition que nous cachons.
      */
    public void hideLayout(final ViewGroup layout) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.setVisibility(View.GONE);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      *
      * @param voir la vue que nous cachons.
      */
    public void hideView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      *
      * @param voir la vue que nous montrons.
      */
    public void showView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails
      *
      * @param textView le TextView que nous cachons.
      */
    public void hideTextView(final TextView textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.GONE);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      *
      * @param ratingBar la RatingBar que nous cachons.
      */
    public void hideRatingBar(final RatingBar ratingBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setVisibility(View.GONE);
            }
        });
    }

    /**
      * Cette méthode est utilisée dans MovieDetails, CastDetails.
      * Rend une vue invisible.
      *
      * @param voir la vue que nous rendons invisible.
      */
    public void invisibleView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
      * Nous l'utilisons pour obtenir l'élément Viewpager actuel dans les films.
      */
    public int getCurrentMovViewPagerPos() {
        return currentMovViewPagerPos;
    }

    /**
      * Nous l'utilisons pour définir l'élément Viewpager actuel dans les films.
      */
    public void setCurrentMovViewPagerPos(int currentMovViewPagerPos) {
        this.currentMovViewPagerPos = currentMovViewPagerPos;
    }
    /**
      * @return notre activity_main.xml
      */
    public DrawerLayout getMDrawerLayout() {
        return mDrawerLayout;
    }

    /**
      * @return vrai si notre ViewPager dans les films devrait reAttacher les fragments.
      */
    public boolean getReAttachMovieFragments() {
        return reAttachMovieFragments;
    }

    /**
      * Définir si nous devions reattacher les fragments dans le ViewPager dans les films.
      */
    public void setReAttachMovieFragments(boolean reAttachMovieFragments) {
        this.reAttachMovieFragments = reAttachMovieFragments;
    }
    /**
      * Renvoyé lorsque l'activité est recréée.
      *
      * @param outState Bundle dans lequel placer votre état enregistré.
      */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("oldPos", oldPos);
        outState.putInt("currentMovViewPagerPos", currentMovViewPagerPos);
        outState.putBoolean("restoreMovieDetailsAdapterState", restoreMovieDetailsAdapterState);
        outState.putBoolean("restoreMovieDetailsState", restoreMovieDetailsState);
        outState.putParcelableArrayList("movieDetailsBundle", movieDetailsBundle);
        outState.putParcelableArrayList("castDetailsBundle", castDetailsBundle);
        outState.putInt("currOrientation", currOrientation);
        outState.putInt("lastVisitedSimMovie", lastVisitedSimMovie);
        outState.putInt("lastVisitedMovieInCredits", lastVisitedMovieInCredits);
        outState.putBoolean("saveInMovieDetailsSimFragment", saveInMovieDetailsSimFragment);
    }

    /**
      * Méthode qui renvoie la taille de tas maximale de l'appareil.
      */
    public static long getMaxMem() {
        return maxMem;
    }

    /**
      * Méthode qui renvoie la vue TrailerList.
      */
    public TrailerList getTrailerListView() {
        return trailerListView;
    }

    /**
      * Méthode qui retourne la vue GalleryList.
      */
    public GalleryList getGalleryListView() {
        return galleryListView;
    }

    /**
      * Méthode qui définit notre Fragment movieDetails.
      */
    public void setMovieDetailsFragment(MovieDetails movieDetailsFragment) {
        this.movieDetailsFragment = movieDetailsFragment;
    }

    /**
      * Méthode qui renvoie notre Fragment MovieDetails.
      */
    public MovieDetails getMovieDetailsFragment() {
        return movieDetailsFragment;

    }

    /**
      * Méthode qui définit notre fragment CastDetails.
      */
    public void setCastDetailsFragment(CastDetails castDetailsFragment) {
        this.castDetailsFragment = castDetailsFragment;
    }

    /**
      * Méthode qui renvoie notre fragment CastDetails.
      */
    public CastDetails getCastDetailsFragment() {
        return castDetailsFragment;
    }
    /**
      * Récupère la config pour l'imageLoader avec l'effet de fondu.
      */
    public DisplayImageOptions getOptionsWithFade() {
        return optionsWithFade;
    }

    /**
      * Récupère la configuration pour l'imageLoader sans effet de fondu.
      */
    public DisplayImageOptions getOptionsWithoutFade() {
        return optionsWithoutFade;
    }


    public class OnDrawerBackButton implements View.OnClickListener {
        public OnDrawerBackButton() {
        }

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    }

    /**
      * Récupère l'instance enregistrée dans notre onglet Infos sur les détails du film.
      */
    public Bundle getMovieDetailsInfoBundle() {
        return movieDetailsInfoBundle;
    }

    /**
      * Définit l'instance enregistrée dans notre onglet Informations sur les détails du film. Très probablement le définit à null.
      */
    public void setMovieDetailsInfoBundle(Bundle movieDetailsInfoBundle) {
        this.movieDetailsInfoBundle = movieDetailsInfoBundle;
    }

    /**
      * Obtient l'instance enregistrée dans notre onglet Casting de détails de film.
      */
    public Bundle getMovieDetailsCastBundle() {
        return movieDetailsCastBundle;
    }

    /**
      * Définit l'instance enregistrée dans notre onglet Casting Détails du film. Très probablement le définit à null.
      */
    public void setMovieDetailsCastBundle(Bundle movieDetailsCastBundle) {
        this.movieDetailsCastBundle = movieDetailsCastBundle;
    }

    /**
      * Récupère l'instance enregistrée dans notre onglet Infos sur la présentation du film.
      */
    public Bundle getMovieDetailsOverviewBundle() {
        return movieDetailsOverviewBundle;
    }

    /**
      * Définit l'instance enregistrée dans notre onglet Présentation des détails du film. Très probablement le définit à null.
      */
    public void setMovieDetailsOverviewBundle(Bundle movieDetailsOverviewBundle) {
        this.movieDetailsOverviewBundle = movieDetailsOverviewBundle;
    }

    /**
      * Récupère l'instance enregistrée dans l'onglet Informations sur les détails de diffusion.
      */
    public Bundle getCastDetailsInfoBundle() {
        return castDetailsInfoBundle;
    }

    /**
      * Définit l'instance enregistrée dans notre onglet Informations sur les détails de diffusion. Très probablement le définit à null.
      */
    public void setCastDetailsInfoBundle(Bundle castDetailsInfoBundle) {
        this.castDetailsInfoBundle = castDetailsInfoBundle;
    }

    /**
      * Obtient l'instance enregistrée dans notre onglet Cast Details Credits.
      */
    public Bundle getCastDetailsCreditsBundle() {
        return castDetailsCreditsBundle;
    }

    /**
      * Définit l'instance enregistrée dans notre onglet Cast Details Credits. Très probablement le définit à null.
      */
    public void setCastDetailsCreditsBundle(Bundle castDetailsCreditsBundle) {
        this.castDetailsCreditsBundle = castDetailsCreditsBundle;
    }

    /**
      * Obtient l'instance enregistrée dans notre onglet Biographie des détails de diffusion.
      */
    public Bundle getCastDetailsBiographyBundle() {
        return castDetailsBiographyBundle;
    }

    /**
      * Définit l'instance enregistrée dans notre onglet Biographie des détails de diffusion. Très probablement le définit à null.
      */
    public void setCastDetailsBiographyBundle(Bundle castDetailsBiographyBundle) {
        this.castDetailsBiographyBundle = castDetailsBiographyBundle;
    }
    /**
      * Méthode qui ajoute Movie Details savedState à notre ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public void addMovieDetailsBundle(Bundle movieDetailsBundle) {
        this.movieDetailsBundle.add(movieDetailsBundle);
    }

    /**
      * Méthode qui supprime Movie Details savedState à notre ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public void removeMovieDetailsBundle(int pos) {
        movieDetailsBundle.remove(pos);
    }

    /**
      * Méthode qui réinitialise Movie Details ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public void resetMovieDetailsBundle() {
        movieDetailsBundle = new ArrayList<>();
    }

    /**
      * Méthode qui obtient Movie Details savedState à partir de notre ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public ArrayList<Bundle> getMovieDetailsBundle() {
        return movieDetailsBundle;
    }

    /**
      * Méthode qui ajoute Cast Details savedState à notre ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public void addCastDetailsBundle(Bundle castDetailsBundle) {
        this.castDetailsBundle.add(castDetailsBundle);
    }

    /**
      * Méthode qui supprime les détails de cast savedState à notre ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public void removeCastDetailsBundle(int pos) {
        castDetailsBundle.remove(pos);
    }

    /**
      * Méthode qui réinitialise Cast Details ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public void resetCastDetailsBundle() {
        castDetailsBundle = new ArrayList<>();
    }

    /**
      * Méthode qui obtient Cast Détails sauvegardésState de notre ArrayList.
      * Nous l'utilisons pour notre navigation arrière.
      */
    public ArrayList<Bundle> getCastDetailsBundle() {
        return castDetailsBundle;
    }

    /**
      * Réglez ceci à true si nous devons restaurer nos détails de film savedState lorsque nous appuyons sur le bouton de retour.
      */
    public void setRestoreMovieDetailsState(boolean restoreMovieDetailsState) {
        this.restoreMovieDetailsState = restoreMovieDetailsState;
    }

    /**
      * Vrai si nous devrions restaurer nos détails de film savedState lorsque nous appuyons sur le bouton Retour.
      */
    public boolean getRestoreMovieDetailsState() {
        return restoreMovieDetailsState;
    }

    /**
      * Réglez ceci sur true si nous devons restaurer notre adaptateur de détails de film savedState lorsque nous revenons en arrière.
      */
    public void setRestoreMovieDetailsAdapterState(boolean restoreMovieDetailsAdapterState) {
        this.restoreMovieDetailsAdapterState = restoreMovieDetailsAdapterState;
    }

    /**
      * Vrai si nous devons restaurer notre adaptateur de détails de film savedState lorsque nous appuyons sur le bouton de retour.
      */
    public boolean getRestoreMovieDetailsAdapterState() {
        return restoreMovieDetailsAdapterState;
    }

    /**
      * Réglez sur true si l'orientation a changé.
      */
    public void setOrientationChanged(boolean orientationChanged) {
        this.orientationChanged = orientationChanged;
    }

    /**
      * Renvoie true si nous avons utilisé searchView et après que nous avons tapé sur item de searchView.
      * Le fragment qui est poussé vérifie cela. Si cela est vrai, il commence à compter incSearchMovieDetails () combien
      * les composants que nous avons poussés jusqu'ici de la searchView au dernier composant.
      * Donc, si nous cliquons sur l'icône de vue de recherche à nouveau. Nous effaçons les éléments que nous avons poussés jusqu'à la searchView,
      * mais si nous cliquons sur backButton, nous les avons stockés et les fonctions decSearchMovieDetails ou Cast ou TV sont appelées.
      */
    public boolean getSearchViewCount() {
        return searchViewCount;
    }

    /**
      * Définissez la valeur searchViewCount. Vrai si nous comptons. Vérifiez getSearchViewCount () pour plus d'informations.
      */
    public void setSearchViewCount(boolean searchViewCount) {
        this.searchViewCount = searchViewCount;
    }

    /**
      * Appelé à partir de CastDetails, MovieDetails.
      * Vérifiez si nous avons utilisé notre vue de recherche et nous comptons les composants ajoutés à backStack à partir de searchView jusqu'à la date actuelle.
      * Vérifiez getSearchViewCount () pour plus d'informations.
      */
    public void incSearchMovieDetails() {
        searchMovieDetails++;
    }

    /**
      * Décrémente la valeur lorsque nous restaurons l'état du fragment. Ceci n'est appelé que si nous avons utilisé notre vue de recherche.
      * Vérifiez getSearchViewCount () pour plus d'informations.
      */
    public void decSearchMovieDetails() {
        searchMovieDetails--;
    }

    /**
      * Identique à incSearchMovieDetails ()
      */
    public void incSearchCastDetails() {
        searchCastDetails++;
    }

    /**
      * Identique à decSearchMovieDetails ()
      */
    public void decSearchCastDetails() {
        searchCastDetails--;
    }

    /**
      * Appelé lorsque nous tapons sur l'icône searchView.
      * Efface l'item savedState que nous avons poussé de la searchView jusqu'à la fin.
      * Par exemple: MovieList -> MovieDetails -> CastDetails-> SearchView -> MovieDetails -> CastDetails -> MovieDetails.
      * Cette méthode effacera seulement MovieDetails savedState après le SearchView.
      * Donc searchMovieDetails aura 2 et effacera les deux derniers SavedDates de MovieDetails.
      */
    public void clearMovieDetailsBackStack() {
        if (movieDetailsBundle.size() > 0) {
            for (int i = 0; i < searchMovieDetails; i++) {
                removeMovieDetailsBundle(movieDetailsBundle.size() - 1);
            }
        }
        searchMovieDetails = 0;
    }

    /**
      * Identique à clearMovieDetailsBackStack ()
      */
    public void clearCastDetailsBackStack() {
        if (castDetailsBundle.size() > 0) {
            for (int i = 0; i < searchCastDetails; i++) {
                removeCastDetailsBundle(castDetailsBundle.size() - 1);
            }
        }
        searchCastDetails = 0;
    }

    /**
      * Renvoie le fragment MovieSlideTab. C'est le parent ViewPager pour la MovieList.
      */
    public MovieSlideTab getMovieSlideTab() {
        return movieSlideTab;
    }

    public void setMovieSlideTab(MovieSlideTab movieSlideTab) {
        this.movieSlideTab = movieSlideTab;
    }

    public GenresList getGenresList() {
        return genresList;
    }

    /**
      * Renvoie notre barre d'outils.
      */
    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
      * Classe qui écoute les événements de suggestion.
      */
    private class SearchSuggestionListener implements SearchView.OnSuggestionListener {

        /**
          * Appelé lorsqu'une suggestion a été cliquée.
          *
          * @param positionner la position
          * @return vrai si l'écouteur gère l'événement et veut remplacer le comportement par défaut de
          * lancer une intention ou soumettre une requête de recherche spécifiée sur cet élément. Retourne faux sinon.
          * Nous ne voulons pas lancer d'intention, alors nous nous occupons de cela.
          */
        @Override
        public boolean onSuggestionClick(int position) {
            Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
            if (searchView.getQuery().length() > 1)
                addSuggestion(cursor);

            searchList.onSuggestionClick(cursor.getInt(4), cursor.getString(5), cursor.getString(1));
            return true;
        }

        /**
          * Appelé lorsqu'une suggestion a été sélectionnée en naviguant vers celle-ci.
          *
          * @param positionne la position absolue dans la liste des suggestions.
          * @return vrai si l'écouteur gère l'événement et veut remplacer le comportement par défaut de
          * lancer une intention ou soumettre une requête de recherche spécifiée sur cet élément. Retourne faux sinon.
          */
        public boolean onSuggestionSelect(int position) {
            Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
            if (searchView.getQuery().length() > 1)
                addSuggestion(cursor);

            searchList.onSuggestionClick(cursor.getInt(4), cursor.getString(5), cursor.getString(1));
            return true;
        }

    }

    private void addSuggestion(Cursor cursor) {
        if (searchDB.getSuggestionSize() > 9) {
            searchDB.cleanSuggestionRecords();
        }

        searchDB.insertSuggestion(cursor.getInt(4), cursor.getString(1), Uri.parse(cursor.getString(3)), cursor.getString(2), cursor.getString(5));
    }

    /**
      * Appelé lorsque nous sommes sur SearchView. Nous devrions effacer notre compte.
      */
    public void clearSearchCount() {
        searchMovieDetails = 0;
        searchCastDetails = 0;
    }

    /**
      * Retourne l'ancienne position de notre tiroir de navigation.
      */
    public int getOldPos() {
        return oldPos;
    }


    public int getLastVisitedSimMovie() {
        return lastVisitedSimMovie;
    }

    public void setLastVisitedSimMovie(int lastVisitedSimMovie) {
        this.lastVisitedSimMovie = lastVisitedSimMovie;
    }
    public boolean getSaveInMovieDetailsSimFragment() {
        return saveInMovieDetailsSimFragment;
    }

    public void setSaveInMovieDetailsSimFragment(boolean saveInMovieDetailsSimFragment) {
        this.saveInMovieDetailsSimFragment = saveInMovieDetailsSimFragment;
    }

    public MovieDetails getMovieDetailsSimFragment() {
        return movieDetailsSimFragment;
    }

    public void setMovieDetailsSimFragment(MovieDetails movieDetailsSimFragment) {
        this.movieDetailsSimFragment = movieDetailsSimFragment;
    }
    /**
      * Cette classe gère la connexion à notre serveur principal.
      * Si la connexion est réussie, nous établissons nos données de liste.
      */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        private ArrayList<Integer> idsList;
        private ArrayList<String> posterPathList;
        private String queryZ;

        public void setQuery(String query) {
            this.queryZ = query;
        }

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

                    JSONObject searchData = new JSONObject(sb.toString());
                    JSONArray searchResultsArray = searchData.getJSONArray("results");
                    int length = searchResultsArray.length();
                    if (length > 10)
                        length = 10;

                    searchDB.cleanAutoCompleteRecords();
                    idsList = new ArrayList<>();
                    posterPathList = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        JSONObject object = searchResultsArray.getJSONObject(i);

                        int id = 0;
                        String title = "", posterPath = "", releaseDate = "", mediaType = "";


                        if (object.has("id") && object.getInt("id") != 0)
                            id = object.getInt("id");

                        if (object.has("title"))
                            title = object.getString("title");

                        if (object.has("name"))
                            title = object.getString("name");
                        title = title.replaceAll("'", "");

                        if (object.has("poster_path") && !object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                            posterPath = MovieDB.imageUrl + "w154" + object.getString("poster_path");


                        if (object.has("profile_path") && !object.getString("profile_path").equals("null") && !object.getString("profile_path").isEmpty())
                            posterPath = MovieDB.imageUrl + "w154" + object.getString("profile_path");

                        if (object.has("release_date") && !object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(object.getString("release_date"));
                                String formattedDate = dateFormat.format(date);
                                releaseDate = "(" + formattedDate + ")";
                            } catch (java.text.ParseException e) {
                            }
                        }

                        if (object.has("first_air_date") && !object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(object.getString("first_air_date"));
                                String formattedDate = dateFormat.format(date);
                                releaseDate = "(" + formattedDate + ")";
                            } catch (java.text.ParseException e) {
                            }
                        }

                        if (object.has("media_type") && !object.getString("media_type").isEmpty())
                            mediaType = object.getString("media_type");


                        Uri path = Uri.parse("android.resource://" + R.drawable.placeholder_default);
                        if (!posterPath.isEmpty()) {
                            if (imageLoader.getDiskCache().get(posterPath).exists())
                                path = Uri.fromFile(new File(imageLoader.getDiskCache().get(posterPath).getPath()));
                            else {
                                idsList.add(id);
                                posterPathList.add(posterPath);
                            }
                        }

                        searchDB.insertAutoComplete(id, title, path, releaseDate, mediaType);


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

        @Override
        protected void onPostExecute(Boolean result) {
            if (query.length() > 1) {
                searchAdapter.changeCursor(searchDB.autoComplete());

                if (posterPathList != null && posterPathList.size() > 0) {
                    for (int i = 0; i < posterPathList.size(); i++) {
                        searchImgLoadingListener = new SearchImgLoadingListener(idsList.get(i), queryZ);
                        imageLoader.loadImage(posterPathList.get(i), searchImgLoadingListener);
                    }
                }
            }


        }

    }

    private class SearchImgLoadingListener extends SimpleImageLoadingListener {
        private int currId;
        private String queryZ;

        public SearchImgLoadingListener(int currId, String query) {
            this.currId = currId;
            this.queryZ = query;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (query.equals(queryZ)) {
                Uri uriFile = Uri.fromFile(new File(imageLoader.getDiskCache().get(imageUri).getPath()));
                searchDB.updateImg(currId, uriFile);
                searchAdapter.changeCursor(searchDB.autoComplete());
            }
        }
    }

    public void setLastVisitedMovieInCredits(int lastVisitedMovieInCredits) {
        this.lastVisitedMovieInCredits = lastVisitedMovieInCredits;
    }

    public int getLastVisitedMovieInCredits() {
        return lastVisitedMovieInCredits;
    }

    public int getIconMarginConstant() {
        return iconMarginConstant;
    }

    public int getIconMarginLandscape() {
        return iconMarginLandscape;
    }

    public int getIconConstantSpecialCase() {
        return iconConstantSpecialCase;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public int getOneIcon() {
        return oneIcon;
    }

    public int getOneIconToolbar() {
        return oneIconToolbar;
    }

    public int getTwoIcons() {
        return twoIcons;
    }

    public int getTwoIconsToolbar() {
        return twoIconsToolbar;
    }

    public int getThreeIcons() {
        return threeIcons;
    }

    public int getThreeIconsToolbar() {
        return threeIconsToolbar;
    }

}