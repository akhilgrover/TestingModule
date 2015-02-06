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
public class DpOscillatorIndicator extends AbstractIndicator {

    @Override
    public void init()
    {
        this.name="Detrended Price Oscillator";
        params=new HashMap();
        params.put(1, 6);
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param) {
        this.name="Detrended Price Oscillator";
        this.params=param;
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        MA ma1 = new MA(param1,MA.Simple);
	IndicatorList dpo=new IndicatorList(sl.getSize());
	int fp=(param1/2)+1;
        Iterator itr=sl.getIterator();
	for(int i=0;i<sl.getSize();i++)
	{
            double sma=ma1.next(((ShareData)itr.next()).getClosePrice());
            if(i>=param1)
            {
                ShareData sd=sl.getSharedata(i-fp);
                double dp=sd.getClosePrice()-sma;
                IndicatorField indF=new IndicatorField(sd.getDate(), dp, 0);
                dpo.addIndField(indF);
            }
	}
	return dpo;
    }
    
    
    @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");
            buffer.append(param1);
            buffer.append(" DP Oscillator ");

            return buffer.toString();
	}
    

}
