# -*- coding: utf-8 -*-
"""
Created on Wed Oct  2 18:26:16 2019

@author: kgodo
"""
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.metrics import classification_report
from pandas import DataFrame, read_csv
import time
import matplotlib.pyplot as plt
from math import *
from random import randint
from sklearn.preprocessing import MinMaxScaler
import datetime
import time
from pandas_ml import ConfusionMatrix
from sklearn.tree import DecisionTreeRegressor
from sklearn import preprocessing
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import confusion_matrix
import numpy as np

def martFuncBuy(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit):

    SL=SL/factor
    TP=TP/factor
    cost=cost/factor
    profitArray=np.zeros(len(data))
    bBuy=1
    mBuy=1  
    martCountBuy=1 
    data=np.array(data)


    for a in range(startPoint,len(data)):     
          
          if (((data[a]-data[bBuy])>TP and martCountBuy>martSteps) or (martCountBuy==martLimit)):
            martCountBuy=1 
            profitArray[a]=mBuy*(data[a]-data[a-1])-mBuy*cost
            mBuy=1
            bBuy=a 
            print(a) 
                              
          elif ((data[a]-data[bBuy])>TP):
            martCountBuy=1            
            mBuy=1
            bBuy=a           
         
          elif (data[a]-data[bBuy]<-SL and martCountBuy>martSteps):            
            martCountBuy=martCountBuy+1;
            profitArray[a]=mBuy*(data[a]-data[a-1])-mBuy*cost;            
            bBuy=a
            mBuy=mBuy*multiplier
           
          elif ((data[a]-data[bBuy])<-SL):            
            martCountBuy=martCountBuy+1           
            bBuy=a
            mBuy=1
         
          elif martCountBuy>martSteps:
            profitArray[a]=mBuy*(data[a]-data[a-1])               
 
    profitArray=pipBet*profitArray
    return profitArray

def martFuncSell(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit):

    SL=SL/factor
    TP=TP/factor
    cost=cost/factor
    profitArray=np.zeros(len(data))
    bSell=1
    mSell=-1  
    martCountSell=1
    data=np.array(data)


    for a in range(startPoint,len(data)):           
            
          if (((data[a]-data[bSell])<-TP and martCountSell>martSteps) or (martCountSell==martLimit)):
            martCountSell=1 
            profitArray[a]=mSell*(data[a]-data[a-1])+mSell*cost
            mSell=-1
            bSell=a 
            print(a) 
                              
          elif ((data[a]-data[bSell])<-TP):
            martCountSell=1            
            mSell=-1
            bSell=a           
         
          elif (data[a]-data[bSell]>SL and martCountSell>martSteps):            
            martCountSell=martCountSell+1;
            profitArray[a]=mSell*(data[a]-data[a-1])+mSell*cost;            
            bSell=a
            mSell=mSell*multiplier
           
          elif ((data[a]-data[bSell])>SL):            
            martCountSell=martCountSell+1           
            bSell=a
            mSell=-1
         
          elif martCountSell>martSteps:
            profitArray[a]=mSell*(data[a]-data[a-1])   
   
    profitArray=pipBet*profitArray
    return profitArray

#currency='USDMXN'
currency='EURUSD'
#currency='XAUUSD'
#currency='BITCOIN'
year_start=2019
year_stop=2020
path_data =r'C:/main_folder/FX_trading/Data/' + currency + '/'
SL=20
TP=20
multiplier=2
martSteps=2
factor=1000
capital=10000
pipBet=2000
startPoint=1
martLimit=6
factor=100


if __name__ == '__main__':    
    for year in range(year_start,year_stop):
        print(year)
        data = pd.read_csv(path_data + str(currency) + str(year) + '.csv')
        cost=abs(data['Ask']-data['Bid']).mean()
        data = data['Ask']
        factor=data[1]/100
        if factor<0.1:
            factor=10000
        elif factor<0.5:
            factor=100
        else:
            factor=1
        #sentiment = data['AskVolume']- data['BidVolume']
        #sentiment=sentiment.rolling(window=1000).mean()
        profitArrayBuy=martFuncBuy(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit)
        profitArraySell=martFuncSell(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit)
        plt.plot(np.cumsum(profitArrayBuy+profitArraySell)+capital)
