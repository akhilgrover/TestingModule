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
public class DayCountFilter extends BasicFilter {

    public DayCountFilter() {
        super();
        params.put(1, 0);
        params.put(2, 100);
    }
    
    public DayCountFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        double dayStart=(Double)params.get(1);
        double dayEnd=(Double)params.get(2);
        double dCount=sum.getDayPercentage();
        boolean b=(dCount>=dayStart && dCount<=dayEnd);
        return b;
    }

}
