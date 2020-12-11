package com.duiya.model;

import java.util.ArrayList;

public class VocabWordList {
    private ArrayList<VocabWord> vocabWordList;  // 词表
    private int[] hash_index;  // 词的哈希表，哈希值-->索引
    private int vocabHashSize = 30000000;  //哈希表大小
    private int minRudece = 2;

    public VocabWordList() {
        this.minRudece = 2;
        this.vocabHashSize = 30000000;
        this.hash_index = new int[this.vocabHashSize];
        this.vocabWordList = new ArrayList<>(1000);
        init();

    }

    public VocabWordList(int len, int vocabHashSize) {
        this.vocabWordList = new ArrayList<>(len);
        this.hash_index = new int[vocabHashSize];
        this.vocabHashSize = vocabHashSize;
        init();
    }

    public void init(){
        for(int i = 0; i < hash_index.length; i++){
            this.hash_index[i] = -1;
        }
    }

    public int add(String str){
        VocabWord word = new VocabWord();
        word.setCount(1);
        word.setWord(str);
        this.vocabWordList.add(word);
        int index = this.vocabWordList.size();
        int hash = this.getWordHash(str);
        while (this.hash_index[hash] != -1) {
            hash = (hash + 1) % this.vocabHashSize;// hash的碰撞检测
        }
        this.hash_index[hash] = index;// 词的hash值->词的词库中的索引
        return index;

    }

    public int searchVocab(String str){
        int hash = getWordHash(str);
        while (true) {
            if (this.hash_index[hash] == -1) {
                return -1;// 不存在该词
            }
            if (!str.equals(this.vocabWordList.get(hash_index[hash]).getWord())){
                return this.hash_index[hash];// 返回索引值
            }
            hash = (hash + 1) % this.vocabHashSize;
        }
    }

    public int getWordHash(String str) {
        int hash = str.hashCode();
        hash = hash % this.vocabHashSize;
        return hash;
    }

    public void reduceVocab(){
        int b = 0;
        for(int i = 0; i < this.vocabWordList.size(); i++){
            if (this.vocabWordList.get(i).getCount() > this.minRudece){
                this.vocabWordList.get(b).setWord(this.vocabWordList.get(i).getWord());
                this.vocabWordList.get(b).setCount(this.vocabWordList.get(i).getCount());
                b += 1;

            }
        }
        for(int i = b; i < this.vocabWordList.size(); i++){
            this.vocabWordList.remove(b);
        }

        for(int i = 0; i < this.vocabHashSize; i++){
            this.hash_index[i] = -1;
        }
        for(int i = 0; i < this.vocabWordList.size(); i++){
            int hash = getWordHash(this.vocabWordList.get(i).getWord());
            while (this.hash_index[hash] != -1){
                hash = (hash + 1) % this.vocabHashSize;
            }
            this.hash_index[hash] = i;
        }
        this.minRudece++;
    }
}
