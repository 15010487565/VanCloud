package com.vgtech.vancloud.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.vgtech.common.swipeback.SwipeBackFragment;

import roboguice.RoboGuice;

public class BaseSwipeBackFragment extends SwipeBackFragment {
    protected OnAddFragmentListener mAddFragmentListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectViewMembers(this);
    }
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnAddFragmentListener) {
            mAddFragmentListener = (OnAddFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAddFragmentListener = null;
    }

    public interface OnAddFragmentListener {
        void onAddFragment(Fragment fromFragment, Fragment toFragment);
    }
}
