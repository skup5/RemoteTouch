package cz.zelenikr.remotetouch.app;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
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

    private final InstalledAppsFragment.OnListItemStateChangedListener mListener;
    private final List<AppInfo> allItems;
    private List<AppInfo> filteredItemList;
    private Filter searchFilter;

    public AppInfoRecyclerViewAdapter(List<AppInfo> items, OnListItemStateChangedListener listener) {
        filteredItemList = items;
        allItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_appinfo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final AppInfo item = filteredItemList.get(position);
        // Get the current package name
        final String packageName = item.getAppPackage();

        // Get the current app icon
        Drawable icon = item.getAppIcon();

        // Get the current app label
        String label = item.getAppName();

        // Set the current app label
        holder.mTextViewLabel.setText(label);
        if (item.isSystem()) {
            holder.mTextViewLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            holder.mTextViewLabel.setTypeface(null, Typeface.NORMAL);
        }
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
        return filteredItemList.size();
    }

    @Override
    public Filter getFilter() {
        if (searchFilter == null) searchFilter = createSearchFilter();
        return searchFilter;
    }

    private Filter createSearchFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<AppInfo> filtered = new ArrayList<>();
                String query = constraint.toString().toLowerCase();

                if (query.isEmpty()) {
                    filtered = allItems;
                } else {
                    for (AppInfo item : allItems) {
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
                filteredItemList = (List<AppInfo>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private void onItemCheckClick(int itemIndex) {
        AppInfo item = filteredItemList.get(itemIndex);
        item.setSelected(!item.isSelected());
        mListener.onStateChanged(item, itemIndex);
        AppInfoRecyclerViewAdapter.this.notifyDataSetChanged();
    }

    public void selectByPackage(Collection<String> packageNames) {
        boolean changed = false;
        for (AppInfo appInfo : allItems) {
            if (packageNames.contains(appInfo.getAppPackage())) {
                appInfo.setSelected(true);
                changed = true;
            }
        }
        if (changed) notifyDataSetChanged();
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
