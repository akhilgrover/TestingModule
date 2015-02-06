package trade;

import Dividend.*;
import Results.ResultData;
import Results.ResultList;
import Sectors.SuperSect;
import indicator.*;
import Share.*;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import datasource.BasicShareDB;
import datasource.ShareListDB;
import gnu.trove.map.hash.THashMap;
import java.io.Serializable;
import java.net.URI;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import trade.close.*;
import trade.filter.*;
import trade.open.*;
import ma.MA;
import report.Summary;
import report.SummaryGroup;

/**
 *
 * @author admin
 */
public class TradeCalculator implements Serializable {

    private transient THashMap<String, ShareList> shList;
    private transient THashMap<String, ShareList> shListMa;
    //private transient Cache shList;
    private transient THashMap<String, IndicatorList> indList;
    private transient ConcurrentHashMap<String,CountDownLatch> indStore;
    private transient THashMap<String,THashMap<Boolean, THashMap<Date, ArrayList<String>>>> shDayList;
    private transient THashMap<String, DividendList> divList;
    private transient THashMap<String, ResultList> resList;
    private transient ShareListDB shareDB;
    private transient BasicShareDB bsDB;
    private transient SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private transient TreeMap<Date,String> ukxHighLow;
    private transient CouchbaseClient client=null;
    private transient boolean live=true;
    private transient final List<String>arr= Arrays.asList(" ","","Indicator","I","Close","C","Open","O","Shares","Sh","Filter","Ft","Below","Be","Above","Av",".0","","Sys","S","INC","IC","EXC","EC");
    private transient Date dd = null;
    private transient final int exp=3600;
    private transient final int expDay=exp*2;
    private transient HashMap<String,SuperSect> sectorList=new HashMap<>();


    private static final TradeCalculator INSTANCE = new TradeCalculator();

    public TradeCalculator() {
        try {
            Calendar c= Calendar.getInstance();
            c.set(1995, 6, 1);
            dd = c.getTime();
            /*shList=new FastMap<String, ShareList>().setKeyComparator(FastComparator.STRING);
            indList=new FastMap<String, IndicatorList>().setKeyComparator(FastComparator.STRING);
            shDayList=new FastMap<Date, ArrayList>().setKeyComparator(FastComparator.IDENTITY);
            divList=new FastMap<String, DividendList>().setKeyComparator(FastComparator.STRING);*/
    //        shList=FastMap.newInstance();
    //        indList=FastMap.newInstance();
    //        shDayList=FastMap.newInstance();
    //        divList=FastMap.newInstance();
            //shList = new HashMap<String, ShareList>(270);
           /* Config cfg = new Config();
            MapConfig mcfg=new MapConfig("Near Cache");
            NearCacheConfig ncfg=new NearCacheConfig(3600, 500, "NONE", 3600, true);
            mcfg.setNearCacheConfig(ncfg);
            MaxSizeConfig mm=new MaxSizeConfig();
            mm.setMaxSizePolicy(MaxSizeConfig.POLICY_USED_HEAP_SIZE);
            mm.setSize(4096);
            mcfg.setMaxSizeConfig(mm);
            cfg.addMapConfig(mcfg);
            HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);

            shList=hz.getMap("shlist");
            System.out.println(shList.size());*/
            //CacheManager ehMgr = CacheManager.getInstance();
            //Configuration configuration = new Configuration().terracotta(new TerracottaClientConfiguration().url("192.168.1.18:9510")).defaultCache(new CacheConfiguration("defaultCache", 100)).cache(new CacheConfiguration("shList", 10000).timeToIdleSeconds(3600).timeToLiveSeconds(3600).terracotta(new TerracottaConfiguration()));
            //Configuration configuration = new Configuration().defaultCache(new CacheConfiguration("defaultCache", 10000)).cache(new CacheConfiguration("shList", 10000).eternal(true));
            //CacheConfiguration config = new CacheConfiguration("shList", 10000);//.overflowToOffHeap(true).maxBytesLocalOffHeap(1,MemoryUnit.GIGABYTES);
            //CacheManager ehMgr = new CacheManager(configuration);
            //shList=ehMgr.getCache("shList");
            shList=new THashMap<String, ShareList>(500);
            if (client == null && !live) {
                try{
                List<URI> hosts = Arrays.asList(
                        /*new URI("http://192.168.1.4:8091/pools"),
                        new URI("http://192.168.1.12:8091/pools"),
                        new URI("http://192.168.1.18:8091/pools"),
                        new URI("http://192.168.1.26:8091/pools"),
                        new URI("http://192.168.1.27:8091/pools"),
                        new URI("http://192.168.1.18:8091/pools"),*/
                        new URI("http://192.168.1.40:8091/pools"));

                CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
                cfb.setOpTimeout(1000);
                //cfb.setShouldOptimize(true);
                cfb.setOpQueueMaxBlockTime(1000);
                CouchbaseConnectionFactory cf = cfb.buildCouchbaseConnection(hosts, "BackTradeMem", "");
                client = new CouchbaseClient(cf);
                }catch (Exception ex) {
                }
//                try {
//                    ShareList sl = (ShareList) client.get("UKX");
//                    if (sl.getSharedata(sl.getSize() - 1).getDate().before(sdf.parse(sdf.format(new Date())))) {
//                        client.flush().get();
//                    }
//                } catch (Exception ex) {
//                }
            }
            shListMa=new THashMap<String, ShareList>(500);
            indList = new THashMap<String, IndicatorList>(1500);
            indStore = new ConcurrentHashMap<String, CountDownLatch>();
            shDayList = new THashMap<String, THashMap<Boolean, THashMap<Date, ArrayList<String>>>> (2);
            divList = new THashMap<String, DividendList>(200);
            shareDB = new ShareListDB();
            bsDB = new BasicShareDB();
            resList = new THashMap<String, ResultList>();
        } catch (Exception ex) {
            Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static final TradeCalculator getInstance(){
        return INSTANCE;
    }

    public synchronized  void refresh() throws Exception {
        if (shareDB != null) {
            shareDB.close();
        }
        if (bsDB != null) {
            bsDB.close();
        }
        shareDB = new ShareListDB();
        bsDB = new BasicShareDB();
        boolean b=bsDB.checkConnQ();
//        FastMap.recycle(shList);
//        FastMap.recycle(indList);
//        FastMap.recycle(shDayList);
//        FastMap.recycle(divList);
//        shList=FastMap.newInstance();
//        indList=FastMap.newInstance();
//        shDayList=FastMap.newInstance();
//        divList=FastMap.newInstance();
        //shList.removeAll();

        shList.clear();
        shListMa.clear();
        indList.clear();
        shDayList.clear();
        divList.clear();
        //client.shutdown(5, TimeUnit.SECONDS);
        try{
            ShareList sl = (ShareList) client.get("UKX");
            ShareList sln = bsDB.getShareData("UKX");
            if(sl.getSharedata(sl.getSize()-1).getDate().before(sln.getSharedata(sl.getSize()-1).getDate()))
            {
                client.flush().get();
                System.out.println("Flushed");

            }
        }catch(Exception ex){

        }

    }

    public void close() throws Exception {
        /*if (shareDB != null) {
            shareDB.close();
        }
        if (bsDB != null) {
            bsDB.close();
        }
        if (shList != null) {
            shList.clear();
        }
        if (indList != null) {
            indList.clear();
        }
        if (shDayList != null) {
            shDayList.clear();
        }
        if (divList != null) {
            divList.clear();
        }
        //FastMap.recycle(shList);
        shList = null;
        //FastMap.recycle(indList);
        indList = null;
        //FastMap.recycle(shDayList);
        shDayList = null;
        //FastMap.recycle(divList);
        divList = null;
        //shList.clear();
        //shDayList.clear();
         *
         */
        refresh();
    }

    /**
     * Calculate the trades for a single function.
     * 1: calculate open trades
     * 2: calculated closes for the open.
     * return the trade list.
     *
     * @param indList
     *  Indicator list to be used to calculate trades.
     * @param indCloseList
     * @param sl
     *  Share list to calculate the trades on.
     * @param open
     *  Open class to calculate open trades. an implementation of open interface
     *  is used.
     * @param openFilter
     *  A Filter implementing Filters interface used to filter open trades.
     * @param close
     *  A Close calculating class implementing Close interface.
     * @param closeFilter
     *  A Filter implementing Filters interface used to filter Close trades.
     * @return
     *  Returns the Trade List for the given function with an open
     *  and close parameter pair
     *
     */
    public TradeList getTrades(IndicatorList indList, IndicatorList indCloseList, ShareList sl, Open open, Filters openFilter, Close close, Filters closeFilter) {
        TradeList tl, tlfinal;
        //if(!shList.containsKey(sl.getShare()))
        //shList.putIfAbsent(new Element(sl.getShare(), sl));

        if (!shList.contains(sl.getShare())) {
            shList.put(sl.getShare(), sl);
            if(!live){
                try{
                client.add(sl.getShare(), expDay, sl);
                }catch(Exception e){}
            }

        }
        tl = open.fillOpen(indList, sl, openFilter);
        if (indCloseList == null) {
            tlfinal = close.fillClose(tl, indList, sl, closeFilter);
        } else {
            if (close instanceof ThresholdOrDaysCutCloseSL) {
                ThresholdOrDaysCutCloseSL tclose = (ThresholdOrDaysCutCloseSL) close;
                tlfinal = tclose.fillClose(tl, indCloseList, indList, sl, closeFilter);
            } else {
                tlfinal = close.fillClose(tl, indCloseList, sl, closeFilter);
            }
        }
        setTradeShare(tlfinal, sl.getShare());
        return tlfinal;
    }

    public ArrayList<String> getRanksOn(ShareList sl, Date d, int days, int topShares, boolean sell) throws Exception {
        ArrayList<String> arr = null;
        if(shDayList.get(sl.getShare())==null){
            THashMap<Boolean,THashMap<Date, ArrayList<String>>> shs=new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
            shs.put(sell,new THashMap<Date, ArrayList<String>>(5000));
            shDayList.put(sl.getShare(),shs);
        }
        if (shDayList.get(sl.getShare()).get(sell) == null) {
            shDayList.get(sl.getShare()).put(sell,new THashMap<Date, ArrayList<String>>(5000));
        }
        if (shDayList.get(sl.getShare()).get(sell).containsKey(d)) {
            arr = shDayList.get(sl.getShare()).get(sell).get(d);
        }
        //arr=null;
        if (arr == null || arr.size()==0) {
            try {
                arr = shareDB.getShareOnDateExclude(sl, d, days, topShares + 20, sell);
            } catch (SQLException exSql) {
                shareDB.close();
                shareDB = new ShareListDB();
                arr = shareDB.getShareOnDateExclude(sl, d, days, topShares + 20, sell);

            } catch (Exception exSql) {
                shareDB.close();
                shareDB = new ShareListDB();
                arr = shareDB.getShareOnDateExclude(sl, d, days, topShares + 20, sell);

            }
            //arr=shareDB.getShareOnDate(sl, trd.getStartDate(),days,topShares,sell);
//                if(shDayList.size()>4000)
//                    System.out.println(shDayList.size());
            shDayList.get(sl.getShare()).get(sell).put(d, arr);
        }
        return arr;
    }

    public ArrayList<String> getRanksOn(ShareList sl, Date d, TradeParameters tp) throws Exception {
        ArrayList<String> arrOrig = null;
        ArrayList<String> ret = new ArrayList<String>();
        boolean sell=!tp.isBuy();
        boolean selectSell=sell;
        int days=tp.getBackPeriod();
        //int days=150;
        int topShares=tp.getShareCount();
        boolean div = true;
        int res = tp.getResult();
        int sector=0;//tp.getSector();
        int shortdays=tp.getShortDays();
        int shortDaysRank=tp.getShortDaysRank();
//        IndicatorList il=indList.get(sl.getShare()+"RSI12");
//        if (il == null) {
//            RsiIndicator ukrsi = new RsiIndicator();
//            HashMap hm=new HashMap();
//            hm.put(1, 12);
//            hm.put(2, 1);
//            ukrsi.init(hm);
//            il=ukrsi.buildIndicator(sl);
//            indList.put(sl.getShare()+"RSI12",il);
//        }
//        IndicatorField idf=il.getSharedata(il.isDatePresent(d));
//        if(idf.getValue()>60 && idf.getValue()<100){
//            selectSell=!sell;
//        }
        HashMap<Integer,ArrayList<String>> sectors=new HashMap<Integer, ArrayList<String>>();
        if(shDayList.get(sl.getShare())==null){
            THashMap<Boolean,THashMap<Date, ArrayList<String>>> shs=new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
            shs.put(selectSell,new THashMap<Date, ArrayList<String>>(5000));
            shDayList.put(sl.getShare(),shs);
        }
        if (shDayList.get(sl.getShare()).get(selectSell) == null) {
            shDayList.get(sl.getShare()).put(selectSell,new THashMap<Date, ArrayList<String>>(5000));
        }
        if (shDayList.get(sl.getShare()).get(selectSell).containsKey(d)) {
            arrOrig = shDayList.get(sl.getShare()).get(selectSell).get(d);
        }
        arrOrig=null;
        if (arrOrig == null || arrOrig.isEmpty()) {
            try {
                arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100, selectSell);
            } catch (SQLException exSql) {
                shareDB.close();
                shareDB = new ShareListDB();
                arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100, selectSell);

            } catch (Exception exSql) {
                shareDB.close();
                shareDB = new ShareListDB();
                arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100, selectSell);

            }
            //arr=shareDB.getShareOnDate(sl, trd.getStartDate(),days,topShares,sell);
//                if(shDayList.size()>4000)
//                    System.out.println(shDayList.size());
            //shDayList.get(sl.getShare()).get(selectSell).put(d, arrOrig);
        }
        for(String sh:arrOrig){
            ShareList sl1=getSL(sh,sl.getShare());
            int st=sl1.isDatePresent(d);
            if (div && isDividend(sl1, d, sell, sl.getShare())) {
                continue;
            }
            if (shortdays > 0) {
                //index back
//                    int index=arrShort.indexOf(sh);
//                    if(index>-1 && index<shortDaysRank){
//                        System.out.println(sh+","+index);
//                        continue;
//                    }
                //perc back
                if (st == -1 || st < shortdays) {
                    continue;
                }
                double cl = sl1.getSharedata(st).getClosePrice();
                double op = sl1.getSharedata(st - shortdays).getClosePrice();
                double perc = (cl - op) * 100.0 / op;
                if ((sell && perc >= shortDaysRank) || (!sell && perc <= shortDaysRank * -1)) {
                    //System.out.println(sh + "," + perc);
                    continue;
                }
            }
            if (sector > 0) {
                Integer secid = sectorList.get(sl.getShare()).getSectorId(sh);
                ArrayList<String> sList;
                if (secid != null) {
                    sList = sectors.get(secid);
                    if (sList == null) {
                        sList = new ArrayList<String>();
                    }
                    if (sList.size() == sector) {
                        continue;
                    } else {
                        sList.add(sh);
                        sectors.put(secid, sList);
                    }
                }
            }
            ret.add(sh);
        }
        if(ret.size()<topShares)
            System.out.println(d+","+ret.size());
        return ret;
    }

    public ArrayList<String> getRanksOn(ShareList sl, Date d, TradeParameters tp,int type) throws Exception {
        ArrayList<String> arrOrig = null;
        ArrayList<String> ret = new ArrayList<String>();
        boolean sell=!tp.isBuy();
        boolean selectSell=sell;
        //int days=tp.getBackPeriod();
        int days=25;
        int shcnt=40;
        int topShares=tp.getShareCount();
        boolean div = true;
        int res = tp.getResult();
        int sector=0;
        int shortdays=tp.getShortDays();
        int shortDaysRank=tp.getShortDaysRank();
//        IndicatorList il=indList.get(sl.getShare()+"RSI12");
//        if (il == null) {
//            RsiIndicator ukrsi = new RsiIndicator();
//            HashMap hm=new HashMap();
//            hm.put(1, 12);
//            hm.put(2, 1);
//            ukrsi.init(hm);
//            il=ukrsi.buildIndicator(sl);
//            indList.put(sl.getShare()+"RSI12",il);
//        }
//        IndicatorField idf=il.getSharedata(il.isDatePresent(d));
//        if(idf.getValue()>60 && idf.getValue()<100){
//            selectSell=!sell;
//        }
        HashMap<Integer,ArrayList<String>> sectors=new HashMap<Integer, ArrayList<String>>();
        if(shDayList.get(sl.getShare())==null){
            THashMap<Boolean,THashMap<Date, ArrayList<String>>> shs=new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
            shs.put(selectSell,new THashMap<Date, ArrayList<String>>(5000));
            shDayList.put(sl.getShare(),shs);
        }
        if (shDayList.get(sl.getShare()).get(selectSell) == null) {
            shDayList.get(sl.getShare()).put(selectSell,new THashMap<Date, ArrayList<String>>(5000));
        }
        if (shDayList.get(sl.getShare()).get(selectSell).containsKey(d)) {
            arrOrig = shDayList.get(sl.getShare()).get(selectSell).get(d);
        }
        arrOrig=null;
        if (arrOrig == null || arrOrig.isEmpty()) {
            try {
                //arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100, selectSell,diff);
                arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100, 0, selectSell);
            } catch (SQLException exSql) {
                shareDB.close();
                shareDB = new ShareListDB();
                arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100, 0, selectSell);

            } catch (Exception exSql) {
                shareDB.close();
                shareDB = new ShareListDB();
                arrOrig = shareDB.getShareOnDateExclude(sl, d, days, 100,0, selectSell);

            }
            //arr=shareDB.getShareOnDate(sl, trd.getStartDate(),days,topShares,sell);
