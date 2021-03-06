/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import Share.ShareList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import indicator.AbstractIndicator;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import ma.MA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import trade.Trade;
import trade.TradeCalculator;
import trade.TradeList;
import trade.TradeParameters;
import trade.close.AbstractIndividualClose;
import trade.close.Close;
import trade.close.IndividualClose;
import trade.open.Open;

/**
 *
 * @author admin
 */
public class SystemGrader {

    private Map<String, ShareList> sSL;
    private TradeCalculator tc;
    private final int RANGE_DAYS = 40;
    //private final int GEN_RANGE_DAYS=15;
    private DecimalFormat df = new DecimalFormat("##.##");
    //private BasicShareDB bsDB;
    private ConcurrentHashMap<String, Summary> tradeProfits;
    //private ConcurrentHashMap<String,Double> tlProfits;
    private TObjectDoubleHashMap<String> tlProfits;
    //private final int max=9;

    public SystemGrader() {
        try {
            //sSL=new FastMap<String, ShareList>().shared().setKeyComparator(FastComparator.STRING);;

            //sSL=new HashMap<String, ShareList>(5000);
            tc = TradeCalculator.getInstance();
            //sSL= tc.getShList();
            //bsDB = new BasicShareDB();
            tradeProfits = new ConcurrentHashMap<String, Summary>(10000);
            //tlProfits=new ConcurrentHashMap<String, Double>();
            tlProfits = new TObjectDoubleHashMap<String>();
        } catch (Exception ex) {
            Logger.getLogger(SystemGrader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public SystemGrader(TradeCalculator tc) {
        try {
            //sSL=new FastMap<String, ShareList>().shared().setKeyComparator(FastComparator.STRING);;
            //sSL=new HashMap<String, ShareList>(5000);

            this.tc = tc;
            //sSL=tc.getShList();
            //bsDB = new BasicShareDB();
            tradeProfits = new ConcurrentHashMap<String, Summary>(50000);
            //tlProfits=new ConcurrentHashMap<String, Double>();
            tlProfits = new TObjectDoubleHashMap<String>();
        } catch (Exception ex) {
            Logger.getLogger(SystemGrader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public SystemGrader(ConcurrentHashMap<String, Summary> tradeProfits, ConcurrentHashMap<String, Double> tlProfits, TradeCalculator tc) {
        try {
            //sSL=tc.getShList();//new HashMap<String, ShareList>(5000);
            this.tc = tc;//=TradeCalculator.getInstance();;
            //bsDB = new BasicShareDB();
            this.tradeProfits = tradeProfits;
            //this.tlProfits = tlProfits;
            this.tlProfits = new TObjectDoubleHashMap<String>();
        } catch (Exception ex) {
            Logger.getLogger(SystemGrader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            //sSL=null;
            tradeProfits = null;
            tlProfits = null;
            //tc.close();
            //bsDB.close();
        } catch (Exception ex) {
            Logger.getLogger(SystemGrader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean isOpen() {
        return sSL != null;
    }

    public SystemGrade gradeSystem(Summary sum, String sys) throws Exception {
        TradeParameters tp = sum.getTradeParams();
        if (tradeProfits.size() > 500) {
            tradeProfits.clear();
        }
        //tradeProfits=new ConcurrentHashMap<String, Summary>();
        tradeProfits.put(tp.toString(), sum);
        sum.setSg(gradeSystem(tp, sys, 1));
        return sum.getSg();
    }

    public SystemGrade gradeSystem(Summary sum, String sys, int min) throws Exception {
        TradeParameters tp = sum.getTradeParams();
        if (tradeProfits.size() > 500) {
            tradeProfits.clear();
        }
        //tradeProfits=new ConcurrentHashMap<String, Summary>();
        tradeProfits.put(tp.toString(), sum);
        sum.setSg(gradeSystem(tp, sys, min));
        return sum.getSg();
    }

    @SuppressWarnings("unchecked")
    public SystemGrade gradeSystem(Summary sum) throws Exception {
        SystemGrade sg = new SystemGrade();
        TradeParameters tp = sum.getTradeParams();
        int rsi = (Integer) sum.getiClose().getParams().get(1);
        int threshold = (Integer) sum.getiClose().getParams().get(2);
        int startRsi = (int) (rsi - (rsi * 0.15));
        int endRsi = (int) (rsi + (rsi * 0.15));
        int startTh = (int) (threshold - (threshold * 0.15));
        int endTh = (int) (threshold + (threshold * 0.15));
        double avg = 0.0, avgL = 0.0;
        int cnt = 0;
        if (endRsi - rsi < 2) {
            endRsi = rsi + 2;
        }
        if (rsi - startRsi < 2 && rsi > 3) {
            startRsi = rsi - 2;
        }
        if (startRsi == 1) {
            startRsi = 2;
        }
        if (endTh - threshold < 2) {
            endTh = threshold + 2;
        }
        if (rsi - startTh < 2) {
            startTh = threshold - 2;
        }

        for (int i = startRsi; i <= endRsi; i++) {
            for (int j = startTh; j <= endTh; j++) {
                HashMap hmParams = new HashMap();
                hmParams.put(1, i);
                hmParams.put(2, j);
                hmParams.put(3, sum.getiClose().getParams().get(3));
                IndividualClose iclose = new AbstractIndividualClose(hmParams);
                TradeList tl = tc.getTradeList(tp);
                tl = tc.indicatorStopTradeList(tl, iclose, !tp.isBuy());
                Summary sumN = new Summary(tp.getSl().getShare(), (AbstractIndicator) tp.getIndList(), tp.getOpen(), tp.getCloseSL().getShare(), (AbstractIndicator) tp.getIndCloseList(), tp.getClose(), tp.getOpenFilter(), null, tp.getBuy(), tp.getBackPeriod(), tp.getConsec(), tp.getShareStop(), tp.getIndStop(), tp.getIndex(), tp.getiClose(), tp.getTopCap());
                sumN.calculateTrades(tl, tc.getSL(tp.getIndex(),tp.getIndex()), tp.getShareCount(), tc, false);
                //double totalProfit=tradeListProfit(tl,tp);
                avg += sumN.getTProfit();
                avgL += sumN.getAvgTradeLength();
                cnt++;

            }
        }
        avg /= cnt;
        avgL = avgL * 1.0 / cnt;

        sg.setAvgPercent(avg * 100.0 / sum.getTProfit());
        sg.setAvgLength(avgL);
        return sg;
    }

    public SystemGrade gradeSystem(TradeParameters tp, String sys) throws Exception {
        return gradeSystem(tp, sys, 1);
    }

    @SuppressWarnings("unchecked")
    public SystemGrade gradeSystem(TradeParameters tp, String sys, int min) throws Exception {
        int max = 7;
        SystemGrade ret = new SystemGrade();
        ArrayList<Double> pArr = new ArrayList<Double>();
        //double mProfit=Double.parseDouble(maxProfit.getText())*100;
        TradeParameters tpnew = null;
        /*if(sSL.size()>5000)
        sSL.clear();
        if(tp.getSl().getSize()>0 && !sSL.containsKey(tp.getSl().getShare()))
        sSL.put(tp.getSl().getShare(), tp.getSl());
        if(tp.getCloseSL().getSize()>0 && !sSL.containsKey(tp.getCloseSL().getShare()))
        sSL.put(tp.getCloseSL().getShare(), tp.getCloseSL());
         *
         */

        //change share if any
        ShareList sl = tc.getSL(tp.getSl().getShare(), tp.getIndex());
        ShareList clssl = tc.getSL(tp.getCloseSL().getShare(), tp.getIndex());//tp.getCloseSL();
        if (sl.getShare().contains(" ")) {
            String shares[] = sl.getShare().split(" ");
            int days = Integer.parseInt(shares[3]);
            if (days > 1) {
                int pm = Double.valueOf(Math.ceil((RANGE_DAYS / 100.0) * days)).intValue();
                if (pm > max) {
                    pm = max;
                }

                int start = days - pm;

                for (int i = start; i < start + (pm * 2) + 1; i++) {
                    tpnew = TradeParameters.buildParameter(tp);
                    MA ma = new MA(i, MA.Simple);
                    String str = shares[0] + " " + shares[1] + " " + shares[2] + " " + ma.toString();
                    /*if(sSL.containsKey(str))
                    sl=sSL.get(str);
                    else
                    {
                    sl= bsDB.getShareData(shares[0]+ " " + shares[1], shares[2],ma,false);
                    sSL.put(str, sl);
                    }*/
                    ShareList slN = tc.getSL(str, tp.getIndex());
                    tpnew.setSl(slN);
                    if (tp.getSl().getShare().equals(clssl.getShare())) {
                        tpnew.setCloseSL(slN);
                    }
                    double totalProfit = 0;

                    if (!tradeProfits.containsKey(tpnew.toString())) {
                        if (tlProfits.containsKey(tpnew.toString())) {
                            totalProfit = tlProfits.get(tpnew.toString());
                        } else {
                            TradeList tl = tc.getTradeList(tpnew);
                            totalProfit = tradeListProfit(tl, tpnew);
                        }
                    } else {
                        totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                    }
                    if (!tlProfits.containsKey(tpnew.toString())) {
                        tlProfits.put(tpnew.toString(), totalProfit);

                    }

                    pArr.add(totalProfit);
                }
                Integer[] scr;
                if (pm > 7) {
                    scr = getscore(pArr, "B", tp.getBuy());
                } else {
                    scr = getscore(pArr, sys, tp.getBuy());
                }
                ret.setoShare(scr);
                if (scr[0] < min || scr[1] < min) {
                    return ret;
                }
            }
            else{
                Integer[] scr={5,5,100};
                ret.setoShare(scr);
                tpnew = TradeParameters.buildParameter(tp);
            }
        }
        //change close share
        if (clssl.getShare().contains(" ") && !tp.getSl().getShare().equals(clssl.getShare())) {

            pArr = new ArrayList<Double>();
            String shares[] = clssl.getShare().split(" ");
            int days = Integer.parseInt(shares[3]);
            if (days >= 1) {
                int pm = Double.valueOf(Math.ceil((RANGE_DAYS / 100.0) * days)).intValue();
                if (pm > max) {
                    pm = max;
                }
                int start = days - pm;

                for (int i = start; i < start + (pm * 2) + 1; i++) {
                    tpnew = TradeParameters.buildParameter(tp);
                    MA ma = new MA(i, MA.Simple);
                    //clssl= bsDB.getShareData(shares[0], shares[1],ma,false);
                    String str = shares[0] + " " + shares[1] + " " + shares[2] + " " + ma.toString();
                    /*if(sSL.containsKey(str))
                    clssl=sSL.get(str);
                    else
                    {
                    clssl= bsDB.getShareData(shares[0]+ " " + shares[1], shares[2],ma,false);
                    sSL.put(str, clssl);
                    }*/
                    //tpnew.setSl(sl);
                    //if(tp.getSl().getShare().equals(clssl.getShare()))
                    ShareList clsSlN = tc.getSL(str, tp.getIndex());
                    tpnew.setCloseSL(clsSlN);
                    double totalProfit = 0;
                    if (!tradeProfits.containsKey(tpnew.toString())) {
                        if (tlProfits.containsKey(tpnew.toString())) {
                            totalProfit = tlProfits.get(tpnew.toString());
                        } else {
                            TradeList tl = tc.getTradeList(tpnew);
                            totalProfit = tradeListProfit(tl, tpnew);
                        }
                    } else {
                        totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                    }
                    if (!tlProfits.containsKey(tpnew.toString())) {
                        tlProfits.put(tpnew.toString(), totalProfit);

                    }
                    pArr.add(totalProfit);

                }
                Integer[] scr;
                if (pm > 7) {
                    scr = getscore(pArr, "B", tp.getBuy());
                } else {
                    scr = getscore(pArr, sys, tp.getBuy());
                }
                ret.setcShare(scr);
                if (scr[0] < min || scr[1] < min) {
                    return ret;
                }
            } else {
                Integer[] scr = {5, 5, 100};
                ret.setoShare(scr);
            }
        }

        //change open indicator if any

        AbstractIndicator absInd = (AbstractIndicator) tpnew.getIndList();
        HashMap hm = absInd.getParams();
        if (!hm.isEmpty()) {
            pArr = new ArrayList<Double>();
            int start = (Integer) hm.get(1);
            int pm = Double.valueOf(Math.ceil((RANGE_DAYS / 100.0) * start)).intValue();
            if (pm > max) {
                pm = max;
            }
            if (start - pm > 0) {
                for (int i = start - pm; i < start + pm + 1; i++) {
                    if (i <= 1) {
                        double totalProfit = 0;
                        if (!tradeProfits.containsKey(tp.toString())) {
                            if (tlProfits.containsKey(tp.toString())) {
                                totalProfit = tlProfits.get(tp.toString());
                            } else {
                                TradeList tl = tc.getTradeList(tpnew);
                                totalProfit = tradeListProfit(tl, tpnew);
                            }
                        } else {
                            totalProfit = tradeProfits.get(tp.toString()).getTProfit();
                        }
                        if (!tlProfits.containsKey(tp.toString())) {
                            tlProfits.put(tp.toString(), totalProfit);
                        }
                        pArr.add(totalProfit);
                        continue;
                    }
                    tpnew = TradeParameters.buildParameter(tp);
                    HashMap hmnew = new HashMap(hm);
                    hmnew.put(1, i);
                    if (hm.size() > 1) {
                        hmnew.put(2, hm.get(2));
                    }
                    //absInd=absInd.getClass().getConstructor(parameterTypes);
                    //Constructor<?>[] cnt=absInd.getClass().getConstructors();
                    absInd = absInd.getClass().newInstance();
                    absInd.init(hmnew);
                    tpnew.setIndList(absInd);
                    double totalProfit = 0;
                    if (!tradeProfits.containsKey(tpnew.toString())) {
                        if (tlProfits.containsKey(tpnew.toString())) {
                            totalProfit = tlProfits.get(tpnew.toString());
                        } else {
                            TradeList tl = tc.getTradeList(tpnew);
                            totalProfit = tradeListProfit(tl, tpnew);
                        }
                    } else {
                        totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                    }
                    if (!tlProfits.containsKey(tpnew.toString())) {
                        tlProfits.put(tpnew.toString(), totalProfit);
                    }
                    pArr.add(totalProfit);
                }
                Integer[] scr;
                if (pm > 7) {
                    scr = getscore(pArr, "B", tp.getBuy());
                } else {
                    scr = getscore(pArr, sys, tp.getBuy());
                }
                ret.setoInd(scr);
                if (scr[0] < min || scr[1] < 2) {
                    return ret;
                }
            }
        }

        //change close indicator
        if (!tp.getIndList().toString().equals(tp.getIndCloseList().toString())) {

            pArr = new ArrayList<Double>();
            AbstractIndicator absIndCls = (AbstractIndicator) tpnew.getIndCloseList();
            hm = absIndCls.getParams();
            if (!hm.isEmpty()) {
                int start = (Integer) hm.get(1);
                int pm = Double.valueOf(Math.ceil((RANGE_DAYS / 100.0) * start)).intValue();
                if (pm > max) {
                    pm = max;
                }
                if (start - pm > 0) {
                    for (int i = start - pm; i < start + pm + 1; i++) {
                        tpnew = TradeParameters.buildParameter(tp);
                        HashMap hmnew = new HashMap(hm);
                        hmnew.put(1, i);
                        if (hm.size() > 1) {
                            hmnew.put(2, hm.get(2));
                        }
                        absIndCls = absIndCls.getClass().newInstance();
                        absIndCls.init(hmnew);
                        tpnew.setIndCloseList(absIndCls);
                        double totalProfit = 0;
                        if (!tradeProfits.containsKey(tpnew.toString())) {
                            if (tlProfits.containsKey(tpnew.toString())) {
                                totalProfit = tlProfits.get(tpnew.toString());
                            } else {
                                TradeList tl = tc.getTradeList(tpnew);
                                totalProfit = tradeListProfit(tl, tpnew);
                            }
                        } else {
                            totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                        }
                        if (!tlProfits.containsKey(tpnew.toString())) {
                            tlProfits.put(tpnew.toString(), totalProfit);
                        }
                        pArr.add(totalProfit);
                    }
                    Integer[] scr;
                    if (pm > 7) {
                        scr = getscore(pArr, "B", tp.getBuy());
                    } else {
                        scr = getscore(pArr, sys, tp.getBuy());
                    }
                    ret.setcInd(scr);
                    if (scr[0] < min || scr[1] < 2) {
                        return ret;
                    }
                }
            }
        }

        //change open

        Open opn = tp.getOpen();
        int opParams = opn.getParamCount();
        hm = opn.getParams();
        for (int i = 1; i <= opParams; i++) {
            Double start = (Double) hm.get(i);
            int pm = Double.valueOf(Math.ceil((RANGE_DAYS / 100.0) * start)).intValue();
            if (pm > max) {
                pm = max;
            }
            if (start - pm > 0 && start > 7) {
                pArr = new ArrayList<Double>();
                for (int j = start.intValue() - pm; j < start.intValue() + pm + 1; j++) {
                    tpnew = TradeParameters.buildParameter(tp);
                    HashMap hmnew = new HashMap(hm);
                    hmnew.put(i, j * 1.0);
                    opn = opn.getClass().newInstance();
                    opn.setParams(hmnew);
                    opn.setBuy(tp.getBuy());
                    tpnew.setOpen(opn);
                    double totalProfit = 0;
                    if (!tradeProfits.containsKey(tpnew.toString())) {
                        if (tlProfits.containsKey(tpnew.toString())) {
                            totalProfit = tlProfits.get(tpnew.toString());
                        } else {
                            TradeList tl = tc.getTradeList(tpnew);
                            totalProfit = tradeListProfit(tl, tpnew);
                        }
                    } else {
                        totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                    }
                    if (!tlProfits.containsKey(tpnew.toString())) {
                        tlProfits.put(tpnew.toString(), totalProfit);

                    }
                    pArr.add(totalProfit);
                }
                Integer[] scr;
                if (pm > 7) {
                    scr = getscore(pArr, "B", tp.getBuy());
                } else {
                    scr = getscore(pArr, sys, tp.getBuy());
                }
                ret.setOpn(i - 1, scr);
                if (scr[0] < min && i == 1 || scr[0] < 2 || scr[1] < 2) {
                    return ret;
                }
            } else {
                if (start <= 3) {
                    BigDecimal badd = new BigDecimal(0.2);
                    BigDecimal bstart = new BigDecimal(start);
                    BigDecimal bend = bstart.add(badd.multiply(BigDecimal.valueOf(4)));
                    bstart = bstart.subtract(badd.multiply(BigDecimal.valueOf(4)));
                    pArr = new ArrayList<Double>();
                    for (BigDecimal j = bstart; j.compareTo(bend) <= 0; j = j.add(badd)) {
                        tpnew = TradeParameters.buildParameter(tp);
                        HashMap hmnew = new HashMap(hm);
                        hmnew.put(i, j.doubleValue());
                        opn = opn.getClass().newInstance();
                        opn.setParams(hmnew);
                        opn.setBuy(tp.getBuy());
                        tpnew.setOpen(opn);
                        double totalProfit = 0;
                        if (!tradeProfits.containsKey(tpnew.toString())) {
                            if (tlProfits.containsKey(tpnew.toString())) {
                                totalProfit = tlProfits.get(tpnew.toString());
                            } else {
                                TradeList tl = tc.getTradeList(tpnew);
                                totalProfit = tradeListProfit(tl, tpnew);
                            }
                        } else {
                            totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                        }
                        if (!tlProfits.containsKey(tpnew.toString())) {
                            tlProfits.put(tpnew.toString(), totalProfit);

                        }
                        pArr.add(totalProfit);
                    }
                    Integer[] scr;
                    scr = getscore(pArr, sys, tp.getBuy());
                    ret.setOpn(i - 1, scr);
                    if (scr[0] < min && i == 1 || scr[0] < 2 || scr[1] < 2) {
                        return ret;
                    }
                    //ret.setOpn(i-1,getscore(pArr,sys));
                } else if (start > 3) {
                    BigDecimal badd = new BigDecimal(0.2);
                    BigDecimal bstart = new BigDecimal(start);
                    BigDecimal bend = bstart.add(badd.multiply(BigDecimal.valueOf(0005)));
                    bstart = bstart.subtract(badd.multiply(BigDecimal.valueOf(5)));
                    pArr = new ArrayList<Double>();
                    for (BigDecimal j = bstart; j.compareTo(bend) <= 0; j = j.add(badd)) {
                        tpnew = TradeParameters.buildParameter(tp);
                        HashMap hmnew = new HashMap(hm);
                        hmnew.put(i, j.doubleValue());
                        opn = opn.getClass().newInstance();
                        opn.setParams(hmnew);
                        opn.setBuy(tp.getBuy());
                        tpnew.setOpen(opn);
                        double totalProfit = 0;
                        if (!tradeProfits.containsKey(tpnew.toString())) {
                            if (tlProfits.containsKey(tpnew.toString())) {
                                totalProfit = tlProfits.get(tpnew.toString());
                            } else {
                                TradeList tl = tc.getTradeList(tpnew);
                                totalProfit = tradeListProfit(tl, tpnew);
                            }
                        } else {
                            totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                        }
                        if (!tlProfits.containsKey(tpnew.toString())) {
                            tlProfits.put(tpnew.toString(), totalProfit);

                        }
                        pArr.add(totalProfit);
                    }
                    Integer[] scr;
                    scr = getscore(pArr, sys, tp.getBuy());
                    ret.setOpn(i - 1, scr);
                    if (scr[0] < min && i == 1 || scr[0] < 2 || scr[1] < 2) {
                        return ret;
                    }
                    //ret.setOpn(i-1,getscore(pArr,sys));
                }
            }
        }
        if (opParams == 0) {
            Integer[] scr = {5, 5, 0};
            ret.setOpn(0, scr);
        }

        //change close
        Close cls = tp.getClose();
        int clParams = cls.getParamCount();
        hm = cls.getParams();
        for (int i = 1; i <= clParams; i++) {
            Double start = (Double) hm.get(i);
            int pm = Double.valueOf(Math.ceil((RANGE_DAYS / 100.0) * start)).intValue();
            if (pm > max) {
                pm = max;
            }
            if (start - pm > 0 && start > 7) {
                pArr = new ArrayList<Double>();
                for (int j = start.intValue() - pm; j < start.intValue() + pm + 1; j++) {
                    tpnew = TradeParameters.buildParameter(tp);
                    HashMap hmnew = new HashMap(hm);
                    hmnew.put(i, j * 1.0);

                    cls = cls.getClass().newInstance();
                    cls.setParams(hmnew);
                    cls.setBuy(tp.getBuy());
                    tpnew.setClose(cls);
                    double totalProfit = 0;
                    if (!tradeProfits.containsKey(tpnew.toString())) {
                        if (tlProfits.containsKey(tpnew.toString())) {
                            totalProfit = tlProfits.get(tpnew.toString());
                        } else {
                            TradeList tl = tc.getTradeList(tpnew);
                            totalProfit = tradeListProfit(tl, tpnew);
                        }
                    } else {
                        totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                    }
                    if (!tlProfits.containsKey(tpnew.toString())) {
                        tlProfits.put(tpnew.toString(), totalProfit);

                    }
                    pArr.add(totalProfit);
                }
                Integer[] scr;
                if (pm > 7) {
                    scr = getscore(pArr, "B", tp.getBuy());
                } else {
                    scr = getscore(pArr, sys, tp.getBuy());
                }
                ret.setCls(i - 1, scr);
                if (scr[0] < min && i == 1 || scr[0] < 2 || scr[1] < 2) {
                    return ret;
                }
            } else {
                if (start <= 3) {
                    BigDecimal badd = new BigDecimal(0.2);
                    BigDecimal bstart = new BigDecimal(start);
                    BigDecimal bend = bstart.add(badd.multiply(BigDecimal.valueOf(4)));
                    bstart = bstart.subtract(badd.multiply(BigDecimal.valueOf(4)));
                    pArr = new ArrayList<Double>();
                    for (BigDecimal j = bstart; j.compareTo(bend) <= 0; j = j.add(badd)) {
                        tpnew = TradeParameters.buildParameter(tp);
                        HashMap hmnew = new HashMap(hm);
                        hmnew.put(i, j.doubleValue());
                        cls = cls.getClass().newInstance();
                        cls.setParams(hmnew);
                        cls.setBuy(tp.getBuy());
                        tpnew.setClose(cls);
                        double totalProfit = 0;
                        if (!tradeProfits.containsKey(tpnew.toString())) {
                            if (tlProfits.containsKey(tpnew.toString())) {
                                totalProfit = tlProfits.get(tpnew.toString());
                            } else {
                                TradeList tl = tc.getTradeList(tpnew);
                                totalProfit = tradeListProfit(tl, tpnew);
                            }
                        } else {
                            totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                        }
                        if (!tlProfits.containsKey(tpnew.toString())) {
                            tlProfits.put(tpnew.toString(), totalProfit);

                        }
                        pArr.add(totalProfit);
                    }
                    Integer[] scr;
                    scr = getscore(pArr, sys, tp.getBuy());
                    ret.setCls(i - 1, scr);
                    if (scr[0] < min && i == 1 || scr[0] < 2 || scr[1] < 2) {
                        return ret;
                    }
                } else if (start > 3) {
                    BigDecimal badd = new BigDecimal(0.2);
                    BigDecimal bstart = new BigDecimal(start);
                    BigDecimal bend = bstart.add(badd.multiply(BigDecimal.valueOf(5)));
                    bstart = bstart.subtract(badd.multiply(BigDecimal.valueOf(5)));
                    pArr = new ArrayList<Double>();
                    for (BigDecimal j = bstart; j.compareTo(bend) <= 0; j = j.add(badd)) {
                        tpnew = TradeParameters.buildParameter(tp);
                        HashMap hmnew = new HashMap(hm);
                        hmnew.put(i, j.doubleValue());
                        cls = cls.getClass().newInstance();
                        cls.setParams(hmnew);
                        cls.setBuy(tp.getBuy());
                        tpnew.setClose(cls);
                        double totalProfit = 0;
                        if (!tradeProfits.containsKey(tpnew.toString())) {
                            if (tlProfits.containsKey(tpnew.toString())) {
                                totalProfit = tlProfits.get(tpnew.toString());
                            } else {
                                TradeList tl = tc.getTradeList(tpnew);
                                totalProfit = tradeListProfit(tl, tpnew);
                            }
                        } else {
                            totalProfit = tradeProfits.get(tpnew.toString()).getTProfit();
                        }
                        if (!tlProfits.containsKey(tpnew.toString())) {
                            tlProfits.put(tpnew.toString(), totalProfit);

                        }
                        pArr.add(totalProfit);
                    }
                    Integer[] scr;
                    scr = getscore(pArr, sys, tp.getBuy());
                    ret.setCls(i - 1, scr);
                    if (scr[0] < min && i == 1 || scr[0] < 2 || scr[1] < 2) {
                        return ret;
                    }
                }
            }
        }
        return ret;
    }

    private Integer[] getscore(ArrayList<Double> profit, String system, int buy) {
        if (system.equals("A")) {
            return getScoreA(profit, buy);
        } else if (system.equals("B")) {
            return getScoreB(profit);
        } else if (system.equals("C")) {
            return getScoreC(profit);
        } else {
            return getScoreD(profit);
        }
    }

    private Integer[] getScoreA(ArrayList<Double> profit, int buy) {
        int grade1 = 5, grade2 = 5, grade3 = 0;
        int main = (int) (((profit.size() * 1.0 / 2)) - 0.5);
        //int SHORT_RANGE=GEN_RANGE_DAYS;
        int SHORT_RANGE = (int) (main / 2.0);
        if (SHORT_RANGE < 2 && main > 2) {
            SHORT_RANGE = 2;
        }
        int LONG_RANGE = main;
//        if(profit.size()==9)
//        {
//            SHORT_RANGE=2;
//            LONG_RANGE=2;
//        }
//        else if(profit.size()==11)
//        {
//            SHORT_RANGE=3;
//            LONG_RANGE=2;
//        }


        if (profit.size() % 2 == 1) {
            for (int i = 1; i <= SHORT_RANGE; i++) {
                double prftN = (profit.get(main - i) / profit.get(main)) * 100;
                double prftP = (profit.get(main + i) / profit.get(main)) * 100;
                if (buy == 1) {
                    if (((prftN >= 70 && prftN < 80) || (prftP >= 70 && prftP < 80)) && grade1 > 4) {
                        grade1 = 4;
                    } else if (((prftN >= 60 && prftN < 70) || (prftP >= 60 && prftP < 70)) && grade1 > 3) {
                        grade1 = 3;
                    } else if (((prftN >= 50 && prftN < 60) || (prftP >= 50 && prftP < 60)) && grade1 > 2) {
                        grade1 = 2;
                    } else if ((prftN < 50 || prftP < 50) && grade1 > 1) {
                        grade1 = 1;
                    }
                } else if (buy == 0) {
                    if (((prftN >= 65 && prftN < 75) || (prftP >= 65 && prftP < 75)) && grade1 > 4) {
                        grade1 = 4;
                    } else if (((prftN >= 55 && prftN < 65) || (prftP >= 55 && prftP < 65)) && grade1 > 3) {
                        grade1 = 3;
                    } else if (((prftN >= 45 && prftN < 55) || (prftP >= 45 && prftP < 55)) && grade1 > 2) {
                        grade1 = 2;
                    } else if ((prftN < 45 || prftP < 45) && grade1 > 1) {
                        grade1 = 1;
                    }
                }
            }
            for (int i = 1; i <= LONG_RANGE; i++) {
                double prftN = (profit.get(main - i) / profit.get(main)) * 100;
                double prftP = (profit.get(main + i) / profit.get(main)) * 100;
                if (buy == 1) {
                    if (((prftN >= 50 && prftN < 75) || (prftP >= 50 && prftP < 75)) && grade2 > 4) {
                        grade2 = 4;
                    } else if (((prftN >= 40 && prftN < 50) || (prftP >= 40 && prftP < 50)) && grade2 > 3) {
                        grade2 = 3;
                    } else if (((prftN >= 0 && prftN < 40) || (prftP >= 0 && prftP < 40)) && grade2 > 2) {
                        grade2 = 2;
                    } else if (((prftN >= -30 && prftN < 0) || (prftP >= -30 && prftP < 0)) && grade2 > 1) {
                        grade2 = 1;
                    } else if ((prftN <= -30 || prftP < -30) && grade2 > 1) {
                        grade2 = 0;
                    }
                } else if (buy == 0) {
                    if (((prftN >= 45 && prftN < 70) || (prftP >= 45 && prftP < 70)) && grade2 > 4) {
                        grade2 = 4;
                    } else if (((prftN >= 35 && prftN < 45) || (prftP >= 35 && prftP < 45)) && grade2 > 3) {
                        grade2 = 3;
                    } else if (((prftN >= -5 && prftN < 35) || (prftP >= -5 && prftP < 35)) && grade2 > 2) {
                        grade2 = 2;
                    } else if (((prftN >= -25 && prftN < -5) || (prftP >= -25 && prftP < -5)) && grade2 > 1) {
                        grade2 = 1;
                    } else if ((prftN <= -25 || prftP < -25) && grade2 > 1) {
                        grade2 = 0;
                    }
                }
                if (profit.get(main - i) < profit.get(main - i + 1)) {
                    grade3++;
                }
                if (profit.get(main + i) < profit.get(main + i - 1)) {
                    grade3++;
                }
            }
            //grades.setText(grade1+","+grade2 + ","+grade3);
        }
        Integer[] ret = {grade1, grade2, (int) (grade3 * 100.0 / (profit.size() - 1))};
        return ret;
    }

    private Integer[] getScoreB(ArrayList<Double> profit) {
        int grade1 = 5, grade2 = 5, grade3 = 0;
        @SuppressWarnings("unchecked")
        ArrayList<Double> profitSort = (ArrayList<Double>) profit.clone();
        Collections.sort(profitSort);
        int main = (int) (((profit.size() * 1.0 / 2)) - 0.5);
        double p1 = (profitSort.get(0) / profit.get(main)) * 100;
        double p2 = (profitSort.get(1) / profit.get(main)) * 100;
        if (p1 <= 40) {
            p1 = 0;
        }
        if (p2 <= 40) {
            p2 = 0;
        }
        //int SHORT_RANGE=GEN_RANGE_DAYS;
        //int LONG_RANGE=RANGE_DAYS;
        int SHORT_RANGE = (int) (main / 2.0);
        if (SHORT_RANGE < 2) {
            SHORT_RANGE = 2;
        }
        int LONG_RANGE = main;
//        if(profit.size()==9)
//        {
//            SHORT_RANGE=2;
//            LONG_RANGE=2;
//        }
//        else if(profit.size()==11)
//        {
//            SHORT_RANGE=3;
//            LONG_RANGE=2;
//        }
        if (profit.size() % 2 == 1) {
            for (int i = 1; i <= SHORT_RANGE; i++) {
                double prftN = (profit.get(main - i) / profit.get(main)) * 100;
                double prftP = (profit.get(main + i) / profit.get(main)) * 100;
                if (((prftN >= 70 && prftN < 80 && prftN > p1) || (prftP >= 70 && prftP < 80 && prftP > p1)) && grade1 > 4) {
                    grade1 = 4;
                } else if (((prftN >= 60 && prftN < 70 && prftN > p1) || (prftP >= 60 && prftP < 70 && prftP > p1)) && grade1 > 3) {
                    grade1 = 3;
                } else if (((prftN >= 50 && prftN < 60 && prftN > p1) || (prftP >= 50 && prftP < 60 && prftP > p1)) && grade1 > 2) {
                    grade1 = 2;
                } else if (((prftN < 50 && prftN > p1) || (prftP < 50 && prftP > p1)) && grade1 > 1) {
                    grade1 = 1;
                }
            }
            for (int i = 1; i <= LONG_RANGE; i++) {
                double prftN = (profit.get(main - i) / profit.get(main)) * 100;
                double prftP = (profit.get(main + i) / profit.get(main)) * 100;
                if (((prftN > 50 && prftN < 75 && prftN > p2) || (prftP > 50 && prftP < 75 && prftP > p2)) && grade2 > 4) {
                    grade2 = 4;
                } else if (((prftN >= 40 && prftN < 50 && prftN > p2) || (prftP >= 40 && prftP < 50 && prftP > p2)) && grade2 > 3) {
                    grade2 = 3;
                } else if (((prftN >= 0 && prftN < 40 && prftN > p2) || (prftP >= 0 && prftP < 40 && prftP > p2)) && grade2 > 2) {
                    grade2 = 2;
                } else if (((prftN >= -30 && prftN < 0 && prftN > p2) || (prftP >= -30 && prftP < 0 && prftP > p2)) && grade2 > 1) {
                    grade2 = 1;
                } else if (((prftN < -30 && prftN > p2) || (prftP < -30 && prftP > p2)) && grade2 > 1) {
                    grade2 = 0;
                }
                if (profit.get(main - i) < profit.get(main - i + 1)) {
                    grade3++;
                }
                if (profit.get(main + i) < profit.get(main + i - 1)) {
                    grade3++;
                }
            }
            //grades.setText(grade1+","+grade2 + ","+grade3);
        }
        Integer[] ret = {grade1, grade2, (int) (grade3 * 100.0 / (profit.size() - 1))};
        return ret;
    }

    private Integer[] getScoreC(ArrayList<Double> profit) {
        int grade1 = 5, grade2 = 5, grade3 = 0;
        int main = (int) (((profit.size() * 1.0 / 2)) - 0.5);
        double shortAvg = 0, longAvg = 0;
        int cnt = 0;
        int SHORT_RANGE = (int) (main / 2.0);
        if (SHORT_RANGE < 2) {
            SHORT_RANGE = 2;
        }
        int LONG_RANGE = main;
//        int SHORT_RANGE=GEN_RANGE_DAYS;
//        int LONG_RANGE=RANGE_DAYS;
//        if(profit.size()==9)
//        {
//            SHORT_RANGE=2;
//            LONG_RANGE=2;
//        }
//        else if(profit.size()==11)
//        {
//            SHORT_RANGE=3;
//            LONG_RANGE=2;
//        }
        if (profit.size() % 2 == 1) {
            for (int i = 1; i <= SHORT_RANGE; i++) {
                shortAvg += profit.get(main - i);
                shortAvg += profit.get(main + i);
                cnt += 2;
            }
            shortAvg /= cnt;
            cnt = 0;
            for (int i = 1; i <= LONG_RANGE; i++) {
                longAvg += profit.get(main - i);
                longAvg += profit.get(main + i);
                cnt += 2;
            }
            longAvg /= cnt;
            double prft = (shortAvg / profit.get(main)) * 100;
            if ((prft >= 75 && prft < 85) && grade1 > 4) {
                grade1 = 4;
            } else if ((prft >= 65 && prft < 75) && grade1 > 3) {
                grade1 = 3;
            } else if ((prft >= 55 && prft < 65) && grade1 > 2) {
                grade1 = 2;
            } else if (prft < 55 && grade1 > 1) {
                grade1 = 1;
            }
//            for(int i=1;i<=SHORT_RANGE;i++)
//            {
//                double prftN=(shortAvg/profit.get(main-i))*100;
//                double prftP=(shortAvg/profit.get(main+i))*100;
//                if(((prftN>70 && prftN<80) ||(prftP>70 && prftP<80)) && grade1>4)
//                    grade1=4;
//                else if(((prftN>60 && prftN<70) ||(prftP>60 && prftP<70)) && grade1>3)
//                    grade1=3;
//                else if(((prftN>50 && prftN<60) ||(prftP>50 && prftP<60)) && grade1>2)
//                    grade1=2;
//                else if((prftN<50  ||prftP<50) && grade1>1)
//                    grade1=1;
//            }
            double prftL = (longAvg / profit.get(main)) * 100;
            if ((prftL >= 55 && prftL < 80) && grade2 > 4) {
                grade2 = 4;
            } else if ((prftL >= 45 && prftL < 55) && grade2 > 3) {
                grade2 = 3;
            } else if ((prftL >= 5 && prftL < 45) && grade2 > 2) {
                grade2 = 2;
            } else if ((prftL >= -30 && prftL < 5) && grade2 > 1) {
                grade2 = 1;
            } else if (prftL < -30 && grade2 > 1) {
                grade2 = 0;
            }
            for (int i = 1; i <= LONG_RANGE; i++) {
//                double prftN=(profit.get(main-i)/longAvg)*100;
//                double prftP=(profit.get(main+i)/longAvg)*100;
//                if(((prftN>50 && prftN<75) ||(prftP>50 && prftP<75)) && grade2>4)
//                    grade2=4;
//                else if(((prftN>40 && prftN<55) ||(prftP>40 && prftP<50)) && grade2>3)
//                    grade2=3;
//                else if(((prftN>0 && prftN<40) ||(prftP>0 && prftP<40)) && grade2>2)
//                    grade2=2;
//                else if(((prftN>-30 && prftN<0) ||(prftP>-30 && prftP<0)) && grade2>1)
//                    grade2=1;
//                else if((prftN<-30  ||prftP<-30) && grade2>1)
//                    grade2=0;
                if (profit.get(main - i) < profit.get(main - i + 1)) {
                    grade3++;
                }
                if (profit.get(main + i) < profit.get(main + i - 1)) {
                    grade3++;
                }
            }
            //grades.setText(grade1+","+grade2 + ","+grade3);
        }
        Integer[] ret = {grade1, grade2, (int) (grade3 * 100.0 / (profit.size() - 1))};
        return ret;
    }

    private Integer[] getScoreD(ArrayList<Double> profit) {
        int grade1 = 5, grade2 = 5, grade3 = 0;
        int main = (int) (((profit.size() * 1.0 / 2)) - 0.5);
        double shortAvg = 0, longAvg = 0;
        int cnt = 0;
        int SHORT_RANGE = (int) (main / 2.0);
        if (SHORT_RANGE < 2) {
            SHORT_RANGE = 2;
        }
        int LONG_RANGE = main;
//        int SHORT_RANGE=GEN_RANGE_DAYS;
//        int LONG_RANGE=RANGE_DAYS;
//        if(profit.size()==9)
//        {
//            SHORT_RANGE=2;
//            LONG_RANGE=2;
//        }
//        else if(profit.size()==11)
//        {
//            SHORT_RANGE=3;
//            LONG_RANGE=2;
//        }
        if (profit.size() % 2 == 1) {
            for (int i = 1; i <= SHORT_RANGE; i++) {
                shortAvg += profit.get(main - i);
                shortAvg += profit.get(main + i);
                cnt += 2;
            }
            shortAvg /= cnt;
            cnt = 0;
            for (int i = 1; i <= LONG_RANGE; i++) {
                longAvg += profit.get(main - i);
                longAvg += profit.get(main + i);
                cnt += 2;
            }
            longAvg /= cnt;
            double prft = (shortAvg / profit.get(main)) * 100;
            if ((prft >= 80 && prft < 90) && grade1 > 4) {
                grade1 = 4;
            } else if ((prft >= 70 && prft < 80) && grade1 > 3) {
                grade1 = 3;
            } else if ((prft >= 60 && prft < 70) && grade1 > 2) {
                grade1 = 2;
            } else if (prft < 60 && grade1 > 1) {
                grade1 = 1;
            }

            double prftL = (longAvg / profit.get(main)) * 100;
            if ((prftL >= 60 && prftL < 85) && grade2 > 4) {
                grade2 = 4;
            } else if ((prftL >= 50 && prftL < 60) && grade2 > 3) {
                grade2 = 3;
            } else if ((prftL >= 10 && prftL < 50) && grade2 > 2) {
                grade2 = 2;
            } else if ((prftL >= -20 && prftL < 10) && grade2 > 1) {
                grade2 = 1;
            } else if (prftL < -30 && grade2 > 1) {
                grade2 = 0;
            }
            for (int i = 1; i <= LONG_RANGE; i++) {
                if (profit.get(main - i) < profit.get(main - i + 1)) {
                    grade3++;
                }
                if (profit.get(main + i) < profit.get(main + i - 1)) {
                    grade3++;
                }
            }
        }
        Integer[] ret = {grade1, grade2, (int) (grade3 * 100.0 / (profit.size() - 1))};
        return ret;
    }

    private double tradeListProfit(TradeList tl, TradeParameters tp) throws Exception {
        double profit = 0;
        for (int i = 0; i < tl.getSize(); i++) {
            Trade trd = tc.calcDividendTrade(tl.getTrade(i), tp.getBuy(), tp.getShareCount(), tp.getIndex());
            profit += trd.getProfit();
        }
        return profit;
    }

    /**
     * @param tradeProfits the tradeProfits to set
     */
    public void setTradeProfits(ConcurrentHashMap<String, Summary> tradeProfits) {
        this.tradeProfits = tradeProfits;
    }
}
