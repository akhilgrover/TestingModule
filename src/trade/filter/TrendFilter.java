/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.filter;

import Share.ShareData;
import Share.ShareList;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

/**
 *
 * @author Admin
 */
public class TrendFilter extends AbstractFilter
{

    boolean trendFilter[];
    
    public TrendFilter()
    {
        this.paramCount=1;
    }

    public TrendFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=1;
    }

    public void buildFilter(int buy, ShareList sl)
    {
        if(trendFilter==null)
        {
            int period=(Integer)params.get(1);
            trendFilter = new boolean[sl.getSize()];
            MA sma = new MA(period,MA.Simple);
            Iterator itr=sl.getIterator();
            for (int i=0; i<sl.getSize(); i++)
            {
                ShareData sd=(ShareData) itr.next();
                if (sd.getClosePrice()>sma.next((sd.getClosePrice() + sd.getHigh() + sd.getLow())/3))
                {
                    trendFilter[i]=true;
                }
                else
                {
                    trendFilter[i]=false;
                }
            }
        }
    }

    public boolean filterTrade(int shareIndex, int buy, ShareList sl) 
    {
        if(trendFilter==null)
        {
            int period=(Integer)params.get(1);
            trendFilter = new boolean[sl.getSize()];   
            MA sma = new MA(period,MA.Simple);
            Iterator itr=sl.getIterator();
            for (int i=0; i<sl.getSize(); i++)
            {
                ShareData sd=(ShareData) itr.next();
                if (sd.getClosePrice()>sma.next((sd.getClosePrice() + sd.getHigh() + sd.getLow())/3))
                {
                    trendFilter[i]=true;
                }
                else
                {
                    trendFilter[i]=false;
                }
            }
        }
	return trendFilter[shareIndex];
    }

    @Override
	public String toString()
	{
            int period=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append(" Trend Filter ");
            
            return buffer.toString();
	}

//    public int compareTo(AbstractFilter o) {
//        if(!(o instanceof InertiaThresholdFilter) || o==null)
//            return -1;
//        else
//        {
//            if(o.paramCount!=this.paramCount)
//                return -1;
//            else
//            {
//                int param1O=(Integer)o.params.get(1);
//                int param1=(Integer)params.get(1);
//                int ret=(param1-param1O);
//                return ret;
//            }
//        }
//    }
}
