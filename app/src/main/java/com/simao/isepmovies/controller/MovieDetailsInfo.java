package com.simao.isepmovies.controller;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.R;
import com.simao.isepmovies.adapter.SimilarAdapter;
import com.simao.isepmovies.model.SimilarModel;
import com.simao.isepmovies.view.ObservableParallaxScrollView;


public class MovieDetailsInfo extends Fragment implements AdapterView.OnItemClickListener {
    private MainActivity activity;
    private View rootView;
    private ImageView backDropPath;
    private int backDropCheck;
    private TextView titleText;
    private TextView releaseDate;
    private ImageView posterPath;
    private TextView tagline;
    private TextView statusText;
    private TextView runtime;
    private TextView genres;
    private TextView countries;
    private TextView companies;
    private RatingBar ratingBar;
    private TextView voteCount;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;
    private CircledImageView trailerIcon;
    private GridView movieDetailsSimilarGrid;
    private ArrayList<SimilarModel> similarList;
    private View similarHolder;
    private ObservableParallaxScrollView scrollView;
    private MovieDetails movieDetails = new MovieDetails();

    public MovieDetailsInfo() {

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


        rootView = inflater.inflate(R.layout.moviedetailsinfo, container, false);
        activity = ((MainActivity) getActivity());
        backDropPath = (ImageView) rootView.findViewById(R.id.backDropPath);


        titleText = (TextView) rootView.findViewById(R.id.title);
        releaseDate = (TextView) rootView.findViewById(R.id.releaseDate);
        posterPath = (ImageView) rootView.findViewById(R.id.posterPath);
        tagline = (TextView) rootView.findViewById(R.id.tagline);
        statusText = (TextView) rootView.findViewById(R.id.status);
        runtime = (TextView) rootView.findViewById(R.id.runtime);
        genres = (TextView) rootView.findViewById(R.id.genres);
        countries = (TextView) rootView.findViewById(R.id.countries);
        companies = (TextView) rootView.findViewById(R.id.companies);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        voteCount = (TextView) rootView.findViewById(R.id.voteCount);

        homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
        homeIcon.setVisibility(View.INVISIBLE);
        homeIcon.bringToFront();

        galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
        galleryIcon.setVisibility(View.INVISIBLE);
        galleryIcon.bringToFront();

        trailerIcon = (CircledImageView) rootView.findViewById(R.id.trailerIcon);
        trailerIcon.setVisibility(View.INVISIBLE);
        trailerIcon.bringToFront();

        // Highest Z-index has to be declared last
        moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
        moreIcon.bringToFront();

        movieDetailsSimilarGrid = (GridView) rootView.findViewById(R.id.movieDetailsSimilarGrid);
        similarHolder = rootView.findViewById(R.id.similarHolder);
        scrollView = (ObservableParallaxScrollView) rootView.findViewById(R.id.moviedetailsinfo);
        View detailsLayout = rootView.findViewById(R.id.detailsLayout);
        ViewCompat.setElevation(detailsLayout, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(moreIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(homeIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(galleryIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(trailerIcon, 2 * getResources().getDisplayMetrics().density);
        // Prévenir les bulles d'événements si vous touchez à la mise en page des détails lorsque l'onglet d'information est défilé il ouvrira la vue de la galerie
        detailsLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.getMovieDetailsFragment() != null) {
            moreIcon.setOnClickListener(activity.getMovieDetailsFragment().getOnMoreIconClick());
            activity.getMovieDetailsFragment().getOnMoreIconClick().setKey(false);
        }


        if (activity.getMovieDetailsInfoBundle() != null)
            onOrientationChange(activity.getMovieDetailsInfoBundle());

        if (scrollView != null) {
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getMovieDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getMovieDetailsFragment());
        }
    }

    public TextView getTitleText() {
        return titleText;
    }

    public TextView getReleaseDate() {
        return releaseDate;
    }

    public ImageView getPosterPath() {
        return posterPath;
    }

    public TextView getStatusText() {
        return statusText;
    }

    public TextView getTagline() {
        return tagline;
    }


    public TextView getRuntime() {
        return runtime;
    }

    public TextView getGenres() {
        return genres;
    }

    public TextView getCountries() {
        return countries;
    }

    public TextView getCompanies() {
        return companies;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public TextView getVoteCount() {
        return voteCount;
    }

    public ImageView getBackDropPath() {
        return backDropPath;
    }

    public int getBackDropCheck() {
        return backDropCheck;
    }

    public void setBackDropCheck(int backDropCheck) {
        this.backDropCheck = backDropCheck;
    }

    public CircledImageView getMoreIcon() {
        return moreIcon;
    }

    public CircledImageView getHomeIcon() {
        return homeIcon;
    }

    public CircledImageView getGalleryIcon() {
        return galleryIcon;
    }

    public CircledImageView getTrailerIcon() {
        return trailerIcon;
    }

    public View getRootView() {
        return rootView;
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
    }

    /**
      * Lancé lorsque la restauration à partir de backState ou de l'orientation a changé.
      *
      * @param args notre paquet avec l'état sauvé. Notre fragment parent gère l'économie.
      */
    @SuppressWarnings("ConstantConditions")
    private void onOrientationChange(Bundle args) {
        // BackDrop path
        backDropCheck = args.getInt("backDropCheck");
        if (backDropCheck == 0) {
            activity.setBackDropImage(backDropPath, args.getString("backDropUrl"));
            backDropPath.setTag(args.getString("backDropUrl"));
        }

        // Title
        activity.setText(titleText, args.getString("titleText"));

        // Release date
        activity.setText(releaseDate, args.getString("releaseDate"));

        // Status
        activity.setText(statusText, args.getString("status"));

        // Tag line
        if (!args.getString("tagline").isEmpty())
            tagline.setText(args.getString("tagline"));
        else
            activity.hideTextView(tagline);

        // RunTime
        if (!args.getString("runTime").isEmpty())
            activity.setText(runtime, args.getString("runTime"));
        else activity.hideView(runtime);

        // Genres
        if (!args.getString("genres").isEmpty())
            activity.setText(genres, args.getString("genres"));
        else activity.hideView(genres);

        // Production Countries
        if (!args.getString("productionCountries").isEmpty())
            activity.setText(countries, args.getString("productionCountries"));
        else activity.hideView(countries);

        // Production Companies
        if (!args.getString("productionCompanies").isEmpty()) {
            activity.setText(companies, args.getString("productionCompanies"));
            if (args.getString("productionCountries").isEmpty()) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) companies.getLayoutParams();
                lp.setMargins(0, (int) (28 * getResources().getDisplayMetrics().density), 0, 0);
            }
        } else activity.hideView(companies);


        // Poster path
        if (args.getString("posterPathURL") != null) {
            activity.setImage(posterPath, args.getString("posterPathURL"));
            activity.setImageTag(posterPath, args.getString("posterPathURL"));
        }


        // Rating
        if (args.getString("voteCount").isEmpty()) {
            activity.hideRatingBar(ratingBar);
            activity.hideTextView(voteCount);
        } else {
            ratingBar.setRating(args.getFloat("rating"));
            activity.setText(voteCount, args.getString("voteCount"));
        }

        // Similar list
        similarList = args.getParcelableArrayList("similarList");
        if (similarList != null && similarList.size() > 0)
            setSimilarList(similarList);
        else
            activity.hideView(similarHolder);


    }

    /**
      * Lancé lorsque le fragment est détruit.
      */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setMovieDetailsInfoBundle(null);
        posterPath.setImageDrawable(null);
        backDropPath.setImageDrawable(null);
        movieDetailsSimilarGrid.setAdapter(null);
    }

