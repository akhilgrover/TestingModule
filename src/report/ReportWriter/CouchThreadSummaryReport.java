/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.ReportWriter;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import java.io.*;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.jms.BytesMessage;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.xml.soap.Text;
import jxl.write.WriteException;
import net.spy.memcached.PersistTo;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.ArrayUtils;
import report.Summary;
import report.SystemGrade;

/**
 *
 * @author Admin
 */
public class CouchThreadSummaryReport implements ReportWriter {

    private String id;
    private static CouchbaseClient client=null;
    private final List<String>arr= Arrays.asList(" ","","Indicator","I","Close","C","Open","O","Shares","Sh","Filter","Ft","Below","Be","Above","Av",".0","","Sys","S","INC","IC","EXC","EC");
    private MessageProducer sender;

    @Override
    public synchronized  void open(String file) {
        try {
            if (client == null) {
                List<URI> hosts = Arrays.asList(
                        /*new URI("http://192.168.1.4:8091/pools"),
                        new URI("http://192.168.1.12:8091/pools"),
                        new URI("http://192.168.1.18:8091/pools"),
                        new URI("http://192.168.1.26:8091/pools"),*/
                        new URI("http://192.168.1.41:8091/pools"),
                        new URI("http://192.168.1.40:8091/pools"),
                        new URI("http://192.168.1.18:8091/pools"));

                CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
                cfb.setOpTimeout(60000);
                //cfb.setShouldOptimize(true);
                cfb.setOpQueueMaxBlockTime(1000);
                CouchbaseConnectionFactory cf = cfb.buildCouchbaseConnection(hosts, "BackTrade", "");
                //client=new CouchbaseClient(hosts, "BackTrade","");
                client = new CouchbaseClient(cf);
            }
        } catch (Exception ex) {
            Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void close() throws InterruptedException, SQLException {

        client.shutdown(5, TimeUnit.SECONDS);


    }

    @Override
    @SuppressWarnings("unchecked")
    public String writeSummFile(String file, List<Summary> summ) throws IOException, WriteException, SQLException, ClassNotFoundException {
        //LinkedList<Summary>s=new LinkedList<Summary>();
        //s.addAll(summ);

        String fname = "Summary " + file;

        ArrayList<Future<Boolean>> v = new ArrayList<Future<Boolean>>(summ.size());
        double dd = 30;
        if(summ.size()>0 && summ.get(0).getName().contains("GSPC")){
            dd=45;
        }
        double wl = 1;
        double profit = 15000;
        double minYr = 20;
        int i=0;
        for (Summary ss : summ) {
            Double d = 0.0;
            if (ss.getDrawDown().size() > 0 && ss.getDrawDownTrades().size() > 0) {
                d = Math.min(Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getDrawDown().toArray()))), Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getDrawDownTrades().toArray()))));
            }
            double f = 0;
            if (ss.getLossTradeCount() > 0) {
                f = (ss.getGainTrades() * 1.0 / ss.getLossTradeCount());
            } else {
                f = ss.getGainTrades();
            }
            SystemGrade sg=ss.getSg();
            if(d<-1*dd || ss.getTProfit()<profit || f<=wl || Collections.min(Arrays.asList(ArrayUtils.toObject(ss.getPProfit().toArray())))<-1*minYr)
            {
                continue;
            } else {

                ss.setId(id);
                //ss.getOpenFilter().clear();

                //String pp = JsonWriter.objectToJson(ss);
                //String ppNew=g.toJson(ss);
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    GZIPOutputStream jar = new GZIPOutputStream(bos);
                    ObjectOutput out = null;
                    out = new ObjectOutputStream(jar);
                    out.writeObject(ss);
                    out.close();
                    BytesMessage msg = new ActiveMQBytesMessage();
                    msg.writeBytes(bos.toByteArray());
                    msg.setLongProperty("id", Long.parseLong(id));
                    sender.send(msg);
                    /*String nam = ss.getName();
                    for (int j = 0; j < arr.size(); j += 2) {
                        nam = nam.replace(arr.get(j), arr.get(j + 1));
                    }*/

                    //v.add(client.set(nam, 0, pp));
//                    try {
//                        TextMessage resp = new ActiveMQTextMessage();
//                        resp.setText(pp);
//                        resp.setLongProperty("id", Long.parseLong(id));
//                        sender.send(resp);
//
//                    } catch (Exception ex2) {
//                    }
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                    Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//        try {
//
//            for (Future<Boolean> fu : v) {
//                if(!fu.get(1, TimeUnit.MINUTES))
//                    System.out.print("Not Added" + fu.toString());
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return fname;
    }

    public void writeSummFile(Summary sAdd,Summary sReplace,String t) throws IOException, WriteException, SQLException, ClassNotFoundException {

        ArrayList<Future<Boolean>> v = new ArrayList<Future<Boolean>>();
        int i=0;
        if (sAdd != null) {
            Double d = 0.0;
            {

                sAdd.setId("G"+sReplace.getId());
                //ss.getOpenFilter().clear();
                String pp = JsonWriter.objectToJson(sAdd);
                //String ppNew=g.toJson(ss);
                try {
                    String nam = sAdd.getName();
                    for (int j = 0; j < arr.size(); j += 2) {
                        nam = nam.replace(arr.get(j), arr.get(j + 1));
                    }
                    String nam1 = sReplace.getName();
                    for (int j = 0; j < arr.size(); j += 2) {
                        nam1 = nam1.replace(arr.get(j), arr.get(j + 1));
                    }
                    v.add(client.set(nam, 0, pp, PersistTo.ONE));
                    v.add(client.set(nam1, 0,JsonWriter.objectToJson(sReplace),PersistTo.ONE));

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        } else {
            try {
                //client.delete(t).get();
            } catch (Exception ex) {
                //Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {

            for (Future<Boolean> fu : v) {
                if(!fu.get(1, TimeUnit.MINUTES))
                    System.out.print("Not Added" + fu.toString());
            }
        } catch (Exception ex) {
            Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

       /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id+"";
    }

    @Override
    public void refresh() {
        try {
            close();
            open("");
        } catch (SQLException ex) {
            Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(CouchThreadSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Summary getSumary(String t) throws IOException {

        /*Object val=client.get(t);
        //retry
        if(val==null)
            val=client.get(t);
        if(val==null)
            return null;*/
        return (Summary)JsonReader.jsonToJava(t);
    }

    /**
     * @return the sender
     */
    public MessageProducer getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(MessageProducer sender) {
        this.sender = sender;
    }
}
