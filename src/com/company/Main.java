package com.company;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        long max = -99;
        long[] intArray = new long[n];
        long[] amt = new long[m];
        int[] e1 = new int[m];
        int[] e2 = new int[m];

        for(int i=0;i<m;i++){
            e1[i] = in.nextInt();
            e2[i] = in.nextInt();
            amt[i] = in.nextLong();
        }

        long startTime = System.currentTimeMillis();
        for(int i=0;i<m;i++){

            if(amt[i]==(long)0)
                continue;
            for(int j=e1[i]-1;j<e2[i];j++)
            {
                intArray[j] += (long)amt[i];
                if(max < intArray[j])
                    max = intArray[j];
            }
        }
        System.out.println(max);



        in.close();
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println(totalTime);
    }
}
