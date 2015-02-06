/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.lang3.ArrayUtils;
import report.Summary;

/**
 *
 * @author admin
 */
public class MinYearProfitFilter extends BasicFilter {

    public MinYearProfitFilter() {
        super();
        params.put(1, -25.0);
    }

    public MinYearProfitFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        //double currMin=getMin(sum.getPProfit());
        double currMin=Collections.min(Arrays.asList(ArrayUtils.toObject(sum.getPProfit().toArray())));
        double allowdMin=(Double)params.get(1);
        return currMin>allowdMin;
    }

    private double getMin(ArrayList pProfit)
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
