package com.vgtech.vancloud.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.DataAdapter;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.common.PrfUtils;

import roboguice.inject.InjectView;

/**
 * @author xuanqiang
 */
public class UserGroupsFragment extends ActionBarFragment {
    @InjectView(R.id.user_groups_list)
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return attachToSwipeBack(createContentView(R.layout.user_groups));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        titleView.setText(R.string.select_groups);
        adapter = new Adapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (listener != null) {
//          listener.selectedContacts(null, new ChatGroup(adapter.dataSource.get(position), xmpp.getLogname()));
                    listener.selectedContacts(null, adapter.dataSource.get(position));
                }
            }
        });
        loadData();
    }

    private void loadData() {
        adapter.dataSource.addAll(ChatGroup.findAllGroup(PrfUtils.getUserId(getActivity()), PrfUtils.getTenantId(getActivity())));
        adapter.notifyDataSetChanged();
//    new NetEntityAsyncTask<GroupListInfo>(getActivity()){
//      @Override
//      protected void onPreExecute() throws Exception{
//        super.onPreExecute();
//        afterAnim = true;
//      }
//      @Override
//      protected GroupListInfo doInBackground() throws Exception{
//        return net().chatGroups();
//      }
//      @Override
//      @SuppressWarnings("unchecked")
//      protected void success(GroupListInfo groupListInfo) throws Exception{
//        if(groupListInfo.groupInfos == null) return;
//        adapter.dataSource.addAll(groupListInfo.groupInfos);
//        adapter.notifyDataSetChanged();
//      }
//    }.execute();
    }

    private class Adapter extends DataAdapter<ChatGroup> {
        @SuppressWarnings("ConstantConditions")
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ChatGroup group = dataSource.get(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater(null).inflate(R.layout.user_groups_item, null);
                assert convertView != null;
                viewHolder.nameLabel = (TextView) convertView.findViewById(R.id.user_groups_item_name);
                viewHolder.numLabel = (TextView) convertView.findViewById(R.id.user_groups_item_num);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            float density = getResources().getDisplayMetrics().density;
            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            viewHolder.numLabel.setText("(" + group.peopleNum + ")");
            viewHolder.nameLabel.setText(group.getDisplayNick());
            viewHolder.numLabel.measure(spec, spec);
            viewHolder.nameLabel.setMaxWidth(UserGroupsFragment.this.getView().getMeasuredWidth() - viewHolder.numLabel.getMeasuredWidth() - (int) (80 * density));
            return convertView;
        }

        class ViewHolder {
            TextView nameLabel;
            TextView numLabel;
        }
    }
//  private class Adapter extends DataAdapter<GroupInfo>{
//    @Override
//    public View getView(int position, View convertView, ViewGroup viewGroup){
//      GroupInfo groupInfo = dataSource.get(position);
//      ViewHolder viewHolder;
//      if(convertView == null){
//        viewHolder = new ViewHolder();
//        convertView = getLayoutInflater(null).inflate(R.layout.user_groups_item,null);
//        assert convertView != null;
//        viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.user_groups_item_name);
//        viewHolder.numLabel = (TextView)convertView.findViewById(R.id.user_groups_item_num);
//        convertView.setTag(viewHolder);
//      }else{
//        viewHolder = (ViewHolder)convertView.getTag();
//      }
//      float density = getResources().getDisplayMetrics().density;
//      int spec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//      viewHolder.numLabel.setDettailText("(" + groupInfo.staffs.size() + ")");
//      viewHolder.nameLabel.setDettailText(groupInfo.getDisplayNick());
//      viewHolder.numLabel.measure(spec,spec);
//      viewHolder.nameLabel.setMaxWidth(UserGroupsFragment.this.getView().getMeasuredWidth() - viewHolder.numLabel.getMeasuredWidth() - (int)(80 * density));
//      return convertView;
//    }
//    class ViewHolder{
//      TextView nameLabel;
//      TextView numLabel;
//    }
//  }

    public void setListener(ContactsListener listener) {
        this.listener = listener;
    }

    private ContactsListener listener;
    private Adapter adapter;
    @Inject
    XmppController xmpp;


}
