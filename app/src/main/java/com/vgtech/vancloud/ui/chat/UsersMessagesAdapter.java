package com.vgtech.vancloud.ui.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Subject;
import com.vgtech.vancloud.ui.adapter.DataAdapter;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.MediaController;
import com.vgtech.vancloud.ui.chat.models.ChatMessage;
import com.vgtech.vancloud.ui.chat.models.ChatMessage.ShowType;
import com.vgtech.vancloud.ui.chat.net.NetSilentAsyncTask;
import com.vgtech.vancloud.ui.common.image.ImageCheckActivity;
import com.vgtech.vancloud.utils.Utils;

import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import roboguice.util.Strings;


/**
 * @author xuanqiang
 */
@SuppressLint("InflateParams")
public class UsersMessagesAdapter extends DataAdapter<ChatMessage> implements View.OnClickListener, View.OnLongClickListener {

    private AbstractUsersMessagesFragment fragment;
    private EditText inputView;

    public void setInputView(EditText inputView) {
        this.inputView = inputView;
    }

    private int mMaxInner;
    private int mMaxOutter;
    private int mMinLength;

    public UsersMessagesAdapter(AbstractUsersMessagesFragment fragment) {
        this.fragment = fragment;
        int maxWidth = fragment.getResources().getDisplayMetrics().widthPixels - Utils.convertDipOrPx(fragment.getActivity(), 160);
        mMinLength = Utils.convertDipOrPx(fragment.getActivity(), 25);
        mMaxInner = (int) (maxWidth * 0.7f / 10);
        mMaxOutter = (int) (maxWidth * 0.3f / 50);
    }

    public String getString(int resId) {
        return fragment.getString(resId);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ChatMessage message = dataSource.get(position);
        ChatMessage.ShowType showType = message.showType;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (showType == ShowType.text_tip) {
                convertView = fragment.inflater.inflate(R.layout.msg_text_tip, null);
                assert convertView != null;
                viewHolder.contentLabel = (TextView) convertView.findViewById(R.id.messages_item_content);
            } else if (showType == ShowType.file_au || showType == ShowType.file_au_my) {
                convertView = getConvertViewFromFileAudio(viewHolder, message.isSender);
            } else if (showType == ShowType.file_pic || showType == ShowType.file_pic_my) {
                convertView = getConvertViewFromFilePhoto(viewHolder, message.isSender);
            } else if (showType == ShowType.gps || showType == ShowType.gps_my) {
                convertView = getConvertViewFromGps(viewHolder, message.isSender);
            } else {
                convertView = getConvertView(viewHolder, message.isSender);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (!message.isSender && showType != ShowType.text_tip) {
            viewHolder.nickTv.setText(message.user.nick);
        }
        if (viewHolder.timeLabel != null)
            viewHolder.timeLabel.setVisibility(View.VISIBLE);
        if (showType == ShowType.text_tip) {
            viewHolder.contentLabel.setText(message.content);
        } else if (showType == ShowType.file_au || showType == ShowType.file_au_my) {
            setDataFromFileAudio(viewHolder, message);
        } else if (showType == ShowType.file_pic || showType == ShowType.file_pic_my) {
            setDataFromFilePhoto(viewHolder, message);
        } else if (showType == ShowType.gps || showType == ShowType.gps_my) {
            setDataFromGps(viewHolder, message);
        } else {
            setData(viewHolder, message);
        }
        if (position > 0) {
            ChatMessage lastMessage = dataSource.get(position - 1);
            long lastTime = lastMessage.time;
            long currentTime = message.time;
            if (currentTime - lastTime < 2 * 60 * 1000) {
                if (viewHolder.timeLabel != null)
                    viewHolder.timeLabel.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private View getConvertView(final ViewHolder viewHolder, final boolean isSender) {
        View convertView;
        if (isSender) {
            convertView = fragment.inflater.inflate(R.layout.msg_normal_my, null);
            assert convertView != null;
            viewHolder.failView = convertView.findViewById(R.id.messages_item_fail);
        } else {
            convertView = fragment.inflater.inflate(R.layout.msg_normal, null);
            viewHolder.nickTv = (TextView) convertView.findViewById(R.id.tv_name);
        }
        assert convertView != null;
        viewHolder.avatarView = (SimpleDraweeView) convertView.findViewById(R.id.avatar);
        viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.messages_item_time);
        viewHolder.contentLabel = (TextView) convertView.findViewById(R.id.messages_item_content);
        return convertView;
    }

    private View getConvertViewFromFileAudio(final ViewHolder viewHolder, final boolean isSender) {
        View convertView;
        if (isSender) {
            convertView = fragment.inflater.inflate(R.layout.msg_audio_my, null);
            assert convertView != null;
            viewHolder.failView = convertView.findViewById(R.id.messages_item_fail);
        } else {
            convertView = fragment.inflater.inflate(R.layout.msg_audio, null);
            assert convertView != null;
            viewHolder.nickTv = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.unreadView = (ImageView) convertView.findViewById(R.id.messages_item_unread);
        }
        viewHolder.avatarView = (SimpleDraweeView) convertView.findViewById(R.id.avatar);
        viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.messages_item_time);
        viewHolder.contentLabel = (TextView) convertView.findViewById(R.id.messages_item_content);
        viewHolder.audioView = (TextView) convertView.findViewById(R.id.msg_audio);
        return convertView;
    }

    private View getConvertViewFromFilePhoto(final ViewHolder viewHolder, final boolean isSender) {
        View convertView;
        if (isSender) {
            convertView = fragment.inflater.inflate(R.layout.msg_pic_my, null);
            assert convertView != null;
            viewHolder.failView = convertView.findViewById(R.id.messages_item_fail);
        } else {
            convertView = fragment.inflater.inflate(R.layout.msg_pic, null);
            viewHolder.nickTv = (TextView) convertView.findViewById(R.id.tv_name);
        }
        assert convertView != null;
        viewHolder.avatarView = (SimpleDraweeView) convertView.findViewById(R.id.avatar);
        viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.messages_item_time);
        viewHolder.photoView = (SimpleDraweeView) convertView.findViewById(R.id.messagesPic);
        return convertView;
    }

    private View getConvertViewFromGps(final ViewHolder viewHolder, final boolean isSender) {
        View convertView;
        if (isSender) {
            convertView = fragment.inflater.inflate(R.layout.msg_gps_my, null);
            assert convertView != null;
            viewHolder.failView = convertView.findViewById(R.id.messages_item_fail);
        } else {
            convertView = fragment.inflater.inflate(R.layout.msg_gps, null);
            viewHolder.nickTv = (TextView) convertView.findViewById(R.id.tv_name);
        }
        assert convertView != null;
        viewHolder.avatarView = (SimpleDraweeView) convertView.findViewById(R.id.avatar);
        viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.messages_item_time);
        viewHolder.contentLabel = (TextView) convertView.findViewById(R.id.messages_item_content);
        return convertView;
    }

