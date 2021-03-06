/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Admin
 */
public class BullishBearishIndicator extends AbstractIndicator
{
    private static final long serialVersionUID = 1L;

    public BullishBearishIndicator()
    {
        super();
    }

    @Override
    public void init() {

        this.params=new HashMap();
        params.put(1, 1);
        this.name="BullBear";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param)
    {
        this.params=param;
        this.name="BullBear";
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl)
    {
        IndicatorList ma=new IndicatorList(sl.getSize());
        Iterator itr=sl.getIterator();
        boolean high=Integer.parseInt(params.get(1).toString())==1;
        int cnt=252;
        ArrayList<Double> oldvals=new ArrayList<Double>();
	for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            oldvals.add(sd.getClosePrice());
            double hl = getMax(oldvals, high);
            int value = 0;
            if (oldvals.size() > 2) {
                double clslast = oldvals.get(oldvals.size() - 2);
                double per = (sd.getClosePrice() - clslast) * 100 / sd.getClosePrice();
                double per2 = (clslast - hl) * 100 / hl;
                if (high && per > 0.015 && per2 > -10 && per2 < -6) {
                    value = 1;
                } else if (!high && per <= -0.015 && per2 < 15 && per2 > 6) {
                    value = 1;
                }
            }
            IndicatorField indF = new IndicatorField(sd.getDate(), value, 0);
            ma.addIndField(indF);
            if (oldvals.size() == cnt+1) {
                oldvals.remove(0);
            }
            
            //System.out.println(indF.getDDate() + "\t" + indF.getValue() + "\t" + indF.getSignal());
        }
        return ma;
    }

    @Override
    public String toString()
    {
            if(string==null){
            int param1=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            //buffer.append(name);
            //buffer.append(" ");
            //buffer.append("Year");
            if(param1==1)
                buffer.append("Bull ");
            else
                buffer.append("Bear ");

            string= buffer.toString();
            }
            return string;
	}

    private double getMax(ArrayList<Double> oldvals,boolean high) {
        double highV=oldvals.get(0);
        double lowV=oldvals.get(0);
        for(int i=1;i<oldvals.size()-1;i++){
            double v=oldvals.get(i);
            if(v>highV)
                highV=v;
            if(v<lowV)
                lowV=v;
        }
        double val=oldvals.get(oldvals.size()-1);
        return high ? highV : lowV;
        //return (high && val>highV) || (!high && val<lowV);
    }

}