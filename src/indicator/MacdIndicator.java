/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

/**
 *
 * @author Admin
 */
public class MacdIndicator extends AbstractIndicator  {

    
    @Override
    public void init() 
    {
        this.name="MACD";
        this.paramCount=3;
        this.params=new HashMap();
        params.put(1, 13);
        params.put(2, 26);
        params.put(3, 9);
        
    }

    @Override
    public void init(HashMap param) 
    {
        this.name="MACD";
        this.paramCount=3;
        this.params=param;
    }
    
    /*
    public void init(int period,int period1,int signal) 
    {
        this.name="MACD";
        this.params=3;
        this.period=period;
        this.period1=period1;
        this.signal=signal;
    }*/

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        int param2=(Integer)params.get(2);
        int param3=(Integer)params.get(3);
        
        MA ma1 = new MA(param1, MA.Exponential);
	MA ma2 = new MA(param2, MA.Exponential);
	MA signal_ma = new MA(param3, MA.Exponential);
        IndicatorList macd=new IndicatorList(sl.getSize());
        Iterator itr=sl.getIterator();
	for (int i=0; i<sl.getSize(); i++)
	{
            Double close=((ShareData)itr.next()).getClosePrice();
            double macdVal = ma1.next(close) - ma2.next(close);
            double sig = signal_ma.next(macdVal);	
            macd.addIndField(new IndicatorField(sl.getSharedata(i).getDate(), macdVal, sig));
            //System.out.println(sl.getSharedata(i).getDate()+ "\t" +  macdVal + "\t" + sig);
	}
        return macd;
    }
    
    @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            int param3=(Integer)params.get(3);
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");
            buffer.append(param2);
            buffer.append(" Short Period ");
            buffer.append(param1);
            buffer.append(" Long Period ");
            buffer.append(param3);
            buffer.append(" d signal ");

            return buffer.toString();
	}

}
