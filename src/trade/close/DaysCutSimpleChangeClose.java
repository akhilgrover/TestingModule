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
public class DaysCutSimpleChangeClose extends AbstractClose
{
    /**
     * 
     */
    public DaysCutSimpleChangeClose()
    {
        this.paramCount=2;
    }
    
    /**
     * 
     * @param param
     * @param buy
     */
    public DaysCutSimpleChangeClose(HashMap param, int buy)
    {
        this.buy=buy;
        this.params=param;
        this.paramCount=2;
    }
    
    
    public TradeList fillClose(TradeList trdList, IndicatorList indList,
            ShareList sl, Filters filter) 
    {
        int days=0;
        if(params.get(1) instanceof Double)
            days=((Double)params.get(1)).intValue();
        else
            days=(Integer)params.get(1);
        double pct=0;
        if(params.get(2) instanceof Double)
            pct=(Double)params.get(2);
        else
            pct=((Integer)params.get(2))*1.0;
        TradeList trdListNew=new TradeList();
        Trade trd=null,trdOld;
        for(int i=0;i<trdList.getSize();i++)
        {
            trdOld=trdList.getTrade(i);
            int startIndex=indList.isDatePresent(trdOld.getStartDate());
            int endIndex=indList.getSize()-1;
            if(startIndex+days<indList.getSize())
                endIndex=startIndex+days;
            double high=indList.getSharedata(startIndex).getValue();
            double low=indList.getSharedata(startIndex).getValue();
            if(startIndex>-1)
            {
                for(int j=startIndex+1;j<endIndex;j++)
                {
                    IndicatorField indf2l=indList.getSharedata(j-2);
                    IndicatorField indfl=indList.getSharedata(j-1);
                    IndicatorField indf=indList.getSharedata(j);

                    if(pct==0)
                    {
                        if((buy==0 && indf.getValue()>indfl.getValue() && indfl.getValue()<=indf2l.getValue()) ||
                            (buy==1 && indf.getValue()<indfl.getValue() && indfl.getValue()>=indf2l.getValue()))
                        {
                           endIndex=sl.isDatePresent(indf.getDDate());
                           if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                           break;
                        }
                    }
                    else
                    {
                        if((buy==0 && indf.getValue()>=low+(low*pct*0.01) ) || (buy==1 && indf.getValue()<=high-(high*pct*0.01) ))
                        {
                            endIndex=sl.isDatePresent(indf.getDDate());
                            if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                            break;
                        }
                        if(indf.getValue()>high)
                            high=indf.getValue();
                        if(indf.getValue()<low)
                            low=indf.getValue();

                    }
                }
                if(endIndex==indList.getSize()-1)
                    continue;
                if(endIndex==startIndex+days)
                {
                    int ind=sl.isDatePresent(indList.getSharedata(endIndex).getDDate());
                    if(ind==-1)
                        ind=sl.isLowerDatePresent(indList.getSharedata(endIndex).getDDate());
                    endIndex=ind;
                }
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
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(1)).intValue();
            else
                days=(Integer)params.get(1);
            double pct=0;
            if(params.get(2) instanceof Double)
                pct=(Double)params.get(2);
            else
                pct=((Integer)params.get(2))*1.0;
            StringBuilder buffer = new StringBuilder();
            buffer.append(days + " d");
            if(pct>0)
            {
                buffer.append(pct + "% ");
            }
            buffer.append("Simple Change Close ");
            return buffer.toString();
	}

//    public int compareTo(AbstractClose o) {
//        if(!(o instanceof DaysCutSimpleChangeClose) || o==null)
//            return -1;
//        else
//        {
//            int pctO=(Integer)o.params.get(1);
//            int pct=(Integer)params.get(1);
//            return pct-pctO;
//        }
//    }

}
