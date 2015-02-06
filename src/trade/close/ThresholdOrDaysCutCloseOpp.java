package trade.close;


import trade.*;
import indicator.*;
import Share.*;
import trade.filter.*;
import java.util.*;

public class ThresholdOrDaysCutCloseOpp extends AbstractClose
{
    boolean filterOpen;

    public ThresholdOrDaysCutCloseOpp()
    {
        this.paramCount=2;
        filterOpen=false;
    }

    public ThresholdOrDaysCutCloseOpp(HashMap param,int buy)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=2;
        filterOpen=false;
    }

    public ThresholdOrDaysCutCloseOpp(HashMap param, int buy, boolean filter)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=2;
        filterOpen=filter;
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
        TradeList trdListNew=new TradeList();
        Trade trd=null,trdOld;
        
        for(int i=0;i<trdList.getSize();i++)
        {
            boolean close=false;
            trdOld=trdList.getTrade(i);
            int startIndex=indList.isDatePresent(trdOld.getStartDate());
            Date endDate=null;
            int endIndex=startIndex+days;
            if(startIndex+days>=indList.getSize())
                endIndex=indList.getSize()-1;
            else if(startIndex+days==indList.getSize()-1)
            {
                close=true;
            }

            if(filterOpen)
            {
                IndicatorField indf=indList.getSharedata(startIndex);
                if((buy==0 && indf.getValue() < threshold) ||
                        ((buy==1 && indf.getValue() > threshold)))
                    continue;
            }

            if(startIndex>-1)
            {
                for(int j=startIndex+1;j<=endIndex;j++)
                {
                    //try{
                    IndicatorField indf=indList.getSharedata(j);

                    IndicatorField indfl=indList.getSharedata(j-1);
                    if((buy==0 && indf.getValue()<=threshold && indfl.getValue()>threshold) ||
                        ((buy==1 && indf.getValue()>threshold && indfl.getValue()<=threshold)))
                    {
                        endDate=indf.getDDate();
                        endIndex=j;
                        close=true;
//                        endIndex=sl.isDatePresent(endDate);
//                        if(endIndex==-1)
//                            endIndex=sl.isLowerDatePresent(indf.getDDate());
                        break;
                    }
                    /*}catch(Exception ex)
                    {
                        System.out.println(j);
                    }*/
                }
                if(endIndex>=indList.getSize()-1 && !close)
                {
//                        IndicatorField indf=indList.getSharedata(indList.getSize()-1);
//                        endDate=indf.getDDate();
//                        endIndex=sl.isDatePresent(endDate);
                    //System.out.print(endIndex);
                    continue;
                }
                endDate=indList.getSharedata(endIndex).getDDate();
                endIndex=sl.isDatePresent(endDate);
                if(endIndex==-1)
                    endIndex=sl.isLowerDatePresent(endDate);
                if(endIndex>=sl.getSize()-1 && !close)
                {
                    //System.out.print(endIndex);
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
            //String sep = System.getProperty("line.separator");
            double threshold=(Double) params.get(1);
            int days=0;
            if(params.get(2) instanceof Double)
                days=((Double)params.get(2)).intValue();
            else
                days=(Integer)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(days);
            buffer.append(" d or ");
            buffer.append(threshold);
            //buffer.append(" Close ");
            if(filterOpen)
                buffer.append(" Close28 ");
            else
                buffer.append(" Close27 ");
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
