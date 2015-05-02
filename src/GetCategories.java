//Del av AIn som r�knar ut vilka t�rningar som ska kastas som och kastar om dem
public class GetCategories {
	//Kastar om alla t�rningar i @hand som inte har v�rdet @value
	public static void allOfAKind(Hand hand, int value){
		for(Dice dice : hand.getDices()){
			if(dice.faceValue != value){
				dice.throwDice();
			}
		}
		hand.rollCounter();
	}

	//Kastar om t�rningarna f�r att kunna f� en liten stege
	public static void smallStraight(Hand hand){
		boolean[] straight = { false, false, false, false, false, false };
		for (Dice dice : hand.getDices()){
			if(straight[dice.getDiceValue() - 1] || dice.faceValue == 6){
				dice.throwDice();
			} 
			else{
				straight[dice.getDiceValue() - 1] = true;
			}
		}
		hand.rollCounter();
	}

	//Kastar om t�rningarna f�r att kunna f� en stor stege
	public static void largeStraight(Hand hand) {
		boolean[] straight = { false, false, false, false, false, false };
		for (Dice dice : hand.getDices()){
			if(straight[dice.getDiceValue() - 1] || dice.faceValue == 1){
				dice.throwDice();
			} 
			else{
				straight[dice.getDiceValue() - 1] = true;
			}
		}
		hand.rollCounter();
	}

	//Om vi sitter p� tv� par och vill ha en k�k, anv�nds i getFullHouse
	public static void twoPairToFullHouse(Hand hand){
		int i = 0;
		int j = 0;

		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		//Hittar paren
		for(int c = diceFreq.length - 1; c >= 0; c--){
			if(diceFreq[c] >= 2){
				if(i == 0){
					i = c + 1;
				}
				else{
					j = c + 1;
				}
			}
		}

		for(Dice dice : hand.getDices()){
			//Om t�rningarna inte har de v�rden vi har best�mt att beh�lla
			if(dice.faceValue != i && dice.faceValue != j){
				dice.throwDice();
			}
			//Om vi har mer �n 2 t�rningar med samma v�rde, kan vi kasta om de andra
			if(diceFreq[dice.faceValue - 1] > 2){
				dice.throwDice();
				diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
			}
		}

		hand.rollCounter();
	}

	//Kommer kolla om man har triss f�rst, sen tv� par, ett par och sedan enstaka t�rningar
	public static void getFullHouse(Hand hand){
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		int valueToKeep;
		int trissScore = AI.threeOfAKindScore(hand);
		if(trissScore != 0){
			valueToKeep = trissScore / 3;
			int otherValue = 0;
			for(int e = diceFreq.length; e > 0; e--){
				if(diceFreq[e-1] >= 1){
					otherValue = e;
					break;
				}
			}

			for(Dice dice : hand.getDices()){
				//Om t�rningarna inte har de v�rden vi har best�mt att beh�lla
				if(dice.faceValue != valueToKeep && dice.faceValue != otherValue){
					dice.throwDice();
				}
				//Om vi har mer �n 2 t�rningar med samma v�rde, kan vi kasta om de andra
				if(dice.faceValue == valueToKeep && diceFreq[valueToKeep-1]>3){
					dice.throwDice();
					diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
				}
			}
			hand.rollCounter();
			return;
		}

		if(AI.twoPairScore(hand) != 0){
			twoPairToFullHouse(hand);
			return;
		}

		int pairScore = AI.pairScore(hand);
		if(pairScore != 0){
			valueToKeep = pairScore / 2;
			int otherValue = 0;
			for(int e = diceFreq.length; e > 0; e--){
				if(diceFreq[e-1] >= 1){
					otherValue = e;
					break;
				}
			}

			for(Dice dice : hand.getDices()){
				//Om t�rningarna inte har de v�rden vi har best�mt att beh�lla
				if(dice.faceValue != valueToKeep && dice.faceValue != otherValue){
					dice.throwDice();
				}
				//Om vi har mer �n 2 t�rningar med samma v�rde, kan vi kasta om de andra
				if(dice.faceValue == valueToKeep && diceFreq[valueToKeep-1]>2){
					dice.throwDice();
					diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
				}
			}
			hand.rollCounter();
			return;
		}

		int diceValue = 0;
		int otherDiceValue = 0;
		for(int e = diceFreq.length; e > 0; e--){
			if(diceFreq[e-1] >= 1){
				if(diceValue == 0){
					diceValue = e;
				} 
				else{
					otherDiceValue = e;
					break;
				}
			}
		}

		for(Dice dice : hand.getDices()){
			if(dice.faceValue != diceValue && dice.faceValue != otherDiceValue){
				dice.throwDice();
			}
		}
		hand.rollCounter();
	}

	//F�r att kunna f� tv� olika par
	public static void getTwoPair(Hand hand){
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		int keep1 = 0;
		int keep2 = 0;
		
		for(int i = diceFreq.length; i > 0; i--){
			if(diceFreq[i-1] >= 1){
				if(keep1 == 0){
					keep1 = i;
				}
				else if(keep2 == 0){
					keep2 = i;
				}
				
				//Om vi har hittat tv� v�rden att beh�lla kommer vi kolla om freq �r h�gre f�r mindre val�rer och i s�dana fall beh�lla den
				if(keep1 != 0 && keep2 != 0){
					if(keep1 > keep2){
						if(diceFreq[keep2-1] < diceFreq[i-1]){
							keep2 = i;
						}
					} 
					else{
						if(diceFreq[keep1-1] < diceFreq[i-1]){
							keep1 = i;
						}
					}
				}
			}
		}

		for(Dice dice : hand.getDices()){
			//Om t�rningarna inte har de v�rden vi har best�mt att beh�lla
			if(dice.faceValue != keep1 && dice.faceValue != keep2){
				dice.throwDice();
			}
			//Om vi har mer �n 2 t�rningar med samma v�rde, kan vi kasta om de andra
			if(diceFreq[dice.faceValue - 1] > 2){
				dice.throwDice();
				diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
			}
		}	
		hand.rollCounter();
	}
}