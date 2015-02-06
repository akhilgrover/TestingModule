/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Share.ShareList;
import datasource.BasicShareDB;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gnisoft
 */
public class TradingDays {

    public static TreeSet<Date> tradingDays = getTradingDays();

    private static TreeSet<Date> getTradingDays() {
        TreeSet<Date> arr=new TreeSet<Date>();
        BasicShareDB bsd = null;
        try {
            bsd = new BasicShareDB();
            ShareList sl=bsd.getShareData("UKX");
            for(int i=0;i<sl.getSize();i++)
            {
                arr.add(sl.getSharedata(i).getDate());
            }
        } catch (Exception ex) {
            Logger.getLogger(TradingDays.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            try {
                bsd.close();
            } catch (SQLException ex) {
                Logger.getLogger(TradingDays.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return arr;
    }

    public static void refresh()
    {
        tradingDays=getTradingDays();
    }

    /**
     *
     * @param d1 Start Date
     * @param d2 End Date
     * @return d2-d1
     */

    public static int dayDiff(Date d1, Date d2)
    {
        return tradingDays.subSet(d1,d2).size();
    }


}
