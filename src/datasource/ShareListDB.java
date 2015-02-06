package datasource;

import Share.ShareComparator;
import Share.ShareList;
import indicator.BetaIndicator;
import indicator.IndicatorField;
import indicator.IndicatorList;
import indicator.MaIndicator;
import indicator.MacdIndicator;
import indicator.RsiIndicator;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import trade.TradeCalculator;

public class ShareListDB {

    private HashMap<String,ShareList> slist;
    private Connection con;
    private Statement stmt;
    //private ResultSet rs;
    private BasicShareDB bsdb;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");

    /**
     * Method ShareListDB
     *
     *
     * @throws Exception
     */
    public ShareListDB() throws Exception
    {
        try {
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://MAXPC;user=BackTradeData;password=oracle");
            //con = DriverManager.getConnection("jdbc:odbc:MAXPC","backtradedata","oracle");

            slist = new HashMap<String, ShareList>();//.shared().setKeyComparator(FastComparator.STRING);
            bsdb = new BasicShareDB();
            stmt = con.createStatement();
        } catch (Exception ex) {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String database =
                    "jdbc:odbc:backs";
            con = DriverManager.getConnection(database, "", "");
            stmt = con.createStatement();
        }
    }

    public HashMap getShareListData(String group) throws Exception
    {
        //stmt=con.createStatement();
        String sql="Select rtrim(code) from groups where list='"+group+"'";
        ResultSet rs=stmt.executeQuery(sql);
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            slist.put(code,bsdb.getShareData(code));
        }
        rs.close();
        return slist;
    }
    
    public ArrayList<String> getCurrencies() throws Exception
    {
        //stmt=con.createStatement();
        String sql="Select rtrim(code) from Forex.dbo.CurrencyCodes";
        ArrayList<String> codes=new ArrayList<>();
        ResultSet rs=stmt.executeQuery(sql);
        
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            codes.add(code);
        }
        rs.close();
        return codes;
    }

    /**
     *
     * @param index
     *  Index to use FTSE or GSPC etc
     * @param dd
     *  date to check for the trade
     * @return
     *  returns the list of shares
     * @throws java.lang.Exception
     *  Sql Exception catch
     */
    public ArrayList getShareOnDate(String index,Date dd) throws Exception
    {
        //stmt=con.createStatement();
        //SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
        //String sql="Select TOP 100 code from indices where index='"+index+"' and date<=#"+sdf.format(dd)+"# ORDER BY  indices.Date DESC,code;";
        String sql="SELECT rtrim(indices.code) FROM indices WHERE (((indices.Date)=(SELECT TOP 1 indices.Date FROM indices where indices.[index]='"+index+"' GROUP BY indices.Date HAVING (((indices.Date)<='"+sdf.format(dd)+"')) ORDER BY indices.Date DESC)) AND ((indices.[Index])='"+index+"')) ORDER BY indices.code;";
        //String sql="SELECT indices.code FROM Shares INNER JOIN indices ON Shares.Code = indices.code WHERE (((indices.Date)=(SELECT TOP 1 indices.Date FROM indices GROUP BY indices.Date HAVING (((indices.Date)<=#"+sdf.format(dd)+"#)) ORDER BY indices.Date DESC)) AND ((Shares.ShareDate)=#"+sdf.format(dd)+"#) AND ((indices.Index)='"+index+"')) ORDER BY indices.code;";
        ResultSet rs=stmt.executeQuery(sql);
        ArrayList arr=new ArrayList(100);
        //HashMap arr=new HashMap(100);
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            arr.add(code);
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        return arr;
    }

    /**
     *
     * @param index
     *  Index to use FTSE or GSPC etc
     * @param dd
     *  date to check for the trade
     * @param sc
     * @return
     *  returns the list of shares
     * @throws java.lang.Exception
     *  Sql Exception catch
     */
    public TreeMap getShareOnDate(String index,Date dd,ShareComparator sc) throws Exception
    {
        //stmt=con.createStatement();
        //SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
        //String sql="Select TOP 100 code from indices where index='"+index+"' and date<=#"+sdf.format(dd)+"# ORDER BY  indices.Date DESC,code;";
        String sql="SELECT rtrim(indices.code) FROM indices WHERE (((indices.Date)=(SELECT TOP 1 indices.Date FROM indices where indices.[index]='"+index+"' GROUP BY indices.Date HAVING (((indices.Date)<='"+sdf.format(dd)+"')) ORDER BY indices.Date DESC)) AND ((indices.[index])='"+index+"')) ORDER BY indices.code;";
        //String sql="SELECT indices.code FROM Shares INNER JOIN indices ON Shares.Code = indices.code WHERE (((indices.Date)=(SELECT TOP 1 indices.Date FROM indices GROUP BY indices.Date HAVING (((indices.Date)<=#"+sdf.format(dd)+"#)) ORDER BY indices.Date DESC)) AND ((Shares.ShareDate)=#"+sdf.format(dd)+"#) AND ((indices.Index)='"+index+"')) ORDER BY indices.code;";
        ResultSet rs=stmt.executeQuery(sql);
        //ArrayList arr=new ArrayList(100);
        TreeMap<String,Integer> arr=new TreeMap<String, Integer>(sc);
        int i=0;
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            arr.put(code,i++);
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        return arr;
    }

    public ArrayList getShareOnDate(ShareList sl, Date dd, int backPeriod, int shareCount, boolean sell) throws Exception
    {
        //SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
        String index=sl.getShare();
        int back=sl.isDatePresent(dd)-backPeriod;
        if(back<0)
            back=sl.getSize()-backPeriod;

        String sql="SELECT top("+shareCount+")    Indices.code " +
                " FROM         Shares AS Shares_1 INNER JOIN" +
                "                      Shares INNER JOIN" +
                "                      Indices ON Shares.Code = Indices.code ON Shares_1.Code = Indices.code " +
                "WHERE     [index]='"+index+"' and (Indices.Date = (SELECT    top 1 Date " +
                "FROM         Indices " +
                "WHERE     [INDEX] = '"+index+"' AND date <= '"+sdf.format(dd)+"' " +
                "order by date desc)) AND (Shares.ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') " +
                "ORDER BY (Shares.[Close] - Shares_1.[Close]) / Shares_1.[Close] * 100";
        if(!sell)
              sql+=  " DESC";
        else
              sql+=  " ASC";
        ResultSet rs=stmt.executeQuery(sql);
        //ArrayList arr=new ArrayList(100);
        ArrayList arr=new ArrayList(shareCount);
        int i=0;
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            arr.add(code);
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        return arr;
    }

    public synchronized ArrayList<String> getShareOnDateExclude(ShareList sl, Date dd, int backPeriod, int shareCount, boolean sell) throws Exception
    {
        String index=sl.getShare();
        int back=sl.isDatePresent(dd)-backPeriod;
        if(back<0 && sl.isDatePresent(dd)<0)
            back=sl.getSize()-backPeriod;
        else if(back<0)
            back=0;
        String comb="",comp="",cls="close";
        String invest="";
        if(index.equals("ASX"))
        {
            comb="Combined";
            comp="Complete";
            cls="close1";
            //invest=" and Indices"+comb+".code NOT IN (SELECT code FROM completelist WHERE (list = 'investment trust')) ";
        } else if(index.equals("GSPC")){
            comp="US";
        }
        else if(index.equals("OEX")){
            comp="US";
        }
        //shareCount=shareCount+10;
        String oSQL="select top("+shareCount+") code from DailyIndices "
                + "where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                + " and base='"+index+"' order by profit";

        if(!sell)
              oSQL+=  " DESC";
        else
              oSQL+=  " ASC";
        oSQL+= ",code";
        ResultSet rs=stmt.executeQuery(oSQL);
        //ArrayList arr=new ArrayList(100);
        ArrayList<String> arr=new ArrayList<String>(shareCount);
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            arr.add(code.intern());
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        Date cur=sdf.parse(sdf.format(new Date()));
        if(arr.isEmpty() || dd.equals(cur))
        {
            String sql="SELECT top("+shareCount+")    Indices"+comb+".code " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                    "                      Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+index+"' AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            if(!sell)
                  sql+=  " DESC";
            else
                  sql+=  " ASC";
            String sqlNew="";
            if(dd.equals(cur))
                sqlNew="delete from DailyIndices where tdate='"+sdf.format(dd)+"' and base='"+ index +"' and backperiod="+backPeriod+";";
            if(dd.before(cur)){
             sqlNew+="Insert into DailyIndices " +
                    " SELECT shares"+comp+".sharedate,"+backPeriod+",Indices"+comb+".code,((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100,'"+ index +"' " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                    "                      Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+index+"' AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            }
            try {
                rs = stmt.executeQuery(sql);


                //ArrayList arr=new ArrayList(100);
                arr = new ArrayList<String>(shareCount);
                while (rs.next()) {
                    String code = rs.getString(1).intern();
                    arr.add(code.intern());
                    //arr.put(code,bsdb.getShareData(code));
                }
                rs.close();
                if (dd.before(cur)) {
                    stmt.executeUpdate(sqlNew);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
                Logger.getLogger(ShareListDB.class.getName()).log(Level.SEVERE, sql, ex);
            }
        }
        //return new ArrayList<String>(arr.subList(10, arr.size()));
        return arr;
    }

    public synchronized ArrayList<String> getShareOnDateReview(ShareList sl, Date dd, int backPeriod, int shareCount, boolean sell) throws Exception
    {
        String index=sl.getShare();
        int back=sl.isDatePresent(dd)-backPeriod;
        if(back<0 && sl.isDatePresent(dd)<0)
            back=sl.getSize()-backPeriod;
        else if(back<0)
            back=0;
        String comb="",comp="",cls="close";
        String invest="";
        if(index.equals("ASX"))
        {
            comb="Combined";
            comp="Complete";
            cls="close1";
            //invest=" and Indices"+comb+".code NOT IN (SELECT code FROM completelist WHERE (list = 'investment trust')) ";
        } else if(index.equals("GSPC")){
            comp="US";
        }
        else if(index.equals("OEX")){
            comp="US";
        } else if(index.equals("MCX")){
            comp="350";
        }
        
        //shareCount=shareCount+10;
        String oSQL="select top("+shareCount+") code from DailyIndicesReview "
                + "where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                + " and base='"+index+"' order by profit";

        if(!sell)
              oSQL+=  " DESC";
        else
              oSQL+=  " ASC";
        oSQL+= ",code";
        ResultSet rs=stmt.executeQuery(oSQL);
        //ArrayList arr=new ArrayList(100);
        ArrayList<String> arr=new ArrayList<String>(shareCount);
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            arr.add(code.intern());
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        Date cur=sdf.parse(sdf.format(new Date()));
        if(arr.isEmpty() || dd.equals(cur))
        {
            String sql="SELECT top("+shareCount+")    Indices"+comb+".code " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                    //"                      (select date,code,'MCX' AS [index] from indices where [index]='MCX' union " +
                    //"	select tdate as date,code,'MCX' AS [index] from DailyIndices where base='UKX' and backperiod=225) AS 
                    " Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+index+"' AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            if(!sell)
                  sql+=  " DESC";
            else
                  sql+=  " ASC";
            String sqlNew="";
            if(dd.equals(cur))
                sqlNew="delete from DailyIndicesReview where tdate='"+sdf.format(dd)+"' and base='"+ index +"' and backperiod="+backPeriod+";";
            if(dd.before(cur)){
             sqlNew+="Insert into DailyIndicesReview " +
                    " SELECT shares"+comp+".sharedate,"+backPeriod+",Indices"+comb+".code,((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100,'"+ index +"' " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                   // "                      (select date,code,'MCX' AS [index] from indices where [index]='MCX' union" +
                    //"	select tdate as date,code,'MCX' AS [index] from DailyIndices where base='UKX' and backperiod=225) AS
                     " Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+index+"'"
                     + " AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            }
            try {
                rs = stmt.executeQuery(sql);


                //ArrayList arr=new ArrayList(100);
                arr = new ArrayList<String>(shareCount);
                while (rs.next()) {
                    String code = rs.getString(1).intern();
                    arr.add(code.intern());
                    //arr.put(code,bsdb.getShareData(code));
                }
                rs.close();
                if (dd.before(cur)) {
                    stmt.executeUpdate(sqlNew);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
                Logger.getLogger(ShareListDB.class.getName()).log(Level.SEVERE, sql, ex);
            }
        }
        //return new ArrayList<String>(arr.subList(10, arr.size()));
        return arr;
    }
    
    public synchronized ArrayList<String> getShareOnDateALL(ShareList sl, Date dd, int backPeriod, boolean sell) throws Exception
    {
        String index=sl.getShare();
        int back=sl.isDatePresent(dd)-backPeriod;
        if(back<0 && sl.isDatePresent(dd)<0)
            back=sl.getSize()-backPeriod;
        else if(back<0)
            back=0;
        String comb="",comp="",cls="close";
        String invest="";
        String ind="";
        String nind=index;
        switch (index) {
            
            case "ASX":
                comb="Combined";
                comp="Complete";
                cls="close1";
                //invest=" and Indices"+comb+".code NOT IN (SELECT code FROM completelist WHERE (list = 'investment trust')) ";
                break;
            case "GSPC":
                comp="US";
                break;
            case "OEX":
                comp="US";
                break;
            case "NMX":
                comp = "350";
                ind = "                      (select date,code,'NMX' AS [index] from indices where [index]='MCX' union"
                        + "	select tdate as date,code,'NMX' AS [index] from DailyIndices where base='UKX' and backperiod=225) AS ";
                nind="UKX";
                break;
        }
        
        //shareCount=shareCount+10;
        String oSQL="select top 100 percent code from DailyIndices "
                + "where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                + " and base='"+index+"' order by profit";

        if(!sell)
              oSQL+=  " DESC";
        else
              oSQL+=  " ASC";
        oSQL+= ",code";
        ResultSet rs=stmt.executeQuery(oSQL);
        //ArrayList arr=new ArrayList(100);
        ArrayList<String> arr=new ArrayList<String>();
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            arr.add(code.intern());
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        Date cur=sdf.parse(sdf.format(new Date()));
        if(arr.isEmpty() || dd.equals(cur))
        {
            String sql="SELECT top 100 percent    Indices"+comb+".code " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                    ind + 
                    " Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+nind+"' AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            if(!sell)
                  sql+=  " DESC";
            else
                  sql+=  " ASC";
            String sqlNew="";
            if(dd.equals(cur))
                sqlNew="delete from DailyIndices where tdate='"+sdf.format(dd)+"' and base='"+ index +"' and backperiod="+backPeriod+";";
            if(dd.before(cur)){
             sqlNew+="Insert into DailyIndices " +
                    " SELECT shares"+comp+".sharedate,"+backPeriod+",Indices"+comb+".code,((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100,'"+ index +"' " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" + 
                    ind + " Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+nind+"'"
                     + " AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            }
            try {
                rs = stmt.executeQuery(sql);


                //ArrayList arr=new ArrayList(100);
                arr = new ArrayList<String>();
                while (rs.next()) {
                    String code = rs.getString(1).intern();
                    arr.add(code.intern());
                    //arr.put(code,bsdb.getShareData(code));
                }
                rs.close();
                if (dd.before(cur)) {
                    stmt.executeUpdate(sqlNew);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
                Logger.getLogger(ShareListDB.class.getName()).log(Level.SEVERE, sql, ex);
            }
        }
        //return new ArrayList<String>(arr.subList(10, arr.size()));
        return arr;
    }
    
    public synchronized HashMap<Date,ArrayList<String>> getShareOnALL(ShareList sl, int backPeriod, boolean sell) throws Exception
    {
        String index=sl.getShare();
        String oSQL="select  code,tDate from DailyIndices "
                + "where backperiod="+backPeriod
                + " and base='"+index+"' order by tDate, profit";
        if(!sell)
              oSQL+=  " DESC";
        else
              oSQL+=  " ASC";
        oSQL+= ",code";
        ResultSet rs=stmt.executeQuery(oSQL);
        HashMap<Date,ArrayList<String>> arr=new HashMap<>();
        while(rs.next())
        {
            String code=rs.getString(1).intern();
            Date date=rs.getDate(2);
            if(arr.get(date)==null)
                arr.put(date, new ArrayList<String>());
            arr.get(date).add(code);
        }
        rs.close();
        return arr;
    }

    public synchronized ArrayList<String> getShareOnDateExclude(ShareList sl, Date dd, int backPeriod, int shareCount,int topCap, boolean sell) throws Exception
    {
        String index=sl.getShare();
        int back=sl.isDatePresent(dd)-backPeriod;
        if(back<0 && sl.isDatePresent(dd)<0)
            back=sl.getSize()-backPeriod;
        else if(back<0)
            back=0;
        String comb="",comp="",cls="close";
        String invest="";
        if(index.equals("ASX"))
        {
            comb="Combined";
            comp="Complete";
            cls="close1";
            //invest=" and Indices"+comb+".code NOT IN (SELECT code FROM completelist WHERE (list = 'investment trust')) ";
        } else if(index.equals("GSPC")){
            comp="US";
        }
        else if(index.equals("OEX")){
            comp="US";
        }
        //shareCount=shareCount+10;
        String oSQL="select top("+shareCount+") code from DailyIndices "
                + "where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                + " and base='"+index+"' order by profit";
        if(index.equals("GSPC") && topCap>0){
            oSQL="select top("+shareCount+") code from "
                    + "(select top(" + topCap + ") dailyindices.code, profit,mktcap from DailyIndices inner join sharesus on dailyindices.code=sharesus.code and dailyindices.tdate=sharesus.sharedate"
                    + "                where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                    + "                and base='"+index+"' order by mktcap desc) as a"
                    + "				order by profit";
        } else if(index.equals("OEX") && topCap>0){
            oSQL="select top("+shareCount+") code from "
                    + "           (SELECT        TOP (" + topCap + ") DailyIndices.code, min(DailyIndices.profit) as profit, SharesUS.MKTCAP,min(base) as m,max(base) as ma"
                    + "                          FROM            DailyIndices INNER JOIN"
                    + "                                                    SharesUS ON DailyIndices.code = SharesUS.Code AND DailyIndices.tDate = SharesUS.ShareDate"
                    + "                          WHERE        (DailyIndices.tDate = '"+sdf.format(dd)+"') AND (DailyIndices.BackPeriod = "+backPeriod+") "
                    + "                          AND (DailyIndices.Base in ('"+index+"','GSPC') )"
                    + "						  group by DailyIndices.code,SharesUS.MKTCAP"
                    + "                          ORDER BY max(base) desc,SharesUS.MKTCAP DESC ) AS a"
                    + "				order by profit";
        }
        if(!sell)
              oSQL+=  " DESC";
        else
              oSQL+=  " ASC";
        oSQL+= ",code";
        ResultSet rs=stmt.executeQuery(oSQL);
        //ArrayList arr=new ArrayList(100);
        ArrayList<String> arr=new ArrayList<String>(shareCount);
        while(rs.next())
        {
            String code=rs.getString(1).trim().intern();
            arr.add(code.intern());
            //arr.put(code,bsdb.getShareData(code));
        }
        rs.close();
        Date cur=sdf.parse(sdf.format(new Date()));
        if(arr.isEmpty() || dd.equals(cur))
        {
            String sql="SELECT top("+shareCount+")    Indices"+comb+".code " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                    "                      Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+index+"' AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            if(!sell)
                  sql+=  " DESC";
            else
                  sql+=  " ASC";
            String sqlNew="";
            if(dd.equals(cur))
                sqlNew="delete from DailyIndices where tdate='"+sdf.format(dd)+"' and base='"+ index +"' and backperiod="+backPeriod+";";
            if(dd.before(cur)){
             sqlNew+="Insert into DailyIndices " +
                    " SELECT shares"+comp+".sharedate,"+backPeriod+",Indices"+comb+".code,((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100,'"+ index +"' " +
                    " FROM         Shares"+comp+" AS Shares_1 INNER JOIN" +
                    "                      Shares"+comp+" INNER JOIN" +
                    "                      Indices"+comb+" ON Shares"+comp+".Code = Indices"+comb+".code ON Shares_1.Code = Indices"+comb+".code " +
                    "WHERE     [index]='"+index+"' and (Indices"+comb+".Date = (SELECT    top 1 Date " +
                    "FROM         Indices"+comb+" " +
                    "WHERE     [INDEX] = '"+index+"' AND date <= '"+sdf.format(dd)+"' " +
                    "order by date desc)) AND (Shares"+comp+".ShareDate = '"+sdf.format(dd)+"') AND (Shares_1.ShareDate = '"+sdf.format(sl.getSharedata(back).getDate())+"') "
                    + invest
                    + "AND (Indices"+comb+".code NOT IN"
                    + "                          (SELECT     code"
                    + "                            FROM          TakeOvers"
                    + "                            WHERE      (eDate <= '"+sdf.format(dd)+"')))" +
                    " ORDER BY ((Shares"+comp+".["+cls+"] - Shares_1.["+cls+"])*1.0) / Shares_1.["+cls+"] * 100";
            }
            try {

                if (dd.before(cur)) {
                    stmt.executeUpdate(sqlNew);
                    rs = stmt.executeQuery(oSQL);
                }
                else
                    rs = stmt.executeQuery(sql);

                //ArrayList arr=new ArrayList(100);
                arr = new ArrayList<String>(shareCount);
                while (rs.next()) {
                    String code = rs.getString(1).trim().intern();
                    arr.add(code.intern());
                    //arr.put(code,bsdb.getShareData(code));
                }
                rs.close();
            } catch (Exception ex) {
                //JOptionPane.showMessageDialog(null, ex);
                Logger.getLogger(ShareListDB.class.getName()).log(Level.SEVERE, sql, ex);
            }
        }
        //return new ArrayList<String>(arr.subList(10, arr.size()));
        return arr;
    }

    public synchronized ArrayList<String> getShareOnDateExclude(ShareList sl, Date dd, int backPeriod, int shareCount,int topCap, boolean sell,int type) throws Exception
    {
        String index=sl.getShare();
        int back=sl.isDatePresent(dd)-backPeriod;
        if(back<0 && sl.isDatePresent(dd)<0)
            back=sl.getSize()-backPeriod;
        else if(back<0)
            back=0;
        String comb="",comp="",cls="close";
        String invest="";
        if(index.equals("ASX"))
        {
            comb="Combined";
            comp="Complete";
            cls="close1";
            //invest=" and Indices"+comb+".code NOT IN (SELECT code FROM completelist WHERE (list = 'investment trust')) ";
        } else if(index.equals("GSPC")){
            comp="US";
        }
        else if(index.equals("OEX")){
            comp="US";
        }
        //shareCount=shareCount+10;
        String oSQL="";
//        String oSQL="select top("+shareCount+") code from DailyIndices "
//                + "where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
//                + " and base='"+index+"' order by profit";
        if(type==1){
            oSQL="select top ("+shareCount+") code,(cls-av)*100/av as diff  from "
                + " (select code, sharedate, ["+cls+"] as cls,"
                + " avg(["+cls+"]) OVER (partition by code ORDER BY sharedate ROWS BETWEEN "+(backPeriod)+" PRECEDING AND 1 PRECEDING ) as av"
                + " FROM Shares"+comp+" with (nolock) where sharedate>dateadd(D,-"+(int)(backPeriod*1.5)+",'"+sdf.format(dd)+"') and sharedate<dateadd(D,5,'"+sdf.format(dd)+"') and code in (select code from Indices"+comb+" with (nolock) where [index]='"+index+"' and date=(select max(date) from Indices"+comb+" with (nolock) where [index]='"+index+"' and date<='"+sdf.format(dd)+"'))) as a  "
                + " where a.sharedate='"+sdf.format(dd)+"' and av is not null"
                + " order by diff ";
        } else if(type==2){
            oSQL="select top ("+shareCount+") code,(cls-av)*100/av as diff  from "
                + " (select code, sharedate, avg(["+cls+"]) OVER (partition by code ORDER BY sharedate ROWS BETWEEN 20 PRECEDING AND 1 PRECEDING ) as cls,"
                + " avg(["+cls+"]) OVER (partition by code ORDER BY sharedate ROWS BETWEEN "+(backPeriod+10)+" PRECEDING AND "+(backPeriod-10)+" PRECEDING ) as av"
                + " FROM Shares"+comp+" where sharedate>dateadd(D,-"+(int)(backPeriod*1.5)+",'"+sdf.format(dd)+"') and sharedate<dateadd(D,5,'"+sdf.format(dd)+"') and code in (select code from Indices"+comb+" where [index]='"+index+"' and date=(select max(date) from Indices"+comb+" where [index]='"+index+"' and date<='"+sdf.format(dd)+"'))) as a  "
                + " where a.sharedate='"+sdf.format(dd)+"' and av is not null"
                + " order by diff ";
        } else if(type>=3){
           oSQL="select top("+shareCount+") code from DailyIndices "
                + "where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                + " and base='"+index+"' order by profit ";
        }
        if(index.equals("GSPC") && topCap>0){
            oSQL="select top("+shareCount+") code from "
                    + "(select top(" + topCap + ") dailyindices.code, profit,mktcap from DailyIndices inner join sharesus on dailyindices.code=sharesus.code and dailyindices.tdate=sharesus.sharedate"
                    + "                where tDate='"+sdf.format(dd)+"' and backperiod="+backPeriod
                    + "                and base='"+index+"' order by mktcap desc) as a"
                    + "				order by profit";
        } else if(index.equals("OEX") && topCap>0){
            oSQL="select top("+shareCount+") code from "
                    + "           (SELECT        TOP (" + topCap + ") DailyIndices.code, min(DailyIndices.profit) as profit, SharesUS.MKTCAP,min(base) as m,max(base) as ma"
                    + "                          FROM            DailyIndices INNER JOIN"
                    + "                                                    SharesUS ON DailyIndices.code = SharesUS.Code AND DailyIndices.tDate = SharesUS.ShareDate"
                    + "                          WHERE        (DailyIndices.tDate = '"+sdf.format(dd)+"') AND (DailyIndices.BackPeriod = "+backPeriod+") "
                    + "                          AND (DailyIndices.Base in ('"+index+"','GSPC') )"
                    + "						  group by DailyIndices.code,SharesUS.MKTCAP"
                    + "                          ORDER BY max(base) desc,SharesUS.MKTCAP DESC ) AS a"
                    + "				order by profit";
        }
        if(!sell)
              oSQL+=  " DESC";
        else
              oSQL+=  " ASC";
        oSQL+= ",code";
        ResultSet rs=stmt.executeQuery(oSQL);
        //ArrayList arr=new ArrayList(100);
        ArrayList<String> arr1=new ArrayList<String>(shareCount);
        while(rs.next())
        {
            String code=rs.getString(1).trim().intern();
            arr1.add(code.intern());
        }
        ArrayList<String> arr=null;
        if(type<3)
            arr=arr1;
        if(type==3){
            BetaIndicator ind=new BetaIndicator();
            ind.init();
            arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arr1){
                IndicatorList il=tc.getIndicatorList(s);
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()), sl);
                    tc.putIndicatorList(s, il);
                }
                arr.add(s);
                arVal.put(s,il.getSharedata(il.isDatePresent(dd)).getValue());
            }
            Collections.sort(arr, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==4){
            RsiIndicator ind=new RsiIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 20);
            hm.put(2, 1);
            ind.init(hm);
            arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arr1){
                IndicatorList il=tc.getIndicatorList(s);
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s, il);
                }
                arr.add(s);
                arVal.put(s,il.getSharedata(il.isDatePresent(dd)).getValue());
            }
            Collections.sort(arr, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==5){
            MaIndicator ind=new MaIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 50);
            hm.put(2, ma.MA.Exponential);
            ind.init(hm);
            arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arr1){
                IndicatorList il=tc.getIndicatorList(s);
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s, il);
                }
                arr.add(s);
                IndicatorField indf=il.getSharedata(il.isDatePresent(dd));
                arVal.put(s,(indf.getValue()-indf.getSignal())*100/indf.getValue());
            }
            Collections.sort(arr, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==6){
            MacdIndicator ind=new MacdIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 13);
            hm.put(2, 26);
            hm.put(3, 9);
            ind.init(hm);
            arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arr1){
                IndicatorList il=tc.getIndicatorList(s);
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s, il);
                }
                arr.add(s);
                IndicatorField indf=il.getSharedata(il.isDatePresent(dd));
                arVal.put(s,(indf.getValue()-indf.getSignal())*100/indf.getValue());
            }
            Collections.sort(arr, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o1), arVal.get(o2));
                }
            });
        }
        rs.close();
        return arr;
    }

    public void addShareToList(String code) throws Exception
    {
        if(!slist.containsKey(code))
        {
            slist.put(code,bsdb.getShareData(code));
        }
    }

    public HashMap getShareList()
    {
        return slist;
    }

    public void close() throws SQLException
    {
        if(bsdb!=null)
            bsdb.close();
        if(con!=null)
            con.close();
        slist=null;
        con=null;
        stmt=null;
        //rs=null;
        System.gc();
    }

    public IndicatorList getIndicatorList(String pair,Date d1,Date d2,String ind) throws SQLException {
        String sql="select b.dt,b.indvalue from pairidl3master as a inner join pairidl3 as b on a.id=b.id"
                + " where a.idlkey='"+ind+"' and a.pair='"+ pair +"' and dt between '"+ sdf.format(d1) +"' and '"+ sdf.format(d2) +"'";
        ResultSet rs=stmt.executeQuery(sql);
        IndicatorList il=new IndicatorList();
        int i=0;
        while(rs.next())
        {
            il.addIndField(new IndicatorField(rs.getDate(1), rs.getDouble(2), 0));
        }
        rs.close();
        return il;
    }
}
