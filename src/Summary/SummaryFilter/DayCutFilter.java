/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import java.util.HashMap;
import report.Summary;
import trade.close.Close;

/**
 *
 * @author admin
 */
public class DayCutFilter extends BasicFilter {

    public DayCutFilter() {
        super();
        params.put(1, 0);
        params.put(2, 100);
    }

    public DayCutFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        double dayStart=(Double)params.get(1);
        double dayEnd=(Double)params.get(2);
        Close cls=sum.getClose();
        double dCount=(Double)cls.getParams().get(cls.getParamCount());
        boolean b=(dCount>=dayStart && dCount<=dayEnd);
        return b;
    }

}
