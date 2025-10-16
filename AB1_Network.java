import java.io.File;
import java.io.IOException;
import java.util.Scanner;
/*
 * Sania Gupta
 * 
 * 31 August 2023
 * 
 * Runs and trains an A-B-1 network with XOR, OR, or AND to return values that near the target values determined by a truth table 
 * using steepest descent.
 * ----------------------------------
 * VARIABLE DESCRIPTIONS:
 * 
 * String networkConfig          //The type of network configuration
 * String function               //The type of function being run (XOR, OR, AND)
 * int numLayers                 //The number of connectivity layers in the network
 * int[] numActivations          //The number of activations in each layer of the network
 * String trainOrRun             //Determines if the network will train or run
 * 
 * int numCases                  //The total number of test cases used for training
 * double learningFactor         //The learning factor, or lambda, used to control how much the weights are modified each step
 * int maxTrainIters             //The maximum number of training iterations before the training algorithm will stop
 * double errorThreshold         //The minimum value the error must reach before training stops unless max iterations are reached
 * String loadOrRand             //Determines if weights are loaded in by the user or randomized
 * double[] weightRange          //The range between which the weights are randomized
 * double[][][] loadedWeights    //The weights loaded in by the user
 * 
 * double[][] truthTable         //The truth table which holds values for the given function
 * int trainItersCounter         //The current number of training iterations completed
 * double[][][] weights          //The weight values utilized to modify activation values in the network
 * double[][][] deltaWeights     //The change in weights between each step
 * double[][] outputs            //The output values calculated after running the network with all test cases of a function
 * double currentError           //The average error of the total errors returned after running all test cases of a function
 * double[][] activations        //The activation values of the network
 * double[][] theta              //Contains the dot products of all hidden layers and their corresponding weights
 */

public class AB1_Network 
{
   // Initialize variables
   public String networkConfig;  
   public String function;
   public int numLayers;
   public int[] numActivations;
   public static String trainOrRun;
   
   // Training variables
   public static int numCases;
   public double learningFactor;
   public int maxTrainIters;
   public double errorThreshold;
   public String loadOrRand;
   public double[] weightRange;
   public double[][][] loadedWeights;
   public double[][] truthTable;
   public int trainItersCounter;
   public double[][][] weights;
   public double[][][] deltaWeights;
   public double[][] outputs;
   public double currentError;
   public double[][] activations;
   public double[][] theta;

   /*
    * Constructor for the AB1_Network class
    */
   public AB1_Network() 
   {
   }

   /*
    * Sets the values of the configuration paramaters used in running and training
    */
   public void setConfigParams() 
   {
      networkConfig = "A-B-1";
      function = "OR";
      numLayers = 2;
      numActivations = new int[] { 2, 4, 1 };
      trainOrRun = "train";
      loadOrRand = "rand";
      weightRange = new double[] { -1.5, 1.5 };
      numCases = 4;
            
      if (trainOrRun.equals("train"))
      {
         setTrainConfigParams();
      }
   } // public void setConfigParams()

