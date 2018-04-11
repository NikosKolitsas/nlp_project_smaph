import math
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
    unknowns = []
    with open('unknowns.txt', 'r', newline='') as unknown_file:
        next(unknown_file)
        for line in unknown_file:
            line = line.strip()
            features = np.fromstring(line, sep='\t')
            unknowns.append(features)

    # normalize unknowns
    with open('trained_model/normalization.pickle', 'rb') as handle:
        normalization = pickle.load(handle)
    unknowns = (unknowns - normalization.mean) / normalization.std

    # load SVM
    clf = joblib.load('trained_model/svm.pkl')

    clf.predict(unknowns)


if __name__ == '__main__':
    main()
