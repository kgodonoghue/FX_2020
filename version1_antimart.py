# -*- coding: utf-8 -*-
"""
Created on Wed Oct  2 18:26:16 2019

@author: kgodo

# counter added and time after the limit hit

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

buy_counter=[]
def martFuncBuy(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit,tick_count_limit,sample):
    buy_counter=[]
    SL=SL/factor
    TP=TP/factor
    cost=cost/factor
    profitArray=np.zeros(len(data))
    bBuy=1
    mBuy=-1  
    martCountBuy=1 
    data=np.array(data)
    data=data[1::sample]

    
    timer_bit=0
    tick_count=0


    for a in range(startPoint,len(data)):     
          if timer_bit==1:
              tick_count=tick_count+1
              if tick_count>tick_count_limit:
                  tick_count=0
                  timer_bit=0
              continue
          
            
          if (((data[a]-data[bBuy])>TP and martCountBuy>martSteps)):
            buy_counter.append(martCountBuy)
            martCountBuy=1 
            profitArray[a]=mBuy*(data[a]-data[a-1])+mBuy*cost
            mBuy=-1
            bBuy=a 
            
          elif ((martCountBuy==martLimit)):
            buy_counter.append(martCountBuy)
            martCountBuy=1 
            profitArray[a]=mBuy*(data[a]-data[a-1])+mBuy*cost
            mBuy=-1
            bBuy=a 
            timer_bit=1 
                              
          elif ((data[a]-data[bBuy])>TP):
            martCountBuy=1            
            mBuy=-1
            bBuy=a           
         
          elif (data[a]-data[bBuy]<-SL and martCountBuy>martSteps):            
            martCountBuy=martCountBuy+1;
            profitArray[a]=mBuy*(data[a]-data[a-1])+mBuy*cost;            
            bBuy=a
            mBuy=mBuy*multiplier
           
          elif ((data[a]-data[bBuy])<-SL):            
            martCountBuy=martCountBuy+1           
            bBuy=a
            mBuy=-1
         
          elif martCountBuy>martSteps:
            profitArray[a]=mBuy*(data[a]-data[a-1])               
 
    profitArray=pipBet*profitArray
    return profitArray, buy_counter

def martFuncSell(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit,tick_count_limit,sample ):
    sell_counter=[]
    SL=SL/factor
    TP=TP/factor
    cost=cost/factor
    profitArray=np.zeros(len(data))
    bSell=1
    mSell=1  
    martCountSell=1
    data=np.array(data)
    data=data[1::sample]

    timer_bit=0
    tick_count=0


    for a in range(startPoint,len(data)):     
          
          if timer_bit==1:
              tick_count=tick_count+1
              if tick_count>tick_count_limit:
                  tick_count=0
                  timer_bit=0
              continue       
            
          if (((data[a]-data[bSell])<-TP and martCountSell>martSteps)):
            sell_counter.append(martCountSell)
            martCountSell=1 
            profitArray[a]=mSell*(data[a]-data[a-1])-mSell*cost
            mSell=1
            bSell=a 
              
            
          elif ((martCountSell==martLimit)):
            sell_counter.append(martCountSell)
            martCountSell=1 
            profitArray[a]=mSell*(data[a]-data[a-1])-mSell*cost
            mSell=1
            bSell=a 
            timer_bit=1  
                              
          elif ((data[a]-data[bSell])<-TP):
            martCountSell=1            
            mSell=1
            bSell=a           
         
          elif (data[a]-data[bSell]>SL and martCountSell>martSteps):            
            martCountSell=martCountSell+1;
            profitArray[a]=mSell*(data[a]-data[a-1])-mSell*cost;            
            bSell=a
            mSell=mSell*multiplier
           
          elif ((data[a]-data[bSell])>SL):            
            martCountSell=martCountSell+1           
            bSell=a
            mSell=1
         
          elif martCountSell>martSteps:
            profitArray[a]=mSell*(data[a]-data[a-1])   
   
    profitArray=pipBet*profitArray
    return profitArray, sell_counter

#currency='USDMXN'
currency='usdjpy'
#currency='XAUUSD'
#currency='BITCOIN'
year_start=2015   
year_stop=2021
path_data =r'E:/trading/data/' + currency + '/'
SL=20
TP=20 
commision=0.00005
multiplier=2
martSteps=0
capital=1000000
pipBet=2000
startPoint=1
tick_count_limit=0
sample=1
year_result=[]


if __name__ == '__main__':    
    for martLimit in range(11,12):
        for year in range(year_start,year_stop):
            print(year)
            data = pd.read_csv(path_data + str(currency) + str(year) + '.csv')
            cost=abs(data['Ask']-data['Bid']).mean()+commision
            data = data['Ask']
            factor=data[1]/100
            if factor<0.2:
                factor=10000
            elif factor<2: 
                factor=100
            else:
                factor=1  
            #sentiment = data['AskVolume']- data['BidVolume']
            #sentiment=sentiment.rolling(window=1000).mean()
            [profitArrayBuy,buy_counter]=martFuncBuy(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit,tick_count_limit,sample)
            [profitArraySell,sell_counter]=martFuncSell(data,cost,SL,TP,multiplier,martSteps,factor,capital,pipBet,startPoint,martLimit,tick_count_limit,sample)
            profitArrayBuy=np.cumsum(profitArrayBuy)
            profitArraySell=np.cumsum(profitArraySell)
            year_result.append([martLimit,tick_count_limit,year,profitArrayBuy[len(profitArrayBuy)-1],profitArraySell[len(profitArraySell)-1],profitArrayBuy[len(profitArrayBuy)-1]+profitArraySell[len(profitArraySell)-1]])
            plt.plot((profitArrayBuy+profitArraySell)+capital)
