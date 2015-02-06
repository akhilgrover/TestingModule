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
public class TradeLengthFilter extends BasicFilter {

    public TradeLengthFilter() {
        super();
        params.put(1, 0);
        params.put(2, 999);
    }
    
    public TradeLengthFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        double trdStart=(Double)params.get(1);
        double trdEnd=(Double)params.get(2);
        double tcount=sum.getAvgTradeLength();
        boolean b=(tcount>=trdStart && tcount<=trdEnd);
        return b;
    }

}
