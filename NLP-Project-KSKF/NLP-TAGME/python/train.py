import pickle
import numpy as np
from sklearn import svm
from sklearn.externals import joblib


class Normalization(object):

    def __init__(self, mean, std):
        super(Normalization, self).__init__()
        self.mean = mean
        self.std = std


def main():
    train = []
    trainLabels = []
    with open('training.txt', 'r', newline='') as train_file:
        next(train_file)
        for line in train_file:
            line = line.strip()
            features = np.fromstring(line, sep='\t')
            label = features[-1]
            trainLabels.append(label)
            train.append(features[:-1])

    # normalize training features
    mean = np.mean(train, axis=0)
    std = np.std(train, axis=0)
    normalization = Normalization(mean, std)
    with open('trained_model/normalization.pickle', 'wb') as handle:
        pickle.dump(normalization, handle)
    train = (train - normalization.mean) / normalization.std

    # SVM
    clf = svm.SVC(tol=1e-6)
    clf.fit(train, trainLabels)

    # save SVM model
    joblib.dump(clf, 'trained_model/svm.pkl')


if __name__ == '__main__':
    main()
