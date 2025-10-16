import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.time.Duration;
import java.time.Instant;
/*
 * Sania Gupta
 * 
 * 31 August 2023
 * 
 * Runs and trains an A-B-C-D network with XOR, OR, or AND to return values that near the target values determined by a truth table 
 * using steepest descent.
 * ----------------------------------
 * VARIABLE DESCRIPTIONS:
 * 
 * String networkConfig          //The type of network configuration
 * String nodeFile               //The file with a truth table for the network configuration
 * int numLayers                 //The number of connectivity layers in the network
 * int[] numActivations          //The number of activations in each layer of the network
 * String trainOrRun             //Determines if the network will train or run
 * 
 * int numCases                  //The total number of test cases used for training
 * double learningFactor         //The learning factor, or lambda, used to control how much the weights are modified each step
 * double[][] psi                //The values of omega multiplied by the activation derivative of theta
 * int maxTrainIters             //The maximum number of training iterations before the training algorithm will stop
 * double errorThreshold         //The minimum value the error must reach before training stops unless max iterations are reached
 * String loadOrRand             //Determines if weights are loaded in by the user or randomized
 * String weightFile             //The file which holds the user loaded weights
 * double[] weightRange          //The range between which the weights are randomized
 * double[][][] loadedWeights    //The weights loaded in by the user
 * double[][] truthTable         //The truth table which holds values for the given function
 * int trainItersCounter         //The current number of training iterations completed
 * double[][][] weights          //The weight values utilized to modify activation values in the network
 * double deltaWeights           //The change in weights between each step
 * double[][] outputs            //The output values calculated after running the network with all test cases of a function
 * double currentError           //The average error of the total errors returned after running all test cases of a function
 * double[][] activations        //The activation values of the network
 * double[][] theta              //Contains the dot products of all hidden layers and their corresponding weights
 * String saveWeightsDecide      //Determines if weights are saved or not
 * String saveWeightFile         //The file to saveWeightsTo
 * long timeElapsed              //The amount of time it takes to train the network
 * int saveInterval              //The interval at which weights are saved
 */

public class ABCD_Network_Backprop
{
   /* 
    * Initialize variables
    */ 
   public String networkConfig;
   public String nodeFile;
   public int numLayers;
   public int[] numActivations;
   public static String trainOrRun;
 
   /*
    * Training variables
    */ 
   public static int numCases;
   public double learningFactor;
   public double[][] psi;
   public int maxTrainIters;
   public double errorThreshold;
   public String loadOrRand;
   public String weightFile;
   public double[] weightRange;
   public double[][][] loadedWeights;
   public double[][] truthTable;
   public int trainItersCounter;
   public double[][][] weights;
   public double deltaWeights;
   public double[][] outputs;
   public double currentError;
   public double[][] activations;
   public double[][] theta;
   public String saveWeightsDecide;
   public File saveWeightFile;
   public long timeElapsed;
   public int saveInterval;


   /*
    * Constructor for the ABC_Network_Backprop class
    */
   public ABCD_Network_Backprop() 
   {
   }

   /*
    * Sets the values of the configuration parameters used in running and training based on a control file
    */
   public void setConfigParams(String filePath) throws FileNotFoundException
   {
      Scanner scan = new Scanner(new File(filePath));
      networkConfig = scan.nextLine();
      nodeFile = scan.nextLine();
      numLayers = scan.nextInt();
      scan.nextLine();
      trainOrRun = scan.nextLine();

      numActivations = new int[numLayers + 1];
      for (int ii = 0; ii <= numLayers; ii++)
      {
         numActivations[ii] = scan.nextInt();
      }
      scan.nextLine();
      loadOrRand = scan.nextLine();
      weightFile = scan.nextLine();

      weightRange = new double[2];
      weightRange[0] = scan.nextDouble();
      weightRange[1] = scan.nextDouble();
      scan.nextLine();

      numCases = scan.nextInt();
      learningFactor = scan.nextDouble();
      maxTrainIters = scan.nextInt();
      errorThreshold = scan.nextDouble();
      scan.nextLine();

      saveWeightsDecide = scan.nextLine();
      saveWeightFile = new File(scan.nextLine());
      saveInterval = scan.nextInt();
      scan.close();
   } // public void setConfigParams(String filePath) throws FileNotFoundException

   /*
    * Sets the values of the configuration parameters used in training
    */
   public void setTrainConfigParams() 
   {
      learningFactor = 0.3;
      maxTrainIters = 100000;
      errorThreshold = 0.0002;
      trainItersCounter = 0;
   } // public void setTrainConfigParams()

