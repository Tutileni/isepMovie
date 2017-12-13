package com.simao.isepmovies.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.R;
import com.simao.isepmovies.helper.ObservableScrollView;


public class MovieDetailsOverview extends Fragment {
    private MainActivity activity;
    private TextView overview;
    private ObservableScrollView scrollView;

    public MovieDetailsOverview() {

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

        View rootView = inflater.inflate(R.layout.moviedetailsoverview, container, false);
        activity = ((MainActivity) getActivity());
        overview = (TextView) rootView.findViewById(R.id.overviewContent);
        scrollView = (ObservableScrollView) rootView.findViewById(R.id.moviedetailsoverview);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View toolbarView = activity.findViewById(R.id.toolbar);
                if (toolbarView != null) {
                    int toolbarHeight = toolbarView.getHeight();
                    DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
                    int height = displayMetrics.heightPixels;
                    overview.setMinHeight(height + toolbarHeight);
                }
            }
        });

        return rootView;
    }

    /**
      * @param savedInstanceState si le fragment est en cours de recréation à partir d'un état précédemment enregistré, c'est l'état.
      */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.getMovieDetailsOverviewBundle() != null)
            overview.setText(activity.getMovieDetailsOverviewBundle().getString("overview"));


        if (scrollView != null) {
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getMovieDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getMovieDetailsFragment());
        }

    }

    public TextView getOverview() {
        return overview;
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
       * Lancé lorsque le fragment est détruit.
       */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setMovieDetailsOverviewBundle(null);
    }

    public ObservableScrollView getScrollView() {
        return scrollView;
    }
}
