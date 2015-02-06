/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indicator;

import Share.*;
import java.util.HashMap;

/**
 *
 * @author Admin
 */
public class AlphaIndicator extends AbstractIndicator 
{

    @Override
    public void init() 
    {
        this.name="Alpha Indicator";
        this.params=new HashMap();
        params.put(1, 1);
        this.paramCount=0;
    }

    @Override
    public void init(HashMap param) 
    {
        this.name="Alpha Indicator";
        this.params=param;
        this.paramCount=0;
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) 
    {
	int positive = 0;
	int negetive = 0;
	int current,ignore=0;
	int day=0;
	int dayn=0;
        double pCount=0,pCountl=0;
        double nCount=0,nCountl=0;
        IndicatorList alpha=new IndicatorList(sl.getSize()); 
        
	if(sl.getSharedata(0).getClosePrice() > sl.getSharedata(1).getClosePrice() )
	{
            current = 0;
            pCount=0;
            pCountl=0;
            nCount=1;
            nCountl=1;
	}
	else
	{
            current=1;
            pCount=1;
            pCountl=1;
            nCount=0;
            nCountl=0;
		
	}

	for (int i=1; i<sl.getSize(); i++)
	{
            ShareData sd=sl.getSharedata(i);
            ShareData sdl=sl.getSharedata(i-1);
        
	
            if(current==1)//last positive
            {
                if(sd.getClosePrice()>sdl.getClosePrice())
                {
                    current=1;
                    if(positive<12)
                        positive++;
                    ignore=0;
                    pCount=positive;
                    nCount=0;
                }
                else if(sd.getClosePrice()==sdl.getClosePrice())
                {
                    current=1;
                    pCount=positive;
                    nCount=0;
                }
                else if(i>2 && ignore==0 && sd.getClosePrice()<sdl.getClosePrice() && sd.getClosePrice()>=sl.getSharedata(i-2).getClosePrice())
                {
                    current=1;
                    ignore++;
                    pCount=positive;
                    nCount=0;
                }
                else if(i>3 && ignore==1 && sd.getClosePrice()<sdl.getClosePrice() && sd.getClosePrice()<sl.getSharedata(i-2).getClosePrice() && sd.getClosePrice()>sl.getSharedata(i-3).getClosePrice())
                {
                    current=1;
                    ignore++;
                    pCount=positive;
                    nCount=0;
                }

                else if(i>4 && ignore==2 && sd.getClosePrice()<sdl.getClosePrice() && sd.getClosePrice()<sl.getSharedata(i-2).getClosePrice() && sd.getClosePrice()<sl.getSharedata(i-3).getClosePrice() && sd.getClosePrice()>sl.getSharedata(i-4).getClosePrice())
                {
                    current=1;
                    ignore++;
                    pCount=positive;
                    nCount=0;
                }
                else if(sd.getClosePrice()<sdl.getClosePrice() && sd.getClosePrice()<sl.getSharedata(i-2).getClosePrice() && i>2)
                {
                    current=0;			
                    negetive=1;
                    if(negetive+ignore<12)
                       negetive+=ignore;
                    else
                        negetive=12;
                    ignore=0;
                    positive=0;
                    nCount=negetive;
                    pCount=0;
                }
                else
                {
                    current=1;
                    ignore++;
                    pCount=positive;
                    nCount=0;
                }


            }

            else if(current==0) //last negative
            {
                if(sd.getClosePrice()<sdl.getClosePrice())
                {
                    current=0;
                    if(negetive<12)
                        negetive++;
                    nCount=negetive;
                    pCount=0;
                    ignore=0;
                }
                else if(sd.getClosePrice()==sdl.getClosePrice())
                {
                    current=0;
                    pCount=0;
                    nCount=negetive;

                }
                else if( sd.getClosePrice()>sdl.getClosePrice() && sd.getClosePrice()<=sl.getSharedata(i-2).getClosePrice() && ignore==0 && i>2)
                {
                    current=0;
                    ignore++;
                    nCount=negetive;
                    pCount=0;
                                //print("-1------->!"+negetive+ " " + data[i].date);

                }
                else if(i>3 && ignore==1 && sd.getClosePrice()>sdl.getClosePrice() && sd.getClosePrice()>sl.getSharedata(i-2).getClosePrice() && sd.getClosePrice()<sl.getSharedata(i-3).getClosePrice())
                {
                    current=0;
                    ignore++;
                    nCount=negetive;
                    pCount=0;
                                //print("-2------->!"+negetive+ " " + data[i].date);
                }
                else if(i>4 && ignore==2 && sd.getClosePrice()>sdl.getClosePrice() && sd.getClosePrice()>sl.getSharedata(i-2).getClosePrice() && sd.getClosePrice()>sl.getSharedata(i-3).getClosePrice() && sd.getClosePrice()<sl.getSharedata(i-4).getClosePrice())
                {
                    current=0;
                    ignore++;
                    nCount=negetive;
                    pCount=0;
                                //print("-3------->!"+negetive+ " " + data[i].date);
                }



                else if(sd.getClosePrice()>sdl.getClosePrice() && sd.getClosePrice()>sl.getSharedata(i-2).getClosePrice() && i>2)
                {
                    current=1;
                    positive=1;
                    if(positive+ignore<12)
                        positive+=ignore;
                    else
                        positive=12;
                    ignore=0;
                    negetive=0;
                    pCount=positive;
                    nCount=0;
                    //print("POSITIVE"+ignore+ " " + data[i].date);


                }
                else
                {
                    current=0;
                    ignore++;
                    nCount=negetive;
                    pCount=0;
                }

                    //print(current + " " + data[i].date );
            }
				
		
            //Positive side Modification Calculation	
	
            if(pCountl >=3 && pCount==0 && day==0)
                    day=1;
            if(day==1)
            {
                pCount=(pCountl*0.75);
                day++;
            }
            else if(day==2)
            {
                pCount=(pCountl*0.75);
                day++;
            }	
            else if(day==3)
            {
                pCount=(pCountl*0.75);
                day++;
            }
            else if(day==4)
            {
                pCount=(pCountl*0.5);
                day++;
            }
            else
                day=0;
	
	
            //Negative side Modification Calculation

            if(nCountl >=3 && nCount==0 && dayn==0)
                dayn=1;
            if(dayn==1)
            {
                nCount=(nCountl*0.75);
                dayn++;
            }
            else if(dayn==2)
            {
                nCount=(nCountl*0.75);
                dayn++;
            }	
            else if(dayn==3)
            {
                nCount=(nCountl*0.75);
                dayn++;
            }
            else if(dayn==4)
            {
                nCount=(nCountl*0.5);
                dayn++;
            }
            else
                dayn=0;

            double alp=(pCount-nCount);
            IndicatorField indF=new IndicatorField(sd.getDate(), alp, 0);
            alpha.addIndField(indF);
            pCountl=pCount;
            nCountl=nCount;
            
        }
	return alpha;
    }
    
    @Override
	public String toString()
	{
            StringBuilder buffer = new StringBuilder();
            buffer.append(name);
            buffer.append(" ");

            return buffer.toString();
	}
}

