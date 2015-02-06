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
public class ForcastOscillatorIndicator extends AbstractIndicator {

    @Override
    public void init() {
        this.name="ForcastOscilator";
        this.params=new HashMap();
        params.put(1, 5);
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param) {
        this.name="ForcastOscilator";
        this.params=param;
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) {

        int param1=(Integer)params.get(1);
        MA ma3 = new MA(param1,MA.Simple);
	MA ma4 = new MA(param1,MA.Simple);
        IndicatorList forcast = new IndicatorList(sl.getSize());
        IndicatorField indf;
	int counter=0;
	double  Fo[]=new double[sl.getSize()];
	double t=0,d=0;
	double cp[]=new double[sl.getSize()];
	double avgx=0,avgy=0;
	Iterator itr=sl.getIterator();
	for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            counter=i+1;
            d=0;t=0;
            avgx=ma3.next(Double.valueOf(counter));
            avgy=ma4.next(sd.getClosePrice());
            if(i>=param1-1)
            {
                for(int k=0;k<param1;k++)
                {
                    ShareData sdPrev=sl.getSharedata(i-k);
                    t+=((counter-k)-avgx)*(sdPrev.getClosePrice()-avgy);
                    d+=((counter-k)-avgx)*((counter-k)-avgx);
                }
                double b=t/d;
                double a=avgy-(b*avgx);
                Fo[i]=a+(b*(counter+1));
                if(i>=param1)
                {
                    cp[i]=sd.getClosePrice()- Fo[i-1];
                    double c=100*(cp[i]/sd.getClosePrice());
                    indf=new IndicatorField(sd.getDate(), c, 0);
                    forcast.addIndField(indf);
                }
            }
	}
	return forcast;
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
