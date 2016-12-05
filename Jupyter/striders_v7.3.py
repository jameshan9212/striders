import os
import sys
import numpy as  np
from datetime import datetime

# arguments
# params: FILE_NAME
# params: MODEL_NAME
# params: MAKE || COMPARE FLAG
#
# 0 is to make new model
# 1 is to compare with model


# ex: python strider.py [FILE_NAME] [COMPAGE_FLAG]
# sys.argv[0]: 
# sys.argv[1]: FILE_NAME
# sys.argv[2]: MODEL_NAME
# sys.argv[3]: COMPARE_FLAG

#FILE_NAME = sys.argv[1]
#MODEL_NAME = sys.argv[2]
#COMPARE_FLAG = int(sys.argv[3])


FILE_NAME = "id_js"
MODEL_NAME = "md_hyunryung"
COMPARE_FLAG = 1

MODEL_DIR = MODEL_NAME + "_DIR"
CURR_DIR = os.getcwd()

STEP_SIZE = 5
RANGE_SIZE = 60
RANGE_DIM = 60
HIDDEN_DIM = 100
EXIST_FLAG = False
EPOCH_NUM = 25

IF_DEBUG = False
#########################################################################

# acc vector
pair_acc_x = np.empty(shape=[1], dtype=int)
pair_acc_y = np.empty(shape=[1], dtype=int)
pair_acc_z = np.empty(shape=[1], dtype=int)

# Read the data and make tokens
print "Reading data file '%s' ..." % FILE_NAME
with open(FILE_NAME, 'r') as fp:

        for line in fp.readlines():

            sentence = line.split()

            sens_x = int(sentence[0]) + 30
            sens_y = int(sentence[1]) + 30
            sens_z = int(sentence[2]) + 30

            pair_acc_x = np.append(pair_acc_x, np.array([sens_x]), 0)
            pair_acc_y = np.append(pair_acc_y, np.array([sens_y]), 0)
            pair_acc_z = np.append(pair_acc_z, np.array([sens_z]), 0)

# delete the first element
pair_acc_x = np.delete(pair_acc_x, 0, axis=0)
pair_acc_y = np.delete(pair_acc_y, 0, axis=0)
pair_acc_z = np.delete(pair_acc_z, 0, axis=0)

# transpose of elements
pair_acc_x = np.transpose(pair_acc_x)
pair_acc_y = np.transpose(pair_acc_y)
pair_acc_z = np.transpose(pair_acc_z)

# Make the Training Set and Label
TrainX_set = np.array([pair_acc_x[:-STEP_SIZE-1]])
TrainX_label = np.array([pair_acc_x[1:-STEP_SIZE]])

TrainY_set = np.array([pair_acc_y[:-STEP_SIZE-1]])
TrainY_label = np.array([pair_acc_y[1:-STEP_SIZE]])

TrainZ_set = np.array([pair_acc_z[:-STEP_SIZE-1]])
TrainZ_label = np.array([pair_acc_z[1:-STEP_SIZE]])

for i in range(1, STEP_SIZE):
    TrainX_set = np.append(TrainX_set, [pair_acc_x[i:i-STEP_SIZE-1]], 0)
    TrainX_label = np.append(TrainX_label, [pair_acc_x[i+1:i-STEP_SIZE]], 0)

    TrainY_set = np.append(TrainY_set, [pair_acc_y[i:i-STEP_SIZE-1]], 0)
    TrainY_label = np.append(TrainY_label, [pair_acc_y[i+1:i-STEP_SIZE]], 0)

    TrainZ_set = np.append(TrainZ_set, [pair_acc_z[i:i-STEP_SIZE-1]], 0)
    TrainZ_label = np.append(TrainZ_label, [pair_acc_z[i+1:i-STEP_SIZE]], 0)

TrainX_set = np.transpose(TrainX_set)
TrainX_label = np.transpose(TrainX_label)

TrainY_set = np.transpose(TrainY_set)
TrainY_label = np.transpose(TrainY_label)

TrainZ_set = np.transpose(TrainZ_set)
TrainZ_label = np.transpose(TrainZ_label)

if COMPARE_FLAG == 0:
    print "Training set : %s\nLabel set : %s\n" % (len(TrainX_set), len(TrainX_label))    
else:
    print "Testing : %s\n" % len(TrainX_set)

#########################################################################

def softmax(x):
    xt = np.exp(x - np.max(x))
    return xt / np.sum(xt)

