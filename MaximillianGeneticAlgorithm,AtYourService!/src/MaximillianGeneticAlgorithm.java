import gui.GUI;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

// for finding the global maximum of a hard-coded function using evolutionary techniques
public class MaximillianGeneticAlgorithm {
	
	static ArrayList<Double> generationalMaxFitness = new ArrayList<Double>(0); // holds the maximum fitness for each generation
	static ArrayList<Integer> population = new ArrayList<Integer>(0); // holds the population members (in this case, x-values)
	static ArrayList<Double> populationFitnesses = new ArrayList<Double>(0); // holds the corresponding fitnesses of the members
	static ArrayList<String> chromosomeStrings = new ArrayList<String>(0); // holds the full chromosomes of the members
	
	static GUI g = new GUI(); // to interact with the user
	
	static int evolutionRuntime = 0;
	static int generationNumber = 0;
	static int lengthOfChromosomes = 20;
	
	static String fullGeneaology = ""; // is a fairly full record of what happens
	static String function = "-0.4((x-6)^2) + sin(4(x-4)) + 10"; // the function to be optimized
		
	// gets rid of the "weakest" specimens
	static boolean timeToPrune (double cutoffPercentage) { // cutoffPercentage decides the percentage of the maximum fitness below which members will not survive
		fullGeneaology += "\n";
		fullGeneaology += "! Pruning Time !\n";
		fullGeneaology += "Cutoff Percentage: " + cutoffPercentage + "%\n";
		double maximumFitness = populationFitnesses.get(0); // initializing with the first member's fitness
		for (int counter = 0; counter < populationFitnesses.size(); counter++) { // finding the maximum fitness in the population
			if (populationFitnesses.get(counter) > maximumFitness) {
				maximumFitness = populationFitnesses.get(counter);
			}
		}
		boolean extinction = false; // indicates whether the population will shortly go extinct (due to population loss)
		double cutoffPoint = (cutoffPercentage / 100) * maximumFitness; // finding the numeric value corresponding to that cutoffPercentage
		fullGeneaology += "Cutoff Point: " + cutoffPoint + "\n";
		for (int counter = 0; counter < population.size(); counter++) { // decides which members are not strong enough and prunes them
			if (populationFitnesses.get(counter) < cutoffPoint) {
				double removedFitness = populationFitnesses.remove(counter);
				double removedMember = population.remove(counter);
				String removedChromosome = chromosomeStrings.remove(counter);
				fullGeneaology += "\tMember " + (counter + 1) + " died." + " (X-Coordinate: " + removedMember + "; Chromosome: " + removedChromosome + "; Fitness: " + removedFitness + ")\n";
			}
		}
		if (population.size() == 1) { // if there's only one member left, there's not much we can do...
			extinction = true;
			fullGeneaology += "\n\n...\n";
			fullGeneaology += "Unfortunately, there is only one remaining specimen, and they will die of sadness. Awww...\n";
			fullGeneaology += "Sorry.";
		}
		return extinction;
	}

	// set up the chromosome strings with each other
	static void survivorsMakeBeastsWithTwoBacks () {
		fullGeneaology += "\n";
		Random matchmaker = new Random();
		ArrayList<String> decreasingDuplicateOfChromosomeStrings = new ArrayList<String>(0);
		for (int counter = 0; counter < chromosomeStrings.size(); counter ++) { // fill the duplicate ArrayList
			decreasingDuplicateOfChromosomeStrings.add(chromosomeStrings.get(counter));
		}
		while (decreasingDuplicateOfChromosomeStrings.size() > 1) { // here's why it's a decreasing duplicate...
			int numberOfFirstRandomMate = matchmaker.nextInt(decreasingDuplicateOfChromosomeStrings.size()); // choose a random index...
			String firstParent = decreasingDuplicateOfChromosomeStrings.remove(numberOfFirstRandomMate); // ...that's the first parent
			int numberOfSecondRandomMate = matchmaker.nextInt(decreasingDuplicateOfChromosomeStrings.size()); // choose another random index...
			String secondParent = decreasingDuplicateOfChromosomeStrings.remove(numberOfSecondRandomMate); // ...and that's the second parent
			int chromosomeStringsIndexOfFirstParent = chromosomeStrings.indexOf(firstParent); // getting the 'true'/permanent location of the first parent
			int chromosomeStringsIndexOfSecondParent = chromosomeStrings.indexOf(secondParent); // getting the 'true'/permanent location of the second parent
			fullGeneaology += "! Mating Session !\n";
			mate(firstParent, chromosomeStringsIndexOfFirstParent, secondParent, chromosomeStringsIndexOfSecondParent);
		}
		if (decreasingDuplicateOfChromosomeStrings.size() == 1) { // if there's only one chromosome string left in the decreasing duplicate...
			fullGeneaology += "! Mating Session !\n";
			String firstParent = decreasingDuplicateOfChromosomeStrings.get(0);
			int chromosomeStringsIndexOfFirstParent = chromosomeStrings.indexOf(firstParent);
			int chromosomeStringsIndexOfSecondParent = matchmaker.nextInt(chromosomeStrings.size());
			String secondParent = chromosomeStrings.get(chromosomeStringsIndexOfSecondParent);
			mate(firstParent, chromosomeStringsIndexOfFirstParent, secondParent, chromosomeStringsIndexOfSecondParent); // ...mate that one with a random chromosome from chromosomeStrings
		}
	}
	
