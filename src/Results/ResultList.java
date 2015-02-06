/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Results;

import java.util.Date;
import java.util.TreeMap;

/**
 *
 * @author gnisoft
 */
public class ResultList {

    private TreeMap<Date,ResultData> results;
    private char[] Share;

    public ResultList(String share) {
        results=new TreeMap<Date, ResultData>();
        Share=share.toCharArray();
    }

    public void addResult(ResultData rd){
        if(!results.containsKey(rd.getDate()))
            results.put(rd.getDate(), rd);
    }

    public void clear(){
        results.clear();
    }

    public void setShare(String sh){
        this.Share=sh.toCharArray();
    }

    public String getShare(){
        return new String(Share);
    }

    public int getSize(){
        return results.size();
    }

    public ResultData isDatePresent(Date d){
        return results.get(d);
    }

    public ResultData isHigherDatePresent(Date d){
        ResultData rd=null;
        Date dd=results.higherKey(d);
        if(dd!=null)
            rd=results.get(dd);
        return rd;
    }

    public ResultData isLowerDatePresent(Date d){
        ResultData rd=null;
        Date dd=results.lowerKey(d);
        if(dd!=null)
            rd=results.get(dd);
        return rd;
    }

    public void getResultRange(Date d1,Date d2){
        results.subMap(d1, false, d2, true);
    }

}
