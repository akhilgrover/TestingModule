/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import indicator.BaseIndicator;
import indicator.MaIndicator;
import indicator.RsiIndicator;
import java.util.HashMap;
import report.Summary;
import trade.TradeParameters;

/**
 *
 * @author admin
 */
public class IndexFilter extends BasicFilter {

    public IndexFilter() {
        super();
        params.put(1, 1);
    }

    public IndexFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        String sysType=params.get(1).toString();
        boolean valid=sum.getIndex().equals(sysType);
        return valid;
    }
}