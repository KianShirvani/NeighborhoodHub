package com.example.neighborhoodhub;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class CategoryPagerAdapter extends FragmentStateAdapter {
    private List<Category> categories;

    public CategoryPagerAdapter(FragmentActivity activity, List<Category> categories) {
        super(activity);
        this.categories = categories;
    }

    @Override
    public Fragment createFragment(int position) {
        // Create new fragment for each category with its providers
        return CategoryFragment.newInstance(categories.get(position).getProviders());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
