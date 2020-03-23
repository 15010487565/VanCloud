package com.vgtech.vancloud.ui.common.group.tree;

import android.text.TextUtils;

import com.vgtech.common.api.Node;
import com.vgtech.common.api.TreeNodeBranch;
import com.vgtech.common.api.TreeNodeDepartment;
import com.vgtech.common.api.TreeNodeEmail;
import com.vgtech.common.api.TreeNodeId;
import com.vgtech.common.api.TreeNodeJob;
import com.vgtech.common.api.TreeNodeLabel;
import com.vgtech.common.api.TreeNodePhone;
import com.vgtech.common.api.TreeNodePhoto;
import com.vgtech.common.api.TreeNodePid;
import com.vgtech.common.api.TreeNodeType;
import com.vgtech.common.api.TreeNodeUser;
import com.vgtech.vancloud.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class TreeHelper {
    /**
     * 传入我们的普通bean，转化为我们排序后的Node
     *
     * @param datas
     * @param defaultExpandLevel
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static List<Node> getSortedNodes(List<Node> datas,
                                            int defaultExpandLevel) throws IllegalArgumentException,
            IllegalAccessException

    {
        List<Node> result = new ArrayList<Node>();
        // 将用户数据转化为List<Node>
//        List<Node> nodes = convetData2Node(datas);
        // 拿到根节点
        List<Node> rootNodes = getRootNodes(datas);
        // 排序以及设置Node间关系
        for (Node node : rootNodes) {
            addNode(result, node, defaultExpandLevel, 1);
        }
        return result;
    }

    public static int getChildCount(Node node) {
        int count = 0;
        for (Node n : node.getChildren()) {
            if (!n.isUser()) {
                count += getChildCount(n);
            } else {
                count++;
            }
        }
        return count;
    }
    /**
     * 过滤出所有可见的Node
     *
     * @param nodes
     * @return
     */
    public static List<Node> filterVisibleNode(List<Node> nodes) {
        List<Node> result = new ArrayList<Node>();

        for (Node node : nodes) {
            // 如果为跟节点，或者上层目录为展开状态
            if (node.isRoot() || node.isParentExpand()) {
                setNodeIcon(node);
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 将我们的数据转化为树的节点
     *
     * @param datas
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> List<Node> convetData2Node(List<T> datas)
            throws IllegalArgumentException, IllegalAccessException

    {
        List<Node> nodes = new ArrayList<Node>();
        Node node = null;
        if(datas==null) return nodes;
        for (T t : datas) {
            String id = "-1";
            String pId = "-1";
            int type = 0;
            String label = null;
            String job = null;
            String dempartment = null;
            String phone = null;
            String email = null;
            String photo = null;
            String isBranch = null;
            boolean isUser = false;
            String isCanClick = null;
            Class<? extends Object> clazz = t.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field f : declaredFields) {
                if (f.getAnnotation(TreeNodeId.class) != null) {
                    f.setAccessible(true);
                    id = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeBranch.class) != null) {
                    f.setAccessible(true);
                    isBranch = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeType.class) != null) {
                    f.setAccessible(true);
                    type = f.getInt(t);
                }
                if (f.getAnnotation(TreeNodePid.class) != null) {
                    f.setAccessible(true);
                    pId = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeLabel.class) != null) {
                    f.setAccessible(true);
                    label = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeJob.class) != null) {
                    f.setAccessible(true);
                    job = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodePhoto.class) != null) {
                    f.setAccessible(true);
                    photo = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeDepartment.class) != null) {
                    f.setAccessible(true);
                    dempartment = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodePhone.class) != null) {
                    f.setAccessible(true);
                    phone = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeEmail.class) != null) {
                    f.setAccessible(true);
                    email = (String) f.get(t);
                }
                if (f.getAnnotation(TreeNodeUser.class) != null) {
                    f.setAccessible(true);
                    isUser = (Boolean) f.get(t);
                }
//                if (f.getAnnotation(TreeNodeClick.class) != null) {
//                    f.setAccessible(true);
//                    isCanClick = (String) f.get(t);
//                }
//                if (id != -1 && pId != -1 && label != null) {
//                    break;
//                }
            }
            boolean isbranch = !TextUtils.isEmpty(isBranch) && "1".equals(isBranch);
            node = new Node(id, pId, label, job, dempartment, phone, isUser, photo, type);
            node.isBranch = isbranch;
            node.setEmail(email);
            nodes.add(node);
        }

        /**
         * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
         */
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node m = nodes.get(j);
                if (m.getpId().equals(n.getId())) {
                    n.getChildren().add(m);
                    m.setParent(n);
                } else if (m.getId().equals(n.getpId())) {
                    m.getChildren().add(n);
                    n.setParent(m);
                }
            }
        }

        // 设置图片
        for (Node n : nodes) {
            setNodeIcon(n);
        }
        return nodes;
    }

    private static List<Node> getRootNodes(List<Node> nodes) {
        List<Node> root = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.isRoot())
                root.add(node);
        }
        return root;
    }

    /**
     * 把一个节点上的所有的内容都挂上去
     */
    private static void addNode(List<Node> nodes, Node node,
                                int defaultExpandLeval, int currentLevel) {

        nodes.add(node);
        if (defaultExpandLeval >= currentLevel) {
            node.setExpand(true);
        }

        if (node.isLeaf())
            return;
        for (int i = 0; i < node.getChildren().size(); i++) {
            addNode(nodes, node.getChildren().get(i), defaultExpandLeval,
                    currentLevel + 1);
        }
    }

    /**
     * 设置节点的图标
     *
     * @param node
     */
    private static void setNodeIcon(Node node) {
        if (node.getChildren().size() > 0 && node.isExpand()) {
            node.setIcon(R.mipmap.tree_ex);
        } else if (node.getChildren().size() > 0 && !node.isExpand()) {
            node.setIcon(R.mipmap.tree_ec);
        } else
            node.setIcon(-1);

    }

}
