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
public class DmiIndicator extends AbstractIndicator 
{
    
    @Override
    public void init() 
    {
        this.params=new HashMap();
        params.put(1, 5);
        params.put(2,10);
        this.name="DMI";
        this.paramCount=2;
    }

    @Override
    public void init(HashMap param) 
    {
        this.paramCount=2;
        this.name="DMI";
        this.params=param;
    }
    
    
    /*public void init(int period1, int period2) 
    {
        this.period=period1;
        this.period1=period2;
        this.name="DMI";
        this.params=2;
    }*/

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        int param2=(Integer)params.get(2);
	IndicatorList rsi = new IndicatorList(sl.getSize());
        IndicatorField indf;
	MA ma1 = new MA(param1, MA.Simple);
	MA ma2 = new MA(param2, MA.Simple);
	double avg[] = new double[sl.getSize()];
        double dw[] = new double[sl.getSize()];
        double uw[] = new double[sl.getSize()];
        dw[0] = 0;
        uw[0] = 0;
        double d=0;
        double u=0;
        Iterator itr=sl.getIterator();
        ShareData sdYest=(ShareData) itr.next();
	for (int j=1;j<param1;j++)
	{
            dw[j] = 0;
            uw[j] = 0;
            ShareData sd=(ShareData) itr.next();
            avg[j] = ma1.next(sd.getClosePrice());
            sdYest=sd;
	}
	for (int i=param1-1; i<sl.getSize()-1; i++)
	{
            ShareData sdToday=(ShareData) itr.next();
            
            double h1 = 0;
            avg[i] = ma1.next(sdToday.getClosePrice());

            for (int q=0;q<param1;q++)
            {
                h1 += Math.pow(sl.getSharedata(i-q).getClosePrice() - avg[i],2);
            }	
            double stdev = Math.sqrt(h1/(param1));		
            double mastdev =ma2.next(stdev);
            double volatility=stdev/mastdev;
            double tmp=14/volatility;
            if(volatility==0)
                tmp=30;
            int rsiPeriod=(int) Math.ceil(tmp);
            if(rsiPeriod<3)
                rsiPeriod=3;
            else if(rsiPeriod>30)
                rsiPeriod=30;
            
            
            if (sdToday.getClosePrice()-sdYest.getClosePrice()>0) 
            {
                u=sdToday.getClosePrice()-sdYest.getClosePrice();
                d=0;
            }
            if (sdToday.getClosePrice()-sdYest.getClosePrice()<0) 
            {
                d=sdYest.getClosePrice()-sdToday.getClosePrice();
                u=0;
            }
            dw[i] = dw[i-1] + (1.0/rsiPeriod)*(d-dw[i-1]);
            uw[i] = uw[i-1] + (1.0/rsiPeriod)*(u-uw[i-1]);
            double rs = 100-(100/(1+(uw[i]/dw[i])));
            indf=new IndicatorField(sdToday.getDate(),rs,0);
            rsi.addIndField(indf);
            sdYest=sdToday;
	}
	return rsi;
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
            buffer.append(" Day Std Deviation ");
            buffer.append(" ");
            buffer.append(param2);
            buffer.append(" Day Avg of Std Deviation ");

            return buffer.toString();
	}

}
