/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import indicator.BaseIndicator;
import indicator.MaIndicator;
import java.util.HashMap;
import report.Summary;
import trade.TradeParameters;

/**
 *
 * @author admin
 */
public class NameFilter extends BasicFilter {

    public NameFilter() {
        super();
        params.put(1, "");
    }

    public NameFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        String sysType=(String)params.get(1);
        boolean ret=false;
        if(sysType.isEmpty())
            ret=true;
        else if(sum.getName().replace(" ", "").equals(sysType.replace(" ", "")))
            ret=true;
        return ret;
    }
}