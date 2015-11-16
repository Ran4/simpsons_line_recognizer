/**
 * Class that keeps a "guess" from the Random Index.
 */
public class GuessWord implements Comparable{
    public String word;
    public float sim;

    public GuessWord(String word, float sim){
	this.word=word;
	this.sim=sim;
    }

    public String toString(){
	int numOfFigures=3;
	float simRound = (float) Round.round(sim, numOfFigures);
	return word+"("+simRound+")";
    }

    public int compareTo(Object o){
	GuessWord gw = (GuessWord) o;
	if(this.sim>gw.sim)
	    return -1;
	else if(this.sim<gw.sim)
	    return 1;
	return 0;
    }
}

