package report;

import Share.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.THashMap;
import indicator.AbstractIndicator;
import indicator.IndicatorField;
import indicator.IndicatorList;
import indicator.MaIndicator;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import trade.*;
import trade.close.*;
import trade.filter.*;
import trade.open.Open;

/**
 * Stores Summary of a Single Function
 *
 * @author Admin
 */
@JsonTypeInfo(use = Id.CLASS, property = "_class")
public class Summary implements Serializable {

    
    //id
    private String id;
    //money for each trade
    private transient static double money = 1000000;
    //private transient SimpleDateFormat sdf = new SimpleDateFormat("yy");
    private transient static final DecimalFormat frmt = new DecimalFormat("0.0");
    //Name of Current Summary
    private String name;
    private transient String string;
    //Total profit in pounds
    private double tProfit;
    //Total profit inc 96-00 if it is buy
    private double allprofit;
    //total of negative trades
    private double tLoss;
    //total of positive trade
    private double tGain;
    //total trade count
    private int tCount;
    //positve trade count
    private int gainTrades;
    //negative trade count
    private int lossTrades;
    //Positive Percentage by total profit
    private double gainPct;
    //Highest Positive Trade
    private double gainHighTrd;
    //average positive trade
    private double gainAvgTrd;
    //highest negative trade
    private double lossHighTrd;
    private double lossAvgTrd;
    //highest consecutive positve trades
    private int consecGain = 1;
    //maximum consec profit for positive trades
    private double maxGainProfit = 0;
    //profit on highest consecutive positive trades
    private double consecGainProfit = 0;
    private int consecLoss = 1;
    private double maxLossProfit = 0;
    private double consecLossProfit = 0;
    //longest open trade
    private long maxOpenCount;
    private long minOpenCount = 999;
    private long minOpenCountMidle = 999;
    private long avgOpenCountMidle = 0;
    private int gainTime;
    private int lossTime;
    //sum of all trade days
    private int totalOpenDays;
    private double zero = 0, one = 0, two = 0, three = 0, four = 0, five = 0, six = 0;
    //percentage of open days by total trading days
    private double dayPercent = 0;
    private double multiPercent = 0;
    private double trdDayPercent = 0;
    private ArrayList<String> yr;
    private ArrayList<Integer> TrdCount, yrGainCount, yrLoserCount;
    private transient TDoubleArrayList PProfit, drawDown, drawDownTrades;
    private transient THashMap<Long, ArrayList> totalDrawdown;
    private transient THashMap<Long, Double> totalProfits;
    private transient THashMap<Long, Double> totalProfitsOpen;
    private transient double openUD;
    private transient double closeUD;
    private transient double ftse250;
    private transient double ftse250AWL;
    private transient double ftse250BWL;
    private transient int ftse250AT;
    private transient int ftse250BT;
    private transient TreeMap<Double, ArrayList<Double>> rangesOpen;
    private transient TreeMap<Double, ArrayList<Double>> rangesClose;
    // actual open days without multiple trades
    private int actualOpen = 0;
    private int grpShares = 1;
    private double tradeUnit = 1;
    private String sysType = "";
    private int impScore;
    //share and trading style info
    private String share;
    private String closeShare;
    private AbstractIndicator absInd;
    private Open opn;
    private AbstractIndicator clsAbsInd;
    private Close cls;
    private Filters opnFilter;
    private Filters clsFilter;
    private int buy;
    private int consec;
    private double sStopLoss;
    private double iStopLoss;
    private int topCap;
    private int backPeriod = 225;
    private IndividualClose iClose;
    private transient TradeCalculator tc;
    private transient ShareList sl;
    private SystemGrade sg;
    private String index;
    private transient TradeList tl;
    private transient int goodscore = 0;
    private transient String yearlyPerf;
    private transient double newHighDays;
    private transient int yrGrades;
    //serial version
    private static final long serialVersionUID = 7526472295622776147L;
    //private transient static HashMap<Date,HashMap<Date,Integer>> dateFreq=new HashMap<Date, HashMap<Date, Integer>>(5000, 0.9f);
    private transient int flatGrade;
    private transient int positiveGrade;
    private transient double flatProfit;
    private transient double posYrProfit;
    private transient double posLYrProfit;
    private transient int posYrGrade;
    private transient int posLYrGrade;
    protected transient final int minYear = 2000;

    public Summary() {
    }

    public Summary(String share, AbstractIndicator absInd, Open opn, String closeShare,
            AbstractIndicator clsAbsInd, Close cls, Filters opnFilter, Filters clsFilter, int buy, int lookBack, int consec,
            double shareSL, double indSL, String index, IndividualClose iclose, int topcap) {
        this.share = share;
        this.closeShare = closeShare;
        this.absInd = absInd;
        this.clsAbsInd = clsAbsInd;
        this.opn = opn;
        this.cls = cls;
        this.opnFilter = opnFilter;
        this.clsFilter = clsFilter;
        this.buy = buy;
        this.consec = consec;
        this.sStopLoss = shareSL;
        this.iStopLoss = indSL;
        this.index = index;
        this.iClose = iclose;
        this.topCap = topcap;
        this.backPeriod = lookBack;
    }

    public Summary(String name, double amount) {
        this.name = name;
        this.id = name;
        Summary.money = amount;
    }

    public double calcProfit(double start, double close, int buy) {
        double shareNo = money / start;
        double endMoney = close * shareNo;
        double ret = 0;
        if (buy == 1) {
            ret = endMoney - money;
        } else {
            ret = money - endMoney;
        }

        return (ret / 100) / grpShares;
    }

    public TradeList getTradeProfit(ShareList sl) throws Exception {
        //BasicShareDB bsd=new BasicShareDB();
        //ShareList sl=bsd.getShareData(share);
        IndicatorList il = getAbsInd().buildIndicator(sl);
        IndicatorList ilClose = null;
        if (getTc() == null) {
            setTc(TradeCalculator.getInstance());;
        }
        if (getClsAbsInd() != null) {
            ilClose = getClsAbsInd().buildIndicator(sl);
        }
        TradeList tlist = tc.getTrades(il, ilClose, sl, getOpn(), getOpnFilter(), getCls(), getClsFilter());
        TradeList trdProfits = new TradeList();
        for (int i = 0; i < tlist.getSize(); i++) {
            Trade trd = tlist.getTrade(i);
            //double profit=calcProfit(trd.getStartPrice(),trd.getClosePrice(),buy);
            //Trade newTrade=new Trade(trd.getStartDate(), trd.getStartPrice(), trd.getCloseDate(), trd.getClosePrice());
            //newTrade.setProfit(profit);
            Trade newTrade = tc.calcDividendTrade(trd, buy, grpShares, getIndex());
            trdProfits.addTrade(newTrade);
        }
        //bsd.close();
        return trdProfits;
    }

    public void calculateTrades(TradeList trdList, ShareList sl, int topShares, TradeCalculator tc, boolean drawCalc) throws Exception {
        this.setGrpShares(topShares);
        calculateTrades(trdList, sl, tc, drawCalc);
    }

    public void calculateTrades(TradeList trdList, ShareList sl, int topShares, TradeCalculator tc, boolean drawCalc, double maxD) throws Exception {
        this.setGrpShares(topShares);
        calculateTrades(trdList, sl, tc, drawCalc, maxD);
    }

    public void calculateTrades(TradeList trdList, ShareList sl, TradeCalculator tc, boolean drawCalc) throws Exception {
        calculateTrades(trdList, sl, tc, drawCalc, 999);
    }

