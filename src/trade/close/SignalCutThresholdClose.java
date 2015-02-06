/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.close;

import Share.ShareList;
import indicator.IndicatorField;
import indicator.IndicatorList;
import java.util.Date;
import java.util.HashMap;
import trade.Trade;
import trade.TradeList;
import trade.filter.Filters;

/**
 *
 * @author Admin
 */
public class SignalCutThresholdClose extends AbstractClose
{

    public SignalCutThresholdClose()
    {
        this.paramCount=2;
    }

    public SignalCutThresholdClose(HashMap param, int buy)
    {
        this.buy=buy;
        this.params=param;
        this.paramCount=2;
    }

    public TradeList fillClose(TradeList trdList, IndicatorList indList, ShareList sl, Filters filter)
    {
        TradeList trdListNew=new TradeList();
        Trade trd=null,trdOld;
        double thresh=(Double)params.get(1);
        int cond =((Double)params.get(2)).intValue();
        for(int i=0;i<trdList.getSize();i++)
        {
            trdOld=trdList.getTrade(i);
            int startIndex=indList.isDatePresent(trdOld.getStartDate());
            int endIndex=indList.getSize()-1;

            if(startIndex>-1)
            {
                for(int k=startIndex+1;k<=endIndex;k++)
                {
                    IndicatorField indf=indList.getSharedata(k);
                    IndicatorField indfl=indList.getSharedata(k-1);
                    boolean valid=true;
                    switch(cond)
                    {
                        case 1:
                            if(indf.getValue()>thresh)
                                valid=true;
                            else
                                valid=false;
                            break;
                        case -1:
                            if(indf.getValue()<thresh)
                                valid=true;
                            else
                                valid=false;
                            break;
                    }
                    if(valid && ((buy==1 && indf.getValue()<indf.getSignal() && indfl.getValue()>=indfl.getSignal()) ||
                            (buy==0 && indf.getValue()>indf.getSignal() && indfl.getValue()<=indfl.getSignal())))
                    {
                       endIndex=sl.isDatePresent(indf.getDDate());
                       if(endIndex==-1)
                           endIndex=sl.isLowerDatePresent(indf.getDDate());
                       break;
                    }
                }
                if(endIndex==indList.getSize()-1)
                    continue;
                Date endDate=sl.getSharedata(endIndex).getDate();
                double sharePrice=sl.getSharedata(endIndex).getClosePrice();
                trd=new Trade(trdOld.getStartDate(),trdOld.getStartPrice(),endDate,sharePrice);
                if(filter==null || filter.filterTrade(endIndex,buy,sl))
                    trdListNew.addTrade(trd);
            }
        }
        return trdListNew;
    }

    @Override
	public String toString()
	{
            StringBuilder buffer = new StringBuilder();
            buffer.append("SignalCut ");
            int cond =((Double)params.get(2)).intValue();
            switch(cond)
            {
                case 1:
                    buffer.append("Above ");
                    break;
                case -1:
                    buffer.append("Below ");
                    break;
            }
            buffer.append(params.get(1));
            buffer.append(" Threshold Close ");
            return buffer.toString();
	}

    /*public int compareTo(AbstractClose o) {
        if(!(o instanceof SignalCutThresholdClose) || o==null)
            return -1;
        else
        {
            double pctO=(Double)o.params.get(1);
            double pct=(Double)params.get(1);
            return Double.compare(pct, pctO);
        }
    }*/

}
