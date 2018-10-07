package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentStatAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;

import static android.support.design.widget.TabLayout.MODE_FIXED;

public class StatDetailFragment extends MainActivityFragment {

    public static final String ARG_TOURNAMENT = "role";

    private Tournament tournament;

    public static StatDetailFragment newInstance(Tournament tournament) {
        StatDetailFragment fragment = new StatDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TOURNAMENT, tournament);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_TOURNAMENT));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_games_parent, container, false);
        ViewPager viewPager = root.findViewById(R.id.view_pager);
        TabLayout tabLayout = root.findViewById(R.id.tab_layout);

        viewPager.setAdapter(new TournamentStatAdapter(tournament, getChildFragmentManager()));
        tabLayout.setTabMode(MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        return root;
    }

    @Override
    public void togglePersistentUi() {
        setToolbarTitle(getString(R.string.tournament_stats));
        super.togglePersistentUi();
    }

    @Override
    public boolean showsFab() { return false; }
}