package cz.zelenikr.remotetouch.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.helper.AndroidAppHelper;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Roman Zelenik
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View aboutPage = new AboutPage(getContext())
            .isRTL(false)
            .setDescription(getString(R.string.About_Description))
            .setImage(R.mipmap.ic_launcher)
            .addItem(createVersionElement())
            .addItem(createCopyRightsElement())
            .addGroup(getString(R.string.About_Contacts_Label))
            .addEmail(getString(R.string.Contact_Email))
            .addWebsite(getString(R.string.Contact_Website))
            .create();

        return aboutPage;
    }

    private Element createVersionElement() {
        Element versionElement = new Element();

        Pair<String, Integer> version = AndroidAppHelper.getAppVersionByPackageName(getContext(), getContext().getPackageName());
        versionElement.setTitle(getString(R.string.Version) + " " + version.first);

        String url = getString(R.string.Contact_Website);
        versionElement.setValue(url);

        Uri uri = Uri.parse(url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        versionElement.setIntent(browserIntent);

        versionElement.setGravity(Gravity.CENTER);
        return versionElement;
    }

    private Element createCopyRightsElement() {
        Element copyRightsElement = new Element();

        final String copyrights = String.format(getString(R.string.About_Copyright), getString(R.string.Contact_Author), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);

        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
        copyRightsElement.setGravity(Gravity.CENTER);

        return copyRightsElement;
    }
}
