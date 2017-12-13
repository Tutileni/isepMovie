package com.simao.isepmovies.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.simao.isepmovies.R;

/**
  * A utiliser avec ViewPager pour fournir un composant d'indicateur de tabulation qui donne un retour constant sur
  * La progression du défilement de l'utilisateur.
  * <p />
  * Pour utiliser le composant, ajoutez-le simplement à la hiérarchie de votre vue. Puis dans votre
  * {@link android.app.Activity} ou {@link android.support.v4.app.Fragment} appel
  * {@link #setViewPager (android.support.v4.view.ViewPager)} fournissant le ViewPager pour lequel cette disposition est utilisée.
  * <p />
  * Les couleurs peuvent être personnalisées de deux manières. Le premier et le plus simple est de fournir un tableau de couleurs
  * via {@link #setSelectedIndicatorColors (int ...)} et {@link #setDividerColors (int ...)}. le
  * alternative via l'interface {@link TabColorizer} qui vous permet un contrôle complet sur
  * Quelle couleur est utilisée pour chaque position individuelle.
  * <p />
  * Les vues utilisées comme onglets peuvent être personnalisées en appelant {@link #setCustomTabView (int, int)},
  * fournir l'ID de mise en page de votre mise en page personnalisée.
  */
public class SlidingTabLayout extends HorizontalScrollView {

    /**
      * Permet un contrôle complet sur les couleurs dessinées dans la disposition de l'onglet. Fixé avec
      * {@link #setCustomTabColorizer (TabColorizer)}.
      */
    public interface TabColorizer {

        /**
          * @return renvoie la couleur de l'indicateur utilisé lorsque {@code position} est sélectionné.
          */
        int getIndicatorColor(int position);

        /**
          * @return renvoie la couleur du diviseur dessiné à droite de {@code position}.
          */
        int getDividerColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS = 24;
    private static int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 14;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final SlidingTabStrip mTabStrip;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, (int) (49 * getResources().getDisplayMetrics().density));
    }

    /**
      * Définissez le {@link TabColorizer} personnalisé à utiliser.
      * <p />
      * Si vous n'avez besoin que d'une simple custmisation, vous pouvez utiliser
      * {@link #setSelectedIndicatorColors (int ...)} et {@link #setDividerColors (int ...)} pour atteindre
      * effets similaires.
      */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
      * Définit les couleurs à utiliser pour indiquer l'onglet sélectionné. Ces couleurs sont traitées comme
      * tableau circulaire. Fournir une couleur signifie que tous les onglets sont indiqués avec la même couleur.
      */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
      * Définit les couleurs à utiliser pour les diviseurs de tabulation. Ces couleurs sont traitées comme un tableau circulaire.
      * Fournir une couleur signifie que tous les onglets sont indiqués avec la même couleur.
      */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    /**
      * Définissez le {@link ViewPager.OnPageChangeListener}. Lorsque vous utilisez {@link SlidingTabLayout}, vous êtes
      * requis pour définir {@link ViewPager.OnPageChangeListener} via cette méthode. C'est tellement
      * que la mise en page peut mettre à jour sa position de défilement correctement.
      *
      * @voir ViewPager # setOnPageChangeListener (ViewPager.OnPageChangeListener)
      */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
      * Définissez la mise en page personnalisée à gonfler pour les vues à onglets.
      *
      * @param layoutResId ID de mise en page à gonfler
      * @param textViewId id du {@link android.widget.TextView} dans la vue gonflée
      */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    /**
      * Définit le pager de vue associé. Notez que l'hypothèse ici est que le contenu du téléavertisseur
      * (nombre d'onglets et titres d'onglet) ne change pas après que cet appel a été effectué.
      */
    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    /**
      * Créer une vue par défaut à utiliser pour les onglets. Ceci est appelé si une vue d'onglet personnalisée n'est pas définie via
      * {@link #setCustomTabView (int, int)}.
      */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Si nous courons sur Honeycomb ou plus récent, alors nous pouvons utiliser le thème
            // selectableItemBackground pour s'assurer que la vue a un état enfoncé
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                    outValue, true);
            textView.setBackgroundResource(outValue.resourceId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Si vous utilisez ICS ou plus récent, activez toutes les majuscules pour qu'elles correspondent au style de l'onglet Barre d'actions
            textView.setAllCaps(true);
        }

        boolean phone = getResources().getBoolean(R.bool.portrait_only);
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int width = size.x;
        int height = size.y;
        // cas spécial pour tablettes de 7 pouces
        // si on laisse le remplissage à 16 le titre du texte sur le viewPager sera coupé
        if (!phone && ((width == 1024 && height == 600) || (height == 1024 && width == 600)))
            TAB_VIEW_PADDING_DIPS = 14;


        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        return textView;
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final View.OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView = null;
            TextView tabTitleView = null;

            if (mTabViewLayoutId != 0) {
                // S'il existe un ID d'affichage de présentation d'onglets personnalisé, essayez de le gonfler
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
                        false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
            }

            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }

            tabTitleView.setText(adapter.getPageTitle(i));
            tabView.setOnClickListener(tabClickListener);

            mTabStrip.addView(tabView);

            tabTitleView.setTextColor(getResources().getColorStateList(R.color.tabselector));

            if (i == mViewPager.getCurrentItem()) {
                tabView.setSelected(true);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // Si nous ne sommes pas au premier enfant et que nous sommes au milieu du rouleau, assurez-vous d'obéir au décalage
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }

            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(position == i);
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class TabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

}
