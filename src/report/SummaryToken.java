/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package report;

import java.util.LinkedList;
import report.Grp;

/**
 *
 * @author Admin
 */
public class SummaryToken 
{
    private LinkedList data1;
    private volatile boolean available = false;
    
    public SummaryToken()
    {
        data1=new LinkedList();
    }
    
    public synchronized void put(Grp data) throws Exception
    {
        if(data1.size()>=400000)
        {
            while(data1.size()>1000)
            {
                wait();
            }
        }
        this.data1.add(data);
        available = true;
        notifyAll();
    }
    
    public synchronized Grp read() throws Exception
    {
            while ( !available  )
            {
                    try {
                            wait();
                    } catch(InterruptedException e){ }
            }
            Grp result=(Grp)data1.get(data1.size()-1);
            data1.remove(data1.size()-1);
            if(data1.size()==0)
            {
                    available = false;
            }
            notifyAll();
            return result;
    }
    
    public int getsize()
    {
        return data1.size();
    }

}
