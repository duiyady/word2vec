package com.duiya.model;

import java.util.List;
import java.util.Objects;

public class VocabWord {
    private long count; // 出现的次数
    private List<Integer> point;  //路径
    private String word;  //词
    private List<Integer> code; //哈夫曼编码
    private int codelen;  //编码长度

    public VocabWord() {
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<Integer> getPoint() {
        return point;
    }

    public void setPoint(List<Integer> point) {
        this.point = point;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<Integer> getCode() {
        return code;
    }

    public void setCode(List<Integer> code) {
        this.code = code;
    }

    public int getCodelen() {
        return codelen;
    }

    public void setCodelen(int codelen) {
        this.codelen = codelen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VocabWord vocabWord = (VocabWord) o;
        return word == vocabWord.word;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, point, word, code, codelen);
    }
}
