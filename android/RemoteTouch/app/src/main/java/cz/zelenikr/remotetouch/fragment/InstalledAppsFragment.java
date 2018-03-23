package cz.zelenikr.remotetouch.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.app.AppInfoRecyclerViewAdapter;
import cz.zelenikr.remotetouch.data.AppInfo;
import cz.zelenikr.remotetouch.helper.AndroidAppHelper;

/**
 * A fragment representing a list of installed applications.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListItemStateChangedListener}
 * interface.
 */
public class InstalledAppsFragment extends Fragment {

    private static final String
        ARG_SELECTED_APPS = "param1";

    private OnListItemStateChangedListener mListener;
    private AppInfoRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstalledAppsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param selectedApps apps that should be already selected in the list
     * @return A new instance of fragment InstalledAppsFragment.
     */
    public static InstalledAppsFragment newInstance(ArrayList<String> selectedApps) {
        InstalledAppsFragment fragment = new InstalledAppsFragment();
        Bundle args = new Bundle();
        if (selectedApps != null && !selectedApps.isEmpty()) {
            args.putStringArrayList(ARG_SELECTED_APPS, selectedApps);
        }
        if (!args.isEmpty()) fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the adapter
        List<AppInfo> installedApps = AndroidAppHelper.getApps(getContext());
        adapter = new AppInfoRecyclerViewAdapter(installedApps, mListener);
        // Process arguments
        if (getArguments() != null) {
            List<String> selectedApps = getArguments().getStringArrayList(ARG_SELECTED_APPS);
            if (selectedApps != null) setSelected(selectedApps);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appinfo, container, false);

        // Set the list
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListItemStateChangedListener) {
            mListener = (OnListItemStateChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OnListItemStateChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_appinfo, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_appinfo_search);
        initSearchView(searchItem);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Checks items with specif app package name and update UI list.<p/>
     * Call this method after {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * was called.
     *
     * @param packages set of application package names
     */
    public void setSelected(Collection<String> packages) {
        adapter.selectByPackage(packages);
    }

    private void initSearchView(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListItemStateChangedListener {
        /**
         * @param item     the changed item
         * @param position position of item in the adapter
         */
        void onStateChanged(AppInfo item, int position);
    }
}
