/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.close;

import Share.ShareList;
import indicator.IndicatorField;
import indicator.IndicatorList;
import java.util.Date;
import trade.Trade;
import trade.TradeList;
import trade.filter.Filters;

/**
 *
 * @author Admin
 */
public class SignalCutOppClose extends AbstractClose 
{

    boolean filterOpen;
    
    public SignalCutOppClose()
    {
        this.paramCount=0;
    }
    
    public SignalCutOppClose(int buy)
    {
        this.buy=buy;
        this.paramCount=0;
    }

    public SignalCutOppClose(int buy,boolean filter)
    {
        this.buy=buy;
        this.paramCount=0;
        filterOpen=filter;
    }

    public TradeList fillClose(TradeList trdList, IndicatorList indList, ShareList sl, Filters filter) 
    {
        TradeList trdListNew=new TradeList();
        Trade trd=null,trdOld;
        for(int i=0;i<trdList.getSize();i++)
        {
            trdOld=trdList.getTrade(i);
            int startIndex=indList.isDatePresent(trdOld.getStartDate());
            int endIndex=indList.getSize()-1;
            if(filterOpen)
            {
                IndicatorField indf=indList.getSharedata(startIndex);
                if((buy==0 && indf.getValue()<indf.getSignal()) ||
                    (buy==1 && indf.getValue()>indf.getSignal()))
                continue;
            }
            //int endIndex=-1;
            if(startIndex>-1)
            {
                for(int k=startIndex+1;k<=endIndex;k++)
                {
                    IndicatorField indf=indList.getSharedata(k);
                    IndicatorField indfl=indList.getSharedata(k-1);
                    if((buy==0 && indf.getValue()<indf.getSignal() && indfl.getValue()>=indfl.getSignal()) ||
                            (buy==1 && indf.getValue()>indf.getSignal() && indfl.getValue()<=indfl.getSignal()))
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

    public int paramCount() {
        return 0;
    }
    
    @Override
	public String toString()
	{
            StringBuilder buffer = new StringBuilder();
            buffer.append("SignalCut Opp Close ");
            if(filterOpen)
                buffer.append(" Fileterd ");
            return buffer.toString();
	}

    /*public int compareTo(AbstractClose o) {
        if(!(o instanceof SignalCutClose) || o==null)
            return -1;
        else
        {
            return 0;
        }
    }*/

}
