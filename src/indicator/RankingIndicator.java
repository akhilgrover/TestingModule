/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.ShareData;
import Share.ShareList;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import trade.TradeCalculator;

/**
 *
 * @author Admin
 */
public class RankingIndicator extends AbstractIndicator
{
    private static final long serialVersionUID = 1L;
    private TradeCalculator tc=TradeCalculator.getInstance();
    private static final HashMap<String,HashMap<Integer,HashMap<Date,ArrayList<String>>>> ranks=new HashMap<>();
    

    public RankingIndicator()
    {
        super();
//        try {
//            shdb = new ShareListDB();
//        } catch (Exception ex) {
//            Logger.getLogger(RankingIndicator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public void init() {

        this.params=new HashMap();
        params.put(1, 225);
        this.name="Ranking";
        this.paramCount=1;
    }

    @Override
    public void init(HashMap param)
    {
        this.params=param;
        this.name="Ranking";
        this.paramCount=1;
    }
    
    @Override
    public IndicatorList buildIndicator(ShareList sl) {
        throw new UnsupportedOperationException("Not supported For Beta.");
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl,ShareList index)
    {
        IndicatorList ma=new IndicatorList(sl.getSize());
        
        int back=Integer.parseInt(params.get(1).toString());
        if (ranks.isEmpty()) {
            ranks.put(index.getShare(), new HashMap<Integer, HashMap<Date, ArrayList<String>>>());
        }
        if(!ranks.get(index.getShare()).containsKey(back)){
            HashMap<Integer,HashMap<String,ArrayList<String>>> n=new HashMap<>();
            HashMap<Date,ArrayList<String>> all=new HashMap<>();
            try {
                all = tc.getShareOnALL(index,back, false);
            } catch (Exception ex) {
                Logger.getLogger(RankingIndicator.class.getName()).log(Level.SEVERE, null, ex);
            }
            ranks.get(index.getShare()).put(back, all);
            if (all == null || all.isEmpty()) {
                Iterator itr = index.getIterator();
                for (int i = 0; i < index.getSize(); i++) {
                    try {
                        ShareData sd = (ShareData) itr.next();
                        ArrayList<String> shares = tc.getShareOnDateALL(index, sd.getDate(), back, false);
                        ranks.get(index.getShare()).get(back).put(sd.getDate(), shares);
                    } catch (Exception ex) {
                        Logger.getLogger(RankingIndicator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        Iterator itr=sl.getIterator();
	for(int i=0;i<sl.getSize();i++)
	{
            ShareData sd=(ShareData) itr.next();
            int rnk=0;
            ArrayList<String> rnks=ranks.get(index.getShare()).get(back).get(sd.getDate());
            if(rnks!=null)
                rnk=rnks.indexOf(sl.getShare())+1;
//            else
//                System.out.println(sd.getDate());
            //sldb.getShareOnDateReview(indsl, indsl.getSharedata(indsl.getSize() - i).getDate(), k, 1, sell);
            IndicatorField indF = new IndicatorField(sd.getDate(), rnk, 0);
            ma.addIndField(indF);
        }
        return ma;
    }

    @Override
    public String toString()
    {
            if(string==null){
            int param1=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            //buffer.append(name);
            //buffer.append(" ");
            buffer.append(param1);
            buffer.append("d Rank ");

            string= buffer.toString();
            }
            return string;
	}

}