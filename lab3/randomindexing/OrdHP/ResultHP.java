import java.io.*;
import java.util.*;

public class ResultHP implements Comparable{
    TrainHP trainHP;
    long wordsIndexed;
    long wordformsIndexed;
    String facit;

    public int correct;
    public int missed;
    public int unsufficient;
    public int correctAmongInsufficient;
    public int total;
    
    Vector correctAnswers; // The correct answers
    Vector missedAnswers; // The incorrect answers
    Vector unsufficientAnswers; // "Answers" to questions where the Random Index
                                // lacks information
    Vector correctAmongUnsufficientAnswers;

    String resultFilePath = "results/"; // Default 

    public ResultHP(Vector correctAnswers,
		    Vector missedAnswers,
		    Vector unsufficientAnswers,
		    TrainHP trainHP,
		    long wordsIndexed,
		    long wordformsIndexed,
		    String facit){
	this.correctAnswers=correctAnswers;
	this.missedAnswers=missedAnswers;
	this.unsufficientAnswers=unsufficientAnswers;

	this.trainHP=trainHP;
	this.wordsIndexed=wordsIndexed;
	this.wordformsIndexed=wordformsIndexed;

	this.facit=facit;

	correct = correctAnswers.size();
	missed = missedAnswers.size();
	unsufficient = unsufficientAnswers.size();

	//	correctAmongUnsufficientAnswers = new Vector();
	correctAmongInsufficient=0;
	for(int i=0; i<unsufficientAnswers.size(); i++){
	    Answer a = (Answer) unsufficientAnswers.elementAt(i);
	    if(a.correct()){
		//		correctAmongUnsufficientAnswers.add(a);
		correctAmongInsufficient++;
	    }
	}

	total = correct + missed + unsufficient;
    }

    public void writeTestResults(){
	writeTestResults(new OutputStreamWriter(System.out));
    }

    // Write the test results
    public void writeTestResults(OutputStreamWriter osw){
	writeTestResultWords(osw);
	writeTestResultFigures(osw);
    }


    public void writeTestResultWords(){
	writeTestResultWords(new OutputStreamWriter(System.out));
    }

    public void writeTestResultWords(OutputStreamWriter osw){
	try{
	    osw.write("Test File: "+facit+"\n\n");
	    osw.write("- Correct\n");
	    for(int i=0; i<correctAnswers.size(); i++)
		osw.write(((Answer) correctAnswers.elementAt(i)).toString()+"\n");

	    osw.write("\n- Missed\n");
	    for(int i=0; i<missedAnswers.size(); i++)
		osw.write(((Answer) missedAnswers.elementAt(i)).toString()+"\n");

	    osw.write("\n- Insufficient\n");
	    int correctAmongInsufficient=0;
	    for(int i=0; i<unsufficientAnswers.size(); i++){
		Answer a = (Answer) unsufficientAnswers.elementAt(i);
		if(a.correct()){
		    osw.write("*");
		}
		osw.write(a.toString()+"\n");
	    }
	    osw.flush();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
    }




    public void writeTestResultFigures(){
	writeTestResultFigures(new OutputStreamWriter(System.out));
    }

    public void writeTestResultFigures(OutputStreamWriter osw){
	try{
	    osw.write("\n- Numbers\n");
	    osw.write("Correct: "+correct+"\n");
	    osw.write("Missed: "+missed+"\n");
	    osw.write("Insufficient: "+unsufficient+
		      " ("+correctAmongInsufficient+"*)\n");
	    osw.write("Total: "+total+"\n");
	    osw.flush();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
	
    }


    // Write properties and test results to file
    public void writeResultFile(){
	String fileName = "resultHP."+System.currentTimeMillis()+".res";
	writeResultFile(fileName);
    }

    public void writeResultFile(String fileName){
	try{

	    FileWriter fw = new FileWriter(resultFilePath+fileName, true);

	    fw.write("--- Training Info\n\n");
	    trainHP.getTrainHPProperties().writeProperties(fw);
	    fw.write("Training File Or Directory: "+trainHP.fileOrDirectory+"\n");
	    fw.write("\n");
    
	    fw.write(wordsIndexed + " words indexed\n");
	    fw.write(wordformsIndexed + " wordforms/index terms\n");

	    fw.write("\n\n--- Test Result\n\n");

	    writeTestResultFigures(fw);
	    writeTestResultWords(fw);

	    fw.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
    }

    public void setResultFilePath(String resultFilePath){
	this.resultFilePath=resultFilePath;
    }


    public int compareTo(Object o){
	ResultHP r = (ResultHP) o;
	if(this.correct>r.correct)
	    return -1;
	else if(this.correct<r.correct)
	    return 1;

	/*
	  Även om de har samma resultat ska de inte 
	  betraktas som samma av ett TreeSet.
	  Denna metod används bara i Investigate.java.
	*/
	return 1; 
    }


}

