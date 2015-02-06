package report;

import java.io.*;
import trade.*;
import Share.*;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JOptionPane;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;


public class ExcelReportWriter extends Thread
{
        private String fName;
        //private ArrayList pProfit;
       // private ArrayList yr;
        //private ArrayList trdCount;
        private Summary summ;
        TradeList tl;
        int buy;
        SummaryToken stoken;
        TradeCalculator tc;

        File mFile = null;
        WorkbookSettings ws;
        WritableWorkbook workbook;
        WritableSheet s;
        WritableFont wf;
        WritableCellFormat cf;


        public ExcelReportWriter(SummaryToken stoken) throws Exception
        {
            this.stoken=stoken;
            tc=TradeCalculator.getInstance();;
        }


        public ExcelReportWriter(String fName,TradeList tl, int buy,Summary sum) throws Exception
        {
            this.fName=fName;
            this.tl=tl;
            this.buy=buy;
            this.summ=sum;
            tc=TradeCalculator.getInstance();;

            //mFile = new File(fName + ".xls");
            //workbook = Workbook.createWorkbook(mFile, ws);
            //s = workbook.createSheet(sl.getSharedata(0).getShare(), 0);
        }

    @Override
	public void run()
	{
            while(true)
            {
                try
                {
                    Grp gr=stoken.read();
                    buy=gr.getBuy();
                    summ=gr.getSumm();
                    fName=gr.getFName();
                    tl=gr.getTL();
                    double yrprofit=0,tProfit=0;
                    int yrcount=0,tCount=0;
                    SimpleDateFormat sdf = new SimpleDateFormat("yy");

                    ws = new WorkbookSettings();
                    ws.setLocale(new Locale("en", "EN"));
                    wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
                    cf = new WritableCellFormat(wf);
                    cf.setWrap(true);

                    mFile = new File(fName + ".xls");
                    workbook = Workbook.createWorkbook(mFile, ws);
                    s = workbook.createSheet(summ.getShare(), 0);



                    s.setColumnView(0, 12);
                    s.setColumnView(2, 12);
                    int col = 0;
                    int row = 0;
                    s.addCell(new Label(col++, row, "Open Date"));
                    s.addCell(new Label(col++, row, "Open price"));
                    s.addCell(new Label(col++, row, "Close Date"));
                    s.addCell(new Label(col++, row, "Close Price"));
                    s.addCell(new Label(col++, row, "Profit"));
                    s.addCell(new Label(col, row++, "Difference" ));
                    col = 0;
                    //writer.write(fName + "\n");
                   // double yrprofit = 0;
                    //double tProfit = 0;
                    //int yrcount = 0;
                    //int tCount = 0;
                    //summ = new Summary(fName);
                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    for (int i = 0; i < tl.getSize(); i++)
                    {
                        Trade trd = tl.getTrade(i);
                        Calendar c1=Calendar.getInstance();
                        c1.setTime(trd.getStartDate());
                        Calendar c2=Calendar.getInstance();
                        if(i>0)
                            c2.setTime(tl.getTrade(i - 1).getStartDate());
                        if (i > 0 && c1.get(Calendar.YEAR) > c2.get(Calendar.YEAR)) {
                            col = 4;
                            s.addCell(new Number(col, row++, yrprofit));
                            s.addCell(new Number(col, row, yrcount));
                            row += 2;
                            col = 0;
                            //pProfit.add(yrprofit/100);
                            //trdCount.add(yrcount);
                            //yr.add(sdf.format(tl.getTrade(i-1).getStartDate()));
                            tProfit += yrprofit;
                            tCount += yrcount;
                            yrprofit = 0;
                            yrcount = 0;
                        }
                        col = 0;

                        s.addCell(new Label(col++, row, sdf1.format(trd.getStartDate())));
                        s.addCell(new Number(col++, row, trd.getStartPrice()));
                        s.addCell(new Label(col++, row, sdf1.format(trd.getCloseDate())));
                        s.addCell(new Number(col++, row, trd.getClosePrice()));


                        double profit;
                        if(trd.getProfit()==0)
                            trd=tc.calcDividendTrade(trd, buy,summ.getGrpShares(),summ.getIndex());
                        profit=trd.getProfit();
                        //profit = summ.calcProfit(trd.getStartPrice(), trd.getClosePrice(), buy);
                        //if(buy==1)
                        //	profit=trd.getClosePrice()-trd.getStartPrice();
                        //else
                        //	profit=trd.getStartPrice()-trd.getClosePrice();
                        s.addCell(new Number(col++, row, profit));
                        s.addCell(new Number(col++, row, (trd.getClosePrice()-trd.getStartPrice()) ));
                        if(!trd.getShare().equals(""))
                        {
                            s.addCell(new Label(col++, row, trd.getShare()));
                        }
                        row++;
                        yrprofit += profit;
                        yrcount++;
                    }

                    col = 4;
                    s.addCell(new Number(col, row++, yrprofit));
                    s.addCell(new Number(col, row, yrcount));
                    //pProfit.add(yrprofit/100);
                    //trdCount.add(yrcount);
                    //if(tl.getSize()>0)
                    //    yr.add(sdf.format(tl.getTrade(tl.getSize()-1).getStartDate()));
                    row += 2;
                    col = 0;


                    //summ.calculateTrades(tl, buy, sl);

                    s.addCell(new Label(0, row, "Total Profit"));
                    s.addCell(new Number(1, row++, summ.getTProfit()));
                    s.addCell(new Label(0, row, "Total Loss"));
                    s.addCell(new Number(1, row++, summ.getTLoss()));
                    s.addCell(new Label(0, row, "Total Gain"));
                    s.addCell(new Number(1, row++, summ.getTGain()));
                    s.addCell(new Label(0, row, "Trade Count"));
                    s.addCell(new Number(1, row++, summ.getTCount()));
                    s.addCell(new Label(0, row, "Profit Per trade"));
                    s.addCell(new Number(1, row++, summ.getTProfit() / summ.getTCount()));
                    s.addCell(new Label(0, row, "Profit Per Trade Per Day"));
                    s.addCell(new Number(1, row++, summ.getTProfit() / summ.getTotalOpenDays()));
                    s.addCell(new Label(0, row, "Gain Trade Count"));
                    s.addCell(new Number(1, row++, summ.getGainTrades()));
                    s.addCell(new Label(0, row, "Loss Trade Count"));
                    s.addCell(new Number(1, row++, summ.getLossTradeCount()));
                    s.addCell(new Label(0, row, "Gain Percentage"));
                    s.addCell(new Number(1, row++, summ.getGainPct()));
                    s.addCell(new Label(0, row, "Gain Highest Trade"));
                    s.addCell(new Number(1, row++, summ.getGainHighTrd()));
                    s.addCell(new Label(0, row, "Gain Avg Trade"));
                    s.addCell(new Number(1, row++, summ.getGainAvgTrd()));
                    s.addCell(new Label(0, row, "Loss High Trade"));
                    s.addCell(new Number(1, row++, summ.getLossHighTrd()));
                    s.addCell(new Label(0, row, "Loss Avg Trade"));
                    s.addCell(new Number(1, row++, summ.getLossAvgTrd()));
                    s.addCell(new Label(0, row, "Max Conecutive Gainers"));
                    s.addCell(new Number(1, row++, summ.getConsecGain()));
                    s.addCell(new Label(0, row, "Max Conecutive loseres"));
                    s.addCell(new Number(1, row++, summ.getConsecLoss()));
                    s.addCell(new Label(0, row, "Max Open Days"));
                    s.addCell(new Number(1, row++, summ.getMaxOpenCount()));
                    s.addCell(new Label(0, row, "Total Open Days"));
                    s.addCell(new Number(1, row++, summ.getTotalOpenDays()));
                    workbook.write();
                    workbook.close();

                    if(stoken.getsize()==0)
                        JOptionPane.showMessageDialog(null,"Done");

                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(null, ex.toString() + "Writer");
                }
            }
	}


}
