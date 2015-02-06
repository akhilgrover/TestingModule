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
public class YearsPositiveFilter extends BasicFilter {

    @SuppressWarnings("unchecked")
    public YearsPositiveFilter() {
        super();
        params.put(1, 3);
        params.put(2, 15);
        params.put(3, 2);
        params.put(4, 15);
    }

    public YearsPositiveFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        int yrsPos=(Integer)params.get(1);
        int yrsProf=(Integer)params.get(2);
        int yrsLPos=(Integer)params.get(3);
        int yrsLProf=(Integer)params.get(4);
        int pos=sum.getPosGradePerc();
        double prof=sum.getYearlyGradePerc();
        int Lpos=sum.getLimitGradePerc();
        double Lprof=sum.getLimitYearlyGradePerc();
        boolean b=(pos>=yrsPos && prof>=yrsProf) && (Lpos>=yrsLPos && Lprof>=yrsLProf);
        return b;
    }
}