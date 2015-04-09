import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;


public class test {

	@Test
	public void test() {
		Scorecard card = new Scorecard();
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		Hand hand = new Hand();
		int[] dice = {1,2,3,3,4};
		hand.setDices(dice);

		System.out.println(EarlyStrategy.checkBrokenStraight(hand));
		
		EarlyStrategy.goForStraight(card, hand, emptyCategories);
	}

}
