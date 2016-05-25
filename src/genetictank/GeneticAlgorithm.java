package genetictank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm{
	
	private static int POPULATION_SIZE = 20;
	private static int RENEW_RATE = 15;
	private static int MAX_ROUNDS = 5;
	
	private static int MUTATION_RATIO = 20; 
	private static int MAX_GENES = 6;
	
	private static int MIN_BULLET_POWER = 1;
	private static int MAX_BULLET_POWER = 3;
	private static int BULLET_POWER_PRECISION = 10;
	
	private static List<Chromosome> chromosomes;
	
	private static List<Double> relativeStatistics;
	private static List<Double> absoluteStatistics;
	
	private static int pos;
	private static int round;
	
	private static int absoluteWins;
	private static int absoluteRounds;
	private static int relativeWins;
	private static int relativeRounds;
	
	public static void start(GeneticRobot robot) {
		
		if (chromosomes == null) {
			chromosomes = new ArrayList<Chromosome>();
			
			for (int i = 0; i < POPULATION_SIZE; i++)
				chromosomes.add(generateRandomChromosome(i));
			
			pos = 0;
			round = 0;
			absoluteWins = 0;
			absoluteRounds = 0;
			relativeWins = 0;
			relativeRounds = 0;
			
			relativeStatistics = new ArrayList<Double>();
			absoluteStatistics = new ArrayList<Double>();
		}
		
		if (round == MAX_ROUNDS) {
			pos++;
			round = 0;
		}
		
		if (pos == POPULATION_SIZE) {
			pos = 0;
			round = 0;
			renewGeneration();
			relativeStatistics.add((((double)relativeWins)/relativeRounds)*100);
			absoluteStatistics.add((((double)absoluteWins)/absoluteRounds)*100);
			relativeWins = 0;
			relativeRounds = 0;
		}
		printStatistics();
		System.out.println("Going with robot " + pos + " of " + chromosomes.size() + " in round " + round);
		robot.chromosome = chromosomes.get(pos);
	}
	
	private static Chromosome generateRandomChromosome(int id) {
		Chromosome chromosome = new Chromosome(id);
		
		List<Double> moveRotation = new ArrayList<Double>();
		List<Double> moveDistance = new ArrayList<Double>();
		
		int moves = randomInt(1,MAX_GENES);
		
		for(int i = 0; i < moves; i++) {
			moveRotation.add((double)randomInt(0, 180));
			moveDistance.add((double)randomInt(100, 500));
		}
		
		chromosome.setRotations(moveRotation);
		chromosome.setDistances(moveDistance);
		
		int begin = MIN_BULLET_POWER * BULLET_POWER_PRECISION;
		int end = MAX_BULLET_POWER * BULLET_POWER_PRECISION;
		chromosome.setBulletPower(((double)randomInt(begin, end)) / BULLET_POWER_PRECISION);
		
		return chromosome;
	}
	
	public static void onRobotWin(Chromosome chromosome) {
		chromosome.increaseFitness();
		absoluteWins++;
		relativeWins++;
	}
	
	public static void onRoundEnded(Chromosome chromosome) {
		round++;
		absoluteRounds++;
		relativeRounds++;
	}
	
	public static void onBattleEnded(Chromosome chromosome) {
		System.out.println("========== Relative Statistics ==========");
		for(int i = 0; i < relativeStatistics.size(); i++) {
			//System.out.println("Iteration " + i + ": " + relativeStatistics.get(i) + "%.");
			System.out.println(relativeStatistics.get(i));
		}
		System.out.println("========== Absolute Statistics ==========");
		for(int i = 0; i < absoluteStatistics.size(); i++) {
			//System.out.println("Iteration " + i + ": " + absoluteStatistics.get(i) + "%.");
			System.out.println(absoluteStatistics.get(i));
		}
	}
	
	private static void renewGeneration() {
		Collections.sort(chromosomes);
		for(int i = 0; i < RENEW_RATE; i++) {
			int partition = randomInt(0, 1);
			int beggining = i;
			if(partition == 1)
				beggining = RENEW_RATE;
			
			Chromosome chromosome = chromosomes.get(i);
			int parent1 = randomInt(beggining, POPULATION_SIZE-1);
			int parent2;			
			if(parent1 == POPULATION_SIZE-1) {
				parent2 = POPULATION_SIZE-2;
			} else {
				parent2 = randomInt(parent1+1, POPULATION_SIZE-1);
			}
			int geneSize = chromosomes.get(parent1).getDistances().size();
			int splitPoint = randomInt((geneSize+1)/2, Math.min(geneSize, MAX_GENES/2));
			
			crossOver(geneSize,splitPoint,parent1,parent2,chromosome);
						
		}
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
			chromosomes.get(i).setFitness(0);
		}
	}
	
	private static void crossOver(int geneSize, int splitPoint,int parent1, int parent2,Chromosome chromosome){
		List<Double> moveRotation = new ArrayList<Double>();
		List<Double> moveDistance = new ArrayList<Double>();
		
		for(int j = 0; j < splitPoint; j++) {
			double mr = chromosomes.get(parent1).getRotations().get(j);
			double md = chromosomes.get(parent1).getDistances().get(j);
			
			boolean hasMutation = randomInt(1, MUTATION_RATIO) == 1;
			if(hasMutation) {
				mr += randomInt(-45,45);
				md += randomInt(-100,100);
			}
			
			moveRotation.add(mr);
			moveDistance.add(md);
		}
		
		geneSize = chromosomes.get(parent2).getDistances().size();
		splitPoint = randomInt((geneSize+1)/2, Math.min(geneSize, MAX_GENES/2));
		
		for(int j = geneSize-splitPoint; j < geneSize; j++) {
			double mr = chromosomes.get(parent2).getRotations().get(j);
			double md = chromosomes.get(parent2).getDistances().get(j);
			
			boolean hasMutation = randomInt(1, MUTATION_RATIO) == 1;
			if(hasMutation) {
				mr += randomInt(-20,20);
				md += randomInt(-50,50);
			}
			
			moveRotation.add(mr);
			moveDistance.add(md);
		}
		
		chromosome.setRotations(moveRotation);
		chromosome.setDistances(moveDistance);
		
		double totalBulletPower = chromosomes.get(parent1).getBulletPower() +
							chromosomes.get(parent2).getBulletPower();
		
		chromosome.setBulletPower(totalBulletPower / 2);
		
	}
	
	private static void printStatistics() {
		if(relativeRounds > 0)
			System.out.println("Relative Win ratio: " + ((((double)relativeWins)/relativeRounds)*100) + "%");
		if(absoluteRounds > 0)
			System.out.println("Absolute Win ratio: " + ((((double)absoluteWins)/absoluteRounds)*100) + "%");
		
		System.out.println("relativeWins: " + relativeWins);
		System.out.println("relativeRounds: " + relativeRounds);
		System.out.println("absoluteWins: " + absoluteWins);
		System.out.println("absoluteRounds: " + absoluteRounds);
	}
	
	private static int randomInt(int start, int end) {
		Random rand = new Random();
		return rand.nextInt(end - start + 1) + start;
	}
}
