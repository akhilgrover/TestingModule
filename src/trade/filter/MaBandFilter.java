package trade.filter;

import Share.ShareList;
import gnu.trove.list.array.TDoubleArrayList;
import ma.*;
import java.util.*;
import javolution.util.FastTable;

public class MaBandFilter extends AbstractFilter
{
    /*transient private FastTable<Double> maClose;
    transient private FastTable<Double> maBand1Close;
    transient private FastTable<Double> maBand2Close;/*
     *
     */
    transient private TDoubleArrayList maClose;
    transient private TDoubleArrayList maBand1Close;
    transient private TDoubleArrayList maBand2Close;
    private MA ema;
    private MA ema1Band;
    private MA ema2Band;
    transient private ShareList sl;
    private int buy;
    private double range1Perc;
    private double range2Perc;
    private RsiChangeFilter rchange = null;
    private static final long serialVersionUID = -4441095523212287789L;

    public MaBandFilter()
    {
        this.paramCount=7;
        range1Perc=0.0;
        range2Perc=0.0;
    }

    public MaBandFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=5;
        int period=(Integer)params.get(1);
        int type=(Integer)params.get(8);
        ema=new MA(period,type);
        range1Perc = (Integer)params.get(2);
        int period2 =(Integer)params.get(3);
        ema1Band=new MA(period2,type);
        range2Perc = (Integer)params.get(4);
        int period3 =(Integer)params.get(5);
        ema2Band=new MA(period3,type);
        if ((Integer)param.get(6) > 1) {
            HashMap hm = new HashMap();
            hm.put(1, param.get(6));
            hm.put(2, param.get(7));
            rchange = new RsiChangeFilter(hm);
        }
    }

    @Override
    public void buildFilter(int buy, ShareList sl)
    {
        if(rchange!=null && ((Integer)rchange.getParams().get(1))>1)
            rchange.buildFilter(buy, sl);
        ema.refresh();
        ema1Band.refresh();
        ema2Band.refresh();
        if (maClose == null) {
            /*maClose = new ArrayList(sl.getSize());
            maBand1Close = new ArrayList(sl.getSize());
            maBand2Close = new ArrayList(sl.getSize());*/
//            maClose=FastTable.newInstance();
//            maBand1Close=FastTable.newInstance();
//            maBand2Close=FastTable.newInstance();
            /*maClose=new FastTable();
            maBand1Close=new FastTable();
            maBand2Close=new FastTable();

            maClose.setSize(sl.getSize());
            maBand1Close.setSize(sl.getSize());
            maBand2Close.setSize(sl.getSize());*/
            maClose = new TDoubleArrayList(sl.getSize());
            maBand1Close = new TDoubleArrayList(sl.getSize());
            maBand2Close = new TDoubleArrayList(sl.getSize());

        } else
        {
            maClose.clear(sl.getSize());
            maBand1Close.clear(sl.getSize());
            maBand2Close.clear(sl.getSize());
            /*FastTable.recycle(maClose);
            FastTable.recycle(maBand1Close);
            FastTable.recycle(maBand2Close);
            maClose.setSize(sl.getSize());
            maBand1Close.setSize(sl.getSize());
            maBand2Close.setSize(sl.getSize());*/

        }
        this.sl=sl;
        this.buy=buy;
        for(int i=0;i<sl.getSize();i++)
        {
            Share.ShareData sd=sl.getSharedata(i);
            double price=sd.getClosePrice();
            maClose.add(ema.next(price));
            maBand1Close.add(ema1Band.next(price));
            maBand2Close.add(ema2Band.next(price));
        }
    }


    @Override
    public boolean filterTrade(int shareIndex, int buy, ShareList slst)
    {

        if(maClose==null || maClose.size()==0 || sl==null){
            buildFilter(buy, slst);
        }
        if(!sl.getShare().equals(slst.getShare())){
            buildFilter(buy, slst);
            //sl=slst;
        }
        boolean ret=false;
        boolean ret1=true;
        boolean ret2=true;
        boolean ret3=true;
        double close=0,em=0;
        double emBand=0,emBand2;
        if(sl.getShare().equals(slst.getShare()) && sl.getSize()==slst.getSize())
        {
            if(shareIndex==-1 || shareIndex>=sl.getSize())
            {
                System.out.println(sl.getShare());
                return ret;
            }
            close=sl.getSharedata(shareIndex).getClosePrice();
            if(maClose.size()<=shareIndex)
            {
                buildFilter(buy, slst);
            }
            em=(Double)maClose.get(shareIndex);
            emBand=(Double)maBand1Close.get(shareIndex);
            emBand2=(Double)maBand2Close.get(shareIndex);
        }
        else
        {
            Date d=slst.getSharedata(shareIndex).getDate();
            int index=sl.isDatePresent(d);
            if(index==-1)
                index=sl.isLowerDatePresent(d);
            close=sl.getSharedata(index).getClosePrice();
            em=(Double)maClose.get(index);
            emBand=(Double)maBand1Close.get(index);
            emBand2=(Double)maBand2Close.get(index);
        }

        if (ema.getPeriod() >= 1) {
            if (this.buy == 1 && close < em) {
                ret1 = false;
            } else if (this.buy == 0 && close > em) {
                ret1 = false;
            }
        }
        if (range1Perc > 0) {
            double diff = (emBand - close) * 100 / emBand;
            if (this.buy == 1 && close < em) {
                ret2 = false;
            } else if (this.buy == 0 && close < emBand) {
                if (diff >= range1Perc && diff >= 0) {
                    ret2 = false;
                }
            }
        }
        if (range2Perc > 0) {
            if (this.buy == 0 && close > emBand2) {
                double diff1 = (close - emBand2) * 100 / emBand2;
                if(diff1<range2Perc && diff1>=0)
                    ret3=false;
            }
        }
        boolean ret4=true;
        if(rchange!=null && ((Integer)rchange.getParams().get(1))>1)
            ret4=rchange.filterTrade(shareIndex, buy, slst);
        ret=ret1 && ret2 && ret3 && ret4;
        return ret;
    }

    public void releaseSL()
    {
        sl=null;
        if(rchange!=null && ((Integer)rchange.getParams().get(1))>1)
            rchange.clear();
    }

    @Override
	public String toString()
	{
            if(name==null){
            int period=(Integer)params.get(1);
            int period2 =(Integer)params.get(3);
            int period3 =(Integer)params.get(5);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append("d Big ");
            buffer.append(range1Perc);
            buffer.append("% ");
            buffer.append(period2);
            buffer.append("d Below ");
            buffer.append(range2Perc);
            buffer.append("% ");
            buffer.append(period3);
            buffer.append("d Above ");
            if(rchange!=null && ((Integer)rchange.getParams().get(1))>1)
                buffer.append(rchange.toString());
            buffer.append(" Filter2 ");
            name=buffer.toString();
            }
            return name;//this.ema.toString();
	}


//    public int compareTo(AbstractFilter o) {
//        if(!(o instanceof MaFilter) || o==null)
//            return -1;
//        else
//        {
//            if(o.paramCount!=this.paramCount)
//                return -1;
//            else
//            {
//                int periodO=(Integer)o.params.get(1);
//                int typeO=(Integer)o.params.get(2);
//                int period=(Integer)params.get(1);
//                int type=(Integer)params.get(2);
//                if(typeO!=type)
//                    return -1;
//                else
//                    return period-periodO;
//            }
//        }
//    }

    @Override
    public void clear() {
        if (maClose != null) {
            int size = maClose.size();
            maClose.clear(size);
            maBand1Close.clear(size);
            maBand2Close.clear(size);
        }
        /*FastTable.recycle(maClose);
        FastTable.recycle(maBand1Close);
        FastTable.recycle(maBand2Close);*/
        releaseSL();
        /*if(rchange!=null && ((Integer)rchange.getParams().get(1))>1)
            rchange.clear();*/
    }
}