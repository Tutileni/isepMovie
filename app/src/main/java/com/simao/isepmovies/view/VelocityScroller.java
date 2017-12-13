package com.simao.isepmovies.view;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
  * Basé sur OverScroller de Google, cette classe encapsule le défilement avec le
  * capacité à dépasser les limites d'une opération de défilement. Cette classe est un
  * Remplacement direct pour {@link android.widget.Scroller} dans la plupart des cas.
  * <p />
  * Par rapport à OverScroller de Google, cette classe ne contient que des modifications mineures
  * qui ajoutent un support pour le comportement "en défilement". En outre, le visqueux
  * le comportement fluide est pris de la classe Scroller de Google et inclus ici
  * parce qu'il n'est pas accessible au public via le SDK Android.
  */
public class VelocityScroller {
    private int mMode;

    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;

    private Interpolator mInterpolator;

    private final boolean mFlywheel;

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    private static float sViscousFluidScale;
    private static float sViscousFluidNormalize;

    private static float viscousFluid(float x) {
        x *= sViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float) Math.exp(-x));
        } else {
            float start = 0.36787944117f;   // 1/e == exp(-1)
            x = 1.0f - (float) Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= sViscousFluidNormalize;
        return x;
    }

    /**
      * Crée un VelocityScroller avec un interpolateur de fluide visqueux et un volant moteur.
      *
      * @param contexte
      */
    public VelocityScroller(Context context) {
        this(context, null);
    }

    /**
      * Crée un VelocityScroller avec le volant activé.
      *
      * @param context Le contexte de cette application.
      * @param interpolator L'interpolateur de défilement. Si null, un interpolateur par défaut (visqueux)
      * être utilisé.
      */
    public VelocityScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    /**
      * Crée un VelocityScroller.
      *
      * @param context Le contexte de cette application.
      * @param interpolator L'interpolateur de défilement. Si null, un interpolateur par défaut (visqueux)
      * être utilisé.
      * @param flywheel Si vrai, les mouvements successifs continueront d'augmenter la vitesse de défilement.
      * @cacher
      */
    public VelocityScroller(Context context, Interpolator interpolator, boolean flywheel) {
        mInterpolator = interpolator;
        mFlywheel = flywheel;
        mScrollerX = new SplineOverScroller(context);
        mScrollerY = new SplineOverScroller(context);
    }

    /**
      * Crée un VelocityScroller avec le volant activé.
      *
      * @param context Le contexte de cette application.
      * @param interpolator L'interpolateur de défilement. Si null, un interpolateur par défaut (visqueux)
      * être utilisé.
      * @param bounceCoefficientX Une valeur comprise entre 0 et 1 qui déterminera la proportion de
      * vélocité qui est conservée dans le rebond lorsque le bord horizontal est atteint. Une valeur nulle
      * signifie pas de rebond. Ce comportement n'est plus supporté et ce coefficient n'a aucun effet.
      * @param bounceCoefficientY Identique à bounceCoefficientX mais pour la direction verticale. Ce
      * le comportement n'est plus supporté et ce coefficient n'a aucun effet.
      *! deprecated Utilisez {! link #VelocityScroller (Context, Interpolator, boolean)} à la place.
      */
    public VelocityScroller(Context context, Interpolator interpolator,
                            float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    /**
     * Crée un VelocityScroller.
     *
     * @param context Le contexte de cette application.
     * @param interpolator L'interpolateur de défilement. Si null, un interpolateur par défaut (visqueux)
     * être utilisé.
     * @param bounceCoefficientX Une valeur comprise entre 0 et 1 qui déterminera la proportion de
     * vélocité qui est conservée dans le rebond lorsque le bord horizontal est atteint. Une valeur nulle
     * signifie pas de rebond. Ce comportement n'est plus supporté et ce coefficient n'a aucun effet.
     * @param bounceCoefficientY Identique à bounceCoefficientX mais pour la direction verticale. Ce
     * le comportement n'est plus supporté et ce coefficient n'a aucun effet.
     * @param flywheel Si vrai, les mouvements successifs continueront d'augmenter la vitesse de défilement.
     *! deprecated Utilisez {! link VelocityScroller (Contexte, Interpolator, boolean)} à la place.
     */
    public VelocityScroller(Context context, Interpolator interpolator,
                            float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
      * La quantité de friction appliquée aux flings. La valeur par défaut
      * est {@link android.view.ViewConfiguration # getScrollFriction}.
      *
      * @param friction Une valeur scalaire sans dimension représentant le coefficient de
      * friction.
      */
    public final void setFriction(float friction) {
        mScrollerX.setFriction(friction);
        mScrollerY.setFriction(friction);
    }

    /**
      * Retourne si le défilement a fini de défiler.
      *
      * @return Vrai si le défilement a fini de défiler, faux sinon.
      */
    public final boolean isFinished() {
        return mScrollerX.mFinished && mScrollerY.mFinished;
    }

    /**
      * Force le champ fini à une valeur particulière. Contrairement à
      * {@link #abortAnimation ()}, forçant l'animation à finir
      * ne provoque pas le déplacement du défilement vers les x et y finaux
      * position.
      *
      * @param finished La nouvelle valeur finie.
      */
    public final void forceFinished(boolean finished) {
        mScrollerX.mFinished = mScrollerY.mFinished = finished;
    }

    /**
      * Renvoie le décalage X actuel dans le défilement.
      *
      * @return Le nouveau décalage X en tant que distance absolue par rapport à l'origine.
      * /@return The new X offset as an absolute distance from the origin.
     */
    public final int getCurrX() {
        return mScrollerX.mCurrentPosition;
    }

    /**
      * Renvoie le décalage Y actuel dans le défilement.
      *
      * @return Le nouveau décalage Y en tant que distance absolue par rapport à l'origine.
      */
    public final int getCurrY() {
        return mScrollerY.mCurrentPosition;
    }

    /**
      * Renvoie la valeur absolue de la vitesse actuelle.
      *
      * @return La vitesse d'origine moins la décélération, norme du vecteur vitesse X et Y.
      */
    public float getCurrVelocity() {
        float squaredNorm = mScrollerX.mCurrVelocity * mScrollerX.mCurrVelocity;
        squaredNorm += mScrollerY.mCurrVelocity * mScrollerY.mCurrVelocity;
        return (float) Math.sqrt(squaredNorm);
    }

    /**
      * Renvoie le décalage X de départ dans le défilement.
      *
      * @return Le début X offset est une distance absolue par rapport à l'origine.
      */
    public final int getStartX() {
        return mScrollerX.mStart;
    }

    /**
      * Renvoie le début Y offset dans le défilement.
      *
      * @return Le début Y offset comme une distance absolue de l'origine.
      */
    public final int getStartY() {
        return mScrollerY.mStart;
    }

   /**
      * Retourne où le défilement se terminera. Valable uniquement pour les parchemins "fling".
      *
      * @return Le décalage X final en tant que distance absolue par rapport à l'origine.
      */
    public final int getFinalX() {
        return mScrollerX.mFinal;
    }

    /**
      * Retourne où le défilement se terminera. Valable uniquement pour les parchemins "fling".
      *
      * @return Le décalage Y final en tant que distance absolue par rapport à l'origine.
      */
    public final int getFinalY() {
        return mScrollerY.mFinal;
    }

    /**
      * Renvoie la durée de l'événement de défilement, en millisecondes.
      *
      * @return La durée du défilement en millisecondes.
      * @hide En attente de suppression une fois que cela ne dépend plus
      * VelocityScroller @deprecated n'a pas nécessairement une durée fixe.
      * Cette fonction sera au mieux de ses capacités.
      */
    @Deprecated
    public final int getDuration() {
        return Math.max(mScrollerX.mDuration, mScrollerY.mDuration);
    }

    /**
      * Étendre l'animation de défilement. Cela permet à une animation en cours de défiler
      * plus loin et plus longtemps, lorsqu'il est utilisé avec {@link #setFinalX (int)} ou {@link #setFinalY (int)}.
      *
      * @param extend Temps supplémentaire pour défiler en millisecondes.
      * @hide En attente de suppression une fois que cela ne dépend plus
      * @see #setFinalX (int)
      * @see #setFinalY (int)
      * VelocityScroller @deprecated n'a pas nécessairement une durée fixe.
      * Au lieu de définir une nouvelle position finale et d'étendre
      * la durée d'un parchemin existant, utilisez startScroll
      * pour commencer une nouvelle animation.
      */
    @Deprecated
    public void extendDuration(int extend) {
        mScrollerX.extendDuration(extend);
        mScrollerY.extendDuration(extend);
    }

    /**
      * Définit la position finale (X) pour ce défilement.
      *
      * @param newX Le nouveau décalage X en tant que distance absolue par rapport à l'origine.
      * @hide En attente de suppression une fois que cela ne dépend plus
      * @see #extendDuration (int)
      * @see #setFinalY (int)
      * La position finale de VelocityScroller @deprecated peut changer au cours d'une animation.
      * Au lieu de définir une nouvelle position finale et d'étendre
      * la durée d'un parchemin existant, utilisez startScroll
      * pour commencer une nouvelle animation.
      */
    @Deprecated
    public void setFinalX(int newX) {
        mScrollerX.setFinalPosition(newX);
    }

    /**
      * Définit la position finale (Y) pour ce défilement.
      *
      * @param newY Le nouveau décalage Y en tant que distance absolue par rapport à l'origine.
      * @hide En attente de suppression une fois que cela ne dépend plus
      * @see #extendDuration (int)
      * @see #setFinalX (int)
      * La position finale de VelocityScroller @deprecated peut changer au cours d'une animation.
      * Au lieu de définir une nouvelle position finale et d'étendre
      * la durée d'un parchemin existant, utilisez startScroll
      * pour commencer une nouvelle animation.
      */
    @Deprecated
    public void setFinalY(int newY) {
        mScrollerY.setFinalPosition(newY);
    }

    /**
      * Appelez ceci lorsque vous voulez connaître le nouvel emplacement. Si cela devient vrai, le
      * L'animation n'est pas encore terminée.
      */
    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }

        switch (mMode) {
            case SCROLL_MODE:
                long time = AnimationUtils.currentAnimationTimeMillis();
                // Tout scroller peut être utilisé pour le temps, car ils ont été démarrés
                // ensemble en mode défilement. Nous utilisons X ici.
                final long elapsedTime = time - mScrollerX.mStartTime;

                final int duration = mScrollerX.mDuration;
                if (elapsedTime < duration) {
                    float q = (float) (elapsedTime) / duration;

                    if (mInterpolator == null) {
                        q = viscousFluid(q);
                    } else {
                        q = mInterpolator.getInterpolation(q);
                    }

                    mScrollerX.updateScroll(q);
                    mScrollerY.updateScroll(q);
                } else {
                    abortAnimation();
                }
                break;

            case FLING_MODE:
                if (!mScrollerX.mFinished) {
                    if (!mScrollerX.update()) {
                        if (!mScrollerX.continueWhenFinished()) {
                            mScrollerX.finish();
                        }
                    }
                }

                if (!mScrollerY.mFinished) {
                    if (!mScrollerY.update()) {
                        if (!mScrollerY.continueWhenFinished()) {
                            mScrollerY.finish();
                        }
                    }
                }

                break;
        }

        return true;
    }

    /**
      * Commencez à défiler en fournissant un point de départ et la distance à parcourir.
      * Le parchemin utilisera la valeur par défaut de 250 millisecondes pour
      * durée.
      *
      * @param startX Démarrage du défilement horizontal en pixels. Positif
      * Les numéros feront défiler le contenu vers la gauche.
      * @param startY Démarrage du défilement vertical en pixels. Nombres positifs
      * fera défiler le contenu vers le haut.
      * @param dx Distance horizontale au déplacement. Les nombres positifs feront défiler le
      * contenu à gauche.
      * @param dy Distance verticale au voyage. Les nombres positifs feront défiler le
      * contenu.
      */
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    /**
      * Commencez à défiler en fournissant un point de départ et la distance à parcourir.
      *
      * @param startX Démarrage du défilement horizontal en pixels. Positif
      * Les numéros feront défiler le contenu vers la gauche.
      * @param startY Démarrage du défilement vertical en pixels. Nombres positifs
      * fera défiler le contenu vers le haut.
      * @param dx Distance horizontale au déplacement. Les nombres positifs feront défiler le
      * contenu à gauche.
      * @param dy Distance verticale au voyage. Les nombres positifs feront défiler le
      * contenu.
      * @param duration Durée du défilement en millisecondes.
      */
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mScrollerX.startScroll(startX, dx, duration);
        mScrollerY.startScroll(startY, dy, duration);
    }

    /**
      * Appelez ceci lorsque vous voulez revenir en arrière dans une plage de coordonnées valide.
      *
      * @param startX Starting Coordonnée X
      * @param startY Début coordonnée Y
      * @param minX Valeur X valide minimale
      * @param maxX Valeur X valide maximale
      * @param minY Valeur Y minimale valide
      * @param maxY Valeur Y minimale valide
      * @return vrai si un springback a été initié, false si startX et startY
      * déjà dans la plage valide.
      */
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        mMode = FLING_MODE;

        // Make sure both methods are called.
        final boolean spingbackX = mScrollerX.springback(startX, minX, maxX);
        final boolean spingbackY = mScrollerY.springback(startY, minY, maxY);
        return spingbackX || spingbackY;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
                      int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    /**
     * Commencez à défiler en fonction d'un geste de lancement. La distance parcourue sera
     * dépend de la vitesse initiale de la décharge.
     *
     * @param startX Point de départ du défilement (X)
     * @param startY Point de départ du parchemin (Y)
     * @param velocityX Vitesse initiale de la poussée (X) mesurée en pixels par
     * seconde.
     * @param vélocitéY Vitesse initiale de la décharge (Y) mesurée en pixels par
     * seconde
     * @param minX Valeur X minimum. Le défilement ne défilera pas au-delà de ce point
     * sauf si overX> 0. Si overfling est autorisé, il utilisera minX comme
     * une limite élastique.
     * @param maxX Valeur X maximale. Le défilement ne défilera pas au-delà de ce point
     * sauf si overX> 0. Si overfling est autorisé, il utilisera maxX comme
     * une limite élastique.
     * @param minY Valeur Y minimale. Le défilement ne défilera pas au-delà de ce point
     * sauf si surY> 0. Si l'overfling est autorisé, il utilisera minY comme
     * une limite élastique.
     * @param maxY Valeur Y maximale. Le défilement ne défilera pas au-delà de ce point
     * sauf si overY> 0. Si overfling est autorisé, il utilisera maxY comme
     * une limite élastique.
     * @param overX Plage de débordement. Si> 0, un sur-remplissage horizontal dans l'un ou
     * la direction sera possible.
     * @param overY Plage de débordement. Si> 0, une suralimentation verticale dans l'un ou
     * la direction sera possible.
     */
    public void fling(int startX, int startY, int velocityX, int velocityY,
                      int minX, int maxX, int minY, int maxY, int overX, int overY) {
        // Continue un parchemin ou lance en cours
        if (mFlywheel && !isFinished()) {
            float oldVelocityX = mScrollerX.mCurrVelocity;
            float oldVelocityY = mScrollerY.mCurrVelocity;
            if (Math.signum(velocityX) == Math.signum(oldVelocityX) &&
                    Math.signum(velocityY) == Math.signum(oldVelocityY)) {
                velocityX += oldVelocityX;
                velocityY += oldVelocityY;
            }
        }

        mMode = FLING_MODE;
        mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    /**
      * Notifier le scroller que nous avons atteint une limite horizontale.
      * Normalement, les informations pour gérer ceci seront déjà connues
      * lorsque l'animation est lancée, comme dans un appel à l'un des
      * fonctions de lancement. Cependant, il y a des cas où cela ne peut être connu
      * en avance. Cette fonction va faire passer le mouvement actuel et
      * animer de startX à finalX selon le cas.
      *
      * @param startX Position de départ / actuelle X
      * @param finalX Position finale X souhaitée
      * @param overX Magnitude de l'overscroll autorisée. Cela devrait être le maximum
      * distance souhaitée de finalX. Valeur absolue - doit être positif.
      */
    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    /**
      * Avertissez le défileur que nous avons atteint une limite verticale.
      * Normalement, les informations pour gérer ceci seront déjà connues
      * lorsque l'animation est lancée, comme dans un appel à l'un des
      * fonctions de lancement. Cependant, il y a des cas où cela ne peut être connu
      * en avance. Cette fonction va animer un mouvement parabolique de
      * début à la fin.
      *
      * @param startY Début / position actuelle Y
      * @param finalY Position Y finale souhaitée
      * @param overY Magnitude d'overscroll autorisée. Cela devrait être le maximum
      * distance souhaitée de finalY. Valeur absolue - doit être positif.
      */
    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public void notifyFinalXExtended(int finalX) {
        mScrollerX.notifyFinalPositionExtended(finalX);
    }

    public void notifyFinalYExtended(int finalY) {
        mScrollerY.notifyFinalPositionExtended(finalY);
    }

    /**
      * Renvoie si le Scroller en cours retourne actuellement à une position valide.
      * Les limites valides ont été fournies par le
      * {@link #fling (int, int, int, int, int, int, int, int, int, int)} méthode.
      * <p />
      * On devrait vérifier cette valeur avant d'appeler
      * {@link #startScroll (int, int, int, int)} comme interpolation en cours
      * pour restaurer une position valide sera alors arrêté. L'appelant doit prendre en compte
      * le fait que le défilement démarré commence à partir d'une position surrélevée.
      *
      * @return vrai lorsque la position actuelle est surfacturé et en cours de
      * interpolation à une valeur valide.
      */
    public boolean isOverScrolled() {
        return ((!mScrollerX.mFinished &&
                mScrollerX.mState != SplineOverScroller.SPLINE) ||
                (!mScrollerY.mFinished &&
                        mScrollerY.mState != SplineOverScroller.SPLINE));
    }

    /**
      * Arrête l'animation. Contrairement à {@link #forceFinished (boolean)},
      * abandonner l'animation provoque le déplacement du défilement vers les x et y finaux
      * positions.
      *
      * @see #forceFinished (booléen)
      */
    public void abortAnimation() {
        mScrollerX.finish();
        mScrollerY.finish();
    }

    /**
      * Renvoie le temps écoulé depuis le début du défilement.
      *
      * @return Le temps écoulé en millisecondes.
      * @cacher
      */
    public int timePassed() {
        final long time = AnimationUtils.currentAnimationTimeMillis();
        final long startTime = Math.min(mScrollerX.mStartTime, mScrollerY.mStartTime);
        return (int) (time - startTime);
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        final int dx = mScrollerX.mFinal - mScrollerX.mStart;
        final int dy = mScrollerY.mFinal - mScrollerY.mStart;
        return !isFinished() && Math.signum(xvel) == Math.signum(dx) &&
                Math.signum(yvel) == Math.signum(dy);
    }

    static class SplineOverScroller {
        // Position initiale
        private int mStart;

        // Position actuelle
        private int mCurrentPosition;

        // Position finale
        private int mFinal;

        // Vitesse initiale
        private int mVelocity;

        // Vitesse actuelle
        private float mCurrVelocity;

        // Décélération de courant constant
        private float mDeceleration;

        // Heure de début de l'animation, en millisecondes système
        private long mStartTime;

        // Durée de l'animation, en millisecondes
        private int mDuration;

        // Durée pour compléter le composant spline de l'animation
        private int mSplineDuration;

        // Distance pour se déplacer le long de l'animation spline
        private int mSplineDistance;

        // Si l'animation est actuellement en cours
        private boolean mFinished;

        // La distance de dépassement autorisée avant la limite est atteinte.
        private int mOver;

        // Friction de Fling
        private float mFlingFriction = ViewConfiguration.getScrollFriction();

        // Etat actuel de l'animation
        private int mState = SPLINE;

        // Valeur de gravité constante, utilisée dans la phase de décélération.
        private static final float GRAVITY = 2000.0f;

        // Un coefficient spécifique au contexte ajusté aux valeurs physiques.
        private float mPhysicalCoeff;

        private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        private static final float INFLEXION = 0.35f; // Les lignes de tension se croisent à (INFLEXION, 1)
        private static final float START_TENSION = 0.5f;
        private static final float END_TENSION = 1.0f;
        private static final float P1 = START_TENSION * INFLEXION;
        private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

        private static final int NB_SAMPLES = 100;
        private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
        private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];

        private static final int SPLINE = 0;
        private static final int CUBIC = 1;
        private static final int BALLISTIC = 2;

        static {
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < NB_SAMPLES; i++) {
                final float alpha = (float) i / NB_SAMPLES;

                float x_max = 1.0f;
                float x, tx, coef;
                while (true) {
                    x = x_min + (x_max - x_min) / 2.0f;
                    coef = 3.0f * x * (1.0f - x);
                    tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                    if (Math.abs(tx - alpha) < 1E-5) break;
                    if (tx > alpha) x_max = x;
                    else x_min = x;
                }
                SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

                float y_max = 1.0f;
                float y, dy;
                while (true) {
                    y = y_min + (y_max - y_min) / 2.0f;
                    coef = 3.0f * y * (1.0f - y);
                    dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                    if (Math.abs(dy - alpha) < 1E-5) break;
                    if (dy > alpha) y_max = y;
                    else y_min = y;
                }
                SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
            }
            SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;

            // Cela contrôle l'effet de fluide visqueux (quelle quantité)
            sViscousFluidScale = 8.0f;
            // doit être réglé sur 1.0 (utilisé dans visqueuxFluid ())
            sViscousFluidNormalize = 1.0f;
            sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
        }

        void setFriction(float friction) {
            mFlingFriction = friction;
        }

        SplineOverScroller(Context context) {
            mFinished = true;
            final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
            mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                    * 39.37f
                    * ppi
                    * 0.84f;
        }

        void updateScroll(float q) {
            mCurrentPosition = mStart + Math.round(q * (mFinal - mStart));
        }

        /*
          * Obtenez une décélération signée qui réduira la vélocité.
          */
        static private float getDeceleration(int velocity) {
            return velocity > 0 ? -GRAVITY : GRAVITY;
        }

        /*
          * Modifie mDuration à la durée nécessaire pour obtenir du début à newFinal en utilisant le
          * interpolation spline. La durée précédente était nécessaire pour accéder à oldFinal.
          */
        private void adjustDuration(int start, int oldFinal, int newFinal) {
            final int oldDistance = oldFinal - start;
            final int newDistance = newFinal - start;
            final float x = Math.abs((float) newDistance / oldDistance);
            final int index = (int) (NB_SAMPLES * x);
            if (index < NB_SAMPLES) {
                final float x_inf = (float) index / NB_SAMPLES;
                final float x_sup = (float) (index + 1) / NB_SAMPLES;
                final float t_inf = SPLINE_TIME[index];
                final float t_sup = SPLINE_TIME[index + 1];
                final float timeCoef = t_inf + (x - x_inf) / (x_sup - x_inf) * (t_sup - t_inf);
                mDuration *= timeCoef;
            }
        }

        void startScroll(int start, int distance, int duration) {
            mFinished = false;

            mStart = start;
            mFinal = start + distance;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = duration;

            // Inutilisé
            mDeceleration = 0.0f;
            mVelocity = 0;
        }

        void finish() {
            mCurrentPosition = mFinal;
            // Non réinitialisé car WebView s'appuie sur cette valeur pour lancer rapidement.
            // TODO: restaure lorsque WebView utilise le lancement rapide implémenté dans cette classe.
            // mCurrVelocity = 0.0f;
            mFinished = true;
        }

        void setFinalPosition(int position) {
            mFinal = position;
            mFinished = false;
        }

        void extendDuration(int extend) {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final int elapsedTime = (int) (time - mStartTime);
            mDuration = elapsedTime + extend;
            mFinished = false;
        }

        boolean springback(int start, int min, int max) {
            mFinished = true;

            mStart = mFinal = start;
            mVelocity = 0;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 0;

            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }

            return !mFinished;
        }

        private void startSpringback(int start, int end, int velocity) {

            mFinished = false;
            mState = CUBIC;
            mStart = start;
            mFinal = end;
            final int delta = start - end;
            mDeceleration = getDeceleration(delta);
            // TODO take velocity into account
            mVelocity = -delta; // seul signe est utilisé
            mOver = Math.abs(delta);
            mDuration = (int) (1000.0 * Math.sqrt(-2.0 * delta / mDeceleration));
        }

        void fling(int start, int velocity, int min, int max, int over) {
            mOver = over;
            mFinished = false;
            mCurrVelocity = mVelocity = velocity;
            mDuration = mSplineDuration = 0;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mCurrentPosition = mStart = start;

            if (start > max || start < min) {
                startAfterEdge(start, min, max, velocity);
                return;
            }

            mState = SPLINE;
            double totalDistance = 0.0;

            if (velocity != 0) {
                mDuration = mSplineDuration = getSplineFlingDuration(velocity);
                totalDistance = getSplineFlingDistance(velocity);
            }

            mSplineDistance = (int) (totalDistance * Math.signum(velocity));
            mFinal = start + mSplineDistance;

            // Fixer à une position finale valide
            if (mFinal < min) {
                adjustDuration(mStart, mFinal, min);
                mFinal = min;
            }

            if (mFinal > max) {
                adjustDuration(mStart, mFinal, max);
                mFinal = max;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
        }

        private double getSplineFlingDistance(int velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
        }

        /* Renvoie la durée, exprimée en millisecondes */
        private int getSplineFlingDuration(int velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return (int) (1000.0 * Math.exp(l / decelMinusOne));
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            // Simule un rebond depuis le bord
            final float durationToApex = -velocity / mDeceleration;
            final float distanceToApex = velocity * velocity / 2.0f / Math.abs(mDeceleration);
            final float distanceToEdge = Math.abs(end - start);
            final float totalDuration = (float) Math.sqrt(
                    2.0 * (distanceToApex + distanceToEdge) / Math.abs(mDeceleration));
            mStartTime -= (int) (1000.0f * (totalDuration - durationToApex));
            mStart = end;
            mVelocity = (int) (-mDeceleration * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            mDeceleration = getDeceleration(velocity == 0 ? start - end : velocity);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            if (start > min && start < max) {
                Log.e("VelocityScroller", "startAfterEdge called from a valid position");
                mFinished = true;
                return;
            }
            final boolean positive = start > max;
            final int edge = positive ? max : min;
            final int overDistance = start - edge;
            boolean keepIncreasing = overDistance * velocity >= 0;
            if (keepIncreasing) {
                // entraînera un rebond ou un to_boundary en fonction de la vélocité.
                startBounceAfterEdge(start, edge, velocity);
            } else {
                final double totalDistance = getSplineFlingDistance(velocity);
                if (totalDistance > Math.abs(overDistance)) {
                    fling(start, velocity, positive ? min : start, positive ? start : max, mOver);
                } else {
                    startSpringback(start, edge, velocity);
                }
            }
        }

        void notifyEdgeReached(int start, int end, int over) {
            // mState is used to detect successive notifications
            if (mState == SPLINE) {
                mOver = over;
                mStartTime = AnimationUtils.currentAnimationTimeMillis();
                // Nous étions en mode lancer / défiler avant: la vitesse du courant est telle que la distance à
                // le bord augmente. Cela garantit que startAfterEdge ne démarrera pas une nouvelle impulsion.
                startAfterEdge(start, end, end, (int) mCurrVelocity);
            }
        }

        // TODO: Don't discard current velocity, use spline interpolation instead.
        public void notifyFinalPositionExtended(int position) {
            mOver = 0;
            mFinished = false;
            mDuration = mDuration - (int) (mStartTime - AnimationUtils.currentAnimationTimeMillis());

            if (mDuration < 50) {
                mDuration = 50;
            }

            mSplineDuration = mDuration;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStart = mCurrentPosition;
            mFinal = position;

            mState = SPLINE;

            mSplineDistance = mFinal - mStart;
        }

        private void onEdgeReached() {
            // mStart, mVelocity et mStartTime ont été ajustés à leurs valeurs lorsque edge a été atteint.
            float distance = mVelocity * mVelocity / (2.0f * Math.abs(mDeceleration));
            final float sign = Math.signum(mVelocity);

            if (distance > mOver) {
                // La décélération par défaut n'est pas suffisante pour nous ralentir avant la limite
                mDeceleration = -sign * mVelocity * mVelocity / (2.0f * mOver);
                distance = mOver;
            }

            mOver = (int) distance;
            mState = BALLISTIC;
            mFinal = mStart + (int) (mVelocity > 0 ? distance : -distance);
            mDuration = -(int) (1000.0f * mVelocity / mDeceleration);
        }

        boolean continueWhenFinished() {
            switch (mState) {
                case SPLINE:
                    // Durée du début à la vitesse nulle
                    if (mDuration < mSplineDuration) {
                        // Si l'animation était bloquée, nous avons atteint le bord
                        mStart = mFinal;
                        mVelocity = (int) mCurrVelocity;
                        mDeceleration = getDeceleration(mVelocity);
                        mStartTime += mDuration;
                        onEdgeReached();
                    } else {
                        // Arrêt normal, pas besoin de continuer
                        return false;
                    }
                    break;
                case BALLISTIC:
                    mStartTime += mDuration;
                    startSpringback(mFinal, mStart, 0);
                    break;
                case CUBIC:
                    return false;
            }

            update();
            return true;
        }

        /*
          * Mettre à jour la position actuelle et la vitesse pour l'heure actuelle. Résultats
          * true si la mise à jour a été effectuée et false si la durée de l'animation a été
          * atteint.
          */
        boolean update() {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final long currentTime = time - mStartTime;

            if (currentTime > mDuration) {
                return false;
            }

            double distance = 0.0;
            switch (mState) {
                case SPLINE: {
                    final float t = (float) currentTime / mSplineDuration;
                    final int index = (int) (NB_SAMPLES * t);
                    float distanceCoef = 1.f;
                    float velocityCoef = 0.f;
                    if (index < NB_SAMPLES) {
                        final float t_inf = (float) index / NB_SAMPLES;
                        final float t_sup = (float) (index + 1) / NB_SAMPLES;
                        final float d_inf = SPLINE_POSITION[index];
                        final float d_sup = SPLINE_POSITION[index + 1];
                        velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                    }

                    distance = distanceCoef * mSplineDistance;
                    mCurrVelocity = velocityCoef * mSplineDistance / mSplineDuration * 1000.0f;
                    break;
                }

                case BALLISTIC: {
                    final float t = currentTime / 1000.0f;
                    mCurrVelocity = mVelocity + mDeceleration * t;
                    distance = mVelocity * t + mDeceleration * t * t / 2.0f;
                    break;
                }

                case CUBIC: {
                    final float t = (float) (currentTime) / mDuration;
                    final float t2 = t * t;
                    final float sign = Math.signum(mVelocity);
                    distance = sign * mOver * (3.0f * t2 - 2.0f * t * t2);
                    mCurrVelocity = sign * mOver * 6.0f * (-t + t2);
                    break;
                }
            }

            mCurrentPosition = mStart + (int) Math.round(distance);

            return true;
        }
    }
}