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
public class DaysCutSignalCutOppClose extends AbstractClose
{
    boolean filterOpen;
    private static final long serialVersionUID = 1324568L;

    public DaysCutSignalCutOppClose()
    {
        this.paramCount=1;
        filterOpen=false;
    }

    public DaysCutSignalCutOppClose(int buy)
    {
        this.buy=buy;
        this.paramCount=1;
        filterOpen=false;
    }

    public DaysCutSignalCutOppClose(HashMap param,int buy,boolean filter)
    {
        this.buy=buy;
        this.paramCount=1;
        filterOpen=filter;
        this.params=param;
    }

    public DaysCutSignalCutOppClose(HashMap param,int buy)
    {
        this.buy=buy;
        this.paramCount=1;
        this.params=param;
        filterOpen=false;
    }

    public TradeList fillClose(TradeList trdList, IndicatorList indList, ShareList sl, Filters filter)
    {
        TradeList trdListNew=new TradeList();
        Trade trd=null,trdOld;

        int days=0;
        if(params.get(1) instanceof Double)
            days=((Double)params.get(1)).intValue();
        else
            days=(Integer)params.get(1);
        for(int i=0;i<trdList.getSize();i++)
        {
            trdOld=trdList.getTrade(i);
            int startIndex=indList.isDatePresent(trdOld.getStartDate());
            int endIndex=indList.getSize()-1;
            //int endIndex=-1;
            if(startIndex+days<indList.getSize())
                endIndex=startIndex+days;
            //else
            //    continue;
            boolean close=false;
            if(filterOpen)
            {
                IndicatorField indf=indList.getSharedata(startIndex);
                if((buy==0 && indf.getValue()<indf.getSignal()) ||
                    (buy==1 && indf.getValue()>indf.getSignal()))
                continue;
            }

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
                       close=true;
                       break;
                    }
                }

                if(endIndex==startIndex+days)
                {
                    int ind=sl.isDatePresent(indList.getSharedata(endIndex).getDDate());
                    if(ind==-1)
                        ind=sl.isLowerDatePresent(indList.getSharedata(endIndex).getDDate());
                    endIndex=ind;
                }
                else if(endIndex==indList.getSize()-1 && !close)
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
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(1)).intValue();
            else
                days=(Integer)params.get(1);
            buffer.append(days);
            //buffer.append(" d Close2 ");
            if(filterOpen)
                buffer.append(" d Close16 ");
            else
                buffer.append(" d Close15 ");
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
