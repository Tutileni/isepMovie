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
import com.simao.isepmovies.model.MovieModel;
/**
  * Adaptateur de film. Utilisé pour charger les informations sur les films dans la liste des films.
  */
public class MovieAdapter extends ArrayAdapter<MovieModel> {
    private ArrayList<MovieModel> moviesList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public MovieAdapter(Context context, int resource, ArrayList<MovieModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        moviesList = objects;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                // pendentif scintille le masquage de la barre d'outils
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
    @Override
    */
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.posterPath = (ImageView) v.findViewById(R.id.posterPath);
            holder.character = (TextView) v.findViewById(R.id.character);
            holder.department = (TextView) v.findViewById(R.id.department);
            holder.releaseDate = (TextView) v.findViewById(R.id.releaseDate);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }


        holder.title.setText(moviesList.get(position).getTitle());


        if (moviesList.get(position).getReleaseDate() != null) {
            holder.releaseDate.setText("(" + moviesList.get(position).getReleaseDate() + ")");
            holder.releaseDate.setVisibility(View.VISIBLE);
        } else
            holder.releaseDate.setVisibility(View.GONE);


        if (moviesList.get(position).getCharacter() != null) {
            holder.character.setText(moviesList.get(position).getCharacter());
            holder.character.setVisibility(View.VISIBLE);
        } else
            holder.character.setVisibility(View.GONE);


        if (moviesList.get(position).getDepartmentAndJob() != null) {
            holder.department.setText(moviesList.get(position).getDepartmentAndJob());
            holder.department.setVisibility(View.VISIBLE);
        } else
            holder.department.setVisibility(View.GONE);

        // si getPosterPath renvoie null imageLoader définit automatiquement l'image par défaut
        imageLoader.displayImage(moviesList.get(position).getPosterPath(), holder.posterPath, options);


        return v;

    }

    /**
     * Définit les éléments de ligne de la liste de films.
     */
    static class ViewHolder {
        public TextView title;
        public ImageView posterPath;
        public TextView character;
        public TextView department;
        public TextView releaseDate;
    }


}