package com.example.neighborhoodhub;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class LocalInfoActivity extends BaseActivity {
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_info);
        setupTopBar(false);
        parseXmlFile();

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        CategoryPagerAdapter adapter = new CategoryPagerAdapter(this, categories);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(categories.get(position).getName())
        ).attach();
    }

    private void parseXmlFile() {
        try {
            XmlResourceParser parser = getResources().getXml(R.xml.local_info);
            Category currentCategory = null;

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("category".equals(tagName)) {
                        currentCategory = new Category();
                        currentCategory.setName(parser.getAttributeValue(null, "name"));
                        currentCategory.setProviders(new ArrayList<>());
                        categories.add(currentCategory);
                    } else if ("provider".equals(tagName)) {
                        Provider provider = new Provider();
                        provider.setName(parser.getAttributeValue(null, "name"));
                        provider.setPhone(parser.getAttributeValue(null, "phone"));
                        provider.setAddress(parser.getAttributeValue(null, "address"));
                        provider.setHours(parser.getAttributeValue(null, "hours"));
                        currentCategory.getProviders().add(provider);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}