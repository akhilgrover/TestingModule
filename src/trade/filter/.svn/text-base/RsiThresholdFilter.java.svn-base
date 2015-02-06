package trade.filter;

import Share.ShareList;
import indicator.IndicatorList;
import indicator.RsiIndicator;
import ma.*;
import java.util.*;

public class RsiThresholdFilter extends AbstractFilter
{
    transient private IndicatorList RsiClose;
    transient ShareList sl;
    private static final long serialVersionUID = -4441095523212287789L;

    public RsiThresholdFilter()
    {
        this.paramCount=2;
    }

    public RsiThresholdFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=2;
    }

    public void buildFilter(int buy, ShareList sl)
    {
        int period=(Integer)params.get(1);
        int threshold=(Integer)params.get(2);
        RsiIndicator rsi=new RsiIndicator();
        HashMap hm=new HashMap();
        hm.put(1, period);
        hm.put(2, 1);
        rsi.init(hm);
        RsiClose=rsi.buildIndicator(sl);
        this.sl=sl;        
    }

    public boolean filterTrade(int shareIndex, int buy, ShareList slst)
    {
        int period=(Integer)params.get(1);
        int threshold=(Integer)params.get(2);
        if(RsiClose==null){
            this.sl=slst;
            System.out.print("Error filter not yet Built");
            //RsiClose=rsi.buildIndicator(sl);
        }
        boolean ret=false;
        double close=0,em=0;
        if(sl.getShare().equals(slst.getShare()))
        {
            Date d=slst.getSharedata(shareIndex).getDate();
            int index=sl.isDatePresent(d);
            if(index==-1)
                index=sl.isLowerDatePresent(d);
            close=sl.getSharedata(index).getClosePrice();
            if(RsiClose.isDatePresent(d)>-1)
                em=RsiClose.getSharedata(RsiClose.isDatePresent(d)).getValue();
            else
                System.out.print("Error Invalid Dates");
        }
        else
        {
            System.out.print("Error Different Shares");
//            Date d=slst.getSharedata(shareIndex).getDate();
//            int index=sl.isDatePresent(d);
//            if(index==-1)
//                index=sl.isLowerDatePresent(d);
//            close=sl.getSharedata(index).getClosePrice();
//            em=(Double)maClose.get(index);
        }
        if(buy==1 && em<threshold)
        {
            ret=true;
        }
        else if(buy==0 && em>threshold)
        {
            ret=true;
        }
        return ret;
    }

    public void releaseSL()
    {
        sl=null;
    }

    @Override
	public String toString()
	{
            int period=(Integer)params.get(1);
            int threshold=(Integer)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append("d Rsi ");
            buffer.append(threshold);
            buffer.append(" Threshold Filter ");
            /*buffer.append(period);
            buffer.append(" Day ");
            if(type==MA.Exponential)
                buffer.append("EMA ");
            else if(type==MA.Simple)
                buffer.append("SMA ");
            else if(type==MA.Weighted)
                buffer.append("WMA ");*/
            return buffer.toString();
	}

//    public int compareTo(AbstractFilter o) {
//        if(!(o instanceof MaFilter) || o==null)
//            return -1;
//        else
//        {
//            if(o.paramCount!=this.paramCount)
//                return -1;
//            else
//            {
//                int periodO=(Integer)o.params.get(1);
//                int typeO=(Integer)o.params.get(2);
//                int period=(Integer)params.get(1);
//                int type=(Integer)params.get(2);
//                if(typeO!=type)
//                    return -1;
//                else
//                    return period-periodO;
//            }
//        }
//    }
}