    private void setAvatar(final ViewHolder viewHolder, final ChatMessage message) {
        viewHolder.avatarView.setTag(message);
        viewHolder.avatarView.setOnClickListener(this);
        viewHolder.avatarView.setOnLongClickListener(this);
        String serviceId = PrfUtils.getPrfparams(fragment.getActivity(), Constants.SERVICE_USERID);
        if (!TextUtils.isEmpty(serviceId) && serviceId.equals(message.user.uid)) {
            viewHolder.avatarView.setImageResource(R.drawable.icon_customer);
        } else
            AvatarController.setAvatarView(message.user.avatar, viewHolder.avatarView);
    }

    private void setFailView(final ViewHolder viewHolder, final ChatMessage message) {
        if (message.isSender) {
            viewHolder.failView.setVisibility(message.isFailure ? View.VISIBLE : View.GONE);
            viewHolder.failView.setTag(message);
            viewHolder.failView.setOnClickListener(fragment.failListener);
        }
    }

    private void setData(final ViewHolder viewHolder, final ChatMessage message) {
        setFailView(viewHolder, message);
        setAvatar(viewHolder, message);
        viewHolder.timeLabel.setText(message.getDisplayTime());
        viewHolder.contentLabel.setText(EmojiFragment.getEmojiContentWithAt(fragment.getActivity(), viewHolder.contentLabel.getTextSize(), message.content));
    }

