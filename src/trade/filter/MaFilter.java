package trade.filter;

import Share.ShareList;
import gnu.trove.list.array.TDoubleArrayList;
import ma.*;
import java.util.*;
import javolution.util.FastTable;

public class MaFilter extends AbstractFilter
{
    transient private TDoubleArrayList maClose;
    MA ema;
    transient ShareList sl;
    int buy;
    private static final long serialVersionUID = -4441095523212287789L;

    public MaFilter()
    {
        this.paramCount=1;
    }

    public MaFilter(HashMap param)
    {
        this.params=param;
        this.paramCount=1;
        int period=(Integer)params.get(1);
        int type=(Integer)params.get(2);
        ema=new MA(period,type);
    }

    @Override
    public void buildFilter(int buy, ShareList sl)
    {
        ema.refresh();
        if (maClose == null) {
            //maClose=FastTable.newInstance();
            //maClose=new FastTable();
            //maClose.setSize(sl.getSize());
            maClose=new TDoubleArrayList(sl.getSize());
        } else{
            //FastTable.recycle(maClose);
            //maClose.setSize(sl.getSize());
            maClose.clear(sl.getSize());
        }
        this.sl=sl;
        this.buy=buy;
        for(int i=0;i<sl.getSize();i++)
        {
            Share.ShareData sd=sl.getSharedata(i);
            try{
            maClose.add(ema.next(sd.getClosePrice()));
            }catch(Exception ex){
                System.out.println(i);
            }
        }
    }


    @Override
    public boolean filterTrade(int shareIndex, int buy, ShareList slst)
    {

        if(maClose==null || maClose.size()==0 || sl==null){
            buildFilter(buy, slst);
        }
        boolean ret=false;
        double close=0,em=0;
        if (sl.getShare().equals(slst.getShare()) && sl.getSize() > shareIndex)
        {
            Date d=slst.getSharedata(shareIndex).getDate();
            if(!d.equals(sl.getSharedata(shareIndex).getDate()))
                System.out.println("Error ShareList Date in Ma Filter");
            close=sl.getSharedata(shareIndex).getClosePrice();
            if(maClose.size()<=shareIndex)
            {
                buildFilter(buy, slst);
            }
            em=(Double)maClose.get(shareIndex);
        }
        else
        {
            Date d=slst.getSharedata(shareIndex).getDate();
            int index=sl.isDatePresent(d);
            if(index==-1)
                index=sl.isLowerDatePresent(d);
            close=sl.getSharedata(index).getClosePrice();
            if(maClose.size()<=index)
                buildFilter(buy, slst);
            em=(Double)maClose.get(index);
        }
        if(this.buy==1 && close>em)
        {
            ret=true;
        }
        else if(this.buy==0 && close<em)
        {
            ret=true;
        }
        return ret;
    }

    public void releaseSL()
    {
        sl=null;
    }

    @Override
    public String toString() {
        if (name == null) {
            int period = (Integer) params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(period);
            buffer.append("d Filter1 ");
            name = buffer.toString();//this.ema.toString();
        }
        return name;
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
        if(sl!=null)
            maClose.clear(sl.getSize());
        else if(maClose!=null)
            maClose.clear();
        releaseSL();
        //FastTable.recycle(maClose);
    }
}
