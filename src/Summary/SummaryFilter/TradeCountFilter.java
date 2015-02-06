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
public class TradeCountFilter extends BasicFilter {

    private int trdStart;
    private int trdEnd;

    public TradeCountFilter() {
        super();
        params.put(1, 0);
        params.put(2, 999);
        trdStart=0;
        trdEnd=999;
    }

    public TradeCountFilter(HashMap params) {
        super(params);
        trdStart=(Integer)params.get(1);
        trdEnd=(Integer)params.get(2);
    }


    @Override
    boolean isValid(Summary sum) {
        int tcount=sum.getTCount();
        boolean b=(tcount>=trdStart && tcount<=trdEnd);
        return b;
    }

}
