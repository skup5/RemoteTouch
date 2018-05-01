package cz.zelenikr.remotetouch.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

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
public class InstalledAppsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String
        ARG_SELECTED_APPS = "param1";

    private OnListItemStateChangedListener mListener;
    private AppInfoRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

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
        List<String> selectedApps = null;
        // Process arguments
        if (getArguments() != null) {
            selectedApps = getArguments().getStringArrayList(ARG_SELECTED_APPS);
        }

        // Set the adapter
        prepareAdapter(selectedApps);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_appinfo, container, false);
        ViewGroup content = root.findViewById(R.id.appinfo_content);

        prepareListTypeSpinner((ViewGroup) root);
        prepareListView(inflater, (ViewGroup) content);

        return root;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_appinfo_select_all:
                onSelectAllApps();
                return true;
            case R.id.menu_appinfo_unselect_all:
                onUnselectAllApps();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        adapter.setShowOnlySelected(position != 0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    private void prepareAdapter(List<String> selectedApps) {
        adapter = new AppInfoRecyclerViewAdapter(mListener);
        new AppsLoader().setSelectedApps(selectedApps).execute(this);
    }

    private void prepareListTypeSpinner(ViewGroup root) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
        //   R.array.app_list_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) root.findViewById(R.id.appinfo_list_spinner);
        // Apply the adapter to the spinner
        // spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void prepareListView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_appinfo_list, container, false);
        // Set the list
        recyclerView = (RecyclerView) view;
        recyclerView.setAdapter(adapter);
    }

    private void onSelectAllApps() {
        adapter.selectAll();
    }

    private void onUnselectAllApps() {
        adapter.unSelectAll();
    }
    
    private void changeViewContent(View newContent) {
        ViewGroup content = getView().findViewById(R.id.appinfo_content);
        content.removeAllViews();
        content.addView(newContent);
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
        void onItemStateChanged(AppInfo item, int position);

        /**
         * @param items list of changed items
         */
        void onItemsStateChanged(List<AppInfo> items);
    }

    private static class AppsLoader extends AsyncTask<InstalledAppsFragment, Void, List<AppInfo>> {
        List<String> selectedApps;
        InstalledAppsFragment fragment;

        @Override
        protected List<AppInfo> doInBackground(InstalledAppsFragment... fragments) {
            this.fragment = fragments[0];
            return AndroidAppHelper.getApps(fragment.getContext());
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfos) {
            fragment.changeViewContent(fragment.recyclerView);
            fragment.adapter.setData(appInfos);
            if (selectedApps != null) fragment.setSelected(selectedApps);
        }

        public AppsLoader setSelectedApps(List<String> selectedApps) {
            this.selectedApps = selectedApps;
            return this;
        }
    }

}
