package com.optic.socialmediagamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.models.SliderItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends PagerAdapter {

    private Context context;
    private List<SliderItem> mSliderItems = new ArrayList<>();

    public SliderAdapter(Context context, List<SliderItem> sliderItems) {
        this.context = context;
        this.mSliderItems = sliderItems;
    }

    @Override
    public int getCount() {
        return mSliderItems != null ? mSliderItems.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.slider_layout_item, container, false);
        ImageView imageView = view.findViewById(R.id.imageViewSlider);
        SliderItem sliderItem = mSliderItems.get(position);
        if (sliderItem.getImageUrl() != null && !sliderItem.getImageUrl().isEmpty()) {
            Picasso.with(context).load(sliderItem.getImageUrl()).into(imageView);
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
