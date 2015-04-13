import java.util.LinkedList;

import org.junit.Test;


public class test {
	public test() {
		Scorecard card = new Scorecard();
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		Hand hand = new Hand();
		int[] dice = {1,2,3,3,4};
		hand.setDices(dice);

		System.out.println(EarlyStrategy.checkBrokenStraight(hand));
		
		EarlyStrategy.goForStraight(card, hand, emptyCategories);
	}
	
	@Test
	public void test2(){
		Scorecard card = new Scorecard();
		//LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		Hand hand = new Hand();
		int[] dice = {3,3,4,4,5};
		hand.setDices(dice);
		int[] scores = {-1,-1,9,16,-1,18,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		card.setScores(scores);
		
		card.stillOnPar(card, hand);
		for(int i = 0; i< 15; i++){
			System.out.println(card.categories[i]);
		}
	}

}
