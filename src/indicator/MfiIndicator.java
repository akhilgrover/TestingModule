/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Admin
 */
public class MfiIndicator extends AbstractIndicator 
{

    @Override
    public void init() 
    {
        this.name="Money Flow Index Indicator";
        this.params=new HashMap();
        params.put(1,14);
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param) 
    {
        this.params=param;
        this.name="Money Flow Index Indicator";
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
        IndicatorList mfi = new IndicatorList(sl.getSize());
        IndicatorField indf;
        double typPrice[]=new double[sl.getSize()];
        double pmf[]=new double[sl.getSize()];
        double nmf[]=new double[sl.getSize()];
        double sumPmf=0,sumNmf=0;
        Iterator itr=sl.getIterator();
        for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            typPrice[i]=(sd.getClosePrice()+sd.getHigh()+sd.getLow())/3;
            if(i>0)
            {
                if(typPrice[i]>typPrice[i-1])
                {
                    pmf[i]=sd.getVol()*typPrice[i];
                    nmf[i]=0;
                }
                else
                {
                    nmf[i]=sd.getVol()*typPrice[i];
                    pmf[i]=0;
                }
                sumPmf+=pmf[i];
                sumNmf+=nmf[i];
                if(i>=param1)
                {
                    sumPmf=sumPmf-pmf[i-param1];
                    sumNmf=sumNmf-nmf[i-param1];
                    double moneyRatio=sumPmf/sumNmf;
                    double mf=(100-(100/(1+moneyRatio)));
                    indf=new IndicatorField(sd.getDate(), mf, 0);
                    mfi.addIndField(indf);
                }
            }
        }
        return mfi;
        
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
