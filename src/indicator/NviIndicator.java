

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

/**
 *
 * @author Akhil
 */
public class NviIndicator extends AbstractIndicator
{

    @Override
    public void init()
    {
        this.params=new HashMap();
        params.put(1, 10);
        this.name="NVI";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param)
    {
        this.params=param;
        this.name="NVI";
        this.paramCount=1;
    }

    /**
     * Method buildIndicator
     *
     *
     * @param sl: share price list
     *
     * @return: Indicator result
     *
     */
    @Override
    public IndicatorList buildIndicator(ShareList sl)
    {
        int param1=(Integer)params.get(1);
        MA ema=new MA(param1,MA.Exponential);
        IndicatorList nvi = new IndicatorList(sl.getSize());
        IndicatorField indf;
        indf=new IndicatorField(sl.getSharedata(0).getDate(), 1000, ema.next(1000.0));
        nvi.addIndField(indf);
        Iterator itr=sl.getIterator();
        ShareData sdLast=(ShareData) itr.next();
	for(int i=1;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            double nv=0;
            if(sd.getVol()<sdLast.getVol())
            {
                    nv=nvi.getSharedata(i-1).getValue()+(((sd.getClosePrice()-sdLast.getClosePrice())/sdLast.getClosePrice())*nvi.getSharedata(i-1).getValue());
            }
            else
            {
                    nv=nvi.getSharedata(i-1).getValue();
            }
            indf=new IndicatorField(sl.getSharedata(i).getDate(), nv, ema.next(nv));
            nvi.addIndField(indf);
            sdLast= sd;
	}
        return nvi;
    }

    @Override
	public String toString()
	{
            int param1=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");
            buffer.append(param1);
            buffer.append(" Day Signal ");

            return buffer.toString();
	}

}
