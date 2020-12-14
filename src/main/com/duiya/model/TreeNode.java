package com.duiya.model;

import java.util.ArrayList;

public class TreeNode {
    private TreeNode left;
    private TreeNode right;
    private int value;
    private int vocabIndex;  // 词表的位置
    private int hiddenEmbeddingIndex;  // 词嵌入位置
    private int watch;
    private static ArrayList<TreeNode> treeNodes = new ArrayList<>();

    public int getWatch() {
        return watch;
    }

    public void setWatch(int watch) {
        this.watch = watch;
    }

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode(){
        this.left = null;
        this.right = null;
        this.vocabIndex = -1;
        this.watch = 0;
    }

    public TreeNode(int value) {
        this.value = value;
        this.left = null;
        this.right = null;
        this.vocabIndex = -1;
        this.watch = 0;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setVocabIndex(int vocabIndex) {
        this.vocabIndex = vocabIndex;
    }

    public void setHiddenEmbeddingIndex(int hiddenEmbeddingIndex) {
        this.hiddenEmbeddingIndex = hiddenEmbeddingIndex;
    }

    public TreeNode getRight() {
        return right;
    }

    public int getValue() {
        return value;
    }

    public int getVocabIndex() {
        return vocabIndex;
    }

    public int getHiddenEmbeddingIndex() {
        return hiddenEmbeddingIndex;
    }

    public static void addToList(TreeNode node){
        if (TreeNode.treeNodes.size() == 0){
            TreeNode.treeNodes.add(node);
        }else {
            int index = 0;
            while (index < TreeNode.treeNodes.size() && node.getValue() > TreeNode.treeNodes.get(index).getValue()) {
                index++;
            }
            TreeNode.treeNodes.add(index, node);
        }
    }

    public static void delHead(){
        if(TreeNode.treeNodes.size() > 0){
            TreeNode.treeNodes.remove(0);
        }
    }

    public static TreeNode getFromList(int index){
        return TreeNode.treeNodes.get(index);
    }

    public static TreeNode popFromList(){
        TreeNode node = TreeNode.treeNodes.get(0);
        TreeNode.treeNodes.remove(0);
        return node;
    }

    public static int getListSize(){
        return TreeNode.treeNodes.size();
    }


}
