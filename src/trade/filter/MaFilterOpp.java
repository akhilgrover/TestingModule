package trade.filter;

import Share.ShareList;
import ma.*;
import java.util.*;

public class MaFilterOpp extends AbstractFilter
{
    transient private ArrayList maClose;
    MA ema;
    transient ShareList sl;
    private static final long serialVersionUID = -4441095523212287790L;

    public MaFilterOpp()
    {
        this.paramCount=1;
    }

    public MaFilterOpp(HashMap param)
    {
        this.params=param;
        this.paramCount=1;
        int period=(Integer)params.get(1);
        int type=(Integer)params.get(2);
        ema=new MA(period,type);
    }

    public void buildFilter(int buy, ShareList sl)
    {
        maClose=new ArrayList();
        this.sl=sl;
        for(int i=0;i<sl.getSize();i++)
        {
            Share.ShareData sd=sl.getSharedata(i);
            maClose.add(ema.next(sd.getClosePrice()));
        }
    }


    public boolean filterTrade(int shareIndex, int buy, ShareList slst)
    {
        
        if(maClose==null){
            this.sl=slst;
            maClose=new ArrayList();
            for(int i=0;i<sl.getSize();i++)
            {
                Share.ShareData sd=sl.getSharedata(i);
                maClose.add(ema.next(sd.getClosePrice()));
            }
        }
        boolean ret=false;
        double close=0,em=0;
        if(sl.getShare().equals(slst.getShare()))
        {
            close=sl.getSharedata(shareIndex).getClosePrice();
            em=(Double)maClose.get(shareIndex);
        }
        else
        {
            Date d=slst.getSharedata(shareIndex).getDate();
            int index=sl.isDatePresent(d);
            if(index==-1)
                index=sl.isLowerDatePresent(d);
            close=sl.getSharedata(index).getClosePrice();
            em=(Double)maClose.get(index);
        }
        if(buy==0 && close>em)
        {
            ret=true;
        }
        else if(buy==1 && close<em)
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
            int type=(Integer)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.ema.toString());
            buffer.append(" Opp");
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