//                if(shDayList.size()>4000)
//                    System.out.println(shDayList.size());
            //shDayList.get(sl.getShare()).get(selectSell).put(d, arrOrig);
        }
        ArrayList<String> arrTemp=new ArrayList<String>(topShares);
        if(type==1){
            MaIndicator ind=new MaIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 250);
            hm.put(2, ma.MA.Simple);
            ind.init(hm);
            //arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arrOrig){
                IndicatorList il=tc.getIndicatorList(s+ind.toString());
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s+ind.toString(), il);
                }
                arrTemp.add(s);
                IndicatorField indf=il.getSharedata(il.isDatePresent(d));
                arVal.put(s,(indf.getValue()-indf.getSignal())*100/indf.getValue());
                if(arrTemp.size()>=shcnt)
                    break;
            }
            Collections.sort(arrTemp, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==2){
            MaIndicator ind=new MaIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 20);
            hm.put(2, ma.MA.Simple);
            ind.init(hm);
            //arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arrOrig){
                IndicatorList il=tc.getIndicatorList(s+ind.toString());
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s+ind.toString(), il);
                }

                int indx=il.isDatePresent(d);
                int indx1=indx-265;
                if(indx<265)
                    continue;
                    //indx1=0;
                arrTemp.add(s);
                IndicatorField indf=il.getSharedata(indx);
                IndicatorField indf1=il.getSharedata(indx1);
                arVal.put(s,(indf.getSignal()-indf1.getSignal())*100/indf1.getSignal());
                if(arrTemp.size()>=shcnt)
                    break;
            }
            Collections.sort(arrTemp, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        }
        else if(type==3){
            BetaIndicator ind=new BetaIndicator();
            HashMap hm=new HashMap();
            hm.put(1,20);
            ind.init(hm);
            //arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arrOrig){
                IndicatorList il=tc.getIndicatorList(s+ind.toString());
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()), sl);
                    tc.putIndicatorList(s+ind.toString(), il);
                }
                arrTemp.add(s);
                arVal.put(s,il.getSharedata(il.isDatePresent(d)).getValue());
                if(arrTemp.size()>=shcnt)
                    break;
            }
            Collections.sort(arrTemp, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==4){
            RsiIndicator ind=new RsiIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 20);
            hm.put(2, 1);
            ind.init(hm);
            //arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arrOrig){
                IndicatorList il=tc.getIndicatorList(s+ind.toString());
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s+ind.toString(), il);
                }
                arrTemp.add(s);
                arVal.put(s,il.getSharedata(il.isDatePresent(d)).getValue());
                if(arrTemp.size()>=shcnt)
                    break;
            }
            Collections.sort(arrTemp, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==5){
            MaIndicator ind=new MaIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 275);
            hm.put(2, ma.MA.Exponential);
            ind.init(hm);
            //arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arrOrig){
                IndicatorList il=tc.getIndicatorList(s+ind.toString());
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s+ind.toString(), il);
                }
                arrTemp.add(s);
                IndicatorField indf=il.getSharedata(il.isDatePresent(d));
                arVal.put(s,(indf.getValue()-indf.getSignal())*100/indf.getValue());
                if(arrTemp.size()>=shcnt)
                    break;
            }
            Collections.sort(arrTemp, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o2), arVal.get(o1));
                }
            });
        } else if(type==6){
            MacdIndicator ind=new MacdIndicator();
            HashMap hm=new HashMap();
            hm.put(1, 13);
            hm.put(2, 26);
            hm.put(3, 9);
            ind.init(hm);
            //arr=new ArrayList<String>(shareCount);
            TradeCalculator tc=TradeCalculator.getInstance();
            final HashMap<String,Double> arVal=new HashMap<String, Double>();
            for(String s:arrOrig){
                IndicatorList il=tc.getIndicatorList(s+ind.toString());
                if(il==null){
                    il=ind.buildIndicator(tc.getSL(s,sl.getShare()));
                    tc.putIndicatorList(s+ind.toString(), il);
                }
                arrTemp.add(s);
                IndicatorField indf=il.getSharedata(il.isDatePresent(d));
                arVal.put(s,(indf.getValue()-indf.getSignal())*100/indf.getValue());
            }
            Collections.sort(arrTemp, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(arVal.get(o1), arVal.get(o2));
                }
            });
        } else{
            arrTemp.addAll(arrOrig);
        }
        for(String sh:arrTemp){
            ShareList sl1=getSL(sh,sl.getShare());
            int st=sl1.isDatePresent(d);
            if (div && isDividend(sl1, d, sell, sl.getShare())) {
                continue;
            }
            if (shortdays > 0) {
                //index back
//                    int index=arrShort.indexOf(sh);
//                    if(index>-1 && index<shortDaysRank){
//                        System.out.println(sh+","+index);
//                        continue;
//                    }
                //perc back
                if (st == -1 || st < shortdays) {
                    continue;
                }
                double cl = sl1.getSharedata(st).getClosePrice();
                double op = sl1.getSharedata(st - shortdays).getClosePrice();
                double perc = (cl - op) * 100.0 / op;
                if ((sell && perc >= shortDaysRank) || (!sell && perc <= shortDaysRank * -1)) {
                    //System.out.println(sh + "," + perc);
                    continue;
                }
            }
            if (sector > 0) {
                Integer secid = sectorList.get(sl.getShare()).getSectorId(sh);
                ArrayList<String> sList;
                if (secid != null) {
                    sList = sectors.get(secid);
                    if (sList == null) {
                        sList = new ArrayList<String>();
                    }
                    if (sList.size() == sector) {
                        continue;
                    } else {
                        sList.add(sh);
                        sectors.put(secid, sList);
                    }
                }
            }
            ret.add(sh);
        }
        if(ret.size()<topShares)
            System.out.println(d+","+ret.size());
        return ret;
    }


    public TradeList getTrades(TradeList tl, ShareList sl, HashMap top, boolean sell)
            throws Exception {
        TradeList trdlist = new TradeList();
        int days = 0, topShares = 0, topStart = 0, max = 6, topCap =0;
        boolean div = false;
        int res = 0;
        int sector=0;
        IndicatorList il = null;
        AbstractIndicator ind = null;
        ArrayList<String> arr = null;
        ArrayList<String> arrShort = null;
        Double ilValue = 0.0;
        Double ilValueHigh = 30.0;
        int bckPrd = 5;
        boolean rsiInd=false;
        int shortdays=0,shortDaysRank=0;
        if (top.containsKey("topShares")) {
            days = (Integer) top.get("shareDays");
            topShares = (Integer) top.get("topShares");
            if (top.containsKey("isDiv")) {
                div = (Boolean) top.get("isDiv");
            }
            if (top.containsKey("isRsiInd")) {
                rsiInd = (Boolean) top.get("isRsiInd");
            }
            if (top.containsKey("ShortDays")) {
                shortdays = (Integer) top.get("ShortDays");
            }
            if (top.containsKey("ShortDaysRank")) {
                shortDaysRank = (Integer) top.get("ShortDaysRank");
            }
            if (top.containsKey("isRes")) {
                res = (Integer) top.get("isRes");
            }
            if (top.containsKey("topSharesStart")) {
                topStart = (Integer) top.get("topSharesStart");
            }
            if (top.containsKey("IList")) {
                ind = (AbstractIndicator) top.get("IList");
            }
            //il = ((AbstractIndicator) top.get("IList")).buildIndicator(sl);
            if (top.containsKey("IValue")) {
                ilValue = (Double) top.get("IValue");
            }
            if (top.containsKey("topCaps")) {
                topCap = (Integer) top.get("topCaps");
            }
            if (top.containsKey("IValueHigh")) {
                ilValueHigh = (Double) top.get("IValueHigh");
            }
            if (top.containsKey("IDays")) {
                bckPrd = (Integer) top.get("IDays");
            }
            if (top.containsKey("Sector")) {
                sector = (Integer) top.get("Sector");
            }
        }
        if(sector>0 && sectorList.get(sl.getShare()) == null){
            cacheSector("Sector",sl.getShare());
        }
        boolean selectSell =sell;
        IndicatorList ukil=indList.get(sl.getShare()+"RSI12");
        if (rsiInd) {
            if (ukil == null) {
                RsiIndicator ukrsi = new RsiIndicator();
                HashMap hm = new HashMap();
                hm.put(1, 12);
                hm.put(2, 1);
                ukrsi.init(hm);
                ukil = ukrsi.buildIndicator(sl);
                indList.put(sl.getShare() + "RSI12", ukil);
            }
        }
        int maxpos=0;
        for (int i = 0; i < tl.getSize(); i++) {
            Trade trd = tl.getTrade(i);
            arr = null;
            if (rsiInd) {
                if (ukil.isDatePresent(trd.getStartDate()) > 0) {
                    IndicatorField idf = ukil.getSharedata(ukil.isDatePresent(trd.getStartDate()));
                    IndicatorField idfy = ukil.getSharedata(ukil.isDatePresent(trd.getStartDate()) - 1);
                    if (idf.getValue() > 50 && idfy.getValue() < idf.getValue() && idfy.getValue() < 50) {
                        //if (idf.getValue() > 57 && idf.getValue() < 64 && idfy.getValue() < idf.getValue()) {
                        selectSell = !sell;
                    } else {
                        selectSell = sell;
                    }
                }
            }
            if (shDayList == null) {
                shDayList = new THashMap<String, THashMap<Boolean, THashMap<Date, ArrayList<String>>>>(2);
            }
            if (shDayList.get(sl.getShare()) == null) {
                THashMap<Boolean, THashMap<Date, ArrayList<String>>> shs = new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
                shs.put(selectSell, new THashMap<Date, ArrayList<String>>(5000));
                shDayList.put(sl.getShare(), shs);
            }
            if (shDayList.get(sl.getShare()).get(selectSell) == null) {
                shDayList.get(sl.getShare()).put(selectSell, new THashMap<Date, ArrayList<String>>(5000));
            }
            if (shDayList.get(sl.getShare()).get(selectSell).containsKey(trd.getStartDate())) {
                arr = shDayList.get(sl.getShare()).get(selectSell).get(trd.getStartDate());
            }
            int start=topStart;
//            if(il!=null)
//            {
//                int ind=il.isDatePresent(trd.getStartDate());
//                double val=il.getSharedata(ind).getValue();
//                if(val>=ilValue)
//                    start=topStart;
//            }
//            else start=topStart;
            if (arr == null) {
                int buffer=50;
                if(sl.getShare().equals("GSPC"))
                    buffer=250;
                if(shareDB==null)
                    shareDB=new ShareListDB();
                try {
                    arr = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), days, start + topShares + buffer, topCap,selectSell);
                } catch (Exception exSql) {
                    shareDB.close();
                    shareDB = new ShareListDB();
                    arr = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), days, start + topShares + buffer, topCap, selectSell);

                }
                //arr=shareDB.getShareOnDate(sl, trd.getStartDate(),days,topShares,sell);
