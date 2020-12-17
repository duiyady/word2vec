package com.duiya.model;


public class ExpTable {
    private int EXP_TABLE_SIZE = 1000;
    private int MAX_EXP = 6;
    private float[] expTable;

    public ExpTable() {
        init();
    }

    public ExpTable(int EXP_TABLE_SIZE, int MAX_EXP) {
        this.EXP_TABLE_SIZE = EXP_TABLE_SIZE;
        this.MAX_EXP = MAX_EXP;
        init();
    }

    private void init(){
        expTable = new float[this.EXP_TABLE_SIZE + 1];
        for (int i = 0; i < EXP_TABLE_SIZE; i++) {
            expTable[i] = (float)Math.exp((i / (float)EXP_TABLE_SIZE * 2 - 1) * MAX_EXP); // Precompute the exp() table
            expTable[i] = expTable[i] / (expTable[i] + 1);                   // Precompute f(x) = x / (x + 1)
        }
    }

    public float getExp(float value){
        int index = 0;
        if(value >= MAX_EXP){
            index = EXP_TABLE_SIZE-1;
        }else if(value <= -MAX_EXP){
            index = 0;
        }else {
            index = (int) ((value + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2));
        }
        return expTable[index];
    }
}