	// updates the full geneaology with a survey of the population
	static void survey (int generationNumber) {
		double maximumFitness = populationFitnesses.get(0); // initializing with the first member's fitness
		fullGeneaology += "-----> Status <-----\n";
		fullGeneaology += "Generation Number: " + (generationNumber + 1) + "\n\n";
		for (int counter = 0; counter < population.size(); counter++) { // determining the maximum fitness and updating the full geneaology.
			double currentFitness = populationFitnesses.get(counter);
			if (currentFitness > maximumFitness) {
				maximumFitness = currentFitness;
			}
			double currentXCoordinate = population.get(counter);
			String currentChromosomeString = chromosomeStrings.get(counter);
			fullGeneaology += "\tSpecimen " + (counter + 1) + ": X-Coordinate = " + currentXCoordinate + "; Chromosome = " + currentChromosomeString + "; Fitness = " + currentFitness;
			fullGeneaology += "\n";
		}
		generationalMaxFitness.add(maximumFitness);
		fullGeneaology += "\n";
		fullGeneaology += "\t\tCurrent Maximum Fitness: " + maximumFitness + "\n";
	}
	
	// how two chromosome strings make a child
	static void mate (String firstRandomMate, int firstRandomMateIndex, String secondRandomMate, int secondRandomMateIndex) {
		fullGeneaology += "\tFirst Parent: " + firstRandomMate + "\n";
		fullGeneaology += "\t\tX-Coordinate: " + population.get(firstRandomMateIndex) + "\n";
		fullGeneaology += "\t\tFitness: " + populationFitnesses.get(firstRandomMateIndex) + "\n";
		fullGeneaology += "\tSecond Parent: " + secondRandomMate + "\n";
		fullGeneaology += "\t\tX-Coordinate: " + population.get(secondRandomMateIndex) + "\n";
		fullGeneaology += "\t\tFitness: " + populationFitnesses.get(secondRandomMateIndex) + "\n";
		char[] firstParentChars = firstRandomMate.toCharArray();
		char[] secondParentChars = secondRandomMate.toCharArray();
		String child = "";
		for (int counter = 0; counter < lengthOfChromosomes; counter++) { // the creation of the child
			if (counter % 2 == 0) { // if the bit number is even, the child will take bits from the second parent
				child += secondParentChars[counter];
			} else { // otherwise, the child will take bits from the first parent
				child += firstParentChars[counter];
			}
		}
		Random scythe = new Random(); // for determining which parent dies (a population member had been added (the child), so a population member might be removed (one of the parents). This is done to keep the population from getting ridiculously large.
		boolean coinFlipDeath = scythe.nextBoolean();
		if (coinFlipDeath) { // 50% chance of a parent dying
			if (coinFlipDeath) { // first parent dies
				population.remove(firstRandomMateIndex);
				populationFitnesses.remove(firstRandomMateIndex);
				chromosomeStrings.remove(firstRandomMateIndex);
				fullGeneaology += "\tThe first parent died in the process of childbirth.\n";
			} else { // second parent dies
				population.remove(secondRandomMateIndex);
				populationFitnesses.remove(secondRandomMateIndex);
				chromosomeStrings.remove(secondRandomMateIndex);
				fullGeneaology += "\tThe second parent died in the process of childbirth.\n";
			}
		}
		String childChromosome = String.format("%" + lengthOfChromosomes + "s", child).replace(' ', '0'); // getting the child's full chromosome
		fullGeneaology += "\tTheir Child: " + childChromosome + " (how beautiful!)\n";
		chromosomeStrings.add(childChromosome);
		int childXCoordinate = bitToDigit(childChromosome);
		fullGeneaology += "\t\tX-Coordinate: " + childXCoordinate + "\n";
		population.add(childXCoordinate);
		double childFitness = fOfX(childChromosome);
		fullGeneaology += "\t\tFitness: " + childFitness + "\n";
		populationFitnesses.add(childFitness);
	}
	
