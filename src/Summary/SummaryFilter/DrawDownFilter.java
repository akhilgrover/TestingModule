/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.lang3.ArrayUtils;
import report.Summary;

/**
 *
 * @author admin
 */
public class DrawDownFilter extends BasicFilter {

    public DrawDownFilter() {
        super();
        params.put(1, 30);
    }
    
    public DrawDownFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        double drawDown=(Double)params.get(1)*-1.0;
        double dYear=0,dtrade=0;
        if(sum.getDrawDown().size()>0)
        {
            dYear=(Double) Collections.min(Arrays.asList(ArrayUtils.toObject(sum.getDrawDown().toArray())));
        }
        if(sum.getDrawDownTrades().size()>0)
        {
            dtrade=(Double) Collections.min(Arrays.asList(ArrayUtils.toObject(sum.getDrawDownTrades().toArray())));
        }
        boolean ret=false;
        if(dYear>drawDown && dtrade>drawDown)
            ret=true;
        else
            ret=false;
        return ret;//dYear>drawDown && dtrade>drawDown;
    }

}
