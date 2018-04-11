/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package annotatorstub.cbgeneration.pipeline.deprecated;

import annotatorstub.cbgeneration.pipeline.EntityGenerator;
import annotatorstub.cbgeneration.pipeline.entity.Entity;
import annotatorstub.utils.DistanceCalculator;
import annotatorstub.utils.deprecated.BIOEncoding;
import org.codehaus.jettison.json.JSONException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author N
 */
public class CandidateEnumeration {
    
    EntityGenerator entityGenerator;
    Set<CandidateEnumeration> candidates = new HashSet<>();
    Entity entity;
    String segment;
    
    public static void main(String[] args) throws JSONException {
        Entity entidad = null;
        String segmento = null;
        CandidateEnumeration candidate = new CandidateEnumeration(entidad, segmento);
        System.out.println(candidate.segmentation("george"));
    }
    
    public CandidateEnumeration(Entity entity,String segment) {
        this.entity = entity;
        this.segment = segment;
        
    }
    

    public Set<CandidateEnumeration> segmentation(String query){
       
        //Getting Set segmentation generated by the BIO
        Set<String> resultBIO = BIOEncoding.generateBIOq("george bush gave a speech");

                
        //Getting Eq = E1 U E2 U E3... E_q = union(E_1, E_2, E_3)
        Set<Entity> entities = new HashSet<>();
        entityGenerator = new EntityGenerator();
        entities = entityGenerator.generate(query);
        Set<CandidateEnumeration> cartesian = new HashSet<>();
        Boolean found = false;

        try{
            //For each pair(s,e) if Levenshtein <=0.7 add (s,e) to Cs
            for (String bio : resultBIO) 
                {
                        for (Entity e : entities) {
                            if (DistanceCalculator.minimumEditDistance(bio, e.getTitle()) <= 0.7) {
                                    CandidateEnumeration candidate = new CandidateEnumeration(e, bio);
                                    candidates.add(candidate);
                            }
                        }
                }
            //For each entity that has not been added in the previous 
           //step, add (s,e) to Cs for all s
           
				for(Entity e: entities){
                    Iterator<CandidateEnumeration> iterator = candidates.iterator();
                    while(iterator.hasNext()) {
                        CandidateEnumeration setElement = iterator.next();
                        if(setElement.entity.getTitle().equals(e.getTitle())){
                            found = true;
                        }
                        //The entity doesn't form part of the Candidates, s.t, it is neccesary to incluid it
                        if(!(iterator.hasNext())&& !found){
                            for (String bio : resultBIO) 
                                {
                                 CandidateEnumeration candidate = new CandidateEnumeration(e, bio);
                                 candidates.add(candidate);
                                }
                        }
                    }
                }
            //Cartesian Product Cs1 X...X Csg
          
                for (String bio : resultBIO){
                    Iterator<CandidateEnumeration> iterator = candidates.iterator();
                    while(iterator.hasNext()) {
                        CandidateEnumeration setElement = iterator.next();
                        CandidateEnumeration candidate = new CandidateEnumeration(setElement.entity, bio);
                        cartesian.add(candidate);
                    }
                }
            return cartesian;
        }
       


        catch(RuntimeException e) {
            return cartesian;
        }
    }
    }

    
    
