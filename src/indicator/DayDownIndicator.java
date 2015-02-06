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
public class DayDownIndicator extends AbstractIndicator
{
    private static final long serialVersionUID = 1L;

    public DayDownIndicator()
    {
        super();
    }

    @Override
    public void init() {

        this.params=new HashMap();
        params.put(1, 2);
        this.name="DayDown";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param)
    {
        this.params=param;
        this.name="DayDown";
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl)
    {
        IndicatorList ma=new IndicatorList(sl.getSize());
        Iterator itr=sl.getIterator();
        int cnt=Integer.parseInt(params.get(1).toString());
        ArrayList<Double> oldvals=new ArrayList<Double>();
	for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            oldvals.add(sd.getClosePrice());
            int val=1;
            if (oldvals.size() == cnt+1) {
                for(int k=1;k<oldvals.size();k++){
                    if(oldvals.get(k)>oldvals.get(k-1))
                        val=0;
                }
                IndicatorField indF = new IndicatorField(sd.getDate(), val, 0);
                ma.addIndField(indF);
                oldvals.remove(0);
            } else{
                IndicatorField indF = new IndicatorField(sd.getDate(), 0, 0);
                ma.addIndField(indF);
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
            buffer.append(param1);
            buffer.append("dDown ");

            string= buffer.toString();
            }
            return string;
	}

}