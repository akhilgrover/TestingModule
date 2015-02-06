/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.ReportWriter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.*;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import jxl.write.WriteException;
import org.apache.commons.lang3.ArrayUtils;
import report.Summary;
import report.SystemGrade;

/**
 *
 * @author Admin
 */
public class SQLThreadSummaryReport implements ReportWriter {

    ExecutorService exs;
    private PreparedStatement stmt;
    private static Connection conn;
    private Long id;
    private static BlockingQueue<ArrayList<ArrayList>> q=new LinkedBlockingDeque<ArrayList<ArrayList>>(500);
    private SummaryDBArrSingleInsert[] sas=new SummaryDBArrSingleInsert[2];
    //String range;

    @Override
    public void open(String file) throws SQLException {
        //range="Summary " + file ;

        //setupConnection();
        //String sql = "insert into Summary(Range,sys,summary,profit) values('" + range + "', ?, ?, ?)";
        //stmt = conn.prepareStatement(sql);
        //exs=Executors.newSingleThreadExecutor();
        //exs = Executors.newFixedThreadPool(20);

        exs=Executors.newCachedThreadPool();

        for (int i = 0; i < sas.length; i++) {
            SummaryDBArrSingleInsert sa = new SummaryDBArrSingleInsert(q);
            sas[i] = sa;
            exs.submit(sa);
        }

    }

    @Override
    public void close() throws InterruptedException, SQLException {
        //DbUtils.closeQuietly(conn);
        //conn=null;
        for (int i = 0; i < sas.length; i++) {
            sas[i].close=true;
        }
        exs.shutdown();
        while (!exs.isTerminated()) {

            Thread.sleep(50);
        }
        for (int i = 0; i < sas.length; i++) {
            sas[i].closeConnection();
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public String writeSummFile(String file, List<Summary> summ) throws IOException, WriteException, SQLException, ClassNotFoundException {
        //LinkedList<Summary>s=new LinkedList<Summary>();
        //s.addAll(summ);
        if (id == null && summ.size() > 0) {

            Summary s = summ.get(0);
            String[] ds = s.getShare().split(" ");
            String ind = ds[2];
            String sys = ds[0] + ds[1];
            String[] in = s.getIndicator().toString().split(" ");
            String[] op = s.getOpen().toString().split(" ");
            String[] cl = s.getClose().toString().split(" ");
            String b = "B";
            if (s.getBuy() == 0) {
                b = "S";
            }
            String filter = "";
            if (s.getOpenFilter() != null) {
                String[] ff = s.getOpenFilter().toString().split(" ");
                filter = ff[ff.length - 1];
            }
            double sl = s.getSStopLoss();
            int lb = s.getBackPeriod();


            String sql1 = "Select rangeid from ranges with (ROWLOCK) where SIndex='" + ind + "' and Sys='" + sys + "' and indicator='" + in[in.length - 1] + "' and opening='" + op[op.length - 1] + "' and closing='" + cl[cl.length - 1] + "' and buysell='" + b + "' and lookback=" + lb + " and filter='" + filter + "' and sl=" + sl + ";";
            String sql2 = "insert into ranges([SIndex], [Sys], [Indicator], [Opening], [Closing], [BuySell], [Filter], [SL], [LookBack], [eTime]) values('" + ind + "','" + sys + "','" + in[in.length - 1] + "','" + op[op.length - 1] + "','" + cl[cl.length - 1] + "','" + b + "','" + filter + "'," + sl + "," + lb + ",GETDATE());";
            setupConnection();
            stmt = conn.prepareStatement(sql1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                setId((Long) rs.getLong(1));
            } else {
                try{
                    conn.prepareStatement(sql2).executeUpdate();
                } catch(SQLException se){
                }
                rs = stmt.executeQuery();
                if (rs.next()) {
                    setId((Long) rs.getLong(1));
                } else {
                    Logger.getLogger(SQLThreadSummaryReport.class.getName()).log(Level.SEVERE, sql1, sql2);
                }
            }
            //conn.close();



        }
//        List<Summary> s = null;
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutput out = null;
//        out = new ObjectOutputStream(bos);
//        out.writeObject(summ);
//        ByteArrayInputStream bi = new ByteArrayInputStream(bos.toByteArray());
//        ObjectInputStream in = new ObjectInputStream(bi);
//        s = (List<Summary>) in.readObject();
        String fname = "Summary " + file;

        ArrayList<ArrayList> arr = new ArrayList<ArrayList>(summ.size());
        double dd = 30;
        double wl = 1;
        double profit = 10000;
        double minYr = 20;
        int i=0;
        for (Object s : summ) {
            Summary ss = ((Summary) s);
            ArrayList arr1=new ArrayList(9);
            arr1.add(ss.getName());
            arr1.add(ss.getTProfit());
            arr1.add(ss.getTCount());
            arr1.add(Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getPProfit().toArray()))));
            Double d = 0.0;
            if (ss.getDrawDown().size() > 0 && ss.getDrawDownTrades().size() > 0) {
                d = Math.min(Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getDrawDown().toArray()))), Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getDrawDownTrades().toArray()))));
            }
            arr1.add(d);
            arr1.add(ss.getAvgTradeLength());
            arr1.add(ss.getYearlyGrade());
            double f = 0;
            if (ss.getLossTradeCount() > 0) {
                f = (ss.getGainTrades() * 1.0 / ss.getLossTradeCount());
            } else {
                f = ss.getGainTrades();
            }
            arr1.add(f);
            SystemGrade sg=ss.getSg();
            if(d<-1*dd || ss.getTProfit()<profit || f<=wl || Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getPProfit().toArray())))<-1*minYr)
            {
                //arr1.add(null);
                continue;
            } else {
                /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
                JarOutputStream jar = new JarOutputStream(bos1);
                JarEntry jentry = new JarEntry(ss.getName());
                jar.putNextEntry(jentry);
                ObjectOutput out = null;
                out = new ObjectOutputStream(bos);
                out.writeObject(ss);
                byte[] buff = bos.toByteArray();
                jar.write(buff);
                jar.closeEntry();
                jar.close();
                Blob b = new SerialBlob(bos1.toByteArray());
                arr1.add(b);*/
                Kryo kr = new Kryo();
                kr.register(Summary.class);
                Output o = new Output(1024 * 500, 1024 * 1024 * 10);
                kr.writeObject(o, ss);
                o.close();
                Blob b = new SerialBlob(o.toBytes());
                arr1.add(b);
            }
            arr1.add(id);
            arr.add(arr1);
        }
        try {
            //Runnable thClean = new SummaryDBInsert(s, id);
            //Runnable thClean = new SummaryDBArrInsert(arr, id);
            //exs.execute(thClean);
            if(arr.size()>0)
                q.put(arr);
        } catch (InterruptedException ex) {
            Logger.getLogger(SQLThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fname;
    }

    public static Connection setupConnection() {
        if (conn == null || checkConnQ(conn)) {
            try {
                String driver = "net.sourceforge.jtds.jdbc.Driver";
                Class.forName(driver).newInstance();
                conn = DriverManager.getConnection("jdbc:jtds:sqlserver://maxpc;user=BackTradeData;password=oracle");
            } catch (Exception ex) {
                Logger.getLogger(SQLThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return conn;
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

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    public void refresh() {
        try {
            close();
            open("");
        } catch (SQLException ex) {
            Logger.getLogger(SQLThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SQLThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
