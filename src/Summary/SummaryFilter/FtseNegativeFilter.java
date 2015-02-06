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
public class FtseNegativeFilter extends BasicFilter {

    public FtseNegativeFilter() {
        super();
        params.put(1, 3);
    }

    public FtseNegativeFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        int ftseNeg=(Integer)params.get(1);
        int dCount=sum.getFlatGrade();
        boolean b=dCount>=ftseNeg;
        return b;
    }

}
