package com.vgtech.common.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Node implements Parcelable {
    private String id;
    /**
     * 根节点pId为0
     */
    private String pId = "0";

    private String name;

    /**
     * 当前的级别
     */
    private int level;

    private String job;
    private String department;
    /**
     * 是否展开
     */
    private boolean isExpand = false;


    private int icon;

    /**
     * 下一级的子Node
     */
    private List<Node> children = new ArrayList<Node>();

    /**
     * 父Node
     */
    private Node parent;
    private String phone;
    private boolean isUser;
    private String photo;
    public boolean isBranch;
    public int type;
    private String email;

    public String email() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(photo);
        dest.writeString(phone);
        dest.writeInt(isUser ? 1 : 0);
        dest.writeInt(type);
        dest.writeString(email);
//        dest.writeLong(pId);
//        dest.writeInt(level);
//        dest.writeInt(icon);
//        dest.writeParcelable(parent, flags);
//        dest.writeInt(isUser ? 1 : 0);
//        dest.writeList(children);
    }

    public static final Parcelable.Creator<Node> CREATOR = new Parcelable.Creator<Node>() {
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    private Node(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        id = in.readString();
        name = in.readString();
        photo = in.readString();
        phone = in.readString();
        isUser = in.readInt() == 1;
        type = in.readInt();
        email = in.readString();
//        pId = in.readLong();
//        level = in.readInt();
//        icon = in.readInt();
//        parent = in.readParcelable(Node.class.getClassLoader());
//        isUser = in.readInt()==1;
//        children = in.readArrayList(Node.class.getClassLoader());
    }

    public Node() {
    }

    public Node(String id, String pId, String name, String job, String department, String phone, boolean isUser, String photo) {
        super();
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.job = job;
        this.department = department;
        this.phone = phone;
        this.isUser = isUser;
        this.photo = photo;
    }

    public Node(String id, String pId, String name, String job, String department, String phone, boolean isUser, String photo, int type) {
        super();
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.job = job;
        this.department = department;
        this.phone = phone;
        this.isUser = isUser;
        this.photo = photo;
        this.type = type;
    }


    public Node(String id, String name, boolean isUser, String photo) {
        super();
        this.id = id;
        this.name = name;
        this.isUser = isUser;
        this.photo = photo;
    }

    public Node(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * 是否为跟节点
     *
     * @return
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 判断父节点是否展开
     *
     * @return
     */
    public boolean isParentExpand() {
        if (parent == null)
            return false;
        return parent.isExpand();
    }

    /**
     * 是否是叶子界点
     *
     * @return
     */
    public boolean isLeaf() {
        return children.size() == 0;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setIsUser(boolean isUser) {
        this.isUser = isUser;
    }


    /**
     * 获取level
     */
    public int getLevel() {
        return parent == null ? level != 0 ? level : 0 : parent.getLevel() + 1;
    }

    /**
     * 设置展开
     *
     * @param isExpand
     */
    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
        if (!isExpand) {

            for (Node node : children) {
                node.setExpand(isExpand);
            }
        }
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)                                      //先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Node))
            return false;

        final Node node = (Node) other;

        if (!getId().equals(((Node) other).getId()))
            return false;
//        if (!getName().equals(((Node) other).getName()))
//            return false;
        return true;
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", pId='" + pId + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", job='" + job + '\'' +
                ", department='" + department + '\'' +
                ", isExpand=" + isExpand +
                ", icon=" + icon +
                ", children=" + children +
                ", parent=" + parent +
                ", phone='" + phone + '\'' +
                ", isUser=" + isUser +
                ", photo='" + photo + '\'' +
                ", isBranch=" + isBranch +
                ", type=" + type +
                ", email='" + email + '\'' +
                '}';
    }
}
