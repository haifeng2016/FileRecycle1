package com.samsung.android.filerecycle.doc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.samsung.android.filerecycle.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by haifeng on 2017/6/14.
 */

public class CommonListAdapter extends RecyclerView.Adapter<CommonListAdapter.CommonViewHolder> {

    private Context mContext;
    private List<ListFileBean> mList;
    private IItemAction mItemAction;
    private ShowDocFragment docFragment;
    private int mCheckboxShow = View.GONE;
    private HashMap<Integer, Boolean> mSelectMap = new HashMap<>();

    public interface IItemAction {
        public void itemClick(int pos, String path);
    }

    public CommonListAdapter(Context context, List<ListFileBean> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("ShowDocFragment", "onCreateViewHolder " + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_show_doc_listitem, parent, false);
        CommonViewHolder vh = new CommonViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, final int position) {
        Log.d("ShowDocFragment", "onBindViewHolder " + position);
        holder.position = position;
        holder.nameTextView.setText(mList.get(position).getName());
        holder.timeTextView.setText(mList.get(position).getTime());
        holder.sizeTextView.setText(mList.get(position).getSize());
        holder.mainView.setTag(mList.get(position).getPath());
        holder.selectedCheckBox.setVisibility(mCheckboxShow);
        holder.selectedCheckBox.setChecked(mList.get(position).getSelected());
    }

    @Override
    public int getItemCount() {
        return mList == null ? -1 : mList.size();
    }

    public void updateListCheckState(boolean show) {
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setSelected(show);
            notifyItemChanged(i);
        }
    }

    public boolean getCheckVisible() {
        return mCheckboxShow == View.VISIBLE;
    }

    public void updateCheckboxState(int pos, boolean show) {
        if (mList != null) {
            mList.get(pos).setSelected(show);
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
            mItemAction.itemClick(pos, mList.get(pos).getPath());
        }
        if (mCheckboxShow == View.VISIBLE) {
            updateCheckboxState(pos, true);
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
            }
        };
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("ShowDocFragment", "onLongClick " + position);
                showCheckboxColum(View.VISIBLE);
                updateCheckboxState(position, true);
                return false;
            }
        };
        CheckBox.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("ShowDocFragment", "onCheckedChanged " + position + " " + isChecked);
                updateCheckboxState(position, isChecked);
            }
        };

        public CommonViewHolder(View view) {
            super(view);
            mainView = view;
            mainView.setOnClickListener(clickListener);
            mainView.setOnLongClickListener(longClickListener);
            nameTextView = (TextView) view.findViewById(R.id.filename);
            timeTextView = (TextView) view.findViewById(R.id.delete_time);
            sizeTextView = (TextView) view.findViewById(R.id.docsize);
            selectedCheckBox = (CheckBox) view.findViewById(R.id.docselecectcheckbox);
            selectedCheckBox.setOnCheckedChangeListener(checkedChangeListener);
        }
    }

}
