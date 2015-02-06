/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.close;

import Share.ShareList;
import indicator.IndicatorList;
import indicator.RsiIndicator;
import java.util.HashMap;
import trade.Trade;

/**
 *
 * @author admin
 */
public class RsiThresholdSkipClose extends AbstractIndividualClose {

    RsiIndicator rsi;
    
    public RsiThresholdSkipClose(HashMap<Integer,Number> param) {
        super(param);
        rsi=new RsiIndicator();
        HashMap hm=new HashMap();
        hm.put(1, param.get(1));
        hm.put(2, 1);
        rsi.init(param);
    }
    
  
    public Trade closeTrade(Trade trd, ShareList sl, boolean sell) {
        IndicatorList il=rsi.buildIndicator(sl);
        return trd;
    }

}
