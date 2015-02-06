/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.close;

import Share.ShareList;
import indicator.IndicatorList;
import java.io.Serializable;
import java.util.HashMap;
import trade.TradeList;
import trade.filter.Filters;

/**
 *
 * @author Admin
 */
public abstract class AbstractClose implements Close ,Serializable //, Comparable<AbstractClose>
{
    HashMap params;
    int buy;
    int paramCount;
    private static final long serialVersionUID = 7526472295622776147L;
    
    /**
     * 
     * @param trdList 
     *  Trade list with open trades only to fill closes
     * @param indList
     *  Indicator List to calculate the closes
     * @param sl
     *  Sharelist for rference and to get the close price 
     * @param filter
     *  filter the close or can be null for no filter
     * @return
     *  returns the complete trade list with open and closed trades.
     */
    public abstract TradeList fillClose(TradeList trdList, IndicatorList indList, ShareList sl, Filters filter);

    public int getParamCount(){
        return paramCount;
    }
    
    public HashMap getParams()
    {
        return params;
    }
    
    public int getBuy()
    {
        return buy;
    }
    
    public void setParams(HashMap param)
    {
        this.params=param;
    }
    
    public void setBuy(int buy)
    {
        this.buy=buy;
    }
    
    

}
