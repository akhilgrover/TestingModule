package trade.filter;

import Share.ShareList;
import indicator.IndicatorList;
import indicator.RsiIndicator;
import ma.*;
import java.util.*;

public class RsiChangeFilter extends AbstractFilter {

    transient private IndicatorList RsiClose;
    transient ShareList sl;
    private static final long serialVersionUID = -4441095523212287789L;

    public RsiChangeFilter() {
        this.paramCount = 2;
    }

    public RsiChangeFilter(HashMap param) {
        this.params = param;
        this.paramCount = 2;
    }

    @Override
    public void buildFilter(int buy, ShareList sl) {
        int period = (Integer) params.get(1);
        RsiIndicator rsi = new RsiIndicator();
        HashMap hm = new HashMap();
        hm.put(1, period);
        hm.put(2, 1);
        rsi.init(hm);
        RsiClose = rsi.buildIndicator(sl);
        this.sl = sl;
    }

    @Override
    public boolean filterTrade(int shareIndex, int buy, ShareList slst) {
        int threshold = (Integer) params.get(2);
        if (RsiClose == null || RsiClose.getSize()==0) {
            buildFilter(buy, sl);
        }
        if (!sl.getShare().equals(slst.getShare())) {
            buildFilter(buy, sl);
            sl=slst;
        }
        boolean ret = false;
        double close = 0, em = 0, emyest = 0;
        if (sl.getShare().equals(slst.getShare())) {
            Date d = slst.getSharedata(shareIndex).getDate();
            int index = sl.isDatePresent(d);
            if (index == -1) {
                index = sl.isLowerDatePresent(d);
            }
            close = sl.getSharedata(index).getClosePrice();
            int ind = RsiClose.isDatePresent(d);
            if (ind > -1) {
                em = RsiClose.getSharedata(ind).getValue();
                emyest = em;
                if (ind > 0) {
                    emyest = RsiClose.getSharedata(ind - 1).getValue();
                }
            } else {
                System.out.print("Error Invalid Dates");
            }
        } else {
            System.out.print("Error Different Shares");
        }
        if (buy == 1 && em > emyest && emyest > threshold) {
            ret = false;
        } else if (buy == 0 && em > emyest && emyest < threshold) {
            ret = false;
        } else //if(buy==0)
        {
            ret = true;
        }
        if (threshold == 0) {
            ret = true;
        }
        return ret;
    }

    public void releaseSL() {
        sl = null;
    }

    @Override
    public String toString() {
        int period = (Integer) params.get(1);
        int threshold = (Integer) params.get(2);
        StringBuilder buffer = new StringBuilder();
        buffer.append(period);
        buffer.append("d Rsi ");
        buffer.append(threshold);
        //buffer.append(" Filter3 ");
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
    @Override
    public void clear() {
        RsiClose = null;
    }
}
