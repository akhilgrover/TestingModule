/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

/**
 *
 * @author Admin
 */
public class RangeIndicator  extends AbstractIndicator
{
    
    @Override
    public void init() 
    {
        this.name="Range Indicator";
        this.paramCount=2;
        this.params=new HashMap();
        params.put(1, 5);
        params.put(2, 3);
    }

    @Override
    public void init(HashMap param) {
        this.params=param;
        this.name="Range Indicator";
        this.paramCount=2;
    }
    
    /*
    public void init(int period,int period1) {
        this.period=period;
        this.period1=period1;
        this.name="Range Indicator";
        this.params=2;
    }*/

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        int param2=(Integer)params.get(2);
        
        IndicatorList range=new IndicatorList(sl.getSize());
        MA ema = new MA(param2,MA.Exponential); 
        double tr,ch,ran=0,x=0;
        Iterator itr=sl.getIterator();
        ShareData sdlast=null,sd;
        ArrayList change=new ArrayList();
        for (int i=0; i<sl.getSize(); i++)
	{
            sd=(ShareData) itr.next();
            //sdlast=(ShareData) itr.next();
            if(i>0)
            {
                tr=Math.max(sd.getHigh()-sd.getLow(),sd.getHigh()-sdlast.getClosePrice());
                tr=Math.max(tr, sdlast.getClosePrice()-sd.getLow());
                if(sd.getClosePrice()>sdlast.getClosePrice())
                {
                    ch=tr/(sd.getClosePrice()-sdlast.getClosePrice());
                    change.add(ch);
                }
                else
                {
                    ch=tr;
                    change.add(ch);
                }
                if(i>=param1)
                {
                    int ind=change.size();
                    ArrayList arr =  new ArrayList(change.subList(ind-param1, ind-1));
                    arr.add(ch);
                    double max=(Double)Collections.max(arr);
                    double min=(Double)Collections.min(arr);
                    if((max-min) > 0)
                    {
                        x=100*((ch-min)/(max-min));
                    }
                    else
                    {
                        x=100*(ch-min);
                    }
                    ran=ema.next(x);
                    IndicatorField indf=new IndicatorField(sd.getDate(),ran,0);
                    range.addIndField(indf);
                }
            }
            //System.out.println(sd.getDate() + "\t" + sd.getHigh() + "\t" + sd.getLow() + "\t" + sd.getClosePrice() + "\t" + x + "\t" +ran);
            sdlast=sd;
        }
        return range;
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
            buffer.append(" Period1 ");

            return buffer.toString();
	}

}
