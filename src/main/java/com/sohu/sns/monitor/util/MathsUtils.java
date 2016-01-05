package com.sohu.sns.monitor.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Gary on 2015/12/31.
 */
public class MathsUtils {

    public static List<Double> getStatus(List<Integer> nums) {
        if(null == nums || nums.size() <= 2) {
            return null;
        }
        double[] spanWeight =  new double[nums.size()-1];
        for(int i=0; i < nums.size()-1; i++) {
            spanWeight[i] = getWeight(nums, i+1)/getWeight(nums, i);
        }

        int firstIndex, secondIndex;
        if(spanWeight[0] >= spanWeight[1]) {
            firstIndex = 0;
            secondIndex = 1;
        } else {
            firstIndex = 1;
            secondIndex = 0;
        }
        for(int i=2; i<spanWeight.length; i++) {
            if(spanWeight[i] >= spanWeight[firstIndex]) {
                firstIndex = i;
                secondIndex = firstIndex;
            }else if(spanWeight[i] >= spanWeight[secondIndex]){
                secondIndex = i;
            }
        }
        double max = nums.get(firstIndex+1);
        double min = nums.get(firstIndex+1);
        double sum = 0.0;

        for(int i = firstIndex+1; i <= secondIndex; i++) {
            sum += nums.get(i);
            if(max <= nums.get(i)) {
                max = nums.get(i);
            }
            if(min > nums.get(i)) {
                min = nums.get(i);
            }
        }
        return Arrays.asList(max, min, sum/(Math.abs(secondIndex-firstIndex)-1));
    }

    private static double getSum(List<Integer> list, int pos) {
        double sum = 0;
        for(int i = 0; i <= pos; i++) {
            sum += list.get(i);
        }
        return sum;
    }

    private static double getWeight(List<Integer> list, int pos) {
        double sum = getSum(list, pos);
        return (sum + (list.size()-(pos+1))*list.get(pos))/(pos+1);
    }
}
