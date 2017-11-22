package com.example.byunghwa.newsapp.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.byunghwa.newsapp.R;
import com.example.byunghwa.newsapp.activity.SettingsActivity;
import com.example.byunghwa.newsapp.adapter.CustomStaggeredGridLayoutManager;
import com.example.byunghwa.newsapp.adapter.NewsListRecyclerViewAdapter;
import com.example.byunghwa.newsapp.data.NewsListLoader;
import com.example.byunghwa.newsapp.interfaces.OnRefreshStarted;
import com.example.byunghwa.newsapp.model.News;
import com.example.byunghwa.newsapp.util.NetworkUtil;
import com.example.byunghwa.newsapp.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsListFragment extends Fragment implements NewsListRecyclerViewAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<List<News>>, OnRefreshStarted {

    private static final String TAG = "NewsListFrag";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private NewsListRecyclerViewAdapter mAdapter;

    private List<News> mNewsList;

    // this value is initialized to true
    private boolean isInitiallyStarted = true;

    // loader ID
    private static final int ID_LOADER = 1;

    // recyclerview column count
    private static final int COUNT_COLUMN_RECYCLERVIEW = 2;

    // loader argument key
    private static final String KEY_LOADER_TOPIC = "topic_news";

    public NewsListFragment() {
        // Required empty public constructor
    }

    private void fetchNewsList() {
        if (NetworkUtil.isNetworkAvailable(getContext())) {
            getLoaderManager().getLoader(ID_LOADER).forceLoad();
        } else {
            ToastUtil.showToastMessage(getActivity(), R.string.message_no_network_connection);

            /* only hide recyclerview if the list is empty

              we don't want to lose the existing news list if the refresh operation
              cannot be performed due to a lack of internet connection
             */
            if (mNewsList == null || mNewsList.size() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news_list, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler);

        mRecyclerView.setHasFixedSize(true);// setting this to true will prevent the whole list from refreshing when
        // new items have been added to the list (which prevents list from flashing)

        mAdapter = new NewsListRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        CustomStaggeredGridLayoutManager manager = new CustomStaggeredGridLayoutManager(COUNT_COLUMN_RECYCLERVIEW, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        /*
         note that when you use a SwipeRefreshLayout, you have to set this OnRefreshListener to it
         or the indicator wouldn't show up
          */
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewsList();
            }
        });

        mEmptyView = rootView.findViewById(R.id.tv_empty_view);

        // get topic from SharedPrefs and then store it into a Bundle which is then
        // passed to the loader
        Bundle bundle = new Bundle();
        bundle.putString(KEY_LOADER_TOPIC, getTopFromPrefs());

        getLoaderManager().initLoader(ID_LOADER, bundle, this).forceLoad();

        setOnClickListener();

        return rootView;
    }

    private void setOnClickListener() {
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(View view, int clickedItemPosition) {
        if (mNewsList != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNewsList.get(clickedItemPosition).getWebURL()));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private String getTopFromPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getString(getString(SettingsActivity.MainSettingsFragment.keyPreference), getString(R.string.pref_topic_value));
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "topic: " + args.getString(KEY_LOADER_TOPIC));
        return new NewsListLoader(getActivity(), args.getString(KEY_LOADER_TOPIC), this);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
        if (data != null) {
            mNewsList = data;
            Log.i(TAG, "news list size: " + data.size());
            mAdapter.swapData((ArrayList<News>) data);
            mSwipeRefreshLayout.setRefreshing(false);
            if (data.size() > 0) {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mAdapter.swapData(null);
    }

    // implementation for showing refresh indicator when the task starts
    @Override
    public void onTaskStarted() {
        // only show the refresh indicator when the app is initially started
        // when the user is back from the browser to the article list screen,
        // onStartLoading also gets called and we don't want to show the indicator
        Log.i(TAG, "onTaskStarted getting called");
        Log.i(TAG, "isInitiallyStarted: " + isInitiallyStarted);

        if (isInitiallyStarted) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        isInitiallyStarted = false;
    }

}
