package trade.close;


import trade.*;
import indicator.*;
import Share.*;
import trade.filter.*;
import java.util.*;

public class ThresholdOrDaysCutCloseSL extends AbstractClose
{
    private boolean filterOpen;

    public ThresholdOrDaysCutCloseSL()
    {
        this.paramCount=3;
        filterOpen=false;
    }

    public ThresholdOrDaysCutCloseSL(HashMap param,int buy)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=3;
        filterOpen=false;
    }

    public ThresholdOrDaysCutCloseSL(HashMap param, int buy, boolean filter)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=3;
        filterOpen=filter;
    }

    public TradeList fillClose(TradeList trdList, IndicatorList indList,
                                    ShareList sl, Filters filter)
    {
        double threshold=(Double) params.get(1);
        double thresholdSL=(Double) params.get(3);
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
                if((buy==1 && indf.getValue() < threshold) ||
                        ((buy==0 && indf.getValue() > threshold)))
                    continue;
            }

            if(startIndex>-1)
            {
                for(int j=startIndex+1;j<=endIndex;j++)
                {
                    //try{
                    IndicatorField indf=indList.getSharedata(j);

                    IndicatorField indfl=indList.getSharedata(j-1);
                    if(((buy==1 && indf.getValue()<=threshold && indfl.getValue()>threshold) ||
                        ((buy==0 && indf.getValue()>threshold && indfl.getValue()<=threshold))) ||
                            (buy==1 && indf.getValue()<=thresholdSL && indfl.getValue()>thresholdSL) ||
                        ((buy==0 && indf.getValue()>thresholdSL && indfl.getValue()<=thresholdSL)))
                    {
                        endDate=indf.getDDate();
                        endIndex=j;
                        close=true;
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
    
    public TradeList fillClose(TradeList trdList, IndicatorList indList,IndicatorList indListSL,
                                    ShareList sl, Filters filter)
    {
        double threshold=(Double) params.get(1);
        double thresholdSL=(Double) params.get(3);
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
                if((buy==1 && indf.getValue() < threshold) ||
                        ((buy==0 && indf.getValue() > threshold)))
                    continue;
            }

            if(startIndex>-1)
            {
                for(int j=startIndex+1;j<=endIndex;j++)
                {
                    //try{
                    IndicatorField indf=indList.getSharedata(j);
                    IndicatorField indfl=indList.getSharedata(j-1);
                    IndicatorField indfSL=indListSL.getSharedata(j);
                    IndicatorField indflSL=indListSL.getSharedata(j-1);
                    if(((buy==1 && indf.getValue()<=threshold && indfl.getValue()>threshold) ||
                        ((buy==0 && indf.getValue()>threshold && indfl.getValue()<=threshold))) ||
                            (buy==1 && indfSL.getValue()<=thresholdSL && indflSL.getValue()>thresholdSL) ||
                        ((buy==0 && indfSL.getValue()>thresholdSL && indflSL.getValue()<=thresholdSL)))
                    {
                        endDate=indf.getDDate();
                        endIndex=j;
                        close=true;
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
            double thresholdSL=(Double) params.get(3);
            StringBuilder buffer = new StringBuilder();
            buffer.append(days);
            buffer.append(" dCut or ");
            buffer.append(threshold);
            buffer.append(" Threshold Close ");
            buffer.append(thresholdSL);
            buffer.append(" SL Close ");
            if(filterOpen)
                buffer.append("Fileterd ");
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
