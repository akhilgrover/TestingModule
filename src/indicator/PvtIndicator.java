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
public class PvtIndicator  extends AbstractIndicator
{

    @Override
    public void init() 
    {
        this.name="Price Volume Trend";
        this.paramCount=0;
        
    }
    
    @Override
    public void init(HashMap param) 
    {
        this.name="Price Volume Trend";
        this.paramCount=0;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) {
        IndicatorList pvt = new IndicatorList(sl.getSize());
	IndicatorField indf=new IndicatorField(sl.getSharedata(0).getDate(), 0,0);
        pvt.addIndField(indf);
        double pvtl=0;
        Iterator itr=sl.getIterator();
        ShareData sdLast=(ShareData) itr.next();
	for(int i=1;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            double col1=sd.getClosePrice()-sdLast.getClosePrice();
            double col2=col1/sdLast.getClosePrice();
            double col3=col2*sd.getVol();
            double pvtn=col3+pvtl;
            indf=new IndicatorField(sd.getDate(), pvtn,0);
            pvt.addIndField(indf);
            pvtl=pvtn;
            sdLast=sd;
	}
	return pvt;
    }
    
    @Override
	public String toString()
	{
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");
            return buffer.toString();
	}

    

}
