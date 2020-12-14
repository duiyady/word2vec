package com.duiya.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VocabTest {

    public static void main(String[] args) {
        List<String> list = Arrays.asList("你", "好", "删", "掉", "发", "噶", "啥", "地", "方", "去", "安", "慰", "她","发", "送", "到", "干", "撒", "岗", "位", "的",
                "吃", "饭", "郭", "德", "纲", "水", "电", "费", "水", "电", "费", "算", "法", "算", "法", "施" ,"工", "方", "潍", "坊", "水", "电", "费", "个", "人", "是",
                "大", "股", "东", "税", "控", "盘", "弄", "好", "急", "吼", "吼", "复", "活", "甲", "受", "到", "分", "解" );
//        List<String> list = Arrays.asList("你","你","你","你","你");

        VocabList vocabList = new VocabList();

        vocabList.add("</s>");
        for(String str: list){
            vocabList.add(str);
        }
        vocabList.sortVocab();

        vocabList.createHuffmanTree();
        for(int i = 0; i < vocabList.size(); i++){
            Vocab vocab = vocabList.get(i);
            System.out.println("词：" + vocab.getWord() + " 路径：" + vocab.getPoint() + " 方向:" + vocab.getCode());
        }


    }
}
