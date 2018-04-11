import sys
import pickle
import numpy as np
from sklearn import svm, grid_search
from sklearn.feature_selection import SelectFromModel
from sklearn.ensemble import RandomForestRegressor
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.externals import joblib

PATH = "wat"
TESTFILE = "test.csv"
POSITIVE_WEIGHT = 6  # per-sample weight for a positive sample
NEGATIVE_WEIGHT = 3  # per-sample weight for a negative sample
THRESHOLD = 0.14  # only used for python evaluation
# SVM
SVM_C = 1  # penalty parameter of the error term
SVM_EPS = 0.1  # epsilon-tube within which no penalty is associated in the training loss function
# Random Forest
RF_TREES = 50  # number of trees


class Normalization(object):

    def __init__(self, mean, std):
        super(Normalization, self).__init__()
        self.mean = mean
        self.std = std


def train():
    impure_data_labels = np.genfromtxt("data/" + PATH + "/training_complete.csv", skip_header=1, delimiter=",", missing_values={"null", "NaN"})
    print("svm training for " + PATH + "/training_complete.csv")
    features = [x for x in range(29)]
    features.remove(0)
    features.remove(1)
    features.append(38)
    features.remove(10)  # remove page Rank feature
    data_labels = impure_data_labels[:, features]

    data = data_labels[:, :-1]
    labels = data_labels[:, -1]
    labels = labels.astype(int)

    # sample weights for SVM
    sample_weights = np.empty(len(labels))
    for idx, n in enumerate(labels):
        if n == 1:
            sample_weights[idx] = POSITIVE_WEIGHT
        else:
            sample_weights[idx] = NEGATIVE_WEIGHT

    mean = np.nanmean(data, axis=0)  # compute the mean value of every column
    std = np.nanstd(data, axis=0)
    normalization = Normalization(mean, std)
    with open('trained_model/normalization.pickle', 'wb') as handle:
        pickle.dump(normalization, handle)

    data = fill_values_and_zscore(data, mean, std)
    # test
    # data = data[:, [10, 16, 17, 18, 23, 24]]

    # Regressor
    # clf = svm.SVR(C=SVM_C, epsilon=SVM_EPS)
    # clf = RandomForestRegressor(n_estimators=RF_TREES)
    clf = GradientBoostingRegressor(n_estimators=100)
    clf.fit(data, labels, sample_weights)
    # save SVM model
    joblib.dump(clf, 'trained_model/svm.pkl')


def test():
    # normalization = np.genfromtxt('trained_model/normalization.txt', delimiter=",",comments="#")
    with open('trained_model/normalization.pickle', 'rb') as handle:
        normalization = pickle.load(handle)
    mean = normalization.mean
    std = normalization.std

    # read test data
    impure_data = np.genfromtxt("data/" + PATH + "/" + TESTFILE, skip_header=1, delimiter=",", missing_values={"null", "NaN"})
    print("svm classification for " + PATH + "/" + TESTFILE)
    features = [x + 4 for x in range(29)]
    features.remove(4)
    features.remove(5)
    features.remove(14) #
    data = impure_data[:, features]

    data = fill_values_and_zscore(data, mean, std)
    # test
    # data = data[:, [10, 16, 17, 18, 23, 24]]

    # SVM prediction
    clf = joblib.load('trained_model/svm.pkl')
    prediction_label = clf.predict(data)   #this is the classification label

    # output prediction to file
    prediction_label_col = prediction_label.reshape((-1, 1))    #convert it from row to column

    prediction_threshold = THRESHOLD
    prediction_score_col = np.empty([len(prediction_label_col), 1])
    for idx, s in enumerate(prediction_label_col):
        if s > prediction_threshold:
            prediction_score_col[idx] = 1
        else:
            prediction_score_col[idx] = 0

    real_score_col = impure_data[:, -1].reshape((-1, 1))
    output = np.hstack((impure_data[:, 0:4], prediction_label_col, prediction_score_col, real_score_col))
    ind = np.lexsort((output[:, -3], output[:, 0]))
    sorted_output = [output[i, :] for i in ind]

    np.savetxt('data/prediction.csv', sorted_output, delimiter=',')
    # return result


# fill missing values
def fill_values_and_zscore(data, mean, std):
    mean_mat = np.tile(mean, (data.shape[0], 1))
    mean_mat[~np.isnan(data)] = 0
    data[np.isnan(data)] = 0
    data = data + mean_mat
    # zscore
    assert(np.all(std))  # assert std values!=0  otherwise division by zero
    data = (data - mean) / std
    return data


if __name__ == '__main__':
    if len(sys.argv) != 2 or (sys.argv[1] != 'train' and sys.argv[1] != 'test'):
        print("Usage: python model.py train            for training the model\n       python model.py test             for predicting on test data\n")
    if sys.argv[1] == 'train':
        train()
    elif sys.argv[1] == 'test':
        test()
