package com.vgtech.vancloud.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.ScheduleItem;
import com.vgtech.common.api.ScheduleMap;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.utils.Utils;

public class GalleryAdapter extends
        RecyclerView.Adapter<GalleryAdapter.ViewHolder> {


    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    private LayoutInflater mInflater;
    //    private List<NewUser> mDatas;
    private Context mContext;
    private View mParentView;
    private BaseAdapter mParentAdapter;

    public GalleryAdapter(Context context, BaseAdapter adapter) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mParentAdapter = adapter;
    }

    public void setParentView(View view) {
        mParentView = view;
    }

    private ScheduleMap mScheduleItem;

    public void setData(ScheduleMap schedule) {
        mScheduleItem = schedule;
//        List<NewUser> datats = new ArrayList<NewUser>();
//        for (int i = 0; i < 10; i++)
//            datats.add(new NewUser(i, "zsf" + i, "http://static.oschina.net/uploads/user/86/173728_100.jpg"));
//        mDatas = datats;
        notifyDataSetChanged();
    }

    public ScheduleItem getSelectItem() {
        return mScheduleItem.items.get(mScheduleItem.selectUserPosition);
    }

    public ScheduleItem getItem(int position) {
        return mScheduleItem.items.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View arg0) {
            super(arg0);
        }

        SimpleDraweeView mImg;
        TextView mTxt;
        View arrow;
    }

    @Override
    public int getItemCount() {
        return mScheduleItem.items.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.user_icon_item,
                viewGroup, false);
        view.setTag(this);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.mImg = (SimpleDraweeView) view
                .findViewById(R.id.iv_icon);
        viewHolder.mTxt = (TextView) view.findViewById(R.id.tv_name);
        viewHolder.arrow = view.findViewById(R.id.arrow);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        ScheduleItem scheduleItem = (ScheduleItem) mScheduleItem.items.get(i);
        SimpleDraweeView imageView = viewHolder.mImg;
        int wh = Utils.convertDipOrPx(mContext, 40);
        if (i == mScheduleItem.selectUserPosition) {
            wh = Utils.convertDipOrPx(mContext, 50);
        }
        viewHolder.arrow.setVisibility(i == mScheduleItem.selectUserPosition ? View.VISIBLE : View.INVISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(wh, wh);
        imageView.setLayoutParams(params);
        NewUser newUser = scheduleItem.getData(NewUser.class);
     //   imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        if (!newUser.photo.equals(viewHolder.mImg.getTag())) {
//            imageView.setTag(newUser.photo);
//            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
//            roundingParams.setBorder(R.color.white, 1.0f);
//            roundingParams.setRoundAsCircle(true);
//            GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
//            hierarchy.setRoundingParams(roundingParams);
//            hierarchy.setPlaceholderImage(com.vgtech.common.R.mipmap.user_photo_default_small);
//            hierarchy.setFailureImage(com.vgtech.common.R.mipmap.user_photo_default_small);
            imageView.setImageURI(newUser.photo);
//        }
        viewHolder.mTxt.setText(EmojiFragment.getEmojiContent(mContext, viewHolder.mTxt.getTextSize(),Html.fromHtml(newUser.name)));
        viewHolder.itemView.setTag(i);
        viewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mScheduleItem.selectUserPosition = (Integer) v.getTag();
                mParentAdapter.notifyDataSetChanged();
            }
        });
    }

}
