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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.R;
import com.simao.isepmovies.adapter.SimilarAdapter;
import com.simao.isepmovies.model.SimilarModel;
import com.simao.isepmovies.view.ObservableParallaxScrollView;

/**
  * Ce fragment est utilisé dans les détails de la distribution. Il contient le contenu de l'information.
  */
public class CastDetailsInfo extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private MainActivity activity;
    private View rootView;
    private TextView name;
    private ImageView profilePath;
    private TextView birthInfo;
    private TextView alsoKnownAs;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;
    private ObservableParallaxScrollView scrollView;
    private GridView castDetailsKnownGrid;
    private ArrayList<SimilarModel> knownList;
    private View knownHolder;
    private MovieDetails movieDetails;
    private Button showMoreButton;

    public CastDetailsInfo() {

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


        rootView = inflater.inflate(R.layout.castdetailsinfo, container, false);
        activity = ((MainActivity) getActivity());
        name = (TextView) rootView.findViewById(R.id.name);
        profilePath = (ImageView) rootView.findViewById(R.id.profilePath);
        birthInfo = (TextView) rootView.findViewById(R.id.birthInfo);
        alsoKnownAs = (TextView) rootView.findViewById(R.id.alsoKnownAs);

        homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
        homeIcon.setVisibility(View.INVISIBLE);
        homeIcon.bringToFront();

        galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
        galleryIcon.setVisibility(View.INVISIBLE);
        galleryIcon.bringToFront();

        moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
        moreIcon.bringToFront();
        scrollView = (ObservableParallaxScrollView) rootView.findViewById(R.id.castdetailsinfo);

        castDetailsKnownGrid = (GridView) rootView.findViewById(R.id.castDetailsKnownGrid);
        knownHolder = rootView.findViewById(R.id.knownHolder);
        showMoreButton = (Button) rootView.findViewById(R.id.showMoreButton);
        showMoreButton.setOnClickListener(this);
        View detailsLayout = rootView.findViewById(R.id.detailsLayout);
        ViewCompat.setElevation(detailsLayout, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(moreIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(homeIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(galleryIcon, 2 * getResources().getDisplayMetrics().density);
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
        if (activity.getCastDetailsFragment() != null) {
            moreIcon.setOnClickListener(activity.getCastDetailsFragment().getOnMoreIconClick());
            activity.getCastDetailsFragment().getOnMoreIconClick().setKey(false);
        }

        if (activity.getCastDetailsInfoBundle() != null)
            onOrientationChange(activity.getCastDetailsInfoBundle());

        if (scrollView != null) {
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getCastDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getCastDetailsFragment());
        }

    }

    public TextView getName() {
        return name;
    }

    public ImageView getProfilePath() {
        return profilePath;
    }

    public TextView getBirthInfo() {
        return birthInfo;
    }

    public TextView getAlsoKnownAs() {
        return alsoKnownAs;
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
        // Nom
        name.setText(args.getString("name"));

        // Chemin de l'affiche
        if (args.getString("profilePathURL") != null) {
            activity.setBackDropImage(profilePath, args.getString("profilePathURL"));
            profilePath.setTag(args.getString("profilePathURL"));
        }

        // Informations sur la naissance
        if (!args.getString("birthInfo").isEmpty())
            activity.setText(birthInfo, args.getString("birthInfo"));
        else activity.hideView(birthInfo);

        // Aussi connu sous le nom
        if (!args.getString("alsoKnownAs").isEmpty()) {
            activity.setText(alsoKnownAs, args.getString("alsoKnownAs"));
        } else activity.hideView(alsoKnownAs);


        knownList = args.getParcelableArrayList("knownList");
        if (knownList != null && knownList.size() > 0)
            setKnownList(knownList);
        else
            activity.hideView(knownHolder);

    }

    /**
      * Lancé lorsque le fragment est détruit.
      */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setCastDetailsInfoBundle(null);
        profilePath.setImageDrawable(null);
        castDetailsKnownGrid.setAdapter(null);
    }

    public ObservableParallaxScrollView getScrollView() {
        return scrollView;
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

    public void setKnownList(ArrayList<SimilarModel> similarList) {
        this.knownList = similarList;
        SimilarAdapter similarAdapter = new SimilarAdapter(getActivity(), R.layout.similar_row, similarList);
        castDetailsKnownGrid.setAdapter(similarAdapter);
        castDetailsKnownGrid.setOnItemClickListener(this);

        if (knownList.size() < 4) {
            ViewGroup.LayoutParams lp = castDetailsKnownGrid.getLayoutParams();
            lp.height /= 2;
        }
    }

    public ArrayList<SimilarModel> getKnownList() {
        return knownList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        activity.getCastDetailsFragment().showInstantToolbar();
        activity.setCastDetailsFragment(null);
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);

        if (knownList.get(position).getMediaType().equals("movie")) {
            if (activity.getMovieDetailsFragment() != null && activity.getLastVisitedMovieInCredits() == knownList.get(position).getId() && activity.getMovieDetailsFragment().getTimeOut() == 0) {
                // Old movie details retrieve info and re-init component else crash
                activity.getMovieDetailsFragment().onSaveInstanceState(new Bundle());
                Bundle bundle = new Bundle();
                bundle.putInt("id", knownList.get(position).getId());
                Bundle save = activity.getMovieDetailsFragment().getSave();
                // Re-init movie details and set save information
                movieDetails = new MovieDetails();
                movieDetails.setTimeOut(0);
                movieDetails.setSave(save);
                movieDetails.setArguments(bundle);
            } else movieDetails = new MovieDetails();
        }
        activity.setLastVisitedMovieInCredits(knownList.get(position).getId());
        ((CastDetails) getParentFragment()).setAddToBackStack(true);
        getParentFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchCastDetails();


        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", knownList.get(position).getId());

        if (knownList.get(position).getMediaType().equals("movie")) {
            movieDetails.setTitle(knownList.get(position).getTitle());
            movieDetails.setArguments(bundle);
            transaction.replace(R.id.frame_container, movieDetails);
        }
        // ajoute la transaction en cours à la pile arrière:
        transaction.addToBackStack("castDetails");
        transaction.commit();

    }

    public Button getShowMoreButton() {
        return showMoreButton;
    }

    @Override
    public void onClick(View v) {
        ((CastDetails) getParentFragment()).getmViewPager().setCurrentItem(1);
    }

    public View getKnownHolder() {
        return knownHolder;
    }

}
