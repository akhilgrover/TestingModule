/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.filter;

import Share.ShareData;
import Share.ShareList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

/**
 *
 * @author Admin
 */
public class MACDSignalFilter extends AbstractFilter
{
    private ArrayList<Double> macd;
    private ArrayList<Double> signal;
    
    public MACDSignalFilter()
    {
        this.paramCount=3;
    }

    public MACDSignalFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=3;
    }

    @Override
    public boolean filterTrade(int shareIndex, int buy, ShareList sl) 
    {
        if(macd==null)
        {
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            int param3=(Integer)params.get(3);
            macd=new ArrayList<Double>();
            signal=new ArrayList<Double>();
            MA ma1 = new MA(param1, MA.Exponential);
            MA ma2 = new MA(param2, MA.Exponential);
            MA signal_ma = new MA(param3, MA.Exponential);
            Iterator itr=sl.getIterator();
            for (int i=0; i<sl.getSize(); i++)
            {
                Double close=((ShareData)itr.next()).getClosePrice();
                double macdVal = ma2.next(close) - ma1.next(close);
                double sig = signal_ma.next(macdVal);
                macd.add(macdVal);
                signal.add(sig);
            }
        }
        boolean ret=false;
        if(buy==1 && macd.get(shareIndex)>signal.get(shareIndex))
        {
            ret=true;
        }
        else if(buy==0 && macd.get(shareIndex)<signal.get(shareIndex))
        {
            ret=true;
        }
        return ret;
    }

    public void buildFilter(int buy, ShareList sl) 
    {
        
        int param1=(Integer)params.get(1);
        int param2=(Integer)params.get(2);
        int param3=(Integer)params.get(3);
        macd=new ArrayList<Double>();
        signal=new ArrayList<Double>();

        MA ma1 = new MA(param1, MA.Exponential);
        MA ma2 = new MA(param2, MA.Exponential);
        MA signal_ma = new MA(param3, MA.Exponential);
        Iterator itr=sl.getIterator();
        for (int i=0; i<sl.getSize(); i++)
        {
            Double close=((ShareData)itr.next()).getClosePrice();
            double macdVal = ma2.next(close) - ma1.next(close);
            double sig = signal_ma.next(macdVal);
            macd.add(macdVal);
            signal.add(sig);
        }
    }
    
    @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            int param3=(Integer)params.get(3);
            StringBuilder buffer = new StringBuilder();
            buffer.append("MACD");
            buffer.append(" ");
            buffer.append(param2);
            buffer.append(" Short Period ");
            buffer.append(param1);
            buffer.append(" Long Period ");
            buffer.append(param3);
            buffer.append(" Day signal ");

            return buffer.toString();
	}

//    public int compareTo(AbstractFilter o) {
//        if(!(o instanceof MACDSignalFilter) || o==null)
//            return -1;
//        else
//        {
//            if(o.paramCount!=this.paramCount)
//                return -1;
//            else
//            {
//                int param1O=(Integer)o.params.get(1);
//                int param2O=(Integer)o.params.get(2);
//                int param3O=(Integer)o.params.get(3);
//                int param1=(Integer)params.get(1);
//                int param2=(Integer)params.get(2);
//                int param3=(Integer)params.get(3);
//                int ret=(param1-param1O)*100+(param2-param2O)*10+(param3-param3O);
//                return ret;
//            }
//        }
//    }

}
