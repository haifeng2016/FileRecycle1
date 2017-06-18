package com.samsung.android.filerecycle.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;

/**
 * Created by haifeng on 2017/5/4.
 */

public class SettingsFragment extends BaseFragment {

    private ViewGroup view;

    Switch picswitch ;
    Switch videoswitch;
    Switch audioswitch;
    Switch docswitch ;
    Switch appswitch ;
    Switch othersfilesw;

    private Switch.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.pic_switch:
                    //to do something

                    break;
                case R.id.video_switch:
                    //to do something

                    break;
                case R.id.audio_switch:
                    //to do something

                    break;
                case R.id.doc_switch:
                    //to do something

                    break;
                case R.id.app_switch:
                    //to do something

                    break;
                case R.id.others_file_switch:
                    //to do something

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_settings,container,false);
        title = mContext.getString(R.string.setting);
        updateActionBar();

        picswitch =   (Switch) view.findViewById(R.id.pic_switch);
        videoswitch = (Switch) view.findViewById(R.id.video_switch);
        audioswitch = (Switch) view.findViewById(R.id.audio_switch);
        docswitch =   (Switch) view.findViewById(R.id.doc_switch);
        appswitch =   (Switch) view.findViewById(R.id.app_switch);
        othersfilesw = (Switch) view.findViewById(R.id.others_file_switch);

        picswitch.setOnCheckedChangeListener(checkedChangeListener);
        videoswitch.setOnCheckedChangeListener(checkedChangeListener);
        audioswitch.setOnCheckedChangeListener(checkedChangeListener);
        docswitch.setOnCheckedChangeListener(checkedChangeListener);
        appswitch.setOnCheckedChangeListener(checkedChangeListener);
        othersfilesw.setOnCheckedChangeListener(checkedChangeListener);

        return view;
    }

}