class RNNNumpy:
    
    def __init__(self, range_dim, hidden_dim=100, bptt_truncate=4):
        # Assign instance variables
        self.range_dim = range_dim
        self.hidden_dim = hidden_dim
        self.bptt_truncate = bptt_truncate
        # Randomly initialize the network parameters
        self.U = np.random.uniform(-np.sqrt(1./range_dim), np.sqrt(1./range_dim), (hidden_dim, range_dim))
        self.V = np.random.uniform(-np.sqrt(1./hidden_dim), np.sqrt(1./hidden_dim), (range_dim, hidden_dim))
        self.W = np.random.uniform(-np.sqrt(1./hidden_dim), np.sqrt(1./hidden_dim), (hidden_dim, hidden_dim))

def forward_propagation(self, x):
    # The total number of time steps
    T = len(x)
    # During forward propagation we save all hidden states in s because need them later.
    # We add one additional element for the initial hidden, which we set to 0
    s = np.zeros((T + 1, self.hidden_dim))
    s[-1] = np.zeros(self.hidden_dim)
    # The outputs at each time step. Again, we save them for later.
    o = np.zeros((T, self.range_dim))
    # For each time step...
    for t in np.arange(T):
        # Note that we are indxing U by x[t]. This is the same as multiplying U with a one-hot vector.
        s[t] = np.tanh(self.U[:,x[t]] + self.W.dot(s[t-1]))
        o[t] = softmax(self.V.dot(s[t]))
    return [o, s]

RNNNumpy.forward_propagation = forward_propagation

def predict(self, x):
    # Perform forward propagation and return index of the highest score
    o, s = self.forward_propagation(x)
    return np.argmax(o, axis=1)

RNNNumpy.predict = predict

def calculate_total_loss(self, x, y):
    L = 0
    # For each stride...
    for i in np.arange(len(y)):
        o, s = self.forward_propagation(x[i])
        # We only care about our prediction of the "correct" strides
        correct_word_predictions = o[np.arange(len(y[i])), y[i]]
        # Add to the loss based on how off we were
        L += -1 * np.sum(np.log(correct_word_predictions))
    return L

def calculate_loss(self, x, y):
    # Divide the total loss by the number of training examples
    N = np.sum((len(y_i) for y_i in y))
    return self.calculate_total_loss(x,y)/N

RNNNumpy.calculate_total_loss = calculate_total_loss
RNNNumpy.calculate_loss = calculate_loss

def bptt(self, x, y):
    T = len(y)
    # Perform forward propagation
    o, s = self.forward_propagation(x)
    # We accumulate the gradients in these variables
    dLdU = np.zeros(self.U.shape)
    dLdV = np.zeros(self.V.shape)
    dLdW = np.zeros(self.W.shape)
    delta_o = o
    delta_o[np.arange(len(y)), y] -= 1.
    # For each output backwards...
    for t in np.arange(T)[::-1]:
        dLdV += np.outer(delta_o[t], s[t].T)
        # Initial delta calculation
        delta_t = self.V.T.dot(delta_o[t]) * (1 - (s[t] ** 2))
        # Backpropagation through time (for at most self.bptt_truncate steps)
        for bptt_step in np.arange(max(0, t-self.bptt_truncate), t+1)[::-1]:
            # print "Backpropagation step t=%d bptt step=%d " % (t, bptt_step)
            dLdW += np.outer(delta_t, s[bptt_step-1])              
            dLdU[:,x[bptt_step]] += delta_t
            # Update delta for next step
            delta_t = self.W.T.dot(delta_t) * (1 - s[bptt_step-1] ** 2)
    return [dLdU, dLdV, dLdW]

RNNNumpy.bptt = bptt

# Performs one step of SGD.
def numpy_sdg_step(self, x, y, learning_rate):
    # Calculate the gradients
    dLdU, dLdV, dLdW = self.bptt(x, y)
    # Change parameters according to gradients and learning rate
    self.U -= learning_rate * dLdU
    self.V -= learning_rate * dLdV
    self.W -= learning_rate * dLdW

RNNNumpy.sgd_step = numpy_sdg_step

def train_with_sgd(model, Train_set, Train_label, learning_rate=0.005, nepoch=200, evaluate_loss_after=5):
    # We keep track of the losses so we can plot them later
    losses = []
    num_examples_seen = 0
    for epoch in range(nepoch):
        # Optionally evaluate the loss
        if (epoch % evaluate_loss_after == 0):
            loss = model.calculate_loss(Train_set, Train_label)
            losses.append((num_examples_seen, loss))
            time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            print "%s: Loss after num_examples_seen=%d epoch=%d: %f" % (time, num_examples_seen, epoch, loss)
            # Adjust the learning rate if loss increases
            if (len(losses) > 1 and losses[-1][1] > losses[-2][1]):
                learning_rate = learning_rate * 0.5  
                print "Setting learning rate to %f" % learning_rate
            sys.stdout.flush()
        # For each training example...
        for i in range(len(Train_label)):
            # One SGD step
            model.sgd_step(Train_set[i], Train_label[i], learning_rate)
            num_examples_seen += 1

