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
public class DaysBackIndicator extends AbstractIndicator
{
    private static final long serialVersionUID = -2705440187884990057L;


    public DaysBackIndicator()
    {
        super();
    }

    @Override
    public void init() {

        this.params=new HashMap();
        params.put(1, 10);
        this.name="DaysBack";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param)
    {
        this.params=param;
        this.name="DB";
        this.paramCount=1;
    }

    public void init(HashMap param, int type)
    {
        this.params=param;
        this.name="MA";
        this.paramCount=1;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl)
    {
        int back=Integer.parseInt(params.get(1).toString());
        IndicatorList ma=new IndicatorList(sl.getSize());
	for(int i=1;i<sl.getSize();i++)
	{
            ShareData sd=sl.getSharedata(i);
            int b=i-back;
            if(b<0)
                b=0;
            ShareData sdBack=sl.getSharedata(b);
            IndicatorField indF=new IndicatorField(sd.getDate(), (sd.getClosePrice()-sdBack.getClosePrice())*100/sdBack.getClosePrice(), sdBack.getClosePrice());
            ma.addIndField(indF);
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
            buffer.append("d Back ");

            string= buffer.toString();
            }
            return string;
	}

}