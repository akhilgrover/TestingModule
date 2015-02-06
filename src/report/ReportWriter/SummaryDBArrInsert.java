/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.ReportWriter;

import report.ReportWriter.SQLSummaryReport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author gnisoft
 */
public class SummaryDBArrInsert implements Runnable {


    private PreparedStatement stmt;
    private PreparedStatement stmt1;
    private static  ArrayList<Connection> con;
    private static AtomicInteger count=new AtomicInteger(0);
    private final ArrayList<ArrayList> summ;
    private Long id;
    private static long t1=0,t2=0,t3=0,t4=0,t5=0,cnt=0;
    static final String sql = "if not exists (select 1 from summary with (nolock) where range=? and system=?) begin "
                    + " insert into Summary with (ROWLOCK) (Range,System,profit,TradeCount,minYrProfit,DrawDown,TradeLength,GraphVal,WinLossRatio,Object) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                    + " end ";
                    //+ " else begin "
                    //+ " update summary with(rowlock) set object=?,profit=?,TradeCount=?,minYrProfit=?,DrawDown=?,TradeLength=?,GraphVal=?,WinLossRatio=? where range=? and system=?;"
                    //+ " end";

    SummaryDBArrInsert(ArrayList<ArrayList> summ, Long id) throws SQLException {
        this.id = id;
        this.summ = summ;
    }

    @Override
    public void run() {
        try {
            cnt++;
            Date d1=new Date();
            //setupConnection();
            Connection conn=setupConnection();
            t1+=(new Date()).getTime()-d1.getTime();
            stmt = conn.prepareStatement(sql);
            d1=new Date();
            //String sql1 = "select 1 from summary where range=? and system=?;";
            //stmt1 = conn.prepareStatement(sql1);
            //stmt1.setLong(1, id);
            stmt.setLong(1, id);
            stmt.setLong(3, id);
            //stmt.setLong(21, id);
            boolean added = false;
            for (ArrayList s : summ) {
                JarOutputStream jar = null;
                try {
                    added=true;
                    //Summary ss = (Summary) s;
                    String name=s.get(0).toString();
                    stmt.setString(2, name);
                    stmt.setString(4, name);
                    stmt.setDouble(5, (Double) s.get(1));
                    stmt.setInt(6, (Integer) s.get(2));
                    stmt.setDouble(7, (Double) s.get(3));
                    stmt.setDouble(8, (Double) s.get(4));
                    stmt.setDouble(9, (Double) s.get(5));
                    stmt.setInt(10, (Integer) s.get(6));
                    stmt.setDouble(11, (Double) s.get(7));
                    t2+=(new Date()).getTime()-d1.getTime();
                    d1=new Date();
                    Blob b = null;
                    if (s.get(8) != null) {
                        b = (Blob) s.get(8);
                    }
                    stmt.setBlob(12, b);
                    t3+=(new Date()).getTime()-d1.getTime();
                    d1=new Date();
                    stmt.addBatch();
                    t4+=(new Date()).getTime()-d1.getTime();
                    d1=new Date();
                } catch (SQLException ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                if (added) {
                    int[] i = stmt.executeBatch();
                    t5+=(new Date()).getTime()-d1.getTime();
                }
                //stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SummaryDBArrInsert.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(cnt%10==0 && cnt>0){
            System.out.println("t1:"+t1/cnt+" t2:"+t2/cnt+" t3:"+t3/cnt+" t3:"+t3/cnt+" t4:"+t4/cnt+" t5:"+t5/cnt);
        }
        return;
    }

    static void closeConnection() {
        for(Connection c:con){
            DbUtils.closeQuietly(c);
        }
        con.clear();
        count.set(0);
        System.out.println("t1:"+t1/cnt+" t2:"+t2/cnt+" t3:"+t3/cnt+" t3:"+t3/cnt+" t4:"+t4/cnt+" t5:"+t5/cnt);
        t1=0;
        t2=0;
        t3=0;
        t4=0;
        t5=0;
        cnt=0;
    }

    public final static Connection setupConnection() {
        if (con == null) {
            con=new ArrayList<Connection>(5);
        }

        Connection c = null;
        int cnts = count.get()%100;
        if (cnts >= con.size()) {
            //if ( c == null || checkConnQ(c))
            {
                try {
                    String driver = "net.sourceforge.jtds.jdbc.Driver";
                    Class.forName(driver).newInstance();
                    c = DriverManager.getConnection("jdbc:jtds:sqlserver://MAXPC;user=BackTradeData;password=oracle");
                    con.add(c);
                } catch (Exception ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        count.incrementAndGet();
        c=con.get(cnts);
        /*if (c == null || checkConnQ(c)) {
            try {
                String driver = "net.sourceforge.jtds.jdbc.Driver";
                Class.forName(driver).newInstance();
                c = DriverManager.getConnection("jdbc:jtds:sqlserver://MAXPC;user=BackTradeData;password=oracle");
                con.add(cnts, c);
            } catch (Exception ex) {
                Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        return c;
    }

    private static boolean checkConnQ(Connection con) {
        boolean ret = false;
        try {
            PreparedStatement stmt1 = con.prepareStatement("select 1");
            ResultSet rs = stmt1.executeQuery();
            if (!rs.next()) {
                ret = true;
            } else {
                ret = false;
            }
        } catch (SQLException ex) {
            ret = true;
        }
        return ret;
    }

}
