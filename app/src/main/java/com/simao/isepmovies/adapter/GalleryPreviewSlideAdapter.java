package com.simao.isepmovies.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import com.simao.isepmovies.MovieDB;
import com.simao.isepmovies.R;
import com.simao.isepmovies.controller.GalleryPreviewDetail;


/**
 * L'adaptateur principal qui soutient le ViewPager. Une sous-classe de FragmentStatePagerAdapter comme il
 * pourrait être un grand nombre d'articles dans le ViewPager et nous ne voulons pas les garder tous dans
 * mémoire à la fois mais créez / détruisez-les à la volée.
 */

public class GalleryPreviewSlideAdapter extends FragmentStatePagerAdapter {
    private final int mSize;
    private Resources res;
    private ArrayList<String> galleryList;

    public GalleryPreviewSlideAdapter(FragmentManager fm, Resources res, ArrayList<String> galleryList) {
        super(fm);
        this.res = res;
        this.galleryList = galleryList;
        mSize = galleryList.size();


    }

    /**
     * Returne le nombre des vues disponible.
     */
    @Override
    public int getCount() {
        return mSize;
    }


    /**
    * Renvoie le fragment associé à une position spécifiée.
    * @param position Position dans cet adaptateur
    * @return Identifiant unique pour l'item à la position
    */
    @Override
    public Fragment getItem(int position) {
        return GalleryPreviewDetail.newInstance(MovieDB.imageUrl + res.getString(R.string.galleryPreviewImgSize) + galleryList.get(position));
    }
}