   /*
    * Sets the values of the configuration paramaters used in training
    */
   public void setTrainConfigParams()
   {
      learningFactor = 0.3;
      maxTrainIters = 100000;
      errorThreshold = 0.002;
      loadedWeights = new double[][][]
      {
         {
            { 0.8, 0.9 },
            { 0.9, 0.8 }
         },
         {
            { 0.2 },
            { 0.2 }
         }
      };
      trainItersCounter = 0;
   } //public void setTrainConfigParams()

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
      truthTable = new double[numCases][numLayers + numActivations[numLayers]];
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
      theta = new double[numLayers + 1][maxActivations];
      deltaWeights = new double[numLayers][maxActivations][maxActivations];
   }

   /*
    * Prints out the values of the configuration paramaters
    */
   public void echoRunConfigParams() 
   {
      System.out.println("-----------------------------");
      System.out.println("Network Configuration: " + networkConfig + "\n");

      System.out.println("Number of activations in each layer: ");
      printNumActivations();

      System.out.println("\nNumber of Layers: " + numLayers);
      System.out.println("\nTraining or Running: " + trainOrRun);

      System.out.println("\nTruth table for " + function);
      printTruthTable();
      System.out.println();
   } // public void echoConfigParams()

   /*
    * Prints out the values of the configuration paramaters
    */
   public void echoTrainConfigParams() 
   {
      System.out.println("-----------------------------");
      System.out.println("Network Configuration: " + networkConfig + "\n");

      System.out.println("Number of activations in each layer: ");
      printNumActivations();

      System.out.println("\nNumber of Layers: " + numLayers);
      System.out.println("\nTraining or Running: " + trainOrRun);

      System.out.println("\nTruth table for " + function);
      printTruthTable();

      System.out.println("\nLearning factor (lambda): " + learningFactor + "\n");
      System.out.println("Max number of training iterations: " + maxTrainIters + "\n");
      System.out.println("Random Number Range: " + weightRange[0] + ", " + weightRange[1] + "\n");
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
    * Prints the double values of elements in the truth table for a specific function
    */
   public void printTruthTable() 
   {
      for (int r = 0; r < numCases; r++) //Iterates over the number of cases in the truth table
      {
         for (int c = 0; c < numActivations[0] + numActivations[numLayers]; c++) //Iterates over the number of activation values
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
         loadWeights();
      } 
      else 
      {
         randomizeWeights();
      }

      loadCases("./" + function + "_table.txt");
   } //public void populateArrays() throws IOException 

   public void loadWeights() 
   {
      for (int n = 0; n < numLayers; n++) 
      {
         for (int k = 0; k < numActivations[n]; k++) 
         {
            for (int j = 0; j < numActivations[n + 1]; j++) 
            {
               weights[n][k][j] = loadedWeights[n][k][j];
            }
         }
      }
   } //public void loadWeights() 

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
      } //for (int n = 0; n < numLayers; n++)
   } // public void randomizeWeights() 

   /*
    * Reads and stores the values of a given truth table file in the truthTable array
    */
   public void loadCases(String fileName) throws IOException 
   {
      System.out.println("loadCases");
      Scanner scan = new Scanner(new File(fileName));
      for (int r = 0; r < numCases; r++) 
      {
         for (int c = 0; c < numActivations[0] + numActivations[numLayers]; c++) 
         {
            truthTable[r][c] = 
            (double) (scan.nextInt());
         }
      }
      scan.close();
   } //public void loadCases(String fileName) throws IOException 

   /*
    * Displays the outputs after running the network for all the given training cases.
    */
   public void reportResults() 
   {
      System.out.println("Calculated outputs:");
      for (int r = 0; r < numCases; r++)               //Iterates over the number of cases in the truth table 
      {
         for (int c = 0; c < numActivations[0]; c++)   //Iterates over the number of activation nodes in the input layer
         {
            System.out.print(truthTable[r][c] + "  ");
         }

         for (int c = 0; c < numActivations[numLayers]; c++) //Iterates over the number of activation nodes in the output layer
         {
            System.out.print(outputs[r][c]);
         }
         System.out.println();
      } //for (int r = 0; r < NUM_TRAINING_CASES; r++)

      System.out.println();
   } //public void reportResults() 

   /*
    * Runs the network and calculates the outputs after modifying the activation values for each training case 
    * from a given truth table
    */
   public double[] run(int index) 
   {
      for (int input = 0; input < numActivations[0]; input++) 
      {
         activations[0][input] = truthTable[index][input];
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
         } //for (int j = 0; j < numActivations[n + 1]; j++)
      } //for (int n = 0; n < NUM_LAYERS; n++)

      for (int i = 0; i < numActivations[numLayers]; i++) 
      {
         outputs[index][i] = activations[numLayers][i];
      }
      return outputs[index];
   } //public double[] run(int index)

   /*
    * Runs the network and calculates the outputs after modifying the activation values for each training case 
    * from a given truth table
    */
   public double[] runForTrain(int index) 
   {
      for (int input = 0; input < numActivations[0]; input++) 
      {
         activations[0][input] = truthTable[index][input];
         //theta[0][input] = truthTable[index][input];
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
         } //for (int j = 0; j < numActivations[n + 1]; j++)
      } //for (int n = 0; n < NUM_LAYERS; n++)

      for (int i = 0; i < numActivations[numLayers]; i++) 
      {
         outputs[index][i] = activations[numLayers][i];
      }
      return outputs[index];
   } //public double[] runForTrain(int index) 

   /*
    * Computes an activation function
    *
    * @param dotProduct the dot product of an activation value and a corresponding weight
    */
   public double activationFunction(double dotProduct) 
   {
      return sigmoid(dotProduct);
   }

   /*
    * Computes the derivative of an activation function
    *
    * @param x    the variable of the activation function
    */
   public double activationDerivative(double x) 
   {
      return sigmoidDerivative(x);
   }

   /*
    * Computes the sigmoid function
    *
    * @param x    the exponent variable in the sigmoid function
    */
   public double sigmoid(double x) 
   {
      double denominator = 1.0 + Math.exp(-x);
      return 1.0 / denominator;
   }

   /*
    * Computes the derivative of the sigmoid function
    *
    * @param x    the variable in the sigmoid function
    */
   public double sigmoidDerivative(double x) 
   {
      double sig = sigmoid(x);
      return (sig) * (1.0 - sig);
   }

   /*
    * Trains the network by calculating error values between the target and calculated output values and implementing
    * steepest descent to modify the weights and decrease the error value. The training algorithm stops running if the maximum
    * number of iterations is reached or if the error falls within the error threshold.
    */
   public void train() 
   {
      System.out.println("----------\nTRAINING: \n");
      currentError = Integer.MAX_VALUE;

      // calculated value
      double[] F = new double[numCases];
      // target value
      double[] T = new double[numCases];

      double[] errors = new double[numCases];

      boolean maxItersReached = false;

      while (!(currentError <= errorThreshold) && (!maxItersReached)) 
      {
         //Calculates the error received for each training case and stores the values in the errors array
         for (int ii = 0; ii < numCases; ii++) 
         {
            F[ii] = runForTrain(ii)[0];
            T[ii] = truthTable[ii][2];

            for (int n = 0; n < numLayers; n++) 
            {
               for (int k = 0; k < numActivations[n]; k++) 
               {
                  for (int j = 0; j < numActivations[n + 1]; j++) 
                  {
                     deltaWeights[n][k][j] = -learningFactor * calcPartialDeriv(n, k, j, F[ii], T[ii]);
                     weights[n][k][j] += deltaWeights[n][k][j];   //Adjusts the values of the weights
                  }
               }
            }
            errors[ii] = 0.5 * ((T[ii] - F[ii]) * (T[ii] - F[ii]));
         } //for (int ii = 0; ii < numCases; ii++) 
         trainItersCounter++;

         if (trainItersCounter == maxTrainIters) 
         {
            maxItersReached = true;
         }
         
         for (int ii = 0; ii < numCases; ii++) 
         {
            currentError += errors[ii];
         }

         currentError = currentError / (double)numCases;
      } //while (!(currentError <= errorThreshold) && (!maxItersReached)) 

      //Reasoning for exiting the training algorithm:
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

   } // public void train()

   /*
    * Calculates the partial derivative of the error function
    *
    * @param currLayer  the current layer of the weight
    * @param currUnit   the unit in the current layer
    * @param nextUnit   the next unit that the weight is directed to
    * @param output     the calculated output of the network with adjustment by the given weights
    * @param target     the desired output from the truth table
    */
   public double calcPartialDeriv(int currLayer, int currUnit, int nextUnit, double output, double target) 
   {
      double value = -(target - output) * activations[currLayer][currUnit];

      if (currLayer == 0) 
      {
         value *= activationDerivative(theta[1][nextUnit]) * weights[1][nextUnit][0];
      }
      value *= activationDerivative(theta[numLayers][0]);
      
      return value;
   } //public double calcPartialDeriv(int currLayer, int currUnit, int nextUnit, double output, double target)

   /*
    * Initializes variables and proceeds to run the network or train it using steepest descent. 
    * Displays output values for each test case.
    */
   public static void main(String[] args) throws IOException 
   {
      AB1_Network perceptron = new AB1_Network();
      perceptron.setConfigParams();
      perceptron.allocateArray();
      perceptron.populateArrays();

      if (trainOrRun.equals("train")) //If training
      {
         perceptron.allocateTrainArrays();
         perceptron.echoTrainConfigParams();
         perceptron.train();
         perceptron.reportResults();
      } 
      else //If running
      {
         perceptron.echoRunConfigParams();
         for (int ii = 0; ii < numCases; ii++) //Run for all training cases
         {
            perceptron.run(ii);
         }
         perceptron.reportResults();
      }
   } // public static void main(String[] args) throws IOException
} //public class AB1_Network 
