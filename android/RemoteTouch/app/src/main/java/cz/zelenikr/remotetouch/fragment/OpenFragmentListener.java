package cz.zelenikr.remotetouch.fragment;

import android.support.v4.app.Fragment;

/**
 * This interface must be implemented by an activity, that contains fragments
 * which are using this listener. It allows fragment to make visible another
 * {@link Fragment} instance.
 *
 * @author Roman Zelenik
 */
public interface OpenFragmentListener {

    /**
     * @param fragment fragment to make visible
     */
    void openFragment(Fragment fragment);
}
