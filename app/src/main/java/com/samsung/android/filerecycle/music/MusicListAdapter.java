package com.samsung.android.filerecycle.music;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.IFileSelected;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;

import java.util.HashMap;
import java.util.List;

/**
 * Created by haifeng on 2017/6/19.
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.CommonViewHolder> {

    private IFileSelected mFileSelected;
    private List<FileBean> mList;
    private IItemAction mItemAction;
    private int mCheckboxShow = View.GONE;
    private HashMap<Integer, Boolean> mSelectMap = new HashMap<>();

    public interface IItemAction {
        public void itemClick(int pos, String path);
    }

    public MusicListAdapter(IFileSelected iFileSelected, List<FileBean> list) {
        mFileSelected = iFileSelected;
        mList = list;
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_show_music_listitem, parent, false);
        CommonViewHolder vh = new CommonViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, final int position) {
        holder.position = position;
        holder.nameTextView.setText(mList.get(position).getName());
        holder.timeTextView.setText(mList.get(position).getDelTime().toString());
        holder.sizeTextView.setText(String.valueOf(mList.get(position).getSize()/1024));
        holder.mainView.setTag(mList.get(position).getBackupPath());
        holder.selectedCheckBox.setVisibility(mCheckboxShow);
        holder.selectedCheckBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);
    }

    @Override
    public int getItemCount() {
        return mList == null ? -1 : mList.size();
    }

    public void updateListCheckState(boolean show) {
        for (int i = 0; i < mList.size(); i++) {
            mSelectMap.put(i, show);
            notifyItemChanged(i);
        }
    }

    public boolean getCheckVisible() {
        return mCheckboxShow == View.VISIBLE;
    }

    public void updateCheckboxState(int pos, boolean show) {
        mSelectMap.put(pos, show);
        if (mList != null) {
            mFileSelected.onFileSelected(mList.get(pos), show);
        }
    }

    public void showCheckboxColum(int visible) {
        mCheckboxShow = visible;
        notifyDataSetChanged();
    }

    public void setOnItemClick(IItemAction action) {
        this.mItemAction = action;
    }

    public void clickAction(int pos) {
        if (mItemAction != null) {
            mItemAction.itemClick(pos, mList.get(pos).getBackupPath());
        }
    }

    public void updateCheckboxList() {
        updateListCheckState(false);
        showCheckboxColum(View.GONE);
    }

    public void unCheckAll() {
        if (mSelectMap == null) {
            return;
        }
        for (int i = 0; i < mSelectMap.size(); i++) {
            mSelectMap.put(i, false);
        }
    }

    public void update() {
        unCheckAll();
        notifyDataSetChanged();
    }

    class CommonViewHolder extends RecyclerView.ViewHolder {
        private int position = -1;
        private TextView nameTextView;
        private TextView timeTextView;
        private TextView sizeTextView;
        private CheckBox selectedCheckBox;
        private View mainView;

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAction(position);
                if (mCheckboxShow == View.VISIBLE) {
                    //selectedCheckBox.getVisibility() == View.VISIBLE
                    selectedCheckBox.setChecked(!selectedCheckBox.isChecked());
                }
            }
        };
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showCheckboxColum(View.VISIBLE);
                selectedCheckBox.setChecked(true);
                return true;
            }
        };
        CheckBox.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCheckboxState(position, isChecked);
            }
        };

        public CommonViewHolder(View view) {
            super(view);
            mainView = view;
            mainView.setOnClickListener(clickListener);
            mainView.setOnLongClickListener(longClickListener);
            nameTextView = (TextView) view.findViewById(R.id.musicname);
            timeTextView = (TextView) view.findViewById(R.id.delete_time);
            sizeTextView = (TextView) view.findViewById(R.id.musicsize);
            selectedCheckBox = (CheckBox) view.findViewById(R.id.musicselecectcheckbox);
            selectedCheckBox.setOnCheckedChangeListener(checkedChangeListener);
        }
    }

}
