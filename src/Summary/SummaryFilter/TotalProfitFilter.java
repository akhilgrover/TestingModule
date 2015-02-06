/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import java.util.HashMap;
import report.Summary;

/**
 *
 * @author admin
 */
public class TotalProfitFilter extends BasicFilter {

    private double trdStart;
    private double trdEnd;

    public TotalProfitFilter() {
        super();
        params.put(1, 0);
        params.put(2, 999);
        trdStart=(Double)params.get(1);
        trdEnd=(Double)params.get(2);
    }

    public TotalProfitFilter(HashMap params) {
        super(params);
        trdStart=(Double)params.get(1);
        trdEnd=(Double)params.get(2);
    }


    @Override
    boolean isValid(Summary sum) {
        double tPrft=sum.getTProfit();
        boolean b=(tPrft>=trdStart && tPrft<=trdEnd);
        return b;
    }

}
