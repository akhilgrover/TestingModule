package trade.close;


import trade.*;
import indicator.*;
import Share.*;
import trade.filter.*;
import java.util.*;

public class HighLimitChangeSLClose extends AbstractClose
{

    public HighLimitChangeSLClose()
    {
        this.paramCount=6;
    }

    public HighLimitChangeSLClose(HashMap param,int buy)
    {
        this.params=param;
        this.buy=buy;
        this.paramCount=6;
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
            double thresholdslLow=(Double) params.get(4);
            double change=((Double)params.get(5));
            double changeAbove=((Double)params.get(6));
            TradeList trdListNew=new TradeList();
            Trade trd=null,trdOld;

            for(int i=0;i<trdList.getSize();i++)
            {
                trdOld=trdList.getTrade(i);
                int startIndex=indList.isDatePresent(trdOld.getStartDate());
                Date endDate=null;
                int endIndex=indList.getSize()-1;
                double high=indList.getSharedata(startIndex).getValue();
                double low=indList.getSharedata(startIndex).getValue();
                boolean doStop=false;
                int size=indList.getSize();
                boolean close=false;

                if(startIndex>-1)
                {
                    for(int j=startIndex+1;j<size;j++)
                    {
                        //try{
                        IndicatorField indf=indList.getSharedata(j);

                        IndicatorField indfl=indList.getSharedata(j-1);
                        if(!doStop && ((buy==0 && indf.getValue()<=threshold && indfl.getValue()>threshold) ||
                            ((buy==1 && indf.getValue()>threshold && indfl.getValue()<=threshold))))
                        {
                            endDate=indf.getDDate();
                            endIndex=sl.isDatePresent(endDate);
                            if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                            if(endIndex+days>=sl.getSize())
                                endIndex=sl.getSize()-1;
                            else
                                endIndex=endIndex+days;
                            if(size>=j+days)
                                size=j+days;
                            doStop=true;
                            close=true;
                            //break;
                        }
//                        if((buy==0 && indf.getValue()<threshold )||
//                            (buy==1 && indf.getValue()>threshold ))
//                        {
//                            doStop=true;
//                        }
//                        if(doStop)
                        {
                            
                            if(doStop && ((buy==0 && indf.getValue()>=low+changeAbove ) || (buy==1 && indf.getValue()<=high-changeAbove )) || (!doStop && ((buy==0 && indf.getValue()>=low+change ) || (buy==1 && indf.getValue()<=high-change ))))
                            {
                                endDate=indf.getDDate();
                                endIndex=sl.isDatePresent(endDate);
                                if(endIndex==-1)
                                    endIndex=sl.isLowerDatePresent(indf.getDDate());
                                close=true;
                                break;
                            }
                            if(indf.getValue()>high)
                                high=indf.getValue();
                            if(indf.getValue()<low)
                                low=indf.getValue();
                        }
                        if((buy==1 && indf.getValue()<=thresholdsl && indfl.getValue()>thresholdsl) ||
                            ((buy==0 && indf.getValue()>thresholdsl && indfl.getValue()<=thresholdsl)))
                        {
                            endDate=indf.getDDate();
                            endIndex=sl.isDatePresent(endDate);
                            if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                            close=true;
                            break;
                        }
                        if((buy==1 && indf.getValue()<=thresholdslLow && indfl.getValue()>thresholdslLow) ||
                            ((buy==0 && indf.getValue()>thresholdslLow && indfl.getValue()<=thresholdslLow)))
                        {
                            endDate=indf.getDDate();
                            endIndex=sl.isDatePresent(endDate);
                            if(endIndex==-1)
                                endIndex=sl.isLowerDatePresent(indf.getDDate());
                            close=true;
                            break;
                        }
                        /*}catch(Exception ex)
                        {
                            System.out.println(j);
                        }*/
                    }
                    if(endIndex==indList.getSize()-1 && !close)
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
            double thresholdslLow=(Double) params.get(4); //stop loss
            double change=((Double)params.get(5));//change
            double changeAbove=((Double)params.get(6));//change Above
            StringBuilder buffer = new StringBuilder();
            buffer.append(thresholdsl);
            buffer.append(",");
            buffer.append(thresholdslLow);
            buffer.append(" SL ");
            buffer.append(change);
            buffer.append(",");
            buffer.append(changeAbove);
            buffer.append(" Drop ");
            buffer.append(days);
            buffer.append(" d After ");
            buffer.append(threshold);
            buffer.append(" Close7 ");
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
