/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package report.ReportWriter;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;
import report.Summary;

/**
 *
 * @author Admin
 */
public class ExcelSummaryReport implements ReportWriter
{

    int count=0;


    public static File writeSummReport(String file,LinkedList summ) throws IOException, WriteException
    {
        DecimalFormat frmt=new DecimalFormat("0.0");
        File mFile = null;
        int max=60000;
        mFile = new File("Summary " + file + ".xls");
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook =  Workbook.createWorkbook(mFile, ws);
        WritableSheet s = workbook.createSheet("Summary", 0);
        WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableCellFormat cf = new WritableCellFormat(wf);
        cf.setWrap(true);
        int top=100;
        if(summ.size()<top)
            top=summ.size();
        else if(summ.size()>1000)
            top=summ.size()/10;
        s.addCell(new Label(0, 0, "Function"));
        s.setColumnView(0,((Summary)summ.get(0)).getName().length());
        s.addCell(new Label(1, 0, "Total Profit"));
        s.setColumnView(1,"Total Profit".length());
        s.addCell(new Label(2, 0, "Profit/Trade"));
        s.setColumnView(2,"Profit/Trade".length());
        s.addCell(new Label(3, 0, "Profit/Trade/Day"));
        s.setColumnView(3,"Profit/Trade/Day".length());
        s.addCell(new Label(4, 0, "Trade Count"));
        s.addCell(new Label(5, 0, "Wins"));
        s.addCell(new Label(6, 0, "Losers"));
        s.addCell(new Label(7, 0, "Consecutive Wins"));
        s.addCell(new Label(8, 0, "Draw Up"));
        s.addCell(new Label(9, 0, "Consecutive Losers"));
        s.addCell(new Label(10, 0, "Draw Down"));
        s.addCell(new Label(11, 0, "Open Day% "));
        s.addCell(new Label(12, 0, "Yearly Profit"));
        s.addCell(new Label(13, 0, "Open Day Freq"));
        int row=1;
        for(int i=0;i<top;i++)
        {
            Summary sum=(Summary)summ.get(i);
            s.addCell(new Label(0, row, sum.getName()));
            s.addCell(new Number(1, row, sum.getTProfit()));
            s.addCell(new Number(2, row, sum.getTProfit()/sum.getTCount()));
            s.addCell(new Number(3, row, sum.getTProfit()/sum.getTotalOpenDays()));
            s.addCell(new Number(4, row, sum.getTCount()));
            s.addCell(new Number(5, row, sum.getTGain()));
            s.addCell(new Number(6, row, sum.getTLoss()));
            s.addCell(new Number(7, row, sum.getConsecGain()));
            s.addCell(new Label(8, row, frmt.format(sum.getConsecGainProfit())  + "(" +
                                        frmt.format(sum.getMaxGainProfit()) + ")"));
            s.addCell(new Number(9, row, sum.getConsecLoss()));
            s.addCell(new Label(10, row, frmt.format(sum.getConsecLossProfit())  + "(" +
                                        frmt.format(sum.getMaxLossProfit())  + ")"));
            s.addCell(new Number(11, row, sum.getDayPercentage()));
            s.addCell(new Label(12, row, sum.getYearlyPerf()));
            s.addCell(new Label(13, row++, sum.getFreq()));

            if(row>=max)
            {
                workbook.write();
                workbook.close();

                mFile = new File("Summary " + file + "1.xls");
                ws = new WorkbookSettings();
                ws.setLocale(new Locale("en", "EN"));
                workbook =  Workbook.createWorkbook(mFile, ws);
                s = workbook.createSheet("Summary", 0);
                wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
                cf = new WritableCellFormat(wf);
                cf.setWrap(true);

                s.addCell(new Label(0, 0, "Function"));
                s.setColumnView(0,((Summary)summ.get(0)).getName().length());
                s.addCell(new Label(1, 0, "Total Profit"));
                s.setColumnView(1,"Total Profit".length());
                s.addCell(new Label(2, 0, "Profit/Trade"));
                s.setColumnView(2,"Profit/Trade".length());
                s.addCell(new Label(3, 0, "Profit/Trade/Day"));
                s.setColumnView(3,"Profit/Trade/Day".length());
                s.addCell(new Label(4, 0, "Trade Count"));
                s.addCell(new Label(5, 0, "Wins"));
                s.addCell(new Label(6, 0, "Losers"));
                s.addCell(new Label(7, 0, "Consecutive Wins"));
                s.addCell(new Label(8, 0, "Draw Up"));
                s.addCell(new Label(9, 0, "Consecutive Losers"));
                s.addCell(new Label(10, 0, "Draw Down"));
                s.addCell(new Label(11, 0, "Open Day% "));
                s.addCell(new Label(12, 0, "Yearly Profit"));
                s.addCell(new Label(13, 0, "Open Day Freq"));
                row=1;
            }

        }
        workbook.write();
        workbook.close();
        return mFile;
    }

