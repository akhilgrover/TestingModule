package datasource;

import Dividend.*;
import Results.*;
import Sectors.SuperSect;
import Share.*;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import indicator.AbstractIndicator;
import indicator.IndicatorField;
import indicator.IndicatorList;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import ma.MA;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import trade.Trade;
import trade.TradeCalculator;
import trade.TradeList;

public class BasicShareDB implements Serializable {

    //private ShareList slist;
    private Connection con;
    private Connection conInst;
    private Statement stmt;
    private Statement stmtInst;
    private HashMap<String,ShareList> sListMap;
    //private ResultSet rs;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    private boolean online;
    private Jongo jongo;
    //private static final Calendar c = Calendar.getInstance();

    /**
     * Method BasicShareDB
     *
     *
     */
    public BasicShareDB() throws Exception {

        createConn();
        sListMap=new HashMap<String, ShareList>();

    }

    public Date getIndexStart(String toString)
            throws SQLException {
        Date ret = null;
        //Calendar c=Calendar.getInstance();
        String sql = "SELECT TOP 1 Indices.Date FROM Indices GROUP BY Indices.Date, Indices.[index] HAVING (((Indices.[index])='" + toString + "')) ORDER BY Indices.Date;";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            ret = rs.getDate(1);
        }
        return ret;
    }

    /*
     * get Data for a share since 1995
     */
    public synchronized ShareList getShareData(String Share)
            throws Exception {
        //stmt=con.createStatement();
            /*int count=0;
        String sql="Select count(sharedate) from shares where code='"+Share+"' and shares.ShareDate>'1/1/1995';";
        if(stmt==null || con.isClosed())
        createConn();
        ResultSet rs=stmt.executeQuery(sql);
        while(rs.next())
        {
        count=rs.getInt(1);
        }
        rs.close();
        if(count==0)
        {
        sql="Select count(sharedate) from sharescomplete where code='"+Share+"' and sharescomplete.ShareDate>'1/1/1995';";
        rs=stmt.executeQuery(sql);
        while(rs.next())
        {
        count=rs.getInt(1);
        }
        rs.close();
        if(count==0)
        return new ShareList(count,Share);
        sql="Select sharedate,[close1],high,low,[open],volume from sharescomplete where code='"+Share+"' and sharescomplete.ShareDate>'1/1/1995' order by sharedate;";
        }
        else
        sql="Select sharedate,[close],high,low,[open],volume from shares where code='"+Share+"' and shares.ShareDate>'1/1/1995' order by sharedate;";
        slist=new ShareList(count,Share);

        rs=stmt.executeQuery(sql);
        while(rs.next())
        {
        ShareData sd=new ShareData(Share,rs.getDate(1, c,c),rs.getDouble(2),rs.getDouble(3),rs.getDouble(4),rs.getDouble(5),rs.getLong(6));
        slist.addShareData(sd);
        }
        rs.close();
        return slist;*/
        //Calendar c=Calendar.getInstance();
        String sql = "Select sharedate,[close],high,low,[open],volume from shares where code='" + Share + "' and shares.ShareDate>'1/1/1995' order by sharedate;";
        if(!online)
        {
            sql="Select sharedate,[close],high,low,[open],volume from shares where code='" + Share + "' and shares.ShareDate>#1995/1/1# order by sharedate;";
        }
        ShareList slist = new ShareList(4000, Share.intern());

        if (stmt == null || con.isClosed() || !checkConnQ()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        //rs.last();
        //System.out.println(rs.getRow());
        //rs.first();
        while (rs.next()) {
            ShareData sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
            slist.addShareData(sd);
        }
        rs.close();
        if (slist.getSize() == 0) {
            sql = "Select sharedate,[close1],high,low,[open],volume from sharescomplete where code='" + Share + "' and sharescomplete.ShareDate>'1/1/1995' order by sharedate;";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ShareData sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
                slist.addShareData(sd);
            }
            rs.close();
        }
        return slist;
    }

    public synchronized ShareList getShareTableData(String Share, String table)
            throws Exception {
        //stmt=con.createStatement();
        String cls = "close";
        if (table.equals("SharesComplete")) {
            cls = "close1";
        }
        int count = 0;
        String sql = "Select count(sharedate) from " + table + " where code='" + Share + "' and ShareDate>'1/1/1995';";
        if (stmt == null || con.isClosed() || !checkConnQ()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ShareList slist = new ShareList(count, Share.intern());
        //Calendar c=Calendar.getInstance();
        sql = "Select sharedate,[" + cls + "],high,low,[open],volume from " + table + " where code='" + Share + "' and ShareDate>'1/1/1995' order by sharedate;";
        rs = stmt.executeQuery(sql);
        while (rs.next()) {

            ShareData sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
            slist.addShareData(sd);
        }
        rs.close();
        return slist;
    }

    public synchronized ShareList getInteradayShareData(String Share, String timeFrom, String timeTo)
            throws Exception {
        //stmt=con.createStatement();
        int count = 0;
        String sql = "Select count(sharedate) from InteradayShares where code='" + Share + "' and InteradayShares.ShareDate>'1/1/1995';";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ShareList slist = new ShareList(count, Share.intern());
        sql = "Select sharedate,[close],high,low,[open],volume from InteradayShares where code='" + Share + "' and InteradayShares.ShareDate>'1/1/1995' and (CONVERT(varchar(10), ShareDate, 108) BETWEEN '" + timeFrom + "' AND '" + timeTo + "') order by sharedate;";
        rs = stmt.executeQuery(sql);
        Calendar c=Calendar.getInstance();
        while (rs.next()) {
            ShareData sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
            slist.addShareData(sd);
        }
        rs.close();
        return slist;
    }

    public ArrayList getInteradayShares() throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        String sql = "Select code from InteradayShares group by code;";
        if (online) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                arr.add(rs.getString(1));
            }
            rs.close();

        }
        return arr;
    }

    public synchronized ShareList getShareData(String rfType, String rfGroup)
            throws Exception {
        String Share = rfType + " " + rfGroup;
        ShareList slist = new ShareList(4000, Share.intern());
        rfType = getSys(rfType);
        String sql = "select date," + rfType + " from RisersFallersHistory where Date>'1/1/1995'";
        //String sql="select date,"+rfType+" from RF_ASX where Date>'1/1/1995'";
        if (rfGroup.equals("AllWithoutTrust")) {
            sql = "select date,(" + rfType + "/count)*100 from RisersFallersHistoryWithoutTrust where count>0 and Date>'1/1/1995'";
            //sql="select date,("+rfType+"/count)*100 from RF_ASX where Date>'1/1/1995'";
        } else if (rfGroup.equals("INC")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date>'1/1/1995'";
            sql = "select date,(" + rfType + "/count)*100 from RF_ASX where count>0 and Date>'1/1/1995'";
        } else if (rfGroup.equals("EXC")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date>'1/1/1995'";
            sql = "select date,(" + rfType + "Ex/countEx)*100 from RF_ASX where count>0 and Date>'1/1/1995'";
        } else if (rfGroup.equals("AllWithTrust")) {
            //sql="select date,("+tbl+"/count)*100 from RisersFallersHistoryWithTrust";
        } /*else if (rfGroup.equals("GSPC")) {
            sql = "select date," + rfType + " from RisersFallersHistoryGSPC where count>0 and Date>'1/1/1995'";
        }*/ else if (rfGroup.equals("INCL")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date>'1/1/1995'";
            sql = "select date," + rfType + " from RF_ASX where count>0 and Date>'1/1/1995'";
        } else if (rfGroup.equals("EXCL")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date>'1/1/1995'";
            sql = "select date," + rfType + "Ex from RF_ASX where count>0 and Date>'1/1/1995'";
        }  else if (rfGroup.equals("GSPC")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_GSPC where count>0 order by date";
        }  else if (rfGroup.equals("GSPCL")) {
            sql = "select date," + rfType + " from RF_GSPC where count>0 order by date";
        } else if (rfGroup.equals("OEX")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_OEX where count>0 order by date";
        } else if (rfGroup.equals("GSPC-OEX")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_GSPC where count>0 order by date";
        }
        if (stmt == null || con.isClosed()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        //Calendar c=Calendar.getInstance();
        while (rs.next()) {
            ShareData sd = new ShareData("", rs.getDate(1), rs.getDouble(2), 0, 0, 0, 0);
            slist.addShareData(sd);
        }
        rs.close();
        return slist;
    }

    public synchronized ShareData getShareData(String rfType, String rfGroup, Date dd)
            throws Exception {

        String Share = rfType + " " + rfGroup;
        ShareData sd = null;
        rfType = getSys(rfType);
        String sql = "select " + rfType + " from RisersFallersHistory where Date='" + sdf.format(dd) + "'";
        //String sql="select date,"+rfType+" from RF_ASX where Date='"+sdf.format(dd)+"' order by date";
        if (rfGroup.equals("AllWithoutTrust")) {
            sql = "select (" + rfType + "/count)*100 from RisersFallersHistoryWithoutTrust where Date='" + sdf.format(dd) + "'";
            //sql="select date,("+rfType+"/count)*100 from RF_ASX where Date='"+sdf.format(dd)+"' order by date";
        } else if (rfGroup.equals("INC")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date='"+sdf.format(dd)+"' order by date";
            sql = "select (" + rfType + "/count)*100 from RF_ASX where count>0 and Date='" + sdf.format(dd) + "'";
        } else if (rfGroup.equals("EXC")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date='"+sdf.format(dd)+"' order by date";
            sql = "select (" + rfType + "Ex/countEx)*100 from RF_ASX where countEx>0 and Date='" + sdf.format(dd) + "'";
        } else if (rfGroup.equals("AllWithTrust")) {
            //sql="select date,("+tbl+"/count)*100 from RisersFallersHistoryWithTrust";
        } else if (rfGroup.equals("INCL")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date='"+sdf.format(dd)+"' order by date";
            sql = "select " + rfType + " from RF_ASX where count>0 and Date='" + sdf.format(dd) + "'";
        } else if (rfGroup.equals("EXCL")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date='"+sdf.format(dd)+"' order by date";
            sql = "select " + rfType + "Ex from RF_ASX where countEx>0 and Date='" + sdf.format(dd) + "'";
        }  else if (rfGroup.equals("GSPC")) {
            sql = "select (" + rfType + "/count)*100 from RF_GSPC where count>0 and Date='" + sdf.format(dd) + "' order by date";
        }  else if (rfGroup.equals("OEX")) {
            sql = "select (" + rfType + "/count)*100 from RF_OEX where count>0 and Date='" + sdf.format(dd) + "' order by date";
        }  else if (rfGroup.equals("GSPC-OEX")) {
            sql = "select (" + rfType + "/count)*100 from RF_GSPC where count>0 and Date='" + sdf.format(dd) + "' order by date";
        }
        if (stmt == null || con.isClosed() || !checkConnQ()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        Calendar c=Calendar.getInstance();
        while (rs.next()) {
            sd = new ShareData("", sdf.parse(sdf.format(dd)), rs.getDouble(1), 0, 0, 0, 0);
        }
        rs.close();
        return sd;
    }

    public synchronized ShareList getShareData(String rfType, String rfGroup, MA ma, boolean round)
            throws Exception {
        String Share = rfType + " " + rfGroup + " " + ma.getPeriod() + " DS";
        ShareList slistT = new ShareList(4000, Share.intern());
        rfType = getSys(rfType);
        String sql = "select date," + rfType + " from RisersFallersHistory where Date>'1/1/1995' order by date";
        //String sql="select date,"+rfType+" from RF_ASX where Date>'1/1/1995' order by date";
        if (rfGroup.equals("AllWithoutTrust")) {
            sql = "select date,(" + rfType + "/count)*100 from RisersFallersHistoryWithoutTrust where Date>'1/1/1995' order by date";
            //sql="select date,("+rfType+"/count)*100 from RF_ASX where Date>'1/1/1995' order by date";
        } else if (rfGroup.equals("INC")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_ASX where count>0 order by date";
        } else if (rfGroup.equals("EXC")) {
            sql = "select date,(" + rfType + "Ex/countEx)*100 from RF_ASX where count>0 order by date";
        } else if (rfGroup.equals("AllWithTrust")) {
            //sql="select date,("+tbl+"/count)*100 from RisersFallersHistoryWithTrust";
        }  else if (rfGroup.equals("GSPC")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_GSPC where count>0 order by date";
        }  else if (rfGroup.equals("OEX")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_OEX where count>0 order by date";
        }  else if (rfGroup.equals("GSPC-OEX")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_GSPC where count>0 order by date";
        }
        ma.refresh();
        if (sListMap.containsKey(rfType + " " + rfGroup)) {

            ShareList sl = sListMap.get(rfType + " " + rfGroup);
            int size = sl.getSize();
            for (int i = 0; i < size; i++) {
                ShareData sdO = sl.getSharedata(i);
                ShareData sd = new ShareData("", sdO.getDate(), ma.next(sdO.getClosePrice()), 0, 0, 0, 0);
                slistT.addShareData(sd);
            }
        } else {
            //synchronized(stmt){
                if (stmt == null || con.isClosed()) {
                    createConn();
                }
                Calendar c=Calendar.getInstance();
                ShareList sl = new ShareList();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    if (round) {
                        ShareData sd = new ShareData("", rs.getDate(1), ma.next(rs.getInt(2)), 0, 0, 0, 0);
                        slistT.addShareData(sd);
                        ShareData sdO = new ShareData("", rs.getDate(1), rs.getInt(2), 0, 0, 0, 0);
                        sl.addShareData(sdO);
                    } else {
                        try {
                            Date d=rs.getDate(1);
                            double val=rs.getDouble(2);
                            ShareData sd = new ShareData("", d, ma.next(val), 0, 0, 0, 0);
                            slistT.addShareData(sd);
                            ShareData sdO = new ShareData("", d, val, 0, 0, 0, 0);
                            sl.addShareData(sdO);
                        } catch (Exception ex) {
                            System.out.println(ex.toString());
                            for (int i = 0; i < ex.getStackTrace().length; i++) {
                                System.out.println(ex.getStackTrace()[i]);
                            }
                        }

                    }

                }
                sListMap.put((rfType + " " + rfGroup).intern(), sl);
                rs.close();
            //}
        }
        return slistT;
    }

    public synchronized ShareData getShareData(String rfType, String rfGroup, MA ma, Date dd)
            throws Exception {

        String Share = rfType + " " + rfGroup + " " + ma.getPeriod() + " DS";
        ShareData sd = null;
        rfType = getSys(rfType);
        String sql = "select date," + rfType + " from RisersFallersHistory where Date='" + sdf.format(dd) + "' order by date";
        //String sql="select date,"+rfType+" from RF_ASX where Date='"+sdf.format(dd)+"' order by date";
        if (rfGroup.equals("AllWithoutTrust")) {
            sql = "select date,(" + rfType + "/count)*100 from RisersFallersHistoryWithoutTrust where Date='" + sdf.format(dd) + "' order by date";
            //sql="select date,("+rfType+"/count)*100 from RF_ASX where Date='"+sdf.format(dd)+"' order by date";
        } else if (rfGroup.equals("INC")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date='"+sdf.format(dd)+"' order by date";
            sql = "select date,(" + rfType + "/count)*100 from RF_ASX where count>0 and Date='" + sdf.format(dd) + "' order by date";
        } else if (rfGroup.equals("EXC")) {
            //sql="select date,("+rfType+"/count)*100 from RisersFallersHistoryWithoutTrust where Date='"+sdf.format(dd)+"' order by date";
            sql = "select date,(" + rfType + "Ex/countEx)*100 from RF_ASX where count>0 and Date='" + sdf.format(dd) + "' order by date";
        } else if (rfGroup.equals("AllWithTrust")) {
            //sql="select date,("+tbl+"/count)*100 from RisersFallersHistoryWithTrust";
        } else if (rfGroup.equals("GSPC")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_GSPC where count>0 and Date='" + sdf.format(dd) + "' order by date";
        } else if (rfGroup.equals("OEX")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_OEX where count>0 and Date='" + sdf.format(dd) + "' order by date";
        } else if (rfGroup.equals("GSPC-OEX")) {
            sql = "select date,(" + rfType + "/count)*100 from RF_GSPC where count>0 and Date='" + sdf.format(dd) + "' order by date";
        }
        if (stmt == null || con.isClosed()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        Calendar c=Calendar.getInstance();
        ma.refresh();
        while (rs.next()) {
            sd = new ShareData("", rs.getDate(1), ma.next(rs.getDouble(2)), 0, 0, 0, 0);
        }
        rs.close();
        return sd;
    }

    public synchronized ShareList getShareDataFull(String Share)
            throws Exception {
        //stmt=con.createStatement();
        int count = 0;
        String sql = "Select count(sharedate) from shares where code='" + Share + "';";
        if (stmt == null || con.isClosed()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ShareList slist = new ShareList(count, Share.intern());
        sql = "Select sharedate,[close],high,low,[open],volume from shares where code='" + Share + "' order by sharedate;";
        rs = stmt.executeQuery(sql);
        Calendar c=Calendar.getInstance();
        while (rs.next()) {

            ShareData sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
            slist.addShareData(sd);
        }
        rs.close();
        return slist;
    }

    public synchronized ShareList getShareDataTill(String Share, Date end)
            throws Exception {
        //stmt=con.createStatement();
        int count = 0;
        //SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy");
        String sql = "Select count(sharedate) from shares where code='" + Share + "' and shares.ShareDate>'1/1/1995' and shares.ShareDate<='" + end + "';";
        if (stmt == null || con.isClosed()) {
            createConn();
        }
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ShareList slist = new ShareList(count, Share.intern());
        sql = "Select sharedate,[close],high,low,[open],volume from shares where code='" + Share + "' and shares.ShareDate>'1/1/1995' and shares.ShareDate<='" + end + "' order by sharedate;";
        rs = stmt.executeQuery(sql);
        Calendar c=Calendar.getInstance();
        while (rs.next()) {

            ShareData sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
            slist.addShareData(sd);
        }
        rs.close();
        return slist;
    }
    /*
     *
     * Get Data for share on a date
     */

    public synchronized ShareData getShareDataUKX(String Share, Date dd)
            throws Exception {
        //stmt=con.createStatement();
        int count = 0;
        String sql = "Select sharedate,[close],high,low,[open],volume from shares where code='" + Share + "' and sharedate='" + sdf.format(dd) + "';";
        //if(con.isClosed())
        //    stmt=con.createStatement();
        ResultSet rs;
        try {
            rs = stmt.executeQuery(sql);
        } catch (Exception ex) {
            createConn();
            rs = stmt.executeQuery(sql);
        }
        ShareData sd = null;
        Calendar c=Calendar.getInstance();
        while (rs.next()) {
            sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
        }
        rs.close();

        /*Calendar cal1=Calendar.getInstance();
        Calendar cal2=Calendar.getInstance();
        Calendar cal4=Calendar.getInstance();
        Calendar cal5=Calendar.getInstance();
        Calendar cal3=Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Date d=cal1.getTime();
        if(!dd.before(d))
        {
        cal1.set(Calendar.HOUR_OF_DAY, 16);
        cal1.set(Calendar.MINUTE, 30);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.HOUR_OF_DAY, 16);
        cal2.set(Calendar.MINUTE, 45);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        cal4.set(Calendar.HOUR_OF_DAY, 7);
        cal4.set(Calendar.MINUTE, 55);
        cal4.set(Calendar.SECOND, 0);
        cal4.set(Calendar.MILLISECOND, 0);
        cal5.set(Calendar.HOUR_OF_DAY, 8);
        cal5.set(Calendar.MINUTE, 0);
        cal5.set(Calendar.SECOND, 0);
        cal5.set(Calendar.MILLISECOND, 0);
        if(cal3.getTime().after(cal1.getTime()) && cal3.getTime().before(cal2.getTime()) || cal3.getTime().after(cal4.getTime()) && cal3.getTime().before(cal5.getTime()))
        {
        sql="Select XPrice from instrument where tidm='"+Share+"';";
        rs=stmtInst.executeQuery(sql);
        while(rs.next())
        sd.setClosePrice(rs.getDouble(1));
        }
        }*/
        return sd;
    }

    /*
     *
     * Get Data for share on a date
     */

    public synchronized ShareData getShareDataGSPC(String Share, Date dd)
            throws Exception {
        int count = 0;
        String sql = "Select sharedate,[close],high,low,[open],volume from sharesUS where code='" + Share + "' and sharedate='" + sdf.format(dd) + "';";
        ResultSet rs;
        try {
            rs = stmt.executeQuery(sql);
        } catch (Exception ex) {
            createConn();
            rs = stmt.executeQuery(sql);
        }
        ShareData sd = null;
        Calendar c=Calendar.getInstance();
        while (rs.next()) {
            sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
        }
        rs.close();
        return sd;
    }

    public synchronized ShareData getShareData(String Share, Date dd, String Index)
            throws Exception {
        ShareData sd = null;
        if (Index.equals("UKX")) {
            sd=getShareDataUKX(Share, dd);
        } else if(Index.equals("GSPC")){
            sd=getShareDataGSPC(Share, dd);
        }
        boolean done = false;
        if (Index.equals("UKX")) {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);
            Date d = cal1.getTime();
            if (!dd.before(d)) {
                cal1.set(Calendar.HOUR_OF_DAY, 16);
                cal1.set(Calendar.MINUTE, 30);
                cal1.set(Calendar.SECOND, 0);
                cal1.set(Calendar.MILLISECOND, 0);
                cal2.set(Calendar.HOUR_OF_DAY, 16);
                cal2.set(Calendar.MINUTE, 45);
                cal2.set(Calendar.SECOND, 0);
                cal2.set(Calendar.MILLISECOND, 0);
                cal4.set(Calendar.HOUR_OF_DAY, 7);
                cal4.set(Calendar.MINUTE, 55);
                cal4.set(Calendar.SECOND, 0);
                cal4.set(Calendar.MILLISECOND, 0);
                cal5.set(Calendar.HOUR_OF_DAY, 8);
                cal5.set(Calendar.MINUTE, 0);
                cal5.set(Calendar.SECOND, 0);
                cal5.set(Calendar.MILLISECOND, 0);
                if (cal3.getTime().after(cal1.getTime()) && cal3.getTime().before(cal2.getTime()) || cal3.getTime().after(cal4.getTime()) && cal3.getTime().before(cal5.getTime())) {
                    String sql = "Select XPrice from instrument where tidm='" + Share + "';";
                    if (stmtInst == null || conInst.isClosed()) {
                        createConn();
                    }
                    ResultSet rs = stmtInst.executeQuery(sql);
                    done = true;
                    while (rs.next()) {
                        Double val=rs.getDouble(1);
                        if(sd!=null && val!=null)
                            sd.setClosePrice(val);

                    }
                }
            }
        }
        return sd;
    }

    public synchronized ShareData getShareDataLower(String Share, Date dd)
            throws Exception {
        //stmt=con.createStatement();
        int count = 0;
        String sql = "Select top(1) sharedate,[close],high,low,[open],volume from shares where code='" + Share + "' and sharedate<'" + dd + "' order by sharedate desc;";
        ResultSet rs = stmt.executeQuery(sql);
        ShareData sd = null;
        Calendar c=Calendar.getInstance();
        while (rs.next()) {
            sd = new ShareData(Share.intern(), rs.getDate(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
        }
        rs.close();
        return sd;
    }

    public synchronized ArrayList getShares()
            throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        String sql = "Select code from shares group by code order by code;";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            arr.add(rs.getString(1));
        }
        rs.close();
        return arr;
    }

    public ArrayList getIndexShares(String index)
            throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        String sql = "Select distinct code from indices where [index]='" + index + "' order by code;";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            arr.add(rs.getString(1));
        }
        rs.close();
        return arr;
    }

    public ArrayList getIndexCombinedShares(String index)
            throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        String sql = "Select distinct code from indicescombined where [index]='" + index + "' order by code;";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            arr.add(rs.getString(1));
        }
        rs.close();
        return arr;
    }

    public ArrayList getShareGroups()
            throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        String sql = "Select distinct list from groups;";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            arr.add(rs.getString(1));
        }
        rs.close();
        return arr;
    }

    public ArrayList getBlockGroups()
            throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        String sql = "Select distinct [index] from Indices;";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            arr.add(rs.getString(1));
        }
        rs.close();
        return arr;
    }

    public ArrayList getSharesListQuery(String sql)
            throws Exception {
        ArrayList arr = new ArrayList();
        //stmt=con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            arr.add(rs.getString(1));

        }
        rs.close();
        return arr;
    }

    public synchronized void close() throws SQLException {
        if (con != null) {
            con.close();
        }
        if (conInst != null) {
            conInst.close();
        }
        //slist = null;
        con = null;
        stmt = null;
        conInst = null;
        stmtInst = null;
        //rs=null;
        System.gc();
    }

    public synchronized double getIndexXPrice(ShareList indSL, Date dd) throws SQLException {
        String sql = "SELECT      sum(InstrumentIssues.issue * Instrument.xprice) , "
                + "sum(InstrumentIssues.issue *Instrument.PrevClose)"
                + " FROM         Instrument INNER JOIN "
                + "InstrumentIssues ON Instrument.TIDM = InstrumentIssues.code;";
        ResultSet rs = stmtInst.executeQuery(sql);
        long mCapToday = 0;
        long mCapYest = 0;
        while (rs.next()) {
            mCapToday = rs.getLong(1);
            mCapYest = rs.getLong(2);
        }
        double yestPrice = indSL.getSharedata(indSL.isLowerDatePresent(dd)).getClosePrice();
        double indexDiv = mCapYest / yestPrice;
        return mCapToday / indexDiv;
    }

    public synchronized double getIndexLivePrice(ShareList indSL, Date dd) throws SQLException {
        String sql = "SELECT      sum(InstrumentIssues.issue * Instrument.mid) , "
                + "sum(InstrumentIssues.issue *Instrument.PrevClose)"
                + " FROM         Instrument INNER JOIN "
                + "InstrumentIssues ON Instrument.TIDM = InstrumentIssues.code;";
        ResultSet rs = stmtInst.executeQuery(sql);
        long mCapToday = 0;
        long mCapYest = 0;
        while (rs.next()) {
            mCapToday = rs.getLong(1);
            mCapYest = rs.getLong(2);
        }
        double yestPrice = indSL.getSharedata(indSL.isLowerDatePresent(dd)).getClosePrice();
        double indexDiv = mCapYest / yestPrice;
        return mCapToday / indexDiv;
    }

    public synchronized DividendList getDividendData(String Share, Date dt1, Date dt2) throws Exception {
        int count = 10;
        String sql1 = null, sql2 = null;

        if (dt1 != null && dt2 != null) {
            /* Select count(count1) count from (
            Select count(DivDate) count1 from dividend
            where code='AAL' and dividend.DivDate>='01/01/2005' and dividend.DivDate<='01/01/2008'
            group by DivDate
            ) as dividend_Alias */
            sql1 = "Select count(count1) count from ( ";
            sql1 = sql1 + "Select count(DivDate) count1 from dividend where code='" + Share + "' and dividend.DivDate>='" + dt1 + "' and dividend.DivDate<='" + dt2 + "' group by DivDate ";
            sql1 = sql1 + ") as dividend_Alias ; ";
            /* Select DivDate, sum(DivAmt) DivAmt from dividend
            where code='AAL' and dividend.DivDate>='01/01/2005' and dividend.DivDate<='01/01/2008'
            group by DivDate
            order by DivDate */
            sql2 = "Select DivDate, sum(DivAmt) DivAmt from dividend where code='" + Share + "' and dividend.DivDate>='" + dt1 + "' and dividend.DivDate<='" + dt2 + "' group by DivDate order by DivDate ; ";
        }

        //ResultSet rs = stmt.executeQuery(sql1);
        //while (rs.next()) {
        //    count = rs.getInt(1);
        //}
        //debug.sop("sql1 count " + count);
        //rs.close();
        DividendList divlist = new DividendList(count, Share.intern());
        Calendar c=Calendar.getInstance();
        ResultSet rs = stmt.executeQuery(sql2);
        while (rs.next()) {
            DividendData dd = new DividendData(Share.intern(), rs.getDate(1), rs.getDouble(2));
            divlist.addDividendData(dd);
        }
        //debug.sop("slist " + slist.getSize());
        rs.close();
        return divlist;
    }

    public synchronized DividendList getDividendData(String Share,String index) throws Exception {
        int count = 0;
        String sql1 = null, sql2 = null;
        String dtable="US";
        if(index.equals("UKX"))
            dtable="";
        sql1 = "Select count(count1) count from ( ";
        sql1 += "Select count(DivDate) count1 from dividend"+dtable+" where code='" + Share + "' group by DivDate ";
        sql1 += ") as dividend_Alias ; ";

        sql2 = "Select DivDate, sum(DivAmt) DivAmt from dividend"+dtable+" where code='" + Share + "' group by DivDate order by DivDate ; ";
        if(stmt==null)
            createConn();
        ResultSet rs = stmt.executeQuery(sql1);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        //debug.sop("sql1 count " + count);
        rs.close();
        DividendList divlist = new DividendList(count, Share.intern());
        Calendar c=Calendar.getInstance();
        rs = stmt.executeQuery(sql2);
        while (rs.next()) {
            DividendData dd = new DividendData(Share.intern(), rs.getDate(1), rs.getDouble(2));
            divlist.addDividendData(dd);
        }
        //debug.sop("slist " + slist.getSize());
        rs.close();
        return divlist;
    }

    private synchronized void createConn() throws Exception {
        //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://maxpc;user=BackTradeData;password=oracle");
        //con = DriverManager.getConnection("jdbc:odbc:JEYSQL","BackTradeData","oracle");
            //Class.forName("net.sourceforge.jtds.jdbc.Driver");
            conInst = DriverManager.getConnection("jdbc:jtds:sqlserver://jeysql;user=Interactive;password=oracle");
            //stmt=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt = con.createStatement();
            stmtInst = conInst.createStatement();
            online = true;
        } catch (Exception ex) {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String database
                    = "jdbc:odbc:backs";
            con = DriverManager.getConnection(database, "", "");
            stmt = con.createStatement();
            stmtInst = con.createStatement();

            online = false;
        }
    }
    private Connection getConn() throws Exception {
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        return DriverManager.getConnection("jdbc:jtds:sqlserver://maxpc;user=BackTradeData;password=oracle");

    }
    
    

    public boolean checkConnQ() {
        boolean ret = false;
        try {
            ResultSet rows = stmt.executeQuery("Select 1");
            if (rows.next()) {
                ret = true;
            } else {
                ret = false;
            }
        } catch (SQLException ex) {
            ret = false;
        }
        return ret;
    }

    @Override
    protected void finalize() throws Throwable {

        con.close();
        conInst.close();
        sListMap.clear();
        sListMap=null;
        super.finalize();

    }

    private String getSys(String rfType) {

        if (rfType.equals("Sys A")) {
            return "YearHigh";
        }
        else if (rfType.equals("Sys B")) {
            return "YearLow";
        }
        else if (rfType.equals("Sys C")) {
            return "Risers3Day";
        }
        else if (rfType.equals("Sys D")) {
            return "Fallers3Day";
        }
        else if (rfType.equals("Sys E")) {
            return "Risers4Day";
        }
        else if (rfType.equals("Sys F")) {
            return "Fallers4Day";
        }
        else if (rfType.equals("Sys G")) {
            return "Static3DUp";
        }
        else if (rfType.equals("Sys H")) {
            return "Static3DDown";
        }
        else if (rfType.equals("Sys I")) {
            return "Static4DUp";
        }
        else if (rfType.equals("Sys J")) {
            return "Static4DDown";
        }
        else if (rfType.equals("Sys K")) {
            return "Bullish";
        }
        else if (rfType.equals("Sys L")) {
            return "Bearish";
        }
        else if (rfType.equals("Sys M")) {
            return "Risers1Day";
        }
        else if (rfType.equals("Sys N")) {
            return "Fallers1Day";
        }
        else if (rfType.equals("Sys O")) {
            return "([count]-(Risers1Day+Fallers1Day))";
        }
        else if (rfType.equals("Sys P")) {
            return "(Risers1Day*[count]/Fallers1Day)";
        }
        else if (rfType.equals("Sys Q")) {
            return "(Risers1Day*[count]/([count]-Risers1Day))";
        }
        else if (rfType.equals("Sys R")) {
            return "Risers1DayPer";
        }
        else if (rfType.equals("Sys S")) {
            return "Fallers1DayPer";
        }
        else if (rfType.equals("Sys T")) {
            return "([count]-(Risers1DayPer+Fallers1DayPer))";
        }
        else if (rfType.equals("Sys U")) {
            return "(Risers1DayPer*[count]/Fallers1DayPer)";
        }
        else if (rfType.equals("Sys V")) {
            return "(Risers1DayPer*[count]/([count]-Risers1DayPer))";
        }
        else if (rfType.equals("Sys W")) {
            return "Risers1DayEx*[count]/[countex]";
        }
        else if (rfType.equals("Sys X")) {
            return "Fallers1DayEx*[count]/[countex]";
        }
        else if (rfType.equals("Sys Y")) {
            return "([countEX]-(Risers1DayEX+Fallers1DayEX))*[count]/[countex]";
        }
        else if (rfType.equals("Sys Z")) {
            return "(Risers1DayEX*[count]/Fallers1DayEX)";
        }
        else if (rfType.equals("Sys AA")) {
            return "(Risers1DayEx*[count]/([countEx]-Risers1DayEx))";
        }
        else if (rfType.equals("Sys AB")) {
            return "Risers1DayExPer*[count]/[countex]";
        }
        else if (rfType.equals("Sys AC")) {
            return "Fallers1DayExPer*[count]/[countex]";
        }
        else if (rfType.equals("Sys AD")) {
            return "([countEX]-(Risers1DayEXPer+Fallers1DayEXPer))*[count]/[countex]";
        }
        else if (rfType.equals("Sys AE")) {
            return "(Risers1DayEXPer*[count]/Fallers1DayEXPer)";
        }
        else if (rfType.equals("Sys AF")) {
            return "(Risers1DayExPer*[count]/([countEx]-Risers1DayExPer))";
        }
        else if (rfType.equals("Sys AG")) {
            return "(RSI*[count]/100)";
        }
        return "";
    }

    public String getTable(String index) {
        String table="Shares";
        if(index.equals("ASX"))
            table="SharesComplete";
        else if(index.equals("GSPC"))
            table="SharesUS";
        else if(index.equals("OEX"))
            table="SharesUS";
        return table;
    }

    public Date lastUpdate(String index) throws SQLException{
        String ind="ASX";
        if(index.equals("GSPC")){
            ind="US";
        }
        String sql="select max(sharedate) from sharestick"+ind+" where code!='GSPC'";

        Date d=new Date();
        ResultSet rs = stmt.executeQuery(sql);
        if(rs.next())
            d=rs.getTimestamp(1);
        return d;
    }

    public ResultList getResultsData(String sh, String index) throws Exception {
        String sql2 = null;
        String dtable="US";
        if(index.equals("UKX"))
            dtable="";
        sql2 = "Select date,type,eps from Results"+dtable+" where code='" + sh + "' order by date ; ";
        if(stmt==null)
            createConn();
        ResultList divlist = new ResultList(sh.intern());
        ResultSet rs = stmt.executeQuery(sql2);
        while (rs.next()) {
            ResultData rd = new ResultData(rs.getDate(1), rs.getString(2),rs.getDouble(3));
            divlist.addResult(rd);
        }
        rs.close();
        return divlist;
    }

    public synchronized SuperSect getSuperSectors(String sector,String index) throws Exception {
        ResultSet rs = null;
        SuperSect superSect = new SuperSect();
        StringBuilder query = new StringBuilder();
        String table = "backtrade.dbo.shareSectors";

        query.append("select code, ").append(sector).append(" from ").append(table).append(" WITH(NOLOCK) ");
        query.append("  where code in (select distinct code from backtrade.dbo.indices WITH(NOLOCK) where [index]='").append(index).append("') ");
        query.append("  and [index]='").append(index).append("' ");
        query.append("  and supersector is not null ");
        query.append("  order by supersector, code");

        //debug.sopWC(query.toString());
        rs = stmt.executeQuery(query.toString());

        while(rs.next()){
            superSect.add(rs.getString(sector), rs.getString("code"));
        }
        rs.close();
        return superSect;
    }

    public synchronized ShareList getCurrencyData(String share) throws Exception {
        String[] cname = share.split(",");
        int mins = getMins(cname[1]);
        String Share=cname[0];
        String sh=share;
        String table ="ForexAll";
        if(cname[1].equals("1M")){
            sh=Share;
            table="Forex";
        }
        ResultSet r=stmt.executeQuery("Select count(*) from Forex.dbo.["+table+"] where code='"+sh+"'");
        int cnt=4000;
        if(r.next())
            cnt=r.getInt(1);
        if(cnt==0){
            String sql = "insert into Forex.dbo.["+table+"](datetime,code,openprice,high,low,closeprice,volume) "
                    + "select grp.mdate,'"+sh+"',f.openprice,grp.high,grp.low,l.closeprice,0 from "
                + "  (SELECT  min(code) as code,min([Datetime]) as mdate,max([Datetime]) as ldate, min(low) as low, max(high) as high"
                + "  FROM Forex.dbo.[Forex] where code='"+Share+"' "
                + "  group by CONVERT(VARCHAR(6), datetime, 12),DATEDIFF(MINUTE, CONVERT(date, datetime), datetime)/("+mins+")) as grp inner join "
                + "  (select DateTime, OpenPrice from Forex.dbo.[Forex] where code='"+ Share +"' ) as f on f.datetime=grp.mdate inner join"
                + "  (select DateTime, ClosePrice from Forex.dbo.[Forex] where code='"+ Share +"' ) as l on l.datetime=grp.ldate"
                + "  order by grp.mdate";
            cnt=stmt.executeUpdate(sql);
        }
        ShareList slist = new ShareList(cnt, share.intern());
        String sql="select datetime,closeprice,high,low,openprice,volume from Forex.dbo.["+table+"] where code='"+sh+"' order by datetime;";
//        String sql = "select grp.mdate,l.closeprice,grp.high,grp.low,f.openprice,0 from "
//                + "  (SELECT  min(code) as code,min([Datetime]) as mdate,max([Datetime]) as ldate,min(low) as low, max(high) as high"
//                + "  FROM Forex.dbo.[Forex] where code='"+Share+"' "
//                + "  group by CONVERT(VARCHAR(6), datetime, 12),DATEDIFF(MINUTE, CONVERT(date, datetime), datetime)/("+mins+")) as grp inner join "
//                + "  (select DateTime, OpenPrice from Forex.dbo.[Forex] where code='"+ Share +"' ) as f on f.datetime=grp.mdate inner join"
//                + "  (select DateTime, ClosePrice from Forex.dbo.[Forex] where code='"+ Share +"' ) as l on l.datetime=grp.ldate"
//                + "  order by grp.mdate";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            ShareData sd = new ShareData(share.intern(), rs.getTimestamp(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getLong(6));
            slist.addShareData(sd);
        }
        rs.close();
        return slist;
    }
    private int getMins(String str) {
        int ret=1;
        switch(str){
            case "1M":
                break;
            case "15M":
                ret = 15;
                break;
            case "1H":
                ret=60;
                break;
            case "4H":
                ret=240;
                break;
            case "1D":
                ret=1440;
                break;
            case "1W":
                ret=1440;
        }
        return ret;
    }

    public void storeInd(IndicatorList il, ShareList sl, AbstractIndicator ind) throws Exception {
        Connection conn=getConn();
        SimpleDateFormat sdfT=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        //Statement stmt2=conn.createStatement();
//        stmt2.executeUpdate("CREATE TABLE #forexins("
//                + "	[Indicator] [nvarchar](50) NOT NULL,"
//                + "	[Code] [nvarchar](10) NOT NULL,"
//                + "	[Date] [datetime] NOT NULL,"
//                + "	[Price] [numeric](18, 6) NOT NULL,"
//                + "	[Value] [numeric](18, 6) NOT NULL,"
//                + "	[Signal] [numeric](18, 6) NULL"
//                + "	)");
        conn.setAutoCommit(false);
        StringBuilder sb=new StringBuilder();
        Statement stmt1=conn.createStatement();
        //PreparedStatement stmt1 = conn.prepareStatement("insert into Forex.dbo.forexidl(indicator,date,code,[price],value,signal) values(?,?,?,?,?,?)");
        int slstart=sl.isDatePresent( il.getSharedata(0).getDDate());
        for (int i = 0; i < il.getSize(); i++) {
            IndicatorField indf=il.getSharedata(i);
            ShareData sd = sl.getSharedata(i+slstart);
            
            sb.append("insert into Forex.dbo.forexidl (indicator,date,code,[price],value,signal) values(");
            sb.append("'").append(ind.getShortName()).append("',");
            sb.append("'").append(sdfT.format(sd.getDate())).append("',");
            sb.append("'").append(sl.getShare()).append("',");
            sb.append(sd.getClosePrice()).append(",");
            sb.append(Double.isNaN(indf.getValue()) ? 0.0 : indf.getValue()).append(",");
            sb.append(Double.isNaN(indf.getSignal()) ? 0.0 : indf.getSignal()).append(");");
            
//            stmt1.setString(1, ind.getShortName());
//            stmt1.setTimestamp(2, new java.sql.Timestamp(sd.getDateLong()));
//            stmt1.setString(3, sl.getShare());
//            stmt1.setDouble(4, sd.getClosePrice());
//            stmt1.setDouble(5, Double.isNaN(indf.getValue()) ? 0.0 : indf.getValue());
//            stmt1.setDouble(6, Double.isNaN(indf.getSignal()) ? 0.0 : indf.getSignal());
//            stmt1.addBatch();
            if(i%10000==0 && i>0){
                try {
//                    int[] ret = stmt1.executeBatch();
//                    stmt1.clearParameters();
                    //Date d=new Date();
                    stmt1.executeUpdate(sb.toString());
                    sb=new StringBuilder();
                    conn.commit();
                    //System.out.println((new Date().getTime()-d.getTime())/(1000)+","+d);
                } catch (Exception ex) {
                    Logger.getLogger(BasicShareDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        stmt1.executeUpdate(sb.toString());
        //int[] ret = stmt1.executeBatch();
        conn.commit();
        //stmt2.executeUpdate("insert into Forex.dbo.forexidl select * from #forexins");
        //stmt2.executeUpdate("drop table  #forexins");
        //conn.commit();
        conn.close();
    }

    public synchronized boolean isCurr(String curr, String shortName) throws SQLException {
        Statement stmt1=con.createStatement();
        ResultSet rs=stmt1.executeQuery("select top 1 1 from Forex.dbo.ForexIDL with(nolock) where code='"+curr+"' and indicator='"+shortName+"'");
        if(rs.next())
            return rs.getInt(1)!=0;
        else
            return false;
    }

    public TradeList getThresholdOpen(String cur, String indicator, String value, boolean buy,boolean insert,String id) throws Exception {
        TradeList tl=new TradeList();
        Connection conn=getConn();
        Statement stmt1=conn.createStatement();
        
//        String sql = "select a.date,a.price from "
//                + "  (select rank() over(partition by indicator,code order by date ) as r ,* from Forex.dbo.forexidl where code='"+cur+"' and indicator='"+shortName+"' and value"+(buy?">":"<")+"="+value+") as a inner hash join "
//                + "  (select rank() over(partition by indicator,code order by date ) as r ,* from Forex.dbo.forexidl where code='"+cur+"' and indicator='"+shortName+"' and value"+(buy?">":"<")+value+") as b on a.Code=b.code and a.indicator=b.indicator where a.r-b.r=1" 
//                + "  order by a.date";
        long tid=0;
        try{
        stmt1.executeUpdate("Insert into Forex.dbo.TradeIds(trdkey,currency) values('"+id+"','"+cur.split(",")[0]+"');", Statement.RETURN_GENERATED_KEYS);
        ResultSet r= stmt1.getGeneratedKeys();
        if(r.next())
            tid=r.getLong(1);
        }catch(SQLException se){
            stmt1.executeQuery("select id from Forex.dbo.TradeIds where trdkey='" + id + "' and currency='" + cur.split(",")[0] + "');");
            ResultSet r = stmt1.getResultSet();
            if (r.next()) {
                tid = r.getLong(1);
            }
            return tl;
        }
        conn.setAutoCommit(false);
        String sql = "";//  select  * into #tforex from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "'; ";
        if (insert) {
            sql += "insert into Forex.dbo.forexTrades(trdid,opendate,openprice)";
        }
        sql += "  select  " + tid + ",a.date,a.price from "
                + "  (select  * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "' and  value" + (buy ? ">" : "<") + "=" + value + ") as a inner hash join"
                + "   (select * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "' and  value" + (buy ? "<" : ">") + value + ") as b on a.Code=b.code and a.indicator=b.indicator"
                + "	where a.r=b.r+1 order by a.date;";
                //+ "	drop table #tforex;";
//        if (insert) {
//            sql += "Select opendate,openprice from Forex.dbo.forexTrades where trdid='" + id + "';";
//        }
        if (!insert) {
            ResultSet rs = stmt1.executeQuery(sql);
            while (rs.next()) {
                Date open = rs.getTimestamp(1);
                double price = rs.getDouble(2);
                Trade trd = new Trade(open, price, null, 0.0);
                trd.setShare(cur);
                tl.addTrade(trd);
            }
            rs.close();
        } else{
            stmt1.executeUpdate(sql);
        }
        conn.commit();
        conn.close();
        return tl;
    }

    public TradeList getSignalOpen(String cur, String shortName, boolean buy,boolean insert,String id) throws Exception {
        TradeList tl=new TradeList();
        Connection conn=getConn();
        Statement stmt1=conn.createStatement();
//        String sql = "select a.date,a.price from "
//                + "  (select rank() over(partition by indicator,code order by date ) as r ,* from Forex.dbo.forexidl where code='"+cur+"' and indicator='"+shortName+"' and value"+(buy?">":"<")+"="+value+") as a inner hash join "
//                + "  (select rank() over(partition by indicator,code order by date ) as r ,* from Forex.dbo.forexidl where code='"+cur+"' and indicator='"+shortName+"' and value"+(buy?">":"<")+value+") as b on a.Code=b.code and a.indicator=b.indicator where a.r-b.r=1" 
//                + "  order by a.date";
        long tid=0;
        try{
        stmt1.executeUpdate("Insert into Forex.dbo.TradeIds(trdkey,currency) values('"+id+"','"+cur.split(",")[0]+"');", Statement.RETURN_GENERATED_KEYS);
        ResultSet r= stmt1.getGeneratedKeys();
        if(r.next())
            tid=r.getLong(1);
        }catch(SQLException se){
            stmt1.executeQuery("select id from Forex.dbo.TradeIds where trdkey='" + id + "' and currency='" + cur.split(",")[0] + "');");
            ResultSet r = stmt1.getResultSet();
            if (r.next()) {
                tid = r.getLong(1);
            }
            return tl;
        }
        conn.setAutoCommit(false);
        String sql = "";//  select  * into #tforex from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "'; ";
        if (insert) {
            sql += "insert into Forex.dbo.forexTrades(trdid,opendate,openprice)";
        }
        sql += //"  select  * into #tforex from Forex.dbo.forexIDLV where code='"+cur+"' and indicator='"+shortName+"';"
                "  select  " + tid + ",a.date,a.price from "
                + "  (select  * from #tforex where value"+(buy?">":"<")+"=signal) as a inner hash join"
                + "   (select * from #tforex where value"+(buy?">":"<")+"signal) as b on a.Code=b.code and a.indicator=b.indicator"
                + "	where a.r=b.r+1 order by a.date;";
                //+ "	drop table #tforex;";
        ResultSet rs=stmt1.executeQuery(sql);
        while(rs.next()){
            Date open=rs.getTimestamp(1);
            double price=rs.getDouble(2);
            Trade trd=new Trade(open, price, null, 0.0);
            trd.setShare(cur);
            tl.addTrade(trd);
        }
        rs.close();
        conn.commit();
        conn.close();
        return tl;
    }
    
    public boolean isOpenAvailable(String curr, String indicator, String opens,boolean buy) throws Exception {
        Statement stmt1=con.createStatement();
        String id=(buy?"Buy":"Sell") +";"+curr.split(",")[1]+";"+indicator+";"+opens;
        ResultSet rs=stmt1.executeQuery("select id from Forex.dbo.TradeIds with (nolock) where trdkey = '"+id+"' and currency='"+curr.split(",")[0]+"'");
        if(rs.next()){
            return rs.getInt(1)!=0;
        }
        else
            return false;
    }

    public boolean isOpenAvailable(String curr, String id) throws Exception {
        Statement stmt1=con.createStatement();
        //String id=(buy?"Buy":"Sell") +";"+curr.split(",")[1]+";"+indicator+";"+opens;
        ResultSet rs=stmt1.executeQuery("select id from Forex.dbo.TradeIds with (nolock) where trdkey = '"+id+"' and currency='"+curr.split(",")[0]+"'");
        if(rs.next()){
            int dbid=rs.getInt(1);
            rs=stmt1.executeQuery("select count(1) from Forex.dbo.forextrades where trdid="+dbid+"");
            if(rs.next())
                return rs.getInt(1)!=0;
            else
                return false;
        }
        else
            return false;
    }
    
    public void storeTradesLocal(TradeList openTL, String id) throws Exception {
        try {
            String cur=openTL.getTrade(0).getShare();
            openTL.setName(id);
            openTL.setInd(cur);
            if (jongo == null) {
                connectMongo();
            }
            MongoCollection collJ = jongo.getCollection("ForexTrades");
            collJ.withWriteConcern(WriteConcern.UNACKNOWLEDGED);
            String st=JsonWriter.objectToJson(openTL);
            collJ.insert(st);
        } catch (Exception ex) {
            Logger.getLogger(BasicShareDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //cur.close();
        }
    }

    public void storeTrades(TradeList openTL, String id) throws Exception {
        if(openTL.getSize()==0)
            return;
        Connection conn=getConn();
//        stmt2.executeUpdate("CREATE TABLE #forexTrade("
//                + "	[TrdId] [nvarchar](500) NOT NULL,"
//                + "	[code] [nvarchar](10) NOT NULL,"
//                + "	[OpenDate] [datetime] NOT NULL,"
//                + "	[OpenPrice] [numeric](18, 6) NULL,"
//                + "	[CloseDate] [datetime] NULL," 
//                + "	[ClosePrice] [numeric](18, 6) NULL,"
//                + "	)");
        Statement stmt2=conn.createStatement();
        long tid=0;
        String cur=openTL.getTrade(0).getShare();
        stmt2.executeUpdate("Insert into Forex.dbo.TradeIds(trdkey,currency) values('"+id+"','"+cur.split(",")[0]+"');", Statement.RETURN_GENERATED_KEYS);
        ResultSet r= stmt2.getGeneratedKeys();
        if(r.next())
            tid=r.getLong(1);
        conn.setAutoCommit(false);
        
        PreparedStatement stmt1 = conn.prepareStatement("insert into Forex.dbo.forexTrades(trdid,opendate,openprice,closedate,closeprice) values(?,?,?,?,?)");
        for (int i = 0; i < openTL.getSize(); i++) {
            Trade trd=openTL.getTrade(i);
            stmt1.setLong(1, tid);
            stmt1.setTimestamp(2, new java.sql.Timestamp(trd.getStartDate().getTime()));
            stmt1.setDouble(3, trd.getStartPrice());
            if (trd.getCloseDate() != null) {
                stmt1.setTimestamp(4, new java.sql.Timestamp(trd.getCloseDate().getTime()));
                stmt1.setDouble(5, trd.getClosePrice());
            } else{
                stmt1.setTimestamp(4, null);
                stmt1.setDouble(5, 0.0);
            }
            stmt1.addBatch();
            if(i%1000==0 && i>0){
                try {
                    int[] ret = stmt1.executeBatch();
                    stmt1.clearParameters();
                } catch (Exception ex) {
                    Logger.getLogger(BasicShareDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        int[] ret = stmt1.executeBatch();
        conn.commit();
//        String updateStats = "insert into forex.dbo.tradestats "
//                + "		select trdid,sum(prft),sum(cnt),sum(prft)/sum(cnt),1,1 from "
//                + "		(select trdid,year(opendate) as yr,sum((openprice-ClosePrice)*100/openprice) as prft,count(*) as cnt from forex.dbo.forextrades with (nolock) where trdid =" + tid + " group by trdid,year(opendate) ) as a  group by trdid";
//        stmt2.executeUpdate(updateStats);
//        stmt2.executeUpdate("insert into Forex.dbo.forexTrades select * from #forexTrade;");
//        stmt2.executeUpdate("drop table  #forexTrade;");
        conn.commit();
        conn.close();
    }
    
    public void storeTrades(TradeList openTL, String id,String cur) throws Exception {
        if(openTL.getSize()==0)
            return;
        Connection conn=getConn();
//        stmt2.executeUpdate("CREATE TABLE #forexTrade("
//                + "	[TrdId] [nvarchar](500) NOT NULL,"
//                + "	[code] [nvarchar](10) NOT NULL,"
//                + "	[OpenDate] [datetime] NOT NULL,"
//                + "	[OpenPrice] [numeric](18, 6) NULL,"
//                + "	[CloseDate] [datetime] NULL," 
//                + "	[ClosePrice] [numeric](18, 6) NULL,"
//                + "	)");
        Statement stmt2=conn.createStatement();
        long tid=0;
        stmt2.executeUpdate("Insert into Forex.dbo.TradeIds(trdkey,currency) values('"+id+"','"+cur.split(",")[0]+"');", Statement.RETURN_GENERATED_KEYS);
        ResultSet r= stmt2.getGeneratedKeys();
        if(r.next())
            tid=r.getLong(1);
        conn.setAutoCommit(false);
        
        PreparedStatement stmt1 = conn.prepareStatement("insert into Forex.dbo.forexTrades(trdid,opendate,openprice,closedate,closeprice) values(?,?,?,?,?)");
        for (int i = 0; i < openTL.getSize(); i++) {
            Trade trd=openTL.getTrade(i);
            stmt1.setLong(1, tid);
            stmt1.setTimestamp(2, new java.sql.Timestamp(trd.getStartDate().getTime()));
            stmt1.setDouble(3, trd.getStartPrice());
            if (trd.getCloseDate() != null) {
                stmt1.setTimestamp(4, new java.sql.Timestamp(trd.getCloseDate().getTime()));
                stmt1.setDouble(5, trd.getClosePrice());
            } else{
                stmt1.setTimestamp(4, null);
                stmt1.setDouble(5, 0.0);
            }
            stmt1.addBatch();
            if(i%1000==0 && i>0){
                try {
                    int[] ret = stmt1.executeBatch();
                    stmt1.clearParameters();
                } catch (Exception ex) {
                    Logger.getLogger(BasicShareDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        int[] ret = stmt1.executeBatch();
        conn.commit();
//        String updateStats = "insert into forex.dbo.tradestats "
//                + "		select trdid,sum(prft),sum(cnt),sum(prft)/sum(cnt),1,1 from "
//                + "		(select trdid,year(opendate) as yr,sum((openprice-ClosePrice)*100/openprice) as prft,count(*) as cnt from forex.dbo.forextrades with (nolock) where trdid =" + tid + " group by trdid,year(opendate) ) as a  group by trdid";
//        stmt2.executeUpdate(updateStats);
//        stmt2.executeUpdate("insert into Forex.dbo.forexTrades select * from #forexTrade;");
//        stmt2.executeUpdate("drop table  #forexTrade;");
        conn.commit();
        conn.close();
    }

    public TradeList getThresholdClose(String cur, String indicator, String close, boolean buy, boolean insert, String id,TradeList opens) throws Exception {
        Connection conn=getConn();
        String[] cl=close.split(",");
        String value=cl[1];
        String dcut=cl[2];
        Statement stmt1=conn.createStatement();
        conn.setAutoCommit(false);
        //String sql = "  select  * into #tforex from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "'; ";
        String sql = " select  a.date,a.price,a.r from   "
                + "	  (select  * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "' and value" + (buy ? "<" : ">") + "=" + value + ") as a inner hash join "
                + "	   (select * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + indicator + "' and value" + (buy ? ">" : "<") + "" + value + ") as b "
                + "		on a.Code=b.code and a.indicator=b.indicator"
                + "		where a.r=b.r+1;";
                //+ "	drop table #tforex;";
        ResultSet rs = stmt1.executeQuery(sql);
        TreeMap<Date,Double> doneTrades = new TreeMap<>();
        while (rs.next()) {
            Date open = rs.getTimestamp(1);
            double price = rs.getDouble(2);
            if (!doneTrades.containsKey(open)) {
                doneTrades.put(open,price);
            }
        }
        conn.commit();
        rs.close();
        conn.close();
        for(int i=0;i<opens.getSize();i++){
            Trade trd=opens.getTrade(i);
            Date d=doneTrades.higherKey(trd.getStartDate());
            if(d!=null){
                trd.setCloseDate(d);
                trd.setClosePrice(doneTrades.get(d));
            }
        }
        if(insert){
            storeTrades(opens, id);
        }
        return opens;
    }

    public TradeList getSignalClose(String cur, String indicator, String close, boolean buy, boolean insert, String id,TradeList opens) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public TradeList getCurrencyTradesLocal(String id,String curr) throws Exception {
        TradeList tl=new TradeList();
        try {
            if (jongo == null) {
                connectMongo();
            }
            //MongoCollection collJ = jongo.getCollection("ForexTrades");
            DBObject dbo = (DBObject) JSON.parse("{name:'"+id+"', ind:'"+curr+"'}");
            String st=jongo.getDatabase().getCollection("ForexTrades").findOne(dbo).toString();
            //String st=collJ.findOne("{name:#, ind:#}",id,curr).toString();
            tl=(TradeList) JsonReader.jsonToJava(st);
        } catch (Exception ex) {
            Logger.getLogger(BasicShareDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //cur.close();
            //mongoClient.close();
        }
        return tl;
    }
    
    public TradeList getCurrencyTrades(String id,String curr) throws Exception {
        TradeList tl=new TradeList();
        Connection conn=getConn();
        Statement stmt1=conn.createStatement();
        if(curr.isEmpty()){
            String sql = "Select opendate,openprice,closedate,closeprice,currency from Forex.dbo.forexTrades with (nolock) inner join Forex.dbo.tradeids on tradeids.id=TrdId  where closedate is not null and trdkey='" + id + "' order by opendate;";

            ResultSet rs = stmt1.executeQuery(sql);
            while (rs.next()) {
                //String cur=rs.getString(1);
                Date open = rs.getTimestamp(1);
                double price = rs.getDouble(2);
                Date cls = rs.getTimestamp(3);
                double clsprice = rs.getDouble(4);
                String c=rs.getString(5);
                Trade trd = new Trade(open, price, cls, clsprice);
                trd.setShare(c);
                tl.addTrade(trd);
            }
            rs.close();
        } else {
            ResultSet r = stmt1.executeQuery("Select id from Forex.dbo.TradeIds where trdkey='" + id + "' and currency='" + curr.split(",")[0] + "';");
            long tid = 0;
            if (r.next()) {
                tid = r.getLong(1);
            }

            String sql = "Select opendate,openprice,closedate,closeprice from Forex.dbo.forexTrades with (nolock) where trdid=" + tid + " order by opendate;";

            ResultSet rs = stmt1.executeQuery(sql);
            while (rs.next()) {
                //String cur=rs.getString(1);
                Date open = rs.getTimestamp(1);
                double price = rs.getDouble(2);
                Date cls = rs.getTimestamp(3);
                double clsprice = rs.getDouble(4);
                Trade trd = new Trade(open, price, cls, clsprice);
                trd.setShare(curr);
                tl.addTrade(trd);
            }
            rs.close();
        }
        
        conn.close();
        return tl;
    }

    public boolean isCloseAvailable(String id,String cur,boolean sqls) throws SQLException {
        if (sqls) {
            Statement stmt1 = con.createStatement();
            String sql = "select id from Forex.dbo.TradeIds with (nolock) where trdkey = '" + id + "' and currency='" + cur.split(",")[0] + "';";
            ResultSet rs = stmt1.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1) != 0;
            } else {
                return false;
            }
        } else { /*
            long cnt = 0;
            try {
                if (jongo == null) {
                    connectMongo();
                }
                MongoCollection collJ = jongo.getCollection("ForexTrades");
                cnt = collJ.count("{name:#, ind:#}", id, cur);
                if(cnt==0){
                    collJ = jongo.getCollection("Summary");
                    cnt = collJ.count("{name:#, share:#}", id, cur);
                }
            } catch (Exception ex) {
                Logger.getLogger(BasicShareDB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                //cur.close();
                //mongoClient.close();
                return cnt > 0;
            }*/
            return false;
        }
    }

    public TradeList getCurrencyTrades(String id,String curr, String filterTime,String openFilter) throws Exception {
        TradeList tl=new TradeList();
        Connection conn=getConn();
        conn.setAutoCommit(false);
        Statement stmt2=conn.createStatement();
        String[] filter=null;
        String ema=null;
        int ind=0,cnt=0;
        double rsival=30;
        if(openFilter.contains("|")){
            filter=openFilter.split("\\|");
            for(String str:filter){
                if(str.contains("EMA"))
                {
                    ema=str;
                    cnt=ind;
                    break;
                }
                ind++;
            }
        } else{
            filter=new String[1];
            filter[0]=openFilter;
            ema=openFilter;
        }
        String[] tb=filterTime.split(",");
        String[] cr=curr.split(",");
        String buy=tb[1];
        String cur=cr[0]+","+tb[0];
        ResultSet r=stmt2.executeQuery("Select id from Forex.dbo.TradeIds where trdkey='"+id+"' and currency='"+ cr[0] +"';");
        long tid=0;
        if(r.next())
            tid=r.getLong(1);
//        String sql = "  select  * into #tforex from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + openFilter + "';"
//                + "  select  t.opendate,t.openprice from "
//                + "  (select opendate,openprice,r,date from "
//                + "			   (select * from Forex.dbo.forexTrades where trdid='" + id + "') as ta inner hash join "
//                + "			   (select  * from #tforex) as tb on CONVERT(VARCHAR(10),tb.date,3) = convert(varchar(10),ta.OpenDate,3)) as t inner hash join"
//                + "			     (select  * from #tforex ) as a on a.r=t.r-1 inner hash join"
//                + "   (select * from #tforex where value" + (buy.equals("Buy") ? "<" : ">") + "signal) as b on a.Code=b.code and a.indicator=b.indicator"
//                + "	where a.r=b.r+1 and a.value>b.value "
//                + "     order by a.date;"
//                + "	drop table #tforex;";
        String sql = "";//  select  * into #tforex from Forex.dbo.forexIDLV where code='" + cur + "' and (indicator='" + filter[0] + "'";
//                for(int i=1;i<filter.length;i++){
//                    sql+= " or indicator='" + filter[i] + "'";
//                }
//                sql+= ");"
                sql+= "  select  t.opendate,t.openprice from "
                + "  (select opendate,openprice,r,date from "
                + "			   (select opendate,openprice from Forex.dbo.forexTrades with (nolock) where trdid=" + tid + ") as ta inner hash join "
                + "			   (select  r,date,signal from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='"+ema+"') as tb on CONVERT(VARCHAR(10),tb.date,3) = convert(varchar(10),ta.OpenDate,3)) as t inner hash join ";
                        for(int k=0;k<filter.length;k++){
                            String fil=filter[k];
                            if(k>0)
                                sql+=" inner join ";
                            if(fil.contains("EMA")){
                                sql+= "			     (select  * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='"+ fil +"' and value" + (buy.equals("Buy") ? ">" : "<") + "=signal) as a"+k+" on a"+k+".r=t.r-1 and t.openprice" + (buy.equals("Buy") ? ">" : "<") + "= a"+k+".signal ";
                            } else{
                                sql+= " 			     (select  * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='"+ fil +"' and value" + (buy.equals("Buy") ? ">" : "<") + "="+rsival+") as a"+k+" on a"+k+".r=t.r-1 ";
                            }
                        }
                sql+= "	order by t.opendate;";
                //+ "	drop table #tforex;";
        
//        PreparedStatement stmt1=conn.prepareStatement("Exec Forex.dbo.filterOpenTrades ?,?,?,?");
//        stmt1.setString(1, cur);
//        stmt1.setString(2, openFilter);
//        stmt1.setString(3, id);
//        stmt1.setString(4, buy);
        PreparedStatement stmt1=conn.prepareStatement(sql);
        ResultSet rs=stmt1.executeQuery();
        
        while(rs.next()){
            Date open=rs.getTimestamp(1);
            double price=rs.getDouble(2);
            Trade trd=new Trade(open, price, null, 0.0);
            trd.setShare(curr);
            tl.addTrade(trd);
        }
        rs.close();
        conn.commit();
        conn.close();
        return tl;
    }
    
     public TradeList getCurrencyTrades(String id, String curr, ArrayList<String> filterTime, ArrayList<String> openFilter, ArrayList<String> openValue) throws Exception {
        TradeList tl = new TradeList();
        Connection conn = getConn();
        conn.setAutoCommit(false);
        Statement stmt2 = conn.createStatement();
        //String[] filter=null;
        String ema = "EMA,50,1";
        int ind = 0, cnt = 0;
        double rsival = 30;
        String[] cr = curr.split(",");
        //String buy=tb[1];
        //String cur=cr[0]+","+tb[0];
        ResultSet r = stmt2.executeQuery("Select id from Forex.dbo.TradeIds where trdkey='" + id + "' and currency='" + cr[0] + "';");
        long tid = 0;
        if (r.next()) {
            tid = r.getLong(1);
        }
        String sql = " select  t.opendate,t.openprice from "
                + "  (select opendate,openprice,r,date from "
                + "			   (select opendate,openprice from Forex.dbo.forexTrades with (nolock) where trdid=" + tid + ") as ta inner hash join "
                + "			   (select  r,date,signal from Forex.dbo.forexIDLV where code='" + curr + "' and indicator='" + ema + "') as tb on tb.date = ta.OpenDate) as t inner hash join ";
        for (int k = 0; k < openFilter.size(); k++) {
            String fil =  TradeCalculator.getInstance().buildIndicator(openFilter.get(k)).getShortName();
            String[] tb = filterTime.get(k).split(",");
            String buy = tb[1];
            String cur = cr[0] + "," + tb[0];
            if (k > 0) {
                sql += " inner join ";
            }
            if (fil.contains("EMA")) {
                sql += "			     (select  * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + fil + "' and value" + (buy.equals("Buy") ? ">" : "<") + "=signal) as a" + k + " on a" + k + ".r=t.r-1 ";//and t.openprice" + (buy.equals("Buy") ? ">" : "<") + "= a" + k + ".signal ";
            } else {
                String val = openValue.get(k);
                sql += " 			     (select  * from Forex.dbo.forexIDLV where code='" + cur + "' and indicator='" + fil + "' and value" + (buy.equals("Buy") ? ">" : "<") + "=" + val + ") as a" + k + " on a" + k + ".r=t.r-1 ";
            }
        }
        sql += "	order by t.opendate;";
        PreparedStatement stmt1 = conn.prepareStatement(sql);
        ResultSet rs = stmt1.executeQuery();

        while (rs.next()) {
            Date open = rs.getTimestamp(1);
            double price = rs.getDouble(2);
            Trade trd = new Trade(open, price, null, 0.0);
            trd.setShare(curr);
            tl.addTrade(trd);
        }
        rs.close();
        conn.commit();
        conn.close();
        return tl;
    }

    public IndicatorList getInd(String sl, String ind) throws Exception {
        String sql="select date,value,signal from Forex.dbo.forexIDLV where code='" + sl + "' and indicator='" + ind + "';";
        IndicatorList il=new IndicatorList();
        Connection conn=getConn();
        PreparedStatement stmt1=conn.prepareStatement(sql);
        ResultSet rs=stmt1.executeQuery();
        while(rs.next()){
            Date open=rs.getTimestamp(1);
            double val =rs.getDouble(2);
            double sig =rs.getDouble(3);
            IndicatorField indf=new IndicatorField(open, val, sig);
            il.addIndField(indf);
        }
        return il;
    }

    private void connectMongo() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient("pc1");
        com.mongodb.DB db = mongoClient.getDB("BackTrade");
        jongo = new Jongo(db);
    }
}
