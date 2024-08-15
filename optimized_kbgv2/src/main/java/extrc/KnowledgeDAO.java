package extrc;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date; // This import is necessary for java.util.Date

public class KnowledgeDAO {
    private MongoCollection<Document> collection;

    public KnowledgeDAO() {
        MongoDatabase database = MongoDBConnection.getDatabase("KnowledgeDB");
        collection = database.getCollection("Knowledge");
    }

    //CRUD operations
    //creating...
    public Boolean createKnowledge(Knowledge knowledge) {
        try {
            List<Integer> connectList = Arrays.stream(knowledge.getConnectors())
                                        .boxed()
                                        .collect(Collectors.toList());

               Document doc = new Document("content", knowledge.getContent())
                .append("creationDate", knowledge.getCreationDate())
                .append("numberOfRanks", knowledge.getNumberOfRanks())
                .append("generatorName", knowledge.getGeneratorName())
                .append("generationSpeed", knowledge.getGenerationSpeed())
                .append("distribution", knowledge.getDistribution())
                .append("num",knowledge.getNum())
                .append("characterSet", knowledge.getCharSet())
                .append("connectors", connectList);
                
   
        collection.insertOne(doc);
        return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }
    
    public Boolean createKnowledgeOV2(Knowledge knowledge) {
        try { 
               List<Integer> connectList = Arrays.stream(knowledge.getConnectors())
                                          .boxed()
                                          .collect(Collectors.toList());
               Document doc = new Document("content", knowledge.getContent())
                .append("creationDate", knowledge.getCreationDate())
                .append("numberOfRanks", knowledge.getNumberOfRanks())
                .append("generatorName", knowledge.getGeneratorName())
                .append("generationSpeed", knowledge.getGenerationSpeed())
                .append("usedAtoms", knowledge.getUsedAtoms())
                .append("atomList", knowledge.getAtomList())
                .append("distribution", knowledge.getDistribution())
                .append("num",knowledge.getNum())
                .append("characterSet", knowledge.getCharSet())
                .append("connectors", connectList)
                .append("transitivity", knowledge.getTransitivity())
                .append("reuseConsequent", knowledge.getReuseConsequent());
    
                collection.insertOne(doc);
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    // reading...
    public Knowledge readKnowledge(String id) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            if (doc != null) {
                // Ensure types match the constructor parameters
                ArrayList<String> content = (ArrayList<String>) doc.getList("content",String.class);
                Date creationDate = doc.getDate("creationDate");
                int numberOfRanks = doc.getInteger("numberOfRanks");
                String generatorName = doc.getString("generatorName");
                double generationSpeed = doc.getDouble("generationSpeed");
                ArrayList<String>usedAtoms = (ArrayList<String>) doc.getList("usedAtoms",String.class);
                ArrayList<String> atomList = (ArrayList<String>) doc.getList("atomList",String.class);
                String distribution = doc.getString("distribution");
                int num = doc.getInteger("num");
                String charSet = doc.getString("characterSet");

                List<Integer> intList = doc.getList("connectors",Integer.class);
                int[] conType = intList.stream()
                    .mapToInt(Integer::intValue)
                    .toArray();
                String trans = doc.getString("transitivity");
                boolean reuseCons = doc.getBoolean("reuseConsequent");

                return new Knowledge(
                    content,
                    creationDate,
                    numberOfRanks,
                    generatorName,
                    generationSpeed,
                    usedAtoms,
                    atomList,
                    distribution,
                    num,
                    charSet,
                    conType,
                    trans,
                    reuseCons
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // updating...
    public Boolean updateKnowledgeOV2(String id, Knowledge knowledge) {
        try {
            List<Integer> connectList = Arrays.stream(knowledge.getConnectors())
                                              .boxed()
                                              .collect(Collectors.toList());
            Bson filter = Filters.eq("_id", new ObjectId(id));
            Bson update = Updates.combine(
                Updates.set("content", knowledge.getContent()),
                Updates.set("creationDate", knowledge.getCreationDate()),
                Updates.set("numberOfRanks", knowledge.getNumberOfRanks()),
                Updates.set("generatorName", knowledge.getGeneratorName()),
                Updates.set("generationSpeed", knowledge.getGenerationSpeed()),
                Updates.set("usedAtoms", knowledge.getUsedAtoms()),
                Updates.set("atomList", knowledge.getAtomList()),
                Updates.set("distribution", knowledge.getDistribution()),
                Updates.set("num", knowledge.getNum()),
                Updates.set("characterSet", knowledge.getCharSet()),
                Updates.set("connectors", connectList),
                Updates.set("transitivity", knowledge.getTransitivity()),
                Updates.set("reuseConsequent", knowledge.getReuseConsequent())
            );
            collection.updateOne(filter, update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // deleting...
    public Boolean deleteKnowledge(String id) {  //function currently not working
        try {
            Bson filter = Filters.eq("_id", new ObjectId(id));
            collection.deleteOne(filter);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