//                if(shDayList.size()>4000)
//                    System.out.println(shDayList.size());
                try {
                    THashMap<Boolean, THashMap<Date, ArrayList<String>>> shs = shDayList.get(sl.getShare());
                    if(shs==null){
                        shs=new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
                        shDayList.put(sl.getShare(),shs);
                    }
                    THashMap<Date, ArrayList<String>> shsB =shs.get(sell);
                    if(shsB==null){
                        shsB=new THashMap<Date, ArrayList<String>>(5000);
                        shs.put(sell,shsB);
                    }
                    shsB.put(trd.getStartDate(), arr);
                } catch (Exception ex) {
                    Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            int type=0;
            int shcnt=30;
            ArrayList<String> arrTemp=new ArrayList<String>(topShares);
            if (type == 1) {

                MaIndicator indt = new MaIndicator();
                HashMap hm = new HashMap();
                hm.put(1, 250);
                hm.put(2, ma.MA.Simple);
                indt.init(hm);
                //arr=new ArrayList<String>(shareCount);
                TradeCalculator tc = TradeCalculator.getInstance();
                final HashMap<String, Double> arVal = new HashMap<String, Double>();
                for (String s : arr) {
                    IndicatorList ilt = tc.getIndicatorList(s + indt.toString());
                    if (ilt == null) {
                        ilt = indt.buildIndicator(tc.getSL(s, sl.getShare()));
                        tc.putIndicatorList(s + indt.toString(), ilt);
                    }
                    arrTemp.add(s);
                    IndicatorField indf = ilt.getSharedata(ilt.isDatePresent(trd.getStartDate()));
                    arVal.put(s, (indf.getValue() - indf.getSignal()) * 100 / indf.getValue());
                    if (arrTemp.size() >= shcnt) {
                        break;
                    }
                }
                Collections.sort(arrTemp, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        return Double.compare(arVal.get(o2), arVal.get(o1));
                    }
                });
                arr=arrTemp;
            } else if (type == 2) {
                MaIndicator indt = new MaIndicator();
                HashMap hm = new HashMap();
                hm.put(1, 20);
                hm.put(2, ma.MA.Simple);
                indt.init(hm);
                //arr=new ArrayList<String>(shareCount);
                TradeCalculator tc = TradeCalculator.getInstance();
                final HashMap<String, Double> arVal = new HashMap<String, Double>();
                for (String s : arr) {
                    IndicatorList ilt = tc.getIndicatorList(s + indt.toString());
                    if (ilt == null) {
                        ilt = indt.buildIndicator(tc.getSL(s, sl.getShare()));
                        tc.putIndicatorList(s + indt.toString(), ilt);
                    }

                    int indx = ilt.isDatePresent(trd.getStartDate());
                    int indx1 = indx - 215;
                    if (indx < 215) {
                        continue;
                    }
                    //indx1=0;
                    arrTemp.add(s);
                    IndicatorField indf = ilt.getSharedata(indx);
                    IndicatorField indf1 = ilt.getSharedata(indx1);
                    arVal.put(s, (indf.getSignal() - indf1.getSignal()) * 100 / indf1.getSignal());
                    if (arrTemp.size() >= shcnt) {
                        break;
                    }
                }
                Collections.sort(arrTemp, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        return Double.compare(arVal.get(o2), arVal.get(o1));
                    }
                });
                arr=arrTemp;
            } else if (type == 3) {
                BetaIndicator indt = new BetaIndicator();
                indt.init();
                //arr=new ArrayList<String>(shareCount);
                TradeCalculator tc = TradeCalculator.getInstance();
                final HashMap<String, Double> arVal = new HashMap<String, Double>();
                for (String s : arr) {
                    IndicatorList ilt = tc.getIndicatorList(s + indt.toString());
                    if (ilt == null) {
                        ilt = indt.buildIndicator(tc.getSL(s, sl.getShare()), sl);
                        tc.putIndicatorList(s + indt.toString(), ilt);
                    }
                    arrTemp.add(s);
                    arVal.put(s, ilt.getSharedata(ilt.isDatePresent(trd.getStartDate())).getValue());
                    if (arrTemp.size() >= shcnt) {
                        break;
                    }
                }
                Collections.sort(arrTemp, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        return Double.compare(arVal.get(o2), arVal.get(o1));
                    }
                });
                arr=arrTemp;
            } else if (type == 4) {
                RsiIndicator indt = new RsiIndicator();
                HashMap hm = new HashMap();
                hm.put(1, 14);
                hm.put(2, 1);
                indt.init(hm);
                //arr=new ArrayList<String>(shareCount);
                
                //final HashMap<String, Double> arVal = new HashMap<String, Double>();
                for (String s : arr) {
                    IndicatorList ilt = getIndicatorList(s + indt.toString());
                    if (ilt == null) {
                        ilt = indt.buildIndicator(getSL(s, sl.getShare()));
                        putIndicatorList(s + indt.toString(), ilt);
                    }
                    //arrTemp.add(s);
                    //arVal.put(s, ilt.getSharedata(ilt.isDatePresent(trd.getStartDate())).getValue());
                    //if (arrTemp.size() >= shcnt) {
                    //    break;
                    //}
                }
//                Collections.sort(arrTemp, new Comparator<String>() {
//
//                    @Override
//                    public int compare(String o1, String o2) {
//                        return Double.compare(arVal.get(o2), arVal.get(o1));
//                    }
//                });
//                arr=arrTemp;
            } else if (type == 5) {
                MaIndicator indt = new MaIndicator();
                HashMap hm = new HashMap();
                hm.put(1, 275);
                hm.put(2, ma.MA.Exponential);
                indt.init(hm);
                //arr=new ArrayList<String>(shareCount);
                TradeCalculator tc = TradeCalculator.getInstance();
                final HashMap<String, Double> arVal = new HashMap<String, Double>();
                for (String s : arr) {
                    IndicatorList ilt = tc.getIndicatorList(s + indt.toString());
                    if (ilt == null) {
                        ilt = indt.buildIndicator(tc.getSL(s, sl.getShare()));
                        tc.putIndicatorList(s + indt.toString(), ilt);
                    }
                    arrTemp.add(s);
                    IndicatorField indf = ilt.getSharedata(ilt.isDatePresent(trd.getStartDate()));
                    arVal.put(s, (indf.getValue() - indf.getSignal()) * 100 / indf.getValue());
                    if (arrTemp.size() >= shcnt) {
                        break;
                    }
                }
                Collections.sort(arrTemp, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        return Double.compare(arVal.get(o2), arVal.get(o1));
                    }
                });
                arr=arrTemp;
            } else if (type == 6) {
                MacdIndicator indt = new MacdIndicator();
                HashMap hm = new HashMap();
                hm.put(1, 13);
                hm.put(2, 26);
                hm.put(3, 9);
                indt.init(hm);
                //arr=new ArrayList<String>(shareCount);
                TradeCalculator tc = TradeCalculator.getInstance();
                final HashMap<String, Double> arVal = new HashMap<String, Double>();
                for (String s : arr) {
                    IndicatorList ilt = tc.getIndicatorList(s + indt.toString());
                    if (ilt == null) {
                        ilt = indt.buildIndicator(tc.getSL(s, sl.getShare()));
                        tc.putIndicatorList(s + indt.toString(), ilt);
                    }
                    arrTemp.add(s);
                    IndicatorField indf = ilt.getSharedata(ilt.isDatePresent(trd.getStartDate()));
                    arVal.put(s, (indf.getValue() - indf.getSignal()) * 100 / indf.getValue());
                }
                Collections.sort(arrTemp, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        return Double.compare(arVal.get(o1), arVal.get(o2));
                    }
                });
                arr=arrTemp;
            }

            if(shortdays>0){
//                if(shareDB==null)
//                    shareDB=new ShareListDB();
//                try {
//
//                    arrShort = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), shortdays, start + topShares + 50, topCap,!sell);
//                } catch (Exception exSql) {
//                    shareDB.close();
//                    shareDB = new ShareListDB();
//                    arrShort = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), shortdays, start + topShares + 50, topCap,!sell);
//
//                }
            }
            int count = 0;
            //int end=topShares;
            
            HashMap<Integer,ArrayList<String>> sectors=new HashMap<Integer, ArrayList<String>>();
            ArrayList<Trade> adds=new ArrayList<Trade>();
            int j=0;
            for (j = start; count < topShares && j < arr.size(); j++)
            //for(int j=start;j<start+topShares && j<arr.size();j++)
            {
                String sh = arr.get(j);
                ShareList sl1 = getSL(sh, sl.getShare());
                if (ind != null) {
                    il=getIndicatorList(sh+ind.toString());
                    if(il==null)
                    {
                        il=ind.buildIndicator(sl1);
                        if(indList.size()>1000){
                            indList.clear();
                            Runtime.getRuntime().gc();
                        }
                        putIndicatorList(sh+ind.toString(), il);

                    }
                    int indx = il.isDatePresent(trd.getStartDate());
                    if(indx==-1)
                        continue;
                    double val = il.getSharedata(indx).getValue();
                    if (ind instanceof  RsiIndicator && val <= ilValue) {
                        continue;
                        //start = topStart;
                    }else if (ind instanceof  RsiIndicator){
                    //if (ind instanceof RsiIndicator && val <= ilValueHigh) {
                        double min = val;
                        for (int rs = indx - 1; rs >= indx - bckPrd && rs >= 0; rs--) {
                            double val1 = il.getSharedata(rs).getValue();
                            if (val1 < min) {
                                min = val1;
                            }
                        }
                        if (min <= ilValue) {
                            continue;
                        }
                        //start = topStart;
                    } else if(ind instanceof MaIndicator){
                        if(!sell && val<il.getSharedata(indx).getSignal()){
                            continue;
                        }
                    }
                }

                int st = sl1.isDatePresent(trd.getStartDate());
                int end = sl1.isDatePresent(trd.getCloseDate());
                if (end == -1) {
                    end = sl1.isLowerDatePresent(trd.getCloseDate());
                }
                if (div && isDividend(sl1, trd.getStartDate(), sell,sl.getShare())) {
                    continue;
                }

                if(shortdays>0 && sell){
                    //index back
//                    int index=arrShort.indexOf(sh);
//                    if(index>-1 && index<shortDaysRank){
//                        System.out.println(sh+","+index);
//                        continue;
//                    }
                    
//                    MaIndicator ema=new MaIndicator();
//                    {
//                        HashMap hm=new HashMap();
//                        hm.put(1, shortdays);
//                        ema.init(hm,MA.Exponential);
//                    }
//                    IndicatorList il1=getIndicatorList(sl1.getShare()+ema.getShortName());
//                    if(il1==null)
//                    {
//                        il1=ema.buildIndicator(sl1);
//                        putIndicatorList(sl1.getShare()+ema.getShortName(), il1);
//                    }
//                    int st1=il1.isDatePresent(trd.getStartDate());
//                    IndicatorField indf=il1.getSharedata(st1);
//                    double per=indf.getSignal()-(indf.getSignal()*shortDaysRank*0.01);
//                    if(indf.getValue()<=per){
//                        //System.out.println(sl1.getShare()+","+indf.getDDate()+","+indf.getValue()+","+indf.getSignal()+","+j);
//                        continue;
//                    }
                    
                    //perc back
                    
                    if(st==-1 || st<shortdays)
                        continue;
                    double cl=sl1.getSharedata(st).getClosePrice();
                    double op=sl1.getSharedata(st-shortdays).getClosePrice();
                    double perc=(cl-op)*100.0/op;
                    int slI=sl.isDatePresent(trd.getStartDate().getTime());
                    double clI=sl.getSharedata(slI).getClosePrice();
                    double opI=sl.getSharedata(slI-shortdays).getClosePrice();
                    double percI=(clI-opI)*100.0/opI;
                    double p=perc-percI;
                    if((sell && p>=shortDaysRank) || (!sell && p<=shortDaysRank*-1)){
                        //System.out.println(sh+","+perc);
                        continue;
                    }
                }
                if(sector>0){
                    Integer secid=sectorList.get(sl.getShare()).getSectorId(sh);
                    ArrayList<String> sList;
                    if(secid!=null){
                        sList=sectors.get(secid);
                        if(sList==null)
                            sList=new ArrayList<String>();
                        if(sList.size()==sector)
                            continue;
                        else{
                            sList.add(sh);
                            sectors.put(secid, sList);
                        }
                    }
                }
                if (sl.getShare().equals("GSPC1") && trd.getStartDate().after(dd) && !isLiquid(sl1, trd.getStartDate())) {
                    continue;
                }
//                if(trd.getStartDate().before(dd))
//                    continue;
                if (st == -1) {
                    System.out.println("err," + sh + "," + sdf.format(trd.getStartDate()));
                    continue;
                }

                if (st >= 0 && end >= 0) {
                    //if(st+1<sl1.getSize())
                    //    st+=1;
//                    if(end+1<sl1.getSize())
//                        end+=1;
                    ShareData sdstart = sl1.getSharedata(st);
//                    if(startNtrade!=null){
//                        if(sl1.isDatePresent(startNtrade)>-1)
//                            sdstart = sl1.getSharedata(sl1.isDatePresent(startNtrade));
//                        else
//                            sdstart = sl1.getSharedata(sl1.isLowerDatePresent(startNtrade));
//
//                        startNtrade=null;
//                    }
                    ShareData sdend = sl1.getSharedata(end);
                    //Trade newTrade = new Trade(trd.getStartDate(), sdstart.getClosePrice(), trd.getCloseDate(), sdend.getClosePrice());
                    Trade newTrade = new Trade(sdstart.getDate(), sdstart.getClosePrice(), sdend.getDate(), sdend.getClosePrice());
                    newTrade.setShare(sh);
                    if (res > 0) {
                        if (res == 1 && hasResult(sl1, trd.getStartDate(), 5, sl.getShare())) {
                            continue;
                        } else if (res >= 2) {
                            Trade trd1 = stopResultTrade(newTrade, sl.getShare());
                            if (trd1 != null) {
                                newTrade = trd1;
                                if (res == 3) {
                                    adds.add(trd1);
//                                    count--;
//                                    startNtrade=trd1.getCloseDate();
                                }
                            }
                        }
                    }
                    if(j>maxpos)
                        maxpos=j;
                    trdlist.addTrade(newTrade);
                    count++;
                }
            }
            if (adds.size() > 0) {
                int acnt=0;
                for (; acnt < adds.size() && j < arr.size(); j++) {
                    String sh = arr.get(j);
                    if(j>maxpos)
                        maxpos=j;
                    if(j>=30){
                        sh=sl.getShare();
                    }
                    ShareList sl1 = getSL(sh, sl.getShare());
                    Trade ntrade=adds.get(acnt);
                    acnt++;
                    if (ind != null) {
                        il = getIndicatorList(sh + ind.toString());
                        if (il == null) {
                            il = ind.buildIndicator(sl1);
                            if (indList.size() > 1000) {
                                indList.clear();
                            }
                            putIndicatorList(sh + ind.toString(), il);

                        }
                        int indx = il.isDatePresent(ntrade.getCloseDate());
                        if (indx == -1) {
                            continue;
                        }
                        double val = il.getSharedata(indx).getValue();
                        if (ind instanceof RsiIndicator && val <= ilValue) {
                            continue;
                        } else if (ind instanceof MaIndicator) {
                            if (!sell && val < il.getSharedata(indx).getSignal()) {
                                continue;
                            }
                        }
                    }

                    int st = sl1.isDatePresent(ntrade.getCloseDate());
                    int end = sl1.isDatePresent(trd.getCloseDate());
                    if (end == -1) {
                        end = sl1.isLowerDatePresent(trd.getCloseDate());
                    }
                    if (div && isDividend(sl1, ntrade.getCloseDate(), sell, sl.getShare())) {
                        continue;
                    }
                    if (sl.getShare().equals("GSPC1") && ntrade.getCloseDate().after(dd) && !isLiquid(sl1, trd.getStartDate())) {
                        continue;
                    }
                    if (st == -1) {
                        System.out.println("err," + sh + "," + sdf.format(trd.getStartDate()));
                        continue;
                    }
                    if (st >= 0 && end >= 0) {


                        ShareData sdstart = sl1.getSharedata(st);
                        ShareData sdend = sl1.getSharedata(end);
                        //Trade newTrade = new Trade(trd.getStartDate(), sdstart.getClosePrice(), trd.getCloseDate(), sdend.getClosePrice());
                        Trade newTrade = new Trade(trd.getStartDate(), sdstart.getClosePrice(), sdend.getDate(), sdend.getClosePrice());
                        //Trade newTrade = new Trade(trd.getStartDate(), sdstart.getClosePrice(), sdend.getDate(), sdend.getClosePrice());
                        newTrade.setShare(sh);
//                        if (res > 0) {
//                            if (res >= 1 && hasResult(sl1, trd.getStartDate(), 1, sl.getShare())) {
//                                continue;
//                            } else if (res >= 2) {
//                                Trade trd1 = stopResultTrade(newTrade, sl.getShare());
//                                if (trd1 != null) {
//                                    newTrade = trd1;
//                                    if (res == 3) {
//                                        adds.add(trd1);
//                                    }
//                                }
//                            }
//                        }
                        trdlist.addTrade(newTrade);
                        count++;
                    }
                }
            }


            int amt = count;
            if (count < topShares && arr.size() > 0) {

                //System.out.println(trd.getStartDate()+"No Shares Trade:"+count);

//                for(int k=count;k<=topShares;k++){
//                    Trade newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), trd.getCloseDate(), trd.getClosePrice());
//                    newTrade.setShare(trd.getShare());
//                    trdlist.addTrade(newTrade);
//                }

                if (amt < max) {
                    amt = max;
                }

//                for (int j = 0; j < trdlist.getSize(); j++) {
//                    Trade trdN = trdlist.getTrade(j);
//                    if (trdN.getStartDate().equals(trd.getStartDate()) && trdN.getCloseDate().equals(trd.getCloseDate())) {
//                        ///trdN = calcDividendTrade(trdN, (sell) ? 0 : 1, topShares);
//                        //System.out.println(trdN + "\t" + trdN.getProfit());
//                        trdN = calcDividendTrade(trdN, (sell) ? 0 : 1, amt,sl.getShare());
////                        if(amt<2)
////                            trdN.setProfit(trdN.getProfit()*10.0/5);
////                        else
////                            trdN.setProfit(trdN.getProfit()*(10.0/amt));
//                        //System.out.println(trdN + "\t" + trdN.getProfit());
//                    }
//                }
            }
            //double val=((10.0/amt>2)?5:amt);
            //System.out.println(trd.getStartDate() + "No Shares Trade:" + count * (10000.0 / amt));
        }

        //shDayList.clear();
        //System.out.println(maxpos);
        return trdlist;
    }

    public TradeList getTrades(TradeList tl, ShareList sl, THashMap top, boolean sell, AbstractIndicator indicator, double threshold)
            throws Exception {
        TradeList trdlist = new TradeList();
        int days = 0, topShares = 0;
        ArrayList<String> arr = null;
        if (top.containsKey("topShares")) {
            days = (Integer) top.get("shareDays");
            topShares = (Integer) top.get("topShares");
        }
        if (shDayList.get(sl.getShare()) == null) {
            THashMap<Boolean,THashMap<Date, ArrayList<String>>> shs=new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
            shs.put(sell,new THashMap<Date, ArrayList<String>>(5000));
            shDayList.put(sl.getShare(),shs);
        }
        if (shDayList.get(sl.getShare()).get(sell) == null) {
            shDayList.get(sl.getShare()).put(sell,new THashMap<Date, ArrayList<String>>(5000));
        }
        for (int i = 0; i < tl.getSize(); i++) {
            Trade trd = tl.getTrade(i);
            if (shDayList.get(sl.getShare()).get(sell).containsKey(trd.getStartDate())) {
                arr = shDayList.get(sl.getShare()).get(sell).get(trd.getStartDate());
            } else {
                try {

                    arr = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), days, topShares + 20, sell);
                } catch (SQLException exSql) {
                    shareDB.close();
                    shareDB = new ShareListDB();
                    arr = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), days, topShares + 20, sell);

                } catch (Exception exSql) {
                    shareDB.close();
                    shareDB = new ShareListDB();
                    arr = shareDB.getShareOnDateExclude(sl, trd.getStartDate(), days, topShares + 20, sell);

                }
                //arr=shareDB.getShareOnDate(sl, trd.getStartDate(),days,topShares,sell);
                shDayList.get(sl.getShare()).get(sell).put(trd.getStartDate(), arr);
            }
            int count = 0;
            ArrayList<String> sharesDone = new ArrayList<String>();
            for (int j = 0; count < topShares && j < arr.size(); j++) {

                String sh = (String) arr.get(j);
                ShareList sl1 = getSL(sh,sl.getShare());
                String indStr=sh + indicator.toString();
                IndicatorList il = null;
                il = indList.get(indStr);
                if(il==null && !live){
                    try{
                    //il=(IndicatorList) client.get(indStr);
                    if(il!=null)
                        indList.put(indStr, il);
                    }catch(Exception e){}
                }
                if(il==null) {
                    il = indicator.buildIndicator(sl1);
                    if (indList.size() > 250) {
                        indList.clear();
                    }
                    //indList=new HashMap<String, IndicatorList>();
                    indList.put(indStr, il);
                    if (!live) {
                        try {
                            //if(client.get(indStr)==null)
                                //client.add(indStr, exp, il);
                        } catch (Exception e) {
                        }
                    }
                }

                int ind = il.isDatePresent(trd.getStartDate());
                if (ind > -1) {
                    double val = il.getSharedata(ind).getValue();
                    if (val >= threshold) {
                        //System.out.println("Missed:" + sh);
                        continue;
                    } else {
                        sharesDone.add(sh);
                        //System.out.println("Not Missed," + sh + ","+val+","+trd.getStartDate());
                    }
                }
                int st = sl1.isDatePresent(trd.getStartDate());
                int end = sl1.isDatePresent(trd.getCloseDate());
                if (end == -1) {
                    end = sl1.isLowerDatePresent(trd.getCloseDate());
                }

                if (st >= 0 && end >= 0) {
//                    if(st+5<sl1.getSize())
//                        st+=5;
//                    if(end+5<sl1.getSize())
//                        end+=5;
                    ShareData sdstart = sl1.getSharedata(st);
                    ShareData sdend = sl1.getSharedata(end);
                    Trade newTrade = new Trade(sdstart.getDate(), sdstart.getClosePrice(), sdend.getDate(), sdend.getClosePrice());
                    newTrade.setShare(sh);
                    trdlist.addTrade(newTrade);
                    count++;
                }
            }
            if (count != topShares) {
                for (int j = 0; count < topShares && j < arr.size(); j++) {

                    String sh = (String) arr.get(j);
                    if (sharesDone.contains(sh)) {
                        continue;
                    }
                    ShareList sl1 = getSL(sh,sl.getShare());

                    int st = sl1.isDatePresent(trd.getStartDate());
                    int end = sl1.isDatePresent(trd.getCloseDate());
                    if (end == -1) {
                        end = sl1.isLowerDatePresent(trd.getCloseDate());
                    }

                    if (st >= 0 && end >= 0) {
                        //                    if(st+5<sl1.getSize())
                        //                        st+=5;
                        //                    if(end+5<sl1.getSize())
                        //                        end+=5;
                        ShareData sdstart = sl1.getSharedata(st);
                        ShareData sdend = sl1.getSharedata(end);
                        Trade newTrade = new Trade(sdstart.getDate(), sdstart.getClosePrice(), sdend.getDate(), sdend.getClosePrice());
                        newTrade.setShare(sh);
                        trdlist.addTrade(newTrade);
                        count++;
                    }
                }
                System.out.println("Error");
            }
        }
        //shDayList.clear();
        return trdlist;
    }

    @SuppressWarnings("unchecked")
    public TradeList getTradeList(TradeParameters tp) throws Exception {
        ShareList sl = tp.getSl();
        if (sl.getSize() == 0) {
            //if (shList.isKeyInCache(sl.getShare())) {
            sl=getSL(sl.getShare(),tp.getIndex());
            tp.setSl(sl);
        }
        ShareList closeSl = sl;
        ShareList indSL = getSL(tp.getIndex(),tp.getIndex());


        /*if(shList.containsKey(tp.getIndex()))
        {
        indSL=(ShareList) shList.get(tp.getIndex());
        }
        else
        {
        indSL=bsDB.getShareData(tp.getIndex());
        }*/
        HashMap top = new HashMap();
        if (tp.isTopShares()) {
            top.put("shareDays", tp.getBackPeriod());
            //top.put("shareDays", 300);
            top.put("isDiv", true);
            top.put("topShares", tp.getShareCount());
            top.put("topCaps", tp.getTopCap());
            top.put("topSharesStart", tp.getShareSkipCount());
            top.put("ShortDays", tp.getShortDays());
            top.put("ShortDaysRank", tp.getShortDaysRank());
            top.put("isRes", tp.getResult());
            top.put("Sector", tp.getSector());
            //top.put("isRsiInd", true);
            //tp.
//            MaIndicator ma = new MaIndicator();
//            HashMap hm = new HashMap();
//            hm.put(1, 50);
//            ma.init(hm,MA.Exponential);
            if (tp.getIndPeriod() > 0) {
                RsiIndicator rsi = new RsiIndicator();
                HashMap hm = new HashMap();
                hm.put(1, tp.getIndPeriod());
                hm.put(2, 1);
                rsi.init(hm);
                top.put("IList", rsi);
                top.put("IValue", tp.getIndValue());
                top.put("IValueHigh", tp.getIndValueHigh());
                top.put("IDays", tp.getIndDays());
            }
        }
        if (!sl.getShare().equals(tp.getCloseSL().getShare())) {
            if (tp.getCloseSL().getSize() == 0) {
                closeSl=getSL(tp.getCloseSL().getShare(),tp.getIndex());
                tp.setCloseSL(closeSl);
            } else {
                closeSl = tp.getCloseSL();
            }
        }
        String indStr=sl.getShare() + tp.getIndList().toString();
        IndicatorList il = indList.get(indStr);
        if (il == null && !live) {
            try {
                //il=(IndicatorList) client.get(indStr);
                indList.put(indStr, il);
            } catch (Exception e) {
            }
        }
        if (il == null) {
            if (indList.size() > 250) {
                indList.clear();
            }
            il = tp.getIndList().buildIndicator(sl);
            indList.put(indStr, il);
            try{
                //if(!live)
                    //client.add(indStr,exp, il);
            } catch (Exception e) {
            }
        }
        indStr=closeSl.getShare() + tp.getIndCloseList().toString();
        IndicatorList ilC = indList.get(indStr);
        if (il == null && !live) {
            try{
            //il=(IndicatorList) client.get(indStr);
            //indList.put(indStr, il);
            } catch (Exception e) {
            }
        }
        if (ilC == null) {
            if (indList.size() > 250) {
                indList.clear();
            }
            ilC = tp.getIndCloseList().buildIndicator(closeSl);
            indList.put(indStr, ilC);
            try {
                //if (!live) {
                //    client.add(indStr, exp, il);
                //}
            } catch(Exception ex){}
        }
        TradeList tlo=tp.getOpen().fillOpen(il,indSL,tp.getOpenFilter());
        TradeList tlc = getTrades(il, ilC, indSL, tp.getOpen(), tp.getOpenFilter(), tp.getClose(), null);
        if(tp.getOpenFilter()!=null)
            tp.getOpenFilter().clear();
        if (tp.getConsec() > 0) {
            tlo = filterSingle(tlo,tlc, tp.getConsec());
            tlc = filterSingle(tlc, tp.getConsec());
        }
        if (tlo != null) {
            if (tlo.getSize() - tlc.getSize() > tp.getConsec()) {
                tlo.removeTrade(tlo.getSize() - 1);
            }
            if (tlo.getSize() - tlc.getSize() == tp.getConsec()) {
                if (tlc.getTrade(tlc.getSize() - 1).getCloseDate().equals(indSL.getSharedata(indSL.getSize() - 1).getDate()) && tlo.getTrade(tlo.getSize() - 1).getStartDate().before(indSL.getSharedata(indSL.getSize() - 1).getDate())) {
                    //System.out.println(tlo.getTrade(tlo.getSize()-1)+ " removed");
                    tlo.removeTrade(tlo.getSize() - 1);
                }
                if (tlc.getTrade(tlc.getSize() - 1).getCloseDate().after(tlo.getTrade(tlo.getSize() - 1).getStartDate())) {
                    //System.out.println(tlo.getTrade(tlo.getSize()-1)+ " removed");
                    tlo.removeTrade(tlo.getSize() - 1);
                }
            }
            for (int i = 1; i <= tp.getConsec(); i++) {
                if (tlo.getSize() != tlc.getSize() && (tlo.getTrade(tlo.getSize() - i).getStartDate().after(tlc.getTrade(tlc.getSize() - 1).getStartDate()) || tlo.getTrade(tlo.getSize() - i).getStartDate().before(tlc.getTrade(tlc.getSize() - 1).getCloseDate()))) {
                    Trade trd = tlo.getTrade(tlo.getSize() - i);
                    trd.setCloseDate(Calendar.getInstance().getTime());
                    tlc.addTrade(trd);
                }
            }
        }
        if (tp.getIndStop() > 0) {
            tlc = stopLossTradeList(tlc, tp.getIndStop(), !tp.isBuy(),tp.getIndex(),false);
        }
        IndicatorList hedgIL=null;
        IndicatorList hedgIL2=null;
        double band=8.0;
        TradeList tlc2 = null;
        int hedgtype=0;
        if (hedgtype>1) {
            {
                MaIndicator hedgSwitch = new MaIndicator(MA.Exponential);
                HashMap hm = new HashMap();
                hm.put(1, 125);
                hedgSwitch.init(hm);
                hedgIL = hedgSwitch.buildIndicator(indSL);
            }
            {
                MaIndicator hedgSwitch = new MaIndicator(MA.Exponential);
                HashMap hm = new HashMap();
                hm.put(1, 250);
                hedgSwitch.init(hm);
                hedgIL2 = hedgSwitch.buildIndicator(indSL);
            }
        }

        if (tp.isTopShares()) {


            TradeList tlc1 = null;
//            if(indSL.getShare().equals("UKX") && !tp.isBuy())
//                tlc1 = getTrades(tlc, getSL("GSPC", "GSPC"), top, !tp.isBuy());
//            else
                tlc1 = getTrades(tlc, indSL, top, !tp.isBuy());

            if(hedgtype>0)
            {
                cutDays(tlc,8,indSL);
                tlc2 = getTrades(tlc, indSL, top, tp.isBuy());
            }
            //tlc=new TradeList();
            tlc=tlc1;
            //tlc.addTrade(tlc2);
        }
        if (tp.getShareStop() != 0) {
            //tlc = stopLossTradeList(tlc, tp.getShareStop(), !tp.isBuy(),tp.getIndex(),true);
            tlc = stopLossTradeList(tlc, tp.getShareStop(), !tp.isBuy(),tp.getIndex(),false);;
            if(tlc2!=null){
                tlc2 = stopLossTradeList(tlc2, tp.getShareStop(), tp.isBuy(),tp.getIndex(),false);
            }
        }

        int buy = 1;
        if (!tp.isBuy()) {
            buy = 0;
        }
        tlc = calcDividendTrades(tlc, buy, tp.getShareCount(),tp.getIndex());
        if(tlc2!=null){
            tlc2 = calcDividendTrades(tlc2, (buy==0)?1:0, tp.getShareCount(),tp.getIndex());
            if (hedgIL != null) {
//                for(int i=0;i<hedgIL.getSize();i++){
//                    IndicatorField idf=hedgIL.getSharedata(i);
//                    IndicatorField idfl=hedgIL2.getSharedata(i);
//                    System.out.println(sdf.format(idf.getDDate())+","+idf.getValue()+","+idf.getSignal()+","+idfl.getSignal());
//                }
                for (int i = 0; i < tlc2.getSize(); i++) {
                    Trade trd = tlc2.getTrade(i);
                    IndicatorField indf = hedgIL.getSharedata(hedgIL.isDatePresent(trd.getStartDate()));
                    IndicatorField indf2 = hedgIL2.getSharedata(hedgIL2.isDatePresent(trd.getStartDate()));
                    //double diff=(indf2.getSignal()-indf2.getValue())*100/indf2.getSignal();
//                    if(trd.getStartDate().after(sdf.parse("01/10/2008")) && trd.getStartDate().before(sdf.parse("01/08/2009"))){
//                        System.out.println(trd.getStartDate()+","+trd.getShare()+","+trd.getProfit());
//                    }
                    double diff=(indf2.getValue()-indf2.getSignal())*100/indf2.getValue();
                    if ((buy == 1 && indf2.getValue() > indf2.getSignal() && diff>=band)
                            || (buy == 0 && indf2.getValue() > indf2.getSignal()) || (buy == 0 && indf2.getValue() < indf2.getSignal() && diff<=band)) {
                        trd.setIsValid(false);
                        tlc.addTrade(trd);
                        System.out.println(/*sys+","+*/trd.getShare() + "," + sdf.format(trd.getStartDate()) + "," + sdf.format(trd.getCloseDate()) + "," + trd.getProfit());
                    } else{
//                        if(indf.getValue() < indf.getSignal() && diff>band)
//                            System.out.println(trd.getStartDate()+","+trd.getShare()+","+trd.getProfit());
                    }
                }
            } else
                tlc.addTrade(tlc2);
        }
        return tlc;
    }

    public TradeList indicatorStopTradeList(TradeList oldTL, IndividualClose iclose, boolean sell) throws Exception {
        TradeList tl = new TradeList();
        RsiIndicator rsi = new RsiIndicator();
        HashMap hm = new HashMap();
        hm.put(1, iclose.getParams().get(1));
        hm.put(2, 1);
        rsi.init(hm);
        for (int i = 0; i < oldTL.getSize(); i++) {
            Trade trd = oldTL.getTrade(i);
            Trade nTrd = indicatorStopTrade(trd, rsi, Double.parseDouble(iclose.getParams().get(2).toString()), sell, (Integer) iclose.getParams().get(3));
            tl.addTrade(nTrd);
        }
        return tl;
    }

    public TradeList stopProfitTradeList(TradeList oldTL, double stopProfit, boolean sell,String ind) throws Exception {
        TradeList tl = new TradeList();
        for (int i = 0; i < oldTL.getSize(); i++) {
            Trade trd = oldTL.getTrade(i);
            Trade nTrd = stopProfitTrade(trd, stopProfit, sell,ind);
            tl.addTrade(nTrd);
        }
        return tl;
    }

    public TradeList stopLossTradeList(TradeList oldTL, double stopLoss, boolean sell,String ind,boolean replace) throws Exception {
        TradeList tl = new TradeList();
        HashMap<Date,ArrayList<String>> shDone=new HashMap<Date, ArrayList<String>>();
        int start=10;
        for (int i = 0; i < oldTL.getSize(); i++) {
            Trade trd = oldTL.getTrade(i);

            Trade nTrd = stopLossTrade(trd, stopLoss, sell,ind);
            if(replace) {
                ArrayList<String> arrs = shDone.get(trd.getStartDate());
                if (arrs == null) {
                    arrs = new ArrayList<String>();
                    shDone.clear();
                    shDone.put(trd.getStartDate(), arrs);
                    start=10;
                }
                arrs.add(nTrd.getShare());
                if (trd.getCloseDate().after(nTrd.getCloseDate())) {

                    ArrayList<String> arr = null;
                    //ShareList indSL=getSL(ind,ind);
                    if (shDayList == null) {
                        shDayList = new THashMap<String, THashMap<Boolean, THashMap<Date, ArrayList<String>>>>(2);
                    }
                    if (shDayList.get(ind) == null) {
                        THashMap<Boolean, THashMap<Date, ArrayList<String>>> shs = new THashMap<Boolean, THashMap<Date, ArrayList<String>>>(2);
                        shs.put(sell, new THashMap<Date, ArrayList<String>>(5000));
                        shDayList.put(ind, shs);
                    }
                    if (shDayList.get(ind).get(sell) == null) {
                        shDayList.get(ind).put(sell, new THashMap<Date, ArrayList<String>>(5000));
                    }
                    if (shDayList.get(ind).get(sell).containsKey(trd.getStartDate())) {
                        arr = shDayList.get(ind).get(sell).get(trd.getStartDate());
                    }
                    if (arr == null) {
                        if (shareDB == null) {
                            shareDB = new ShareListDB();
                        }
                        try {
                            arr = shareDB.getShareOnDateExclude(getSL(ind,ind), trd.getStartDate(), 225, 10 + 50, 0, sell);
                        } catch (Exception exSql) {
                            shareDB.close();
                            shareDB = new ShareListDB();
                            arr = shareDB.getShareOnDateExclude(getSL(ind,ind), trd.getStartDate(), 225, 10 + 50, 0, sell);

                        }
                        try {
                            THashMap<Boolean, THashMap<Date, ArrayList<String>>> shs = shDayList.get(ind);
                            THashMap<Date, ArrayList<String>> shsB = shs.get(sell);
                            if (shsB == null) {
                                shsB = new THashMap<Date, ArrayList<String>>(5000);
                                shs.put(sell, shsB);
                            }
                            shsB.put(trd.getStartDate(), arr);
                        } catch (Exception ex) {
                            Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    String sh = ind;
                    ShareList sl =null;
                    for(int j=start;j<arr.size();j++){
                        if(arrs.contains(arr.get(j))){
                            continue;
                        }
                        sh=arr.get(j);
                        start=j+1;
                        sl = getSL(sh, ind);
                        if(sl.isDatePresent(nTrd.getCloseDate())==-1 || sl.isDatePresent(trd.getCloseDate())==-1)
                            continue;
                        break;
                    }
                    System.out.println(sh + "," + nTrd.getCloseDate());
//                    try {
//                        if (sh.equals("AIG") && nTrd.getStartDate().equals(sdf.parse("21/08/2008"))) {
//                            System.out.println();
//                        }
//                        if(sh.equals("AIG")){
//                            System.out.println();
//                        }
//                    } catch (Exception ex) {
//                    }
                    ShareData sdst=sl.getSharedata(sl.isDatePresent(nTrd.getCloseDate()));
                    ShareData sden=sl.getSharedata(sl.isDatePresent(trd.getCloseDate()));
                    Trade nxTrade = new Trade(sdst.getDate(), sdst.getClosePrice(), sden.getDate(), sden.getClosePrice());
                    nxTrade.setShare(sh);
                    Trade nxTrd = stopLossTrade(nxTrade, stopLoss, sell,ind);
                    nxTrd.setStartDate(nTrd.getStartDate());
                    tl.addTrade(nxTrd);
                }
            }
            tl.addTrade(nTrd);
        }
        return tl;
    }

    public TradeList stopLossTradeList(TradeList oldTL, double stopLoss, boolean sell,String ind) throws Exception {
        return stopLossTradeList(oldTL, stopLoss, sell, ind, false);
    }

    public Trade stopLossTrade(Trade trd, double stopLoss, boolean sell,String ind) throws Exception {
        String share = trd.getShare();
        ShareList tsl = getSL(share,ind);
//        if(shList.containsKey(share))
//        {
//            tsl=(ShareList) shList.get(share);
//        }
//        else
//        {
//            tsl=bsDB.getShareData(share);
//            shList.put(share, tsl);
//        }
        int start = tsl.isDatePresent(trd.getStartDate()) + 1;
        int end = tsl.isDatePresent(trd.getCloseDate());
        Trade newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), trd.getCloseDate(), trd.getClosePrice());

        double profit = ((trd.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
        if (sell) {
            profit = ((trd.getStartPrice() - trd.getClosePrice()) / trd.getStartPrice()) * 100;
        }
        if (start >= 1 && end >= 1 && end <= tsl.getSize() - 1) {
            for (int i = start; i <= end; i++) {
                ShareData sdend = tsl.getSharedata(i);
                double prof = ((sdend.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
                if (sell) {
                    prof = ((trd.getStartPrice() - sdend.getClosePrice()) / trd.getStartPrice()) * 100;
                }
                if (stopLoss > 0 &&  prof <= (stopLoss * -1)) {
                    newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                    newTrade.setProfit(prof);
                    profit = prof;
                    break;
                } else if (stopLoss < 0 &&  prof >= (stopLoss * -1)) {
                    newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                    newTrade.setProfit(prof);
                    profit = prof;
                    break;
                }

            }
        }
        newTrade.setShare(trd.getShare());
        if (!trd.isValid()) {
            profit = 0;
            newTrade.setIsValid(false);
        }
        //newTrade.setProfit(profit);
        return newTrade;
    }

    private Trade stopProfitTrade(Trade trd, double stopProfit, boolean sell,String ind) throws Exception {
        String share = trd.getShare();
        ShareList tsl = getSL(share,ind);
        int start = tsl.isDatePresent(trd.getStartDate()) + 1;
        int end = tsl.isDatePresent(trd.getCloseDate());
        Trade newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), trd.getCloseDate(), trd.getClosePrice());

        double profit = ((trd.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
        if (sell) {
            profit = ((trd.getStartPrice() - trd.getClosePrice()) / trd.getStartPrice()) * 100;
        }
        if (start >= 1 && end >= 1 && end <= tsl.getSize() - 1) {
            for (int i = start; i <= end; i++) {
                ShareData sdend = tsl.getSharedata(i);
                double prof = ((sdend.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
                if (sell) {
                    prof = ((trd.getStartPrice() - sdend.getClosePrice()) / trd.getStartPrice()) * 100;
                }
                if (prof >= stopProfit) {
                    newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                    profit = prof;
                    break;
                }

            }
        }
        newTrade.setShare(trd.getShare());
        if (!trd.isValid()) {
            profit = 0;
            newTrade.setIsValid(false);
        }
        newTrade.setProfit(profit);
        return newTrade;
    }

    private void setTradeShare(TradeList tlfinal, String share) {
        for (int i = 0; i < tlfinal.getSize(); i++) {
            Trade trd = tlfinal.getTrade(i);
            trd.setShare(share);
        }
    }

    public TreeMap<Date, Double> getIntraDayProfit(TradeList tl, int buy, int grps, ShareList indSL) throws Exception {
        TreeMap<Date, Double> dateprofit = new TreeMap<Date, Double>();
        Date startD = tl.openDate();
        Date clsD = tl.closeDate();
        String index=indSL.getShare();
        if(index.contains(","))
            index="CUR";
        //ArrayList<Date> days= TradingDays.tradingDays;
        if (clsD != null) {
            int startInd = indSL.isDatePresent(startD);
            int endInd = indSL.isDatePresent(clsD);
            //int startInd = days.indexOf(startD);
            //int endInd = days.indexOf(clsD);
            for (int j = startInd + 1; j <= endInd; j++) {
                for (int i = 0; i < tl.getSize(); i++) {
                    Trade trd = tl.getTrade(i);
                    ShareList tsl = getSL(trd.getShare().isEmpty()?tl.getInd():trd.getShare(),index);
                    int start = tsl.isDatePresent(trd.getStartDate());
                    Date currDate = indSL.getSharedata(j).getDate();//days.get(j);//
                    Date toEnd = currDate;
                    if (toEnd.after(trd.getCloseDate())) {
                        toEnd = trd.getCloseDate();
                    }
                    int end = tsl.isDatePresent(toEnd);
                    if (end == -1) {
                        end = tsl.isLowerDatePresent(toEnd);
                    }


                    if (start >= 1 && end >= 1)//&& end<tsl.getSize()-1)
                    {

                        ShareData sdend = tsl.getSharedata(end);

                        Trade trdTemp=new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                        trdTemp.setShare(trd.getShare());
                        //double prof=calcDividendTrade(trdTemp,buy,grps).getProfit();

                        Double prof = calcDividendTrade(trdTemp, sdend, buy, grps,indSL.getShare()).getProfit();

//                            if(toEnd.equals(sdf.parse("05/02/2010")))
//                                System.out.println(trd.getStartDate()+","+trd.getStartPrice()+","+sdend.getDate()+","+sdend.getClosePrice()+","+prof+","+trd.getShare());

                        Date actEnd = currDate;//indSL.getSharedata(j).getDate();

                        if (dateprofit.containsKey(actEnd)) {
                            dateprofit.put(actEnd, prof + dateprofit.get(actEnd));
                        } else {
                            dateprofit.put(actEnd, prof);
                        }

                    }
                }
            }
        }
        return dateprofit;
    }

    public TradeList calcDividendTrades(TradeList trdList, int buy, int grps,String index) throws Exception {
        TradeList nTl = new TradeList();
        for (int i = 0; i < trdList.getSize(); i++) {
            Trade trd = calcDividendTrade(trdList.getTrade(i), buy, grps,index);
            nTl.addTrade(trd);
        }
        trdList = null;
        return nTl;
    }

    public Trade calcDividendTrade(Trade oldTrd, ShareData sd, int buy, int grps,String index) throws Exception {
        DividendList dl;
//        if(!divList.containsKey(oldTrd.getShare()))
//            divList.put(oldTrd.getShare(), bsDB.getDividendData(oldTrd.getShare()));
        dl = getDL(oldTrd.getShare(),index);
        //DividendList dlRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate());

        //double sumRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate()); neww
        double sumRange1 = dl.getDividendDataRangeOld(oldTrd.getStartDate(), sd.getDate());
        //for(int j=0; j<dlRange1.getSize(); j++) {
        //    sumRange1 = sumRange1 + dlRange1.getDividendData(j).getAmount();
        //}
        double divProfit = calcDividend(oldTrd.getStartPrice(), sumRange1, buy, grps);
        //Trade trd=new Trade(oldTrd.getStartDate(), oldTrd.getStartPrice(), oldTrd.getCloseDate(), oldTrd.getClosePrice());
        //trd.setShare(oldTrd.getShare());
        double prft = calcProfit(oldTrd.getStartPrice(), sd.getClosePrice(), buy, grps);
        oldTrd.setProfit(prft + divProfit);
//        if(Math.abs((divProfit/(prft+divProfit))*100)>4)
//            System.out.println(oldTrd.getShare() + "\t" + oldTrd.toString() + "\t" +divProfit);
        return oldTrd;
    }

    public Trade calcDividendTrade(Trade oldTrd, int buy, int grps) throws Exception {
        return calcDividendTrade(oldTrd, buy, grps,"UKX");
    }

    public Trade calcDividendTrade(Trade oldTrd, int buy, int grps,String index) throws Exception {
        DividendList dl;
//        if(!divList.containsKey(oldTrd.getShare()))
//            divList.put(oldTrd.getShare(), bsDB.getDividendData(oldTrd.getShare()));
        dl = getDL(oldTrd.getShare(),index);
        //DividendList dlRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate());

        //double sumRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate()); neww
        double sumRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate());
        //for(int j=0; j<dlRange1.getSize(); j++) {
        //    sumRange1 = sumRange1 + dlRange1.getDividendData(j).getAmount();
        //}
        double divProfit = calcDividend(oldTrd.getStartPrice(), sumRange1, buy, grps);
        //Trade trd=new Trade(oldTrd.getStartDate(), oldTrd.getStartPrice(), oldTrd.getCloseDate(), oldTrd.getClosePrice());
        //trd.setShare(oldTrd.getShare());
        double prft = calcProfit(oldTrd.getStartPrice(), oldTrd.getClosePrice(), buy, grps);
        oldTrd.setProfit(prft + divProfit);
//        if(Math.abs((divProfit/(prft+divProfit))*100)>4)
//            System.out.println(oldTrd.getShare() + "\t" + oldTrd.toString() + "\t" +divProfit);
        return oldTrd;
    }

    public Trade calcDividendTrade(Trade oldTrd, int buy, int grps,double invest) throws Exception {
        return calcDividendTrade(oldTrd, buy, grps,invest,"UKX");
    }

    public Trade calcDividendTrade(Trade oldTrd, int buy, int grps,double invest,String index) throws Exception {
        DividendList dl;
//        if(!divList.containsKey(oldTrd.getShare()))
//            divList.put(oldTrd.getShare(), bsDB.getDividendData(oldTrd.getShare()));
        dl = getDL(oldTrd.getShare(),index);
        //DividendList dlRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate());

        //double sumRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate()); neww
        double sumRange1 = dl.getDividendDataRange(oldTrd.getStartDate(), oldTrd.getCloseDate());
        //for(int j=0; j<dlRange1.getSize(); j++) {
        //    sumRange1 = sumRange1 + dlRange1.getDividendData(j).getAmount();
        //}
        double divProfit = calcDividend(oldTrd.getStartPrice(), sumRange1, buy, grps,invest);
        //Trade trd=new Trade(oldTrd.getStartDate(), oldTrd.getStartPrice(), oldTrd.getCloseDate(), oldTrd.getClosePrice());
        //trd.setShare(oldTrd.getShare());
        double prft = calcProfit(oldTrd.getStartPrice(), oldTrd.getClosePrice(), buy, grps,invest);
        oldTrd.setProfit(prft + divProfit);
//        if(Math.abs((divProfit/(prft+divProfit))*100)>4)
//            System.out.println(oldTrd.getShare() + "\t" + oldTrd.toString() + "\t" +divProfit);
        return oldTrd;
    }

    public TradeList filterSingle(TradeList tl, int count) {
        TradeList newTl = new TradeList(tl.getSize());

        if (tl.getSize() > 0) {
            Trade trdLast = tl.getTrade(0);
            newTl.addTrade(trdLast);
            int currCount = 1;
            for (int i = 1; i < tl.getSize(); i++) {
                Trade trd = tl.getTrade(i);

                if (trd.getStartDate().after(trdLast.getCloseDate())) {
                    newTl.addTrade(trd);
                    trdLast = trd;
                    currCount = 1;
                } else if (currCount < count) {
                    currCount++;
                    newTl.addTrade(trd);
                }
            }
        }
        return newTl;

    }

    public TradeList filterSingle(TradeList tl, TradeList tlcls, int count) {
        TradeList newTl = new TradeList(tl.getSize());

        if (tl.getSize() > 0 && tlcls.getSize() > 0) {
            Trade trdLast = tlcls.getTrade(0);
            newTl.addTrade(trdLast);
            int currCount = 1;
            int nullCount = 0;
            for (int i = 1; i < tl.getSize(); i++) {
                Trade trd = tl.getTrade(i);
                Trade trdcls = null;
                if (i <= tlcls.getSize() - 1) {
                    trdcls = tlcls.getTrade(i);
                }
                /*if(trdcls!=null && trd.getStartDate().equals(trdcls.getStartDate()))
                {*/
                if (trd.getStartDate().after(trdLast.getCloseDate())) {
                    newTl.addTrade(trd);
                    if (trdcls != null) {
                        trdLast = trdcls;
                    } else {
                        nullCount++;
                        //System.out.println(nullCount);
                        if (nullCount >= count) {
                            break;
                        }
                    }
                    currCount = 1;
                } else if (currCount < count) {
                    currCount++;
                    newTl.addTrade(trd);
                }
                /*}
                else
                {

                }*/
            }
        }
        return newTl;

    }

    public TradeList filterSingle(TradeList tl, TradeList tlcls, ShareList sl, int count) {
        TradeList newTl = new TradeList(tl.getSize());
        TreeMap<Date, Integer> dailyList = new TreeMap<Date, Integer>();
        if (tl.getSize() > 0 && tlcls.getSize() > 0) {
            for (int i = 0; i < tlcls.getSize(); i++) {
                Trade trd = tlcls.getTrade(i);
                int start = sl.isDatePresent(trd.getStartDate());
                int end = sl.isDatePresent(trd.getCloseDate());
                for (int j = start; j <= end; j++) {
                    Date d = sl.getSharedata(j).getDate();
                    int cnt = 1;
                    if (dailyList.containsKey(d)) {
                        cnt = dailyList.get(d) + 1;
                    }
                    dailyList.put(d, cnt);
                }
            }
            for (int i = 0; i < tl.getSize(); i++) {
                Trade trd = tl.getTrade(i);
                int cnt = 0;
                if (dailyList.containsKey(trd.getStartDate())) {
                    cnt = dailyList.get(trd.getStartDate());
                }
                if (cnt <= count) {
                    newTl.addTrade(trd);
                }
            }
        }
        return newTl;

    }


    public double calcProfit(double start, double close, int buy) {
        double money = 1000000;
        double shareNo = money / start;
        double endMoney = close * shareNo;
        double ret = 0;
        if (buy == 1) {
            ret = endMoney - money;
        } else {
            ret = money - endMoney;
        }

        return (ret / 100);
    }

    public double calcProfit(double start, double close, int buy,double money) {
        //double money = 1000000;
        double shareNo = money / start;
        double endMoney = close * shareNo;
        double ret = 0;
        if (buy == 1) {
            ret = endMoney - money;
        } else {
            ret = money - endMoney;
        }

        return (ret / 100);
    }

    public double calcProfit(double start, double close, int buy, int grps,double money) {
        return calcProfit(start, close, buy,money) / grps;
    }

    public double calcProfit(double start, double close, int buy, int grps) {
        return calcProfit(start, close, buy) / grps;
    }

    public double calcDividend(double start, double totalDiv, int buy, int grps) {
        double money = 1000000;
        double shareNo = money / start;
        double endMoney = totalDiv * shareNo;
        double ret = 0;
        if (buy == 1) {
            ret = endMoney;
        } else {
            ret = endMoney * -1.0;
        }

        return (ret / 100) / grps;
    }

    public double calcDividend(double start, double totalDiv, int buy, int grps,double money) {
        double shareNo = money / start;
        double endMoney = totalDiv * shareNo;
        double ret = 0;
        if (buy == 1) {
            ret = endMoney;
        } else {
            ret = endMoney * -1.0;
        }

        return (ret / 100) / grps;
    }

    public ShareList getSL(String sh, String Index) throws Exception {
        ShareList sl = null;
        String s=(sh+Index);
        if (Index.equals("UKX")) {
            return getSL(sh);
        } else if (Index.equals("GSPC") || Index.equals("OEX")) {

            if (sh.contains(" ")) {
                if (shListMa.containsKey(sh)) {
                    sl = shListMa.get(sh);
                } else {
                    if (!live) {
                        try {
                            sl = (ShareList) client.get(sh);
                        } catch (Exception ex) {
                        }
                    }
                    if (sl == null) {
                        String shares[] = sh.split(" ");
                        int days=1;
                        if (shares.length >= 3) {
                            days = Integer.parseInt(shares[3]);

                        } else{
                            System.out.println("share err:"+ sh);
                        }
                        MA ma = new MA(days, MA.Simple);
                        try {
                            sl = bsDB.getShareData(shares[0] + " " + shares[1], shares[2], ma, false);
                        } catch (SQLException ex) {
                            bsDB.close();
                            bsDB = new BasicShareDB();
                            sl = bsDB.getShareData(shares[0] + " " + shares[1], shares[2], ma, false);
                        } finally {
                        }
                        if (!isLive()) {
                            try{
                            client.add(sh, exp, sl);
                            } catch(Exception ex){

                            }
                        }

                    }
                    if (shListMa.size() > 100) {
                        shListMa.clear();
                        shListMa.setUp(100);
                    }
                    shListMa.put(sh, sl);
                }
            } else {


                if (shList.containsKey(s)) {
                    sl = shList.get(s);
                } else {
                    if (!isLive()) {
                        try {
                            sl = (ShareList) client.get(s);
                        } catch (Exception ex) {
                        }
                    }
                    if (sl == null) {
                        try {
                            sl = bsDB.getShareTableData(sh, "SharesUS");
                        } catch (SQLException ex) {
                            bsDB.close();
                            bsDB = new BasicShareDB();
                            sl = bsDB.getShareTableData(sh, "SharesUS");
                        }
                        if(!isLive())
                            try{
                                client.add(s, expDay, sl);
                            } catch(Exception ex){}
                    }
                    shList.put(s, sl);
                }
            }
        } else if(Index.equals("CUR")){
            if (shList.containsKey(s)) {
                sl = shList.get(s);
            } else {
                if (sl == null) {
                    
                    try {
                        sl = bsDB.getCurrencyData(sh);
                    } catch (SQLException ex) {
                        bsDB.close();
                        bsDB = new BasicShareDB();
                        sl = bsDB.getCurrencyData(sh);
                    }
                }
                shList.put(s, sl);
            }
        } else if (Index.equals("MCX")){
            if (shList.containsKey(s)) {
                //sl = (ShareList)shList.get(sh).getObjectValue();
                sl = shList.get(s);
            } else {
                if (!isLive()) {
                    try {
                        sl = (ShareList) client.get(s);
                    } catch (Exception ex) {
                    }
                }
                if (sl == null) {
                    try {
                        sl = bsDB.getShareTableData(sh, "Shares350");
                    } catch (SQLException ex) {
                        bsDB.close();
                        bsDB = new BasicShareDB();
                        sl = bsDB.getShareTableData(sh, "Shares350");
                    }

                    if (!isLive()) {
                        try {
                            client.add(s, expDay, sl);
                        } catch (Exception e) {
                        }
                    }

                }
                shList.put(s, sl);
            }
        }
        else// if (Index.equals("ASX"))
        {
            if (shList.containsKey(s)) {
                //sl = (ShareList)shList.get(sh).getObjectValue();
                sl = shList.get(s);
            } else {
                if (!isLive()) {
                    try {
                        sl = (ShareList) client.get(s);
                    } catch (Exception ex) {
                    }
                }
                if (sl == null) {
                    try {
                        sl = bsDB.getShareTableData(sh, "SharesComplete");
                    } catch (SQLException ex) {
                        bsDB.close();
                        bsDB = new BasicShareDB();
                        sl = bsDB.getShareTableData(sh, "SharesComplete");
                    }

                    if(!isLive())
                        try{
                            client.add(s, expDay, sl);
                        } catch(Exception e){}

                }
                shList.put(s, sl);
            }
            //if (shList.isKeyInCache(sh)) {
            /*if (shList.containsKey(s)) {
                //sl = (ShareList)shList.get(sh).getObjectValue();
                sl = shList.get(s);
            } else {
                try {
                    sl = bsDB.getShareTableData(sh, "SharesComplete");
                } catch (SQLException ex) {
                    bsDB.close();
                    bsDB = new BasicShareDB();
                    sl = bsDB.getShareTableData(sh, "SharesComplete");
                }
                //shList.putIfAbsent(new Element(sh, sl));
                shList.put(s.intern(), sl);
            }*/
        }
        return sl;
    }

    private ShareList getSL(String sh) throws Exception {
        ShareList sl = null;
        String index=(sh+"UKX");
        if(bsDB==null)
            bsDB=new BasicShareDB();
        if (sh.contains(" ")) {
            if (shListMa.containsKey(sh)) {
                sl = shListMa.get(sh);
            } else {
                if (!isLive()) {
                    try {
                        sl = (ShareList) client.get(sh);
                    } catch (Exception ex) {
                    }
                }
                if (sl == null) {
                    String shares[] = sh.split(" ");
                    int days = Integer.parseInt(shares[3]);
                    MA ma = new MA(days, MA.Simple);
                    try {
                        sl = bsDB.getShareData(shares[0] + " " + shares[1], shares[2], ma, false);
                    } catch (SQLException ex) {
                        bsDB.close();
                        bsDB = new BasicShareDB();
                        sl = bsDB.getShareData(shares[0] + " " + shares[1], shares[2], ma, false);
                    } finally {
                    }
                    if (!isLive()) {
                        try{
                        client.add(sh, exp, sl);
                        }catch(Exception e){}
                    }

                }
                if (shListMa.size() > 100) {
                    shListMa.clear();
                    shListMa.setUp(100);
                }
                shListMa.put(sh, sl);
            }

        } else {
            if (shList.containsKey(index)) {
                sl = shList.get(index);
            } else{
                if (!isLive()) {
                    try {
                        sl = (ShareList) client.get(index);
                    } catch (Exception ex) {
                    }
                }
                if (sl == null) {
                    try {
                        sl = bsDB.getShareData(sh);
                    } catch (SQLException ex) {
                        bsDB.close();
                        bsDB = new BasicShareDB();
                        sl = bsDB.getShareData(sh);
                    }
                    if (!isLive())
                        try{
                            client.add(index, expDay, sl);
                        }catch(Exception e){}
                    //shList.put(index.intern(), sl);
                }
                shList.put(index, sl);
            }


//            if (shList.containsKey(index)) {
//                //sl = (ShareList)shList.get(sh).getObjectValue();
//                sl = shList.get(index);
//            } else {
//                try {
//                    sl = bsDB.getShareData(sh);
//                } catch (SQLException ex) {
//                    bsDB.close();
//                    bsDB = new BasicShareDB();
//                    sl = bsDB.getShareData(sh);
//                } finally {
//                    shList.put(index.intern(), sl);
//                }
                //shList.putIfAbsent(new Element(sh, sl));
                //shList.put(sh, sl);
//            }
        }
        return sl;
    }

    public DividendList getDL(String sh,String index) throws Exception {
        String share=sh+index;
        DividendList dl = divList.get(share);
        if (dl == null && !live) {
            try {
                dl = (DividendList) client.get(share + "D");
                divList.put(share.intern(), dl);
            } catch (Exception ex) {
            }
        }
        if (dl == null) {
            dl = bsDB.getDividendData(sh, index);
            divList.put(share.intern(), dl);
            if (!live) {
                try {
                    client.add(share + "D", exp, dl);
                } catch (Exception ex) {
                }
            }

        }
        return dl;
    }

    public ResultList getResults(String sh,String index) throws Exception {
        String share=sh+index;
        ResultList rl = resList.get(share);
        if (rl == null && !live) {
            try {
                rl = (ResultList) client.get(share + "R");
                resList.put(share.intern(), rl);
            } catch (Exception ex) {
            }
        }
        if (rl == null) {
            rl = bsDB.getResultsData(sh, index);
            resList.put(share.intern(), rl);
            if (!live) {
                try {
                    client.add(share + "R", exp, rl);
                } catch (Exception ex) {
                }
            }

        }
        return rl;
    }

    public ShareList updateSL(String share, Date today, String index) throws Exception {
        ShareList slnew = null;
        ShareList sl = getSL(share,index);
        ShareData sd = bsDB.getShareData(share, today, index);
        if (sd != null) {
            if (sl.isDatePresent(sd.getDate()) > -1) {
                sl.updateSharedata(sd);
            } else {
                sl.addShareData(sd);
            }
        }
        slnew = sl;
        return slnew;
    }

    private Trade indicatorStopTrade(Trade trd, AbstractIndicator absInd, double threshold, boolean sell, int use) throws Exception {
        String share = trd.getShare();
        ShareList tsl = getSL(share);
        IndicatorList il = indList.get(share + absInd.toString());

        if (il == null) {
            if (indList.size() > 500) {
                indList.clear();
            }
            il = absInd.buildIndicator(tsl);
            indList.put(share + absInd.toString(), il);

        }
        int start = tsl.isDatePresent(trd.getStartDate()) + 1;
        int end = tsl.isDatePresent(trd.getCloseDate());
        Trade newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), trd.getCloseDate(), trd.getClosePrice());

        double profit = ((trd.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
        if (sell) {
            profit = ((trd.getStartPrice() - trd.getClosePrice()) / trd.getStartPrice()) * 100;
        }
        if (start >= 1 && end >= 1 && end <= tsl.getSize() - 1) {
//            IndicatorList ilnew=il.filterRange(trd.getStartDate(), trd.getCloseDate(), threshold, use, sell);
//            if(ilnew.getSize()>0)
//            {
//                int useful=use-1;
//                if(ilnew.getSize()>=use)
//                {
//                    //useful=ilnew.getSize()-1;
//
//                    Date endD=ilnew.getSharedata(useful).getDDate();
//                    end=tsl.isDatePresent(endD);
//                    ShareData sdend=tsl.getSharedata(end);
//                    double prof=((sdend.getClosePrice()-trd.getStartPrice())/trd.getStartPrice())*100;
//                    if(sell)
//                    {
//                        prof=((trd.getStartPrice()-sdend.getClosePrice())/trd.getStartPrice())*100;
//                    }
//                    newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
//                    profit=prof;
//                }
//            }
            int cnt = 0;
            for (int i = start; i <= end; i++) {
                int ind = il.isDatePresent(tsl.getSharedata(i).getDate());
                IndicatorField indF = il.getSharedata(ind);
                IndicatorField indFL = il.getSharedata(ind - 1);
                if (!sell && indF.getValue() >= threshold && indFL.getValue() < threshold) {
                    //il.addIndField(indF);
                    cnt++;
                    if (cnt == use) {
                        Date edate = indF.getDDate();
                        ShareData sdend = tsl.getSharedata(tsl.isDatePresent(edate));
                        profit = ((sdend.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
                        if (sell) {
                            profit = ((trd.getStartPrice() - sdend.getClosePrice()) / trd.getStartPrice()) * 100;
                        }
                        newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                        break;
                    }
                } else if (sell && indF.getValue() <= threshold && indFL.getValue() < threshold) {
                    //il.addIndField(indF);
                    if (il.getSize() == use) {
                        Date edate = indF.getDDate();
                        ShareData sdend = tsl.getSharedata(tsl.isDatePresent(edate));
                        profit = ((sdend.getClosePrice() - trd.getStartPrice()) / trd.getStartPrice()) * 100;
                        if (sell) {
                            profit = ((trd.getStartPrice() - sdend.getClosePrice()) / trd.getStartPrice()) * 100;
                        }
                        newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                        break;
                    }
                }
            }




        }
        newTrade.setShare(trd.getShare());
        if (!trd.isValid()) {
            profit = 0;
            newTrade.setIsValid(false);
        }
        newTrade.setProfit(profit);
        return newTrade;
    }

    /**
     * @return the shList
     */
    public THashMap<String,ShareList> getShList() {
//        Map<String, ShareList> sl=new HashMap<String, ShareList>(shList.getKeys().size());
//        for(Object sh:shList.getKeys()){
//            Element e=shList.get(sh);
//            sl.put(sh.toString(),(ShareList)e.getObjectValue());
//        }
        return new THashMap<String, ShareList>();
        //return shList;
    }

    /**
     * @return the indList
     */
    public THashMap<String, IndicatorList> getIndList() {
        return indList;
    }



//    public double getDrawDown(TradeList yrTrades,int buy,boolean yearly,int grpShares,ShareList index) throws Exception
//    {
//        double profit=0.0;
//        double min=0.0;
//        double midprofit=0.0;
//        TradeList tl=new TradeList();
//        TreeMap<Date,Double> dateprofit=new TreeMap<Date, Double>();
//        for(int i=0;i<yrTrades.getSize();i++)
//        {
//            Trade trd=yrTrades.getTrade(i);
//            if((i>0 && i%grpShares==0) ||  i==yrTrades.getSize()-1)
//            {
//                if(i==yrTrades.getSize()-1)
//                {
//                    tl.addTrade(trd);
//                    profit+=calcProfit(trd.getStartPrice(), trd.getClosePrice(), buy,grpShares);
//                }
//                TreeMap<Date,Double> dateprofitTmp=getIntraDayProfit(tl, buy, grpShares,index);
//                for(Date d:dateprofitTmp.keySet())
//                {
//                    double prft=dateprofitTmp.get(d)+midprofit;
////                    if(yearly)
////                        System.out.println(d+ "," + prft + "," + dateprofit.get(d));
//                    if(dateprofit.containsKey(d))
//                        prft+=dateprofit.get(d);
//                    dateprofit.put(d, prft);
//                }
////                if(yearly)
////                    System.out.println();
//                tl=new TradeList();
//                if(yearly)
//                    midprofit=profit;
//            }
//            tl.addTrade(trd);
//            if(i==0 && grpShares==1)
//            {
//                TreeMap<Date,Double> dateprofitTmp=getIntraDayProfit(tl, buy, grpShares,index);
//                for(Date d:dateprofitTmp.keySet())
//                {
//                    double prft=dateprofitTmp.get(d)+midprofit;
//                    if(dateprofit.containsKey(d))
//                        prft+=dateprofit.get(d);
//                    dateprofit.put(d, prft);
//                }
//                tl=new TradeList();
//            }
//
//            profit+=calcProfit(trd.getStartPrice(), trd.getClosePrice(), buy,grpShares);
//        }
//        for(Date d:dateprofit.keySet())
//        {
//            double prft=dateprofit.get(d);
////            if(yearly)
////                System.out.println(d+ "," + prft/100);
//            if(prft<min)
//                min=prft;
//        }
//        return min/100;
//    }
    public IndicatorList getIndicatorListFromDB(String pair, Date d1, Date d2, String ind) {
        IndicatorList il = null, il1 = null;

        if (indList.containsKey(pair)) {
            il = indList.get(pair);
            if (il.isDatePresent(d1) == -1 && ind.equals("Correlation/85")) {
                try {
                    il1 = shareDB.getIndicatorList(pair, d1, d2, ind);
                } catch (SQLException ex) {
                    try {
                        shareDB.close();
                        shareDB = new ShareListDB();
                        il1 = shareDB.getIndicatorList(pair, d1, d2, ind);
                    } catch (Exception ex1) {
                        Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                for (int i = 0; i < il1.getSize(); i++) {
                    il.addIndField(il1.getSharedata(i));
                }
            }
        } else {
            try {
                il = shareDB.getIndicatorList(pair, d1, d2, ind);
            } catch (SQLException ex) {
                try {
                    shareDB.close();
                    shareDB = new ShareListDB();
                    il = shareDB.getIndicatorList(pair, d1, d2, ind);
                } catch (Exception ex1) {
                    Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            if (indList.size() > 1500) {
                indList.clear();
            }
            //if(!ind.equals("Correlation/85"))
            //indList.put(pair, il);
        }
        return il;
    }

    public IndicatorList getIndicatorListBuild(String pair, String ind) throws Exception {
        IndicatorList il = null;
        il = indList.get(pair);
        if(il==null && !live){
            try{
            //il=(IndicatorList) client.get(pair);
            //indList.put(pair, il);
            }catch(Exception e){}
        }
        if (il==null) {
            String[] str = pair.split("-");
            String buy = str[0];
            String sell = str[1];
            CorrelationIndicator corr = new CorrelationIndicator();
            HashMap hm = new HashMap();
            hm.put(1, 85);
            corr.init(hm);
            il = corr.buildIndicator(getSL(sell), getSL(buy));
            if (indList.size() > 10000) {
                indList.clear();
            }
            //if(!ind.equals("Correlation/85"))
            indList.put(pair, il);
            try{
            //    if(!live)
            //        client.add(pair, exp, il);
            }catch(Exception s){}
        }
        return il;
    }

    public void putIndicatorList(String share, IndicatorList il) {
        indList.put(share, il);
        try {
//            if(!live)
//                client.add(share, exp, il);
        } catch (Exception ex) {
            Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public IndicatorList getIndicatorList(String share) {
        IndicatorList ret=indList.get(share);
//        if(ret==null)
//        {
//            try{
//            ret=(IndicatorList) client.get(share);
//            indList.put(share, ret);
//            }catch(Exception e){}
//        }
        return ret;
    }

    public void clearIndicatorList() {
        indList.clear();
    }

    private boolean isLiquid(ShareList sl, Date startDate) {
        int end = sl.isDatePresent(startDate);
        double maxliq = 1000000;
        if (end == -1) {
            System.out.println("err1," + sl.getShare() + "," + sdf.format(startDate));
            return false;
        }
        int start = end - 10;
        if (start < 0) {
            return false;
        }
        long vol = 0;
        double close = 0.0;
        int volcount = 0, mrkValue = 0;
        for (int i = start; i < end; i++) {
            long volume = sl.getSharedata(i).getVol();
            vol += volume;
            if (volume >= 1000000l) {
                volcount++;
            }
            double mrk = (volume * (sl.getSharedata(i).getClosePrice() / 100));
            if (mrk >= maxliq) {
                mrkValue++;
            }
            close += mrk;
        }

        return (vol > 1000000l && volcount >= 3) || (close >= maxliq && mrkValue >= 3);
    }

    private boolean isDividend(ShareList Share, Date open, boolean sell,String index) throws Exception {

        boolean ret = false;
        DividendList dl = getDL(Share.getShare(),index);
        int dlind = dl.isDatePresent(open);
        if (dlind == -1) {
            dlind = dl.isHigherDatePresent(open);
        }
        if (dlind != -1) {
            Date div = dl.getDividendData(dlind).getDate();
            int next = Share.isDatePresent(div);
            int diff = 0;
            if (next == -1 && div.after(new Date())) {
                diff=(int)dateDiff(open, div);
            } else {
                diff = Share.isDatePresent(div) - Share.isDatePresent(open);
            }
            if (sell && diff >= 0 && diff <= 25) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean hasResult(ShareList Share, Date open, int days,String index) throws Exception {

        boolean ret = false;
        ResultList rl = getResults(Share.getShare(),index);
        ResultData dlind = rl.isDatePresent(open);
        if (dlind == null) {
            dlind = rl.isHigherDatePresent(open);
        }
        if (dlind != null) {
            Date div = dlind.getDate();
            int next = Share.isDatePresent(div);
            int diff = 0;
            if (next == -1 && div.after(new Date())) {
                diff=(int)dateDiff(open, div);
            } else {
                diff = Share.isDatePresent(div) - Share.isDatePresent(open);
            }
            if (diff >= 0 && diff <= days) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Calculate the difference between two dates, ignoring weekends.
     *
     * @param d1 the first day of the interval
     * @param d2 the day after the end of the interval
     * @return the number of days in the interval, excluding weekends
     */
    public long dateDiff(Date d1, Date d2) {
        return wdnum(d2) - wdnum(d1);
    }

    /**
     * Return the number of week days between Monday, 29 December, 1969 and the
     * given date.
     *
     * @param d a date
     * @return the number of week days since Monday, 29 December, 1969
     */
    private long wdnum(Date date) {
        long l = div(date.getTime(), 1000 * 60 * 60 * 24)[0] + 3;
        long d[] = div(l, 7);
        return 5 * d[0] + Math.min(d[1], 5);
    }

    /**
     * Divide two integers, rounding towards -Inf and returning quotient and
     * remainder.
     *
     * @param n the numerator
     * @param d the denominator
     * @return the quotient and remainder
     * @throws ArithmeticException if <code>d == 0</code>
     */
    public static long[] div(long n, long d) {
	long q = n / d;
	long r = n % d;
	// n == q * d + r == (q - 1) * d + d + r
	if (r < 0) {
	    q--;
	    r += d;
	}
	return new long[] {
	    q, r,
	};
    }

    /**
     * @return the ukxHighs
     * @throws Exception
     */
    public TreeMap<Date,String> getUkxHighLow() throws Exception {
        if(ukxHighLow==null)
        {
            getukxHighLows();
        }
        return ukxHighLow;
    }

    private void getukxHighLows() throws Exception {
        ukxHighLow = new TreeMap<Date, String> ();
        ShareList ukx = getSL("UKX");
        double high = ukx.getSharedata(0).getClosePrice();
        double low = high;
        Date first=ukx.getSharedata(0).getDate();
        Date highD = null;
        Date lowD = null;
        boolean highs = true;
        double perc=5;
        for (int i = 1; i < ukx.getSize(); i++) {
            double price = ukx.getSharedata(i).getClosePrice();
            Date d = ukx.getSharedata(i).getDate();
            if (price > high) {
                high = price;
                highD = d;
            }
            if (price < low) {
                low = price;
                lowD = d;
            }
            //System.out.print(d+","+price);
            if (highs && (high - price) * 100 / high >= perc) {
                //System.out.print(",H,"+high+","+highD);
                ukxHighLow.put(highD,"H");
                //highVals.put(highD, high);
                high = price;
                highD = d;
                low = price;
                lowD = d;
                highs = false;
                if(first!=null)
                {
                    ukxHighLow.put(first,"L");
                    first=null;
                }
            } else if (!highs && (price - low) * 100 / low >= perc) {
                //System.out.print(",,,,L,"+low+","+lowD);
                //lowVals.put(lowD, low);
                ukxHighLow.put(lowD,"L");
                low = price;
                lowD = d;
                high = price;
                highD = d;
                highs = true;
                if(first!=null)
                {
                    ukxHighLow.put(first,"H");
                    first=null;
                }
            }
            //System.out.println();
        }
        /*int days=0;
        for (int i = 1; i < ukx.getSize(); i++) {
            double price = ukx.getSharedata(i).getClosePrice();
            Date d = ukx.getSharedata(i).getDate();
            System.out.print(sdf.format(d) + "," + price);
            days++;
            if (ukxHighLow.containsKey(d)) {
                if (ukxHighLow.get(d).equals("H")) {
                    System.out.print(",H," + price);
                    days = 0;
                } else {
                    System.out.print(",,,L," + price+","+days);
                }
            }
            System.out.println();
        }*/
    }

    /**
     * @return the shDayList
     */
    public THashMap<String, THashMap<Boolean, THashMap<Date, ArrayList<String>>>> getShDayList() {
        return shDayList;
    }

    /**
     * @return the shDayList
     */
    public THashMap<Boolean, THashMap<Date, ArrayList<String>>> getShDayList(String index) {
        return shDayList.get(index);
    }

    /**
     * @return the divList
     */
    public THashMap<String, DividendList> getDivList() {
        return divList;
    }

    public void getTopShares(Date open, String share) {

    }

    /**
     * @return the live
     */
    public boolean isLive() {
        return live;
    }

    /**
     * @param live the live to set
     */
    public void setLive(boolean live) {
        this.live = live;
        if (client == null && !live) {
            try {
                List<URI> hosts = Arrays.asList(
                        new URI("http://192.168.1.32:8091/pools"),
                        new URI("http://192.168.1.40:8091/pools"));

                CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
                cfb.setOpTimeout(5000);
                //cfb.setShouldOptimize(true);
                cfb.setOpQueueMaxBlockTime(5000);
                CouchbaseConnectionFactory cf = cfb.buildCouchbaseConnection(hosts, "BackTradeMem", "");
                client = new CouchbaseClient(cf);
            } catch (Exception ex) {
                Logger.getLogger(TradeCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String getReducedKey(String name){
        String nam = name;
        for (int j = 0; j < arr.size(); j += 2) {
            nam = nam.replace(arr.get(j), arr.get(j + 1));
        }
        return nam;
    }

    public Object getCached(String key){
        return client.get(key);
    }

    public void setCached(String key,Object value){
        if(!live)
            client.add(key,exp,value);
    }

    private TradeList stopResultTradeList(TradeList oldTL, boolean sell, String ind) throws Exception {
        TradeList tl = new TradeList();
        for (int i = 0; i < oldTL.getSize(); i++) {
            Trade trd = oldTL.getTrade(i);
            Trade nTrd = stopResultTrade(trd, ind);
            if(nTrd!=null)
                tl.addTrade(nTrd);
            else
                tl.addTrade(trd);
        }
        return tl;
    }

    private Trade stopResultTrade(Trade trd, String index) throws Exception {
        Trade ret=null;
        String share = trd.getShare();
        ShareList tsl = getSL(share, index);
        ResultList rl = getResults(share,index);
        ResultData rlData = rl.isDatePresent(trd.getStartDate());
        if (rlData == null) {
            rlData = rl.isHigherDatePresent(trd.getStartDate());
        }
        //ShareList indSL=getSL(index, index);
        if (rlData != null) {
            Date div = rlData.getDate();
            if(div.before(trd.getCloseDate())){
                //div = indSL.getSharedata(indSL.isLowerDatePresent(div)).getDate();
                int ind=tsl.isDatePresent(div);
                if(ind==-1)
                    ind=tsl.isHigherDatePresent(div);
                if(ind>0)
                    ind-=1;
                ShareData sdend = tsl.getSharedata(ind);
                if(sdend.getDate().before(trd.getStartDate()) || sdend.getDate().equals(trd.getStartDate()))
                    return ret;
                Trade newTrade = new Trade(trd.getStartDate(), trd.getStartPrice(), sdend.getDate(), sdend.getClosePrice());
                newTrade.setShare(trd.getShare());
                if (!trd.isValid()) {
                    newTrade.setIsValid(false);
                }
                ret=newTrade;
            }
//            else{
//                ret.add(trd);
//            }
        }
//        else{
//            ret.add(trd);
//        }
        return ret;
    }

    private void cacheSector(String sec,String index) throws Exception {
        if(bsDB==null){
            bsDB=new BasicShareDB();
        }
        sectorList.put(index,bsDB.getSuperSectors(sec, index));
    }
    
    public SuperSect getSector(String index) {
        return sectorList.get(index);
    }

    public ArrayList<String> getCurrencies() throws Exception {
        return shareDB.getCurrencies();
    }

    

    public IndicatorList storeInd(ShareList sl,AbstractIndicator ind) throws Exception {
        IndicatorList il= ind.buildIndicator(sl);
        bsDB.storeInd(il,sl,ind);
        return il;
    }
    
    public IndicatorList getInd(String sl,String ind) throws Exception {
        
        IndicatorList il=bsDB.getInd(sl,ind);
        return il;
    }

    public boolean isCurr(String curr, String shortName) throws SQLException {
        return bsDB.isCurr(curr,shortName);
    }

    public TradeList getCurrencyOpenTrades(String cur, String indicator, String opens,boolean buy, boolean insert) throws Exception {
        TradeList tl=null;
        String id=(buy?"Buy":"Sell") +";" + cur.split(",")[1] + ";" + indicator + ";" + opens;
        String[] op=opens.split(",");
        switch(op[0]){
            case "Open1":
                tl=bsDB.getThresholdOpen(cur,indicator,op[1],buy,insert,id);
                break;
            case "Open2":
                tl=bsDB.getThresholdOpen(cur,indicator,op[1],!buy,insert,id);
                break;
            case "Open3":
                tl=bsDB.getSignalOpen(cur,indicator,buy,insert,id);
                break;
            case "Open4":
                tl=bsDB.getSignalOpen(cur,indicator,!buy,insert,id);
                break;
        }
        return tl;
    }
    
    public TradeList getCurrencyOpenTrades(String cur, String indicator, String opens,boolean buy,String filterTime,String openFilter) throws Exception {
        TradeList tl=null;
        String id=(buy?"Buy":"Sell") +";" + cur.split(",")[1] + ";" + indicator + ";" + opens;
        if (openFilter != null && !openFilter.isEmpty()) {
            tl = bsDB.getCurrencyTrades(id,cur, filterTime,openFilter);
        } else{
            tl = bsDB.getCurrencyTrades(id,cur);
        }
        return tl;
    }
   
    public TradeList getCurrencyOpenTrades(String cur, ForexTradeParameters ftr) throws Exception {
        TradeList tl=null;
        String id=ftr.getOpenid();
        if (ftr.getFilters() != null && !ftr.getFilters().isEmpty()) {
            tl = bsDB.getCurrencyTrades(id,cur, ftr.getFilterTime(),ftr.getFilters(),ftr.getFilterValue());
        } else{
            tl = bsDB.getCurrencyTrades(id,cur);
        }
        return tl;
    }
    
    public TradeList getCurrencyCloseTrades(String cur, String indicator,String close,boolean buy, boolean insert,TradeList openTL,String idO,String idC) throws Exception {
        TradeList tl=null;
        String[] cl=close.split(",");
        switch(cl[0]){
            case "Close1":
                tl=bsDB.getThresholdClose(cur,indicator,close,buy,insert,idO+";"+idC,openTL);
                break;
            case "Close2":
                tl=bsDB.getThresholdClose(cur,indicator,close,!buy,insert,idO+";"+idC,openTL);
                break;
            case "Close3":
                tl=bsDB.getSignalClose(cur,indicator,close,buy,insert,idO+";"+idC,openTL);
                break;
            case "Close4":
                tl=bsDB.getSignalClose(cur,indicator,close,!buy,insert,idO+";"+idC,openTL);
                break;
        }
        
        return tl;
    }
    
    public ArrayList<Summary> buildCurrencySystem(ForexTradeParameters ftp, boolean sqlb,ArrayList<String> curList) throws Exception {
        ArrayList<Summary> ret = new ArrayList<Summary>();
        
        String id=ftp.getId();
        String[] curSplit = ftp.getBaseCurrency().split(",");
        String[] curClsSplit = ftp.getCloseCurrency().split(",");
        int combCount=ftp.getSystemCount();
        TradeList total = new TradeList();
        
        if (curSplit[0].equals("ALL")) {
            for (String c : curList) {
                String cur = c + "," + curSplit[1];
                String curCls = c + "," + curClsSplit[1];

                TradeList tl = buildCurrencyTrades(ftp,cur,curCls, sqlb);
                if (tl != null && tl.getSize() > 0) {
                    total.addTrade(tl);
                    Summary sum = new Summary(id, 10000000);
                    sum.setBuy(ftp.getBuyInt());
                    sum.calculateTrades(tl, getSL(cur, "CUR"), 1, this, false);
                    sum.setShare(cur);
                    ret.add(sum);
                }
            }
            if (total.getSize() > 0) {
                total.sort(new Comparator<Trade>() {

                    @Override
                    public int compare(Trade o1, Trade o2) {
                        int datecomp = o1.getStartDate().compareTo(o2.getStartDate());
                        return datecomp == 0 ? o1.getShare().compareTo(o2.getShare()) : datecomp;
                    }
                });
                if (combCount > 0) {
                    total = filterSingle(total, combCount);
                }
                Summary sum = new SummaryGroup(id, 10000000);
                sum.setBuy(ftp.getBuyInt());
                if (total != null && total.getSize() > 0) {
                    sum.calculateTrades(total, getSL("USDJPY,1D", "CUR"), 1, this, false);
                }
                
                sum.setShare(ftp.getBaseCurrency());
                //sum.setId(id);
                ret.add(sum);
            }
        } else {
            TradeList tl = buildCurrencyTrades(ftp,ftp.getBaseCurrency(),ftp.getCloseCurrency(), sqlb);
            Summary sum = new Summary(id, 10000000);
            sum.setBuy(ftp.getBuyInt());
            if (tl != null && tl.getSize() > 0) {
                sum.calculateTrades(tl, getSL(ftp.getBaseCurrency(), "CUR"), 1, this, true);
            }
            sum.setShare(ftp.getBaseCurrency());
            ret.add(sum);
        }
        return ret;
    }
    
    public TradeList buildCurrencyTrades(ForexTradeParameters ftp,String cur,String curCls, boolean sqlb) throws Exception {
        TradeList ret = null;
        if (bsDB.isCloseAvailable(ftp.getId(), cur,sqlb)) {
            Date d11 = new Date();
            ret = getCurrencyCloseTrades(ftp.getId(), cur,sqlb);
        } else {
            
            String indicator=ftp.getOpenIndicator();
            String indicatorCls=ftp.getCloseIndicator();
            ArrayList<String> filters = ftp.getFilters();
            ArrayList<String> filterTime = ftp.getFilterTime();
            ArrayList<String> filtersAfter = ftp.getFiltersAfter();
            ArrayList<String> filterAfterTime = ftp.getFilterAfterTime();
            if (indStore.putIfAbsent(cur + indicator, new CountDownLatch(1)) == null) {
                if (!isCurr(cur, indicator)) {
                    System.out.println(cur + "" + indicator);
                    ShareList sl = getSL(cur, "CUR");
                    storeInd(sl, buildIndicator(indicator));
                }
                indStore.get(cur + indicator).countDown();
            }
            if (indStore.putIfAbsent(curCls + indicatorCls, new CountDownLatch(1)) == null) {
                if (!isCurr(curCls, indicatorCls)) {
                    System.out.println(curCls + "" + indicatorCls);
                    ShareList sl = getSL(curCls, "CUR");
                    storeInd(sl, buildIndicator(indicatorCls));
                }
                indStore.get(curCls + indicatorCls).countDown();
            }
            Iterator<String> ftimeItr = filterTime.iterator();
            for (String filt : filters) {
                String fInd = buildIndicator(filt).getShortName();
                String ftime = ftimeItr.next().split(",")[0];
                String fCur = cur.split(",")[0] + "," + ftime;
                if (indStore.putIfAbsent(fCur + fInd, new CountDownLatch(1)) == null) {
                    if (!isCurr(fCur, fInd)) {
                        System.out.println(fCur + "" + fInd);
                        ShareList sl = getSL(fCur, "CUR");
                        storeInd(sl, buildIndicator(fInd));
                    }
                    indStore.get(fCur + fInd).countDown();
                }
                indStore.get(fCur + fInd).await();
            }
            
            Iterator<String> ftimeAfterItr=filterAfterTime.iterator();
            for(String filt:filtersAfter) {
                String fInd=buildIndicator(filt).getShortName();
                String ftime=ftimeAfterItr.next().split(",")[0];
                String fCur=cur.split(",")[0]+","+ftime;
                if (!indStore.contains(fCur + fInd)) {
                    indStore.put(fCur + fInd, new CountDownLatch(1));
                    if (!isCurr(fCur, fInd)) {
                        System.out.println(fCur + "" + fInd);
                        ShareList sl = getSL(fCur, "CUR");
                        storeInd(sl, buildIndicator(fInd));
                    }
                    indStore.get(fCur + fInd).countDown();
                }
                indStore.get(fCur + fInd).await();
            }
            indStore.get(cur + indicator).await();
            indStore.get(curCls + indicatorCls).await();
            
            TradeList openTL = null;
            if (!bsDB.isOpenAvailable(cur, indicator, ftp.getOpen(), ftp.getBuyBool())) {
                //System.out.println(curr+""+opens);
                try{
                    openTL = getCurrencyOpenTrades(cur, indicator, ftp.getOpen(), ftp.getBuyBool(), true);
                } catch(Exception ex){
                }
            }
            int cnt=1;
            while(!bsDB.isOpenAvailable(cur, ftp.getOpenid()))
            {
                //System.out.println(new Date()+" Sleep");
                Thread.sleep(100);
                cnt++;
                if(cnt>=5)
                    break;
            }
            openTL = getCurrencyOpenTrades(cur, ftp);
            
            TradeList closeTL = getCurrencyCloseTrades(curCls, indicatorCls, ftp.getClose(), ftp.getBuyBool(), false, openTL, ftp.getOpenid(), ftp.getCloseid());
            String[] clss = ftp.getClose().split(",");
            ShareList slc = null;
            if (clss[3].equals("1")) {
                slc = getSL(curCls, "CUR");
            } else if (clss[3].equals("0")) {
                String cur1m = curCls.split(",")[0] + ",1M";
                slc = getSL(cur1m, "CUR");
            }
            closeTL = getBarCut(closeTL, slc, ftp.getClose());
            closeTL = getTargetTrades(closeTL, slc, ftp.getStopLoss(), ftp.getProfitTarget(), ftp.getBuyBool());
            if (ftp.getTrdCount() > 0 && closeTL != null && closeTL.getSize() > 0) {
                closeTL = filterSingle(closeTL, ftp.getTrdCount());
            }
            ret=closeTL;
            if (sqlb) {
                bsDB.storeTrades(ret, ftp.getId());
            } else {
                //bsDB.storeTradesLocal(closeTL, id);
            }
        }
        
        
        return ret;
    }

    public TradeList buildCurrencyTrades(String curr, String indicator, String opens, String openFilter,String filterTime,String currCls,String indicatorCls, String close, boolean buy, int trdCount,int combCount,boolean sql) throws Exception {
        String idO=new StringBuilder(buy?"Buy":"Sell").append(";").append(curr.split(",")[1]).append(";").append(indicator).append(";").append(opens).append((openFilter!=null)?";"+openFilter+","+filterTime:";").toString();
        String idC=new StringBuilder(currCls.split(",")[1]).append(";").append(indicatorCls).append(";").append(close).toString();
        String id=idO + ";" + idC+";"+trdCount+";"+combCount;
        TradeList closeTL = null;
        if (bsDB.isCloseAvailable(id, curr,sql)) {
            Date d11 = new Date();
            closeTL = getCurrencyCloseTrades(id, curr,sql);
        } else {
            synchronized (indList) {
                if (!indList.contains(curr + indicator)) {
                    indList.put(curr + indicator, null);
                    System.out.println(curr + "" + opens);
                    if (!isCurr(curr, indicator)) {
                        ShareList sl = getSL(curr, "CUR");
                        storeInd(sl, buildIndicator(indicator));
                    }
                }
            }

            synchronized (indList) {
                if (!indList.contains(currCls + indicatorCls)) {
                    indList.put(currCls + indicatorCls, null);
                    if (!isCurr(currCls, indicatorCls)) {
                        ShareList slc = getSL(currCls, "CUR");
                        storeInd(slc, buildIndicator(indicatorCls));
                    }
                }
            }

            synchronized (indList) {
                String[] cbreak = curr.split(",");
                String cur = curr;
                String[] ftb=filterTime.split(",");
                if (!cbreak[1].equals(ftb[0])) {
                    cur = cbreak[0] + "," + ftb[0];
                }
                if(openFilter!=null && openFilter.contains("|")){
                    String fils[]=openFilter.split("\\|");
                    for(String fil : fils) {
                        if (fil != null && !indList.contains(cur + fil)) {
                            indList.put(cur + fil, null);
                            if (!isCurr(cur, fil)) {
                                ShareList sli = getSL(cur, "CUR");
                                storeInd(sli, buildIndicator(fil));
                            }
                        }
                    }
                } else {
                    if (openFilter != null && !indList.contains(cur + openFilter)) {
                        indList.put(cur + openFilter, null);
                        if (!isCurr(cur, openFilter)) {
                            ShareList sli = getSL(cur, "CUR");
                            storeInd(sli, buildIndicator(openFilter));
                        }
                    }
                }
            }

            TradeList openTL = null;
            if (!bsDB.isOpenAvailable(curr, indicator, opens, buy)) {
                //System.out.println(curr+""+opens);
                try{
                    openTL = getCurrencyOpenTrades(curr, indicator, opens, buy, true);
                } catch(Exception ex){
                    
                }
            }
            openTL = getCurrencyOpenTrades(curr, indicator, opens, buy, filterTime, openFilter);

            boolean change = false;
            if (!bsDB.isCloseAvailable(id, curr,sql) && openTL.getSize() > 0) {
                closeTL = getCurrencyCloseTrades(currCls, indicatorCls, close, buy, false, openTL, idO, idC);
                String[] clss = close.split(",");
                ShareList slc = null;
                if (clss[3].equals("1")) {
                    slc = getSL(currCls, "CUR");
                } else if (clss[3].equals("0")) {
                    String cur1m = currCls.split(",")[0] + ",1M";
                    slc = getSL(cur1m, "CUR");
                }
                closeTL = getBarCut(closeTL, slc, close);
                closeTL = getTargetTrades(closeTL, slc, close, buy);
                change = true;
            } else if (openTL.getSize() > 0) {
                closeTL = getCurrencyCloseTrades(id, currCls,sql);
            }
            if (trdCount > 0 && closeTL!=null && closeTL.getSize() > 0) {
                closeTL = filterSingle(closeTL, trdCount);
                if (sql) {
                    bsDB.storeTrades(closeTL, id);
                } else {
                    //bsDB.storeTradesLocal(closeTL, id);
                }
            } else if (change) {
                if (sql) {
                    bsDB.storeTrades(closeTL, id);
                } else {
                    //bsDB.storeTradesLocal(closeTL, id);
                }
            }
        }
//        Summary sum=new Summary(id, 10000);
//        sum.setBuy(buy?1:0);
//        if(closeTL!=null && closeTL.getSize()>0)
//            sum.calculateTrades(closeTL, getSL(curr, "CUR"), 1, this, true);
//        sum.setShare(curr);
        return closeTL;
        
    }
    
    public AbstractIndicator buildIndicator(String indic) {
        AbstractIndicator ind=null;
        String[] spl = indic.split(",");
        HashMap hm=new HashMap();
        for(int i=1;i<spl.length;i++){
            if(spl[i].contains("."))
                hm.put(i, Double.parseDouble(spl[i]));
            else
                hm.put(i, Integer.parseInt(spl[i]));
        }
        if (spl[0].equals("RSI")) {
            RsiIndicator rs = new RsiIndicator();
            rs.init(hm);
            ind = rs;
        } else if (spl[0].equals("Trix")) {
            TrixIndicator tr = new TrixIndicator();
            tr.init(hm);
            ind = tr;
        } else if (spl[0].equals("Inertia")) {
            InertiaIndicator inr = new InertiaIndicator();
            inr.init(hm);
            ind = inr;
        } else if (spl[0].equals("PVI")) {
            PviIndicator pvi = new PviIndicator();
            pvi.init(hm);
            ind = pvi;
        } else if (spl[0].equals("NVI")) {
            NviIndicator nvi = new NviIndicator();
            nvi.init(hm);
            ind = nvi;
        } else if (spl[0].equals("MACD")) {
            MacdIndicator macd = new MacdIndicator();
            macd.init(hm);
            ind = macd;
        } else if (spl[0].equals("Trend")) {
            TrendIndicator trnd = new TrendIndicator();
            trnd.init(hm);
            ind = trnd;
        } else if (spl[0].equals("VerticalHorizontal")) {
            VerticalHorizontalIndicator vh = new VerticalHorizontalIndicator();
            vh.init(hm);
            ind = vh;
        } else if (spl[0].equals("DMI")) {
            DmiIndicator dmi = new DmiIndicator();
            dmi.init(hm);
            ind = dmi;
        } else if (spl[0].equals("RMI")) {
            RmiIndicator rmi = new RmiIndicator();
            rmi.init(hm);
            ind = rmi;
        } else if (spl[0].equals("ForcastOscillator")) {
            ForcastOscillatorIndicator fo = new ForcastOscillatorIndicator();
            fo.init(hm);
            ind = fo;
        } else if (spl[0].equals("MoneyFlowIndex")) {
            MfiIndicator mfi = new MfiIndicator();
            mfi.init(hm);
            ind = mfi;
        } else if (spl[0].equals("PVT")) {
            PvtIndicator pvt = new PvtIndicator();
            pvt.init();
            ind = pvt;
        } else if (spl[0].equals("DP Oscillator")) {
            DpOscillatorIndicator dp = new DpOscillatorIndicator();
            dp.init(hm);
            ind = dp;
        } else if (spl[0].equals("RVI")) {
            RviIndicator rvi = new RviIndicator();
            rvi.init(hm);
            ind = rvi;
        } else if (spl[0].equals("SMA")) {
            MaIndicator sma = new MaIndicator();
            if (!hm.isEmpty()) {
                hm.put(2, MA.Simple);
            }
            sma.init(hm);
            ind = sma;
        } else if (spl[0].equals("EMA")) {
            MaIndicator ema = new MaIndicator();
            if (!hm.isEmpty()) {
                hm.put(2, MA.Exponential);
            }
            ema.init(hm);
            ind = ema;
        } else if (spl[0].equals("WMA")) {
            MaIndicator ema = new MaIndicator();
            if (!hm.isEmpty()) {
                hm.put(2, MA.Weighted);
            }
            ema.init(hm);
            ind = ema;
        } else if (spl[0].equals("Range")) {
            RangeIndicator range = new RangeIndicator();
            range.init(hm);
            ind = range;
        } else if (spl[0].equals("TEMA")) {
            TemaIndicator tema = new TemaIndicator();
            tema.init(hm);
            ind = tema;
        } else if (spl[0].equals("Alpha")) {
            AlphaIndicator alpha = new AlphaIndicator();
            alpha.init();
            ind = alpha;
        } else if (spl[0].equals("ATR")) {
            AtrIndicator atr = new AtrIndicator();
            atr.init(hm);
            ind = atr;
        } else if (spl[0].equals("PinBar") || spl[0].equals("P") ) {
            PinBarIndicator atr = new PinBarIndicator();
            atr.init(hm);
            ind = atr;
        } else if (spl[0].equals("Fibonacci") || spl[0].equals("Fib") ) {
            FibIndicator atr = new FibIndicator();
            atr.init(hm);
            ind = atr;
        } else if (spl[0].equals("DaysBack") || spl[0].equals("DB") || spl[0].equals("DAYSBACK")  ) {
            DaysBackIndicator db=new DaysBackIndicator();
            db.init(hm);
            ind = db;
        } else if (spl[0].toUpperCase().equals("BETA") ){
            BetaIndicator bta=new BetaIndicator();
            bta.init(hm);
            ind=bta;
        }else if (spl[0].toUpperCase().equals("DAYUP") ){
            DayUpIndicator bta=new DayUpIndicator();
            bta.init(hm);
            ind=bta;
        }else if (spl[0].toUpperCase().equals("DAYDOWN") ){
            DayDownIndicator bta=new DayDownIndicator();
            bta.init(hm);
            ind=bta;
        }else if (spl[0].toUpperCase().equals("VOLATILITY") ){
            VolatilityIndicator vta=new VolatilityIndicator();
            vta.init(hm);
            ind=vta;
        }else if (spl[0].toUpperCase().equals("YEAR") ){
            YearHLIndicator yr=new YearHLIndicator();
            yr.init(hm);
            ind=yr;
        }else if (spl[0].toUpperCase().equals("BULLBEAR") ){
            BullishBearishIndicator bb=new BullishBearishIndicator();
            bb.init(hm);
            ind=bb;
        }else if (spl[0].toUpperCase().equals("RANKING") ){
            RankingIndicator rnk=new RankingIndicator();
            rnk.init(hm);
            ind=rnk;
        } else {
            throw null;
        }
        if(ind.getParamCount()>hm.size())
        {
            hm.put(hm.size()+1, 1);
        }
        return ind;
    }

    private boolean isOpenStored(String curr, String indicator, String opens,boolean buy) throws Exception {
        return bsDB.isOpenAvailable(curr,indicator,opens,buy);
    }

    private void storeCurrencyTrades(TradeList openTL, String curr, String indicator, String opens, boolean buy) throws Exception {
        String id=(buy?"Buy":"Sell") +";"+curr.split(",")[1]+";"+indicator+";"+opens;
        bsDB.storeTrades(openTL,id);
    }

    private TradeList getBarCut(TradeList tl, ShareList sl, String close) {
        String[] cl=close.split(",");
        int dcut=Integer.parseInt(cl[2]);
        for(int i=0;i<tl.getSize();i++){
            Trade trd=tl.getTrade(i);
            int start=sl.isDatePresent(trd.getStartDate());
            if(start==-1)
                start=sl.isLowerDatePresent(trd.getStartDate());
            if (trd.getCloseDate() != null) {
                int end = sl.isDatePresent(trd.getCloseDate());
                if(end==-1)
                    end = sl.isLowerDatePresent(trd.getCloseDate());
                if (end - start > dcut && start + dcut < sl.getSize()) {
                    ShareData sd = sl.getSharedata(start + dcut);
                    trd.setCloseDate(sd.getDate());
                    trd.setClosePrice(sd.getClosePrice());
                }
            } else if(start + dcut < sl.getSize()){
                ShareData sd = sl.getSharedata(start + dcut);
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
            } else{
                ShareData sd = sl.getSharedata(sl.getSize()-1);
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
            }
        }
        return tl;
    }

    private TradeList getTargetTrades(TradeList tl, ShareList sl, String close,boolean buy) {
        String[] cl=close.split(",");
        double target =Double.parseDouble(cl[4]);
        double stopl =Double.parseDouble(cl[5])*-1;
        for(int i=0;i<tl.getSize();i++){
            Trade trd=tl.getTrade(i);
            int start=sl.isDatePresent(trd.getStartDate());
            if(start==-1)
            {
                start=sl.isLowerDatePresent(trd.getStartDate());
                if(start==-1)
                    start=sl.isHigherDatePresent(trd.getStartDate());
            }
            if (trd.getCloseDate() != null) {
                int end = sl.isDatePresent(trd.getCloseDate());
                for(int st=start+1;st<end;st++){
                    ShareData sd = sl.getSharedata(st);
                    double profit=(sd.getClosePrice()-trd.getStartPrice())*100.0/sd.getClosePrice();
                    if(!buy)
                        profit*=-1;
                    if(profit>=target){
                        trd.setCloseDate(sd.getDate());
                        trd.setClosePrice(sd.getClosePrice());
                        break;
                    } else if(profit<=stopl*-1){
                        trd.setCloseDate(sd.getDate());
                        trd.setClosePrice(sd.getClosePrice());
                        break;
                    }
                }
            }
        }
        return tl;
    }
    
    private TradeList getTargetTrades(TradeList tl, ShareList sl, double stopl,double target,boolean buy) {
        for(int i=0;i<tl.getSize();i++){
            Trade trd=tl.getTrade(i);
            int start=sl.isDatePresent(trd.getStartDate());
            if(start==-1)
            {
                start=sl.isLowerDatePresent(trd.getStartDate());
                if(start==-1)
                    start=sl.isHigherDatePresent(trd.getStartDate());
            }
            if (trd.getCloseDate() != null) {
                int end = sl.isDatePresent(trd.getCloseDate());
                for(int st=start+1;st<end;st++){
                    ShareData sd = sl.getSharedata(st);
                    double profit=(sd.getClosePrice()-trd.getStartPrice())*100.0/sd.getClosePrice();
                    if(!buy)
                        profit*=-1;
                    if(profit>=target){
                        trd.setCloseDate(sd.getDate());
                        trd.setClosePrice(sd.getClosePrice());
                        break;
                    } else if(profit<=stopl*-1){
                        trd.setCloseDate(sd.getDate());
                        trd.setClosePrice(sd.getClosePrice());
                        break;
                    }
                }
            }
        }
        return tl;
    }

    public TradeList getCurrencyCloseTrades(String id,String cur,boolean sql) throws Exception {
        if(sql)
            return bsDB.getCurrencyTrades(id,cur);
        else 
            return bsDB.getCurrencyTradesLocal(id,cur);
    }
    
    public void storeTrades(TradeList closeTL,String id,String cur) throws Exception{
        bsDB.storeTrades(closeTL, id,cur);
    }

    public ArrayList<String> getShareOnDateALL(ShareList sl, Date dd, int backPeriod, boolean sell) throws Exception
    {
        return shareDB.getShareOnDateALL(sl, dd, backPeriod, sell);
    }
    
    public HashMap<Date,ArrayList<String>> getShareOnALL(ShareList sl, int backPeriod, boolean sell) throws Exception
    {
        return shareDB.getShareOnALL(sl, backPeriod, sell);
    }
    
    public void ProfiTarget(TradeList tl,double target,TradeParameters tp) throws Exception{
        for(int i=0;i<tl.getSize();i++){
            Trade trd=tl.getTrade(i);
            ShareList sl=getSL(trd.getShare(), tp.getIndex());
            trd=getTradeProfitTarget(trd,target,sl,tp);
        }
    }
    
    public void profiTrail(TradeList tl,double target,double sloss,TradeParameters tp) throws Exception{
        for(int i=0;i<tl.getSize();i++){
            Trade trd=tl.getTrade(i);
            ShareList sl=getSL(trd.getShare(), tp.getIndex());
            trd=getTradeProfitDrop(trd,target,sloss,sl,tp);
        }
    }
    
    public void profiTrail(TradeList tl,double sloss,int days,TradeParameters tp) throws Exception{
        for(int i=0;i<tl.getSize();i++){
            Trade trd=tl.getTrade(i);
            ShareList sl=getSL(trd.getShare(), tp.getIndex());
            trd=getTradeProfitTrail(trd,sloss,days,sl,tp);
        }
    }

    private void cutDays(TradeList tlc, int days,ShareList indsl) {
        
        for(int i=0;i<tlc.getSize();i++){
            Trade trd=tlc.getTrade(i);
            int start=indsl.isDatePresent(trd.getStartDate());
            int end=indsl.isDatePresent(trd.getCloseDate());
            if(end-start>days){
                int endnew=start+days;
                ShareData sd=indsl.getSharedata(endnew);
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
            }
        }
    }
    
    public void cutDays(TradeList tlc, int days,ShareList indsl, TradeParameters tp) throws Exception {
        
        for(int i=0;i<tlc.getSize();i++){
            Trade trd=tlc.getTrade(i);
            int start=indsl.isDatePresent(trd.getStartDate());
            int end=indsl.isDatePresent(trd.getCloseDate());
            if(end-start>days){
                int endnew=start+days;
                ShareData sd=indsl.getSharedata(endnew);
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
                calcDividendTrade(trd, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
            }
        }
    }

    public Trade getTradeProfitTarget(Trade trd, double target, ShareList sl,TradeParameters tp) throws Exception {
        int start = sl.isDatePresent(trd.getStartDate());
        int end = sl.isDatePresent(trd.getCloseDate());
        for (int i = start+1; i < end; i++) {
            int endnew = i;
            ShareData sd = sl.getSharedata(endnew);
            Trade trdnew=new Trade(trd.getStartDate(), trd.getStartPrice(), sd.getDate(), sd.getClosePrice());
            trdnew.setShare(trd.getShare());
            trdnew=calcDividendTrade(trdnew, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
            if(trdnew.getProfit()>=target){
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
                calcDividendTrade(trd, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
                break;
            }
        }
        return trd;
    }

    public Trade getTradeProfitDrop(Trade trd, double startProfit, double sloss, ShareList sl, TradeParameters tp) throws Exception {
        int start = sl.isDatePresent(trd.getStartDate());
        int end = sl.isDatePresent(trd.getCloseDate());
        double target=-1;
        for (int i = start+1; i < end; i++) {
            int endnew = i;
            ShareData sd = sl.getSharedata(endnew);
            Trade trdnew=new Trade(trd.getStartDate(), trd.getStartPrice(), sd.getDate(), sd.getClosePrice());
            trdnew.setShare(trd.getShare());
            trdnew=calcDividendTrade(trdnew, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
            if(target>-1 && trdnew.getProfit()<=target){
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
                calcDividendTrade(trd, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
                break;
            }
            if(trdnew.getProfit()>=startProfit){
                target=trdnew.getProfit()-trdnew.getProfit()*sloss/100;
            }
        }
        return trd;
    }
    
    public Trade getTradeProfitTrail(Trade trd, double sloss,int days, ShareList sl, TradeParameters tp) throws Exception {
        int start = sl.isDatePresent(trd.getStartDate());
        int end = sl.isDatePresent(trd.getCloseDate());
        double target=sloss*-10;
        for (int i = start+1; i < end; i++) {
            int endnew = i;
            ShareData sd = sl.getSharedata(endnew);
            Trade trdnew=new Trade(trd.getStartDate(), trd.getStartPrice(), sd.getDate(), sd.getClosePrice());
            trdnew.setShare(trd.getShare());
            trdnew=calcDividendTrade(trdnew, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
            if(trdnew.getProfit()-(sloss*10)>target)
                target=trdnew.getProfit()-(10*sloss);
            if(trdnew.getProfit()<=target){
                trd.setCloseDate(sd.getDate());
                trd.setClosePrice(sd.getClosePrice());
                calcDividendTrade(trd, tp.isBuy() ? 1 : 0, tp.getShareCount(), tp.getIndex());
                break;
            }
            
        }
        return trd;
    }
        
    
}
