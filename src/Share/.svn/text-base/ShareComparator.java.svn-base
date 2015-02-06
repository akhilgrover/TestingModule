/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Share;

import datasource.BasicShareDB;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Admin
 */

public class ShareComparator implements Comparator
{
    private boolean asc=false;
    private HashMap<String,ShareList> shareMap;
    private Date dd;
    private int days;
    private BasicShareDB bsdb;

    public ShareComparator(boolean asc, Date dd, int days,HashMap hm, BasicShareDB bs)
    {
        this.asc=asc;
        this.dd=dd;
        this.days=days;
        this.shareMap=hm;
        this.bsdb=bs;
    }

    public int compare(Object o1, Object o2)
    {
        String sh1=o1.toString();
        String sh2=o2.toString();
        ShareList sl1,sl2;
        Double profit1 = 0.0,profit2 = 0.0;
        int ret = 0;

        try
        {
            if(shareMap.containsKey(sh1))
            {
                sl1=shareMap.get(sh1);
            }
            else
            {
                sl1 = bsdb.getShareData(sh1);
                shareMap.put(sh1, sl1);
            }
            if(shareMap.containsKey(sh2))
            {
                sl2= shareMap.get(sh2);
            }
            else
            {
                sl2 = bsdb.getShareData(sh2);
                shareMap.put(sh2, sl2);
            }
            int stIndex=sl1.isDatePresent(dd);
            int stIndex1=sl2.isDatePresent(dd);

            if(stIndex>=0 && stIndex1>=0 )
            {
                int d=days;
                if(stIndex-days<0)
                    d=0;
                profit1=((sl1.getSharedata(stIndex).getClosePrice()-sl1.getSharedata(stIndex-d).getClosePrice())/sl1.getSharedata(stIndex-d).getClosePrice())*100;
                //System.out.println(sl1.getSharedata(stIndex-d));
                d=days;
                if(stIndex1-days<0)
                    d=0;
                profit2=((sl2.getSharedata(stIndex1).getClosePrice()-sl2.getSharedata(stIndex1-d).getClosePrice())/sl2.getSharedata(stIndex1-d).getClosePrice())*100;
            }
            else if(stIndex==-1)
            {
                profit2=100.0;
            }
            else if(stIndex1==-1)
            {
                profit1=100.0;
            }
            
            ret=Double.compare(profit2 , profit1);
            //ret=(int)(profit2 - profit1);
            if(ret==0)
                ret=1;
            if(asc)
                ret=-ret;
        }
        catch (Exception ex)
        {
            Logger.getLogger(ShareComparator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }



}