	// for determining the fitness of a chromosome
	static double fOfX (String chromosomeString) {
		double digit = bitToDigit(chromosomeString);
		double fitness = (-0.4 * Math.pow((digit - 6.0), 2.0)) + Math.sin(4 * (digit - 4)) + 10; // plugging in digit into the function (which is -0.4((x-6)^2) + sin(4(x-4)) + 10)
		return fitness;
	}
	
	// sees if a mutation will occur, then creates that mutation
	static void bruceBannerEffect (int mutationRate) { // mutation rate is equal to 1 in mutation rate (e.g., if mutationRate == 10, then the mutation rate is 1 in 10, or 10%).
		fullGeneaology += "\n";
		Random theHandsOfFate = new Random();
		if (theHandsOfFate.nextInt(mutationRate) == 1) {
			fullGeneaology += "!! Mutation Occurring !!\n";
			int randomChromosomeIndex = theHandsOfFate.nextInt(chromosomeStrings.size()); // chooses a random chromosome to mutate
			fullGeneaology += "Specimen " + (randomChromosomeIndex + 1) + " chosen for mutation.\n";
			String randomChromosome = chromosomeStrings.remove(randomChromosomeIndex); // removes that chromosome...
			double unmutatedFitness = populationFitnesses.remove(randomChromosomeIndex); // ...and its fitness value...
			int unmutatedXCoordinate = population.remove(randomChromosomeIndex); // ...and its x-coordinate representation
			fullGeneaology += "\tTheir unmutated x-coordinate: " + unmutatedXCoordinate + "\n";
			fullGeneaology += "\tTheir unmutated chromosome: " + randomChromosome + "\n";
			fullGeneaology += "\tTheir unmutated fitness: " + unmutatedFitness + "\n";
			int randomMutationPoint = theHandsOfFate.nextInt(lengthOfChromosomes); // chooses a random bit to mutate
			fullGeneaology += "\tBit number " + (randomMutationPoint + 1) + " chosen for mutation.\n";
			String mutant = "";
			for (int counter = 0; counter < lengthOfChromosomes; counter ++) { // constructing the mutated chromosome
				if (counter == (lengthOfChromosomes - randomMutationPoint)) { // we're at the mutation bit
					if (randomChromosome.charAt(counter) == '0') { // if it's zero, flip it to one
						mutant += "1";
					}
					else if (randomChromosome.charAt(counter) == '1') { // if it's one, flip it to zero
						mutant += "0";
					}
				}
				else { // otherwise, just fill in the rest of the bits as they are
					mutant += randomChromosome.charAt(counter);
				}
			}
			String mutantChromosome = String.format("%" + lengthOfChromosomes + "s", mutant).replace(' ', '0'); // get the full chromosome of the mutant
			int mutantXCoordinate = bitToDigit(mutantChromosome);
			population.add(mutantXCoordinate);
			fullGeneaology += "\tSpecimen " + (randomChromosomeIndex + 1) + "\'s new x-coordinate: " + mutantXCoordinate + "\n";
			chromosomeStrings.add(mutantChromosome);
			fullGeneaology += "\tSpecimen " + (randomChromosomeIndex + 1) + "\'s new chromsome: " + mutant + "\n";
			double mutantFitness = fOfX(mutantChromosome);
			populationFitnesses.add(mutantFitness);
			fullGeneaology += "\tSpecimen " + (randomChromosomeIndex + 1) + "\'s new fitness: " + mutantFitness + "\n";
		}
	}
	
