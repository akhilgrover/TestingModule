package indicator;

import gnu.trove.map.hash.TLongIntHashMap;
import java.io.Serializable;
import java.util.*;

public class IndicatorList implements Serializable {

    private static final long serialVersionUID = 1L;

	private ArrayList<IndicatorField> indList;
        //private FastTable<IndicatorField> indList;
        //private TreeMap<Long,Integer> dDate;
        private TLongIntHashMap dDate;

	/**
	 * Method IndicatorList
	 *
	 *
	 */
	public IndicatorList() {

            //indList=new FastTable<IndicatorField>(4000);
            indList=new ArrayList<IndicatorField>(5000);
            //dDate=new TreeMap<Long, Integer>();
            //dDate = new TLongIntHashMap(5000);
            dDate = new TLongIntHashMap(5000);
	}

        public IndicatorList(int count) {

            indList=new ArrayList<IndicatorField>(count);
            //indList=new FastTable<IndicatorField>(count);
            //dDate=new TreeMap<Long, Integer>();
            dDate = new TLongIntHashMap(count);
	}

	public void addIndField(IndicatorField indf)
	{
            indList.add(indf);
            dDate.put(indf.getDDateLong(), indList.size()-1);
	}

	public IndicatorField getSharedata(int i)
	{
            return indList.get(i);
	}

	public int getSize()
	{
            return indList.size();
	}

	public int isDatePresent(Date dd)
	{
            int ret=-1;
            int d=dDate.get(dd.getTime());
            //if(d!=null)
                ret=d;
            return ret;
	}
        
        public int isDatePresent(long dd)
	{
            int ret=-1;
            int d=dDate.get(dd);
            //if(d!=null)
                ret=d;
            return ret;
	}

        /**
         *
         * @return
         */
        public Iterator<IndicatorField> getIterator()
        {
            return indList.iterator();
        }
/*
        public IndicatorList filterRange(Date start, Date end,double threshold,int skips,boolean sell)
        {
            NavigableMap<Long, Integer> m=dDate.subMap(start.getTime(), false, end.getTime(), false);
            IndicatorList il=new IndicatorList();
            for(Long d:m.keySet())
            {
                IndicatorField indF=indList.get(m.get(d));
                IndicatorField indFL=indList.get(dDate.lowerEntry(d).getValue());
                if(!sell && indF.getValue()>=threshold && indFL.getValue()<threshold)
                {
                    il.addIndField(indF);
                    if(il.getSize()==skips)
                        break;
                }
                else if(sell && indF.getValue()<=threshold && indFL.getValue()<threshold)
                {
                    il.addIndField(indF);
                    if(il.getSize()==skips)
                        break;
                }
            }
            return il;
        }*/

}
