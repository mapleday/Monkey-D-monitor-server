package com.sohu.sns.monitor.util;

import com.sohu.sns.monitor.model.ExceptionValue;

import java.util.List;

/**
 * Created by Gary on 2015/12/31.
 */
public class MathsUtils {

    public static ExceptionValue getStatus(List<Integer> nums) {
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
        if(Math.abs(secondIndex-firstIndex)-1<= 0) {
            return null;
        }
        Integer begin, end;
        if(firstIndex < secondIndex) {
            begin = firstIndex;
            end = secondIndex;
        } else {
            begin = secondIndex;
            end = firstIndex;
        }
        Integer max = nums.get(begin+1);
        Integer min = nums.get(begin+1);
        Integer sum = 0;

        for(int i = begin+1; i <= end; i++) {
            sum += nums.get(i);
            if(max <= nums.get(i)) {
                max = nums.get(i);
            }
            if(min > nums.get(i)) {
                min = nums.get(i);
            }
        }
        ExceptionValue exceptionValue = new ExceptionValue();
        exceptionValue.setMaxVisitCount(max);
        exceptionValue.setMinVisitCount(min);
        exceptionValue.setAvgVisitCount(sum/(Math.abs(secondIndex-firstIndex)));
        return exceptionValue;
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