    @SuppressWarnings("ConstantConditions")
    private void setDataFromFileAudio(final ViewHolder viewHolder, final ChatMessage message) {
        setFailView(viewHolder, message);
        setAvatar(viewHolder, message);
        viewHolder.timeLabel.setText(message.getDisplayTime());
        viewHolder.audioView.setClickable(true);
        viewHolder.audioView.setLongClickable(true);
        viewHolder.audioView.setOnClickListener(itemListener);
        viewHolder.audioView.setTag(message);
        setAudioViewSelect(viewHolder.audioView, message);
        final Subject.File subjectFile = message.getFile();
        if (!message.isSender) {
            viewHolder.unreadView.setVisibility(subjectFile.isUnread() ? View.VISIBLE : View.INVISIBLE);
        }
        if (Strings.notEmpty(subjectFile.getFilePath())) {
            int duration = Integer.valueOf(subjectFile.getDuration());
            if (duration > 60)
                duration = 60;
            int inner = 0, outter = 0;
            if (duration <= 10) {
                inner = duration;
            } else {
                inner = 10;
                outter = duration - 10;
            }
            viewHolder.audioView.getLayoutParams().width = inner * mMaxInner + outter * mMaxOutter + mMinLength;
//            viewHolder.audioView.getLayoutParams().width = fragment.controller.getPixels(Math.min(10,
//                    Math.max(60, Integer.valueOf(subjectFile.getDuration()) * 6)));
            viewHolder.contentLabel.setText(subjectFile.getDuration() + "″");
        } else {
            viewHolder.audioView.getLayoutParams().width = fragment.controller.getPixels(60);
            viewHolder.contentLabel.setText("");
            if (Strings.isEmpty(subjectFile.url)) return;
            final Activity activity = fragment.getActivity();
            new NetSilentAsyncTask<String>(activity) {
                @Override
                protected void onSuccess(String filePath) throws Exception {
                    if (Strings.isEmpty(filePath)) return;
                    long duration = MediaController.getAmrDuration(new File(filePath));
                    subjectFile.setFilePath(filePath);
                    subjectFile.setDuration(String.valueOf(duration / 1000));
                    message.setFile(subjectFile);
                    message.save();
                    notifyDataSetInvalidated();
                }

                @Override
                protected String doInBackground() throws Exception {
                    return net().download(subjectFile.url, "amr", activity);
                }
            }.execute();
        }
    }

    private void setDataFromFilePhoto(final ViewHolder viewHolder, final ChatMessage message) {
        setFailView(viewHolder, message);
        setAvatar(viewHolder, message);
        viewHolder.timeLabel.setText(message.getDisplayTime());
        viewHolder.photoView.setClickable(true);
        viewHolder.photoView.setLongClickable(true);
        viewHolder.photoView.setOnClickListener(itemListener);
        viewHolder.photoView.setTag(message);
        viewHolder.photoView.setImageURI(message.displayImageUrl());
    }


    private void setDataFromGps(final ViewHolder viewHolder, final ChatMessage message) {
        setFailView(viewHolder, message);
        setAvatar(viewHolder, message);
        viewHolder.timeLabel.setText(message.getDisplayTime());
        viewHolder.contentLabel.setText(message.content);
        View parentView = (View) viewHolder.contentLabel.getParent();
        assert parentView != null;
        parentView.setLongClickable(true);
        parentView.setOnClickListener(itemListener);
        parentView.setTag(message);
    }

