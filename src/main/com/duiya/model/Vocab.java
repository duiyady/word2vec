package com.duiya.model;

import java.util.List;
import java.util.Objects;

public class Vocab implements Comparable{
    private int count; // 出现的次数
    private List<Integer> point;  //路径
    private String word;  //词
    private List<Integer> code; //哈夫曼编码
    private int codelen;  //编码长度

    public Vocab() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
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

    public void countAdd(){
        this.count++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vocab vocab = (Vocab) o;
        return word == vocab.word;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, point, word, code, codelen);
    }

    @Override
    public int compareTo(Object o) {
        Vocab v = (Vocab)o;
        if(this.count > v.count){
            return 1;
        }else if(this.count < v.count){
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Vocab{" +
                "count=" + count +
                ", word='" + word + '\'' +
                '}';
    }
}
