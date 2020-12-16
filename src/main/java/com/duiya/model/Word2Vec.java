package com.duiya.model;

import com.duiya.utils.WordSplitUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Word2Vec {
    private static boolean HAFUMAN = true;
    private static boolean CBOW = true;
    private static int EMBEDDING_SIZE = 128;

    public static void main(String[] args) {
        String fileName = WordSplitUtil.class.getClassLoader().getResource("train.txt").getPath();
        VocabList vocabList = new VocabList();
        Word2Vec.createVocab(fileName, vocabList);
        vocabList.add("</default>");
        vocabList.sortVocab();

        if (Word2Vec.HAFUMAN){ //哈夫曼
            vocabList.initEmbedding(Word2Vec.EMBEDDING_SIZE);
            vocabList.initHiddenEmbedding(Word2Vec.EMBEDDING_SIZE, 1);
            vocabList.createHuffmanTree();
        }else {// 负采样
            vocabList.initEmbedding(Word2Vec.EMBEDDING_SIZE);
            vocabList.initHiddenEmbedding(Word2Vec.EMBEDDING_SIZE, 0);
            vocabList.initNegativeSamplingTable();
        }


        if(CBOW){ // cbow

        }else { // skip-gram

        }

    }

    public static void train(Integer center, ArrayList<Integer> neighbor){
        if(Word2Vec.CBOW){// cbow
            if(Word2Vec.HAFUMAN){//哈夫曼树
                
            }else {

            }
        }else {// skip-gram
            if(Word2Vec.HAFUMAN){//哈夫曼树

            }else {

            }
        }

    }

    public static void createVocab(String path, VocabList vocabList){
        String fileName = WordSplitUtil.class.getClassLoader().getResource("train.txt").getPath();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            File file = new File(fileName);
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

            String line = "";
            int nowID = 0;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "");
                List<String> strings = jiebaSegmenter.sentenceProcess(line);
                strings.add(0, "</s>");
                for (String str : strings) {
                    vocabList.add(str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
