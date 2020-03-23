package com.vgtech.vancloud.ui.chat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.Constants;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.image.Bimp;
import com.vgtech.common.image.ImageUtility;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.ui.publish.PicSelectActivity;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Subject;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.common.record.DialogManager;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.EmotionKeyboard;
import com.vgtech.vancloud.utils.VgTextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import roboguice.util.Strings;

import static android.app.Activity.RESULT_OK;
import static com.vgtech.vancloud.models.Subject.File.TYPE_AUDIO;
import static com.vgtech.vancloud.models.Subject.File.TYPE_PICTURE;

/**
 * @author xuanqiang
 * @date 13-7-18
 */
public class MessageInputFragment extends RoboFragment implements MapFragment.MapListener, View.OnClickListener {
    @InjectView(R.id.message_input_editText)
    private EditText editText;
    @InjectView(R.id.message_input_sendButton)
    Button sendButton;
    @InjectView(R.id.message_input_emojiButton)
    ImageButton emojiButton;
    @InjectView(R.id.mikeButton)
    ImageButton mikeButton;
    @InjectView(R.id.message_more)
    View moreView;
    @InjectView(R.id.more_input_container)
    View moreContainer;
    @InjectView(R.id.msg_input_rec_text)
    TextView recText;
    @InjectView(R.id.message_input_menuButton)
    View menuButton;
    @InjectView(R.id.msg_input_rec_button)
    View recButton;
    @InjectView(R.id.gridview)
    GridView moreGridview;
    EmojiFragment emojiFragment;
    private MessageInputListener listener;
    MediaRecorder mediaRecorder;
    File audioFile;
    int minute;
    float second;
    Timer timer;
    float downY, curY;
    private ChatGroup mChatGroup;
    private boolean mGroup;


    //需要绑定的内容view
    private View contentView;
    private EmotionKeyboard mEmotionKeyboard;

    public void setChatGroup(ChatGroup chatGroup) {
        mChatGroup = chatGroup;
        if (mChatGroup != null && ChatGroup.GroupTypeGroup.equals(mChatGroup.type)) {
            mGroup = true;
        }
    }

    private UsersMessagesAdapter messagesAdapter;

    public void setAdapter(UsersMessagesAdapter adapter) {
        messagesAdapter = adapter;
    }
    /**
     * 相机权限
     */
    protected static final int CAMERA_REQUESTCODE = 20001;
    protected static final String[] CAMERAPERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.READ_EXTERNAL_STORAGE
            ,Manifest.permission.CAMERA
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_input, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mikeButton.setOnClickListener(this);
//        menuButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
//        emojiButton.setOnClickListener(this);

