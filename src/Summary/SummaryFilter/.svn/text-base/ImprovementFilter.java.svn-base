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
public class ImprovementFilter extends BasicFilter {

    public ImprovementFilter() {
        super();
        params.put(1, 0);
    }

    /**
     *
     * @param params
     */
    public ImprovementFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        int imp=(Integer)params.get(1);
        String share=sum.getName();
        if(share.contains("Near Range"))
        {
            if(imp<sum.getImpScore())
                return true;
            else
                return false;
        }
        else
            return true;
    }

}