    JarOutputStream jar;
    FileOutputStream fos;
    ObjectOutputStream outStream;
    ExecutorService exs;

    @Override
    public void open(String file)
    {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("ddMMyy kkmm");
            String fname="Summary " + file + " " +sdf.format(new Date());
            fos = null;
            fos = new FileOutputStream("Data\\"+fname+ ".jar",true);
            jar = new JarOutputStream(fos);
            exs=Executors.newWorkStealingPool();
            //JarEntry jentry=new JarEntry(fname+ " " + count + ".jar" );
            //        outStream = new ObjectOutputStream(fos);
    //        outStream = new ObjectOutputStream(fos);
        } catch (IOException ex) {
            Logger.getLogger(ExcelSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void open(String file,String path) throws IOException, WriteException
    {
        SimpleDateFormat sdf=new SimpleDateFormat("ddMMyy kkmm");
            String fname="Summary " + file + " " +sdf.format(new Date());
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
            Logger.getLogger(ExcelSummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String writeSummFile(String file,List summ) throws IOException, WriteException
    {
        final String fname="Summary " + file + " " + count ;
        List sumL=summ;
        if(sumL.isEmpty())
            return "";
        String Tfile = fname + ".dat";
        JarEntry jentry = new JarEntry(fname + ".jar");
        jar.putNextEntry(jentry);
//        FileOutputStream fos = null;
//        fos = new FileOutputStream(fname+ ".jar",true);
//        JarOutputStream jar = new JarOutputStream(fos);

//        ByteArrayOutputStream bout=new ByteArrayOutputStream();
//        ObjectOutputStream out=new ObjectOutputStream(bout);
//        out.writeObject(sumL);
//        System.out.println(bout.toString());
//        System.out.println(sumL);
//        out.flush();
//        out.close();



        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(sumL);
            byte[] buff = bos.toByteArray();
            jar.write(buff);

        } finally {
            out.flush();
            out.close();
            bos.close();
            jar.closeEntry();
        }


//        outStream = new ObjectOutputStream(new FileOutputStream(Tfile));
//        outStream.writeObject(sumL);
//        outStream.flush();
//        outStream.close();
//        byte[] buffer = new byte[4096]; // Create a buffer for copying
//        int bytesRead;
//        FileInputStream in = new FileInputStream(Tfile); // Stream to read file
//        while ((bytesRead = in.read(buffer)) != -1) {
//            jar.write(buffer, 0, bytesRead);
//        }
//        in.close();
//        jar.closeEntry();
//
//        Callable thClean = new CallableImpl(fname);
//        exs.submit(thClean);
        //thClean.setPriority(Thread.MIN_PRIORITY);
        //thClean.start();

//        jar.close();
//        fos.close();
        count++;
        //JarEntry jentry=new JarEntry(fname+ " " + count + ".jar" );
        //jar.putNextEntry(jentry);
        //outStream = new ObjectOutputStream(jar);
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
            String Tfile = fname + ".dat";
            File ff = new File(Tfile);
            ff.delete();
            return null;
        }
    }

}
