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
import com.simao.isepmovies.helper.CircleBitmapDisplayer;
import com.simao.isepmovies.model.CastModel;

public class CastAdapter extends ArrayAdapter<CastModel> {
    private ArrayList<CastModel> castList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public CastAdapter(Context context, int resource, ArrayList<CastModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        castList = objects;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                // Les bitmaps dans RGB_565 consomment 2 fois moins de mémoire que dans ARGB_8888
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .displayer(new CircleBitmapDisplayer())
                .showImageOnLoading(R.drawable.placeholder_cast)
                .showImageForEmptyUri(R.drawable.placeholder_cast)
                .showImageOnFail(R.drawable.placeholder_cast)
                .cacheOnDisk(true)
                .build();

    }

    /**
     * Obtient une vue qui affiche les données à la position spécifiée dans l'ensemble de données.
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
            holder.name = (TextView) v.findViewById(R.id.name);
            holder.character = (TextView) v.findViewById(R.id.character);
            holder.profilePath = (ImageView) v.findViewById(R.id.profilePath);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.name.setText(castList.get(position).getName());

        if (castList.get(position).getCharacter() != null) {
            holder.character.setText(castList.get(position).getCharacter());
            holder.character.setVisibility(View.VISIBLE);
        } else holder.character.setVisibility(View.GONE);

        imageLoader.displayImage(castList.get(position).getProfilePath(), holder.profilePath, options);


        return v;

    }

    /**
     * Définit les éléments de ligne de la liste de distribution.
     */
    static class ViewHolder {
        public TextView name;
        public TextView character;
        public ImageView profilePath;
    }

}