def load_model():    
    
    print "FROM " + CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_*.txt"
    
    modelX.U = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_XU.txt")
    modelX.V = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_XV.txt")
    modelX.W = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_XW.txt")

    modelY.U = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_YU.txt")
    modelY.V = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_YV.txt")
    modelY.W = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_YW.txt")

    modelZ.U = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_ZU.txt")
    modelZ.V = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_ZV.txt")
    modelZ.W = np.genfromtxt(CURR_DIR + "/" + MODEL_DIR + "/" + MODEL_NAME + "_ZW.txt")

    print "[Loaded] %s ACCX... " % MODEL_NAME
    print "[Loaded] %s ACCY... " % MODEL_NAME
    print "[Loaded] %s ACCZ... " % MODEL_NAME

np.random.seed(10)
modelX = RNNNumpy(RANGE_SIZE)
modelY = RNNNumpy(RANGE_SIZE)
modelZ = RNNNumpy(RANGE_SIZE)

try:
    load_model()
    print "/%s_DIR Loading..." % MODEL_NAME
    EXIST_FLAG = True
except:
    print "model '%s' not exist." % MODEL_NAME
    EXIST_FLAG = False

#########################################################################

if COMPARE_FLAG == 0:
    if EXIST_FLAG == False:
        print "is not registered user!\n"

    if not(EXIST_FLAG):
        
        np.random.seed(10)
        modelX = RNNNumpy(RANGE_SIZE)
        modelY = RNNNumpy(RANGE_SIZE)
        modelZ = RNNNumpy(RANGE_SIZE)

        oX, sX = modelX.forward_propagation(TrainX_set[10])
        oY, sY = modelY.forward_propagation(TrainY_set[10])
        oZ, sZ = modelZ.forward_propagation(TrainZ_set[10])

        print oX.shape
        print oX

        print oY.shape
        print oY

        print oZ.shape
        print oZ

        predictionsX = modelX.predict(TrainX_set[10])
        predictionsY = modelY.predict(TrainY_set[10])
        predictionsZ = modelZ.predict(TrainZ_set[10])

        print predictionsX.shape
        print "predict X-axis"
        print predictionsX

        print predictionsY.shape
        print "predict Y-axis"
        print predictionsY

        print predictionsZ.shape
        print "predict Z-axis"
        print predictionsZ


        # calculate loss of X, Y, Z
        print "Expected Loss for random predictions: %f" % np.log(RANGE_SIZE)
        print "Actual X loss: %f" % modelX.calculate_loss(TrainX_set, TrainX_label)
        print "Actual Y loss: %f" % modelY.calculate_loss(TrainY_set, TrainY_label)
        print "Actual Z loss: %f" % modelZ.calculate_loss(TrainZ_set, TrainZ_label)


        # Outer SGD Loop
        # - model: The RNN model instance
        # - TrainX_set: The training data set
        # - TrainX_label: The training data labels
        # - learning_rate: Initial learning rate for SGD
        # - nepoch: Number of times to iterate through the complete dataset
        # - evaluate_loss_after: Evaluate the loss after this many epochs

        np.random.seed(10)
        modelX = RNNNumpy(RANGE_SIZE)
        modelY = RNNNumpy(RANGE_SIZE)
        modelZ = RNNNumpy(RANGE_SIZE)
        %timeit modelX.sgd_step(TrainX_set[10], TrainX_label[10], 0.005)
        %timeit modelY.sgd_step(TrainY_set[10], TrainY_label[10], 0.005)
        %timeit modelZ.sgd_step(TrainZ_set[10], TrainZ_label[10], 0.005)

        np.random.seed(10)
        # Train on a small subset of the data to see what happens
        modelX = RNNNumpy(RANGE_SIZE)
        modelY = RNNNumpy(RANGE_SIZE)
        modelZ = RNNNumpy(RANGE_SIZE)

        print "\nLosses for [X-axis]"
        lossesX = train_with_sgd(modelX, TrainX_set, TrainX_label, nepoch = EPOCH_NUM, evaluate_loss_after=1)
        print "\nLosses for [Y-axis]"
        lossesY = train_with_sgd(modelY, TrainY_set, TrainY_label, nepoch = EPOCH_NUM, evaluate_loss_after=1)
        print "\nLosses for [Z-axis]"
        lossesZ = train_with_sgd(modelZ, TrainZ_set, TrainZ_label, nepoch = EPOCH_NUM, evaluate_loss_after=1)

        def save_model():
            
            if not os.path.exists(MODEL_DIR):
                os.makedirs(MODEL_DIR)
                        
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_XU.txt", modelX.U)
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_XV.txt", modelX.V)
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_XW.txt", modelX.W)

            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_YU.txt", modelY.U)
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_YV.txt", modelY.V)
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_YW.txt", modelY.W)

            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_ZU.txt", modelZ.U)
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_ZV.txt", modelZ.V)
            np.savetxt(MODEL_DIR + "/" + MODEL_NAME + "_ZW.txt", modelZ.W)

            print "saved model at /%s" % MODEL_DIR

        save_model()
    else:
        print "already has %s model" % MODEL_NAME

