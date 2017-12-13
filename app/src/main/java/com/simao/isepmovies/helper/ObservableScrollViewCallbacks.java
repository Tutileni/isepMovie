package com.simao.isepmovies.helper;


/**
 * Rappels pour les widgets défilables.
 */
public interface ObservableScrollViewCallbacks {
    /**
      * Appelé lorsque les événements de changement de défilement ont eu lieu.
      * Ceci ne sera pas appelé juste après que la vue soit affichée, donc si vous voulez
      * initialisez la position de vos vues avec cette méthode, vous devriez appeler ceci manuellement
      * ou invoquer le défilement, selon le cas.
      *
      * @param scrollY position de défilement dans l'axe Y
      * @param firstScroll true quand il est appelé pour la première fois dans les événements de mouvement consécutifs
      * @param faisant glisser true lorsque la vue est déplacée et false lorsque la vue défile dans l'inertie
      */
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging);

    /**
     *  Appelé lorsque l'événement de mouvement vers le bas s'est produit.
     */
    public void onDownMotionEvent();

    /**
     * Appelé lorsque le glisser est terminé ou annulé.
     *
     * @param scrollState état pour indiquer la direction de défilement
     */
    public void onUpOrCancelMotionEvent(ScrollState scrollState);
}