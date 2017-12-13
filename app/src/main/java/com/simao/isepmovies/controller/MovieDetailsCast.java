package com.simao.isepmovies.controller;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.R;
import com.simao.isepmovies.adapter.CastAdapter;
import com.simao.isepmovies.helper.ObservableListView;
import com.simao.isepmovies.model.CastModel;


public class MovieDetailsCast extends Fragment implements AdapterView.OnItemClickListener {
    private ObservableListView listView;
    private CastAdapter castAdapter;
    private ArrayList<CastModel> castList;
    private MainActivity activity;
    private int lastVisitedPerson;
    private CastDetails castDetails;

    public MovieDetailsCast() {

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

        View rootView = inflater.inflate(R.layout.moviedetailscast, container, false);
        listView = (ObservableListView) rootView.findViewById(R.id.castList);
        activity = ((MainActivity) getActivity());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (listView != null) {
            listView.setScrollViewCallbacks(activity.getMovieDetailsFragment());
            listView.setTouchInterceptionViewGroup((ViewGroup) activity.getMovieDetailsFragment().getView().findViewById(R.id.containerLayout));
            listView.setOnItemClickListener(this);
            Bundle save = activity.getMovieDetailsCastBundle();
            if (save != null) {
                castList = save.getParcelableArrayList("castList");
                castAdapter = new CastAdapter(getActivity(), R.layout.castrow, castList);
                listView.setAdapter(castAdapter);
                lastVisitedPerson = save.getInt("lastVisitedPerson");
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
        activity.getMovieDetailsFragment().showInstantToolbar();
        activity.setMovieDetailsFragment(null);
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        if (activity.getCastDetailsFragment() != null && lastVisitedPerson == castList.get(position).getId() && activity.getCastDetailsFragment().getTimeOut() == 0) {
            // Les anciens détails du film récupèrent les informations et le composant de réinitialisation
            activity.getCastDetailsFragment().onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", castList.get(position).getId());
            Bundle save = activity.getCastDetailsFragment().getSave();
            castDetails = new CastDetails();
            castDetails.setTimeOut(0);
            castDetails.setSave(save);
            castDetails.setArguments(bundle);
        } else castDetails = new CastDetails();

        lastVisitedPerson = castList.get(position).getId();
        ((MovieDetails) getParentFragment()).setAddToBackStack(true);
        getParentFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchMovieDetails();

        castDetails.setTitle(castList.get(position).getName());
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", castList.get(position).getId());
        castDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, castDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("movieDetails");
        transaction.commit();

    }

    /**
      * Définit l'adaptateur de la liste de distribution.
      * Nous appelons cette méthode de MovieDetails (fragment parent).
      *
      * @param castList liste des données
      */
    public void setAdapter(ArrayList<CastModel> castList) {
        this.castList = castList;
        castAdapter = new CastAdapter(getActivity(), R.layout.castrow, this.castList);
        listView.setAdapter(castAdapter);
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
      * Renvoie la valeur de la dernière personne visitée.
      * Nous l'utilisons pour vérifier si l'identifiant actuel est le même que le dernier visité.
      * De cette façon, nous empêchons la nouvelle demande au serveur.
      */
    public int getLastVisitedPerson() {
        return lastVisitedPerson;
    }

    /**
      * Lancé lorsque le fragment est détruit.
      */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setMovieDetailsCastBundle(null);
        listView.setAdapter(null);
    }

    public ObservableListView getListView() {
        return listView;
    }

    public ArrayList<CastModel> getCastList() {
        return castList;
    }

    public boolean canScroll() {
        if (isAdded()) {
            int last = listView.getLastVisiblePosition();
            if (listView.getChildAt(last) != null) {
                if (last == listView.getCount() - 1 && listView.getChildAt(last).getBottom() <= (listView.getHeight() + (63 * getResources().getDisplayMetrics().density))) {
                    // It fits!
                    return false;
                } else {
                    // It doesn't fit...
                    return true;
                }
            }
        } // si getChildAt (last) est null, cela signifie que nous avons changé sur une vue différente et oui nous pouvons faire défiler
        return true;
    }
}
