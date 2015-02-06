/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareList;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Admin
 */
public class VerticalHorizontalIndicator extends AbstractIndicator{

    @Override
    public void init() 
    {
        this.params=new HashMap();
        params.put(1, 5);
        this.name="Vertical Horizontal";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param) {
        this.params=param;
        this.name="Vertical Horizontal";
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
        int param1=(Integer)params.get(1);
	IndicatorList vh=new IndicatorList(sl.getSize());
	double vhabs=0;
        ArrayList vhAbs=new ArrayList();
	for(int i=1;i<sl.getSize();i++)
	{
            if(i>=param1)
            {
                    double min=sl.getSharedata(i).getClosePrice();
                    for(int j=1;j<param1;j++)
                    {
                        if(sl.getSharedata(i-j).getClosePrice()<min)
                            min=sl.getSharedata(i-j).getClosePrice();

                    }
                    double max=sl.getSharedata(i).getClosePrice();
                    for(int j=1;j<param1;j++)
                    {
                        if(sl.getSharedata(i-j).getClosePrice()>max)
                            max=sl.getSharedata(i-j).getClosePrice();

                    }

                    vhabs=Math.abs(max-min);

            }
            double abs=Math.abs(sl.getSharedata(i).getClosePrice()-sl.getSharedata(i-1).getClosePrice());
            vhAbs.add(abs);
            double sum=0;
            if(i>=param1)
            {
                for(int j=1;j<=param1;j++)
                {
                        sum += (Double) vhAbs.get(vhAbs.size()-j);
                }					
            }

            vh.addIndField(new IndicatorField(sl.getSharedata(i).getDate(), vhabs/sum, 0));
	
	}
	return vh;
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
