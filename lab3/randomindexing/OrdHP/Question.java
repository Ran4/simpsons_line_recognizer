import java.util.*;

/**
 * Class that keeps a question, 
 * i.e. a word and a Vector of possible synonyms.
 */
public class Question{
    private String word;
    private Vector optionWords; 
    private int correctOption;

    public Question(String word, Vector optionWords, int correctOption){
	this.word=word;
	this.optionWords=optionWords;
	this.correctOption=correctOption;
    }

    public String getQuestionWord(){
	return word;
    }

    public Vector getOptionWords(){
	return optionWords;
    }

    public int getNumberOfOptions(){
	return optionWords.size();
    }

    public String getCorrect(){
	return (String) optionWords.elementAt(correctOption);
    }
}
