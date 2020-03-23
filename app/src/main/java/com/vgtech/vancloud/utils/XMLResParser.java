package com.vgtech.vancloud.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.vgtech.common.Constants;
import com.vgtech.vancloud.R;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XMLResParser {

    private final static String LOG_TAG = "ResXMLParser";
    private final Context mContext;

    public XMLResParser(Context context) {
        mContext = context;
    }

    public RootData parser(int xmlId) {
        RootData result = new RootData();

        XMLResData tempData = null;
        int tempDepth;
        XMLResData data = result;
        int depth = 1;

        try {
            XmlResourceParser xml = mContext.getResources().getXml(xmlId);
            int xmlEventType;

            String name = null;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                switch (xmlEventType) {
                    case XmlResourceParser.START_TAG:
                        name = xml.getName();
                        tempDepth = xml.getDepth();
                        if (tempDepth == 1) {
                            result.fillAttribute(xml);
                        }

                        if (tempDepth - 1 == depth) {
                            tempData = XMLResDataFactory.getData(name);
                            if (tempData != null) {
                                tempData.fillAttribute(xml);
                                if (tempData.isValid()) {
                                    data.addChild(tempData);
                                    data = tempData;
                                    depth = tempDepth;
                                }
                            }
                        }
                        break;
                    case XmlResourceParser.TEXT:
                        if (tempData == data) {
                            data.fillText(xml.getText());
                        }
                        break;
                    case XmlResourceParser.END_TAG:
                        if (xml.getDepth() == depth) {
                            data = data.getParent();
                            depth--;
                        }
                        break;
                    default:
                        /**
                         *ignore
                         */
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while trying to load a xml data in res.", e);
        }

        return result;
    }

    public static class XMLResDataFactory {
        public static XMLResData getData(String name) {
            if ("item".equalsIgnoreCase(name)) {
                return new MenuItem();
            } else if ("appmenu".equalsIgnoreCase(name)) {
                return new AppMenu();
            } else if ("intent".equalsIgnoreCase(name)) {
                return new IntentItem();
            } else if ("onClick".equalsIgnoreCase(name)) {
                return new OnClickItem();
            } else if ("contentItem".equalsIgnoreCase(name)) {
                return new ContentItem();
            } else if ("title".equalsIgnoreCase(name)) {
                return new TitleItem();
            } else if ("comment".equalsIgnoreCase(name)) {
                return new CommentItem();
            } else if ("editor".equalsIgnoreCase(name)) {
                return new EditItem();
            } else if ("markable".equalsIgnoreCase(name)) {
                return new MarkableItem();
            }

            return null;
        }
    }

    public static abstract class XMLResData {
        protected final List<XMLResData> mChildren;
        protected Map<String, XMLResData> mMap;
        protected String key = null;
        protected String text;
        private XMLResData mParent;

        public XMLResData() {
            mChildren = new ArrayList<XMLResData>();
        }

        boolean isValid() {
            return true;
        }

        abstract void fillAttribute(XmlResourceParser xml);

        void fillText(String text) {
            this.text = text;
        }

        public <T extends XMLResData> T remove(T child) {
            mChildren.remove(child);
            if (!TextUtils.isEmpty(child.key)) {
                Map<String, XMLResData> map = mMap;
                if (map != null) {
                    map.remove(child.key.toLowerCase());
                }
            }
            return child;
        }

        public void addChild(XMLResData child) {
            child.mParent = this;
            mChildren.add(child);
            if (!TextUtils.isEmpty(child.key)) {
                Map<String, XMLResData> map = mMap;
                if (map == null) {
                    map = mMap = new HashMap<String, XMLResData>();
                }
                map.put(child.key.toLowerCase(), child);
            }
        }

        XMLResData getParent() {
            return mParent;
        }

        public boolean hasChild(Class<? extends XMLResData> c, boolean fullMatch) {
            for (XMLResData child : mChildren) {
                if (fullMatch && c.equals(child.getClass())) {
                    return true;
                } else if (c.isAssignableFrom(child.getClass())) {
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        public <T extends XMLResData> T getChild(Class<? extends T> c,
                                                 boolean fullMatch) {
            for (XMLResData child : mChildren) {
                if (fullMatch && c.equals(child.getClass())) {
                    return (T) child;
                } else if (c.isAssignableFrom(child.getClass())) {
                    return (T) child;
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T extends XMLResData> T getChild(String key) {
            Map<String, XMLResData> map = mMap;
            T result = null;
            if (map != null && key != null) {
                try {
                    result = (T) map.get(key.toLowerCase());
                } catch (ClassCastException e) {
                    result = null;
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        public <T extends XMLResData> T[] getChildren(Class<? extends T> c,
                                                      boolean fullMatch) {
            ArrayList<T> list = new ArrayList<T>();

            for (XMLResData child : mChildren) {
                if (fullMatch && c.equals(child.getClass())) {
                    list.add((T) child);
                } else if (c.isAssignableFrom(child.getClass())) {
                    list.add((T) child);
                }
            }

            T[] result = (T[]) Array.newInstance(c, list.size());
            list.toArray(result);
            return result;
        }
    }

    public static class RootData extends XMLResData {
        protected String title;
        protected String label;
        protected int pos;

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            title = xml.getAttributeValue(null, "title");
            pos = xml.getAttributeIntValue(null, "pos", 0);
        }

        public String toString() {
            return String.format("{title=%s}", title);
        }
    }

    public static class MarkableItem extends MenuItem {
        protected String hint;
        protected boolean marked;
        protected int markOnIcon = 0;
        protected int markOffIcon = 0;

        @Override
        boolean isValid() {
            return super.isValid() && !TextUtils.isEmpty(key);
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            hint = xml.getAttributeValue(null, "hint");
            marked = xml.getAttributeBooleanValue(null, "marked", false);

            String markOnIcon = xml.getAttributeValue(null, "markOnIcon");
            String markOffIcon = xml.getAttributeValue(null, "markOffIcon");

            super.fillAttribute(xml);
        }
    }

    public static class MenuItem extends XMLResData {
        protected String id;
        protected int label;
        protected int icon;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getLabel() {
            return label;
        }

        public void setLabel(int label) {
            this.label = label;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            id = xml.getAttributeValue(null, "id");
            label = getResId(Uri.parse(xml.getAttributeValue(null, "label")), 0);
            icon = getResId(Uri.parse(xml.getAttributeValue(null, "icon")), 0);
        }

    }

    public static class AppMenu extends XMLResData {
        protected String tag;
        protected int name;
        protected int icon;
        protected int color;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public int getName() {
            return name;
        }

        public void setName(int name) {
            this.name = name;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            tag = xml.getAttributeValue(null, "tag");
            name = getResId(Uri.parse(xml.getAttributeValue(null, "name")), 0);
            icon = getResId(Uri.parse(xml.getAttributeValue(null, "icon")), 0);
            color = getResId(Uri.parse(xml.getAttributeValue(null, "color")), 0);
        }

        @Override
        public String toString() {
            return "AppMenu{" +
                    "tag='" + tag + '\'' +
                    ", name=" + name +
                    ", icon=" + icon +
                    ", color=" + color +
                    '}';
        }
    }

    public static class IntentItem extends XMLResData {
        private String targetPackage;
        private String targetClass;
        private String action;
        private Uri data;
        private boolean external;
        protected int requestCode;

        @Override
        boolean isValid() {
            return !TextUtils.isEmpty(action)
                    || (!TextUtils.isEmpty(targetPackage) && !TextUtils
                    .isEmpty(targetClass));
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            action = xml.getAttributeValue(null, "action");
            targetPackage = xml.getAttributeValue(null, "targetPackage");
            targetClass = xml.getAttributeValue(null, "targetClass");
            String data = xml.getAttributeValue(null, "data");
            if (data != null) {
                this.data = Uri.parse(data);
            }
            external = xml.getAttributeBooleanValue(null, "external", false);
            requestCode = xml.getAttributeIntValue(null, "requestCode", -1);
        }

        public Intent getIntent(Context context) {
            Intent intent = new Intent(action);
            if (!TextUtils.isEmpty(targetPackage)
                    && !TextUtils.isEmpty(targetClass)) {
                intent.setClassName(targetPackage, targetClass);
            }
            if (data != null) {
                intent.setData(data);
            }
            return intent;
        }

        public String toString() {
            return String.format(
                    "{action=%s, data=%s, package=%s, class=%s, external=%s}",
                    action, data, targetPackage, targetClass, external);
        }
    }

    public static class OnClickItem extends XMLResData {
        private int id = -1;

        @Override
        boolean isValid() {
            return id != -1;
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            id = xml.getAttributeIntValue(null, "id", -1);
        }

        public String toString() {
            return String.format("{Id=%d}", id);
        }
    }

    public static class ContentItem extends XMLResData {
        private int pos = 0;
        private int gravity = 19;
        private String label;

        @Override
        void fillAttribute(XmlResourceParser xml) {
            key = xml.getAttributeValue(null, "key");
            pos = xml.getAttributeIntValue(null, "pos", 0);
            gravity = xml.getAttributeIntValue(null, "gravity", 19);
            label = xml.getAttributeValue(null, "label");
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String toString() {
            return String.format("{Key=%s, Pos=%d, Label=%s, text=%s}", key,
                    pos, label, text);
        }
    }

    public static class TitleItem extends XMLResData {
        private String label;

        @Override
        boolean isValid() {
            return !TextUtils.isEmpty(label);
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            label = xml.getAttributeValue(null, "label");
        }

        public String toString() {
            return String.format("{Label=%s}", label);
        }
    }

    public static class CommentItem extends XMLResData {
        private String label;

        @Override
        boolean isValid() {
            return !TextUtils.isEmpty(label);
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            label = xml.getAttributeValue(null, "label");
        }

        public String toString() {
            return String.format("{Label=%s}", label);
        }
    }

    public static class EditItem extends XMLResData {
        private int pos;
        private String label;
        private String hint;
        private String type;
        protected int gravity = 19;
        protected boolean multiline = false;

        protected String value;

        // public WeakReference<View> viewRef;

        @Override
        boolean isValid() {
            return label != null;// !TextUtils.isEmpty(label);
        }

        @Override
        void fillAttribute(XmlResourceParser xml) {
            key = xml.getAttributeValue(null, "key");
            pos = xml.getAttributeIntValue(null, "pos", 0);
            label = xml.getAttributeValue(null, "label");
            hint = xml.getAttributeValue(null, "hint");
            type = xml.getAttributeValue(null, "type");
            gravity = xml.getAttributeIntValue(null, "gravity", 19);
            multiline = xml.getAttributeBooleanValue(null, "multiline", false);
        }

        public String toString() {
            return String.format(
                    "{Key=%s, Pos=%d, Label=%s, hint=%s, type=%s}", key, pos,
                    label, hint, type);
        }
    }

    private static final Map<String, Class<?>> sClassMap;
    private static final Map<String, Class<?>> sCommonClassMap;

    static {
        sClassMap = new HashMap<String, Class<?>>();
        Class<?>[] classes = R.class.getClasses();
        for (Class<?> cl : classes) {
            sClassMap.put(cl.getSimpleName(), cl);
        }
        sCommonClassMap = new HashMap<String, Class<?>>();
        Class<?>[] commonClasses = com.vgtech.common.R.class.getClasses();
        for (Class<?> cl : commonClasses) {
            sCommonClassMap.put(cl.getSimpleName(), cl);
        }
    }

    public static int getResId(Uri uri, int defId) {
        int id = defId;
        if (uri != null
                && ContentResolver.SCHEME_ANDROID_RESOURCE.equalsIgnoreCase(uri
                .getScheme())
        ) {
            if (Constants.PACKAGE_NAME.equalsIgnoreCase(uri.getHost())) {
                List<String> paths = uri.getPathSegments();
                Class<?> cl = sClassMap.get(paths.get(0));
                if (cl != null && paths.size() == 2) {
                    try {
                        Field field = cl.getField(paths.get(1));
                        if (field != null) {
                            id = field.getInt(null);
                        }
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "The Res id is not validate: " + uri);
                    }
                }
            } else if (Constants.COMMON_PACKAGE_NAME.equals(uri.getHost())) {
                List<String> paths = uri.getPathSegments();
                Class<?> cl = sCommonClassMap.get(paths.get(0));
                if (cl != null && paths.size() == 2) {
                    try {
                        Field field = cl.getField(paths.get(1));
                        if (field != null) {
                            id = field.getInt(null);
                        }
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "The Res id is not validate: " + uri);
                    }
                }
            }

        }

        return id;
    }
}
