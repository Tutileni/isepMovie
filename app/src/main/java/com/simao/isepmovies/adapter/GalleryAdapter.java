package com.simao.isepmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import com.simao.isepmovies.MainActivity;
import com.simao.isepmovies.R;
import com.simao.isepmovies.model.GalleryModel;


public class GalleryAdapter extends ArrayAdapter<GalleryModel> {
    private ArrayList<GalleryModel> galleryList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private Context mContext;
    private ImageLoader imageLoader;

    public GalleryAdapter(Context context, int resource, ArrayList<GalleryModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        galleryList = objects;
        mContext = context;
        imageLoader = ImageLoader.getInstance();
    }

    /**
    * Obtenez une vue qui affiche les données à la position spécifiée dans l'ensemble de données.
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
            holder.filePath = (ImageView) v.findViewById(R.id.filePath);


            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (imageLoader.getDiskCache().get(galleryList.get(position).getFilePath()).exists())
            imageLoader.displayImage(galleryList.get(position).getFilePath(), holder.filePath, ((MainActivity) mContext).getOptionsWithoutFade());
        else
            imageLoader.displayImage(galleryList.get(position).getFilePath(), holder.filePath, ((MainActivity) mContext).getOptionsWithFade());


        return v;

    }

    /**
     *Définit les éléments de ligne de la liste de la galerie.
     */
    static class ViewHolder {
        public ImageView filePath;
    }


}