package com.duiya.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VocabList {
    private ArrayList<Vocab> vocabList;  // 词表
    private int[] hash_index;  // 词的哈希表，哈希值-->索引
    private int vocabHashSize;  //哈希表大小
    private int minRudece = 2;
    private final int table_size = (int)Math.pow(10, 8);
    private int[] negativeSamplingTable = new int[table_size];
    private float[][] embedding;
    private float[][] hidden_embedding;
    private Random random;

    public VocabList() {
        this.minRudece = 2;
        this.vocabHashSize = 30000000;

        this.hash_index = new int[this.vocabHashSize];
        this.vocabList = new ArrayList<Vocab>(1000);
        init();

    }

    public VocabList(int len, int vocabHashSize) {
        this.vocabList = new ArrayList<Vocab>(len);
        this.hash_index = new int[vocabHashSize];
        this.vocabHashSize = vocabHashSize;
        init();
    }

    public void init(){
        for(int i = 0; i < this.hash_index.length; i++){
            this.hash_index[i] = -1;
        }
    }

    public int[] getNegativeSamplingTable() {
        return negativeSamplingTable;
    }

    public float[][] getEmbedding() {
        return embedding;
    }

    public float[][] getHidden_embedding() {
        return hidden_embedding;
    }

    /**
     * 添加词
     * @param str
     * @return index
     */
    public int add(String str){
        int index = this.searchVocab(str);// 查找词在词库中的位置
        if (index == -1) {// 没有查找到对应的词
            Vocab word = new Vocab();
            word.setCount(1);
            word.setWord(str);
            this.vocabList.add(word);
            index = this.vocabList.size()-1;
            int hash = this.getWordHash(str);
            while (this.hash_index[hash] != -1) {
                hash = (hash + 1) % this.vocabHashSize;// hash的碰撞检测
            }
            this.hash_index[hash] = index;// 词的hash值->词的词库中的索引
        } else {
            vocabList.get(index).countAdd();
        }

        if (vocabList.size() > this.vocabHashSize * 0.7){
            this.reduceVocab();
        }
        return index;
    }

    /**
     * 搜索列表中是否有某个词
     * @param str
     * @return index
     */
    public int searchVocab(String str){
        int hash = getWordHash(str);

        while (true) {
            if (this.hash_index[hash] == -1) {
                return -1;// 不存在该词
            }
            if (str.equals(this.vocabList.get(hash_index[hash]).getWord())){
                return this.hash_index[hash];// 返回索引值
            }
            hash = (hash + 1) % this.vocabHashSize;
        }
    }

    /**
     * 获取hash值
     * @param str
     * @return
     */
    public int getWordHash(String str) {
        int hash = str.hashCode();
        if(hash >= 0) {
            hash = hash % this.vocabHashSize;
        }else {
            hash = this.vocabHashSize + hash % this.vocabHashSize;
        }
        return hash;
    }

    /**
     * 删掉词频小的词
     */
    public void reduceVocab(){
        int b = 0;
        for(int i = 0; i < this.vocabList.size(); i++){
            if (this.vocabList.get(i).getCount() > this.minRudece && !this.vocabList.get(i).getWord().equals("</s>") && !this.vocabList.get(i).getWord().equals("</default>")){
                this.vocabList.get(b).setWord(this.vocabList.get(i).getWord());
                this.vocabList.get(b).setCount(this.vocabList.get(i).getCount());
                b += 1;

            }
        }
        for(int i = b; i < this.vocabList.size(); i++){
            this.vocabList.remove(b);
        }

        for(int i = 0; i < this.vocabHashSize; i++){
            this.hash_index[i] = -1;
        }
        for(int i = 0; i < this.vocabList.size(); i++){
            int hash = getWordHash(this.vocabList.get(i).getWord());
            while (this.hash_index[hash] != -1){
                hash = (hash + 1) % this.vocabHashSize;
            }
            this.hash_index[hash] = i;
        }
        this.minRudece++;
    }

    /**
     * 对词列表排序
     */
    public void sortVocab(){
        // hash值重算
        for(int i = 0; i < hash_index.length; i++){
            this.hash_index[i] = -1;
        }

        Collections.sort(vocabList);
//        Collections.reverse(vocabList);

        for(int i = 0; i < this.vocabList.size(); i++){
            int hash = getWordHash(this.vocabList.get(i).getWord());
            while (this.hash_index[hash] != -1){
                hash = (hash + 1) % this.vocabHashSize;
            }
            this.hash_index[hash] = i;
        }
    }

    /**
     * 按下标获取
     * @param index
     * @return 词
     */
    public Vocab get(int index){
        if(index < this.vocabList.size()){
            return this.vocabList.get(index);
        }
        return null;
    }

    /**
     * 返回词表大小
     * @return
     */
    public int size(){
        return this.vocabList.size();
    }

    /**
     * 初始化负采样
     */
    public void initNegativeSamplingTable(){
        double trainWordPow = 0.0, power=0.75;
        for(Vocab vocab : vocabList){
            trainWordPow += Math.pow(vocab.getCount(), power);
        }
        int i = 0;
        double nowPow = Math.pow(vocabList.get(i).getCount(), power);
        for(int a = 0; a < this.table_size; a++){
            this.negativeSamplingTable[a] = i;
            if((a+1)/(double)this.table_size > nowPow){
                i++;
                nowPow += Math.pow(vocabList.get(i).getCount(), power)/trainWordPow;
            }
            if(i > this.vocabList.size()){
                i = this.vocabList.size() - 1;
            }
        }
        this.random = new Random();
    }

    public int getNextNegativeSample(){
        int value = random.nextInt(this.table_size);
        return this.negativeSamplingTable[value];
    }

    /**
     * 初始化嵌入
     * @param embeddingSize
     */
    public void initEmbedding(int embeddingSize){
        this.embedding = new float[this.vocabList.size()][embeddingSize];
        Random random = new Random();
        for(int i = 0; i < this.embedding.length; i++){
            for(int j = 0; j < embeddingSize; j++){
                embedding[i][j] = random.nextFloat();
            }
        }
    }

    /**
     * 初始化映射
     * @param embeddingSize
     * @param haff
     */
    public void initHiddenEmbedding(int embeddingSize, int haff){
        if (haff == 1){
            this.hidden_embedding = new float[this.vocabList.size() - 1][embeddingSize];  // 哈夫曼树
        }else {
            this.hidden_embedding = new float[this.vocabList.size()][embeddingSize];
        }
        Random random = new Random();
        for(int i = 0; i < this.hidden_embedding.length; i++){
            for(int j = 0; j < embeddingSize; j++){
                embedding[i][j] = random.nextFloat();
            }
        }
    }

    /**
     * 构造哈夫曼树
     */
    public void createHuffmanTree(){
        int nowHiddenIndex = 0;
        int vocabIndex = 0;
        while (vocabIndex < this.vocabList.size()){
            if(TreeNode.getListSize() < 2){  // 从列表中添加点
                TreeNode node = new TreeNode(this.vocabList.get(vocabIndex).getCount());
                node.setVocabIndex(vocabIndex++);
                TreeNode.addToList(node);
            }else {
                if(this.vocabList.get(vocabIndex).getCount() >= TreeNode.getFromList(1).getValue()){  // 合并点
                    TreeNode right = TreeNode.popFromList();
                    TreeNode left = TreeNode.popFromList();
                    TreeNode node = new TreeNode(right.getValue()+left.getValue());
                    node.setLeft(left);
                    node.setRight(right);
                    node.setHiddenEmbeddingIndex(nowHiddenIndex++);
                    TreeNode.addToList(node);
                }else { // 从列表中添加点
                    TreeNode node = new TreeNode(this.vocabList.get(vocabIndex).getCount());
                    node.setVocabIndex(vocabIndex++);
                    TreeNode.addToList(node);
                }
            }
        }

        while (TreeNode.getListSize() > 1){
            TreeNode right = TreeNode.popFromList();
            TreeNode left = TreeNode.popFromList();
            TreeNode node = new TreeNode(right.getValue()+left.getValue());
            node.setLeft(left);
            node.setRight(right);
            node.setHiddenEmbeddingIndex(nowHiddenIndex++);
            TreeNode.addToList(node);
        }
        TreeNode root = TreeNode.getFromList(0);

        List<TreeNode> path = new ArrayList<TreeNode>();
        List<Integer> direct = new ArrayList<Integer>();
        path.add(root);
        while (path.size() > 0){
            TreeNode node = path.get(path.size()-1);
            if(node.getWatch() == 0){// 还没有走过
                if(node.getVocabIndex() == -1){ // 这是非叶子节点
                    node.setWatch(1);
                    path.add(node.getLeft());
                    direct.add(1);
                }else { // 这是叶子节点
                    List<Integer> pathIndex = new ArrayList<Integer>();
                    path.remove(path.size()-1);
                    for(TreeNode val : path){
                        pathIndex.add(val.getHiddenEmbeddingIndex());
                    }
                    List<Integer> pathDirect = new ArrayList<Integer>();
                    for(Integer val : direct){
                        pathDirect.add(val);
                    }
                    this.vocabList.get(node.getVocabIndex()).setCode(pathDirect);
                    this.vocabList.get(node.getVocabIndex()).setPoint(pathIndex);

                    direct.remove(direct.size()-1);
                }
            }else if(node.getWatch() == 1){//已经看过左子树
                node.setWatch(2);
                path.add(node.getRight());
                direct.add(0);
            }else if(node.getWatch() == 2){//去掉
                path.remove(path.size()-1);
                if(direct.size()>0) {
                    direct.remove(direct.size() - 1);
                }
            }
        }
    }


}
