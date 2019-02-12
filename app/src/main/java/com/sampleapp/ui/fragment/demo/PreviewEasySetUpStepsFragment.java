package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.BaseFragment;

public class PreviewEasySetUpStepsFragment extends BaseFragment implements View.OnClickListener {

    private ViewPager vViewPager;
    private Button vButtonGotIt;
    private Button vButtonGotItHighlighted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_demo_preview, container, false);
        changeActionBarState(false, true, getString(R.string.walkthru_mode));

        initViews(view);
        vButtonGotIt.setOnClickListener(this);
        vButtonGotItHighlighted.setOnClickListener(this);

        StepsPagerAdapter pagerAdapter = new StepsPagerAdapter(getChildFragmentManager());
        vViewPager.setAdapter(pagerAdapter);
        setListenerOnChangeFragment(view);

        return view;
    }

    private void initViews(View view) {
        vViewPager = (ViewPager) view.findViewById(R.id.demo_preview_pager);
        vButtonGotIt = (Button) view.findViewById(R.id.button_got_it);
        vButtonGotItHighlighted = (Button) view.findViewById(R.id.button_got_it_highlighted);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_got_it:
            case R.id.button_got_it_highlighted:
                getActivity().onBackPressed();
        }
    }

    private void setListenerOnChangeFragment(View view) {
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        vViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ((RadioButton) radioGroup.getChildAt(position)).setChecked(true);

                vButtonGotIt.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                vButtonGotItHighlighted.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private class StepsPagerAdapter extends FragmentPagerAdapter {
        private final int countFragments = 3;

        StepsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new PreviewEasySteps1();
                    break;
                case 1:
                    fragment = new PreviewEasySteps2();
                    break;
                case 2:
                    fragment = new PreviewEasySteps3();
                    break;
                default:
                    fragment = new PreviewEasySteps1();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return countFragments;
        }
    }

}
