package trade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;
import javolution.util.FastTable;

public class TradeList implements Serializable
{

    private static final long serialVersionUID = 1L;

	private FastTable<Trade> tList;
        private String name;
        private String ind;

	public TradeList()
	{
            tList=new FastTable<Trade> (20);
            //tList=new ArrayList<Trade> (20);
	}

        public TradeList(int cnt)
	{
            tList=new FastTable<Trade> (cnt);
            tList.trimToSize();
            //tList=new ArrayList<Trade> (20);
	}

	public void addTrade(Trade trd)
	{
            tList.add(trd);
	}

        public void addTrade(TradeList tl)
	{
            tList.addAll(tl.tList);
	}

        public void addTradeSorted(Trade trd)
	{
            int ind=0;
            for(Trade t:tList)
            {
                if(t.getStartDate().after(trd.getStartDate()))
                    break;
                ind++;
            }
            tList.add(ind, trd);
	}

	public Trade getTrade(int i)
	{
            return tList.get(i);
	}

	public int getSize()
	{
            return tList.size();
	}

        public Date closeDate()
        {
            TreeMap<Date,Integer> ret=new TreeMap<Date, Integer>();
            for(int i=0;i<tList.size();i++)
            {
                Date cls=tList.get(i).getCloseDate();
                ret.put(cls, i);
            }
            if(ret.size()>0)
            {
                Date d=ret.lastKey();
                ret.clear();
                return d;
            }
            else
                return null;
        }

        public Date openDate()
        {
            if(tList.size()>0)
                return tList.get(0).getStartDate();
            else
                return null;
        }

        public void sort(Comparator<Trade> c)
        {
            Collections.sort(tList,c);
        }

        public ArrayList<Trade> getTradesOpening(Date d)
        {
            ArrayList<Trade> ret=new ArrayList<Trade>();
            for(Trade trd:tList)
            {
                if(trd.getStartDate().compareTo(d)==0)
                    ret.add(trd);
            }
            return ret;
        }

        public void removeTrade(int index)
        {
            if(index<tList.size())
                tList.remove(index);
            return;
        }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the ind
     */
    public String getInd() {
        return ind;
    }

    /**
     * @param ind the ind to set
     */
    public void setInd(String ind) {
        this.ind = ind;
    }

}
