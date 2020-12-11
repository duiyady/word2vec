package com.duiya.model;

import java.nio.file.attribute.UserPrincipalLookupService;

public class ExpTable {
    private int EXP_TABLE_SIZE = 1000;
    private int MAX_EXP = 6;
    private float[] expTable;

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


}
