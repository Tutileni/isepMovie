package com.simao.isepmovies.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

import com.simao.isepmovies.R;
import com.simao.isepmovies.model.SimilarModel;

/**
  * Adaptateur de galerie. Utilisé pour charger les images de la galerie dans la liste des galeries.
  */
public class SimilarAdapter extends ArrayAdapter<SimilarModel> {
    private ArrayList<SimilarModel> similarList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public SimilarAdapter(Context context, int resource, ArrayList<SimilarModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        similarList = objects;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.placeholder_default)
                .showImageForEmptyUri(R.drawable.placeholder_default)
                .showImageOnFail(R.drawable.placeholder_default)
                .cacheOnDisk(true)
                .build();
    }

    /**
     * Obtient une vue qui affiche les données à la position spécifiée dans l'ensemble de données.
     *
     * @param position Position de l'élément dans l'ensemble de données de l'adaptateur de l'élément dont nous voulons voir la vue.
     * @param convertView L'ancienne vue à réutiliser, si possible. Remarque: Vous devez vérifier que cette vue est non nulle et de type approprié avant de l'utiliser.
     * @param parent Le parent auquel cette vue sera éventuellement attachée.
     * @return Une vue correspondant à la date à la position spécifiée.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.posterPath = (ImageView) v.findViewById(R.id.posterPath);
            holder.releaseDate = (TextView) v.findViewById(R.id.releaseDate);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }


        holder.title.setText(similarList.get(position).getTitle());


        if (similarList.get(position).getReleaseDate() != null)
            holder.releaseDate.setText(similarList.get(position).getReleaseDate());



        // si getPosterPath renvoie null imageLoader définit automatiquement l'image par défaut
        imageLoader.displayImage(similarList.get(position).getPosterPath(), holder.posterPath, options);

        return v;

    }

    /**
     *Définit les éléments de la ligne de la liste de la galerie.
     */
    static class ViewHolder {
        public TextView title;
        public ImageView posterPath;
        public TextView releaseDate;
    }


}