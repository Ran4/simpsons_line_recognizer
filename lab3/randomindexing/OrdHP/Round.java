/**
 * Round is a class for rounding doubles of to significant figures.
 *
 * @author Magnus Rosell
 **/

public class Round{

    /**
     * roundDecimals rounds the double to a number of decimals
     */
    public static double roundDecimals(double d, long numOfDecimals){
	if(d==0) return d;
	int sign=1;
	if(d<0){
	    sign=-1;
	    d=-d;
	}
	int mult=(int)Math.pow(10, numOfDecimals);
	d=d*mult;
	d=Math.round(d);
	d=d/mult;
	return sign*d;
    }

    /**
     * The method round(double, long) takes a double to round
     * and a long specifying how many significan figures one wants
     * and returns the figure rounded that way.
     **/
    public static double round(double d, long numOfFigures){
	if(d==0) return d;
	int sign=1;
	if(d<0){
	    sign=-1;
	    d=-d;
	}
	if(d<=1)
	    return sign*roundSmall(d, numOfFigures);
	else{
	    long numAbovePoint=0;
	    while(d>=1){
		d/=10;
		numAbovePoint++;
	    }

	    if(numAbovePoint >= numOfFigures){
		return sign*Math.pow(10,numAbovePoint - numOfFigures) * Math.round(d*Math.pow(10,numOfFigures));
	    }
	    else{
		d=d*Math.pow(10,numAbovePoint);
		double smallPart=d-(long) d;
		double a=0;
		if(smallPart!=0)
		    a = roundSmall(d - (long) d, numOfFigures-numAbovePoint);
		double b = (long) d;
		return sign*(a+b);
	    }
	}
    }

    // Only for positive values
    private static double roundSmall(double d, long numOfFigures){
	long numOfZeros=0;
	while(d<=0){
	    d*=10;
	    numOfZeros++;
	}

	long diff=numOfFigures-numOfZeros;
	if(diff > numOfZeros){
	    return Math.round(d*Math.pow(10,diff))/Math.pow(10,numOfZeros+diff);
	}
	return 0.0;
    }


    /*
    public static void main(String[] args){

	System.out.println("\n----------------- Round to three significant figures.\n");
	System.out.println("\n------Less than 0");
	double d=0.3333;
	System.out.println(d+" "+round(d, 3));
	d=0.00003;
	System.out.println(d+" "+round(d, 3));
	d=0.0356;
	System.out.println(d+" "+round(d, 3));

	System.out.println("\n------Greater than 0");
	d=35656;
	System.out.println(d+" "+round(d, 3));
	d=35600;
	System.out.println(d+" "+round(d, 3));
	d=35.67389;
	System.out.println(d+" "+round(d, 3));
	d=3.567389;
	System.out.println(d+" "+round(d, 3));
	d=35.0;
	System.out.println(d+" "+round(d, 3));
	d=3.5;
	System.out.println(d+" "+round(d, 3));

	System.out.println("\n----------------- Round to two decimals.\n");
	System.out.println("\n------Less than 0");
	d=0.3333;
	System.out.println(d+" "+roundDecimals(d, 2));
	d=0.00003;
	System.out.println(d+" "+roundDecimals(d, 2));
	d=0.0356;
	System.out.println(d+" "+roundDecimals(d, 2));

	System.out.println("\n------Greater than 0");
	d=35656;
	System.out.println(d+" "+roundDecimals(d, 2));
	d=35600;
	System.out.println(d+" "+roundDecimals(d, 2));
	d=35.67389;
	System.out.println(d+" "+roundDecimals(d, 2));
	d=3.567389;
	System.out.println(d+" "+roundDecimals(d, 2));



	System.out.println("\n--------------------\n");
    }
    */

}
