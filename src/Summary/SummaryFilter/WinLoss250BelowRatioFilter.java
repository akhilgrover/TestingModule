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
public class WinLoss250BelowRatioFilter extends BasicFilter {

    public WinLoss250BelowRatioFilter() {
        super();
        params.put(1, 0.9);
    }

    public WinLoss250BelowRatioFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {

        double grdStart=(Double)params.get(1);
        double grd=sum.getEMAFilterBelowWL();
        boolean b=(grd>=grdStart);
        return b;
    }

}
