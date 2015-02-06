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
public class GraphGradeFilter extends BasicFilter {

    public GraphGradeFilter() {
        super();
        params.put(1, 10);
        params.put(2, 14);
    }
    
    public GraphGradeFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
                
        int grdStart=(Integer)params.get(1);
        int grdEnd=(Integer)params.get(2);
        int grd=sum.getYearlyGrade();
        boolean b=(grd>=grdStart && grd<=grdEnd);
        return b;
    }

}
