package com.simao.isepmovies.adapter;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;


public class SearchDB {

    private static final String DBNAME = "search";
    private static final int VERSION = 1;
    private SearchDBOpenHelper mSearchDBOpenHelper;
    private static final String FIELD_id = "_id";
    private static final String FIELD_searchID = "searchID";
    private static final String FIELD_title = "title";
    private static final String FIELD_subTitle = "subTitle";
    private static final String FIELD_imgUrl = "imgUrl";
    private static final String FIELD_mediaType = "mediaType";
    private static final String TABLE1_NAME = "search";
    private static final String TABLE2_NAME = "suggestions";
    private HashMap<String, String> mAliasMap;

    public SearchDB(Context context) {
        mSearchDBOpenHelper = new SearchDBOpenHelper(context, DBNAME, null, VERSION);
        // Cette HashMap est utilisée pour mapper les champs de la table aux champs Suggestion personnalisée
        mAliasMap = new HashMap<>();
        // ID unique pour chaque suggestion (obligatoire)
        mAliasMap.put("_ID", FIELD_id + " as " + "_id");

        // Cette valeur sera ajoutée aux données Intent sur la sélection d'un élément dans le résultat de la recherche ou Suggestions (facultatif)
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, FIELD_searchID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);

        // Texte pour les suggestions (obligatoire)
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, FIELD_title + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);

        //Texte pour les suggestions (obligatoire)
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, FIELD_subTitle + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2);

        // Icon pour les suggestions (facultatif)
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, FIELD_imgUrl + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);

        // Icône pour les suggestions (facultatif)
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, FIELD_mediaType + " as " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
    }

    public synchronized Cursor autoComplete() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);
        queryBuilder.setTables(TABLE1_NAME);

        SQLiteDatabase db = mSearchDBOpenHelper.getReadableDatabase();
        Cursor c = null;
        if (db.isOpen()) {
            c = queryBuilder.query(db,
                    new String[]{"_ID",
                            SearchManager.SUGGEST_COLUMN_TEXT_1,
                            SearchManager.SUGGEST_COLUMN_TEXT_2,
                            SearchManager.SUGGEST_COLUMN_ICON_1,
                            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA},
                    null, null, null, null, null, "10"
            );
        }

        return c;
    }


    /**
     * Retours des Suggestions
     */
    public synchronized Cursor getSuggestions(String[] selectionArgs) {
        String selection = FIELD_title + " like ? ";

        if (selectionArgs != null) {
            if (!selectionArgs[0].isEmpty()) {
                selectionArgs[0].replaceAll("'", "");
                selectionArgs[0] = "%" + selectionArgs[0] + "%";
            } else {
                selection = null;
                selectionArgs = null;
            }
        }

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);
        queryBuilder.setTables(TABLE2_NAME);
        SQLiteDatabase db = mSearchDBOpenHelper.getReadableDatabase();
        Cursor c = null;
        if (db.isOpen()) {
            c = queryBuilder.query(db,
                    new String[]{"_ID",
                            SearchManager.SUGGEST_COLUMN_TEXT_1,
                            SearchManager.SUGGEST_COLUMN_TEXT_2,
                            SearchManager.SUGGEST_COLUMN_ICON_1,
                            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    FIELD_title + " asc ", "10"
            );
        }
        return c;
    }

    public int getSuggestionSize() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);

        queryBuilder.setTables(TABLE2_NAME);
        Cursor c = queryBuilder.query(mSearchDBOpenHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_TEXT_2,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA},
                null, null, null, null, null, "10"
        );
        return c.getCount();
    }


    public void insertAutoComplete(int id, String title, Uri posterPath, String subTitle, String mediaType) {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();

        // Définition de l'instruction d'insert
        String sql = "insert into " + TABLE1_NAME + " ( " +
                FIELD_searchID + " , " +
                FIELD_title + " , " +
                FIELD_imgUrl + " , " +
                FIELD_subTitle + " , " +
                FIELD_mediaType + ") " +
                " values ( " +
                " " + id + " ," +
                "  '" + title + "'  ," +
                "  '" + posterPath + "'  ," +
                "  '" + subTitle + "'  ," +
                " '" + mediaType + "' ) ";

        // Insertion de valeurs dans la table
        db.execSQL(sql);
    }

    public void cleanAutoCompleteRecords() {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Définition de l'instruction de DELETE
        String sql = "DELETE FROM " + TABLE1_NAME + " ; ";

        // Insertion de valeurs dans la table
        db.execSQL(sql);
    }


    public void insertSuggestion(int id, String title, Uri posterPath, String subTitle, String mediaType) {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Définition de l'instruction d'insert
        String sql = "insert into " + TABLE2_NAME + " ( " +
                FIELD_searchID + " , " +
                FIELD_title + " , " +
                FIELD_imgUrl + ", " +
                FIELD_subTitle + ", " +
                FIELD_mediaType + " ) " +
                " values ( " +
                " " + id + " ," +
                "  '" + title + "'  ," +
                "  '" + posterPath + "'  ," +
                "  '" + subTitle + "'  ," +
                " '" + mediaType + "' ) ";

        // Insertion de valeurs dans la table
        db.execSQL(sql);
    }

    public void cleanSuggestionRecords() {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Définition de l'instruction de DELETE
        String sql = "DELETE FROM " + TABLE2_NAME + " ; ";

        // Insertion de valeurs dans la table
        db.execSQL(sql);
    }

    public void updateImg(int currId, Uri uriFile) {
        SQLiteDatabase db = mSearchDBOpenHelper.getWritableDatabase();
        // Définition de l'instruction d'UPDATE
        String sql = "UPDATE " + TABLE1_NAME + " SET " + FIELD_imgUrl + "=\"" + uriFile + "\" WHERE " + FIELD_searchID + "=" + currId + "; ";


        // Insertion de valeurs dans la table
        db.execSQL(sql);
    }


    private class SearchDBOpenHelper extends SQLiteOpenHelper {

        public SearchDBOpenHelper(Context context,
                                  String name,
                                  CursorFactory factory,
                                  int version) {
            super(context, DBNAME, factory, VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            //Définir la structure de la table
            String sql = " CREATE TABLE " + TABLE1_NAME + "" +
                    " ( " +
                    FIELD_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FIELD_searchID + " INTEGER, " +
                    FIELD_title + " VARCHAR(100), " +
                    FIELD_imgUrl + " VARCHAR(100), " +
                    FIELD_subTitle + " VARCHAR(100), " +
                    FIELD_mediaType + " VARCHAR(100) " +
                    " ) ";

            // Créer une table
            db.execSQL(sql);

            //Définir la structure de la table
            sql = " CREATE TABLE " + TABLE2_NAME + "" +
                    " ( " +
                    FIELD_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FIELD_searchID + " INTEGER, " +
                    FIELD_title + " VARCHAR(100), " +
                    FIELD_imgUrl + " VARCHAR(100), " +
                    FIELD_subTitle + " VARCHAR(100), " +
                    FIELD_mediaType + " VARCHAR(100) " +
                    " ) ";

            // Créer une table
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
        }
    }

}