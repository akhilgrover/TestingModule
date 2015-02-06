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
public class MACDThresholdFilter extends AbstractFilter
{
    private ArrayList<Double> macd;
    
    //param 1,2 for macd
    //param 3  -1 for '<' and 1 for '>'
    //param 4 threshold
    
    public MACDThresholdFilter()
    {
        this.paramCount=4;
    }

    public MACDThresholdFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=4;
    }

    @Override
    public boolean filterTrade(int shareIndex, int buy, ShareList sl) 
    {
        int param3=(Integer)params.get(3);
        int param4=(Integer)params.get(4);
        if(macd==null)
        {
            macd=new ArrayList<Double>();
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            MA ma1 = new MA(param1, MA.Exponential);
            MA ma2 = new MA(param2, MA.Exponential);
            //MA signal_ma = new MA(param3, MA.Exponential);
            Iterator itr=sl.getIterator();
            for (int i=0; i<sl.getSize(); i++)
            {
                Double close=((ShareData)itr.next()).getClosePrice();
                double macdVal = ma2.next(close) - ma1.next(close);
                //double sig = signal_ma.next(macdVal);
                macd.add(macdVal);
            }
        }
        
        boolean ret=false;
        if(param3<0)
        {
            if(macd.get(shareIndex)<param4)
            {
                ret=true;
            }
        }
        else if(param3>0)
        {
            if(macd.get(shareIndex)>param4)
            {
                ret=true;
            }
        }        
        
        return ret;
    }

    public void buildFilter(int buy, ShareList sl) 
    {
        if(macd==null)
        {
            macd=new ArrayList<Double>();
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            //int param3=(Integer)params.get(3);

            MA ma1 = new MA(param1, MA.Exponential);
            MA ma2 = new MA(param2, MA.Exponential);
            //MA signal_ma = new MA(param3, MA.Exponential);
            Iterator itr=sl.getIterator();
            for (int i=0; i<sl.getSize(); i++)
            {
                Double close=((ShareData)itr.next()).getClosePrice();
                double macdVal = ma2.next(close) - ma1.next(close);
                //double sig = signal_ma.next(macdVal);
                macd.add(macdVal);
                //signal.add(sig);
            }
        }
    }
    
    @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            int param4=(Integer)params.get(4);
            StringBuilder buffer = new StringBuilder();
            buffer.append("MACD");
            buffer.append(" ");
            buffer.append(param2);
            buffer.append(" Short Period ");
            buffer.append(param1);
            buffer.append(" Long Period ");
            buffer.append(param4);
            buffer.append(" Threshold ");

            return buffer.toString();
	}

//    public int compareTo(AbstractFilter o) {
//        if(!(o instanceof MACDThresholdFilter) || o==null)
//            return -1;
//        else
//        {
//            if(o.paramCount!=this.paramCount)
//                return -1;
//            else
//            {
//                int param4O=(Integer)o.params.get(4);
//                int param4=(Integer)params.get(4);
//                if(param4!=param4O)
//                    return -1;
//                else
//                {
//                    int param1O=(Integer)o.params.get(1);
//                    int param2O=(Integer)o.params.get(2);
//                    int param3O=(Integer)o.params.get(3);
//                    int param1=(Integer)params.get(1);
//                    int param2=(Integer)params.get(2);
//                    int param3=(Integer)params.get(3);
//                    int ret=(param1-param1O)*100+(param2-param2O)*10+(param3-param3O);
//                    return ret;
//                }
//            }
//        }
//    }

}
