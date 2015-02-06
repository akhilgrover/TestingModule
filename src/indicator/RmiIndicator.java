/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.HashMap;
import ma.MA;

/**
 *
 * @author Admin
 */
public class RmiIndicator extends AbstractIndicator 
{

    @Override
    public void init() {
        
        this.name="RMI";
        this.paramCount=2;
        this.params=new HashMap();
        params.put(1, 8);
        params.put(2, 4);
    }

    @Override
    public void init(HashMap param) {
        
        this.name="RMI";
        this.params=param;
        this.paramCount=2;
    }
    
    /*
    public void init(int period,int period2) {
        this.period=period;
        this.period2=period2;
        this.name="RMI";
        this.params=2;
    }*/

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        int param2=(Integer)params.get(2);
        IndicatorList rmi = new IndicatorList(sl.getSize());
        IndicatorField indf;
	MA ma1 = new  MA(param1,MA.Simple);
	MA ma2 = new  MA(param1,MA.Simple);
	
	double dw[] = new double[sl.getSize()];
        double uw[] = new double[sl.getSize()];
        double d=0;
        double u=0;
        double SC=0;
        double SC1=0;
        double E=0;
	for (int j=0;j<param2;j++)
	{
            dw[j] = 0;
            uw[j] = 0;
	}
	for(int i=param2;i<sl.getSize();i++)
	{
            ShareData sdToday=sl.getSharedata(i);
            ShareData sdYest=sl.getSharedata(i-param2);
            if(sdToday.getClosePrice()-sdYest.getClosePrice()>=0)
            {
                u=sdToday.getClosePrice()-sdYest.getClosePrice();
                d=0;
            }
            else if(sdToday.getClosePrice()-sdYest.getClosePrice()<=0)
            {
                d=Math.abs(sdToday.getClosePrice()-sdYest.getClosePrice());
                u=0;
            }
            if(i<param1+param2)
            {
                SC=ma1.next(u);
                SC1=ma2.next(d);
            }
            else
            {
                SC=SC+((u-SC)/param1);
                SC1=SC1+((d-SC1)/param1);
            }
            E=SC/SC1;
            indf=new IndicatorField(sdToday.getDate(),(100*(E/(1+E))),0);
            rmi.addIndField(indf);
        }
        return rmi;
    }
    
     @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            int param2=(Integer)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");
            buffer.append(param1);
            buffer.append(" Period ");
            buffer.append(" ");
            buffer.append(param2);
            buffer.append(" Momentum Period ");
            buffer.append(" ");
            return buffer.toString();
	}

}
