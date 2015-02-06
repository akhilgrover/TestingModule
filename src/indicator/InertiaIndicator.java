/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.*;
import java.util.HashMap;
import ma.MA;

/**
 *
 * @author Admin
 */
public class InertiaIndicator extends AbstractIndicator{

    private int stdPeriod=10;
    
    @Override
    public void init() 
    {
        this.name="Inertia";
        this.params=new HashMap();
        params.put(1, 5);
        params.put(2, 20);
        this.paramCount=2;
    }

    @Override
    public void init(HashMap param) 
    {
        this.name="Inertia";
        this.params=param;
        this.paramCount=2;
    }
    
    /*
    public void init(int period,int period2) 
    {
        this.period=period;
        this.periodInit=period2;
        this.name="Inertia";
        this.params=2;
    }*/

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        int param2=(Integer)params.get(2);

        IndicatorList inertia=new IndicatorList(sl.getSize());
        double rvi[] = new double[sl.getSize()]; //relative volatility index
        double rvih[] = new double[sl.getSize()]; //relative volatility index high
        double rvil[] = new double[sl.getSize()]; //relative volatility index low
        MA ma1 = new MA(stdPeriod,MA.Simple);
        MA ma2 = new MA(stdPeriod,MA.Simple);
        MA ma3 = new MA(param2,MA.Simple);
        MA ma4 = new MA(param2,MA.Simple);

        double h1 = 0;
        double l1 = 0;
        double h2[] = new double[sl.getSize()];
        double l2[] = new double[sl.getSize()];
        double hh2[] = new double[sl.getSize()];
        double ll2[] = new double[sl.getSize()];
        double avHigh[] = new double[sl.getSize()]; // high MA
        double avLow[] = new double[sl.getSize()]; // low MA
        int counter=0;
        int x[] = new int[sl.getSize()];
        double a=0,b=0;
        double t=0,d=0;
        double avgx=0,avgy=0;
        for (int j=0;j<stdPeriod;j++)
        {
                l2[j] = h2[j] = rvih[j] = 0;
                ll2[j] = hh2[j] = rvil[j] = 0;
                rvi[j]=0;
                ShareData sd=sl.getSharedata(j);
                avHigh[j] = ma1.next(sd.getHigh());
                avLow[j] = ma2.next(sd.getLow());
        }
        for (int i=stdPeriod; i<sl.getSize(); i++)
        {
            h1 = 0;
            l1 = 0;
            ShareData sd=sl.getSharedata(i);
            avHigh[i] = ma1.next(sd.getHigh());
            avLow[i] = ma2.next(sd.getLow());

            for (int q=0;q<stdPeriod;q++)
            {
                ShareData sd1=sl.getSharedata(i-q);
                h1 += Math.pow(sd1.getHigh() - avHigh[i],2);
                l1 += Math.pow(sd1.getLow() - avLow[i],2);
            }	
            double stdevH = Math.sqrt(h1/(stdPeriod));		
            double stdevL = Math.sqrt(l1/(stdPeriod));
            ShareData sdt=sl.getSharedata(i-1);
            if(sd.getHigh()>sdt.getHigh())
            {
                h2[i] = h2[i-1] + (1.0/param1)*(stdevH-h2[i-1]);
                hh2[i] = hh2[i-1] + (1.0/param1)*(0-hh2[i-1]);
            }
            else if(sd.getHigh()<sdt.getHigh())
            {
                h2[i] = h2[i-1] + (1.0/param1)*(0-h2[i-1]);
                hh2[i] = hh2[i-1] + (1.0/param1)*(stdevH-hh2[i-1]);
            }
            else
            {
                h2[i] = h2[i-1] + (1.0/param1)*(0-h2[i-1]);
                hh2[i] = hh2[i-1] + (1.0/param1)*(0-hh2[i-1]);
            }

            if(sd.getLow()>sdt.getLow())
            {
                l2[i] = l2[i-1] + (1.0/param1)*(stdevL-l2[i-1]);
                ll2[i] = ll2[i-1] + (1.0/param1)*(0-ll2[i-1]);
            }
            else if(sd.getLow()<sdt.getLow())
            {
                l2[i] = l2[i-1] + (1.0/param1)*(0-l2[i-1]);
                ll2[i] = ll2[i-1] + (1.0/param1)*(stdevL-ll2[i-1]);
            }
            else
            {
                l2[i] = l2[i-1] + (1.0/param1)*(0-l2[i-1]);
                ll2[i] = ll2[i-1] + (1.0/param1)*(0-ll2[i-1]);
            }
            rvih[i] = 100*h2[i]/(h2[i]+hh2[i]);
            rvil[i] = 100*l2[i]/(l2[i]+ll2[i]);
            rvi[i]=(rvih[i]+rvil[i])/2;

            if(i+1>=(stdPeriod-1)+param1)
                    x[i]=++counter;
            //avgx=avgy=0;
            b=a=d=t=0;
            avgx=ma3.next(Double.parseDouble(Integer.toString(counter)));
            avgy=ma4.next(rvi[i]);
            double init=0;
            if(counter>=param2)
            {
                for(int k=0;k<param2;k++)
                {
                    t+=(x[i-k]-avgx)*(rvi[i-k]-avgy);
                    d+=(x[i-k]-avgx)*(x[i-k]-avgx);
                }
                b=t/d;
                a=    (avgy - (b * avgx));
                init=a+b*counter;
                IndicatorField indf=new IndicatorField(sd.getDate(),init,0);
                inertia.addIndField(indf);
                
            }
         }
        return inertia;
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
		buffer.append(" RVI ");
        buffer.append(" ");
		buffer.append(param2);
		buffer.append(" LR ");
                
		return buffer.toString();
	}
    
}