# Compare with model and return
if COMPARE_FLAG == 1:
    
    def calculate_loss_result(x, y, z):
        # Divide the total loss by the number of training examples
        N = len(x)
        
        DX = N * (np.amax(x) - np.amin(x))
        DY = N * (np.amax(y) - np.amin(y))
        DZ = N * (np.amax(z) - np.amin(z))
        
        if IF_DEBUG:
            print "max X: %d" % np.amax(x)
            print "min X: %d" % np.amin(x)
            print "rangeX: %d" % (np.amax(x) - np.amin(x))

            print "max Y: %d" % np.amax(y)
            print "min Y: %d" % np.amin(y)
            print "rangeY: %d" % (np.amax(y) - np.amin(y))

            print "max Z: %s" % np.amax(z)
            print "min Z: %s" % np.amin(z)
            print "rangeZ: %d" % (np.amax(z) - np.amin(z))

        LX = 0
        LY = 0
        LZ = 0

        print "Spliting steps..."

        # For each stride set
        for i in range(0, N):
            
            TX_TMP = x[i][:-1]
            TY_TMP = y[i][:-1]
            TZ_TMP = z[i][:-1]
            
            next_word_probsX = modelX.forward_propagation(TX_TMP)
            next_word_probsY = modelY.forward_propagation(TY_TMP)
            next_word_probsZ = modelZ.forward_propagation(TZ_TMP)
            
            samplesX = np.random.multinomial(1, next_word_probsX[0][-1])
            samplesY = np.random.multinomial(1, next_word_probsY[0][-1])
            samplesZ = np.random.multinomial(1, next_word_probsZ[0][-1])
            
            sampled_strideX = np.argmax(samplesX)
            sampled_strideY = np.argmax(samplesY)
            sampled_strideZ = np.argmax(samplesZ)
            
            if IF_DEBUG:
                print "X_PREDICT"
                print x[i]
                print TX_TMP
                print sampled_strideX
                print "\n"

                print "Y_PREDICT"
                print y[i]
                print TY_TMP
                print sampled_strideY
                print "\n"

                print "Z_PREDICT"
                print z[i]
                print TZ_TMP
                print sampled_strideZ
                print "\n"

            LX_TMP = x[i][STEP_SIZE - 1] - sampled_strideX
            LY_TMP = y[i][STEP_SIZE - 1] - sampled_strideY
            LZ_TMP = z[i][STEP_SIZE - 1] - sampled_strideZ
            
            if LX_TMP < 0:
                LX_TMP *= -1
            if LY_TMP < 0:
                LY_TMP *= -1
            if LZ_TMP < 0:
                LZ_TMP *= -1
            
            LX += LX_TMP * LX_TMP
            LY += LY_TMP * LY_TMP
            LZ += LZ_TMP * LZ_TMP
        
            if IF_DEBUG:
                print "X-axis Loss: %s" % LX
                print "X range: %s" % DX
                print "Y-axis Loss: %s" % LY
                print "Y range: %s" % DY
                print "Z-axis Loss: %s" % LZ
                print "Z range: %s" % DZ
                print "X-axis Correctness: %f" % (100 - 100 * LX / float(DX))
                print "Y-axis Correctness: %f" % (100 - 100 * LY / float(DY))
                print "Z-axis Correctness: %f" % (100 - 100 * LZ / float(DZ))
        print "\nTotal Correctness: %f" % (100 - 100 * (LX + LY + LZ) / float(DX + DY + DZ))

    print "\nCalculating correctness..."   
    calculate_loss_result(TrainX_set, TrainY_set, TrainZ_set)