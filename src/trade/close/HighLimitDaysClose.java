package trade.close;


import trade.*;
import indicator.*;
import Share.*;
import trade.filter.*;
import java.util.*;

public class HighLimitDaysClose extends AbstractClose
{

    public HighLimitDaysClose()
    {
        this.paramCount=3;
    }

    public HighLimitDaysClose(HashMap param,int buy)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=3;
    }

    public TradeList fillClose(TradeList trdList, IndicatorList indList,
                                    ShareList sl, Filters filter)
    {
            double threshold=(Double) params.get(1);
            int days=0;
            if(params.get(2) instanceof Double)
                days=((Double)params.get(2)).intValue();
            else
                days=(Integer)params.get(2);
            double thresholdsl=(Double) params.get(3);
            TradeList trdListNew=new TradeList();
            Trade trd=null,trdOld;
            for(int i=0;i<trdList.getSize();i++)
            {
                trdOld=trdList.getTrade(i);
                int startIndex=indList.isDatePresent(trdOld.getStartDate());
                Date endDate=null;
                int endIndex=indList.getSize()-1;
                if(startIndex>-1)
                {
                    for(int j=startIndex+1;j<indList.getSize();j++)
                    {
                        //try{
                        IndicatorField indf=indList.getSharedata(j);

                        IndicatorField indfl=indList.getSharedata(j-1);
                        if((buy==0 && indf.getValue()<=threshold && indfl.getValue()>threshold) ||
                            ((buy==1 && indf.getValue()>threshold && indfl.getValue()<=threshold)))
                        {
                            endDate=indf.getDDate();
                            endIndex=sl.isDatePresent(endDate);
                            if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                            if(endIndex+days>=sl.getSize())
                                endIndex=sl.getSize();
                            else
                                endIndex=endIndex+days;
                            break;
                        }
                        if((buy==1 && indf.getValue()<=thresholdsl && indfl.getValue()>thresholdsl) ||
                            ((buy==0 && indf.getValue()>thresholdsl && indfl.getValue()<=thresholdsl)))
                        {
                            endDate=indf.getDDate();
                            endIndex=sl.isDatePresent(endDate);
                            if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                            break;
                        }
                        /*}catch(Exception ex)
                        {
                            System.out.println(j);
                        }*/
                    }
                    if(endIndex==indList.getSize()-1)
                    {
//                        IndicatorField indf=indList.getSharedata(indList.getSize()-1);
//                        endDate=indf.getDDate();
//                        endIndex=sl.isDatePresent(endDate);
                        continue;
                    }
                    endDate=sl.getSharedata(endIndex).getDate();
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
            //String sep = System.getProperty("line.separator");
            double threshold=(Double) params.get(1);    //close
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(2)).intValue();
            else
                days=(Integer)params.get(2);
            double thresholdsl=(Double) params.get(3); //stop loss
            StringBuilder buffer = new StringBuilder();
            buffer.append(thresholdsl);
            buffer.append(" StopLoss ");
            buffer.append(days);
            buffer.append(" d ");
            buffer.append(threshold);
            buffer.append(" High Close ");
            return buffer.toString();
	}

//    public int compareTo(AbstractClose o) {
//        if(!(o instanceof ThresholdClose) || o==null)
//            return -1;
//        else
//        {
//            Double pctO=(Double)o.params.get(1);
//            Double pct=(Double)params.get(1);
//            return Double.compare(pct,pctO);
//        }
//    }
}
