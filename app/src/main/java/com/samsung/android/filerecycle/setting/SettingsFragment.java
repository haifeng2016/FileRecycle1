package com.samsung.android.filerecycle.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;
import com.samsung.android.filerecycle.common.Util;

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
    Switch savetime;

    private Switch.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.pic_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_SWITCH_PIC, buttonView.isChecked());
                    break;
                case R.id.video_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_SWITCH_VIDEO, buttonView.isChecked());
                    break;
                case R.id.audio_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_SWITCH_MUSIC, buttonView.isChecked());
                    break;
                case R.id.doc_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_SWITCH_DOC, buttonView.isChecked());
                    break;
                case R.id.app_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_SWITCH_APP, buttonView.isChecked());
                    break;
                case R.id.others_file_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_SWITCH_OTHER, buttonView.isChecked());
                    break;
                case R.id.settings_switch:
                    SettingFeatures.setSwitchState(SettingFeatures.KEY_KEEP_DURATION, buttonView.isChecked());
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
        savetime = (Switch) view.findViewById(R.id.settings_switch);

        picswitch.setChecked(SettingFeatures.getSwitchState(SettingFeatures.KEY_SWITCH_PIC));
        videoswitch.setChecked(SettingFeatures.getSwitchState(SettingFeatures.KEY_SWITCH_VIDEO));
        audioswitch.setChecked(SettingFeatures.getSwitchState(SettingFeatures.KEY_SWITCH_MUSIC));
        docswitch.setChecked(SettingFeatures.getSwitchState(SettingFeatures.KEY_SWITCH_DOC));
        appswitch.setChecked(SettingFeatures.getSwitchState(SettingFeatures.KEY_SWITCH_APP));
        othersfilesw.setChecked(SettingFeatures.getSwitchState(SettingFeatures.KEY_SWITCH_OTHER));
        savetime.setChecked(SettingFeatures.getSaveTimeSwitchState(SettingFeatures.KEY_KEEP_DURATION));

        picswitch.setOnCheckedChangeListener(checkedChangeListener);
        videoswitch.setOnCheckedChangeListener(checkedChangeListener);
        audioswitch.setOnCheckedChangeListener(checkedChangeListener);
        docswitch.setOnCheckedChangeListener(checkedChangeListener);
        appswitch.setOnCheckedChangeListener(checkedChangeListener);
        othersfilesw.setOnCheckedChangeListener(checkedChangeListener);
        savetime.setOnCheckedChangeListener(checkedChangeListener);

        Util.setSPBooleanValue(Util.SP_FIRST_USE, false);

        return view;
    }

}
