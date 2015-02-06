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
public class GradeNegFilter extends BasicFilter {

    public GradeNegFilter() {
        super();
        params.put(1, 0.0);
        params.put(1, 999.0);
    }

    public GradeNegFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {

        double grdStart=(Double)params.get(1);
        double grdEnd=(Double)params.get(2);
        double grd=sum.getPosGradePerc();
        boolean b=(grd>=grdStart) && (grd<=grdEnd);
        return b;
    }

}
