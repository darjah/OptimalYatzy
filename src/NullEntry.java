import java.util.LinkedList;

public class NullEntry {
	final static int[] onlyNullUp = {0, 1, 2, 3, 4, 5};
	
	final static int[] nullUp = {0, 1, 2, 3, 4, 5, Scorecard.smallStraight, Scorecard.largeStraight, Scorecard.yatzy, Scorecard.fourOfAKind, 
								Scorecard.fullHouse, Scorecard.threeOfAKind, Scorecard.twoPair, Scorecard.pair};
	
	final static int[] nullDown = {Scorecard.smallStraight, Scorecard.largeStraight, Scorecard.fourOfAKind, Scorecard.yatzy, 
									Scorecard.fullHouse, Scorecard.threeOfAKind, Scorecard.twoPair, Scorecard.pair, 0, 1, 2, 3, 4, 5};
	
	//Nolla endast f�rsta halvan
	public static boolean onlyZeroUp(Scorecard card){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		for(int i : onlyNullUp){
			if(emptyCategories.contains(i)){
				card.categories[i] = 0;
				return true;
			}
		}
		return false;
	}
	
	//Nolla f�rsta halvan
	public static void zeroUp(Scorecard card){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		for(int i : nullUp){
			if(emptyCategories.contains(i)){
				card.categories[i] = 0;
				return;
			}
		}
	}

	//Nolla andra halvan
	public static void zeroDown(Scorecard card){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		for(int i : nullDown){
			if(emptyCategories.contains(i)){
				card.categories[i] = 0;
				return;
			}
		}
	}

	public static void nullEntry(Scorecard card){	
		//N�r vi redan har bonusen eller det inte l�ngre �r m�jligt att f� den
		if(card.doWeHaveBonus(card) || !card.possibleToGetBonus(card)){
			NullEntry.zeroUp(card);
		}
		/*//N�r vi ligger �ver onPar
		else if(card.onPar() == 1){
			NullEntry.zeroUp(card);
		}*/
		//Alla andra fall
		else{
			NullEntry.zeroDown(card);
		}
	}
}