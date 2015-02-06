/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade;

import java.util.Date;

/**
 *
 * @author Admin
 */
public class TradeProfit {

	Date trdDate;
	double profit;

	/**
	 * Method Trade
	 *
	 *
	 */
	public TradeProfit(Date trdD, double prft)
	{

		this.trdDate = trdD;
		this.profit = prft;
	}


	public void setTradeDate(Date trdDate) {
		this.trdDate = trdDate;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public Date getTradeDate() {
		return (this.trdDate);
	}

	public double getProfit() {
		return (this.profit);
	}



    /*@Override
	public String toString()
	{
		String sep = System.getProperty("line.separator");

		StringBuilder buffer = new StringBuilder();
		buffer.append(startDate);
		buffer.append("\t");
		buffer.append(startPrice);
		buffer.append("\t");
		buffer.append(closeDate);
		buffer.append("\t");
		buffer.append(closePrice);

		return buffer.toString();
	}*/


}
