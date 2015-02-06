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
public class GradeFilter extends BasicFilter {


    public GradeFilter() {
        super();
        params=new HashMap();
        params.put(1, 3);

    }

    public GradeFilter(HashMap params) {
        super(params);
    }

    @Override
    boolean isValid(Summary sum) {
        int val=(Integer)params.get(1);
        boolean b = (sum.getSg()==null)?true:sum.getSg().isGood(val);
        return b;
    }
}
