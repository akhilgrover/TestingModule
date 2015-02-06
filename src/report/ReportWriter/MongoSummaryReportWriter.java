/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package report.ReportWriter;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.WriteException;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import report.Summary;
import report.SummaryGroup;

/**
 *
 * @author akhil
 */
public class MongoSummaryReportWriter implements ReportWriter {

    Jongo jongo;
    
    @Override
    public void close() throws InterruptedException, SQLException {
        jongo.getDatabase().getMongo().close();
    }

    @Override
    public void refresh() {
        try {
            close();
            open("");
        } catch (Exception ex) {
            Logger.getLogger(MongoSummaryReportWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void open(String file) throws Exception {
        MongoClient mongoClient = new MongoClient("pc1");
        com.mongodb.DB db = mongoClient.getDB("BackTrade");
        //DBCollection coll = db.getCollection("ShareList");
        jongo = new Jongo(db);
    }

    @Override
    public String writeSummFile(String file, List<Summary> summ) throws IOException, WriteException, SQLException, ClassNotFoundException {
        
        MongoCollection collJ = jongo.getCollection("Summary");
        collJ.withWriteConcern(WriteConcern.ERRORS_IGNORED);
//        GsonBuilder gsonb = new GsonBuilder();
//        gsonb.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
//            // Year in 4, month in 2, day in 2, hour in 24, minutes in hour, seconds in minute, timezone in 4  
//            final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");
//
//            @Override
//            public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
//                try {
//                    return df.parse(json.getAsString());
//                } catch (final java.text.ParseException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        });
//        gsonb.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
//            // Year in 4, month in 2, day in 2, hour in 24, minutes in hour, seconds in minute, timezone in 4  
//            final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");
//            @Override
//            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
//                return new JsonPrimitive(df.format(src));
//            }
//        });
//        Gson gson=gsonb.create();
        ArrayList<Summary> list=new ArrayList<>();
        String id="";
        for(Summary s:summ){
            if(collJ.count("{name:#,share:#}",s.getName(),s.getShare())==0)
            {
//                Date d=new Date();
                //String s2=gson.toJson(s);
                //BasicDBObject b=(BasicDBObject) JSON.parse(s2);
                //list.add(b);
//                Date d3=new Date();
                //jongo.getDatabase().getCollection("Summary");
                if(s instanceof SummaryGroup){
                    //collJ.insert((SummaryGroup)s);
                    if(s.getTProfit()<=0)
                        return "";
                    id=s.getId();
                    list.add(s);
                } else
                {
                    list.add(s);
                    //collJ.insert(s);
                }
                    
//                Date d4=new Date();
//                System.out.println(s2.length()+","+(d3.getTime()-d.getTime())+","+(d4.getTime()-d3.getTime()));
            }
            
        }
        collJ.insert(list.toArray());
        collJ = jongo.getCollection("ForexTrades");
        long cnts=collJ.count("{_id:#}",id);
        if(cnts==0){
            collJ.insert("{_id#,counts:#}",id,1);
        } else{
            collJ.update("{_id:#}",id).with("{{$inc: {counts: 1}}}");
        }
        //jongo.getDatabase().getCollection("Summary").insert(list);
        return "";
    }
    
}
