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
public class TemaIndicator extends AbstractIndicator
{
    
    @Override
    public void init() 
    {
        this.params=new HashMap();
        params.put(1, 5);
        this.name="TEMA Indicator";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param) 
    {
        this.params=param;
        this.name="TEMA Indicator";
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        IndicatorList tema = new IndicatorList(sl.getSize());
        IndicatorField indf;
        MA ema1=new MA(param1,MA.Exponential);
        MA ema2=new MA(param1,MA.Exponential);
        MA ema3=new MA(param1,MA.Exponential);
        Iterator itr=sl.getIterator();
        for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            double em1=ema1.next(sd.getClosePrice());
            double em2=ema2.next(em1);
            double em3=ema3.next(em2);
            if(i>=(param1-1)*3)
            {
                double tem=((3*em1)-(3*em2)+em3);
                indf=new IndicatorField(sd.getDate(), tem, 0);
                tema.addIndField(indf);
            }
            
        }
        return tema;
    }
    
    @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");
            buffer.append(param1);
            buffer.append(" Period ");
            return buffer.toString();
	}

}
