/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SummaryFilter;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import report.Summary;

/**
 *
 * @author admin
 */
public class MinYearProfitFilter extends BasicFilter {

    public MinYearProfitFilter() {
        super();
        params.put(1, -25);
    }

    @Override
    boolean isValid(Summary sum) {
        double currMin=getMin(sum.getPProfit());
        double allowdMin=(Double)params.get(1);
        return currMin>allowdMin;
    }

    private double getMin(TDoubleArrayList pProfit)
    {
        double min=(Double)pProfit.get(0);
        for(int i=1;i<pProfit.size();i++)
        {
            if((Double)pProfit.get(i)<min)
                min=(Double)pProfit.get(i);
        }
        return min;
    }

}