    @SuppressWarnings("ConstantConditions")
    private void setAudioViewSelect(final TextView audioView, final ChatMessage message) {
        audioView.setSelected(message.isPlaying);
        if (audioView.isSelected()) {
            if (message.isSender) {
                fragment.controller.textViewRightDrawable(audioView, R.drawable.audio_play_my);
            } else {
                fragment.controller.textViewLeftDrawable(audioView, R.drawable.audio_play);
            }
            AnimationDrawable audioAnim;
            if (message.isSender) {
                audioAnim = (AnimationDrawable) audioView.getCompoundDrawables()[2];
            } else {
                audioAnim = (AnimationDrawable) audioView.getCompoundDrawables()[0];
            }
            audioAnim.start();
        } else {
            if (message.isSender) {
                fragment.controller.textViewRightDrawable(audioView, R.drawable.msg_audio_my_icon);
            } else {
                fragment.controller.textViewLeftDrawable(audioView, R.drawable.msg_audio_icon);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = dataSource.get(position);
        return message.getShowType().ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ShowType.values().length;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.avatar:
                ChatMessage message = (ChatMessage) view.getTag();

                String photo = message.user.avatar;
                String nick = message.user.nick;
                String userId = message.user.uid;
                String serviceId = PrfUtils.getPrfparams(fragment.getActivity(), Constants.SERVICE_USERID);
                if (TextUtils.isEmpty(serviceId) || !serviceId.equals(userId)) {
                    UserUtils.enterUserInfo(fragment.getActivity(), userId, nick, photo, Message.Type.chat == message.group.getChatType());
                }

                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.avatar:
                ChatMessage message = (ChatMessage) v.getTag();
                String nick = message.user.nick;
                if (inputView != null) {
                    String str = inputView.getText().toString();
                    StringBuffer stringBuffer = new StringBuffer(str);
                    int start = inputView.getSelectionStart();
                    String atname = "@" + nick + " ";
                    stringBuffer.insert(start, atname);
                    inputView.setText(stringBuffer);
                    inputView.setSelection(start + atname.length());
                }
                return true;
        }
        return false;
    }

    class ViewHolder {
        SimpleDraweeView avatarView;
        TextView timeLabel;
        TextView nickTv;
        TextView contentLabel;
        View failView;
        TextView audioView;
        SimpleDraweeView photoView;
        ImageView unreadView;
    }

    public void scrollToLast() {

//        fragment.listView.getRefreshableView().setSelection(fragment.listView.getBottom());
        scrollToPosition(getCount());

    }

    public void scrollToPosition(final int position) {
        fragment.listView.getRefreshableView().post(new Runnable() {
            @Override
            public void run() {
                fragment.listView.getRefreshableView().setSelection(position);
            }
        });
    }

    View.OnClickListener itemListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ChatMessage message = (ChatMessage) view.getTag();
            ShowType showType = message.showType;
            if (showType == ShowType.file_au || showType == ShowType.file_au_my) {
                playMessageAudio(message);
            } else if (showType == ShowType.file_pic || showType == ShowType.file_pic_my) {
//                fragment.controller.ftAdd(fragment.controller.ftFadeAnimations(),
//                        PhotoShowFragment.newInstance(message.displayImageUrl()), null).addToBackStack(null).commit();
                List<ImageInfo> imgInfo = new ArrayList<>();
                int i = 0;
                int position = 0;
                for (ChatMessage msg : dataSource) {
                    ShowType type = msg.showType;
                    if (type == ShowType.file_pic || type == ShowType.file_pic_my) {
                        if (message.equals(msg)) {
                            position = i;
                        }
                        i++;
                        imgInfo.add(new ImageInfo(msg.displayImageUrl()));
                    }
                }
                String json = new Gson().toJson(imgInfo);
                Intent intent = new Intent(fragment.getActivity(), ImageCheckActivity.class);
                intent.putExtra("listjson", json);
                intent.putExtra("position", position);
                intent.putExtra("numVisible", false);
                fragment.getActivity().startActivity(intent);
            } else if (showType == ShowType.gps || showType == ShowType.gps_my) {
                Subject.Gps gps = message.getGps();
                fragment.controller.pushFragment(MapFragment.newInstance(message.content, gps.latitude, gps.longitude));
            }
        }
    };

    private void playMessageAudio(final ChatMessage message) {
        final Activity activity = fragment.getActivity();
        if (message.isPlaying) {
            MediaController.stopAudio();
            message.isPlaying = false;
            notifyDataSetInvalidated();
        } else {
            final Subject.File subjectFile = message.getFile();
            boolean isSender = message.isSender;
            if (Strings.notEmpty(subjectFile.getFilePath())) {
                File file = new File(subjectFile.getFilePath());
                if (isSender && !file.exists()) {
                    Toast.makeText(activity, R.string.audiodel_toast, Toast.LENGTH_SHORT).show();
                } else {
                    MediaController.playAudio(file,
                            new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    message.isPlaying = true;
                                    if (!message.isSender && subjectFile.isUnread()) {
                                        subjectFile.setUnread(false);
                                        message.setFile(subjectFile);
                                        message.save();
                                    }
                                    notifyDataSetInvalidated();
                                    MediaController.setAudioMode(activity);
                                }
                            }, new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    message.isPlaying = false;
                                    MediaController.setAudioMode(activity, false);
                                    notifyDataSetInvalidated();
                                }
                            }, new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                                    message.isPlaying = false;
                                    MediaController.setAudioMode(activity, false);
                                    notifyDataSetInvalidated();
                                    return false;
                                }
                            });
                }
            } else {
                Toast.makeText(activity, R.string.downloading, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
