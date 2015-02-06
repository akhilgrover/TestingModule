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
public class WinLossRatioFilter extends BasicFilter {

    @SuppressWarnings("unchecked")
    public WinLossRatioFilter() {
        super();
        params.put(1, 0.9);
    }

    public WinLossRatioFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {

        double grdStart=(Double)params.get(1);
        double grd=sum.getGainTrades() * 1.0 / sum.getLossTradeCount();
        boolean b=(grd>=grdStart);
        return b;
    }

}