   /*
    * Allocates space to each array
    */
   public void allocateArray() 
   {
      int maxActivations = 0;
      for (int n = 0; n <= numLayers; n++) 
      {
         maxActivations = Math.max(numActivations[n], maxActivations);
      }

      activations = new double[numLayers + 1][maxActivations];
      truthTable = new double[numCases][numActivations[0] + numActivations[numLayers]];
      weights = new double[numLayers][maxActivations][maxActivations];
      outputs = new double[numCases][numActivations[numLayers]];
      
   } //public void allocateArray() 

   /*
    * Allocates space to the deltaWeights array which is only used for training
    */
   public void allocateTrainArrays() 
   {
      int maxActivations = 0;
      for (int n = 0; n <= numLayers; n++) 
      {
         maxActivations = Math.max(numActivations[n], maxActivations);
      }
      psi = new double[numLayers + 1][maxActivations];
      theta = new double[numLayers + 1][maxActivations];
   } //public void allocateTrainArrays() 

   /*
    * Prints out the values of the configuration parameters
    */
   public void echoRunConfigParams() 
   {
      System.out.println("-----------------------------");
      System.out.println("Network Configuration: " + networkConfig + "\n");

      System.out.println("Number of activations in each layer: ");
      printNumActivations();

      System.out.println("\nNumber of Layers: " + numLayers);
      System.out.println("\nTraining or Running: " + trainOrRun);

      printTruthTable();
      System.out.println();
   } // public void echoRunConfigParams()

   /*
    * Prints out the values of the configuration parameters
    */
   public void echoTrainConfigParams() 
   {
      System.out.println("-----------------------------");
      System.out.println("Network Configuration: " + networkConfig + "\n");

      System.out.println("Number of activations in each layer: ");
      printNumActivations();

      System.out.println("\nNumber of Layers: " + numLayers);
      System.out.println("\nTraining or Running: " + trainOrRun);

      printTruthTable();

      System.out.println("\nLearning factor (lambda): " + learningFactor + "\n");
      System.out.println("Max number of training iterations: " + maxTrainIters + "\n");
      System.out.println("Random Number Range: " + weightRange[0] + ", " + weightRange[1] + "\n");
      System.out.println("Weights randomized or loaded: " + loadOrRand + "\n");
      System.out.println("Error Threshold: " + errorThreshold + "\n");
   } // public void echoTrainConfigParams()

   /*
    * Prints the number of activations in each layer of the network
    */
   public void printNumActivations() 
   {
      for (int n = 0; n < numLayers; n++) 
      {
         System.out.print(numActivations[n] + ", ");
      }
      System.out.println(numActivations[numLayers]);
   } //public void printNumActivations() 

   /*
    * Prints the double values of elements in the truth table
    */
   public void printTruthTable() 
   {
      for (int r = 0; r < numCases; r++)
      {
         for (int c = 0; c < numActivations[0] + numActivations[numLayers]; c++)
         {
            System.out.print(truthTable[r][c] + "  ");
         }
         System.out.println("");
      }
   } //public void printTruthTable() 

   /*
    * Adds data to each array
    */
   public void populateArrays() throws IOException 
   {
      if (loadOrRand.equals("load")) 
      {
         loadWeights(weightFile);
      } 
      else 
      {
         randomizeWeights();
      }

      loadCases(nodeFile);
   } // public void populateArrays() throws IOException

   /*
    * Loads weights into the weights array from a given file
    *
    * @param fileName   the given file to load weights from
    */
   public void loadWeights(String fileName) throws IOException 
   {
      Scanner scan = new Scanner(new File(fileName));
      for (int n = 0; n < numLayers; n++) 
      {
         for (int k = 0; k < numActivations[n]; k++) 
         {
            for (int j = 0; j < numActivations[n + 1]; j++) 
            {
               weights[n][k][j] = (scan.nextDouble());
               System.out.print(j);
            }
         }
      } // for (int n = 0; n < numLayers; n++)
      scan.close();
   } // public void loadWeights(String fileName) throws IOException

   /*
    * Saves the final weights to a given file
    * 
    * @param fileName   the given file to save the weights to
    */
   public void saveWeights(File fileName) throws IOException
   {
      fileName = new File(fileName.getName());
      try 
      {
         BufferedWriter myWriter = new BufferedWriter(new FileWriter(fileName));
         
         String weight;
         for (int n = 0; n < numLayers; n++) 
         {
            for (int k = 0; k < numActivations[n]; k++) 
            {
               for (int j = 0; j < numActivations[n + 1]; j++) 
               {
                  weight = weights[n][k][j] + " ";
                  myWriter.write(weight);
               }
            } // for (int k = 0; k < numActivations[n]; k++)
         } // for (int n = 0; n < numLayers; n++)
         myWriter.flush();  
         myWriter.close();
      } //try
      catch (IOException e) 
      {
         System.out.println("An error occurred.");
         e.printStackTrace();
      }
   } // public void saveWeights(String fileName) throws IOException

