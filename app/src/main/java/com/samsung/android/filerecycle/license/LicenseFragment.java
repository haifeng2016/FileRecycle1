package com.samsung.android.filerecycle.license;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by samsung on 2017/5/5.
 */

public class LicenseFragment extends BaseFragment {
    private static final String TAG = LicenseFragment.class.getSimpleName();

    protected View view;

    public LicenseFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_license,container,false);
        title = mContext.getString(R.string.open_source_license);

        TextView licenseTextView = (TextView) view.findViewById(R.id.licenseTextView);

        AssetManager assetManager = mContext.getAssets();
        StringBuilder licenseStringBuilder = new StringBuilder();

        try (InputStream inputStream = assetManager.open("LICENSE-2.0.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String lineBuffer;
            String lineSeparator = System.getProperty("line.separator", "\n");
            while ((lineBuffer = bufferedReader.readLine()) != null) {
                licenseStringBuilder.append(lineBuffer);
                licenseStringBuilder.append(lineSeparator);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        licenseTextView.setText(licenseStringBuilder.toString());

        updateActionBar();

        return view;
    }
}