	// for finding the x-coordinate representation of a chromosomeString
	static int bitToDigit (String chromosomeString) {
		int digit = 0;
		for (int counter = 1; counter < lengthOfChromosomes; counter ++) { // converting the chromosomeString into a digit
			if (chromosomeString.charAt(counter) == '1') {
				double value = Math.pow(2.0, (double) ((lengthOfChromosomes - counter) - 1)); // calculating 2^(current location in the chromosomeString)
				digit += value;
			}
		}
		if (chromosomeString.charAt(0) == '1') { // the sign is negative
			return 0 - digit;
		} else { // the sign is positive
			return digit;
		}
	}
	
	public static void main (String[] args) {
		g.give("Hey there! I'm Maximillian Genetic Algorithm, and I'm designed to find the global maximum of y = " + function + "!");
		g.give("Let\'s get started!");
		int initialPopulationSize;
		while (true) { // input loop: exits once the user has input a valid answer for initial population size
			String initialPopulationSizeString = g.get("Please enter the population size...");
			if (initialPopulationSizeString == null) { // user selected "Cancel"
				g.give("So long!");
				System.exit(0);
			}
			try {
				initialPopulationSize = Integer.parseInt(initialPopulationSizeString);
			} catch (NumberFormatException numbFormExce) { // user didn't enter in a number, go back to the top of the loop and start again
				g.giveThrowable(numbFormExce);
				g.give("Enter a number!");
				continue;
			}
			break;
		}
		int choice = g.giveYesNo("Population will be randomized with values between -10,000 and 10,000.\n"
				  + "Click \"Yes\" to confirm; Click \"No\" to randomize with ALL POSSIBLE int values.");
		ThreadLocalRandom initializePopulationTLR = null;
		Random initializePopulationR = null;
		if (choice == 0) { // user chose "Yes"
			initializePopulationTLR = ThreadLocalRandom.current();
		} else if (choice == 1) { // user chose "No"
			initializePopulationR = new Random();
		} else { // user chose "Quit"
			g.give("Goodbye!");
			System.exit(0);
		}
		for (int counter = 0; counter < initialPopulationSize; counter ++) { // getting the initial population members
			int number = 0;
			if (choice == 0) {
				number = initializePopulationTLR.nextInt(-10000, 10001);
			} else if (choice == 1) {
				number = initializePopulationR.nextInt();
			}
			population.add(counter, number);
			double fOfX = (-0.4 * Math.pow((number - 6.0), 2.0)) + Math.sin(4 * (number - 4)); // finding the f(x) (the fitness) of the current number
			populationFitnesses.add(counter, fOfX);
			String binary, chromosomeString;
			if (number < 0) { // need to have binary be the representation that I am using for negative numbers
				binary = Integer.toBinaryString(0 - number);
				chromosomeString = String.format("%" + lengthOfChromosomes + "s", binary).replace(' ', '0');
				char[] chromosomeStringCharArray = chromosomeString.toCharArray();
				chromosomeStringCharArray[0] = '1'; // signed value for negative
				chromosomeString = new String(chromosomeStringCharArray);
			} else {
				binary = Integer.toBinaryString(number);
				chromosomeString = String.format("%" + lengthOfChromosomes + "s", binary).replace(' ', '0');
			}
			chromosomeStrings.add(counter, chromosomeString);
		}
		int pruningCutoffPercentage, mutationRate; // the percentage of maximum fitness above which members are fit enough to survive, and the chance of mutation
		while (true) { // input loop: exits if the user inputs valid answers for the fields generationRuntime, desiredFitness, pruningCutoffPercentage, and mutationRate.
			String generationRuntimeString = g.get("Please enter how many generations you'd like to run for...");
			if (generationRuntimeString == null) { // the user chose "Cancel", exit the program
				g.give("So long!");
				System.exit(0);
			}
			String pruningCutoffPercentageString = g.get("Please enter the percentage of generational maximum fitness above which members will survive...");
			if (pruningCutoffPercentageString == null) { // the user chose "Cancel", exit the program
				g.give("So long!");
				System.exit(0);
			}
			String mutationRateString = g.get("And finally, please enter the chance of mutation (one in ______ chance)...");
			if (mutationRateString == null) { // the user chose "Cancel", exit the program
				g.give("So long!");
				System.exit(0);
			}
			try {
				evolutionRuntime = Integer.parseInt(generationRuntimeString);
				pruningCutoffPercentage = Integer.parseInt(pruningCutoffPercentageString);
				mutationRate = Integer.parseInt(mutationRateString);
			} catch (NumberFormatException numbFormExce) { // didn't enter a number, go to the beginning of the while-loop
				g.giveThrowable(numbFormExce);
				g.give("Enter a number!");
				continue;
			}
			break;
		}
		JFrame jeff = new JFrame("Generations Lived"); // will hold a progress bar that keeps the user informed on the completion percentage of the genetic algorithm
		jeff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JProgressBar status = new JProgressBar(0, 100);
		int mark = 0;
		status.setValue(mark);
		status.setStringPainted(true);
		jeff.getContentPane().add(status);
		jeff.pack();
		jeff.setLocation((g.getScreensize().width / 2) - (jeff.getWidth() / 2), (g.getScreensize().height / 2) - (jeff.getHeight() / 2)); // put the JFrame in the middle of the computer display
		jeff.setVisible(true);
		fullGeneaology += "▼ ▼ Starting Conditions ▼ ▼\n";
		survey(generationNumber);
		fullGeneaology += "▲ ▲ Starting Conditions ▲ ▲\n";
		while (generationNumber < evolutionRuntime) { // abides by the runtime cap set by the user
			if (generationNumber == evolutionRuntime) { // we've reached the last generation
				break;
			}
			boolean nearlyExtinct = timeToPrune(pruningCutoffPercentage); // get rid of "weakest" specimens, see if extinction is near
			if (nearlyExtinct) {
				jeff.dispose();
				g.give("Can no longer continue...take a look at the full geneaology for why!");
				g.give("Last generation's max fitness: " + generationalMaxFitness.get(generationalMaxFitness.size() - 1));
				g.giveTextArea(fullGeneaology, 0, 0, 1, 2);
				MaxFitnessOverTime.main(args);
				g.give("Goodbye!");
				System.exit(0);
			}
			survivorsMakeBeastsWithTwoBacks(); // surviving specimens mate
			bruceBannerEffect(mutationRate); // mutation opportunity
			survey(generationNumber);
			generationNumber ++;
			mark = (int) (((double) generationNumber / (double) evolutionRuntime) * 100);
			status.setValue(mark);
		}
		jeff.dispose();
		g.give("Yay, no more to do!");
		g.give("Last generation's max fitness: " + generationalMaxFitness.get(generationalMaxFitness.size() - 1));
		g.give("Let me give you the full geneaology of the evolution and a graph of the maximum fitness over time...");
		g.giveTextArea(fullGeneaology, 0, 0, 1, 2); // place the fullGeneaology in the upper-left corner
		MaxFitnessOverTime.main(args); // form and display the graph
		g.give("Goodbye!");
		System.exit(0);
	}
	
