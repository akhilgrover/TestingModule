/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package report;

import Share.ShareList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.THashMap;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import trade.Trade;
import trade.TradeCalculator;
import trade.TradeList;

/**
 *
 * @author akhil
 */
public class SummaryGroup extends Summary {
    private static final long serialVersionUID = 1L;
    private final ArrayList<Summary> sums;
    private int pairStats;
    private int countHigh;
    private int countLow;
    private int rank;

    public SummaryGroup(){
        sums=new ArrayList<>();
    }
    
    public SummaryGroup(ArrayList<Summary> sums) {
        this.sums = sums;
    }

    public SummaryGroup(String id, double i) {
        super(id,i);
        sums=new ArrayList<>();
    }
    
    public ArrayList<Summary> getGroup(){
        return sums;
    }
    
    public Summary getSummary(int i) {
        return sums.get(i);
    }
    
    public void addSummary(List<Summary> sum) {
        sums.addAll(sum);
    }
    
    public void addSummary(Summary sum){
        sums.add(sum);
    }

    @Override
    public void calculateTrades(TradeList trdList, ShareList sl, int topShares, TradeCalculator tc, boolean drawCalc) throws Exception {
        setGrpShares(topShares);
        if (sl == null) {
            sl=tc.getSL(getShare(),sl.getShare());
        }
        this.setTc(tc);
        this.setSl(sl);
        if(getShare() ==null ||getShare().isEmpty())
            setShare(sl.getShare());
        //this.tl=trdList;
        String bb = "Sell";
        if (getBuy() == 1) {
            bb = "Buy";
        }
        if(getName()==null || getName().isEmpty()) {
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
            if (getGrpShares() > 1) {
                setName(getName() + " " + getGrpShares() + " Shares ");
            }
            setName(getName() + bb);
        }
        if (tc == null) {
            tc = TradeCalculator.getInstance();
        }
        TDoubleArrayList arrProfit = new TDoubleArrayList(sl.getSize());
        setTotalProfits(new THashMap<Long, Double>());
        setTotalProfitsOpen(new THashMap<Long, Double>());
        //Double profit=0.0;
        int currGStreak = 0, currLStreak = 0;
        THashMap<Date, THashMap<Date, Integer>> dateFreq = new THashMap<Date, THashMap<Date, Integer>>(sl.getSize());
        HashMap<String, Double> pair=new HashMap<>();
        HashMap<String, Integer> shareTrades=new HashMap<>();
        HashMap<String, Double> shareProfit=new HashMap<>();
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
        int size=trdList.getSize();
//        if(sdf==null)
//            sdf=new SimpleDateFormat("yy");
        Calendar cal=Calendar.getInstance();
        for (int i = 0; i < size; i++) {
            Trade trd = trdList.getTrade(i);
            double profit=0.0;
            if(trd.getProfit()==0 && getIndex()!=null){
                trd=tc.calcDividendTrade(trd, getBuy(), getGrpShares(), getIndex());
                profit = trd.getProfit();//calcProfit(trd.getStartPrice(),trd.getClosePrice(),buy);
            }
            else if(getIndex()==null){
                profit = calcProfit(trd.getStartPrice(),trd.getClosePrice(),getBuy());
            } else{
                profit = trd.getProfit();//calcProfit(trd.getStartPrice(),trd.getClosePrice(),buy);
            }
            String sh=trd.getShare().split(",")[0];
            Integer nCnt=shareTrades.get(sh);
            Double nProfit=shareProfit.get(sh);
            if(nCnt==null)
                nCnt=0;
            shareTrades.put(sh, ++nCnt);
            if(nProfit==null)
                nProfit=0.0;
            nProfit+=profit;
            shareProfit.put(sh, nProfit);
            if(pair.containsKey(sh)){
                pair.put(sh, pair.get(sh)+profit);
            } else if(sh.length()>4){
                String cur1=sh.substring(3, 6)+sh.substring(0, 3);
                if(pair.containsKey(cur1)){
                    pair.put(cur1, pair.get(cur1)+profit);
                } else{
                    pair.put(sh, profit);
                }
            } else{
                pair.put(sh, profit);
            }
            Trade Old=trd;
            if(i>0)
                Old=trdList.getTrade(i - 1);
            cal.setTime(trd.getStartDate());
            int nStart=cal.get(Calendar.YEAR);
            setAllprofit(getAllprofit() + profit);
            if(getBuy()==1 && nStart<minYear){
                continue;
            }
            cal.setTime(Old.getStartDate());
            int oStart=cal.get(Calendar.YEAR);
            if (i > 0 && nStart > oStart && ((getBuy()==1 && oStart>=minYear) || getBuy()==0)) {
                String yrd = oStart+"";//sdf.format(Old.getStartDate());
                yrd=yrd.substring(2);
                if (!getYr().contains(yrd)) {
                    getYr().add(yrd);
                } else {
                    System.out.println(yrd + "," + Old);
                }
                getPProfit().add(yrprofit / 100);
                getTrdCount().add(yrcount / getGrpShares());
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
                ArrayList<Double> downs=getDrawDown(yrTrades,buy, getGrpShares(),sl,tc);
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
            int dyr =cal.get(Calendar.YEAR);
            String yrd = (dyr+"").substring(2);//sdf.format(trd.getCloseDate());
            cal.setTime(trd.getStartDate());
            int dyrS =cal.get(Calendar.YEAR);
            String yrdS = (dyrS+"").substring(2);//sdf.format(trd.getStartDate());
            if (!yrTrades.containsKey(yrdS)) {
                yrTrades.put(yrdS, new TradeList());
            }
            TradeList yrs = yrTrades.get(yrd);
            if (yrs == null) {
                yrs = new TradeList();
            }
            yrs.addTrade(trd);
            yrTrades.put(yrd, yrs);
            if(getGrpShares()==1)
                grpProfit += profit;
            if ((i > 0 && i % getGrpShares() == 0) || i == trdList.getSize() - 1 || (i == 0 && getGrpShares() == 1)) {
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
                                setConsecLossProfit(((startProfit - (getTProfit() + grpProfit)) / (startProfit + 10000)) * 100);
                            } else if (startProfit == 0) {
                                setConsecLossProfit(((startProfit - (getTProfit() + grpProfit)) / 10000) * 100);
                            }
                        }
                    } else {
                        if (startProfit > 0) {
                            cGainP = ((getTProfit() - startProfit) / (startProfit + 10000)) * 100;
                        } else {
                            cGainP = 0;
                        }

                        //System.out.println(trd.getStartDate() + "\t" + startProfit + "\t" + tProfit + "\t" + maxGainProfit + "\t" + cGainP);
                        if (cGainP > getMaxGainProfit()) {
                            setMaxGainProfit(cGainP);
                        }
                        startProfit = getTProfit();
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
                                setConsecGainProfit((((getTProfit() + grpProfit) - startProfit) / (startProfit + 10000)) * 100);
                            } else if (startProfit == 0) {
                                setConsecGainProfit((((getTProfit() + grpProfit) - startProfit) / 10000) * 100);
                            }
                        }
                    } else {
                        if (startProfit > 0) {
                            cLossP = ((startProfit - getTProfit()) / (startProfit + 10000)) * 100;
                        } else {
                            cLossP = 0;
                        }
                        //System.out.println(trd.getStartDate() + "\t" + startProfit + "\t" + tProfit + "\t" + maxLossProfit + "\t" + cLossP);
                        if (cLossP > getMaxLossProfit()) {
                            setMaxLossProfit(cLossP);
                        }
                        startProfit = getTProfit();
                        currGStreak = 1;
                    }
                }
                grpProfit = 0;
            }
            if(getGrpShares()>1)
                grpProfit += profit;
            settCount(gettCount() + 1);
            settProfit(getTProfit() + profit);
            Double prft = profit;
            //LocalDate dat = LocalDate.ofEpochDay(trd.getCloseDate().getTime());
            //Instant instant = trd.getCloseDate().toInstant();
            //LocalDate res = instant.atZone(ZoneId.systemDefault()).toLocalDate();
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
            String cur="CUR";
            if(!getShare().contains(","))
                cur=getIndex();
            ShareList sltrd=tc.getSL(trd.getShare(),cur);
            int endIndex = sltrd.isDatePresent(trd.getCloseDate());
            if (endIndex == -1) {
                endIndex = sltrd.isLowerDatePresent(trd.getCloseDate());
            }
            int stIndex = sltrd.isDatePresent(trd.getStartDate());
            if (stIndex == -1) {
                stIndex = sltrd.isLowerDatePresent(trd.getStartDate());
            }
            for (int j = stIndex; j < endIndex; j++) {
                THashMap<Date, Integer> tmap = null;//new HashMap<Date, Integer>(4);
                Date dd = sltrd.getSharedata(j).getDate();
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
            if (arrTdiff.size() == getGrpShares() && i < trdList.getSize()) {
                Collections.sort(arrTdiff);
                Double midle = (0.7 * getGrpShares()) - 1;
                int hDif = arrTdiff.get(getGrpShares() - 1);
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
        }//end of trades
        if (trdList.getSize() > 0) {
            cal.setTime(trdList.getTrade(trdList.getSize() - 1).getStartDate());
            int dyrS =cal.get(Calendar.YEAR);
            String yrd = (dyrS+"").substring(2);//sdf.format(trdList.getTrade(trdList.getSize() - 1).getStartDate());
            if (!getYr().contains(yrd)) {
                getYr().add(yrd);
            }
        }
        for(Map.Entry<String,Double> prftPair: pair.entrySet()){
            if(prftPair.getValue()>10000.0)
                setPairStats(getPairStats() + 1);
        }
        for(Map.Entry<String,Double> e:shareProfit.entrySet()){
            if(e.getValue()>0.0)
                setRank(getRank() + 1);
        }
        ArrayList<Integer> trdsSh=new ArrayList<>(shareTrades.values());
        Collections.sort(trdsSh);
        if (trdsSh.size() > 2) {
            setCountHigh(trdsSh.get(trdsSh.size() - 3));
            setCountLow(trdsSh.get(2));
        }
        if (drawCalc) {
            setTotalDrawdown(getTotalDrawDown(trdList, getBuy(), getGrpShares(), sl, tc));
        } else{
            setTotalDrawdown(new THashMap<Long, ArrayList>());
        }
        getPProfit().add(yrprofit / 100);
        getTrdCount().add(yrcount / getGrpShares());
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
                if (!getYr().contains(yrs)) {
                    getYr().add(yrs);

                    Collections.sort(getYr(), new Comparator<String>() {

                        @Override
                        public int compare(String o1, String o2) {
                            int i = 0;
                            try {
                                //i = sdf.parse(o1).compareTo(sdf.parse(o2));
                                Integer i1=Integer.valueOf(o1);
                                Integer i2=Integer.valueOf(o2);
                                i =i1.compareTo(i2);
                            } catch (Exception ex) {
                                Logger.getLogger(Summary.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            return i;
                        }
                    });
                    int ind = getYr().indexOf(yrs);
                    getPProfit().insert(ind, 0.0);
                    getTrdCount().add(ind, 0);
                    getYrLoserCount().add(ind, 0);
                    getYrGainCount().add(ind, 0);
                }
                if (drawCalc) {
                    TradeList yrTrds = yrTrades.get(yrs);
                    ArrayList<Double> downs = getDrawDown(yrTrds, getBuy(), getGrpShares(), sl, tc);
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

        settCount(gettCount() / getGrpShares());
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
        if (getTProfit() > 0) {
            setGainPct((gettGain() / getTProfit()) * 100);
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
    
//    public String getPairStats(){
//        
//    }

    /**
     * @return the pairStats
     */
    public int getPairStats() {
        return pairStats;
    }

    /**
     * @param pairStats the pairStats to set
     */
    public void setPairStats(int pairStats) {
        this.pairStats = pairStats;
    }

    /**
     * @return the countHigh
     */
    public int getCountHigh() {
        return countHigh;
    }

    /**
     * @param countHigh the countHigh to set
     */
    public void setCountHigh(int countHigh) {
        this.countHigh = countHigh;
    }

    /**
     * @return the countLow
     */
    public int getCountLow() {
        return countLow;
    }

    /**
     * @param countLow the countLow to set
     */
    public void setCountLow(int countLow) {
        this.countLow = countLow;
    }

    /**
     * @return the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    
    
}
