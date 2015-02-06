/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Utils;

import indicator.IndicatorField;
import java.util.TreeSet;

/**
 *
 * @author akhil
 */
public class Fibonacci {
    
    public static TreeSet<Double> fibonacciRetracement(double min,double max){
        TreeSet<Double> ret=new TreeSet<>();
        double diff=max-min;
        double s382=max-(diff*0.382);
        double s50=max-(diff*0.5);
        double s618=max-(diff*0.618);
        ret.add(s382);
        ret.add(s50);
        ret.add(s618);
        return ret;
    }
    
    public static TreeSet<Double> fibonacciRetracement(IndicatorField indf){
        TreeSet<Double> ret=new TreeSet<>();
        double diff = indf.getValue() - indf.getSignal();
        double s382 = indf.getValue() - (diff * 0.382);
        double s50 = indf.getValue() - (diff * 0.5);
        double s618 = indf.getValue() - (diff * 0.618);
        ret.add(s382);
        ret.add(s50);
        ret.add(s618);
        return ret;
    }
    
    public static TreeSet<Double> fibonacciProjection(double min,double max){
        TreeSet<Double> ret=new TreeSet<>();
        double diff=max-min;
        double s618 = max + (diff * 0.618);
        double s100 = max + (diff * 1.0);
        double s1382 = max + (diff * 1.382);
        double s1618 = max + (diff * 0.618);
        ret.add(s618);
        ret.add(s100);
        ret.add(s1382);
        ret.add(s1618);
        return ret;
    }
    
    public static TreeSet<Double> fibonacciProjection(IndicatorField indf){
        TreeSet<Double> ret=new TreeSet<>();
        double diff = indf.getValue() + indf.getSignal();
        double s618 = indf.getValue() + (diff * 0.618);
        double s100 = indf.getValue() + (diff * 1.0);
        double s1382 = indf.getValue() + (diff * 1.382);
        double s1618 = indf.getValue() + (diff * 0.618);
        ret.add(s618);
        ret.add(s100);
        ret.add(s1382);
        ret.add(s1618);
        return ret;
    }
    
}
