package com.simao.isepmovies.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.R;
import com.simao.isepmovies.controller.MovieDetailsCast;
import com.simao.isepmovies.controller.MovieDetailsInfo;
import com.simao.isepmovies.controller.MovieDetailsOverview;
/**
  * Adaptateur MovieDetailsSlide utilisé par le Viewpager Détails du film.
  * Implémentation de PagerAdapter qui utilise un fragment pour gérer chaque page. Cette classe gère également l'enregistrement et la restauration de l'état du fragment.
  */
public class MovieDetailsSlideAdapter extends FragmentStatePagerAdapter {
    private String[] navMenuTitles;
    /*
     * Nous utilisons registeredFragments afin que nous puissions obtenir nos fragments actifs de l'application.
     */
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private MainActivity activity;

    public MovieDetailsSlideAdapter(FragmentManager fm, Resources res, MainActivity activity) {
        super(fm);
        navMenuTitles = res.getStringArray(R.array.detailTabs);
        this.activity = activity;
    }

    /**
     *
     * Renvoie le nombre de vues disponibles.
     */
    @Override
    public int getCount() {
        return 3;
    }


    /**
     * Renvoie le fragment associé à une position spécifiée.
     *
     * @param position Position dans cet adaptateur
     * @return Identificateur unique de l'article à la position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                MovieDetailsInfo info = new MovieDetailsInfo();
                return info;
            case 1:
                MovieDetailsCast cast = new MovieDetailsCast();
                return cast;
            case 2:
                MovieDetailsOverview overview = new MovieDetailsOverview();
                return overview;
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
            default:
                return navMenuTitles[1];
        }
    }

    /**
     * Créer la page pour la position donnée. L'adaptateur est responsable de l'ajout de la vue au conteneur donné ici,
     * Bien que cela devrait seulement assurer que cela est fait au moment où il revient de finishUpdate (ViewGroup).
     *
     * @param container La vue contenant dans laquelle la page sera affichée.
     * @param position La position de la page à instancier.
     * @return Renvoie un objet représentant la nouvelle page. Cela n'a pas besoin d'être une vue, mais peut être un autre conteneur de la page.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }


    /**
    * Supprimer une page pour la position donnée. L'adaptateur est responsable de la suppression de la vue de son conteneur,
    * bien que cela ne devrait être garanti qu'au retour de finishUpdate (ViewGroup)
    *
    * @param container La vue contenant la page à supprimer.
    * @param position La position de la page à supprimer.
    * @param object Le même objet qui a été retourné par instantiateItem (View, int).
    */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }


    /**
     * Obtenir le fragment pour la position actuelle
     *
     *@param position le fragment de la position.
     */
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }


    /**
     * Nous ne restituons pas l'état, car nous avons déjà détruit nos fragments.
     * Cela entraînera des fragments vides.
     */
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        try {
            if (activity.getRestoreMovieDetailsAdapterState()) {
                super.restoreState(state, loader);
            } else {
                activity.setRestoreMovieDetailsAdapterState(true);
            }
        } catch (java.lang.IllegalStateException e) {

        }
    }

}