package trade.filter;

import Share.ShareList;
import ma.*;
import java.util.*;

public class MaCustomFilter extends AbstractFilter
{
    private ArrayList maClose;
    MA ema;

    public MaCustomFilter()
    {
        this.paramCount=2;
    }

    public MaCustomFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=2;
        int period=(Integer)params.get(1);
        //this.cond=(Integer)params.get(2);
        int type=(Integer)params.get(3);
        ema=new MA(period,type);
    }

    @Override
    public void buildFilter(int buy, ShareList sl)
    {
        maClose=new ArrayList();
        for(int i=0;i<sl.getSize();i++)
        {
            Share.ShareData sd=sl.getSharedata(i);
            maClose.add(ema.next(sd.getClosePrice()));
        }
    }


    @Override
    public boolean filterTrade(int shareIndex, int buy, ShareList sl)
    {

        if(maClose==null){
            maClose=new ArrayList();
            ema.refresh();
            for(int i=0;i<sl.getSize();i++)
            {
                Share.ShareData sd=sl.getSharedata(i);
                maClose.add(ema.next(sd.getClosePrice()));
            }
        }
        boolean ret=false;
        double close=sl.getSharedata(shareIndex).getClosePrice();
        double em=(Double)maClose.get(shareIndex);
        int cond=(Integer)params.get(2);
        if(cond==-1 && close<em)
        {
            ret=true;
        }
        else if(cond==1 && close>em)
        {
            ret=true;
        }
        return ret;
    }

    @Override
	public String toString()
	{
            int period=(Integer)params.get(1);
            int type=(Integer)params.get(3);
            int cond=(Integer)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append(" d ");
            if(cond==-1)
                buffer.append("LT ");
            else
                buffer.append("GT ");
            if(type==MA.Exponential)
                buffer.append("EMA ");
            else if(type==MA.Simple)
                buffer.append("SMA ");
            else if(type==MA.Weighted)
                buffer.append("WMA ");
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
