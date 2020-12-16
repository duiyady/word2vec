package com.duiya.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordSplitUtil {

    public static List<List<String>> readFile(String path){
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        List<List<String>> list = new ArrayList<List<String>>();
        try {
            File file = new File(path);
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

            String line = "";
            int nowID = 0;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "");
                List<String> strings = jiebaSegmenter.sentenceProcess(line);
                strings.add(0, "</s>");
                list.add(strings);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        return list;
    }

    public static void main(String[] args) {
        String fileName = WordSplitUtil.class.getClassLoader().getResource("train.txt").getPath();
        WordSplitUtil.readFile(fileName);
    }
}
