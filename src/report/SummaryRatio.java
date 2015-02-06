/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import java.text.DecimalFormat;

/**
 *
 * @author gnisoft
 */
public class SummaryRatio {
    private static final long serialVersionUID = 1L;
    private double ratio;
    private Summary sum;

    public SummaryRatio(double ratio, Summary sum) {
        this.ratio = ratio;
        this.sum = sum;
    }

    public void setSum(Summary sum) {
        this.sum = sum;
    }

    public Summary getSum() {
        return sum;
    }

    /**
     * @return the ratio
     */
    public double getRatio() {
        return ratio;
    }

    /**
     * @param ratio the ratio to set
     */
    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public String getName(){
        return sum.getName() + " " + new DecimalFormat("0.00").format(ratio);
    }

    @Override
    public String toString() {
        return getName();
    }


}