   /*
    * Randomizes the weights in the weights array for training
    */
   public void randomizeWeights() 
   {
      double low = weightRange[0];
      double high = weightRange[1];

      for (int n = 0; n < numLayers; n++) 
      {
         for (int k = 0; k < numActivations[n]; k++) 
         {
            for (int j = 0; j < numActivations[n + 1]; j++) 
            {
               weights[n][k][j] = (Math.random() * (high - low)) + low;
            }
         }
      } // for (int n = 0; n < numLayers; n++)
   } // public void randomizeWeights()

   /*
    * Reads and stores the values of a given truth table file in the truthTable array
    * 
    * @param fileName   the given file to load the test cases from
    */
   public void loadCases(String fileName) throws IOException 
   {
      Scanner scan = new Scanner(new File(fileName));
      for (int r = 0; r < numCases; r++) 
      {
         for (int c = 0; c < numActivations[0] + numActivations[numLayers]; c++) 
         {
            truthTable[r][c] = (double) (scan.nextInt());
         }
      }
      scan.close();
   } // public void loadCases(String fileName) throws IOException

   /*
    * Displays the outputs after running the network for all the given training cases.
    */
   public void reportResults() throws IOException 
   {
      System.out.println("Calculated outputs:");
      for (int r = 0; r < numCases; r++) // Iterates over the number of cases in the truth table
      {
         for (int c = 0; c < numActivations[0]; c++)
         {
            System.out.print(truthTable[r][c] + "  ");
         }

         for (int c = 0; c < numActivations[numLayers]; c++)
         {
            System.out.print(outputs[r][c]);
            System.out.print("  ");
         }
         System.out.println();
      } // for (int r = 0; r < NUM_TRAINING_CASES; r++)
      System.out.println();
      if (trainOrRun.equals("train"))
      {
         System.out.println("Training took " + timeElapsed + " milliseconds");
         System.out.println();
      }
   } // public void reportResults() throws IOException

   /*
    * Runs the network and calculates the outputs after modifying the activation values 
    * for each training case from a given truth table
    *
    * @param caseIndex   the case that the network is being run for
    */
   public double[] run(int caseIndex) 
   {
      for (int input = 0; input < numActivations[0]; input++) 
      {
         activations[0][input] = truthTable[caseIndex][input];
      }

      double calcTheta = 0.0;

      for (int n = 0; n < numLayers; n++) 
      {
         for (int j = 0; j < numActivations[n + 1]; j++) 
         {
            calcTheta = 0.0;
            for (int k = 0; k < numActivations[n]; k++) 
            {
               calcTheta += activations[n][k] * weights[n][k][j];
            }
            activations[n + 1][j] = activationFunction(calcTheta);
         } // for (int j = 0; j < numActivations[n + 1]; j++)
      } // for (int n = 0; n < numLayers; n++)

      for (int i = 0; i < numActivations[numLayers]; i++) 
      {
         outputs[caseIndex][i] = activations[numLayers][i];
      }
      return outputs[caseIndex];
   } // public double[] run(int caseIndex)

   /*
    * Runs the network and calculates the outputs after modifying the activation
    * values for each training case from a given truth table
    *
    * @param caseIndex   the case that the network is being trained for
    */
   public double[][] runForTrain(int caseIndex) 
   {
      for (int input = 0; input < numActivations[0]; input++) 
      {
         activations[0][input] = truthTable[caseIndex][input];
      }

      double calcTheta = 0.0;

      for (int n = 0; n < numLayers; n++) 
      {
         for (int j = 0; j < numActivations[n + 1]; j++) 
         {
            calcTheta = 0.0;
            for (int k = 0; k < numActivations[n]; k++) 
            {
               calcTheta += activations[n][k] * weights[n][k][j];
            }

            theta[n + 1][j] = calcTheta;
            activations[n + 1][j] = activationFunction(calcTheta);
         } // for (int j = 0; j < numActivations[n + 1]; j++)
      } // for (int n = 0; n < NUM_LAYERS; n++)

      for (int i = 0; i < numActivations[numLayers]; i++) 
      {
         psi[numLayers][i] = (truthTable[caseIndex][i + numActivations[0]] - activations[numLayers][i]) * activationDerivative(theta[numLayers][i]);
      }
      return psi;
   } // public double[][] runForTrain(int caseIndex)

   /*
    * Computes an activation function
    *
    * @param dotProduct the dot product of an activation value and a corresponding
    * weight
    */
   public double activationFunction(double dotProduct) 
   {
      return sigmoid(dotProduct);
   }

