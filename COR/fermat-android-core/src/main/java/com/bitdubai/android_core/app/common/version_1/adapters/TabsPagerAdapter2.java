package com.bitdubai.android_core.app.common.version_1.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;

import com.bitdubai.android_core.app.common.version_1.util.res_manager.ResourceLocationSearcherHelper;
import com.bitdubai.fermat_android_api.layer.definition.wallet.AbstractFermatFragmentInterface;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.FermatDrawable;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.Tab;

import java.lang.ref.WeakReference;

/**
 * Created by Matias Furszyfer on 2016.06.08..
 */
public class TabsPagerAdapter2<F extends Fragment & AbstractFermatFragmentInterface> extends FermatScreenAdapter<F> implements FermatUIAdapter<F> {

    private FermatDrawable[] drawables;
    private WeakReference<Context> contextWeakReference;

    public TabsPagerAdapter2(FragmentManager fragmentManager, Tab[] tabsTitles, F[] fragments) {
        super(fragmentManager, fragments);
        titles = tabsTitles;
    }

    public TabsPagerAdapter2(Context context, FragmentManager fragmentManager, Tab[] tabsTitles, F[] fragments, FermatDrawable[] fermatDrawable) {
        super(fragmentManager, fragments);
        titles = tabsTitles;
        this.drawables = fermatDrawable;
        this.contextWeakReference = new WeakReference<Context>(context);
    }

    public TabsPagerAdapter2(FragmentManager fm, F[] fragments) {
        super(fm, fragments);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        SpannableString title = null;
        Tab tab = titles[position];
        if (titles.length > 0) {
            title = new SpannableString(tab.getLabel());
        }
        if (drawables != null) {
            FermatDrawable fermatDrawable = drawables[position];
            if (fermatDrawable != null) {
                Drawable drawable = ResourceLocationSearcherHelper.obtainDrawable(contextWeakReference.get(), fermatDrawable);
                // Generate title based on item position
//        Drawable image = context.getResources().getDrawable(imageResId[position]);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                // Replace blank spaces with image icon
                SpannableString sb = null;
                if (title != null) {
                    sb = new SpannableString("   " + title);
                } else sb = new SpannableString("   ");
                ImageSpan imageSpan = new ImageSpan(drawable);
                sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return sb;
            }
        }
        if (title!=null) {
            if (tab.getTabTextSize() != 0) {
                title.setSpan(new AbsoluteSizeSpan(tab.getTabTextSize()), 0, tab.getLabel().length(), 0);
                title.setSpan(new RelativeSizeSpan(tab.getTabTextSize()), 0, tab.getLabel().length(), 0);
            }
        }


        return title;
    }


    @Override
    public Parcelable saveState() {
        return null;
    }

}