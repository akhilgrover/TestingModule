/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import report.Summary;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 *
 * @author admin
 */
@SuppressWarnings("serial")
public class SummaryFilter implements Serializable
{
    private LinkedList<BasicFilter> filters;

    public SummaryFilter() {
        filters=new LinkedList<BasicFilter>();
    }

    public void addFilter(BasicFilter filter)
    {
        filters.add(filter);
    }

    public Collection<Summary> filter(Collection<Summary> list) throws Exception
    {
        LinkedList<Summary> retList=new LinkedList<Summary>();
        //Iterator<Summary> itr=list.iterator();
        if(list==null)
            return retList;
        for(Summary sum:list)
        {
            //Summary sum=itr.next();
            boolean add=true;
            for(BasicFilter bf:filters)
            {
                add=(add && bf.isValid(sum));
            }
            if(add)
            {
                retList.add(sum);
            }
        }
        //list.clear();
        return retList;
    }

    public Collection<Summary> filterLinked(Collection<Summary> list,int cnt) throws Exception
    {
        LinkedList<Summary> retList=new LinkedList<Summary>();
        //Iterator<Summary> itr=list.iterator();
        if(list==null)
            return retList;
        int i=0;
        ArrayList<Summary> tRet=new ArrayList<Summary>(cnt);
        for(Summary sum:list)
        {
            if(i%cnt==0 && i>0){
                if(tRet.size()==cnt){
                    retList.addAll(tRet);
                }
                tRet.clear();
            }
            boolean add=true;
            for(BasicFilter bf:filters)
            {
                add=(add && bf.isValid(sum));
            }
            if(add)
            {
                tRet.add(sum);
            }
            i++;
        }
        if (i % cnt == 0 && i > 0) {
            if (tRet.size() == cnt) {
                retList.addAll(tRet);
            }
            tRet.clear();
        }
        return retList;
    }

    @SuppressWarnings("unchecked")
    public static TreeMap group(LinkedList<Summary> list,String key) throws Exception
    {

        TreeMap<Object,LinkedList<Summary>> retList=new TreeMap<Object, LinkedList<Summary>>(new StringComparitor());
        Object o=null;
        for(Summary sum:list)
        {
            Class cls=sum.getClass();
            Method mtd=cls.getMethod(key);
            Object obj=mtd.invoke(sum);
            String k="0";
            if(obj!=null)
                k=obj.toString();
            if(retList.containsKey(k))
                retList.get(k).add(sum);
            else
            {
                LinkedList<Summary> nList=new LinkedList<Summary>();
                nList.add(sum);
                retList.put(k, nList);
            }
        }
        return retList;
    }

    @SuppressWarnings("unchecked")
    public static TreeMap group(LinkedList<Summary> list, String key, String key1) throws Exception
    {

        TreeMap<Object,LinkedList<Summary>> retList=new TreeMap<Object, LinkedList<Summary>>(new StringComparitor());
        for(Summary sum:list)
        {
            Class cls=sum.getClass();
            Method mtd=cls.getMethod(key);
            Object obj=mtd.invoke(sum);

            Method mtd1=cls.getMethod(key1);
            Object obj1=mtd1.invoke(sum);

            String k="0";
            if(obj!=null)
                k=obj.toString();
            if(obj1!=null)
                k+=","+obj1.toString();
            if(retList.containsKey(k))
                retList.get(k).add(sum);
            else
            {
                LinkedList<Summary> nList=new LinkedList<Summary>();
                nList.add(sum);
                retList.put(k, nList);
            }
        }
        return retList;
    }

    @SuppressWarnings("unchecked")
    public static TreeMap group(LinkedList<Summary> list, String key, String key1, String key2) throws Exception
    {


        TreeMap<Object,LinkedList<Summary>> retList=new TreeMap<Object, LinkedList<Summary>>(new StringComparitor());
        for(Summary sum:list)
        {
            Class cls=sum.getClass();
            Method mtd=cls.getMethod(key);
            Object obj=mtd.invoke(sum);

            Method mtd1=cls.getMethod(key1);
            Object obj1=mtd1.invoke(sum);

            Method mtd2=cls.getMethod(key2);
            Object obj2=mtd2.invoke(sum);

            String k="0";
            if(obj!=null)
                k=obj.toString();
            if(obj1!=null)
                k+=","+obj1.toString();
            if(obj2!=null)
                k+=","+obj2.toString();
            if(retList.containsKey(k))
                retList.get(k).add(sum);
            else
            {
                LinkedList<Summary> nList=new LinkedList<Summary>();
                nList.add(sum);
                retList.put(k, nList);
            }
        }
        return retList;
    }
}
