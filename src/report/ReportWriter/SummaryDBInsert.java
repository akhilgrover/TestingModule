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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.ArrayUtils;
import report.Summary;

/**
 *
 * @author gnisoft
 */
class SummaryDBInsert implements Runnable {


    private PreparedStatement stmt;
    private PreparedStatement stmt1;
    private static  ArrayList<Connection> con;
    private static AtomicInteger count=new AtomicInteger(0);
    private final List summ;
    private Long id;
    static final String sql = "if not exists (select 1 from summary with (nolock) where range=? and system=?) begin "
                    + " insert into Summary with (ROWLOCK)(Range,System,profit,TradeCount,minYrProfit,DrawDown,TradeLength,GraphVal,WinLossRatio,Object) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                    + " end ";
                    //+ " else begin "
                    //+ " update summary with(rowlock) set object=?,profit=?,TradeCount=?,minYrProfit=?,DrawDown=?,TradeLength=?,GraphVal=?,WinLossRatio=? where range=? and system=?;"
                    //+ " end";

    SummaryDBInsert(List summ, Long id) throws SQLException {
        this.id = id;
        this.summ = summ;
    }

    @Override
    public void run() {
        try {
            //setupConnection();
            Connection conn=setupConnection();

            stmt = conn.prepareStatement(sql);
            //String sql1 = "select 1 from summary where range=? and system=?;";
            //stmt1 = conn.prepareStatement(sql1);
            //stmt1.setLong(1, id);
            stmt.setLong(1, id);
            stmt.setLong(3, id);
            //stmt.setLong(21, id);
            boolean added = false;
            for (Object s : summ) {
                JarOutputStream jar = null;
                try {
                    Summary ss = (Summary) s;
                    //stmt1.setString(2, ss.getName());
                    stmt.setString(2, ss.getName());
                    //stmt.setString(22, ss.getName());
                    //ResultSet rs = stmt1.executeQuery();
                    //if (!rs.next()) {
                    //    rs.close();
                        added = true;
                        stmt.setString(4, ss.getName());
                        stmt.setDouble(5, ss.getTProfit());
                        //stmt.setDouble(14, ss.getTProfit());
                        stmt.setInt(6, ss.getTCount());
                        //stmt.setInt(15, ss.getTCount());
                        stmt.setDouble(7, Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getPProfit().toArray()))));
                        //stmt.setDouble(16, Collections.min(ss.getPProfit()));
                        Double d = 0.0;
                        if (ss.getDrawDown().size() > 0 && ss.getDrawDownTrades().size() > 0) {
                            d = Math.min(Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getDrawDown().toArray()))), Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getDrawDownTrades().toArray()))));
                        }
                        stmt.setDouble(8, d);
                        //stmt.setDouble(17, d);
                        stmt.setDouble(9, ss.getAvgTradeLength());
                        //stmt.setDouble(18, ss.getAvgTradeLength());
                        stmt.setInt(10, ss.getYearlyGrade());
                        //stmt.setInt(19, ss.getYearlyGrade());
                        float f = 0;
                        if (ss.getLossTradeCount() > 0) {
                            f = (float) (ss.getGainTrades() * 1.0 / ss.getLossTradeCount());
                        } else {
                            f = ss.getGainTrades();
                        }
                        stmt.setDouble(11, f);
                        //stmt.setDouble(20, f);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
                        jar = new JarOutputStream(bos1);
                        JarEntry jentry = new JarEntry(ss.getName());
                        jar.putNextEntry(jentry);
                        ObjectOutput out = null;
                        out = new ObjectOutputStream(bos);
                        out.writeObject(s);
                        byte[] buff = bos.toByteArray();
                        jar.write(buff);
                        jar.closeEntry();
                        jar.close();
                        Blob b = new SerialBlob(bos1.toByteArray());
                        stmt.setBlob(12, b);
                        //stmt.setBlob(13, b);
                        stmt.addBatch();
                    //}
                } catch (IOException ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                if (added) {
                    int[] i = stmt.executeBatch();
                }
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SummaryDBInsert.class.getName()).log(Level.SEVERE, null, ex);
        }
        return;
    }

    static void closeConnection() {
        for(Connection c:con){
            DbUtils.closeQuietly(c);
        }
        con.clear();
    }

    public final static Connection setupConnection() {
        if (con == null) {
            con=new ArrayList<Connection>(100);
        }

        int cnts = count.get()%100;
        if (cnts == con.size()) {
            //if ( c == null || checkConnQ(c))
            {
                try {
                    String driver = "net.sourceforge.jtds.jdbc.Driver";
                    Class.forName(driver).newInstance();
                    Connection c = DriverManager.getConnection("jdbc:jtds:sqlserver://MAXPC;user=BackTradeData;password=oracle");
                    con.add(c);
                } catch (Exception ex) {
                    Logger.getLogger(SQLSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        count.incrementAndGet();
        Connection c=con.get(cnts);
        return c;
    }

    private boolean checkConnQ(Connection con) {
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
