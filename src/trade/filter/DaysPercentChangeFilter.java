package trade.filter;

import Share.ShareList;
import indicator.IndicatorList;
import indicator.RsiIndicator;
import ma.*;
import java.util.*;

public class DaysPercentChangeFilter extends AbstractFilter
{
    private static final long serialVersionUID = -4441095523212287781L;

    public DaysPercentChangeFilter()
    {
        this.paramCount=2;
    }

    public DaysPercentChangeFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=2;
    }

    public void buildFilter(int buy, ShareList sl)
    {
        return;
    }

    public boolean filterTrade(int shareIndex, int buy, ShareList slst)
    {
        int period=(Integer)params.get(1);
        int threshold=(Integer)params.get(2);
        boolean ret=false;
        double curPrice=slst.getSharedata(shareIndex).getClosePrice();
        double prevPrice=slst.getSharedata(shareIndex-period).getClosePrice();
        double pchange=((curPrice-prevPrice)*100)/prevPrice;
        if(buy==1 && pchange<threshold)
        {
            ret=true;
        }
        else if(buy==0 && pchange>threshold)
        {
            ret=true;
        }
        return ret;
    }

    @Override
	public String toString()
	{
            int period=(Integer)params.get(1);
            int threshold=(Integer)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append("d %change ");
            buffer.append(threshold);
            buffer.append(" Threshold Filter ");
            return buffer.toString();
	}
}
