/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package report.ReportWriter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import report.Summary;
import report.SummaryGroup;

/**
 *
 * @author Admin
 */
public class SummaryKryoReport implements ReportWriter
{

    int count=0;
    JarOutputStream jar;
    FileOutputStream fos;
    ObjectOutputStream outStream;
    //Kryo kr;
    ExecutorService exs;

    @Override
    public void open(String file)
    {
        try {
            
            String fname="Summary " + file ;//+ " " +sdf.format(new Date());
            SimpleDateFormat sdf=new SimpleDateFormat("ddMMyy kkmm");
            fos = null;
            fos = new FileOutputStream("Data\\"+fname +".jar",true);
            jar = new JarOutputStream(fos);
            exs=Executors.newWorkStealingPool();
            //JarEntry jentry=new JarEntry(fname+ " " + count + ".jar" );
            //        outStream = new ObjectOutputStream(fos);
    //        outStream = new ObjectOutputStream(fos);
        } catch (IOException ex) {
            Logger.getLogger(SummaryKryoReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void open(String file,String path) throws IOException
    {
        String fname = "Summary " + file;
//        kr = new Kryo();
//        kr.setRegistrationRequired(false);
//        kr.register(Summary.class);
//        kr.register(SummaryGroup.class);
//        kr.register(ArrayList.class);
//        kr.register(TreeMap.class);
//        kr.register(AbstractIndicator.class);
//        kr.register(Open.class);
//        kr.register(Close.class);
        
//        kr.setAutoReset(true);
        fos = null;
        fos = new FileOutputStream(path+"\\"+fname+ ".jar",true);
        jar = new JarOutputStream(fos);
        exs=Executors.newWorkStealingPool();
        //JarEntry jentry=new JarEntry(fname+ " " + count + ".jar" );
        //jar.putNextEntry(jentry);
//        outStream = new ObjectOutputStream(fos);
    }

    @Override
    public void close() throws  InterruptedException
    {
        try {
            //outStream.flush();
            //jar.closeEntry();
            jar.close();
            fos.close();
            exs.shutdown();
            while(!exs.isTerminated())
                Thread.sleep(5);
        } catch (IOException ex) {
            Logger.getLogger(SummaryKryoReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String writeSummFile(String file,List<Summary> summ) throws IOException
    {
        final String fname="Summary " + file + " " + count ;
        List sumL=summ;
        if(sumL.isEmpty())
            return "";
        Kryo kr = new Kryo();
//        kr.register(LinkedList.class);
//        kr.register(ArrayList.class);
//        kr.register(TreeMap.class);
//        kr.register(Double.class);
//        kr.register(Integer.class);
//        kr.register(Date.class);
        kr.register(Summary.class);
        kr.register(SummaryGroup.class);
//        kr.register(AbstractIndicator.class);
//        kr.register(Open.class);
//        kr.register(Close.class);
//        kr.register(Filters.class);
//        kr.register(SystemGrade.class);
//        kr.register(IndividualClose.class);
//        kr.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kr.setAutoReset(true);
//        String Tfile = fname + ".dat";
//        JarEntry jentry = new JarEntry(fname + ".jar");
//        jar.putNextEntry(jentry);
        //byte buffer[] = new byte[10240];
        for(Summary s:summ){
            String f=s.getName()+".dat";
            ByteArrayOutputStream bb=new ByteArrayOutputStream();
            OutputStream bos = new GZIPOutputStream(bb);
            Output out = new Output(bos);
            try {
                if(s instanceof SummaryGroup)
                    kr.writeObjectOrNull(out, s,SummaryGroup.class);
                else
                    kr.writeObjectOrNull(out, s,Summary.class);
                //out.flush();
                //out.close();
                
                JarEntry jentry = new JarEntry(s.getName() + ".jar");
                jar.putNextEntry(jentry);
                //BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                
//                while (true) {
//                    int nRead = in.read(buffer, 0, buffer.length);
//                    if (nRead <= 0) {
//                        break;
//                    }
//                    jar.write(buffer, 0, nRead);
//                }
//                in.close();
//                exs.submit(new CallableImpl(f));
                jar.write(bb.toByteArray());
            } catch (Exception ex) {
                Logger.getLogger(SummaryKryoReport.class.getName()).log(Level.SEVERE, null, ex);
            }
//            out.flush();
//            out.close();
            bb.flush();
            bb.close();
            bos.close();
            jar.closeEntry();
//            Input in=new Input(bos.toByteArray());
//            try{
//            Summary ss=(Summary)kr.readClassAndObject(in);
//            } catch(Exception ex){
//                Logger.getLogger(SummaryKryoReport.class.getName()).log(Level.SEVERE, null, ex);
//            }
            
        }
        
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        //ObjectOutput out = null;
//        Output out = new Output(bos);
//        try {
//            kr.writeObject(out, sumL);
//            //out = new ObjectOutputStream(bos);
//            //out.writeObject(sumL);
//            byte[] buff = out.toBytes();
//            jar.write(buff);
//
//        } finally {
//            out.flush();
//            out.close();
//            bos.close();
//            jar.closeEntry();
//        }
        count++;
        return fname;
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class CallableImpl implements Callable {

        private final String fname;

        private CallableImpl(String fname) {
            this.fname=fname;
        }

        @Override
        public Object call() throws Exception {
            String Tfile = fname;
            File ff = new File(Tfile);
            ff.delete();
            return null;
        }
    }

}
