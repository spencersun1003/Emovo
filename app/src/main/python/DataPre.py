#!/usr/bin/env python
# -*- encoding: utf-8 -*-
'''
@File    :   DataPre.py
@Last Modified    :   2021/11/25 18:12:26
@Author  :   Yuang Tong 
@Contact :   yuangtong1999@gmail.com
'''

# here put the import lib

import librosa
import numpy as np
from os.path import dirname,join






def getSingleAudioEmbd(data,hop_length=512,sr=22050): # generate single audio embedding
    #y = librosa.load(wav_dir)
    n_fft = 2048
    f0 = librosa.feature.zero_crossing_rate(data, hop_length=hop_length).T  # (seq_len, 1)
    mfcc = librosa.feature.mfcc(y=data, sr=sr, hop_length=hop_length, htk=True, n_fft=n_fft).T  # (seq_len, 20)
    mfcc_delta = librosa.feature.delta(mfcc)  # (seq_len,20)
    cqt = librosa.feature.chroma_cqt(y=data, sr=sr, hop_length=hop_length).T  # (seq_len, 12)
    tonnetz = librosa.feature.tonnetz(y=data, sr=sr).T  # (seq_len,6)

    # spectral features
    centroid = librosa.feature.spectral_centroid(y=data, sr=sr, n_fft=n_fft, hop_length=hop_length).T  # (seq_len, 1)
    bandwidth = librosa.feature.spectral_bandwidth(y=data, sr=sr, n_fft=n_fft, hop_length=hop_length).T  # (seq_len, 1)
    contrast = librosa.feature.spectral_contrast(y=data, sr=sr, n_fft=n_fft, hop_length=hop_length).T  # (seq_len, 7)
    flatness = librosa.feature.spectral_flatness(y=data, n_fft=n_fft, hop_length=hop_length).T  # (seq_len, 1)
    rolloff = librosa.feature.spectral.spectral_rolloff(y=data, sr=sr, n_fft=n_fft,
                                                        hop_length=hop_length).T  # (seq_len, 1)
    spectral_features = np.concatenate([centroid, bandwidth, contrast, flatness, rolloff], axis=-1)  # (seq_len, 11)

    # rhythm features
    tpg = librosa.feature.tempogram(y=data, sr=sr, hop_length=hop_length).T  # (seq_len,384)

    # melspectrogram
    mels = librosa.feature.melspectrogram(y=data, sr=sr, n_fft=n_fft, hop_length=hop_length).T  # (seq_len,128)

    return np.concatenate([f0, mfcc, cqt, tonnetz, spectral_features, tpg, mels], axis=-1)  # (seq_len,562)
    #return np.concatenate([f0, mfcc, cqt], axis=-1)

def padding(feature,MAX_LEN=588,padding_mode='zeros',padding_loc='back'):
    # input: (seqlen,feature_dim)
    # output: (MAXLEN,feature_dim)


    length = feature.shape[0]

    if length > MAX_LEN:
        # cut
        start = int((length-MAX_LEN)/2)
        end = start+MAX_LEN
        return feature[start:end,:]

    elif length == MAX_LEN:
        return feature

    pad_len = MAX_LEN-length

    if padding_mode == "zeros":
        pad = np.zeros([pad_len,feature.shape[-1]])
    else:
        mean,std = feature.mean(),feature.std()
        pad = np.random.normal(mean,std,(pad_len,feature.shape[-1]))

    feature = np.concatenate([pad, feature], axis=0) if(padding_loc == "front") else \
        np.concatenate((feature, pad), axis=0)

    return feature

def Preprocess(data,hop_length=512,sr=22050,padding_len=588,padding_mode='zeros',padding_loc='back'):
    print(dirname(__file__))
    data=np.array(data,dtype=np.float32)
    data=Normalize(data)
    #data=librosa.resample(data,sr,sr)
    feature=getSingleAudioEmbd(data,hop_length)
    #feature=padding(feature,padding_len,padding_mode,padding_loc)
    feature=np.mean(feature,axis=0)
    feature=feature.reshape(1,-1)
    feature=feature[0].tolist()
    return feature

def Preprocess2(file,hop_length=512,sr=22050,padding_len=588,padding_mode='zeros',padding_loc='back'):
    #data,sr=librosa.load(join(dirname(__file__),"record.wav"),sr=sr)

    data,sr=librosa.load("/storage/emulated/0/Download/myEmovo/record/record2.wav",sr=sr)
    print(type(data))
    #data=librosa.resample(data,sr,sr)
    feature=getSingleAudioEmbd(data,hop_length)
    #feature=padding(feature,padding_len,padding_mode,padding_loc)
    feature=np.mean(feature,axis=0)
    feature=feature.reshape(1,-1)
    feature=feature[0].tolist()
    return feature

def Normalize(data):
    maxdelta=max(data)-min(data)
    data=(data-sum(data)/len(data))/maxdelta*2
    return data



if __name__ == "__main__":

    print("none")


