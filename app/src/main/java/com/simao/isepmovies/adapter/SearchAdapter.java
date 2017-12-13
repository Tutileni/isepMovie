package com.simao.isepmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import com.simao.isepmovies.R;
import com.simao.isepmovies.model.SearchModel;
/**
 * Rechercher un adaptateur. Utilisé pour charger les résultats de la recherche dans la liste de recherche.
 */
public class SearchAdapter extends ArrayAdapter<SearchModel> {
    private ArrayList<SearchModel> searchList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private ImageLoader imageLoader;

    public SearchAdapter(Context context, int resource, ArrayList<SearchModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        searchList = objects;
        imageLoader = ImageLoader.getInstance();

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
            holder.character = (TextView) v.findViewById(R.id.character);
            holder.department = (TextView) v.findViewById(R.id.department);
            holder.character.setVisibility(View.GONE);
            holder.department.setVisibility(View.GONE);
            holder.releaseDate = (TextView) v.findViewById(R.id.releaseDate);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }


        holder.title.setText(searchList.get(position).getTitle());


        if (searchList.get(position).getReleaseDate() != null) {
            holder.releaseDate.setText("(" + searchList.get(position).getReleaseDate() + ")");
            holder.releaseDate.setVisibility(View.VISIBLE);
        } else
            holder.releaseDate.setVisibility(View.GONE);


        // si getPosterPath renvoie null imageLoader définit automatiquement l'image par défaut
        imageLoader.displayImage(searchList.get(position).getPosterPath(), holder.posterPath);


        return v;

    }

    /**
     *Définit les éléments de la ligne de la liste de recherche.
     */
    static class ViewHolder {
        public TextView title;
        public ImageView posterPath;
        public TextView character;
        public TextView department;
        public TextView releaseDate;
    }


}