    public void calculateTrades(TradeList trdList, ShareList sl, TradeCalculator tc, boolean drawCalc, double maxDraw) throws Exception {
        if (sl == null) {
            //BasicShareDB bsd = new BasicShareDB();
            //sl = bsd.getShareData(getShare());
            //bsd.close();
            sl = tc.getSL(share,index);
        }
        this.setTc(tc);
        this.setSl(sl);
        if (share == null || share.isEmpty()) {
            share = sl.getShare();
        }
        //this.tl=trdList;
        String bb = "Sell";
        if (buy == 1) {
            bb = "Buy";
        }
        if (name == null || name.isEmpty()) {
            setName(getShare());
            if (getCloseShare() != null) {
                setName(getName() + " Open " + getCloseShare());
            }
            setName(getName() + " " + getAbsInd().toString() + getOpn().toString());
            if (getOpnFilter() != null) {
                setName(getName() + getOpnFilter().toString());
            }
            if (getClsAbsInd() != null) {
                setName(getName() + getClsAbsInd().toString() + " Cls ");
            }
            setName(getName() + getCls().toString());
            if (getClsFilter() != null) {
                setName(getName() + getClsFilter().toString());
            }
            if (grpShares > 1) {
                setName(getName() + " " + grpShares + " Shares ");
            }
            setName(getName() + bb);
        }
        if (tc == null) {
            tc = TradeCalculator.getInstance();
        }
        TDoubleArrayList arrProfit = new TDoubleArrayList(sl.getSize());
        setTotalProfits(new THashMap<Long, Double>());
        setTotalProfitsOpen(new THashMap<Long, Double>());
        Double profit = 0.0;
        int currGStreak = 0, currLStreak = 0;
        THashMap<Date, THashMap<Date, Integer>> dateFreq = new THashMap<Date, THashMap<Date, Integer>>(sl.getSize());
        //dateFreq.clear();
        double startProfit = 0, cLossP = 0, cGainP = 0;
        setYr(new ArrayList<String>(20));
        setPProfit(new TDoubleArrayList(20));
        setTrdCount(new ArrayList<Integer>(20));
        setYrGainCount(new ArrayList<Integer>(20));
        setYrLoserCount(new ArrayList<Integer>(20));
        setDrawDown(new TDoubleArrayList(20));
        setDrawDownTrades(new TDoubleArrayList(20));
        double yrprofit = 0;
        int yrcount = 0;
        double grpProfit = 0;
        int dif = 0;
        ArrayList<Integer> arrTdiff = new ArrayList<Integer>();
        HashMap<String, TradeList> yrTrades = new LinkedHashMap<String, TradeList>(20);
        int size = trdList.getSize();
//        if(sdf==null)
//            sdf=new SimpleDateFormat("yy");
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < size; i++) {
            Trade trd = trdList.getTrade(i);
            if (trd.getProfit() == 0 && index != null) {
                trd = tc.calcDividendTrade(trd, buy, grpShares, index);
                profit = trd.getProfit();//calcProfit(trd.getStartPrice(),trd.getClosePrice(),buy);
            } else if (index == null) {
                profit = calcProfit(trd.getStartPrice(), trd.getClosePrice(), buy);
            } else {
                profit = trd.getProfit();//calcProfit(trd.getStartPrice(),trd.getClosePrice(),buy);
            }

            Trade Old = trd;
            if (i > 0) {
                Old = trdList.getTrade(i - 1);
            }
            cal.setTime(trd.getStartDate());
            int nStart = cal.get(Calendar.YEAR);
            setAllprofit(getAllprofit() + profit);
            if (buy == 1 && nStart < minYear) {
                continue;
            }
            cal.setTime(Old.getStartDate());
            int oStart = cal.get(Calendar.YEAR);
            if (i > 0 && nStart > oStart && ((buy == 1 && oStart >= minYear) || buy == 0)) {
                String yrd = oStart + "";//sdf.format(Old.getStartDate());
                yrd = yrd.substring(2);
                if (!yr.contains(yrd)) {
                    getYr().add(yrd);
                } else {
                    System.out.println(yrd + "," + Old);
                }
                PProfit.add(yrprofit / 100);
                TrdCount.add(yrcount / grpShares);
                int lstrd = getLossTrades();
                int gntrd = getGainTrades();
                int siz = getYrLoserCount().size();
                if (siz > 0) {
                    lstrd = getLossTrades() - getYrLoserCount().get(siz - 1);
                    gntrd = getGainTrades() - getYrGainCount().get(siz - 1);
                }
                getYrLoserCount().add(lstrd);
                getYrGainCount().add(gntrd);
                yrprofit = 0;
                yrcount = 0;
                /*if(drawCalc)
                 {
                 ArrayList<Double> downs=getDrawDown(yrTrades,buy, grpShares,sl,tc);
                 drawDown.add(downs.get(0));
                 drawDownTrades.add(downs.get(1));
                 if(downs.get(0)<maxDraw*-1 || downs.get(1)<maxDraw*-1)
                 break;
                 }
                 else
                 {
                 drawDown.add(0.0);
                 drawDownTrades.add(0.0);
                 }
                 yrTrades=new TradeList();*/

            }
            yrprofit += profit;
            yrcount++;

            //yr trades
            cal.setTime(trd.getCloseDate());
            int dyr = cal.get(Calendar.YEAR);
            String yrd = (dyr + "").substring(2);//sdf.format(trd.getCloseDate());
            cal.setTime(trd.getStartDate());
            int dyrS = cal.get(Calendar.YEAR);
            String yrdS = (dyrS + "").substring(2);//sdf.format(trd.getStartDate());
            if (!yrTrades.containsKey(yrdS)) {
                yrTrades.put(yrdS, new TradeList());
            }
            TradeList yrs = yrTrades.get(yrd);
            if (yrs == null) {
                yrs = new TradeList();
            }
            yrs.addTrade(trd);
            yrTrades.put(yrd, yrs);

            if ((i > 0 && i % grpShares == 0) || i == trdList.getSize() - 1 || (i == 0 && grpShares == 1)) {
                arrProfit.add(grpProfit);
                if (grpProfit < 0) {
                    settLoss(gettLoss() + grpProfit);
                    setLossTrades(getLossTrades() + 1);
                    if (grpProfit < getLossHighTrd()) {
                        setLossHighTrd(grpProfit);
                    }
                    setLossTime(getLossTime() + dif);
                    if (arrProfit.size() > 1 && arrProfit.get(arrProfit.size() - 2) < 0) {
                        currLStreak++;
                        if (currLStreak >= getConsecLoss()) {
                            setConsecLoss(currLStreak);
                            if (startProfit > 0)//&& consecLossProfit<((startProfit-(tProfit+profit))/startProfit)*100)
                            {
                                setConsecLossProfit(((startProfit - (tProfit + grpProfit)) / (startProfit + 10000)) * 100);
                            } else if (startProfit == 0) {
                                setConsecLossProfit(((startProfit - (tProfit + grpProfit)) / 10000) * 100);
                            }
                        }
                    } else {
                        if (startProfit > 0) {
                            cGainP = ((tProfit - startProfit) / (startProfit + 10000)) * 100;
                        } else {
                            cGainP = 0;
                        }

                        //System.out.println(trd.getStartDate() + "\t" + startProfit + "\t" + tProfit + "\t" + maxGainProfit + "\t" + cGainP);
                        if (cGainP > getMaxGainProfit()) {
                            setMaxGainProfit(cGainP);
                        }
                        startProfit = tProfit;
                        currLStreak = 1;

                    }
                } else if (grpProfit > 0) {
                    settGain(gettGain() + grpProfit);
                    setGainTrades(getGainTrades() + 1);
                    if (grpProfit > getGainHighTrd()) {
                        setGainHighTrd(grpProfit);
                    }
                    setGainTime(getGainTime() + dif);
                    if (arrProfit.size() > 1 && arrProfit.get(arrProfit.size() - 2) > 0) {
                        currGStreak++;
                        if (currGStreak >= getConsecGain()) {
                            setConsecGain(currGStreak);
                            if (startProfit > 0)// && consecGainProfit<(((tProfit+profit)-startProfit)/startProfit)*100)
                            {
                                setConsecGainProfit((((tProfit + grpProfit) - startProfit) / (startProfit + 10000)) * 100);
                            } else if (startProfit == 0) {
                                setConsecGainProfit((((tProfit + grpProfit) - startProfit) / 10000) * 100);
                            }
                        }
                    } else {
                        if (startProfit > 0) {
                            cLossP = ((startProfit - tProfit) / (startProfit + 10000)) * 100;
                        } else {
                            cLossP = 0;
                        }
                        //System.out.println(trd.getStartDate() + "\t" + startProfit + "\t" + tProfit + "\t" + maxLossProfit + "\t" + cLossP);
                        if (cLossP > getMaxLossProfit()) {
                            setMaxLossProfit(cLossP);
                        }
                        startProfit = tProfit;
                        currGStreak = 1;
                    }
                }
                grpProfit = 0;
            }
            grpProfit += profit;
            settCount(gettCount() + 1);
            settProfit(tProfit + profit);
            Double prft = profit;
            cal.setTime(trd.getCloseDate());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            if (getTotalProfits().containsKey(cal.getTimeInMillis())) {
                prft += getTotalProfits().get(cal.getTimeInMillis());
            }
            getTotalProfits().put(cal.getTimeInMillis(), prft);
            cal.setTime(trd.getStartDate());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Double prftO = profit;
            if (getTotalProfitsOpen().containsKey(cal.getTimeInMillis())) {
                prftO += getTotalProfitsOpen().get(cal.getTimeInMillis());
            }
            getTotalProfitsOpen().put(cal.getTimeInMillis(), prftO);
            int endIndex = sl.isDatePresent(trd.getCloseDate());
            if (endIndex == -1) {
                endIndex = sl.isLowerDatePresent(trd.getCloseDate());
            }
            int stIndex = sl.isDatePresent(trd.getStartDate());
            if (stIndex == -1) {
                stIndex = sl.isLowerDatePresent(trd.getStartDate());
            }
            for (int j = stIndex; j < endIndex; j++) {
                THashMap<Date, Integer> tmap = null;//new HashMap<Date, Integer>(4);
                Date dd = sl.getSharedata(j).getDate();
                int c = 1;
                tmap = dateFreq.get(dd);
                if (tmap == null) {
                    tmap = new THashMap<Date, Integer>(4);
                }
                if (tmap.containsKey(trd.getStartDate())) {
                    c = tmap.get(trd.getStartDate());
                    c++;
                }
                tmap.put(trd.getStartDate(), c);
                dateFreq.put(dd, tmap);
            }

            dif = endIndex - stIndex;
            arrTdiff.add(dif);
            setTotalOpenDays(getTotalOpenDays() + dif);
            if (arrTdiff.size() == grpShares && i < trdList.getSize()) {
                Collections.sort(arrTdiff);
                Double midle = (0.7 * grpShares) - 1;
                int hDif = arrTdiff.get(grpShares - 1);
                int lDif = arrTdiff.get(midle.intValue());
                if (hDif > getMaxOpenCount()) {
                    setMaxOpenCount(hDif);
                }
                if (hDif < getMinOpenCount()) {
                    setMinOpenCount(hDif);
                }
                if (lDif < getMinOpenCountMidle()) {
                    setMinOpenCountMidle(lDif);
                }
                setAvgOpenCountMidle(getAvgOpenCountMidle() + lDif);
                arrTdiff = new ArrayList<Integer>();
            }
        }
        if (trdList.getSize() > 0) {
            cal.setTime(trdList.getTrade(trdList.getSize() - 1).getStartDate());
            int dyrS = cal.get(Calendar.YEAR);
            String yrd = (dyrS + "").substring(2);//sdf.format(trdList.getTrade(trdList.getSize() - 1).getStartDate());
            if (!yr.contains(yrd)) {
                getYr().add(yrd);
            }
        }
        if (drawCalc) {
            setTotalDrawdown(getTotalDrawDown(trdList, getBuy(), getGrpShares(), sl, tc));
        } else {
            setTotalDrawdown(new THashMap<Long, ArrayList>());
        }
        PProfit.add(yrprofit / 100);
        TrdCount.add(yrcount / grpShares);
        int lstrd = getLossTrades();
        int gntrd = getGainTrades();
        int siz = getYrLoserCount().size();
        if (siz > 0) {
            lstrd = getLossTrades() - getYrLoserCount().get(siz - 1);
            gntrd = getGainTrades() - getYrGainCount().get(siz - 1);
        }
        getYrLoserCount().add(lstrd);
        getYrGainCount().add(gntrd);

        if (trdList.getSize() > 0) {

            for (String yrs : yrTrades.keySet()) {
                //String str=sdf.format(trdList.getTrade(trdList.getSize()-1).getCloseDate());
                if (!yr.contains(yrs)) {
                    yr.add(yrs);

                    Collections.sort(getYr(), new Comparator<String>() {

                        @Override
                        public int compare(String o1, String o2) {
                            int i = 0;
                            try {
                                //i = sdf.parse(o1).compareTo(sdf.parse(o2));
                                Integer i1 = Integer.valueOf(o1);
                                Integer i2 = Integer.valueOf(o2);
                                i = i1.compareTo(i2);
                            } catch (Exception ex) {
                                Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            return i;
                        }
                    });
                    int ind = getYr().indexOf(yrs);
                    PProfit.insert(ind,0.0);
                    TrdCount.add(ind, 0);
                    getYrLoserCount().add(ind, 0);
                    getYrGainCount().add(ind, 0);
                }
                if (drawCalc) {
                    TradeList yrTrds = yrTrades.get(yrs);
                    ArrayList<Double> downs = getDrawDown(yrTrds, buy, grpShares, sl, tc);
                    getDrawDown().add(downs.get(0));
                    getDrawDownTrades().add(downs.get(1));
                } else {
                    getDrawDown().add(0.0);
                    getDrawDownTrades().add(0.0);
                }

            }
        }
        int zzero = 0, oone = 0, ttwo = 0, tthree = 0, ffour = 0, ffive = 0, ssix = 0;
        setActualOpen(dateFreq.size());
        //Iterator itr=dateFreq.values().iterator();
//        System.out.print(Collections.frequency(dateFreq.values(), 1));
        //while(itr.hasNext())
        int multipleOpen = 0, countOpen = 0;
        for (Date d : dateFreq.keySet()) {
            THashMap<Date, Integer> hm = dateFreq.get(d);
            int c = hm.size();
            multipleOpen += c;
            for (Date dd : hm.keySet()) {
                countOpen += dateFreq.get(d).get(dd);
            }
            switch (c) {
                case 1:
                    oone++;
                    break;
                case 2:
                    ttwo++;
                    break;
                case 3:
                    tthree++;
                    break;
                case 4:
                    ffour++;
                    break;
                case 5:
                    ffive++;
                    break;
                default:
                    ssix++;
            }
            hm.clear();
        }

        settCount(gettCount() / grpShares);
        if (gettCount() > 0) {
            setAvgOpenCountMidle(getAvgOpenCountMidle() / gettCount());
        }
        zzero = sl.getSize() - dateFreq.size();
        this.setZero(((zzero * 1.0) / sl.getSize()) * 100);
        this.setOne(((oone * 1.0) / sl.getSize()) * 100);
        this.setTwo(((ttwo * 1.0) / sl.getSize()) * 100);
        this.setThree(((tthree * 1.0) / sl.getSize()) * 100);
        this.setFour(((ffour * 1.0) / sl.getSize()) * 100);
        this.setFive(((ffive * 1.0) / sl.getSize()) * 100);
        this.setSix(((ssix * 1.0) / sl.getSize()) * 100);
        //System.out.println((oone+(ttwo*2)+(tthree*3)+(ffour*4)+(ffive*5)+(ssix*6))/actualOpen);
        setTradeUnit(((oone + (ttwo * 2) + (tthree * 3) + (ffour * 4) + (ffive * 5) + (ssix * 6))) * 1.0 / getActualOpen());
        if (tProfit > 0) {
            setGainPct((gettGain() / tProfit) * 100);
        }
        if (getGainTrades() > 0) {
            setGainAvgTrd(gettGain() / getGainTrades());
        }
        if (getLossTrades() > 0) {
            setLossAvgTrd(gettLoss() / getLossTrades());
        }
        setDayPercent(((getActualOpen() * 1.0) / sl.getSize()) * 100);
        setMultiPercent(((multipleOpen * 1.0) / sl.getSize()) * 100);
        setTrdDayPercent(((countOpen * 1.0) / (sl.getSize() * 10.0)) * 100);
        //System.out.println(((multipleOpen*1.0)/sl.getSize())*100);
        //System.out.println(((countOpen*1.0)/(sl.getSize()*10.0))*100);
        dateFreq.clear();
        this.setTc(null);
        //if (opnFilter instanceof MaFilter) {
        //MaFilter maf = (MaFilter) opnFilter;
        //maf.releaseSL();
        //}
    }

    /**
     * Method getTProfit
     *
     *
     * @return
     *
     */
    public double getTProfit() {
        return (this.tProfit);
    }

    /**
     * Method getTLoss
     *
     *
     * @return
     *
     */
    public double getTLoss() {
        return (this.gettLoss());
    }

    /**
     * Method getTGain
     *
     *
     * @return
     *
     */
    public double getTGain() {
        return (this.gettGain());
    }

    /**
     * Method getTCount
     *
     *
     * @return
     *
     */
    public int getTCount() {
        return (this.gettCount());
    }

    /**
     * Method getGainTrades
     *
     *
     * @return
     *
     */
    public int getGainTrades() {
        return (this.gainTrades);
    }

    /**
     * Method getGainPct
     *
     *
     * @return
     *
     */
    public double getGainPct() {
        return (this.gainPct);
    }

    /**
     * Method getGainHighTrd
     *
     *
     * @return
     *
     */
    public double getGainHighTrd() {
        return (this.gainHighTrd);
    }

    /**
     * Method getGainAvgTrd
     *
     *
     * @return
     *
     */
    public double getGainAvgTrd() {
        return (this.gainAvgTrd);
    }

    /**
     * Method getLossHighTrd
     *
     *
     * @return
     *
     */
    public double getLossHighTrd() {
        return (this.lossHighTrd);
    }

    /**
     * Method getLossAvgTrd
     *
     *
     * @return
     *
     */
    public double getLossAvgTrd() {
        return (this.lossAvgTrd);
    }

    /**
     * Method getConsecGain
     *
     *
     * @return
     *
     */
    public int getConsecGain() {
        return (this.consecGain);
    }

    /**
     * Method getConsecLoss
     *
     *
     * @return
     *
     */
    public int getConsecLoss() {
        return (this.consecLoss);
    }

    /**
     * Method getMaxOpenCount
     *
     *
     * @return
     *
     */
    public long getMaxOpenCount() {
        return (this.maxOpenCount);
    }

    /**
     * Method getTotalOpenDays
     *
     *
     * @return
     *
     */
    public int getTotalOpenDays() {
        return (this.totalOpenDays);
    }

    /**
     * Method getActualOpenDays
     *
     *
     * @return
     *
     */
    public int getActualOpenDays() {
        return (this.getActualOpen());
    }

    /**
     * Method getLossTradeCount
     *
     *
     * @return
     *
     */
    public int getLossTradeCount() {
        return (this.getLossTrades());
    }

    /**
     * Method getConsecGainProfit
     *
     *
     * @return
     *
     */
    public double getConsecGainProfit() {
        return (this.consecGainProfit);
    }

