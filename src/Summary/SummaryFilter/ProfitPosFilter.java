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
public class ProfitPosFilter extends BasicFilter {

    public ProfitPosFilter() {
        super();
        params.put(1, -999.0);
        params.put(1, 999.0);
    }

    public ProfitPosFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {

        double grdStart=(Double)params.get(1);
        double grdEnd=(Double)params.get(2);
        double grd=sum.getFlatProfit()/100;
        boolean b=(grd>=grdStart) && (grd<=grdEnd);
        return b;
    }

}
