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
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
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
public class SummaryDBArrSingleInsert implements Runnable {


    private static final int BATCH = 300;
    private PreparedStatement stmt;
    private Connection con;
    private AtomicInteger count=new AtomicInteger(0);
    private BlockingQueue<ArrayList<ArrayList>> summ;
    public volatile boolean close;
    private long t1=0,t2=0,t3=0,t4=0,t5=0,cnt=0;
    //static final String sql = " insert into ? with (ROWLOCK) (Range,System,profit,TradeCount,minYrProfit,DrawDown,TradeLength,GraphVal,WinLossRatio,Object) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                        //"if not exists (select 1 from summary with (nolock) where range=? and system=?) begin "
                    //+ " insert into Summary with (ROWLOCK) (Range,System,profit,TradeCount,minYrProfit,DrawDown,TradeLength,GraphVal,WinLossRatio,Object) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                    //+ " end ";
                    //+ " else begin "
                    //+ " update summary with(rowlock) set object=?,profit=?,TradeCount=?,minYrProfit=?,DrawDown=?,TradeLength=?,GraphVal=?,WinLossRatio=? where range=? and system=?;"
                    //+ " end";

    SummaryDBArrSingleInsert(BlockingQueue<ArrayList<ArrayList>> summ) throws SQLException {
        this.summ = summ;
    }

    @Override
    public void run() {
        //Date d1 = new Date();
        //setupConnection();
        Connection conn = setupConnection();
        //t1 += (new Date()).getTime() - d1.getTime();
        String sql ="";
        long idChk=0;
        while (!close || summ.size()>0) {
            try {
                ArrayList<ArrayList> sum=summ.take();
                cnt++;
                if (sum.size() > 0) {
                    Date d1 = new Date();
                    long idd = (Long) sum.get(0).get(9);
                    if (idChk != idd) {
                        Runtime.getRuntime().gc();
                        sql = " insert into Summary" + idd + " with (rowlock) (System,profit,TradeCount,minYrProfit,DrawDown,TradeLength,GraphVal,WinLossRatio,Object) values(?, ?, ?, ?, ?, ?, ?, ?, ?);";
                        try {
                            stmt = conn.prepareStatement(sql);
                        } catch (SQLException ex) {
                            Logger.getLogger(SummaryDBArrSingleInsert.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        t1 += (new Date()).getTime() - d1.getTime();
                        d1 = new Date();
                        idChk = idd;
                    }
                    boolean added = false;
                    for (ArrayList s : sum) {
                        //JarOutputStream jar = null;
                        try {
                            added = true;
                            //long id=(Long)s.get(9);
                            //stmt.setLong(1, id);

                            //Summary ss = (Summary) s;
                            String name = s.get(0).toString().replaceAll(" ", ",");
                            stmt.setString(1, name);
                            //stmt.setString(4, name);
                            stmt.setDouble(2, (Double) s.get(1));
                            stmt.setInt(3, (Integer) s.get(2));
                            stmt.setDouble(4, (Double) s.get(3));
                            stmt.setDouble(5, (Double) s.get(4));
                            stmt.setDouble(6, (Double) s.get(5));
                            stmt.setInt(7, (Integer) s.get(6));
                            stmt.setDouble(8, (Double) s.get(7));
                            //t2 += (new Date()).getTime() - d1.getTime();
                            //d1 = new Date();
                            Blob b = null;
                            if (s.get(8) != null) {
                                b = (Blob) s.get(8);
                            }
                            stmt.setBlob(9, b);


                            //stmt.setLong(3, id);
                            //t3 += (new Date()).getTime() - d1.getTime();
                            //d1 = new Date();
                            stmt.addBatch();
                            //t4 += (new Date()).getTime() - d1.getTime();

                        } catch (SQLException ex) {
                            Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    t4 += (new Date()).getTime() - d1.getTime();
                    d1 = new Date();
                    try {
                        if (added) {

                            stmt.executeBatch();
                            //conn.commit();
                            t5 += (new Date()).getTime() - d1.getTime();
                            stmt.clearParameters();

                        }

                        //stmt.close();
                    } catch (BatchUpdateException ex) {
                        //Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                        stmt.clearParameters();
                    } catch (SQLException ex) {
                        Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                        conn = setupConnection();
                        //t1 += (new Date()).getTime() - d1.getTime();
                        stmt = conn.prepareStatement(sql);
                    } finally {
                        //if (cnt % BATCH/100 == 0) {
                        //    conn.commit();
                        //    t5 += (new Date()).getTime() - d1.getTime();
                        //}
                    }
                }

            }  catch (InterruptedException ex) {
                Logger.getLogger(SummaryDBArrSingleInsert.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(SummaryDBArrSingleInsert.class.getName()).log(Level.SEVERE, null, ex);
                conn = setupConnection();
                //t1 += (new Date()).getTime() - d1.getTime();
                try {
                    stmt = conn.prepareStatement(sql);
                } catch (Exception e) {
                    Logger.getLogger(SummaryDBArrSingleInsert.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (cnt % 10 == 0 && cnt > 0) {
                System.out.println(new SimpleDateFormat("dd/MM/yy hh:mm:ss").format(new Date()) +" t1:" + t1 / cnt + " t2:" + t2 / cnt + " t3:" + t3 / cnt + " t3:" + t3 / cnt + " t4:" + t4 / cnt + " t5:" + t5 / cnt+" Q:"+summ.size()+", "+cnt);
                Runtime.getRuntime().gc();
            }
        }
        return;
    }

    void closeConnection() {
        //for(Connection c:con){
            DbUtils.closeQuietly(con);
        //}
        //con.clear();
        count.set(0);
        System.out.println("t1:"+t1/cnt+" t2:"+t2/cnt+" t3:"+t3/cnt+" t3:"+t3/cnt+" t4:"+t4/cnt+" t5:"+t5/cnt);
        t1=0;
        t2=0;
        t3=0;
        t4=0;
        t5=0;
        cnt=0;
    }

    public Connection setupConnection() {
        try {
            String driver = "net.sourceforge.jtds.jdbc.Driver";
            //String driver = "com.nuodb.jdbc.Driver";

            Class.forName(driver).newInstance();
            //con = DriverManager.getConnection("jdbc:jtds:sqlserver://MAXPC;user=BackTradeData;password=oracle;appName=SingleRun;batchSize="+BATCH+";autoCommit=false");
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://maxpc;user=BackTradeData;password=oracle;appName=SingleRun;batchSize="+BATCH+"");
            /*Properties properties = new Properties();
            properties.put("user", "BackTradeData");
            properties.put("password", "oracle");
            //properties.put("schema", "user");
            con = DriverManager.getConnection("jdbc:com.nuodb://MAXPC:48004/BackTrade",properties);*/
        } catch (Exception ex) {
            Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
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
