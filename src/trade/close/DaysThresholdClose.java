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
public class DaysThresholdClose extends AbstractClose
{

    /*private double threshold;
    private int days;
    private int buy;
    private int params=2;*/
    
    public DaysThresholdClose()
    {
        this.paramCount=2;       
    }
    
    public DaysThresholdClose(HashMap param, int buy)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=2;       
    }
        
    public TradeList fillClose(TradeList trdList, IndicatorList indList, ShareList sl, Filters filter) 
    {
        int days=0;
        if(params.get(1) instanceof Double)
            days=((Double)params.get(1)).intValue();
        else 
            days=(Integer)params.get(1);
        double threshold=(Double)params.get(2);
        TradeList trdListNew=new TradeList();
        Trade trd=null,trdOld;
        for(int i=0;i<trdList.getSize();i++)
        {
            trdOld=trdList.getTrade(i);
            int startIndex=indList.isDatePresent(trdOld.getStartDate());
            Date endDate=null;
            int endIndex=-1;
            if(startIndex>-1)
            {
                for(int j=startIndex+1;j<indList.getSize();j++)
                {
                    boolean invalid=false;
                    if(j>(startIndex+days-1))
                    {
                        for(int k=0;k<days-1;k++)
                        {
                            IndicatorField	indTemp=indList.getSharedata(j-(k+2));
                            if((buy==0 && indTemp.getValue()<threshold) || (buy==1 && indTemp.getValue()>threshold))
                            {
                                invalid=true;
                            }
                        }
                        IndicatorField	indf=indList.getSharedata(j);
                        IndicatorField	indfl=indList.getSharedata(j-1);
                        if(((buy==0 && indf.getValue()>threshold && indfl.getValue()<=threshold) ||
                                ((buy==1 && indf.getValue()<threshold && indfl.getValue()>=threshold))) && !invalid)
                        {
                            endDate=indf.getDDate();
                            endIndex=sl.isDatePresent(endDate);
                            break;
                        }
                    }
                }

                if(endIndex==-1)
                {
//                    IndicatorField indf=indList.getSharedata(indList.getSize()-1);
//                    endDate=indf.getDDate();
//                    endIndex=sl.isDatePresent(endDate);
                    continue;
                }

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
            double threshold=(Double)params.get(2);
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(1)).intValue();
            else
                days=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(days);
            buffer.append(" d ");
            buffer.append(threshold);
            buffer.append(" Thrshold Close ");
            return buffer.toString();
	}

    /*public int compareTo(AbstractClose o) {
        if(!(o instanceof DaysThresholdClose) || o==null)
            return -1;
        else
        {
            int days=(Integer)params.get(1);
            int daysO=(Integer)o.params.get(1);
            if(days==daysO)
            {
                double pctO=(Double)o.params.get(2);
                double pct=(Double)params.get(2);
                return Double.compare(pct, pctO);
            }
            else{
                return days-daysO;
            }
        }
    }
*/
}