    public void setSimilarList(ArrayList<SimilarModel> similarList) {
        this.similarList = similarList;
        SimilarAdapter similarAdapter = new SimilarAdapter(getActivity(), R.layout.similar_row, similarList);
        movieDetailsSimilarGrid.setAdapter(similarAdapter);
        movieDetailsSimilarGrid.setOnItemClickListener(this);

        if (similarList.size() < 4) {
            ViewGroup.LayoutParams lp = movieDetailsSimilarGrid.getLayoutParams();
            lp.height /= 2;
        }
    }

    public ArrayList<SimilarModel> getSimilarList() {
        return similarList;
    }

    public View getSimilarHolder() {
        return similarHolder;
    }


    public ObservableParallaxScrollView getScrollView() {
        return scrollView;
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

        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        if (activity.getMovieDetailsSimFragment() != null && activity.getLastVisitedSimMovie() == similarList.get(position).getId() && activity.getMovieDetailsSimFragment().getTimeOut() == 0) {
            // Les anciens détails du film récupèrent les informations et le composant de réinitialisation
            activity.getMovieDetailsSimFragment().onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", similarList.get(position).getId());
            Bundle save = activity.getMovieDetailsSimFragment().getSave();
            // Réinitialiser les détails de la vidéo et définir les informations de sauvegarde
            movieDetails = new MovieDetails();
            movieDetails.setTimeOut(0);
            movieDetails.setSave(save);
            movieDetails.setArguments(bundle);
        } else movieDetails = new MovieDetails();

        activity.setLastVisitedSimMovie(similarList.get(position).getId());
        activity.getMovieDetailsFragment().setAddToBackStack(true);
        activity.getMovieDetailsFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchMovieDetails();

        activity.setMovieDetailsFragment(null);
        activity.setSaveInMovieDetailsSimFragment(true);
        movieDetails.setTitle(similarList.get(position).getTitle());
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", similarList.get(position).getId());
        movieDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, movieDetails);
        // ajoute la transaction en cours à la pile arrière:
        transaction.addToBackStack("similarDetails");
        transaction.commit();


    }

    /**
      * @return Renvoie vrai ce ScrollView peut être défilé
      */
    public boolean canScroll() {
        if (isAdded()) {
            View child = scrollView.getChildAt(0);
            if (child != null) {
                int childHeight = child.getHeight();
                return (scrollView.getHeight() + (119 * getResources().getDisplayMetrics().density)) < childHeight;
            }
        }
        return false;
    }
}