    /**
     * Method getConsecLossProfit
     *
     *
     * @return
     *
     */
    public double getConsecLossProfit() {
        return (this.consecLossProfit);
    }

    /**
     * Method getMaxGainProfit
     *
     *
     * @return
     *
     */
    public double getMaxGainProfit() {
        return (this.maxGainProfit);
    }

    /**
     * Method getMaxLossProfit
     *
     *
     * @return
     *
     */
    public double getMaxLossProfit() {
        return (this.maxLossProfit);
    }

    /**
     * Method getLossTradeCount
     *
     *
     * @return
     *
     */
    public String getFreq() {
        //DecimalFormat frmt=new DecimalFormat("0.0");
        String sep = ", ";
        String ret = frmt.format(getZero()) + sep + frmt.format(getOne()) + sep
                + frmt.format(getTwo()) + sep + frmt.format(getThree()) + sep
                + frmt.format(getFour()) + sep + frmt.format(getFive()) + sep + frmt.format(getSix());
        return (ret);
    }

    /**
     * Method getDayPercentage
     *
     *
     * @return
     *
     */
    public double getDayPercentage() {
        return getDayPercent();
    }

    /**
     * Method getYearlyPerf
     *
     *
     * @return
     *
     */
    public String getYearlyPerf() {

        if (yearlyPerf == null || yearlyPerf.equals("")) {
            StringBuilder yrPerf = new StringBuilder();
            //DecimalFormat frmt=new DecimalFormat("0.0");
            for (int y = 0; y < yr.size(); y++) {
                yrPerf.append("*").append(getYr().get(y)).append("(").append(frmt.format(PProfit.get(y))).append(
                        ",").append(TrdCount.get(y)).append(",").append((y < getDrawDown().size() ? frmt.format(getDrawDown().get(y)) : 0)).append(",").append((y < getDrawDown().size() ? frmt.format(getDrawDownTrades().get(y)) : 0)).append(") ");
            }
            yearlyPerf = yrPerf.toString();
        } /*else {
         yrPerf.append(yearlyPerf);
         }*/

        return yearlyPerf;//yrPerf.toString();
    }

    /**
     * Method getYearlyGrade
     *
     *
     * @return
     *
     */
    public int getYearlyGrade() {
        int counts = getYrGrades(), mising = 0;
        if (counts == 0) {
            double min = 0;
            double prft = 0;
            ArrayList<Integer> yrly = new ArrayList<Integer>();
            ArrayList<Double> yrlyPft = new ArrayList<Double>();
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            TreeSet<Long> s = new TreeSet<>(getTotalProfits().keySet());
            for (Long d : s) {
                c1.setTimeInMillis(d);
                if (s.lower(d) != null) {
                    c2.setTimeInMillis(s.lower(d));
                }
                if (s.lower(d) != null && c1.get(Calendar.YEAR) > c2.get(Calendar.YEAR)) {
                    yrly.add(c2.get(Calendar.YEAR));
                    yrlyPft.add(prft);
                    if (prft >= min) {
                        counts++;
                    } else if (buy == 1 && yrly.size() > 1) {

                        int y1 = yrly.get(yrly.size() - 1);
                        int y2 = yrly.get(yrly.size() - 2);
                        //if( y1-y2>1)
                        {
                            mising = y1 - y2;
                        }
                        if (prft <= min && (yrlyPft.get(yrly.size() - 2) <= min || mising > 1)) {
                            if (yrly.size() > 2 && (yrlyPft.get(yrly.size() - 3) <= min) && mising == 1) {
                                counts--;
                            } else {
                                counts -= 2;
                            }
                        }
                    }
                    prft = 0;
                }
                prft += getTotalProfits().get(d);
                if (s.higher(d) == null) {
                    int yr = c1.get(Calendar.YEAR);
                    if (!yrly.contains(yr)) {
                        yrly.add(c1.get(Calendar.YEAR));
                        yrlyPft.add(prft);
                    }
                    if (prft >= min) {
                        counts++;
                    } else if (buy == 1 && yrly.size() > 1) {

                        int y1 = yrly.get(yrly.size() - 1);
                        int y2 = yrly.get(yrly.size() - 2);
                        //if( y1-y2>1)
                        {
                            mising = y1 - y2;
                        }
                        if (prft <= min && (yrlyPft.get(yrly.size() - 2) <= min || mising > 1)) {
                            if (yrly.size() > 2 && (yrlyPft.get(yrly.size() - 3) <= min) && mising == 1) {
                                counts--;
                            } else {
                                counts -= 2;
                            }
                        }
                    }
                    prft = 0;
                }
            }
            setYrGrades(counts);
        }
        return counts;
    }

    /**
     * Method getYearlyGrade
     *
     *
     * @return
     *
     */
    public double getYearlyGradePerc() {
        double ret = getPosYrProfit();
//        try {
        // Date dfrom = sdf.parse("97");
//        if (sdf == null) {
//            sdf = new SimpleDateFormat("yy");
//        }
        if (ret == 0) {
            if (buy == 1) {
                Calendar c = new GregorianCalendar(2001, 0, 1);
                Date dtill = c.getTime();
//            Collection<Double> d= totalProfits.subMap(dfrom, dtill).values();
//            for(Double val:d)
//            {
//                profit += val;
//            }
                TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
                for (Long d : td) {

                    if (d > dtill.getTime()) {
                        break;
                    }
                    ret += getTotalProfits().get(d);

                }
//        } catch (ParseException ex) {
//            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
//        }
                ret = (ret / tProfit) * 100;
            } else if (buy == 0) {
                Calendar c = Calendar.getInstance();
                TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
                for (Long d : td) {
                    Double prft = getTotalProfits().get(d);
                    c.setTimeInMillis(d);
                    if (c.get(Calendar.YEAR) == 2001 || c.get(Calendar.YEAR) == 2002 || c.get(Calendar.YEAR) == 2008 || c.get(Calendar.YEAR) == 2011) {
                        ret += prft;
                    }
                }
                ret /= 100;
            }
            setPosYrProfit(ret);
        }
        return ret;
    }

    /**
     * Method getYearlyGrade sell 04-06
     *
     * @return
     *
     */
    public double getLimitYearlyGradePerc() {
        double ret = 0;
//        try {
        // Date dfrom = sdf.parse("97");
//        if (sdf == null) {
//            sdf = new SimpleDateFormat("yy");
//        }
        if (ret == 0) {
            if (buy == 1) {
                Calendar c = new GregorianCalendar(2001, 0, 1);
                //Date dtill = c.getTime();
                TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
                for (Long d : td) {
                    //if (/*s.equals("09") ||*/s.equals("01") || s.equals("02") || s.equals("08") || s.equals("11"))
                    c.setTimeInMillis(d);
                    if (c.get(Calendar.YEAR) == 2001 || c.get(Calendar.YEAR) == 2002 || c.get(Calendar.YEAR) == 2008 || c.get(Calendar.YEAR) == 2011) {
                        ret += getTotalProfits().get(d);
                    }
                }
                ret /= 100;
            } else if (buy == 0) {
                Calendar c = new GregorianCalendar(2003, 11, 31);
                Date start = c.getTime();
                c.set(2007, 0, 1);
                Date end = c.getTime();
                TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
                for (Long d : td) {
                    Double prft = getTotalProfits().get(d);
                    if (d > start.getTime() && d < end.getTime()) {
                        ret += prft;
                    }
                }
                ret /= 100;
            }
            setPosLYrProfit(ret);
        }
        return ret;
    }

    /**
     * Method getPosGradePerc
     *
     * 03-07,10
     *
     * @return
     *
     */
    public int getPosGradePerc() {
        int ret = getPosYrGrade();
        if (ret == 0) {
            if (buy == 1) {
            } else if (buy == 0) {
                Calendar c = new GregorianCalendar(2002, 11, 31);
                HashMap<Integer, Double> yrly = new LinkedHashMap<Integer, Double>();
                TreeSet<Long> td = new TreeSet<>(totalProfits.keySet());
                for (Long d : td) {
                    Double prft = getTotalProfits().get(d);
                    c.setTimeInMillis(d);
                    int yrc = c.get(Calendar.YEAR);
                    Double p = yrly.get(yrc);
                    if (p == null) {
                        p = 0.0;
                    }
                    yrly.put(yrc, p + prft);
                }
                for (int y : yrly.keySet()) {
                    double d = yrly.get(y);
                    if (y == 2001 || y == 2002 || y == 2008 || y == 2011) {
                        if (d / 100 >= 15) {
                            ret++;
                        }
                    }
                }
            }
            setPosYrGrade(ret);
        }
        return ret;
    }

    /**
     * Method getPosGradePerc 04,05,06
     *
     * @return
     *
     */
    public int getLimitGradePerc() {
        int ret = getPosLYrGrade();
        if (ret == 0) {
            if (buy == 1) {
            } else if (buy == 0) {
                Calendar c = new GregorianCalendar(2002, 11, 31);
                HashMap<Integer, Double> yrly = new LinkedHashMap<Integer, Double>();
                TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
                for (Long d : td) {
                    Double prft = getTotalProfits().get(d);
                    c.setTimeInMillis(d);
                    int yrc = c.get(Calendar.YEAR);
                    Double p = yrly.get(yrc);
                    if (p == null) {
                        p = 0.0;
                    }
                    yrly.put(yrc, p + prft);
                }
                for (int y : yrly.keySet()) {
                    double d = yrly.get(y);
                    if (y >= 2004 && y < 2007) {
                        if (d > 0) {
                            ret++;
                        }
                    }
                }
            }
            setPosLYrGrade(ret);
        }
        return ret;
    }

    /**
     * Method getFlatGrade
     *
     *
     * @return
     *
     */
    public int getFlatGrade() {
        int count = flatGrade;
        if (count == 0) {
//            if (sdf == null) {
//                sdf = new SimpleDateFormat("yy");
//            }
            Calendar c = Calendar.getInstance();
            HashMap<String, Double> arr = new HashMap<String, Double>();
            TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
            for (Long d : td) {
                Double prft = getTotalProfits().get(d);
                c.setTimeInMillis(d);
                String form = (c.get(Calendar.YEAR) + "").substring(2);//sdf.format(d);
                if (arr.containsKey(form)) {
                    prft += arr.get(form);
                }
                arr.put(form, prft);
            }
            if (buy == 1) {
                for (String s : arr.keySet()) {
                    if (/*s.equals("09") ||*/s.equals("01") || s.equals("02") || s.equals("08") || s.equals("11")) {
                        Double prf = arr.get(s);
                        if (prf > 0) {
                            count++;
                        }
                    }
                }
            } else if (buy == 0) {
                double min = 20;
                for (String s : arr.keySet()) {
                    if (/*s.equals("09") || */s.equals("01") || s.equals("02") || s.equals("08") || s.equals("11")) {
                        Double prf = arr.get(s);
                        if (prf < min) {
                            min = prf;
                        }
                        if (prf / 100 >= 0) {
                            count++;
                        }
                    }
                }
                if (min / 100 <= -10) {
                    count--;
                }
            }
        }
        return count;
    }

