package com.simao.isepmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.simao.isepmovies.R;
import com.simao.isepmovies.model.GenresModel;

/**
 * Genres adapter. Used to load genres information in the genres list.
 */
public class GenresAdapter extends ArrayAdapter<GenresModel> {
    private ArrayList<GenresModel> genresList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;

    public GenresAdapter(Context context, int resource, ArrayList<GenresModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        genresList = objects;
    }

    /**
     * Obtenez une vue qui affiche les données à la position spécifiée dans l'ensemble de données.
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
            holder.name = (TextView) v.findViewById(R.id.name);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.name.setText(genresList.get(position).getName());


        return v;

    }
    /**
     *Définit les éléments de ligne de liste de genres.
     */
    static class ViewHolder {
        public TextView name;
    }

}