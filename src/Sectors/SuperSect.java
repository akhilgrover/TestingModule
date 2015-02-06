package Sectors;

import java.util.HashMap;

public class SuperSect {

    private static final long serialVersionUID = 1111111111111111111L;
    private int counter=1;

    //<"Basic Resources", 1>
    //<"Financial Services", 2>
    private HashMap<String, Integer> hmSuperSect;

    //<"AAL", 1>
    //<"BARC", 2>
    //<"RBS", 2>
    private HashMap<String, Integer> hmShare;


    public SuperSect() {
        hmSuperSect = new HashMap<String, Integer>();
        hmShare = new HashMap<String, Integer>();
    }


    public void add(String superSect, String share) {
        Integer id = hmSuperSect.get(superSect);
        if(id==null){
            hmSuperSect.put(superSect, counter);
            id = counter;
            counter++;
        }
        hmShare.put(share, id);
    }

    public void clear() {
        hmShare.clear();
        hmSuperSect.clear();
    }


    public Integer getSectorId(String code){
        return hmShare.get(code);
    }


    public boolean isSameSuperSector(String share1, String share2) {
        Integer id1 = hmShare.get(share1);
        Integer id2 = hmShare.get(share2);
        if(id1==null || id2==null)
            return false;
        if(id1.intValue()==id2.intValue())
            return true;
        else
            return false;
    }


    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("SuperSect: ");
        buffer.append((char) 10);
        buffer.append("============");
        buffer.append((char) 10);
        for(String supersect : hmSuperSect.keySet()){
            buffer.append(supersect).append("\t").append(hmSuperSect.get(supersect)).append((char) 10);
        }
        buffer.append("------------");
        buffer.append((char) 10);
        for(String sh : hmShare.keySet()){
            buffer.append(sh).append("\t").append(hmShare.get(sh)).append((char) 10);
        }
        return buffer.toString();
    }
}
