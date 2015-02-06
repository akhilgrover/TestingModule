package indicator;

import java.sql.*;
import java.util.*;

public class IndicatorDB {
	
	private Connection con;
	private Statement stmt;
	
	/**
	 * Method IndicatorDB
	 *
	 *
	 */
	public IndicatorDB() throws ClassNotFoundException, SQLException 
	{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            con = DriverManager.getConnection("jdbc:odbc:BackTest","","");
            stmt=con.createStatement();
		
	}
	
	public boolean isPresent(String share, String ind, HashMap params) 
                throws SQLException
	{
            int count=0;
            String sql="SELECT Count(Indicator.code) AS CountOfcode FROM [Indicator] GROUP BY Indicator.code, Indicator.indicator,param1,param2,param3 HAVING (((Indicator.code)='"+share+"') AND ((Indicator.indicator)='"+ind+"') ";
            for(int i=1;i<=params.size();i++)
            {
                sql+="AND Param"+i+"=" + params.get(i) + " ";
            }
            sql+=");";
            ResultSet rs=stmt.executeQuery(sql);
            while(rs.next())
            {
                count=rs.getInt(1);
            }
            rs.close();
            return count>0;
	}
	
	public IndicatorList getDBIndicator(String share,String ind, HashMap params) throws SQLException
	{
            IndicatorField indf;
            IndicatorList indL=new IndicatorList();

            String sql="SELECT Indicator.date, Indicator.value, Indicator.signal FROM [Indicator] where (((Indicator.code)='"+share+"') AND ((Indicator.indicator)='"+ind+"') ";
            for(int i=1;i<=params.size();i++)
            {
                sql+="AND Param"+i+"=" + params.get(i) + " ";
            }
            sql+=") ORDER BY Indicator.date  ;";
            ResultSet rs=stmt.executeQuery(sql);
            while(rs.next())
            {
                indf=new IndicatorField(new java.sql.Date(rs.getDate(1).getTime()),rs.getDouble(2),rs.getDouble(3));
                indL.addIndField(indf);
            }
            rs.close();
            return indL;
	}
	
	public void setDBIndicator(String share,String ind, HashMap params,IndicatorList indL) throws SQLException
	{
            IndicatorField indf;
            for(int j=0;j<indL.getSize();j++)
            {
                indf=indL.getSharedata(j);
                String sql="insert into [Indicator] ( code, [date], [indicator], ";
                for(int i=1;i<=params.size();i++)
                {
                    sql+= "Param"+i+", ";
                }
                sql+=" [value], signal ) values ('"+share+"',#"+indf.getDDate()+"#,'"+ind+"',";
                for(int i=1;i<=params.size();i++)
                {
                    sql+= params.get(i)+", ";
                }
                //if(indf.getValue()>0 && indf.getValue()<100)
                sql+=indf.getValue()+", " + indf.getSignal() +");";
                //else
                //    sql+="0, 0);";
                try{
                stmt.executeUpdate(sql);
                }
                catch (Exception ex)
                {
                    System.out.println(ex.toString() + sql);
                }
            }
	}
	
}
