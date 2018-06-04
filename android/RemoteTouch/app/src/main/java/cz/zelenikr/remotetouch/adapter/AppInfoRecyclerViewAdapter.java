package cz.zelenikr.remotetouch.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.AppInfo;
import cz.zelenikr.remotetouch.fragment.InstalledAppsFragment;
import cz.zelenikr.remotetouch.fragment.InstalledAppsFragment.OnListItemStateChangedListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AppInfo} and makes a call to the
 * specified {@link InstalledAppsFragment.OnListItemStateChangedListener}.
 */
public class AppInfoRecyclerViewAdapter extends RecyclerView.Adapter<AppInfoRecyclerViewAdapter.ViewHolder>
    implements Filterable {

    public static final String TAG = AppInfoRecyclerViewAdapter.class.getSimpleName();

    private final InstalledAppsFragment.OnListItemStateChangedListener mListener;
    private final List<AppInfo> allItems;
    private List<AppInfo> filteredItemList, visibleItemList;
    private Filter searchFilter;
    private boolean showOnlySelected;

    public AppInfoRecyclerViewAdapter(OnListItemStateChangedListener mListener) {
        this(new ArrayList<>(), mListener);
    }

    public AppInfoRecyclerViewAdapter(List<AppInfo> items, OnListItemStateChangedListener listener) {
        showOnlySelected = false;
        filteredItemList = items;
        visibleItemList = items;
        allItems = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_appinfo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final AppInfo item = getActualData().get(position);
        // Get the current package name
        final String packageName = item.getAppPackage();

        // Get the current app icon
        Drawable icon = item.getAppIcon();

        // Get the current app label
        String label = item.getAppName();

        // Set the current app label
        holder.mTextViewLabel.setText(label);
        /* if (item.isSystem()) {
            holder.mTextViewLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            holder.mTextViewLabel.setTypeface(null, Typeface.NORMAL);
        }*/

        // Set the current app package name
        holder.mTextViewPackage.setText(packageName);

        // Set the current app icon
        holder.mImageViewIcon.setImageDrawable(icon);

        holder.mAppSelect.setChecked(item.isSelected());
        holder.mAppSelect.setOnClickListener((buttonView) -> onItemCheckClick(position));

        holder.mItem.setOnClickListener(v -> onItemCheckClick(position));

    }

    @Override
    public int getItemCount() {
        return getActualData().size();
    }

    @Override
    public Filter getFilter() {
        if (searchFilter == null) searchFilter = createSearchFilter();
        return searchFilter;
    }

    public void selectAll() {
        Log.d(TAG, "selectAll: ");
        for (AppInfo appInfo : allItems) {
            appInfo.setSelected(true);
        }
        mListener.onItemsStateChanged(allItems);
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        Log.d(TAG, "unSelectAll: ");
        for (AppInfo appInfo : allItems) {
            appInfo.setSelected(false);
        }
        mListener.onItemsStateChanged(allItems);
        notifyDataSetChanged();
    }

    public void selectByPackage(Collection<String> packageNames) {
        Log.i(TAG, "selectByPackage: " + Arrays.toString(packageNames.toArray()));
        boolean changed = false;
        for (AppInfo appInfo : allItems) {
            if (packageNames.contains(appInfo.getAppPackage())) {
                appInfo.setSelected(true);
                changed = true;
            }
        }
        if (changed) notifyDataSetChanged();
    }

    public void setData(@NonNull List<AppInfo> data) {
        allItems.clear();
        allItems.addAll(data);
        notifyDataSetChanged();
    }

    public void setShowOnlySelected(boolean showOnlySelected) {
        if (this.showOnlySelected != showOnlySelected) {
            this.showOnlySelected = showOnlySelected;
            showOnlySelected();
        }
    }

    private Filter createSearchFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<AppInfo> filtered = new ArrayList<>();
                String query = constraint.toString().toLowerCase();

                if (query.isEmpty()) {
                    filtered = visibleItemList;
                } else {
                    for (AppInfo item : visibleItemList) {
                        if (item.getAppName().toLowerCase().contains(query) || item.getAppPackage().contains(query)) {
                            filtered.add(item);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                setActualData((List<AppInfo>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private List<AppInfo> getActualData() {
        return filteredItemList;
    }

    private void onItemCheckClick(int itemIndex) {
        AppInfo item = getActualData().get(itemIndex);
        item.setSelected(!item.isSelected());
        mListener.onItemStateChanged(item, itemIndex);
        notifyDataSetChanged();
    }

    private void setActualData(@NonNull List<AppInfo> items) {
        filteredItemList = items;
    }

    private void showOnlySelected() {
        if (showOnlySelected) {
            List<AppInfo> visibleItems = new ArrayList<>();
            for (AppInfo appInfo : allItems) {
                if (appInfo.isSelected()) visibleItems.add(appInfo);
            }
            visibleItemList = visibleItems;
        } else {
            visibleItemList = allItems;
        }
        setActualData(visibleItemList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewLabel;
        public TextView mTextViewPackage;
        public ImageView mImageViewIcon;
        public CheckBox mAppSelect;
        public RelativeLayout mItem;

        public ViewHolder(View v) {
            super(v);
            // Get the widgets reference from custom layout
            mTextViewLabel = v.findViewById(R.id.appinfo_name);
            mTextViewPackage = v.findViewById(R.id.appinfo_package_name);
            mImageViewIcon = v.findViewById(R.id.appinfo_image);
            mAppSelect = v.findViewById(R.id.appinfo_select);
            mItem = v.findViewById(R.id.appinfo_item);
        }
    }
}
