package com.vgtech.vancloud.utils;

import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class EditUtils {

    public static final int BLACK = 0xFF636363;
    public static final int GREY = 0x4D636363;
    public static final int RED = 0xFFFF3F3F;
    public static final int DIAPHANEITYRED = 0xFFFD9595;
    public static final int ORANGE = 0xFFFF7C42;
    public static final int DIAPHANEITYORANGE = 0xFFF6B391;

    /**
     * @return 返回字符长度
     */
    public static void limitEditTextLength(final EditText editText,
                                           final int limit, final TextWatcher textWatcher) {
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int start, int before,
                                      int count) {
                if (textWatcher != null)
                    textWatcher.onTextChanged(cs, start, before, count);
                String c = cs.toString();
                double len = 0;
                int limitIndex = 0;
                for (int i = 0; i < c.length(); i++) {
                    int tmp = (int) c.charAt(i);
                    if (tmp > 0 && tmp < 127) {
                        len += 0.5;
                    } else {
                        len++;
                    }
                    if (Math.round(len) * 2 > limit) {
                        boolean isEmoji = !isEmojiCharacter(c.charAt(i));
                        if (isEmoji && i + 1 < c.length()) {

                            boolean tisEmoji = !isEmojiCharacter(c
                                    .charAt(i + 1));
                            if (tisEmoji) {
                                limitIndex = i;
                                break;
                            }
                        } else if (isEmoji) {
                            boolean tisEmoji = !isEmojiCharacter(c
                                    .charAt(i - 1));
                            if (tisEmoji) {
                                limitIndex = i - 1;
                                break;
                            }
                        }
                        limitIndex = i;
                        break;
                    }
                }
                if (limitIndex != 0) {
                    editText.setText(c.subSequence(0, limitIndex));
                    editText.setSelection(limitIndex);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (textWatcher != null)
                    textWatcher.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textWatcher != null)
                    textWatcher.afterTextChanged(s);
            }
        });
    }

    public static String subString(CharSequence c, int limit) {
        double len = 0;
        int limitIndex = 0;
        for (int i = 0; i < c.length(); i++) {
            int tmp = (int) c.charAt(i);
            if (tmp > 0 && tmp < 127) {
                len += 0.5;
            } else {
                len++;
            }
            if (Math.round(len) * 2 > limit) {
                boolean isEmoji = !isEmojiCharacter(c.charAt(i));
                if (isEmoji && i + 1 < c.length()) {

                    boolean tisEmoji = !isEmojiCharacter(c
                            .charAt(i + 1));
                    if (tisEmoji) {
                        limitIndex = i;
                        break;
                    }
                } else if (isEmoji) {
                    boolean tisEmoji = !isEmojiCharacter(c
                            .charAt(i - 1));
                    if (tisEmoji) {
                        limitIndex = i - 1;
                        break;
                    }
                }
                limitIndex = i;
                break;
            }
        }
        String str = null;
        if (limitIndex != 0) {
            str = c.subSequence(0, limitIndex).toString();
        } else {
            str = c.toString();
        }
        return str;
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA)
                || (codePoint == 0xD)
                || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }


    public static ColorStateList createColorStateList(int normal, int pressed, int cheched, int focused, int unable) {

        int[] colors = new int[]{pressed, cheched, pressed, focused, normal, focused, unable, normal};

        int[][] states = new int[8][];

        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};

        states[1] = new int[]{android.R.attr.state_checked, android.R.attr.state_enabled};

        states[2] = new int[]{android.R.attr.state_selected, android.R.attr.state_enabled};

        states[3] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};

        states[4] = new int[]{android.R.attr.state_enabled};

        states[5] = new int[]{android.R.attr.state_focused};

        states[6] = new int[]{android.R.attr.state_window_focused};

        states[7] = new int[]{};

        ColorStateList colorList = new ColorStateList(states, colors);

        return colorList;

    }

    public static ColorStateList redCreateColorStateList() {
        return createColorStateList(RED, DIAPHANEITYRED, DIAPHANEITYRED, DIAPHANEITYRED, RED);
    }

    public static ColorStateList greyCreateColorStateList() {
        return createColorStateList(BLACK, GREY, GREY, GREY, BLACK);
    }

    public static ColorStateList orangeCreateColorStateList() {
        return createColorStateList(ORANGE, DIAPHANEITYORANGE, DIAPHANEITYORANGE, DIAPHANEITYORANGE, ORANGE);
    }


    public static void SetTextViewMaxLines(TextView textView, int maxLines) {

        textView.setMaxLines(maxLines);
        textView.setEllipsize(TextUtils.TruncateAt.END);

    }


}