    /**
     * Method getPositiveGrade
     *
     *
     * @return
     *
     */
    public int getPositiveGrade() {
        int count = positiveGrade;
        if (count == 0) {
//            if (sdf == null) {
//                sdf = new SimpleDateFormat("yy");
//            }
            Calendar c = Calendar.getInstance();
            if (buy == 1) {
//            for (String s : arr.keySet()) {
//                if (!s.equals("09") && !s.equals("01") && !s.equals("02") && !s.equals("08")) {
//                    Double prf = arr.get(s);
//                    if (prf > 0) {
//                        count++;
//                    }
//                }
//            }
                count = getYearlyGrade();
            } else if (buy == 0) {
                HashMap<String, Double> arr = new HashMap<String, Double>();
                TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
                for (Long d : td) {
                    Double prft = getTotalProfits().get(d);
                    c.setTimeInMillis(d);
                    String form = (c.get(Calendar.YEAR) + "").substring(2);//sdf.format(d);
                    if (arr.containsKey(form)) {
                        prft += arr.get(form);
                    }
                    arr.put(form, prft);
                }
                for (String s : arr.keySet()) {
                    if (/*!s.equals("09") &&*/!s.equals("01") && !s.equals("02") && !s.equals("08") && !s.equals("11")) {
                        Double prf = arr.get(s);
                        if (prf > 0) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * Method getFlatProfit
     *
     *
     * @return
     *
     */
    public double getFlatProfit() {
        double profit = flatProfit;
        Calendar c1 = new GregorianCalendar(2000, 8, 30);
        Calendar c2 = new GregorianCalendar(2001, 4, 1);
        Calendar c = Calendar.getInstance();
        if (profit == 0.0) {
            TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
            for (Long d : td) {
                c.setTimeInMillis(d);
                String form = (c.get(Calendar.YEAR) + "").substring(2);//sdf.format(d);
                if (buy == 1) {

                    /*if (form.equals("01")) {
                     profit += totalProfits.get(d);
                     }

                     if (form.equals("02")) {
                     profit += totalProfits.get(d);
                     }
                     if (form.equals("03")) {
                     break;
                     }*/
                    if (d > c1.getTimeInMillis() && d < c2.getTimeInMillis()) {
                        profit += getTotalProfits().get(d);
                    } else if (d > c2.getTimeInMillis()) {
                        break;
                    }
                } else if (buy == 0) {
                    if (/*!sdf.format(d).equals("00") &&*/!form.equals("01") && !form.equals("02") && !form.equals("08") && !form.equals("11")) {
                        profit += getTotalProfits().get(d);
                    }
                }
            }

        }
        return profit;
    }

    /**
     * Method getYearlyGainLoss
     *
     *
     * @return
     *
     */
    public String getYearlyGainLoss() {
        StringBuilder yrPerf = new StringBuilder();
        yrPerf.append(frmt.format((getGainTrades() * 1.0) / gettCount())).append("(").append(getGainTrades()).append(",").append(getLossTrades()).append("), ");
        for (int y = 0; y < getYr().size(); y++) {
            double d = (getYrGainCount().get(y) * 1.0) / (getYrGainCount().get(y) + getYrLoserCount().get(y));
            yrPerf.append(getYr().get(y)).append("(").append(frmt.format(d)).append(",").append(getYrGainCount().get(y)).append(",").append(getYrLoserCount().get(y)).append(") ");
        }
        return yrPerf.toString();
    }

    /**
     * Method toString
     *
     *
     * @return
     *
     */
    @Override
    public String toString() {
        if (getString() == null) {
            String sep = System.getProperty("line.separator");

            StringBuilder buffer = new StringBuilder();
            buffer.append(sep);
            buffer.append("Total Profit  \t");
            buffer.append(tProfit);
            buffer.append(sep);
            buffer.append("Total Loss  \t");
            buffer.append(gettLoss());
            buffer.append(sep);
            buffer.append("Total Gain \t");
            buffer.append(gettGain());
            buffer.append(sep);
            buffer.append("Trade Count \t");
            buffer.append(gettCount());
            buffer.append(sep);
            buffer.append("Profit Per Trade \t");
            buffer.append(tProfit / gettCount());
            buffer.append(sep);
            buffer.append("Profit Per Trade Per Day \t");
            buffer.append(tProfit / getTotalOpenDays());
            buffer.append(sep);
            buffer.append("Gain Trade Count \t");
            buffer.append(getGainTrades());
            buffer.append(sep);
            buffer.append("Loss Trade Count \t");
            buffer.append(getLossTrades());
            buffer.append(sep);
            buffer.append("Gain Percentage \t");
            buffer.append(getGainPct());
            buffer.append(sep);
            buffer.append("Gain Highest Trade \t");
            buffer.append(getGainHighTrd());
            buffer.append(sep);
            buffer.append("Gain Avg Trade \t");
            buffer.append(getGainAvgTrd());
            buffer.append(sep);
            buffer.append("Loss High Trade \t");
            buffer.append(getLossHighTrd());
            buffer.append(sep);
            buffer.append("Loss Avg Trade \t");
            buffer.append(getLossAvgTrd());
            buffer.append(sep);
            buffer.append("Max Conecutive Gainers \t");
            buffer.append(getConsecGain());
            buffer.append(sep);
            buffer.append("Max Conecutive loseres \t");
            buffer.append(getConsecLoss());
            buffer.append(sep);
            buffer.append("Max Open Days \t");
            buffer.append(getMaxOpenCount());
            buffer.append(sep);
            buffer.append("Total Open Days \t");
            buffer.append(getTotalOpenDays());
            buffer.append(sep);

            setString(buffer.toString());
        }
        return getString();
    }

    public void setName(String name) {
        this.name = name;
        //setId(name);
    }

    public String getName() {
        return (this.name);
    }

    public String getShare() {
        return this.share;
    }

    public String getCloseShare() {
        return this.closeShare;
    }

    public AbstractIndicator getIndicator() {
        return this.getAbsInd();
    }

    public Open getOpen() {
        return this.getOpn();
    }

    public AbstractIndicator getCloseIndicator() {
        return this.getClsAbsInd();
    }

    public Close getClose() {
        return this.getCls();
    }

    public Filters getOpenFilter() {
        return this.getOpnFilter();
    }

    public Filters getCloseFilter() {
        return this.getClsFilter();
    }

    public int getBuy() {
        return this.buy;
    }

    /**
     * @return the PProfit
     */
    public TDoubleArrayList getPProfit() {
        return PProfit;
    }

    public double getAvgTradeLength() {
        Double d = totalOpenDays / (tCount * grpShares * 1.0);
        return Math.round(d);
    }

    public double getPPTPD() {
        return (tProfit * grpShares * 1.0) / getTotalOpenDays();
    }

    public double getGainHighPercent() {
        return getGainHighTrd() * 100.0 / tProfit;
    }

    public double getLossHighPercent() {
        return getLossHighTrd() * 100.0 / tProfit;
    }

    public double getPPTExec() {
        return (tProfit - getGainHighTrd()) * 1.0 / (gettCount() - 1);
    }

    /**
     * @return the minOpenCount
     */
    public long getMinOpenCount() {
        return minOpenCount;
    }

    /**
     * @return the gainTime
     */
    public int getGainTime() {
        return gainTime;
    }

    /**
     * @return the lossTime
     */
    public int getLossTime() {
        return lossTime;
    }

    /**
     * @return the gainTime
     */
    public double getGainTimePercent() {
        return (getGainTime() * 100.0) / (getTotalOpenDays() / grpShares);
    }

    /**
     * @return the lossTime
     */
    public double getLossTimePercent() {
        return (getLossTime() * 100.0) / (getTotalOpenDays() / grpShares);
    }

    public ArrayList<Double> getDrawDown(TradeList yrTrades, int buy, int grpShares, ShareList index, TradeCalculator tc) throws Exception {
        double profit = 0.0;
        double min = 0.0;
        double midprofit = 0.0;
        TradeList tl = new TradeList(grpShares);
        TreeMap<Date, Double> dateprofit = new TreeMap<Date, Double>();
        TreeMap<Date, Double> dateprofitYr = new TreeMap<Date, Double>();
        TreeMap<Date, Double> profits = new TreeMap<Date, Double>();
        Date lastDate = null;
//        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        int size = yrTrades.getSize();
        for (int i = 0; i < size; i++) {
            Trade trd = yrTrades.getTrade(i);
            if (lastDate == null) {
                lastDate = trd.getCloseDate();
            }
            if ((i > 0 && i % grpShares == 0))//||  i==yrTrades.getSize()-1)
            {
//                if(i==yrTrades.getSize()-1)
//                {
//                    tl.addTrade(trd);
//                    profit+=calcProfit(trd.getStartPrice(), trd.getClosePrice(), buy);
//                }
                TreeMap<Date, Double> dateprofitTmp = tc.getIntraDayProfit(tl, buy, grpShares, index);
                for (Date d : dateprofitTmp.keySet()) {
                    Date dd = profits.lowerKey(d);
                    if (dd == null) {
                        midprofit = 0;
                    } else {
                        midprofit = profits.get(dd);
                    }
                    Double prft = dateprofitTmp.get(d);
                    Double prftyr = dateprofitTmp.get(d);
//                    if(yearly)
//                        System.out.println(d+ "," + prft + "," + dateprofit.get(d));
//                    SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
//                    if(d.after(sdf.parse("1/1/2006")) && d.before(sdf.parse("1/1/2007")))
//                        System.out.println(d+ "," + prft + "," + dateprofit.get(d));
                    if (dateprofit.containsKey(d)) {
                        prft += dateprofit.get(d);
                        prftyr += dateprofitYr.get(d);
                    } else {
                        prftyr += midprofit;
                    }
                    dateprofit.put(d, prft);
                    dateprofitYr.put(d, prftyr);
                }
//                if(yearly)
//                    System.out.println();
                tl = new TradeList(grpShares);
                //midprofit=profit;
                profits.put(lastDate, profit);
            }
            tl.addTrade(trd);
            if (i == yrTrades.getSize() - 1)//i==0 && grpShares==1)
            {
                TreeMap<Date, Double> dateprofitTmp = tc.getIntraDayProfit(tl, buy, grpShares, index);
                for (Date d : dateprofitTmp.keySet()) {
                    Date dd = profits.lowerKey(d);
                    if (dd == null) {
                        midprofit = 0;
                    } else {
                        midprofit = profits.get(dd);
                    }
                    Double prft = dateprofitTmp.get(d);
                    Double prftyr = dateprofitTmp.get(d);
                    if (dateprofit.containsKey(d)) {
                        prft += dateprofit.get(d);
                        prftyr += dateprofitYr.get(d);
                    } else {
                        prftyr += midprofit;
                    }
                    dateprofit.put(d, prft);
                    dateprofitYr.put(d, prftyr);
                }
                tl = new TradeList();
                profits.put(lastDate, profit);
            }
            if (lastDate == null) {
                lastDate = trd.getCloseDate();
            } else if (lastDate.before(trd.getCloseDate())) {
                lastDate = trd.getCloseDate();
            }
            profit += trd.getProfit();//calcProfit(trd.getStartPrice(), trd.getClosePrice(), buy);
        }
        Double minyr = 0.0;
        for (Date d : dateprofit.keySet()) {
            Double prft = dateprofit.get(d);
            Double prftyr = dateprofitYr.get(d);
//            if(yearly)
//            if(d.after(sdf.parse("1/1/2010")) && d.before(sdf.parse("1/1/2011")))
//                System.out.println(d+ "," + prftyr/100);

            if (prft < min) {
                min = prft;

//                if(d.after(sdf.parse("1/1/2006")) && d.before(sdf.parse("1/1/2007")))
//                    System.out.println(d+ "," + prft/100);
            }
            if (prftyr < minyr) {
                minyr = prftyr;
            }
        }
        ArrayList<Double> ret = new ArrayList<Double>(2);
        ret.add(min / 100.0);
        ret.add(minyr / 100.0);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public THashMap<Long, ArrayList> getTotalDrawDown(TradeList yrTrades, int buy, int grpShares, ShareList index, TradeCalculator tc) throws Exception {
        double profit = 0.0;
        double midprofit = 0.0;
        TradeList tl = new TradeList();
        TreeMap<Date, Double> dateprofit = new TreeMap<Date, Double>();
        //TreeMap<Date,Double> dateprofitYr=new TreeMap<Date, Double>();
        TreeMap<Date, Double> profits = new TreeMap<Date, Double>();
        Date lastDate = null;
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < yrTrades.getSize(); i++) {
            Trade trd = yrTrades.getTrade(i);
            c.setTime(trd.getStartDate());
            if (c.get(Calendar.YEAR) < minYear) {
                continue;
            }
            if (lastDate == null) {
                lastDate = trd.getCloseDate();
            }
            if ((i > 0 && i % grpShares == 0)) {
                TreeMap<Date, Double> dateprofitTmp = tc.getIntraDayProfit(tl, buy, grpShares, index);
                for (Date d : dateprofitTmp.keySet()) {
                    Date dd = profits.lowerKey(d);
                    if (dd == null) {
                        midprofit = 0.0;
                    } else {
                        midprofit = profits.get(dd);
                    }
                    Double prft = dateprofitTmp.get(d);
                    //double prftyr=dateprofitTmp.get(d);
                    if (dateprofit.containsKey(d)) {
                        prft += dateprofit.get(d);
                        //prftyr+=dateprofitYr.get(d);
                    } else {
                        prft += midprofit;
                    }
                    dateprofit.put(d, prft);
                    //dateprofitYr.put(d, prftyr);
                }
                tl = new TradeList();
                profits.put(lastDate, profit);
            }
            tl.addTrade(trd);
            if (i == yrTrades.getSize() - 1) {
                TreeMap<Date, Double> dateprofitTmp = tc.getIntraDayProfit(tl, buy, grpShares, index);
                for (Date d : dateprofitTmp.keySet()) {
                    Date dd = profits.lowerKey(d);
                    if (dd == null) {
                        midprofit = 0.0;
                    } else {
                        midprofit = profits.get(dd);
                    }
                    Double prft = dateprofitTmp.get(d);
                    //double prftyr=dateprofitTmp.get(d);
                    if (dateprofit.containsKey(d)) {
                        prft += dateprofit.get(d);
                        //prftyr+=dateprofitYr.get(d);
                    } else {
                        prft += midprofit;
                    }
                    dateprofit.put(d, prft);
                    //dateprofitYr.put(d, prftyr);
                }
                tl = new TradeList();
                profits.put(lastDate, profit);
                dateprofitTmp.clear();
            }
            if (lastDate == null) {
                lastDate = trd.getCloseDate();
            } else if (lastDate.before(trd.getCloseDate())) {
                lastDate = trd.getCloseDate();
            }
            profit += trd.getProfit();
        }
        profits.clear();
        THashMap<Long, ArrayList> ret = new THashMap<Long, ArrayList>();
        Double high = 0.0, low = 0.0;
        Date lowDate = new Date();
        Date highDate = new Date();
        for (Date d : dateprofit.keySet()) {
            Double prft = dateprofit.get(d);
            //System.out.println(d+","+prft);
            if (prft > high) {
                //get lowest
                if (low != high) {
                    ArrayList li = new ArrayList();
                    Double dif = (high - low) / 100;
                    if (dif > 3.0) {
                        li.add(dif * -1);
                        int dDff = index.isDatePresent(lowDate) - index.isDatePresent(highDate);
                        li.add(dDff);
                        li.add(highDate);
                        ret.put(lowDate.getTime(), li);
                    }
                    //System.out.println(lowDate +","+ (high-low) + "," + dDff);
                }
                high = prft;
                highDate = d;
                low = high;

            }
            if (prft < low) {
                low = prft;
                lowDate = d;
            }
        }
        dateprofit.clear();

        //ret.add(min/100);
        //ret.add(minyr/100);
        return ret;
    }

    /**
     * @return the drawDown
     */
    public TDoubleArrayList getDrawDown() {
        return drawDown!=null?drawDown:new TDoubleArrayList();
    }

    /**
     * @return the minOpenCountMidle
     */
    public long getMinOpenCountMidle() {
        return minOpenCountMidle;
    }

    /**
     * @return the drawDownTrades
     */
    public TDoubleArrayList getDrawDownTrades() {
        return drawDownTrades!=null?drawDownTrades:new TDoubleArrayList();
    }

    /**
     * @return the avgOpenCountMidle
     */
    public long getAvgOpenCountMidle() {
        return avgOpenCountMidle;
    }

    /**
     * @return the tradeUnit
     */
    public double getTradeUnit() {
        return tradeUnit;
    }

    /**
     * @return the grpShares
     */
    public int getGrpShares() {
        return grpShares;
    }

    /**
     * @return the totalDrawdown
     */
    public THashMap<Long, ArrayList> getTotalDrawdown() {
        return totalDrawdown!=null?totalDrawdown:new THashMap<Long, ArrayList>();
    }

    /**
     * @return the totalProfits
     */
    public THashMap<Long, Double> getTotalProfits() {
        return totalProfits!=null?totalProfits:new THashMap<Long, Double>();
    }

    /**
     * @return the TradeParams
     */
    public TradeParameters getTradeParams() {
        boolean ema = false;
        if (getBackPeriod() == 0) {
            setBackPeriod(225);
        }

        if (getAbsInd() instanceof MaIndicator) {
            ema = true;
        }
        //if (sl == null)
        {
            try {
                //BasicShareDB bsd=new BasicShareDB();
                if (getShare().contains(" ")) {
                    //String shares[]=share.split(" ");
                    //int days=Integer.parseInt(shares[3]);
                    //MA ma = new MA(days, MA.Simple);
                    //sl= bsd.getShareData(shares[0]+ " "+shares[1] , shares[2],ma,false);
                    setSl(new ShareList(0, getShare()));
                } else {
                    //BasicShareDB bsd = new BasicShareDB();
                    //setSl(bsd.getShareData(getShare()));
                    //bsd.close();
                    setSl(tc.getSL(share,index));
                }
                //sl=bsd.getShareData(share);
                //bsd.close();
            } catch (Exception ex) {
                Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ShareList slCls = getSl();
        if (getCloseShare() != null /*&& share.length() <= closeShare.length() && !share.equals(closeShare.substring(0, share.length()))*/) {
            try {
                if (!(share.equals(closeShare) || (share.length() <= closeShare.length() && share.equals(closeShare.substring(0, share.length()))))) {
                    if (getCloseShare().contains(" ")) {
                        //String shares[]=share.split(" ");
                        //int days=Integer.parseInt(shares[3]);
                        //MA ma = new MA(days, MA.Simple);
                        //sl= bsd.getShareData(shares[0]+ " "+shares[1] , shares[2],ma,false);
                        slCls = new ShareList(0, getCloseShare());
                    } else {
                        //BasicShareDB bsd = new BasicShareDB();
                        //setSl(bsd.getShareData(getShare()));
                        //bsd.close();
                        setSl(tc.getSL(share,index));
                    }
                }
                /*BasicShareDB bsd = new BasicShareDB();
                 slCls = bsd.getShareData(closeShare);
                 bsd.close();*/
            } catch (Exception ex) {
                Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return TradeParameters.buildParameter(buy == 1, getAbsInd(), getClsAbsInd(), getSl(), slCls, getOpn(), getOpnFilter(), getCls(), getConsec(), getsStopLoss(), getiStopLoss(), grpShares > 1, getIndex(), grpShares, getBackPeriod(), ema, getiClose(), getTopCap());
    }

    /**
     * @return the consec
     */
    public int getConsec() {
        return consec;
    }

    /**
     * @return the shareSL
     */
    public double getSStopLoss() {
        return getsStopLoss();
    }

    /**
     * @return the indSL
     */
    public double getIStopLoss() {
        return getiStopLoss();
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * @return the multiPercent
     */
    public double getMultiPercent() {
        return multiPercent;
    }

    /**
     * @return the trdDayPercent
     */
    public double getTrdDayPercent() {
        return trdDayPercent;
    }

    /**
     * @return the iClose
     */
    public IndividualClose getiClose() {
        return iClose;
    }

    /**
     * @return the sg
     */
    public SystemGrade getSg() {
        return sg;
    }

    /**
     * @param sg the sg to set
     */
    public void setSg(SystemGrade sg) {
        this.sg = sg;
    }

    /**
     * @return the sysType
     */
    public String getSysType() {
        return sysType;
    }

    /**
     * @param sysType the sysType to set
     */
    public void setSysType(String sysType) {
        this.sysType = sysType;
    }

    /**
     * @return the impScore
     */
    public int getImpScore() {
        return impScore;
    }

    /**
     * @param impScore the impScore to set
     */
    public void setImpScore(int impScore) {
        this.impScore = impScore;
    }

    /**
     * @return the Good Score of System
     */
    public int getGoodScore() {
        int score = getGoodscore();
        if (score == 0) {
            //profit
            {
                double val = tProfit / 100.0;
                if ((buy == 1 && val >= 300) || (buy == 0 && val >= 200)) {
                    score++;
                }
            }
            //win/loss
            {
                double val = getGainTrades() * 1.0 / getLossTrades();
                if (val >= 3) {
                    score++;
                }
            }
            //Day %
            {
                double val = getDayPercent();
                if ((buy == 1 && val <= 43) || (buy == 0 && val <= 15)) {
                    score++;
                }
            }
            //Trade Units
            {
                double val = getTradeUnit();
                if (val <= 1.35) {
                    score++;
                }
            }
            //drawdown
            {
                if (getDrawDown().size() > 0 && getDrawDownTrades().size() > 0) {

                    double val = Math.min(Collections.min(Arrays.asList(ArrayUtils.toObject(getDrawDown().toArray()))), Collections.min(Arrays.asList(ArrayUtils.toObject(getDrawDownTrades().toArray()))));
                    if (val >= -20) {
                        score++;
                    }
                }
            }
            //Graph Grade
            {
                int val = getYearlyGrade();
                if ((buy == 1 && val >= 12) || (buy == 0 && val >= 9)) {
                    score++;
                }
            }
            //Graph Trend
            {
                double val = getYearlyGradePerc();
                if ((buy == 1 && val <= 43)) {// || (buy == 0 && val >= 5 && val <= 30)) {
                    score++;
                }

            } //Grade
            {
                if (getSg() != null) {
                    if ((buy == 1 && getSg().isGoodStrict(4)) || buy == 0 && getSg().isGoodStrict(3)) {
                        score++;
                    }
                }
            } //01-02 profit
            {
                double val = getFlatProfit() / 100;
                if ((buy == 1 && val >= 10) || (buy == 0 && val >= 50)) {
                    score++;
                }

            } //-ve grade
            {
                int val = getFlatGrade();
                if ((buy == 1 && val >= 2) || (buy == 0 && val >= 3)) {
                    score++;
                }
            }//+ve grade
            {
                int val = getPositiveGrade();
                if ((buy == 1 && val >= 12) || (buy == 0 && val >= 6)) {
                    score++;
                }
            }
            {
                double val = getNewHighDays();
                if (val < 3.5) {
                    score++;
                }
            }
            setGoodscore(score);
        } /*else {
         score = goodscore;
         }*/

        return score;
    }

    public double getNewHighDays() {
        double max = newHighDays;
        if (max == 0) {
            TreeMap<Long, Double> eq = new TreeMap<Long, Double>();
            TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
            for (Long d : td) {
                double old = 0.0;
                if (eq.containsKey(d)) {
                    old = eq.get(d);
                } else if (eq.lowerKey(d) != null) {
                    old = eq.lowerEntry(d).getValue();
                }
                eq.put(d, getTotalProfits().get(d) + old);
            }
            double high = 0;
            if (eq.isEmpty()) {
                return max * 1.0 / 365.0;
            }
            Long old = eq.firstKey();
            for (Long d : eq.keySet()) {
                if (eq.get(d) > high) {
                    high = eq.get(d);
                    int dif = (int) ((d - old) / (1000 * 60 * 60 * 24));
                    if (dif > max) {
                        max = dif;
                    }
                    old = d;
                }
            }
            if (old < eq.lastKey()) {
                int dif = (int) ((eq.lastKey() - old) / (1000 * 60 * 60 * 24));
                if (dif > max) {
                    max = dif;
                }
            }
        }
        return max * 1.0 / 365.0;
    }

    public int getEquitySmooth() {
        int ret = 0;
        Calendar c = Calendar.getInstance();
        TreeSet<Long> s = new TreeSet<>(getTotalProfits().keySet());
        int months = getMonthsDifference(s.first(), s.last()) / 3;
        double avg = tProfit / months;
        c.setTimeInMillis(s.first());
        c.add(Calendar.MONTH, 3);
        Date next = c.getTime();
        double mProfit = 0;
        for (Long d : getTotalProfits().keySet()) {
            if (d > next.getTime()) {
                if (Math.abs((mProfit - avg) * 100 / avg) < 20) {
                    ret++;
                }
                c.add(Calendar.MONTH, 3);
                next = c.getTime();
                mProfit = 0;
            }
            mProfit += getTotalProfits().get(d);
        }
        if (mProfit != 0) {
            if (Math.abs((mProfit - avg) * 100 / avg) < 20) {
                ret++;
            }
        }
        return ret;
    }

    public final int getMonthsDifference(Long date1, Long date2) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date1);
        int y1 = c.get(Calendar.YEAR);
        int m1 = c.get(Calendar.MONTH);
        c.setTimeInMillis(date2);
        int y2 = c.get(Calendar.YEAR);
        int m2 = c.get(Calendar.MONTH);
        int mo1 = y1 * 12 + m1;
        int mo2 = y2 * 12 + m2;
        return mo2 - mo1 + 1;
    }

    public double[] getHighLowPerc() {
        double[] ret = {0, 0};
        if (getTl() == null) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            TreeMap<Date, String> hls = tc.getUkxHighLow();
//            ShareList ukx = tc.getSL(index);
//            Calendar c=Calendar.getInstance();
//            c.set(1997, 1, 1);
//            Date starter=c.getTime();
            TreeMap<Date, Integer> tlDates = getTradingDays(getTl());
            ArrayList<TreeSet<Date>> ud = getUpDowns(hls);
            int ups = 0;
            int downs = 0;
            for (Date d : tlDates.keySet()) {
                int cnts = tlDates.get(d);
                if (ud.get(0).contains(d)) {
                    ups += cnts;
                } else if (ud.get(1).contains(d)) {
                    downs += cnts;
                }
            }
            ret[0] = ups * 100.0 / (ud.get(0).size() * getTradeUnit());
            ret[1] = downs * 100.0 / (ud.get(1).size() * getTradeUnit());
            //System.out.println(ups+","+downs+","+ud.get(0).size()+","+ud.get(1).size());
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public double[] getHighLowPoints() {
        double[] ret = {0, 0};
        if (getTl() == null) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            TreeMap<Date, String> hls = tc.getUkxHighLow();
            ShareList ukx = tc.getSL(getIndex(),index);
            Calendar c = Calendar.getInstance();
            c.set(1997, 1, 1);
            Date starter = c.getTime();
            double totalftse = 0, totaltrade = 0;
            double posSum = 0, posFtse = 0;
            double negSum = 0, negFtse = 0;
            TreeMap<Date, Date> datesDone = new TreeMap<Date, Date>();
            for (Date d : hls.keySet()) {
                if (hls.higherKey(d) == null || hls.higherKey(d).before(starter)) {
                    continue;
                }
                Date high = hls.higherKey(d);
                double open = ukx.getSharedata(ukx.isDatePresent(d)).getClosePrice();
                double close = ukx.getSharedata(ukx.isDatePresent(high)).getClosePrice();
                double perc = (close - open) * 100 / open;
                totalftse += perc;
                if (perc <= 0) {
                    negFtse += perc;
                } else if (perc > 0) {
                    posFtse += perc;
                }
            }
            for (int i = 0; i < getTl().getSize(); i++) {
                Trade trd = getTl().getTrade(i);
                Date close = datesDone.get(trd.getStartDate());
                if (close == null || trd.getCloseDate().after(close)) {
                    datesDone.put(trd.getStartDate(), trd.getCloseDate());
                    int start = ukx.isDatePresent(trd.getStartDate());
                    int end = ukx.isDatePresent(trd.getCloseDate());
                    double openV = ukx.getSharedata(start).getClosePrice();
                    double closeV = ukx.getSharedata(end).getClosePrice();
                    double perc = (closeV - openV) * 100 / openV;
                    Date next = hls.higherKey(trd.getStartDate());
                    if (next == null) {
                        continue;
                    }
                    if (close != null) {
                        //System.out.print(perc+","+(totaltrade-perc));
                        end = ukx.isDatePresent(close);
                        openV = ukx.getSharedata(start).getClosePrice();
                        closeV = ukx.getSharedata(end).getClosePrice();
                        perc = (closeV - openV) * 100 / openV;
                        totaltrade -= perc;
                        if (hls.get(next).equals("H")) {
                            if (next.before(close)) {
                                end = ukx.isDatePresent(next);
                                closeV = ukx.getSharedata(end).getClosePrice();
                                perc = (closeV - openV) * 100 / openV;
                                posSum -= perc;

                                start = ukx.isDatePresent(next);
                                end = ukx.isDatePresent(close);
                                openV = ukx.getSharedata(start).getClosePrice();
                                closeV = ukx.getSharedata(end).getClosePrice();
                                perc = (closeV - openV) * 100 / openV;
                                negSum -= perc;
                            } else {
                                posSum -= perc;
                            }
                        } else if (hls.get(next).equals("L")) {
                            if (next.before(close)) {

                                end = ukx.isDatePresent(next);
                                closeV = ukx.getSharedata(end).getClosePrice();
                                perc = (closeV - openV) * 100 / openV;
                                negSum -= perc;

                                start = ukx.isDatePresent(next);
                                end = ukx.isDatePresent(close);
                                openV = ukx.getSharedata(start).getClosePrice();
                                closeV = ukx.getSharedata(end).getClosePrice();
                                perc = (closeV - openV) * 100 / openV;
                                posSum -= perc;

                            } else {
                                negSum -= perc;
                            }
                        }
                        start = ukx.isDatePresent(trd.getStartDate());
                        end = ukx.isDatePresent(trd.getCloseDate());
                        openV = ukx.getSharedata(start).getClosePrice();
                        closeV = ukx.getSharedata(end).getClosePrice();
                        perc = (closeV - openV) * 100 / openV;
                    }
                    totaltrade += perc;

                    if (hls.get(next).equals("H")) {
                        if (next.before(trd.getCloseDate())) {
                            end = ukx.isDatePresent(next);
                            closeV = ukx.getSharedata(end).getClosePrice();
                            perc = (closeV - openV) * 100 / openV;
                            posSum += perc;

                            start = ukx.isDatePresent(next);
                            end = ukx.isDatePresent(trd.getCloseDate());
                            openV = ukx.getSharedata(start).getClosePrice();
                            closeV = ukx.getSharedata(end).getClosePrice();
                            perc = (closeV - openV) * 100 / openV;
                            negSum += perc;
                        } else {
                            posSum += perc;
                        }
                    } else if (hls.get(next).equals("L")) {
                        if (next.before(trd.getCloseDate())) {

                            end = ukx.isDatePresent(next);
                            closeV = ukx.getSharedata(end).getClosePrice();
                            perc = (closeV - openV) * 100 / openV;
                            negSum += perc;

                            start = ukx.isDatePresent(next);
                            end = ukx.isDatePresent(trd.getCloseDate());
                            openV = ukx.getSharedata(start).getClosePrice();
                            closeV = ukx.getSharedata(end).getClosePrice();
                            perc = (closeV - openV) * 100 / openV;
                            posSum += perc;

                        } else {
                            negSum += perc;
                        }
                    }
                }
            }
            ret[0] = (posSum / getTradeUnit()) * 100 / posFtse;
            ret[1] = (negSum / getTradeUnit()) * 100 / negFtse;
            //System.out.println(posSum+","+negSum);
            //System.out.println(ups+","+downs+","+ud.get(0).size()+","+ud.get(1).size());
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public double getOpenUDPhase() {
        double ret = getOpenUD();
        if (getTotalProfitsOpen() == null || ret != 0) {
            return ret;
        }

        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            TreeMap<Date, String> hls = tc.getUkxHighLow();
            ShareList ukx = tc.getSL(getIndex(),index);

            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                try {
                    int dds = ukx.isDatePresent(d);
                    double perc = 0;
                    double mult = 0;
                    Date dd = new Date(d);
                    if (hls.containsKey(dd)) {
                        //System.out.println(d+","+hls.get(d));
                        mult = 0;
                        if (hls.get(dd).equals("H")) {
                            mult = 1;
                        }
                        //System.out.println(d+","+mult);
                        //ret += mult;
                    } else {
                        Date min = hls.lowerKey(dd);
                        int mind = ukx.isDatePresent(min);
                        Date max = hls.higherKey(dd);
                        if (max == null) {
                            continue;
                        }
                        int maxd = ukx.isDatePresent(max);
                        int diff = maxd - mind;
                        if (diff == 0) {
                            diff = 1;
                        }
                        perc = (dds - mind) * 100 / diff;
                        mult = getQuat(perc);
                        if (hls.get(min).equals("H")) {
                            if (mult == 1) {
                                mult *= -1;
                            } else {
                                mult = (1 - mult) * -1;
                            }
                        }
                        //System.out.println(d+","+mult);

                    }
                    ret += mult;

                    ArrayList<Double> profit = getRangesOpen().get(mult);
                    if (profit == null) {
                        profit = new ArrayList<Double>();
                    }
                    profit.add(getTotalProfitsOpen().get(d));
                    getRangesOpen().put(mult, profit);

                } catch (Exception ex) {
                    Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
//        for(Double d:ranges.keySet())
//        {
//            double val=0;
//            for(Double dd:ranges.get(d))
//                val+=dd;
//            System.out.println(d+","+ranges.get(d).size()+","+val);
//        }
        setOpenUD(ret);
        return ret;
    }

    public double getOpenAwayPhase() {
        double ret = 0;
        if (getTotalProfitsOpen() == null) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            };
            TreeMap<Date, String> hls = tc.getUkxHighLow();
            ShareList ukx = tc.getSL(getIndex(),index);
            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                try {
                    int dds = ukx.isDatePresent(d);
                    double perc = 0;
                    Date dd = new Date(d);
                    if (hls.containsKey(dd)) {
                        //System.out.println(d+","+hls.get(d));
                        double mult = 0;
                        if (hls.get(dd).equals("H")) {
                            mult = 1;
                        }
                        //System.out.println(d+","+mult);
                        ret += mult;
                    } else {
                        Date min = hls.lowerKey(dd);
                        int mind = ukx.isDatePresent(min);
                        Date max = hls.higherKey(dd);
                        if (max == null) {
                            continue;
                        }
                        int maxd = ukx.isDatePresent(max);
                        int diff = maxd - mind;
                        if (diff == 0) {
                            diff = 1;
                        }
                        perc = (dds - mind) * 100 / diff;
                        double mult = getQuat(perc);
                        if (hls.get(min).equals("H")) {
                            if (mult == 1) {
                                mult *= -1;
                            } else {
                                mult = (1 - mult) * -1;
                            }
                        } else {
                            mult *= -1;
                        }
                        //System.out.println(d+","+mult+","+(dds-mind)+","+diff);
                        ret += mult;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public double getCloseAwayPhase() {
        double ret = 0;
        if (getTotalProfitsOpen() == null) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            };
            TreeMap<Date, String> hls = tc.getUkxHighLow();
            ShareList ukx = tc.getSL(getIndex(),index);
            for (Long d : getTotalProfits().keySet()) {
                try {
                    int dds = ukx.isDatePresent(d);
                    double perc = 0;
                    Date dd = new Date(d);
                    if (hls.containsKey(dd)) {
                        //System.out.println(d+","+hls.get(d));
                        double mult = 0;
                        if (hls.get(dd).equals("H")) {
                            mult = 1;
                        }
                        //System.out.println(d+","+mult);
                        ret += mult;
                    } else {

                        Date min = hls.lowerKey(dd);
                        int mind = ukx.isDatePresent(min);
                        Date max = hls.higherKey(dd);
                        if (max == null) {
                            continue;
                        }
                        int maxd = ukx.isDatePresent(max);
                        int diff = maxd - mind;
                        if (diff == 0) {
                            diff = 1;
                        }
                        perc = (dds - mind) * 100 / diff;
                        double mult = getQuat(perc);
                        if (hls.get(min).equals("H")) {
                            if (mult == 1) {
                                mult *= -1;
                            } else {
                                mult = (1 - mult) * -1;
                            }
                        } else {
                            mult *= -1;
                        }
                        //System.out.println(d+","+mult+","+(dds-mind)+","+diff);
                        ret += mult;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public double getCloseUDPhase() {
        double ret = getCloseUD();
        if (ret != 0) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            setRangesClose(new TreeMap<Double, ArrayList<Double>>());
            TreeMap<Date, String> hls = tc.getUkxHighLow();
            ShareList ukx = tc.getSL(getIndex(),index);
            TreeSet<Long> td = new TreeSet<>(getTotalProfits().keySet());
            for (Long d : td) {
                try {
                    int dds = ukx.isDatePresent(d);
                    double perc = 0;
                    double mult = 0;
                    Date dd = new Date(d);
                    if (hls.containsKey(dd)) {
                        //System.out.println(d+","+hls.get(d));
                        mult = 0;
                        if (hls.get(dd).equals("H")) {
                            mult = 1;
                        }
                        //System.out.println(d+","+mult);
                        //ret += mult;
                    } else {
                        Date min = hls.lowerKey(dd);
                        int mind = ukx.isDatePresent(min);
                        Date max = hls.higherKey(dd);
                        if (max == null) {
                            continue;
                        }
                        int maxd = ukx.isDatePresent(max);
                        int diff = maxd - mind;
                        if (diff == 0) {
                            diff = 1;
                        }
                        perc = (dds - mind) * 100 / diff;
                        mult = getQuat(perc);
                        if (hls.get(min).equals("H")) {
                            if (mult == 1) {
                                mult *= -1;
                            } else {
                                mult = (1 - mult) * -1;
                            }
                        }
                        //System.out.println(d+","+mult);
                    }
                    ret += mult;

                    ArrayList<Double> profit = getRangesClose().get(mult);
                    if (profit == null) {
                        profit = new ArrayList<Double>();
                    }
                    profit.add(getTotalProfits().get(d));
                    getRangesClose().put(mult, profit);

                } catch (Exception ex) {
                    Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        setCloseUD(ret);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public double getEMAFilterProfit() {
        double ret = getFtse250();
        if (getTotalProfitsOpen() == null || ret != 0) {
            return ret;
        }
        try {
            if (tc == null) {
                tc = TradeCalculator.getInstance();
            }
            String sh = getIndex() + " 250d EMA";

            IndicatorList ukema = tc.getIndicatorList(sh);
            if (ukema == null) {
                MaIndicator ema = new MaIndicator();
                HashMap param = new HashMap();
                param.put(1, 250);
                ema.init(param, ma.MA.Exponential);
                ShareList ukx = tc.getSL(getIndex(),index);
                ukema = ema.buildIndicator(ukx);
                tc.putIndicatorList(sh, ukema);
            }
            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                double profit = getTotalProfitsOpen().get(d);
                IndicatorField inf = ukema.getSharedata(ukema.isDatePresent(d));
                if (buy == 1) {
                    if (inf.getValue() < inf.getSignal()) {
                        ret += profit;
                    }
                } else if (inf.getValue() > inf.getSignal()) {
                    ret += profit;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        setFtse250((ret) * 100 / tProfit);
        return getFtse250();
    }

    @SuppressWarnings("unchecked")
    public double getEMAFilterAboveWL() {
        double ret = getFtse250AWL();
        int wins = 0;
        int loss = 0;
        if (getTotalProfitsOpen() == null || ret != 0) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            String sh = getIndex() + " 250d EMA";
            IndicatorList ukema = tc.getIndicatorList(sh);
            if (ukema == null) {
                MaIndicator ema = new MaIndicator();
                HashMap param = new HashMap();
                param.put(1, 250);
                ema.init(param, ma.MA.Exponential);
                ShareList ukx = tc.getSL(getIndex(),index);
                ukema = ema.buildIndicator(ukx);
                tc.putIndicatorList(sh, ukema);
            }
            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                double profit = getTotalProfitsOpen().get(d);
                IndicatorField inf = ukema.getSharedata(ukema.isDatePresent(d));
                //if (buy == 1) {
                if (inf.getValue() > inf.getSignal()) {
                    if (profit > 0) {
                        wins++;
                    } else {
                        loss++;
                    }
                }
                /*} else if (inf.getValue() < inf.getSignal()) {
                 if (profit > 0) {
                 wins++;
                 } else {
                 loss++;
                 }
                 }*/
            }

        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (loss > 0) {
            setFtse250AWL(wins * 1.0 / loss);
        } else {
            setFtse250AWL(wins);
        }
        return getFtse250AWL();
    }

    @SuppressWarnings("unchecked")
    public int getEMAFilterAboveTrds() {
        int ret = getFtse250AT();
        if (getTotalProfitsOpen() == null || ret != 0) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            String sh = getIndex() + " 250d EMA";
            IndicatorList ukema = tc.getIndicatorList(sh);
            if (ukema == null) {
                MaIndicator ema = new MaIndicator();
                HashMap param = new HashMap();
                param.put(1, 250);
                ema.init(param, ma.MA.Exponential);
                ShareList ukx = tc.getSL(getIndex(),index);
                ukema = ema.buildIndicator(ukx);
                tc.putIndicatorList(sh, ukema);
            }
            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                IndicatorField inf = ukema.getSharedata(ukema.isDatePresent(d));
                //if (buy == 1) {
                if (inf.getValue() > inf.getSignal()) {
                    ret++;
                }
                /*} else if (inf.getValue() < inf.getSignal()) {
                 if (profit > 0) {
                 wins++;
                 } else {
                 loss++;
                 }
                 }*/
            }

        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        setFtse250AT(ret);
        return getFtse250AT();
    }

    @SuppressWarnings("unchecked")
    public double getEMAFilterBelowWL() {
        double ret = getFtse250BWL();
        int wins = 0;
        int loss = 0;
        if (getTotalProfitsOpen() == null || ret != 0) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            };
            String sh = getIndex() + " 250d EMA";
            IndicatorList ukema = tc.getIndicatorList(sh);
            if (ukema == null) {
                MaIndicator ema = new MaIndicator();
                HashMap param = new HashMap();
                param.put(1, 250);
                ema.init(param, ma.MA.Exponential);
                ShareList ukx = tc.getSL(getIndex(),index);
                ukema = ema.buildIndicator(ukx);
                tc.putIndicatorList(sh, ukema);
            }
            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                double profit = getTotalProfitsOpen().get(d);
                IndicatorField inf = ukema.getSharedata(ukema.isDatePresent(d));
                //if (buy == 1) {
                if (inf.getValue() < inf.getSignal()) {
                    if (profit > 0) {
                        wins++;
                    } else {
                        loss++;
                    }
                }
                /*} else if (inf.getValue() > inf.getSignal()) {
                 if (profit > 0) {
                 wins++;
                 } else {
                 loss++;
                 }
                 }*/
            }

        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (loss > 0) {
            setFtse250BWL(wins * 1.0 / loss);
        } else {
            setFtse250BWL(wins);
        }
        return getFtse250BWL();
    }

    @SuppressWarnings("unchecked")
    public int getEMAFilterBelowTrds() {
        int ret = getFtse250BT();
        if (getTotalProfitsOpen() == null || ret != 0) {
            return ret;
        }
        try {
            if (getTc() == null) {
                setTc(TradeCalculator.getInstance());
            }
            String sh = getIndex() + " 250d EMA";
            IndicatorList ukema = tc.getIndicatorList(sh);
            if (ukema == null) {
                MaIndicator ema = new MaIndicator();
                HashMap param = new HashMap();
                param.put(1, 250);
                ema.init(param, ma.MA.Exponential);
                ShareList ukx = tc.getSL(getIndex(),index);
                ukema = ema.buildIndicator(ukx);
                tc.putIndicatorList(sh, ukema);
            }
            for (long d : totalProfitsOpen.keySet()) {
                IndicatorField inf = ukema.getSharedata(ukema.isDatePresent(d));
                //if (buy == 1) {
                if (inf.getValue() < inf.getSignal()) {
                    ret++;
                }
                /*} else if (inf.getValue() < inf.getSignal()) {
                 if (profit > 0) {
                 wins++;
                 } else {
                 loss++;
                 }
                 }*/
            }

        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        setFtse250BT(ret);
        return getFtse250BT();
    }

    private double getQuat(double perc) {
        double ret = 100;
        if (perc <= 25) {
            ret = 0.25;
        } else if (perc > 25 && perc <= 50) {
            ret = 0.5;
        } else if (perc > 50 && perc <= 75) {
            ret = 0.75;
        } else if (perc > 75) {
            ret = 1;
        }
        return ret;
    }

    /**
     * @return the rangesOpen
     */
    public TreeMap<Double, ArrayList<Double>> getRangesOpen() {
        if (rangesOpen == null) {
            rangesOpen = new TreeMap<Double, ArrayList<Double>>();
        }
        return rangesOpen;
    }

    /**
     * @return the rangesClose
     */
    public TreeMap<Double, ArrayList<Double>> getRangesClose() {
        if (rangesClose == null) {
            rangesClose = new TreeMap<Double, ArrayList<Double>>();
        }
        return rangesClose;
    }

    /**
     * @return the totalProfitsOpen
     */
    public THashMap<Long, Double> getTotalProfitsOpen() {
        return totalProfitsOpen!=null?totalProfitsOpen:new THashMap<Long, Double>();
    }

    /**
     * @return the tl
     */
    public TradeList getTl() {
        return tl;
    }

    /**
     * @param tl the tl to set
     */
    public void setTl(TradeList tl) {
        this.tl = tl;
    }

    private TreeMap<Date, Integer> getTradingDays(TradeList tl) throws Exception {
        HashMap<Date, Date> datesDone = new HashMap<Date, Date>();
        TreeMap<Date, Integer> ret = new TreeMap<Date, Integer>();
        ShareList ukx = tc.getSL(getIndex(),index);
        for (int i = 0; i < tl.getSize(); i++) {
            Trade trd = tl.getTrade(i);
            Date close = datesDone.get(trd.getStartDate());
            if (close == null || trd.getCloseDate().after(close)) {
                datesDone.put(trd.getStartDate(), trd.getCloseDate());
                int start = ukx.isDatePresent(trd.getStartDate());
                if (close != null) {
                    start = ukx.isDatePresent(close) + 1;
                }
                int end = ukx.isDatePresent(trd.getCloseDate());
                for (int j = start; j <= end; j++) {
                    Date d = ukx.getSharedata(j).getDate();
                    Integer v = ret.get(d);
                    if (v == null) {
                        v = 1;
                    } else {
                        v += 1;
                    }
                    ret.put(d, v);
                }

            }
        }
        return ret;
    }

    private ArrayList<TreeSet<Date>> getUpDowns(TreeMap<Date, String> hls) throws Exception {
        ArrayList<TreeSet<Date>> ret = new ArrayList<TreeSet<Date>>(2);
        TreeSet<Date> up = new TreeSet<Date>();
        TreeSet<Date> down = new TreeSet<Date>();
        Calendar c = Calendar.getInstance();
        c.set(1997, 1, 1);
        Date starter = c.getTime();
        ShareList ukx = tc.getSL(getIndex(),index);
        for (Date d : hls.keySet()) {
            if (hls.higherKey(d) == null || hls.higherKey(d).before(starter)) {
                continue;
            }
            String val = hls.get(d);
            if (val.equals("L")) {
                Date high = hls.higherKey(d);
                if (high != null) {
                    int start = ukx.isDatePresent(d);
                    int end = ukx.isDatePresent(high);
                    for (int j = start; j <= end; j++) {
                        up.add(ukx.getSharedata(j).getDate());
                    }
                    //ret += end - start;
                }
            } else {
                Date high = hls.higherKey(d);
                if (high != null) {
                    int start = ukx.isDatePresent(d);
                    int end = ukx.isDatePresent(high);
                    for (int j = start; j <= end; j++) {
                        down.add(ukx.getSharedata(j).getDate());
                    }
                    //ret+=end-start;
                }
            }
        }
        ret.add(up);
        ret.add(down);
        return ret;
    }

    public int[] getUpDownTrades() {
        int[] ret = {0, 0, 0, 0, 0};
        ShareList ukx = null;
        try {
            ukx = tc.getSL(getIndex(),index);
        } catch (Exception ex) {
            Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (getTotalProfitsOpen() == null) {
            return ret;
        }
        try {
            TreeSet<Long> td = new TreeSet<>(getTotalProfitsOpen().keySet());
            for (Long d : td) {
                int dds = ukx.isDatePresent(d);
                ShareData sdT = ukx.getSharedata(dds);
                ShareData sdY = ukx.getSharedata(dds - 1);
                if (sdT.getClosePrice() > sdY.getClosePrice()) {
                    ret[0]++;
                } else {
                    ret[1]++;
                }
                double pdiff = (sdT.getClosePrice() - sdY.getClosePrice()) * 100 / sdY.getClosePrice();
                if (pdiff >= 0.5) {
                    ret[2]++;
                } else if (pdiff <= -0.5) {
                    ret[3]++;
                } else {
                    ret[4]++;
                }
            }
        } catch (Exception ex) {

        }
        return ret;
    }

    /**
     * @return the backPeriod
     */
    public int getBackPeriod() {
        return backPeriod;
    }

    /**
     * @param backPeriod the backPeriod to set
     */
    public void setBackPeriod(int backPeriod) {
        this.backPeriod = backPeriod;
    }

    public double[] getGraphLinear(int months) {
        double[] ret = new double[2];
        ret[0] = 0;
        ret[1] = 0;
        //HashMap<Date,Double> profits=new LinkedHashMap<Date, Double>();
        ArrayList<Double> profits = new ArrayList<Double>();
        Calendar cend = new GregorianCalendar(2000, 0, 1);
        Calendar cstart = Calendar.getInstance();
        cstart.setTime(cend.getTime());
        cend.add(Calendar.MONTH, months);
        double prft = 0;
        int cnt = 0;
        Date start = cstart.getTime();
        Date end = cend.getTime();
        Calendar c = Calendar.getInstance();
        TreeSet<Long> tp = new TreeSet<>(getTotalProfits().keySet());
        for (Long e : tp) {
            c.setTimeInMillis(e);
            if (c.get(Calendar.YEAR) < 2003) {
                continue;
            }
            Date d = new Date(e);
            Double p = getTotalProfits().get(e);
            if ((d.after(start) && d.before(end)) || (d.equals(end) || d.equals(start))) {
                prft += p;
            } else {
                do {
                    if (prft / 100 > 2) {
                        ret[0]++;
                    }
                    //profits.put(cend.getTime(),prft);
                    //profits.addShareData(new ShareData(months+"monts", e, prft, 0, 0, 0, 0));
                    profits.add(prft);
                    prft = 0;
                    cstart.add(Calendar.MONTH, months);
                    cend.add(Calendar.MONTH, months);
                    start = cstart.getTime();
                    end = cend.getTime();
                } while (!(d.after(start) && d.before(end) || (d.equals(end) || d.equals(start))));
                prft += p;
            }
        }
        double[] d = new double[profits.size()];

        for (int i = 0; i < profits.size(); i++) {
            d[i] = profits.get(i) / 100;
        }
        StandardDeviation sd = new StandardDeviation();
        double sdV = sd.evaluate(d);
        double mean = StatUtils.mean(d);
        ret[1] = sdV / mean;
        return ret;
    }

    /**
     * @return the _id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the _id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @param string the string to set
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     * @return the tProfit
     */
    public double gettProfit() {
        return tProfit;
    }

    /**
     * @param tProfit the tProfit to set
     */
    public void settProfit(double tProfit) {
        this.tProfit = tProfit;
    }

    /**
     * @return the allprofit
     */
    public double getAllprofit() {
        return allprofit;
    }

    /**
     * @param allprofit the allprofit to set
     */
    public void setAllprofit(double allprofit) {
        this.allprofit = allprofit;
    }

    /**
     * @return the tLoss
     */
    public double gettLoss() {
        return tLoss;
    }

    /**
     * @param tLoss the tLoss to set
     */
    public void settLoss(double tLoss) {
        this.tLoss = tLoss;
    }

    /**
     * @return the tGain
     */
    public double gettGain() {
        return tGain;
    }

    /**
     * @param tGain the tGain to set
     */
    public void settGain(double tGain) {
        this.tGain = tGain;
    }

    /**
     * @return the tCount
     */
    public int gettCount() {
        return tCount;
    }

    /**
     * @param tCount the tCount to set
     */
    public void settCount(int tCount) {
        this.tCount = tCount;
    }

    /**
     * @param gainTrades the gainTrades to set
     */
    public void setGainTrades(int gainTrades) {
        this.gainTrades = gainTrades;
    }

    /**
     * @return the lossTrades
     */
    public int getLossTrades() {
        return lossTrades;
    }

    /**
     * @param lossTrades the lossTrades to set
     */
    public void setLossTrades(int lossTrades) {
        this.lossTrades = lossTrades;
    }

    /**
     * @param gainPct the gainPct to set
     */
    public void setGainPct(double gainPct) {
        this.gainPct = gainPct;
    }

    /**
     * @param gainHighTrd the gainHighTrd to set
     */
    public void setGainHighTrd(double gainHighTrd) {
        this.gainHighTrd = gainHighTrd;
    }

    /**
     * @param gainAvgTrd the gainAvgTrd to set
     */
    public void setGainAvgTrd(double gainAvgTrd) {
        this.gainAvgTrd = gainAvgTrd;
    }

    /**
     * @param lossHighTrd the lossHighTrd to set
     */
    public void setLossHighTrd(double lossHighTrd) {
        this.lossHighTrd = lossHighTrd;
    }

    /**
     * @param lossAvgTrd the lossAvgTrd to set
     */
    public void setLossAvgTrd(double lossAvgTrd) {
        this.lossAvgTrd = lossAvgTrd;
    }

    /**
     * @param consecGain the consecGain to set
     */
    public void setConsecGain(int consecGain) {
        this.consecGain = consecGain;
    }

    /**
     * @param maxGainProfit the maxGainProfit to set
     */
    public void setMaxGainProfit(double maxGainProfit) {
        this.maxGainProfit = maxGainProfit;
    }

    /**
     * @param consecGainProfit the consecGainProfit to set
     */
    public void setConsecGainProfit(double consecGainProfit) {
        this.consecGainProfit = consecGainProfit;
    }

    /**
     * @param consecLoss the consecLoss to set
     */
    public void setConsecLoss(int consecLoss) {
        this.consecLoss = consecLoss;
    }

    /**
     * @param maxLossProfit the maxLossProfit to set
     */
    public void setMaxLossProfit(double maxLossProfit) {
        this.maxLossProfit = maxLossProfit;
    }

    /**
     * @param consecLossProfit the consecLossProfit to set
     */
    public void setConsecLossProfit(double consecLossProfit) {
        this.consecLossProfit = consecLossProfit;
    }

    /**
     * @param maxOpenCount the maxOpenCount to set
     */
    public void setMaxOpenCount(long maxOpenCount) {
        this.maxOpenCount = maxOpenCount;
    }

    /**
     * @param minOpenCount the minOpenCount to set
     */
    public void setMinOpenCount(long minOpenCount) {
        this.minOpenCount = minOpenCount;
    }

    /**
     * @param minOpenCountMidle the minOpenCountMidle to set
     */
    public void setMinOpenCountMidle(long minOpenCountMidle) {
        this.minOpenCountMidle = minOpenCountMidle;
    }

    /**
     * @param avgOpenCountMidle the avgOpenCountMidle to set
     */
    public void setAvgOpenCountMidle(long avgOpenCountMidle) {
        this.avgOpenCountMidle = avgOpenCountMidle;
    }

    /**
     * @param gainTime the gainTime to set
     */
    public void setGainTime(int gainTime) {
        this.gainTime = gainTime;
    }

    /**
     * @param lossTime the lossTime to set
     */
    public void setLossTime(int lossTime) {
        this.lossTime = lossTime;
    }

    /**
     * @param totalOpenDays the totalOpenDays to set
     */
    public void setTotalOpenDays(int totalOpenDays) {
        this.totalOpenDays = totalOpenDays;
    }

    /**
     * @return the zero
     */
    public double getZero() {
        return zero;
    }

    /**
     * @param zero the zero to set
     */
    public void setZero(double zero) {
        this.zero = zero;
    }

    /**
     * @return the one
     */
    public double getOne() {
        return one;
    }

    /**
     * @param one the one to set
     */
    public void setOne(double one) {
        this.one = one;
    }

    /**
     * @return the two
     */
    public double getTwo() {
        return two;
    }

    /**
     * @param two the two to set
     */
    public void setTwo(double two) {
        this.two = two;
    }

    /**
     * @return the three
     */
    public double getThree() {
        return three;
    }

    /**
     * @param three the three to set
     */
    public void setThree(double three) {
        this.three = three;
    }

    /**
     * @return the four
     */
    public double getFour() {
        return four;
    }

    /**
     * @param four the four to set
     */
    public void setFour(double four) {
        this.four = four;
    }

    /**
     * @return the five
     */
    public double getFive() {
        return five;
    }

    /**
     * @param five the five to set
     */
    public void setFive(double five) {
        this.five = five;
    }

    /**
     * @return the six
     */
    public double getSix() {
        return six;
    }

    /**
     * @param six the six to set
     */
    public void setSix(double six) {
        this.six = six;
    }

    /**
     * @return the dayPercent
     */
    public double getDayPercent() {
        return dayPercent;
    }

    /**
     * @param dayPercent the dayPercent to set
     */
    public void setDayPercent(double dayPercent) {
        this.dayPercent = dayPercent;
    }

    /**
     * @param multiPercent the multiPercent to set
     */
    public void setMultiPercent(double multiPercent) {
        this.multiPercent = multiPercent;
    }

    /**
     * @param trdDayPercent the trdDayPercent to set
     */
    public void setTrdDayPercent(double trdDayPercent) {
        this.trdDayPercent = trdDayPercent;
    }

    /**
     * @return the yr
     */
    public ArrayList<String> getYr() {
        return yr;
    }

    /**
     * @param yr the yr to set
     */
    public void setYr(ArrayList<String> yr) {
        this.yr = yr;
    }

    /**
     * @return the TrdCount
     */
    public ArrayList<Integer> getTrdCount() {
        return TrdCount;
    }

    /**
     * @param TrdCount the TrdCount to set
     */
    public void setTrdCount(ArrayList<Integer> TrdCount) {
        this.TrdCount = TrdCount;
    }

    /**
     * @return the yrGainCount
     */
    public ArrayList<Integer> getYrGainCount() {
        return yrGainCount;
    }

    /**
     * @param yrGainCount the yrGainCount to set
     */
    public void setYrGainCount(ArrayList<Integer> yrGainCount) {
        this.yrGainCount = yrGainCount;
    }

    /**
     * @return the yrLoserCount
     */
    public ArrayList<Integer> getYrLoserCount() {
        return yrLoserCount;
    }

    /**
     * @param yrLoserCount the yrLoserCount to set
     */
    public void setYrLoserCount(ArrayList<Integer> yrLoserCount) {
        this.yrLoserCount = yrLoserCount;
    }

    /**
     * @param PProfit the PProfit to set
     */
    public void setPProfit(TDoubleArrayList PProfit) {
        this.PProfit = PProfit;
    }

    /**
     * @param drawDown the drawDown to set
     */
    public void setDrawDown(TDoubleArrayList drawDown) {
        this.drawDown = drawDown;
    }

    /**
     * @param drawDownTrades the drawDownTrades to set
     */
    public void setDrawDownTrades(TDoubleArrayList drawDownTrades) {
        this.drawDownTrades = drawDownTrades;
    }

    /**
     * @param totalDrawdown the totalDrawdown to set
     */
    public void setTotalDrawdown(THashMap<Long, ArrayList> totalDrawdown) {
        this.totalDrawdown = totalDrawdown;
    }

    /**
     * @param totalProfits the totalProfits to set
     */
    public void setTotalProfits(THashMap<Long, Double> totalProfits) {
        this.totalProfits = totalProfits;
    }

    /**
     * @param totalProfitsOpen the totalProfitsOpen to set
     */
    public void setTotalProfitsOpen(THashMap<Long, Double> totalProfitsOpen) {
        this.totalProfitsOpen = totalProfitsOpen;
    }

    /**
     * @return the openUD
     */
    public double getOpenUD() {
        return openUD;
    }

    /**
     * @param openUD the openUD to set
     */
    public void setOpenUD(double openUD) {
        this.openUD = openUD;
    }

    /**
     * @return the closeUD
     */
    public double getCloseUD() {
        return closeUD;
    }

    /**
     * @param closeUD the closeUD to set
     */
    public void setCloseUD(double closeUD) {
        this.closeUD = closeUD;
    }

    /**
     * @return the ftse250
     */
    public double getFtse250() {
        return ftse250;
    }

    /**
     * @param ftse250 the ftse250 to set
     */
    public void setFtse250(double ftse250) {
        this.ftse250 = ftse250;
    }

    /**
     * @return the ftse250AWL
     */
    public double getFtse250AWL() {
        return ftse250AWL;
    }

    /**
     * @param ftse250AWL the ftse250AWL to set
     */
    public void setFtse250AWL(double ftse250AWL) {
        this.ftse250AWL = ftse250AWL;
    }

    /**
     * @return the ftse250BWL
     */
    public double getFtse250BWL() {
        return ftse250BWL;
    }

    /**
     * @param ftse250BWL the ftse250BWL to set
     */
    public void setFtse250BWL(double ftse250BWL) {
        this.ftse250BWL = ftse250BWL;
    }

    /**
     * @return the ftse250AT
     */
    public int getFtse250AT() {
        return ftse250AT;
    }

    /**
     * @param ftse250AT the ftse250AT to set
     */
    public void setFtse250AT(int ftse250AT) {
        this.ftse250AT = ftse250AT;
    }

    /**
     * @return the ftse250BT
     */
    public int getFtse250BT() {
        return ftse250BT;
    }

    /**
     * @param ftse250BT the ftse250BT to set
     */
    public void setFtse250BT(int ftse250BT) {
        this.ftse250BT = ftse250BT;
    }

    /**
     * @param rangesOpen the rangesOpen to set
     */
    public void setRangesOpen(TreeMap<Double, ArrayList<Double>> rangesOpen) {
        this.rangesOpen = rangesOpen;
    }

    /**
     * @param rangesClose the rangesClose to set
     */
    public void setRangesClose(TreeMap<Double, ArrayList<Double>> rangesClose) {
        this.rangesClose = rangesClose;
    }

    /**
     * @return the actualOpen
     */
    public int getActualOpen() {
        return actualOpen;
    }

    /**
     * @param actualOpen the actualOpen to set
     */
    public void setActualOpen(int actualOpen) {
        this.actualOpen = actualOpen;
    }

    /**
     * @param grpShares the grpShares to set
     */
    public void setGrpShares(int grpShares) {
        this.grpShares = grpShares;
    }

    /**
     * @param tradeUnit the tradeUnit to set
     */
    public void setTradeUnit(double tradeUnit) {
        this.tradeUnit = tradeUnit;
    }

    /**
     * @param share the share to set
     */
    public void setShare(String share) {
        this.share = share;
    }

    /**
     * @param closeShare the closeShare to set
     */
    public void setCloseShare(String closeShare) {
        this.closeShare = closeShare;
    }

    /**
     * @return the absInd
     */
    public AbstractIndicator getAbsInd() {
        return absInd;
    }

    /**
     * @param absInd the absInd to set
     */
    public void setAbsInd(AbstractIndicator absInd) {
        this.absInd = absInd;
    }

    /**
     * @return the opn
     */
    public Open getOpn() {
        return opn;
    }

    /**
     * @param opn the opn to set
     */
    public void setOpn(Open opn) {
        this.opn = opn;
    }

    /**
     * @return the clsAbsInd
     */
    public AbstractIndicator getClsAbsInd() {
        return clsAbsInd;
    }

    /**
     * @param clsAbsInd the clsAbsInd to set
     */
    public void setClsAbsInd(AbstractIndicator clsAbsInd) {
        this.clsAbsInd = clsAbsInd;
    }

    /**
     * @return the cls
     */
    public Close getCls() {
        return cls;
    }

    /**
     * @param cls the cls to set
     */
    public void setCls(Close cls) {
        this.cls = cls;
    }

    /**
     * @return the opnFilter
     */
    public Filters getOpnFilter() {
        return opnFilter;
    }

    /**
     * @param opnFilter the opnFilter to set
     */
    public void setOpnFilter(Filters opnFilter) {
        this.opnFilter = opnFilter;
    }

    /**
     * @return the clsFilter
     */
    public Filters getClsFilter() {
        return clsFilter;
    }

    /**
     * @param clsFilter the clsFilter to set
     */
    public void setClsFilter(Filters clsFilter) {
        this.clsFilter = clsFilter;
    }

    /**
     * @param buy the buy to set
     */
    public void setBuy(int buy) {
        this.buy = buy;
    }

    /**
     * @param consec the consec to set
     */
    public void setConsec(int consec) {
        this.consec = consec;
    }

    /**
     * @return the sStopLoss
     */
    public double getsStopLoss() {
        return sStopLoss;
    }

    /**
     * @param sStopLoss the sStopLoss to set
     */
    public void setsStopLoss(double sStopLoss) {
        this.sStopLoss = sStopLoss;
    }

    /**
     * @return the iStopLoss
     */
    public double getiStopLoss() {
        return iStopLoss;
    }

    /**
     * @param iStopLoss the iStopLoss to set
     */
    public void setiStopLoss(double iStopLoss) {
        this.iStopLoss = iStopLoss;
    }

    /**
     * @return the topCap
     */
    public int getTopCap() {
        return topCap;
    }

    /**
     * @param topCap the topCap to set
     */
    public void setTopCap(int topCap) {
        this.topCap = topCap;
    }

    /**
     * @param iClose the iClose to set
     */
    public void setiClose(IndividualClose iClose) {
        this.iClose = iClose;
    }

    /**
     * @return the tc
     */
    public TradeCalculator getTc() {
        return tc;
    }

    /**
     * @param tc the tc to set
     */
    public void setTc(TradeCalculator tc) {
        this.tc = tc;
    }

    /**
     * @return the sl
     */
    public ShareList getSl() {
        return sl;
    }

    /**
     * @param sl the sl to set
     */
    public void setSl(ShareList sl) {
        this.sl = sl;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * @return the goodscore
     */
    public int getGoodscore() {
        return goodscore;
    }

    /**
     * @param goodscore the goodscore to set
     */
    public void setGoodscore(int goodscore) {
        this.goodscore = goodscore;
    }

    /**
     * @param yearlyPerf the yearlyPerf to set
     */
    public void setYearlyPerf(String yearlyPerf) {
        this.yearlyPerf = yearlyPerf;
    }

    /**
     * @param newHighDays the newHighDays to set
     */
    public void setNewHighDays(double newHighDays) {
        this.newHighDays = newHighDays;
    }

    /**
     * @return the yrGrades
     */
    public int getYrGrades() {
        return yrGrades;
    }

    /**
     * @param yrGrades the yrGrades to set
     */
    public void setYrGrades(int yrGrades) {
        this.yrGrades = yrGrades;
    }

    /**
     * @param flatGrade the flatGrade to set
     */
    public void setFlatGrade(int flatGrade) {
        this.flatGrade = flatGrade;
    }

    /**
     * @param positiveGrade the positiveGrade to set
     */
    public void setPositiveGrade(int positiveGrade) {
        this.positiveGrade = positiveGrade;
    }

    /**
     * @param flatProfit the flatProfit to set
     */
    public void setFlatProfit(double flatProfit) {
        this.flatProfit = flatProfit;
    }

    /**
     * @return the posYrProfit
     */
    public double getPosYrProfit() {
        return posYrProfit;
    }

    /**
     * @param posYrProfit the posYrProfit to set
     */
    public void setPosYrProfit(double posYrProfit) {
        this.posYrProfit = posYrProfit;
    }

    /**
     * @return the posLYrProfit
     */
    public double getPosLYrProfit() {
        return posLYrProfit;
    }

    /**
     * @param posLYrProfit the posLYrProfit to set
     */
    public void setPosLYrProfit(double posLYrProfit) {
        this.posLYrProfit = posLYrProfit;
    }

    /**
     * @return the posYrGrade
     */
    public int getPosYrGrade() {
        return posYrGrade;
    }

    /**
     * @param posYrGrade the posYrGrade to set
     */
    public void setPosYrGrade(int posYrGrade) {
        this.posYrGrade = posYrGrade;
    }

    /**
     * @return the posLYrGrade
     */
    public int getPosLYrGrade() {
        return posLYrGrade;
    }

    /**
     * @param posLYrGrade the posLYrGrade to set
     */
    public void setPosLYrGrade(int posLYrGrade) {
        this.posLYrGrade = posLYrGrade;
    }

}
