import java.util.*;

public class InvestigateHelper {
 
    private TrainHPProperties trainProperties;
    public Vector propertyVector;

    private Vector dimensionalityV = new Vector();
    private Vector randomDegreeV = new Vector();
    private Vector seedV = new Vector();
    private Vector leftWindowSizeV = new Vector();
    private Vector rightWindowSizeV = new Vector();
    private Vector weightingSchemeV = new Vector();
    private Vector stoplistV = new Vector();
    private Vector shortest_wordV = new Vector();
    private Vector longest_wordV = new Vector();
    private Vector minimum_words_per_fileV = new Vector();

    private Vector vectorVector = new Vector();

    public InvestigateHelper(){
	dimensionalityV = vectorFromIntArray(new int[]{1000, 2000});
	randomDegreeV = vectorFromIntArray(new int[]{4, 8, 100});
	//	seedV = vectorFromIntArray(new int[]{40812, 710225, 750711});
	seedV = vectorFromIntArray(new int[]{750711});
	leftWindowSizeV = vectorFromIntArray(new int[]{2, 4, 6});
	rightWindowSizeV = vectorFromIntArray(new int[]{2, 4, 6});
	weightingSchemeV = 
	    vectorFromStringArray(new String[]{"moj.ri.weighting.MangesWS", 
					       "moj.ri.weighting.MartinsWS", 
					       "moj.ri.weighting.RosellsWS"});
	stoplistV = vectorFromBooleanArray(new boolean[]{true, false});
	shortest_wordV = vectorFromIntArray(new int[]{1, 3, 5});
	//	longest_wordV = vectorFromIntArray(new int[]{10, 20, 50});
	//	minimum_words_per_fileV = vectorFromIntArray(new int[]{1, 2, 5});


	vectorVector.add(dimensionalityV);
	vectorVector.add(randomDegreeV);
	vectorVector.add(seedV);
	vectorVector.add(leftWindowSizeV);
	vectorVector.add(rightWindowSizeV);
	vectorVector.add(weightingSchemeV);
	vectorVector.add(stoplistV);
	vectorVector.add(shortest_wordV);
	/*
	vectorVector.add(longest_wordV);
	vectorVector.add(minimum_words_per_fileV);
	*/
    }



    public void makePropertyVector(int vectorVectorIndex, Vector props){
	if(vectorVectorIndex == vectorVector.size()){
	    TrainHPProperties trainHPProperties = 
		new TrainHPProperties(((Integer)props.elementAt(0)).intValue(),
				      ((Integer)props.elementAt(1)).intValue(),
				      ((Integer)props.elementAt(2)).intValue(),
				      ((Integer)props.elementAt(3)).intValue(),
				      ((Integer)props.elementAt(4)).intValue(),
				      (String) props.elementAt(5),
				      ((Boolean)props.elementAt(6)).booleanValue(),
				      ((Integer)props.elementAt(7)).intValue());

	    propertyVector.add(trainHPProperties);
	    return;
	}
	
	Vector vector = (Vector) vectorVector.elementAt(vectorVectorIndex);
	for(int i=0; i<vector.size(); i++){
	    props.add(vector.elementAt(i));
	    makePropertyVector(vectorVectorIndex+1, props);
	    props.remove(props.size()-1);
	}
    }

    public Vector getPropertyVector(){
	propertyVector = new Vector();
	makePropertyVector(0, new Vector());
	return propertyVector;
    }




    private Vector vectorFromIntArray(int[] a){
	Vector v = new Vector();
	for(int i=0; i<a.length; i++)
	    v.add(new Integer(a[i]));
	return v;
    }

    private Vector vectorFromBooleanArray(boolean[] a){
	Vector v = new Vector();
	for(int i=0; i<a.length; i++)
	    v.add(new Boolean(a[i]));
	return v;
    }

    private Vector vectorFromStringArray(String[] a){
	Vector v = new Vector();
	for(int i=0; i<a.length; i++)
	    v.add(a[i]);
	return v;
    }




}

