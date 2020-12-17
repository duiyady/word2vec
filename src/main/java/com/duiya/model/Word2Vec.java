package com.duiya.model;

import com.duiya.utils.WordSplitUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Word2Vec {
    private boolean HAFUMAN = true;
    private boolean CBOW = true;
    private int EMBEDDING_SIZE = 128;
    private VocabList vocabList;
    private ExpTable expTable;
    private float learnRate = 0.00005f;
    private int negNum = 10;  //负采样个数

    public Word2Vec() {
        this.expTable = new ExpTable();
        String fileName = WordSplitUtil.class.getClassLoader().getResource("train.txt").getPath();
        this.vocabList = new VocabList();
        createVocab(fileName);
        this.vocabList.add("</default>");
        vocabList.sortVocab();

        if (this.HAFUMAN){ //哈夫曼
            vocabList.initEmbedding(this.EMBEDDING_SIZE);
            vocabList.initHiddenEmbedding(this.EMBEDDING_SIZE, 1);
            vocabList.createHuffmanTree();
        }else {// 负采样
            vocabList.initEmbedding(this.EMBEDDING_SIZE);
            vocabList.initHiddenEmbedding(this.EMBEDDING_SIZE, 0);
            vocabList.initNegativeSamplingTable();
        }


        if(this.CBOW){ // cbow

        }else { // skip-gram

        }
    }

    public static void main(String[] args) {
        Word2Vec model = new Word2Vec();

    }

    public float train(Integer center, ArrayList<Integer> neighbor){
        if(this.CBOW){// cbow
            float[] sumEmbedding = new float[this.EMBEDDING_SIZE];
            float loss = 0;
            for(int i = 0; i < neighbor.size(); i++){// 累加
                for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                    sumEmbedding[j] += (this.vocabList.getEmbedding()[neighbor.get(i)][j])/neighbor.size();
                }
            }

            if(this.HAFUMAN){//哈夫曼树
                List<Integer> path = this.vocabList.get(center).getPoint();  // 走的路径
                List<Integer> code = this.vocabList.get(center).getCode();  // 哈夫曼树走的方向
                float[][] hiddenGrad = new float[path.size()][this.EMBEDDING_SIZE];  // 映射层的梯度
                float[] embedGrad = new float[this.EMBEDDING_SIZE];  // 嵌入层的梯度

                for(int i = 0; i < path.size(); i++){
                    float[] hiddenEmbedding = this.vocabList.getHidden_embedding()[path.get(i)];
                    int direct = code.get(i);
                    float value = 0;
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        value += sumEmbedding[j]*hiddenEmbedding[j];
                    }
                    float expValue = this.expTable.getExp(value);
                    loss += (1 - direct)*Math.log(expValue) + direct*Math.log(1 - expValue);
                    float baseGrad = 1 - direct - expValue;
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        hiddenGrad[i][j] = baseGrad*sumEmbedding[j];
                        embedGrad[j] += (baseGrad*hiddenEmbedding[j])/neighbor.size();
                    }
                }

                // 嵌入层更新
                for(int i = 0; i < neighbor.size(); i++){
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        this.vocabList.getEmbedding()[i][j] += this.learnRate*embedGrad[j];
                    }
                }
                // 隐藏层更新
                for(int i = 0; i < path.size(); i++){
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        this.vocabList.getHidden_embedding()[path.get(i)][j] += this.learnRate*hiddenGrad[i][j];
                    }
                }
                return loss;
            }else { // 负采样
                List<Integer> path = new ArrayList<Integer>(this.negNum+1);
                List<Integer> code = new ArrayList<Integer>(this.negNum + 1);
                path.add(center);
                code.add(1);
                while (path.size() != this.negNum+1){
                    int next = this.vocabList.getNextNegativeSample();
                    if(next != center){
                        path.add(next);
                        code.add(0);
                    }
                }
                float[][] hiddenGrad = new float[path.size()][this.EMBEDDING_SIZE];  // 映射层的梯度
                float[] embedGrad = new float[this.EMBEDDING_SIZE];  // 嵌入层的梯度

                for(int i = 0; i < path.size(); i++){
                    float[] hiddenEmbedding = this.vocabList.getHidden_embedding()[path.get(i)];
                    int direct = code.get(i);
                    float value = 0;
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        value += sumEmbedding[j]*hiddenEmbedding[j];
                    }
                    float expValue = this.expTable.getExp(value);
                    loss += direct*Math.log(expValue) + (1 - direct)*Math.log(1 - expValue);
                    float baseGrad = direct - expValue;
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        hiddenGrad[i][j] = baseGrad*sumEmbedding[j];
                        embedGrad[j] += (baseGrad*hiddenEmbedding[j])/neighbor.size();
                    }
                }

                // 嵌入层更新
                for(int i = 0; i < neighbor.size(); i++){
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        this.vocabList.getEmbedding()[i][j] += this.learnRate*embedGrad[j];
                    }
                }
                // 隐藏层更新
                for(int i = 0; i < path.size(); i++){
                    for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                        this.vocabList.getHidden_embedding()[path.get(i)][j] += this.learnRate*hiddenGrad[i][j];
                    }
                }
                return loss;
            }
        }else {// skip-gram
            float[] centerEmbedding = new float[this.EMBEDDING_SIZE];
            float loss = 0;
            for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                centerEmbedding[j] += this.vocabList.getEmbedding()[center][j];
            }
            if(this.HAFUMAN){//哈夫曼树
                List<List<Integer>> neighborsPath= new ArrayList<List<Integer>>();
                List<List<Integer>> neighborsCode = new ArrayList<List<Integer>>();
                int allHiddenCount = 0;
                for(int i = 0; i < neighbor.size(); i++){
                    neighborsPath.add(this.vocabList.get(neighbor.get(i)).getPoint());
                    neighborsCode.add(this.vocabList.get(neighbor.get(i)).getCode());
                    allHiddenCount += this.vocabList.get(neighbor.get(i)).getPoint().size();
                }

                float[][] hiddenGrad = new float[allHiddenCount][this.EMBEDDING_SIZE];  // 映射层的梯度
                float[] embedGrad = new float[this.EMBEDDING_SIZE];  // 嵌入层的梯度
                int nowHiddenIndex = 0;
                for(int i = 0; i < neighbor.size(); i++){
                    for(int j = 0; j < neighborsPath.get(i).size(); j++){
                        float[] hiddenEmbed = this.vocabList.getHidden_embedding()[neighborsPath.get(i).get(j)];
                        int direct = neighborsCode.get(i).get(j);
                        float value = 0.0f;
                        for(int k = 0; k < this.EMBEDDING_SIZE; k++){
                            value += centerEmbedding[k]*hiddenEmbed[k];
                        }
                        float expValue = this.expTable.getExp(value);
                        loss += (1-direct)*Math.log(expValue) + direct*Math.log(1 - expValue);
                        float baseGrad = 1 - direct - expValue;
                        for(int k = 0; k < this.EMBEDDING_SIZE; k++){
                            hiddenGrad[nowHiddenIndex][k] = baseGrad*centerEmbedding[k];
                            embedGrad[j] = baseGrad*hiddenEmbed[k];
                        }
                        nowHiddenIndex++;
                    }
                }

                // 嵌入层更新
                for(int i = 0; i < this.EMBEDDING_SIZE; i++){
                    this.vocabList.getEmbedding()[center][i] += this.learnRate*embedGrad[i];
                }

                // 映射层更新
                nowHiddenIndex = 0;
                for(int i = 0; i < neighbor.size(); i++){
                    for(int j = 0; j < neighborsPath.get(i).size(); j++){
                        for(int k = 0; k < this.EMBEDDING_SIZE; k++){
                            this.vocabList.getHidden_embedding()[neighborsPath.get(i).get(j)][k] += this.learnRate*hiddenGrad[nowHiddenIndex][k];
                        }
                        nowHiddenIndex++;
                    }
                }
                return loss;
            }else {  //负采样
                List<List<Integer>> neighborsPath= new ArrayList<List<Integer>>();
                List<List<Integer>> neighborsCode = new ArrayList<List<Integer>>();
                int allHiddenCount = 0;
                for(int i = 0; i < neighbor.size(); i++){
                    List<Integer> path = new ArrayList<Integer>();
                    List<Integer> code = new ArrayList<Integer>();
                    path.add(neighbor.get(i));
                    code.add(1);
                    while (path.size() != this.negNum+1){
                        int next = this.vocabList.getNextNegativeSample();
                        if(next != neighbor.get(i)){
                            path.add(next);
                            code.add(0);
                        }
                    }
                    neighborsPath.add(path);
                    neighborsCode.add(code);
                    allHiddenCount += path.size();
                }

                float[][] hiddenGrad = new float[allHiddenCount][this.EMBEDDING_SIZE];  // 映射层的梯度
                float[] embedGrad = new float[this.EMBEDDING_SIZE];  // 嵌入层的梯度

                int nowHiddenIndex = 0;
                for(int i = 0; i < neighbor.size(); i++){
                    for(int j = 0; j < neighborsPath.get(i).size(); j++){
                        float[] hiddenEmbed = this.vocabList.getHidden_embedding()[neighborsPath.get(i).get(j)];
                        int direct = neighborsCode.get(i).get(j);
                        float value = 0.0f;
                        for(int k = 0; k < this.EMBEDDING_SIZE; k++){
                            value += centerEmbedding[k]*hiddenEmbed[k];
                        }
                        float expValue = this.expTable.getExp(value);
                        loss += direct*Math.log(expValue) + (1 - direct)*Math.log(1 - expValue);
                        float baseGrad = direct - expValue;
                        for(int k = 0; k < this.EMBEDDING_SIZE; k++){
                            hiddenGrad[nowHiddenIndex][k] = baseGrad*centerEmbedding[k];
                            embedGrad[j] = baseGrad*hiddenEmbed[k];
                        }
                        nowHiddenIndex++;
                    }
                }

                // 嵌入层更新
                for(int i = 0; i < this.EMBEDDING_SIZE; i++){
                    this.vocabList.getEmbedding()[center][i] += this.learnRate*embedGrad[i];
                }

                // 映射层更新
                nowHiddenIndex = 0;
                for(int i = 0; i < neighbor.size(); i++){
                    for(int j = 0; j < neighborsPath.get(i).size(); j++){
                        for(int k = 0; k < this.EMBEDDING_SIZE; k++){
                            this.vocabList.getHidden_embedding()[neighborsPath.get(i).get(j)][k] += this.learnRate*hiddenGrad[nowHiddenIndex][k];
                        }
                        nowHiddenIndex++;
                    }
                }
                return loss;
            }
        }
    }

    public void createVocab(String path){
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
                    this.vocabList.add(str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
