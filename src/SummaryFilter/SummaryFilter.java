/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SummaryFilter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import report.Summary;

/**
 *
 * @author admin
 */
public class SummaryFilter implements Serializable
{
    LinkedList<BasicFilter> filters;

    public SummaryFilter() {
        filters=new LinkedList<BasicFilter>();
    }

    public void addFilter(BasicFilter filter)
    {
        filters.add(filter);
    }

    public Collection<Summary> filter(Collection<Summary> list)
    {
        LinkedList<Summary> retList=new LinkedList<Summary>();
        Iterator<Summary> itr=list.iterator();
        while(itr.hasNext())
        {
            Summary sum=itr.next();
            boolean add=false;
            for(BasicFilter bf:filters)
            {
                if(bf.isValid(sum))
                    add=true;
            }
            if(add)
                retList.add(sum);
        }
        return retList;
    }

}
