/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.open;

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
public abstract class AbstractOpen implements Open, Serializable//,Comparable<AbstractOpen>
{

    HashMap params;
    int buy;
    int paramCount;
    protected transient String name;
    private static final long serialVersionUID = 7526472295622776147L;

    public abstract TradeList fillOpen(IndicatorList indList, ShareList sl, Filters filter);

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
