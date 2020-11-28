package com.duiya.model;

public class Word2Vec {
    private boolean CBOW = true;
    private boolean HAFUMAN_TREE = false;
    private int MAX_STRING = 100;
    private int EXP_TABLE_SIZE = 1000;
    private int MAX_EXP = 6;
    private int MAX_SENTENCE_LENGTH = 1000;
    private int MAX_CODE_LENGTH = 40;
    private int window, min_count, min_reduce;
    private long vocab_max_size;
    private long vocab_size;
    private int vacab_hash_size = 30000000;


    public static void main(String[] args) {
        System.out.println("hello world");
    }
}
