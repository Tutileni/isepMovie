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
import com.simao.isepmovies.MovieDB;
import com.simao.isepmovies.R;
import com.simao.isepmovies.model.TrailerModel;
/**
 * Adaptateur de remorque. Utilisé pour charger les informations de la remorque dans la liste de la remorque.
 */
public class TrailerAdapter extends ArrayAdapter<TrailerModel> {
    private ArrayList<TrailerModel> trailerList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private Context mContext;
    private ImageLoader imageLoader;

    public TrailerAdapter(Context context, int resource, ArrayList<TrailerModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        trailerList = objects;
        mContext = context;
        imageLoader = ImageLoader.getInstance();
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
            holder.filePath = (ImageView) v.findViewById(R.id.filePath);


            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (imageLoader.getDiskCache().get(MovieDB.trailerImageUrl + trailerList.get(position).getFilePath() + "/hqdefault.jpg").exists())
            imageLoader.displayImage(MovieDB.trailerImageUrl + trailerList.get(position).getFilePath() + "/hqdefault.jpg", holder.filePath, ((MainActivity) mContext).getOptionsWithoutFade());
        else
            imageLoader.displayImage(MovieDB.trailerImageUrl + trailerList.get(position).getFilePath() + "/hqdefault.jpg", holder.filePath, ((MainActivity) mContext).getOptionsWithFade());


        return v;

    }

    /**
     * Définit les éléments de la ligne de la liste de pistes.
     */
    static class ViewHolder {
        public ImageView filePath;
    }


}