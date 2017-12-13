package com.simao.isepmovies.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.simao.isepmovies.R;
import com.simao.isepmovies.controller.MovieList;
/**
  * Le {@link android.support.v4.view.PagerAdapter} utilisé pour afficher les pages de cet exemple.
  * Les pages individuelles sont simples et affichent simplement deux lignes de texte. La section importante de
  * cette classe est la méthode {@link #getPageTitle (int)} qui contrôle ce qui est affiché dans le
  * {@link com.simao.isepmovies.view.SlidingTabLayout}.
  */
public class MovieSlideAdapter extends FragmentPagerAdapter {
    private String[] navMenuTitles;
    private FragmentManager manager;
    private FragmentTransaction mCurTransaction = null;
    private Resources res;

    public MovieSlideAdapter(FragmentManager fm, Resources res) {
        super(fm);
        this.manager = fm;
        navMenuTitles = res.getStringArray(R.array.moviesTabs);
        this.res = res;
    }

    /**
     * Renvoie le nombre de vues disponibles.
     */
    @Override
    public int getCount() {
        return 4;
    }

    /**
      * Renvoie le fragment associé à une position spécifiée.
      *
      * @param position Position dans cet adaptateur
      * @return Identifiant unique pour l'article à la position
      */
    @Override
    public Fragment getItem(int position) {
        String upcoming = "movie/upcoming";
        String nowPlaying = "movie/now_playing";
        String popular = "movie/popular";
        String topRated = "movie/top_rated";
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                args.putString("currentList", "upcoming");
                MovieList upcomingList = new MovieList();
                upcomingList.setTitle(res.getString(R.string.moviesTitle));
                upcomingList.setArguments(args);
                upcomingList.setCurrentList(upcoming);
                return upcomingList;
            case 1:
                args.putString("currentList", "nowPlaying");
                MovieList nowPlayingList = new MovieList();
                nowPlayingList.setTitle(res.getString(R.string.moviesTitle));
                nowPlayingList.setArguments(args);
                nowPlayingList.setCurrentList(nowPlaying);
                return nowPlayingList;
            case 2:
                args.putString("currentList", "popular");
                MovieList popularList = new MovieList();
                popularList.setTitle(res.getString(R.string.moviesTitle));
                popularList.setArguments(args);
                popularList.setCurrentList(popular);
                return popularList;
            case 3:
                args.putString("currentList", "topRated");
                MovieList topRatedList = new MovieList();
                topRatedList.setTitle(res.getString(R.string.moviesTitle));
                topRatedList.setArguments(args);
                topRatedList.setCurrentList(topRated);
                return topRatedList;
            default:
                return null;
        }

    }

    /**
      * Cette méthode peut être appelée par ViewPager pour obtenir une chaîne décrivant la page spécifiée.
      *
      * @param position La position du titre demandé
      * @return Un titre pour la page demandée
      */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return navMenuTitles[0];
            case 1:
                return navMenuTitles[1];
            case 2:
                return navMenuTitles[2];
            case 3:
                return navMenuTitles[3];
            default:
                return navMenuTitles[1];
        }
    }

    /**
      * @param container - notre Viewpager
      * Tiré lorsque nous sommes dans les détails de films et nous avons appuyé sur le bouton de retour.
      * Recrée nos fragments.
      */
    public void reAttachFragments(ViewGroup container) {
        if (mCurTransaction == null) {
            mCurTransaction = manager.beginTransaction();
        }

        for (int i = 0; i < getCount(); i++) {

            final long itemId = getItemId(i);

            // Do we already have this fragment?
            String name = "android:switcher:" + container.getId() + ":" + itemId;
            Fragment fragment = manager.findFragmentByTag(name);

            if (fragment != null) {
                mCurTransaction.detach(fragment);
            }
        }
        // Ajoute cette vérification pour les tests JUnit
        // Ce bloc try est ajouté car le test JUnit échoue dans la méthode MainActivityTest.java, setUp ().
        // java.lang.IllegalStateException: entrée récursive à executePendingTransactions
        try {
            mCurTransaction.commit();
        } catch (java.lang.IllegalStateException e) {
        }
        mCurTransaction = null;
    }


}