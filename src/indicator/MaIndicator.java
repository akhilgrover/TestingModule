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
public class MaIndicator extends AbstractIndicator
{
    private static final long serialVersionUID = -2705440187884990057L;

    MA avg;
    private int type;

    public MaIndicator()
    {
        super();
    }

    public MaIndicator(int type)
    {
        super();
        this.type=type;
    }


    @Override
    public void init() {

        this.params=new HashMap();
        params.put(1, 10);
        this.name="Moving Average";
        this.paramCount=1;
        this.avg=new MA(10, MA.Simple);
    }

    @Override
    public void init(HashMap param)
    {
        this.params=param;
        this.name="MA";
        this.paramCount=1;
        int typ=type;
        if(params.size()==2)
            typ=(Integer)params.get(2);
        if(params.size()>0)
            this.avg=new MA((Integer)params.get(1), typ);
        else
            this.avg=new MA(10, type);

        if(typ==MA.Exponential)
           this.name = "E";
        else if(typ==MA.Simple)
           this.name = "S";
        else if(typ==MA.Weighted)
           this.name = "W";
        this.name+="MA";
    }

    public void init(HashMap param, int type)
    {
        this.params=param;
        this.name="MA";
        this.paramCount=1;
        if(params.size()>0)
            this.avg=new MA((Integer)params.get(1), type);
        else
            this.avg=new MA(10, type);

        if(type==MA.Exponential)
           this.name = "E";
        else if(type==MA.Simple)
           this.name = "S";
        else if(type==MA.Weighted)
           this.name = "W";
        this.name+="MA";
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl)
    {
        avg.refresh();
        IndicatorList ma=new IndicatorList(sl.getSize());
        Iterator itr=sl.getIterator();
	for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            IndicatorField indF=new IndicatorField(sd.getDate(), sd.getClosePrice(), avg.next(sd.getClosePrice()));
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
            buffer.append("d Indicator2 ");

            string= buffer.toString();
            }
            return string;
	}

}