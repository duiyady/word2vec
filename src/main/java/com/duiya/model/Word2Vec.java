package com.duiya.model;

import com.duiya.utils.WordSplitUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Word2Vec {
    private boolean HAFUMAN = false;
    private boolean CBOW = true;
    private int EMBEDDING_SIZE = 128;
    private VocabList vocabList;
    private ExpTable expTable;
    private float learnRate = 0.0005f;
    private int negNum = 10;  //负采样个数
    private int window = 2;
    private int DEFAULT_INDEX; // 默认

    public Word2Vec() {
        this.expTable = new ExpTable();
        this.vocabList = new VocabList();
        this.vocabList.add("</default>");
        vocabList.sortVocab();
        DEFAULT_INDEX = this.vocabList.searchVocab("</default>");
    }

    public void init(){
        if (this.HAFUMAN){ //哈夫曼
            vocabList.initEmbedding(this.EMBEDDING_SIZE);
            vocabList.initHiddenEmbedding(this.EMBEDDING_SIZE, 1);
            vocabList.createHuffmanTree();
        }else {// 负采样
            vocabList.initEmbedding(this.EMBEDDING_SIZE);
            vocabList.initHiddenEmbedding(this.EMBEDDING_SIZE, 0);
            vocabList.initNegativeSamplingTable();
        }
    }

    public static void main(String[] args) {
        Word2Vec model = new Word2Vec();
        String fileName = WordSplitUtil.class.getClassLoader().getResource("train.txt").getPath();
        model.createVocab(fileName);
        model.init();
        model.train(fileName, 99999);
        List<String> tt = model.getMaxRelation("唐朝", 10);
        System.out.println(tt);
        tt = model.getMaxRelation("韩信", 10);
        System.out.println(tt);
        tt = model.getMaxRelation("曹操", 10);
        System.out.println(tt);




    }

    public List<String> getMaxRelation(String word, int top){
        List<String> words = new ArrayList<String>(top);
        List<Float> value = new ArrayList<Float>();

        int centerIndex = this.vocabList.searchVocab(word);
        if(centerIndex == -1){
            return null;
        }
        float[] center = this.vocabList.getEmbedding()[centerIndex];
        for(int i = 0; i < this.vocabList.size(); i++){
            if(i != centerIndex){
                float tmp = 0;
                float[] nei = this.vocabList.getEmbedding()[i];
                for(int j = 0; j < this.EMBEDDING_SIZE; j++){
                    tmp += center[j]*nei[j];
                }
                if(value.size() == 0){
                    value.add(tmp);
                    words.add(this.vocabList.get(i).getWord());
                }else {
                    boolean flag = false;
                    int m = 0;
                    for(; m < value.size(); m++){
                        if(tmp > value.get(m)){
                            flag = true;
                            break;
                        }
                    }
                    if(flag == false){
                        value.add(tmp);
                        words.add(this.vocabList.get(i).getWord());
                    }else {
                        value.add(m, tmp);
                        words.add(m, this.vocabList.get(i).getWord());
                    }
                    while (value.size() > top){
                        value.remove(value.size()-1);
                        words.remove(words.size()-1);
                    }
                }
            }
        }
        return words;
    }

    public void train(String fileName, int epoch){
        JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();
        int noWord = this.vocabList.searchVocab("</default>");
        float loss = 0;
        int count = 1;
        for(int epo = 0; epo < epoch; epo++){
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            try {
                File file = new File(fileName);
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "");
                    List<String> strings = jiebaSegmenter.sentenceProcess(line);
                    strings.add(0, "</s>");
                    for(int i = 1; i < strings.size(); i++){
                        int center = this.vocabList.searchVocab(strings.get(i));
                        if (center == -1){
                            continue;
                        }
                        List<Integer> neighbor = new ArrayList<Integer>();
                        int start = Math.max(i - this.window, 0);
                        int end = Math.min(i + this.window, strings.size()-1);
                        if(end == strings.size()-1){
                            start = Math.max(strings.size() - 4, 0);
                        }
                        if(start != end){
                            for(int j = start; j <= end; j++){
                                if(j != i) {
                                    int index = this.vocabList.searchVocab(strings.get(j));
                                    if (index == -1) {
                                        index = noWord;
                                    }
                                    neighbor.add(index);
                                }
                            }
                            loss += this.train(center, neighbor);
                            count += 1;
                            if(count%1000 == 0){
                                System.out.println(loss/count);
                                loss = 0;
                                count = 1;
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }


    public float train(Integer center, List<Integer> neighbor){
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

    public void createVocab(String fileName){
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            File file = new File(fileName);
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

            String line;
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