   /*
    * Computes the derivative of an activation function
    *
    * @param x the variable of the activation function
    */
   public double activationDerivative(double x) 
   {
      return sigmoidDerivative(x);
   }

   /*
    * Computes the sigmoid function
    *
    * @param x the exponent variable in the sigmoid function
    */
   public double sigmoid(double x) 
   {
      double denominator = 1.0 + Math.exp(-x);
      return 1.0 / denominator;
   }

   /*
    * Computes the derivative of the sigmoid function
    *
    * @param x the variable in the sigmoid function
    */
   public double sigmoidDerivative(double x) 
   {
      double sig = sigmoid(x);
      return (sig) * (1.0 - sig);
   }

   /*
    * Trains the network by calculating error values between the target and calculated 
    * output values and implementing steepest descent to modify the weights and decrease the 
    * error value. The training algorithm stops running if the maximum number of iterations is 
    * reached or if the error falls within the error threshold.
    */
   public void train() throws IOException 
   {
      Instant start = Instant.now();
      System.out.println("----------\nTRAINING: \n");
      currentError = Integer.MAX_VALUE;
      
      boolean maxItersReached = false;
      double bigOmega = 0.0;

      while (!(currentError <= errorThreshold) && (!maxItersReached)) 
      {
         currentError = 0.0;
         
      /*
       * Calculates the error received for each training case and stores the values in the errors array
       */
         for (int ii = 0; ii < numCases; ii++) 
         {
            runForTrain(ii);

            for (int n = numLayers - 1; n >= 0; n--)
            {
               for (int j = 0; j < numActivations[n]; j++)
               {
                  bigOmega = 0.0;
      
                  for (int i = 0; i < numActivations[n + 1]; i++)
                  {
                     bigOmega += psi[n + 1][i] * weights[n][j][i];
                     deltaWeights = learningFactor * activations[n][j] * psi[n + 1][i];
                     weights[n][j][i] += deltaWeights;
                  }
      
                  psi[n][j] = activationDerivative(theta[n][j]) * bigOmega;
               } // for (int j = 0; j < noActivations[n]; j++)
            } // for (int n = numLayers - 1; n >= 0; n--)
            
            run(ii);

            for (int i = 0; i < numActivations[numLayers]; i++)
            {
               currentError += 0.5 * ((truthTable[ii][i + numActivations[0]] - activations[numLayers][i]) * (truthTable[ii][i + numActivations[0]] - activations[numLayers][i]));
            }
         } // for (int ii = 0; ii < numCases; ii++)

         trainItersCounter++;

         if (trainItersCounter == maxTrainIters) 
         {
            maxItersReached = true;
         }

         currentError = currentError / (double) numCases;
         Instant end = Instant.now();
         timeElapsed = Duration.between(start, end).toMillis();
         
         if (saveWeightsDecide.equals("save") && saveInterval > 0 && trainItersCounter % saveInterval == 0)
         {
            saveWeights(saveWeightFile);
         }
      } // while (!(currentError <= errorThreshold) && (!maxItersReached))

      if (saveWeightsDecide.equals("save"))
      {
         saveWeights(saveWeightFile);
      }
   /* 
    * Reasoning for exiting the training algorithm 
    */
      if (maxItersReached) 
      {
         System.out.println("The maximum number of training iterations of " + maxTrainIters + " has been reached. \n");
      } 
      else 
      {
         System.out.println("The error threshold of " + errorThreshold + " has been reached. \n");
      }
      System.out.println("Iterations Reached: " + trainItersCounter + "\n");
      System.out.println("Error Reached: " + currentError + "\n");
   } // public void train() throws IOException 

   /*
    * Initializes variables and proceeds to run the network or train it using steepest descent.
    * Displays output values for each test case.
    */
   public static void main(String[] args) throws IOException 
   {
      ABCD_Network_Backprop perceptron = new ABCD_Network_Backprop();
      String filePath = "./controlFile.txt";
      if (args.length > 0)
      {
         filePath = args[0];
      }
      perceptron.setConfigParams(filePath);
      perceptron.allocateArray();
      perceptron.populateArrays();
      
      if (trainOrRun.equals("train")) 
      {
         perceptron.allocateTrainArrays();
         perceptron.echoTrainConfigParams();
         perceptron.train();
         for   (int ii = 0; ii < numCases; ii++)
         {
            perceptron.run(ii);
         }
         perceptron.reportResults();
      } // if (trainOrRun.equals("train"))
      else
      {
         perceptron.echoRunConfigParams();
         for (int ii = 0; ii < numCases; ii++) 
         {
            perceptron.run(ii);
         }
         perceptron.reportResults();
      } // else
   } // public static void main(String[] args) throws IOException
} // public class ABC_Network_Backprop
