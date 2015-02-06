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
public class ProfitPTradeFilter extends BasicFilter {

    private double trdStart;

    public ProfitPTradeFilter() {
        super();
        params.put(1, 100);
        trdStart = (Double) params.get(1);
    }

    public ProfitPTradeFilter(HashMap params) {
        super(params);
        trdStart = (Double) params.get(1);
    }

    @Override
    boolean isValid(Summary sum) {
        double tPrft = sum.getTProfit() / sum.getTCount();
        boolean b = (tPrft >= trdStart);
        return b;
    }
}
