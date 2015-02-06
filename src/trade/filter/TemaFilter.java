/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.filter;

import Share.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

/**
 *
 * @author Admin
 */
public class TemaFilter extends AbstractFilter {

    private ArrayList tema;
    
    public TemaFilter()
    {
        this.paramCount=1;
    }
    
    
    public TemaFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=1;
        
    }

    public void buildFilter( int buy, ShareList sl)
    {
        if(tema==null)
        {
            int period=(Integer)params.get(1);
            MA ema1=new MA(period,MA.Exponential);
            MA ema2=new MA(period,MA.Exponential);
            MA ema3=new MA(period,MA.Exponential);
            Iterator itr=sl.getIterator();
            for(int i=0;i<sl.getSize();i++)
            {
                ShareData sd=(ShareData) itr.next();
                double em1=ema1.next(sd.getClosePrice());
                double em2=ema2.next(em1);
                double em3=ema3.next(em2);
                if(i>=(period-1)*3)
                {
                    double tem=((3*em1)-(3*em2)+em3);
                    tema.add(tem);
                }
                else
                    tema.add(0);
            }
        }
    }
    
    public boolean filterTrade(int shareIndex, int buy, ShareList sl) 
    {
        if(tema==null)
        {
            int period=(Integer)params.get(1);
            MA ema1=new MA(period,MA.Exponential);
            MA ema2=new MA(period,MA.Exponential);
            MA ema3=new MA(period,MA.Exponential);
            Iterator itr=sl.getIterator();
            for(int i=0;i<sl.getSize();i++)
            {
                ShareData sd=(ShareData) itr.next();
                double em1=ema1.next(sd.getClosePrice());
                double em2=ema2.next(em1);
                double em3=ema3.next(em2);
                if(i>=(period-1)*3)
                {
                    double tem=((3*em1)-(3*em2)+em3);
                    tema.add(tem);
                }
                else
                    tema.add(0);
            }
        }
        
        boolean ret=false;
        double close=sl.getSharedata(shareIndex).getClosePrice();
        double tem=(Double)tema.get(shareIndex);
        //todo: filter conditions
        if(buy==1 && close>tem)
        {
            ret=true;
        }
        else if(buy==0 && close<tem)
        {
            ret=true;
        }
        return ret;
    }
    
    @Override
	public String toString()
	{
            int period=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append(" TEMA Filter ");
            return buffer.toString();
	}

//    public int compareTo(AbstractFilter o) {
//        if(!(o instanceof TemaFilter) || o==null)
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
