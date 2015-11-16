import java.io.*;
import java.util.*;
import moj.ri.RandomIndex;
import moj.ri.RandomLabel;
import moj.ri.SparseDistributedMemory;
import moj.util.VectorSpace;

public class TesterHP{
    String facit;
    Vector questions;

    public TesterHP(String facit){
	this.facit=facit;
	questions = new Vector();
	readTestWords();
    }

    // Read the test from file
    private void readTestWords(){
	try{
	    String line;
	    BufferedReader txtbr = new BufferedReader(new FileReader(facit));
	    while((line = txtbr.readLine()) != null) {
		String fields[] = line.split("\\|");	
		String qWord = fields[0];
		Vector optionWords = new Vector();
		for(int i=1; i<fields.length-1; i++)
		    optionWords.add(fields[i]);
		int correctOption = Integer.parseInt(fields[fields.length-1]);
		questions.add(new Question(qWord, optionWords, correctOption));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
    }

    // Test a Random Index
    public ResultHP test(SparseDistributedMemory sdm, TrainHP trainHP) {
	Vector correctAnswers = new Vector();
	Vector missedAnswers = new Vector();
	Vector unsufficientAnswers = new Vector();

	for(int i=0; i<questions.size(); i++){
	    Question q = (Question)questions.elementAt(i);
	    String qWord = q.getQuestionWord();
	    RandomLabel qL = sdm.getRandomLabel(qWord);
	    
	    Answer a = new Answer(q);
	    if(qL!=null){
		Vector optionWords = q.getOptionWords();
		for(int j=0; j<optionWords.size(); j++){
		    String oWord = (String) optionWords.elementAt(j);
		    RandomLabel oL = sdm.getRandomLabel(oWord);
		    if(oL!=null){
			float sim = 
			    VectorSpace.cosineSim(qL.getContext(), oL.getContext());
			GuessWord gw = new GuessWord(oWord, sim);
			a.addGuessWord(gw);
		    }
		}
		if(a.numberOfGuessWords() == optionWords.size()){
		    if(a.correct())
			correctAnswers.add(a);
		    else
			missedAnswers.add(a);
		} else
		    unsufficientAnswers.add(a);

	    }else
		unsufficientAnswers.add(a);
	}
	
	long wordsIndexed = sdm.getWordsIndexed();
	long wordformsIndexed = sdm.size();
	return 
	    new ResultHP(correctAnswers, missedAnswers, unsufficientAnswers, 
			 trainHP, wordsIndexed, wordformsIndexed,
			 facit);
    }


}