        messagesAdapter.setInputView(editText);
//        editText.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                moreView.setVisibility(View.GONE);
//                emojiButton.setSelected(false);
////                editText.requestFocus();
//                editText.requestFocusFromTouch();
//                return false;
//            }
//        });
//        editText.setOnClickListener(this);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (editText.getSelectionStart() == editText.getSelectionEnd()) {
                        String str = editText.getText().toString();
                        String endStr = str.substring(0, editText.getSelectionEnd());
                        int end = endStr.lastIndexOf("@");
                        if (end != -1) {
                            String lastStr = endStr.substring(end, editText.getSelectionEnd());
                            String findStr = VgTextUtils.find(lastStr);
                            if (!TextUtils.isEmpty(findStr) && findStr.equals(lastStr)) {
                                StringBuffer stringBuffer = new StringBuffer(str);
                                stringBuffer.delete(end, editText.getSelectionEnd());
                                editText.setText(stringBuffer);
                                editText.setSelection(end);
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });
        EditUtils.limitEditTextLength(editText, Constants.TEXT_MAX_VALUE, new TextWatcher() {
            private boolean hasChanged = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!hasChanged) {
                    return;
                }

                String str = editable.toString();
                if (mGroup && !TextUtils.isEmpty(str) && str.toString().endsWith("@")) {
                    hideMoreView();
                    String nick = mChatGroup.nick;
                    String photo = mChatGroup.avatar;
                    Intent intent = new Intent(getActivity(), AtListActivity.class);
                    intent.putExtra("name", nick);
                    intent.putExtra("photo", photo);
                    startActivityForResult(intent, 1001);
                    return;
                }
                if (Strings.isEmpty(str)) {
                    sendButton.setVisibility(View.GONE);
                    menuButton.setVisibility(View.VISIBLE);
                } else {
                    sendButton.setVisibility(View.VISIBLE);
                    menuButton.setVisibility(View.GONE);
                    if (!emojiButton.isSelected()) {
                        return;
                    }
                    hasChanged = false;
                    int start = editText.getSelectionStart();
                    int end = editText.getSelectionEnd();
                    editText.setText(EmojiFragment.getEmojiContent(getActivity(), editText.getTextSize(),str));
                    editText.setSelection(start, end);
                    hasChanged = true;
                }
            }
        });

        //<editor-fold desc="record touch">
        recButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = event.getY();
                        startRecord();
                        recText.setText(getString(R.string.loosen_end));
                        recButton.setSelected(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                        boolean shortTime = false;
                        if (minute == 0 && second < 2) {
                            shortTime = true;
                        }
                        stopRecord();
                        recText.setText(getString(R.string.hold_down_talk));
                        recButton.setSelected(false);
                        if (downY - curY > controller.getPixels(60)) {
                            return true;
                        }
                        if (shortTime) {
                            tipView(3);
                            return true;
                        }
                        sendRecord();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        stopRecord();
                        recText.setText(getString(R.string.hold_down_talk));
                        recButton.setSelected(false);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        curY = event.getY();
                        if (downY - curY > controller.getPixels(60)) {
                            tipView(1);
                        } else {
                            tipView(0);
                        }
                        return true;
                }
                return false;
            }
        });
        //</editor-fold>

        emojiFragment = new EmojiFragment();
        emojiFragment.setListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Map<String, Object> map = emojiFragment.emojis.get(emojiFragment.page).get(pos);
                int start = Math.max(editText.getSelectionStart(), 0);
                int end = Math.max(editText.getSelectionEnd(), 0);
                String str = (String) map.get("text");
                if (str.contains("删除")) {

                    //动作按下
                    int action = KeyEvent.ACTION_DOWN;
                    //code:删除，其他code也可以，例如 code = 0
                    int code = KeyEvent.KEYCODE_DEL;
                    KeyEvent event = new KeyEvent(action, code);
                    editText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
                    return;
                }
                //noinspection ConstantConditions
                editText.getText().replace(Math.min(start, end), Math.max(start, end), str, 0, str.length());
            }
        });

        int[] moreImageResIds = {
                R.drawable.msg_input_photo, R.drawable.msg_input_capture, R.drawable.msg_input_gps,
                R.drawable.msg_input_emoji
        };
        String[] moreNames = {getString(R.string.photo), getString(R.string.take),
                getString(R.string.location)};
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image", moreImageResIds[i]);
            map.put("text", moreNames[i]);
            data.add(map);
        }
        moreGridview.setAdapter(new SimpleAdapter(getActivity(), data, R.layout.input_more_item,
                new String[]{"image", "text"},
                new int[]{R.id.imageView, R.id.textView}));
        moreGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos == 0) {
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                    } else {
//                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//                    }
//                    startActivityForResult(intent, 1);
                    Intent intent = new Intent(getActivity(),
                            PicSelectActivity.class);
                    startActivityForResult(intent, 1);
                    hideMoreView();
                } else if (pos == 1) {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    File file = new File(FileCacheUtils.getXmppImageDir(getActivity()), System.currentTimeMillis() + ".jpg");
//                    mPath = file.getAbsolutePath();
//                    Uri imageUri = Uri.fromFile(file);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                    startActivityForResult(intent, 2);
                    PermissionsChecker mChecker = new PermissionsChecker(getActivity());
                    if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                        // 请求权限
                        PermissionsActivity.startActivityForResult(getActivity(), CAMERA_REQUESTCODE, CAMERAPERMISSION);
                    } else {
                        // 全部权限都已获取
                        EasyPhotos.createCamera(MessageInputFragment.this)
                                .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                .start(2);
                    }
                    hideMoreView();
                } else if (pos == 2) {
                    MapFragment mapFragment = new MapFragment();
                    mapFragment.setListener(MessageInputFragment.this);
                    controller.pushFragment(mapFragment);
                    hideMoreView();
                } else if (pos == 3) {
                    moreGridview.setVisibility(View.GONE);
                    moreContainer.setVisibility(View.VISIBLE);
                    emojiButton.setSelected(true);
                    getFragmentManager().beginTransaction().replace(R.id.more_input_container, emojiFragment).commit();
                }
            }
        });

        mEmotionKeyboard = EmotionKeyboard.with(getActivity())
                .setEmotionView(moreView)//绑定表情面板
                .bindToContent(contentView)//绑定内容view
                .bindToEditText(editText)//判断绑定那种EditView
                .bindToEmotionButton(emojiButton)//绑定表情按钮
                .setShowAndHidListener(new EmotionKeyboard.ShowAndHidListener() {

                    @Override
                    public void onClick() {
                        if (menuButton.isSelected()) {
                            moreView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onShowListener() {
                        editText.setVisibility(View.VISIBLE);
                        emojiButton.setSelected(true);
                        mikeButton.setSelected(false);
                        menuButton.setSelected(false);
                        recButton.setVisibility(View.INVISIBLE);
                        moreGridview.setVisibility(View.GONE);
                        getFragmentManager().beginTransaction().replace(R.id.more_input_container, emojiFragment).commit();
                        moreContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onHidListener() {
                        emojiButton.setSelected(false);
                        moreContainer.setVisibility(View.GONE);
                        getFragmentManager().beginTransaction().remove(emojiFragment).commit();
                    }
                })
                .build();

        EmotionKeyboard.with(getActivity())
                .setEmotionView(moreView)//绑定表情面板
                .bindToContent(contentView)//绑定内容view
                .bindToEditText(editText)//判断绑定那种EditView
                .bindToEmotionButton(menuButton)//绑定表情按钮
                .setShowAndHidListener(new EmotionKeyboard.ShowAndHidListener() {

                    @Override
                    public void onClick() {
                        if (emojiButton.isSelected()) {
                            moreView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onShowListener() {
                        editText.setVisibility(View.VISIBLE);
                        menuButton.setSelected(true);
                        emojiButton.setSelected(false);
                        mikeButton.setSelected(false);
                        moreGridview.setVisibility(View.VISIBLE);
                        moreContainer.setVisibility(View.GONE);
                        recButton.setVisibility(View.INVISIBLE);
                        getFragmentManager().beginTransaction().remove(emojiFragment).commit();
                    }

                    @Override
                    public void onHidListener() {
                        menuButton.setSelected(false);
                        moreGridview.setVisibility(View.GONE);
                    }
                })
                .build();

    }

    private String mPath;

    public EditText getInputView() {
        return editText;
    }

    public void hideMoreView() {
        if (moreView != null) {
            moreView.setVisibility(View.GONE);
            emojiButton.setSelected(false);
            menuButton.setSelected(false);
            imManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    public void setListener(final MessageInputListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (view == sendButton) {//发送按钮
            String content = Strings.toString(editText.getText());
            if (!Strings.isEmpty(content) && listener != null) {
                listener.onMessageSend(content, null);
            }
            editText.setText("");
            if (emojiButton.isSelected()) {
                return;
            }
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm != null) {
//                imm.showSoftInput(editText, 0);
//            }

            editText.setText("");
//            editText.requestFocusFromTouch();
        } else if (view == mikeButton) {
            mikeButton.setSelected(!mikeButton.isSelected());
            editText.setVisibility(mikeButton.isSelected() ? View.GONE : View.VISIBLE);
            recButton.setVisibility(mikeButton.isSelected() ? View.VISIBLE : View.INVISIBLE);
            if (mikeButton.isSelected()) {
                listener.chageState();
                hideMoreView();
            } else {
//                editText.requestFocus();
                editText.requestFocusFromTouch();
                moreView.setVisibility(View.GONE);
                imManager.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        } else if (view == menuButton) {
            if (menuButton.isSelected()) {
                menuButton.setSelected(false);
                hideMoreView();
                return;
            }
            menuButton.setSelected(true);
            emojiButton.setSelected(false);
            getFragmentManager().beginTransaction().remove(emojiFragment).commit();
            mikeButton.setSelected(false);
            editText.setVisibility(View.VISIBLE);
            recButton.setVisibility(View.INVISIBLE);
            moreContainer.setVisibility(View.GONE);
            moreGridview.setVisibility(View.VISIBLE);
            moreView.setVisibility(View.VISIBLE);
        } else if (view == emojiButton) {
            if (emojiButton.isSelected()) {
                hideMoreView();
                emojiButton.setSelected(false);
//                editText.requestFocus();
                editText.requestFocusFromTouch();
                imManager.showSoftInput(editText, InputMethodManager.HIDE_NOT_ALWAYS);
                return;
            }
            emojiButton.setSelected(true);
            moreGridview.setVisibility(View.GONE);
            imManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            getFragmentManager().beginTransaction().replace(R.id.more_input_container, emojiFragment).commit();
            mikeButton.setSelected(false);
            editText.setVisibility(View.VISIBLE);
            recButton.setVisibility(View.INVISIBLE);
            moreContainer.setVisibility(View.VISIBLE);
            moreView.setVisibility(View.VISIBLE);

        } else if (view == editText) {
            moreView.setVisibility(View.GONE);
            emojiButton.setSelected(false);
//                editText.requestFocus();
            editText.requestFocusFromTouch();
        }
    }

    @Override
    public void onPause() {
        editText.clearFocus();
        super.onPause();
    }

    @SuppressWarnings("deprecation")
    private boolean startRecord() {
        mSend = false;
        try {
            stopRecord();
            audioFile = new File(FileCacheUtils.getXmppAudioDir(getActivity()), String.valueOf(System.currentTimeMillis())
                    + ".amr");
            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.setAudioChannels(1);
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            timer = new Timer();
            minute = 0;
            second = 0f;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    second += 0.1f;
                    if (second >= 60) {
                        second = 0;
                        minute++;
                    }
                    if (minute >= 1) {
                        stopRecord();
                        handler.sendEmptyMessage(1001);
                    }
                    handler.sendEmptyMessage(1);
                }
            }, 100, 100);

            createRecordTipView();
        } catch (Exception e) {
            stopRecord();
            Ln.e(e);
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private DialogManager mDialogManager;

    private void createRecordTipView() {
        mDialogManager = new DialogManager(getActivity());
        mDialogManager.showRecordingDialog();
    }

    private boolean mSend;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void sendRecord() {
        if (mSend)
            return;
        mSend = true;
        String cachePath = FileCacheUtils.getXmppAudioDir(getActivity()) + "/" + System.currentTimeMillis() + ".amr";
        audioFile.renameTo(new File(cachePath));
        listener.onMessageSend(null, new Subject.File(cachePath, TYPE_AUDIO, String.valueOf(minute * 60 + (int) second), null));
    }

    // 获得声音的level
    public int getVoiceLevel(int maxLevel) {
        // mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
        if (mediaRecorder != null) {
            try {
                // 取证+1，否则去不到7
                return maxLevel * mediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
                // TODO Auto-generated catch block

            }
        }

        return 1;
    }

    private void stopRecord() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                Ln.e(e);
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mDialogManager != null) {
            mDialogManager.dimissDialog();
        }
    }

    @SuppressWarnings("deprecation")
    private void tipView(int type) {
        if (type == 3) {
            createRecordTipView();
        }
        if (mDialogManager == null) {
            return;
        }
//        TextView timeView = (TextView)dialog.findViewById(R.id.timeView);
//        TextView textView = (TextView)dialog.findViewById(R.id.textView);
//        ImageView iv_recoder = (ImageView)dialog.findViewById(R.id.iv_recoder);
        if (type == 0) {
            mDialogManager.recording();
//            iv_recoder.setImageResource(R.drawable.msg_rec_icon);
//            textView.setDettailText(R.string.slide_cancel_send);
//            textView.setBackgroundDrawable(null);
        } else if (type == 1) {
            mDialogManager.wantToCancel();
//            iv_recoder.setImageResource(R.drawable.msg_rec_cancel_icon);
//            textView.setDettailText(R.string.loosen_cancel_send);
//            textView.setBackgroundResource(R.drawable.msg_cancel_bg);
        } else if (type == 2) {
            int second = (int) this.second;
            mDialogManager.setTime((minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second));
            mDialogManager.updateVoiceLevel(getVoiceLevel(7));
//            timeView.setDettailText((minute < 10 ? "0"+minute : minute) + ":" + (second < 10 ? "0"+second : second));
        } else if (type == 3) {
            mDialogManager.tooShort();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDialogManager.dimissDialog();
                }
            }, 1000);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    mDialogManager.updateVoiceLevel(getVoiceLevel(7));
                    break;
                case 1001:
                    recText.setText(getString(R.string.hold_down_talk));
                    recButton.setSelected(false);
                    sendRecord();
                    break;
                default:
                    tipView(2);
                    break;
            }


        }
    };

    public EditText getEditText() {
        return editText;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                if (1 == requestCode) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> tmpList = new ArrayList<String>();
                            tmpList.addAll(Bimp.drr);
                            Bimp.drr.clear();
                            for (String add : tmpList) {
                                String path = getImgPath(add);
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = path;
                                mHandler.sendMessage(msg);
                            }
                        }
                    }).start();

                } else if (2 == requestCode) {
                    //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                    ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                    //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                    boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                    if (resultPaths !=null&&resultPaths.size()>0) {
                        mPath = resultPaths.get(0);
                        String path = getImgPath(mPath);
                        listener.onMessageSend(null, new Subject.File(path, TYPE_PICTURE, null, null));
                    }
                } else if (1001 == requestCode) {
                    String nick = data.getStringExtra("name");
                    if (editText != null) {
                        String str = editText.getText().toString();
                        StringBuffer stringBuffer = new StringBuffer(str);
                        int start = editText.getSelectionStart();
                        String atname = nick + " ";
                        stringBuffer.insert(start, atname);
                        editText.setText(stringBuffer);
                        editText.setSelection(start + atname.length());
                    }
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String path = (String) msg.obj;
                    listener.onMessageSend(null, new Subject.File(path, TYPE_PICTURE, null, null));
                    break;
            }

        }
    };

    private String getImgPath(String path) {
        Bitmap bm = null;
        try {
            bm = Bimp.revitionImageSize(path);
            bm = ImageUtility.checkFileDegree(path, bm);
            String newStr = path.substring(
                    path.lastIndexOf("/") + 1,
                    path.lastIndexOf("."));
            path = FileUtils.saveXmppBitmap(getActivity(), bm, "" + newStr);
        } catch (Exception e) {

        } finally {
            if (bm != null && !bm.isRecycled()) {
                bm.recycle();
                bm = null;
            }

        }
        return path;
    }

    @Override
    public void onLocationSelected(String address, double latitude, double longitude) {
        getActivity().onBackPressed();
        listener.onMessageSend(address, new Subject.Gps(latitude, longitude));
    }

    @Inject
    InputMethodManager imManager;
    @Inject
    Controller controller;


    /**
     * 绑定内容view
     *
     * @param contentView
     * @return
     */
    public void bindToContentView(View contentView) {
        this.contentView = contentView;
    }

    /**
     * 是否拦截返回键操作，如果此时表情布局未隐藏，先隐藏表情布局
     * @return true则隐藏表情布局，拦截返回键操作
     *         false 则不拦截返回键操作
     */
    public boolean isInterceptBackPress(){
        return mEmotionKeyboard.interceptBackPress();
    }
}
