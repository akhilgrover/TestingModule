package Share;

import gnu.trove.list.array.TLongArrayList;
import java.io.Serializable;
import java.util.*;
import javolution.util.*;

public class ShareList implements Serializable
{
    private static final long serialVersionUID = 7260688629951574589L;

    //private ArrayList<ShareData> shd;
    private FastTable<ShareData> shd;
    //private LinkedHashMap<Long,Integer> dDate;
    private TLongArrayList dDate;
    //private ArrayList<Long> dDate;
    private String Share;
    private String _id;
    /**
     * Method ShareList
     *
     *
     */
    public ShareList() {
        //shd=new ArrayList<ShareData>(5000);
        //shd=new LinkedList<ShareData>();
        shd=new FastTable<ShareData>(5000);
        //dDate=new LinkedHashMap<Long, Integer> (5000);
        dDate=new TLongArrayList(5000, -1);
    }

    public ShareList(int count,String share) {
        //shd=new ArrayList<ShareData>(count);
        //shd=new LinkedList<ShareData>();
        shd=new FastTable<ShareData>(count);
        //dDate=new LinkedHashMap<Long, Integer>(count);
        dDate=new TLongArrayList(count);
        this.Share=share;
    }

    public void addShareData(ShareData sd)
    {
        shd.add(sd);
        dDate.add(sd.getDateLong());
    }

    public ShareData getSharedata(int i)
    {
        return shd.get(i);
    }

    public synchronized void updateSharedata(ShareData sd)
    {
        if(dDate.contains(sd.getDateLong()))
        {
            int ind=dDate.indexOf(sd.getDateLong());
            //shd.set(ind, sd);
            ShareData old=shd.get(ind);
            if(old==null)
                addShareData(sd);
            else if(old.getDate().equals(sd.getDate()))
            {
                old.setClosePrice(sd.getClosePrice());
            }
        }
        else
        {
            addShareData(sd);
        }
    }

    public int getSize()
    {
        return shd.size();
    }

    public String getShare()
    {
        return Share;
    }

    public int isDatePresent(Date dd)
    {
        int ret=-1;
        long ldd=dd.getTime();
        int d=dDate.binarySearch(ldd);
        if(d>-1)
            return d;
        return ret;
    }
    
    public int isDatePresent(Long dd)
    {
        int ret=-1;
        int d=dDate.binarySearch(dd);
        if(d>-1)
            return d;
        return ret;
    }

    public int isLowerDatePresent(Date dd)
    {
        int ret=-1;
        //Long d=dDate.lowerKey(dd.getTime());
        long ldd=dd.getTime();
        int d=dDate.binarySearch(ldd);
//        if(d==-1 && dDate.get(dDate.size())<ldd)
//            ldd=dDate.size()-1;
        if(d>-1)
            return d-1;
        for(long l:dDate.toArray())
        {
            if(l<ldd)
                ret++;
            else break;
        }
        //if(d!=null)
            //ret=(Integer)dDate.get(d);
        return ret;
    }
    
    public int isLowerDatePresent(Long dd)
    {
        int ret=-1;
        //Long d=dDate.lowerKey(dd.getTime());
        int d=dDate.binarySearch(dd);
//        if(d==-1 && dDate.get(dDate.size())<ldd)
//            ldd=dDate.size()-1;
        if(d>-1)
            return d-1;
        for(long l:dDate.toArray())
        {
            if(l<dd)
                ret++;
            else break;
        }
        //if(d!=null)
            //ret=(Integer)dDate.get(d);
        return ret;
    }

    public int isHigherDatePresent(Date dd)
    {
        int ret=-1;
        long ldd=dd.getTime();
        int d=dDate.binarySearch(ldd);
        if(d>-1 && d<dDate.size())
            return d+1;
        for(long l:dDate.toArray())
        {
            ret++;
            if(l>ldd)
                break;
        }
        //Long d=dDate.higherKey(dd.getTime());
//        if(d!=null)
//            ret=(Integer)dDate.get(d);
        return ret;
    }

    public int isHigherDatePresent(Long dd)
    {
        int ret=-1;
        int d=dDate.binarySearch(dd);
        if(d>-1 && d<dDate.size())
            return d+1;
        for(long l:dDate.toArray())
        {
            ret++;
            if(l>dd)
                break;
        }
        //Long d=dDate.higherKey(dd.getTime());
//        if(d!=null)
//            ret=(Integer)dDate.get(d);
        return ret;
    }
    
    public ListIterator getIterator()
    {
        return shd.listIterator();
    }

    /**
     * @param Share the Share to set
     */
    public void setShare(String Share) {
        this.Share = Share;
    }

//    /**
//     *
//     * @throws Throwable
//     */
//    @Override
//    protected void finalize() throws Throwable {
//        dDate.clear();
//        shd.clear();
//        super.finalize();
//    }

    /**
     * @return the dDate
     */
    public SortedMap<Long,Integer> getdDate() {
        //SortedMap<Long,Integer> ret=new TreeMap<Long, Integer>(dDate);
        //return Collections.unmodifiableSortedMap(new TreeMap<Long, Integer>(dDate));
        TreeMap<Long,Integer> t=new TreeMap<Long, Integer>();
        int i=0;
        for(long l:dDate.toArray()){
            t.put(l, i++);
        }
        return t;
    }

    /**
     * @return the _id
     */
    public String getId() {
        return _id;
    }

    /**
     * @param _id the _id to set
     */
    public void setId(String _id) {
        this._id = _id;
    }


}
