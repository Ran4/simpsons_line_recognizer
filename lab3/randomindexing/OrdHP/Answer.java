import java.util.*;

/**
 * Class that keeps an answer to a question.
 */
public class Answer{
    private Question question;
    private TreeSet guessOrder;

    public Answer(Question question){
	this.question=question;
	guessOrder = new TreeSet();
    }

    public void addGuessWord(GuessWord guessWord){
	guessOrder.add(guessWord);
    }

    public boolean correct(){
	if(guessOrder.size()>0)
	    return ((GuessWord) guessOrder.first()).word.equals(question.getCorrect());
	return false;
    }

    public int numberOfGuessWords(){
	return guessOrder.size();
    }

    /*
    public int getNumberOfOptions(){
	return question.getNumberOfOptions();
    }
    */

    public String toString(){
	String answer="[";
	Iterator it=guessOrder.iterator();
	int i=0;
	while(it.hasNext()){
	    GuessWord gw = (GuessWord) it.next();
	    answer += gw;
	    if(i<guessOrder.size()-1)
		answer += " ";
	    i++;
	}
	answer += "]";
	return question.getQuestionWord()+" => "+question.getCorrect()+" "+answer;
    }

}
