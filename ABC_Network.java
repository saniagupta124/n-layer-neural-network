import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
/*
 * Sania Gupta
 * 
 * 31 August 2023
 * 
 * Runs and trains an A-B-C network with XOR, OR, or AND to return values that near the target values determined by a truth table 
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
 * double littlePsi              //The value of omega multiplied by the activation derivative of theta
 * int maxTrainIters             //The maximum number of training iterations before the training algorithm will stop
 * double errorThreshold         //The minimum value the error must reach before training stops unless max iterations are reached
 * String loadOrRand             //Determines if weights are loaded in by the user or randomized
 * double[] weightRange          //The range between which the weights are randomized
 * double[][][] loadedWeights    //The weights loaded in by the user
 * double[][] truthTable         //The truth table which holds values for the given function
 * int trainItersCounter         //The current number of training iterations completed
 * double[][][] weights          //The weight values utilized to modify activation values in the network
 * double[][][] deltaWeights     //The change in weights between each step
 * double[][] outputs            //The output values calculated after running the network with all test cases of a function
 * double currentError           //The average error of the total errors returned after running all test cases of a function
 * double[][] activations        //The activation values of the network
 * double[][] theta              //Contains the dot products of all hidden layers and their corresponding weights
 */

public class ABC_Network 
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
    public double[] littlePsi;
    public int maxTrainIters;
    public double errorThreshold;
    public String loadOrRand;
    public String weightFile;
    public double[] weightRange;
    public double[][][] loadedWeights;
    public double[][] truthTable;
    public int trainItersCounter;
    public double[][][] weights;
    public double[][][] deltaWeights;
    public double[][] outputs;
    public double currentError;
    public double[] errors;
    public double[][] activations;
    public double[][] theta;
    public double[] omega;

   /*
    * Constructor for the ABC_Network class
    */
   public ABC_Network() 
   {
   }

   /*
    * Sets the values of the configuration parameters used in running and training
    */
   public void setConfigParams() 
   {
      networkConfig = "A-B-C";
      nodeFile = "./truthTable.txt";
      numLayers = 2;
      numActivations = new int[] { 2, 5, 3 };
      trainOrRun = "train";
      loadOrRand = "rand";
      weightFile = "./weightsFile.txt";
      weightRange = new double[] { 0.1, 1.5 };
      numCases = 4;
 
      if (trainOrRun.equals("train")) 
      {
         setTrainConfigParams();
      }
   } // public void setConfigParams()

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
   }

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
      littlePsi = new double[numActivations[numLayers]];
      theta = new double[numLayers + 1][maxActivations];
      deltaWeights = new double[numLayers][maxActivations][maxActivations];
      errors = new double[numCases];
   }

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
   } // public void echoConfigParams()

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
   }

   /*
    * Prints the double values of elements in the truth table
    */
   public void printTruthTable() 
   {
      for (int r = 0; r < numCases; r++) // Iterates over the number of cases in the truth table
      {
         for (int c = 0; c < numActivations[0] + numActivations[numLayers]; c++) // Iterates over the number of activation values
         {
            System.out.print(truthTable[r][c] + "  ");
         }
         System.out.println("");
      }
   }

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
   public void saveWeights(String fileName)
   {
      try 
      {
         FileWriter myWriter = new FileWriter(fileName);
         for (int n = 0; n < numLayers; n++) 
         {
            for (int k = 0; k < numActivations[n]; k++) 
            {
               for (int j = 0; j < numActivations[n + 1]; j++) 
               {
                  String weight = String.valueOf(weights[n][k][j] + " ");
                  myWriter.write(weight);
               }
            } // for (int k = 0; k < numActivations[n]; k++)
         } // for (int n = 0; n < numLayers; n++)
         myWriter.close();
      } //try
      catch (IOException e) 
      {
         System.out.println("An error occurred.");
         e.printStackTrace();
      }
   } // public void saveWeights(String fileName)

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
   public void reportResults() 
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
   } // public void reportResults()

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

      for (int n = 0; n < numLayers; n++) 
      {
         for (int j = 0; j < numActivations[n + 1]; j++) 
         {
            double dotProduct = 0.0;
            for (int k = 0; k < numActivations[n]; k++) 
            {
               dotProduct += activations[n][k] * weights[n][k][j];
            }
            activations[n + 1][j] = activationFunction(dotProduct);
         } // for (int j = 0; j < numActivations[n + 1]; j++)
      } // for (int n = 0; n < NUM_LAYERS; n++)

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
   public double[] runForTrain(int caseIndex) 
   {
      for (int input = 0; input < numActivations[0]; input++) 
      {
         activations[0][input] = truthTable[caseIndex][input];
      }

      for (int n = 0; n < numLayers; n++) 
      {
         for (int j = 0; j < numActivations[n + 1]; j++) 
         {
            double dotProduct = 0.0;
            for (int k = 0; k < numActivations[n]; k++) 
            {
               dotProduct += activations[n][k] * weights[n][k][j];
            }

            theta[n + 1][j] = dotProduct;
            activations[n + 1][j] = activationFunction(dotProduct);
         } // for (int j = 0; j < numActivations[n + 1]; j++)
      } // for (int n = 0; n < NUM_LAYERS; n++)

      for (int i = 0; i < numActivations[numLayers]; i++) 
      {
         outputs[caseIndex][i] = activations[numLayers][i];
      }
      return outputs[caseIndex];
   } // public double[] runForTrain(int caseIndex)

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
   public void train() 
   {
      System.out.println("----------\nTRAINING: \n");
      currentError = Integer.MAX_VALUE;

      double[][] T = new double[numCases][numActivations[numLayers]]; //target value
      boolean maxItersReached = false;

      while (!(currentError <= errorThreshold) && (!maxItersReached)) 
      {
         currentError = 0;
         
         double[][] F = new double[numCases][numActivations[numLayers]]; //calculated value
         double[] errors = new double[numCases];

      /*
       * Calculates the error received for each training case and stores the values in the errors array
       */
         for (int ii = 0; ii < numCases; ii++) 
         {
            for (int i = 0; i < numActivations[numLayers]; i++) 
            {
               F[ii][i] = runForTrain(ii)[i];
               T[ii][i] = truthTable[ii][i + 2];
               double omega;

               omega = T[ii][i] - F[ii][i];
               littlePsi[i] = omega * activationDerivative(theta[numLayers][i]);

               for (int j = 0; j < numActivations[numLayers - 1]; j++) // loop for output layer
               {
                  double partialDeriv;
                  partialDeriv = -(activations[numLayers - 1][j]) * littlePsi[i];
                  deltaWeights[numLayers - 1][j][i] = -learningFactor * partialDeriv;
               }
            } //for (int i = 0; i < numActivations[numLayers]; i++)
               
            for (int k = 0; k < numActivations[0]; k++) //loop for hidden layers
            {
               for (int j = 0; j < numActivations[1]; j++) 
               {
                  double bigOmega = 0.0;
                  double bigPsi;
                  double partialDeriv;

                  for (int i = 0; i < numActivations[numLayers]; i++)
                  {
                     bigOmega += littlePsi[i] * weights[1][j][i];
                  }

                  bigPsi = bigOmega * activationDerivative(theta[1][j]);
                  partialDeriv = -(activations[0][k]) * bigPsi;
                  deltaWeights[0][k][j] = -learningFactor * partialDeriv;
               } // for (int j = 0; j < numActivations[n + 1]; j++)
            } // for (int k = 0; k < numActivations[n]; k++)
               
            for (int n = 0; n < numLayers; n++) 
            {
               for (int k = 0; k < numActivations[n]; k++) 
               {
                  for (int j = 0; j < numActivations[n + 1]; j++) 
                  {
                     weights[n][k][j] += deltaWeights[n][k][j]; // Adjusts the values of the weights
                  }
               }
            } //for (int n = 0; n < numLayers; n++) 
               
            for (int i = 0; i < numActivations[numLayers]; i++)
            {
               errors[ii] += 0.5 * ((T[ii][i] - F[ii][i]) * (T[ii][i] - F[ii][i]));
            }
         } // for (int ii = 0; ii < numCases; ii++)

         trainItersCounter++;

         if (trainItersCounter == maxTrainIters) 
         {
            maxItersReached = true;
         }

         for (int ii = 0; ii < numCases; ii++) 
         {
            currentError += errors[ii];
         }

         currentError = currentError / (double) numCases;
      } // while (!(currentError <= errorThreshold) && (!maxItersReached))

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
      saveWeights("savedWeights.txt");
   } // public void train()

   /*
    * Initializes variables and proceeds to run the network or train it using steepest descent.
    * Displays output values for each test case.
    */
   public static void main(String[] args) throws IOException 
   {
      ABC_Network perceptron = new ABC_Network();
      perceptron.setConfigParams();
      perceptron.allocateArray();
      perceptron.populateArrays();

      if (trainOrRun.equals("train")) // If training
      {
         perceptron.allocateTrainArrays();
         perceptron.echoTrainConfigParams();
         perceptron.train();
         perceptron.reportResults();
      } 
      else // If running
      {
         perceptron.echoRunConfigParams();
         for (int ii = 0; ii < numCases; ii++) // Run for all training cases
         {
            perceptron.run(ii);
         }
         perceptron.reportResults();
      }
   } // public static void main(String[] args) throws IOException
} // public class ABC_Network