	// displays a graph of maximum fitness vs. generation number
	public static class MaxFitnessOverTime extends Application {
		public void start (Stage s) {
			Stage stage = new Stage();
			Platform.setImplicitExit(true);
			stage.setTitle("How Did the Evolving Go?");
			NumberAxis horace = new NumberAxis("Generation Number", 1, evolutionRuntime, 1);
			NumberAxis vesuvio = new NumberAxis();
			vesuvio.setLabel("Maximum Fitness");
			LineChart<Number, Number> maxFitnessVsGenerationNumber = new LineChart<Number, Number>(horace, vesuvio);
			maxFitnessVsGenerationNumber.setTitle("Max Fitness Over Time");
			XYChart.Series<Number, Number> xyPoints = new XYChart.Series<Number, Number>();
			xyPoints.setName("Data");
			for (int counter = 0; counter < generationalMaxFitness.size(); counter ++) {
				xyPoints.getData().add(new XYChart.Data<Number, Number>((counter + 1), generationalMaxFitness.get(counter)));
			}
			maxFitnessVsGenerationNumber.getData().add(xyPoints);
			Scene scene = new Scene(maxFitnessVsGenerationNumber, g.getScreensize().getWidth(), g.getScreensize().getHeight() / 2);
			stage.setScene(scene);
			stage.setX(0);
			stage.setY(g.getScreensize().getHeight() / 2);
			stage.show();
		}
		
		public static void main (String[] args) {
			launch();
		}
	}
}