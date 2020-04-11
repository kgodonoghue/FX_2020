
//package dukascopy.strategies.forum;
package dukascopy.strategies.indicators;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IIndicators.*;
import com.dukascopy.api.IOrder.State;
import com.dukascopy.api.*;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.util.RenkoFeedDescriptor;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//2 is to update the track to due to bug on the 18/02/2014, position not opened after multiple deletes
public class Version1_MART implements IStrategy {
    
    private IEngine engine;
    private IConsole console;
    private IContext context;
    private IHistory history;
    private IIndicators indicators;
    private IAccount account;
    private ArrayList<String> orderLabelListBuy    = new ArrayList<String>();
    private ArrayList<String> orderLabelListSell    = new ArrayList<String>();

    
    private double amountDeltaBuy = 0;   private double amountDeltaSell = 0;
    private double amountDeltaBuy1 = 0;   private double amountDeltaSell1 = 0;

    
    private int counterBuy = 1;    private int counterSell = 1001;

    
    public double takeProfitPriceBuy;    public double stopLossPriceBuy;
    public double takeProfitPriceSell;   public double stopLossPriceSell;
    private String currentLabel;
    public int trackBuy =0;   public int trackSell =0;
    public int initBuy =1;   public int initSell =1;
    public int extraBuy =1;   public int extraSell =1;
    
    
    
    
        
    public int martCountBuy =1;       public int martCountSell =1;

    
    
    
    
    @Configurable("Currency")       public Instrument currencyInstrument = Instrument.EURUSD;
    @Configurable("Start rate")     public double startRate = 100;
    @Configurable("Amount")         public double amount = 0.002;
    @Configurable("Stop Loss")      public int stopLoss = 19;
    @Configurable("Multiplier")     public double m = 1.5;
    @Configurable("MultiplierSL")   public double SLmult = 2;
    @Configurable("martSteps")      public int martSteps =13;
    @Configurable("martSteps1")     public int martSteps1 =13;


    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {    }
    
    //Method onStart initializes class fields, sets date format (0-23 hour and minutes separated by colon), 
    //and replace colon from the entered time string; 
    public void onStart(IContext context) throws JFException 
    {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.context = context;
        this.indicators = context.getIndicators();
        this.history = context.getHistory();
        
        amountDeltaBuy = amount;
        amountDeltaSell = amount;
        amountDeltaBuy1 = amount;
        amountDeltaSell1 = amount;
        
         
        
        
        // subscribe the instrument that we are going to work with
        context.setSubscribedInstruments(java.util.Collections.singleton(currencyInstrument));
    }
       

    public void onAccount(IAccount account) throws JFException 
    {
        this.account = account;
    }

    public void onMessage(IMessage message) throws JFException 
    {
    }
    
    public void onStop() throws JFException 
    {
        //print(" Stop stragegy");
        for (IOrder order : engine.getOrders()) {
            order.close();
        }
    }

    
   // Method onTick executes order monitoring if tick is come for the selected 
   //instrument and current time is between selected range 
    public void onTick(Instrument instrument, ITick tick) throws JFException 
    {
        if (instrument.equals(currencyInstrument) )
        {  
            if ((trackBuy == 0) && (trackSell == 0))
            {
              monitorOrder(instrument, tick);                                      
            } 
        
        }    
          
    }
       
    // need to be sure here
    private void monitorOrder(Instrument instrument, ITick tick) throws JFException { 
        double useOfLeverage = account.getUseOfLeverage();
        if ((useOfLeverage < 100) ) {
            recreateOrderIfClosedBuy(instrument, tick);
            recreateOrderIfClosedSell(instrument, tick);
            
        } else {
            monitorOrder(instrument, tick);
            context.stop();
        }
    }
    
    //
    private void recreateOrderIfClosedBuy(Instrument instrument, ITick tick) throws JFException 
    {
        double currentPriceBuy = tick.getAsk();                
        recreateOrderBuy(instrument, tick, currentPriceBuy);
 
    }
    
  

     private void recreateOrderIfClosedSell(Instrument instrument, ITick tick) throws JFException 
      {
        double currentPriceSell = tick.getBid();
       recreateOrderSell(instrument, tick, currentPriceSell); 
     } 
     
     

    private void recreateOrderBuy(Instrument instrument, ITick tick, double currentPriceBuy) throws JFException 
    {
            // code to start after x goes of martingale
               
            if ((initBuy==1) && (extraBuy < martSteps))
            {
            stopLossPriceBuy = currentPriceBuy - (instrument.getPipValue() * stopLoss);                
            takeProfitPriceBuy = currentPriceBuy + (instrument.getPipValue() * (stopLoss*SLmult));    
            initBuy=0;  
            }           
            
            else if ((currentPriceBuy > takeProfitPriceBuy) && (extraBuy < martSteps))
            {
                    extraBuy=1;
                    initBuy=1; 
            }           
            
            else if ((currentPriceBuy < stopLossPriceBuy) && (extraBuy < martSteps))
            {
                    extraBuy=extraBuy+1;
                    initBuy=1;  
            }            
           
            else if (extraBuy==martSteps)
            {
                 createOrdersBuy(tick, instrument, amountDeltaBuy, IEngine.OrderCommand.BUY);
                 extraBuy=extraBuy+1;
            }
            
 
            else if (currentPriceBuy > takeProfitPriceBuy)  
            {
                    trackBuy = 1;
                    amountDeltaBuy = amount; amountDeltaBuy1 = amount; 
                    deleteOrderFromListBuy(instrument, tick);
                    extraBuy=1;
                    trackBuy = 0;
            } 

            
            else if (currentPriceBuy < stopLossPriceBuy) 
            {
                    trackBuy = 1;
                    martCountBuy =martCountBuy+1;                                             
                    amountDeltaBuy1 = m*amountDeltaBuy1;                 amountDeltaBuy= amountDeltaBuy1-(amountDeltaBuy1/m);
                    createOrdersBuy(tick, instrument, amountDeltaBuy, IEngine.OrderCommand.BUY);                                      
                    trackBuy = 0;
                                      
            } 
                        
              
    }
    
 
    
    private void recreateOrderSell(Instrument instrument, ITick tick, double currentPriceSell) throws JFException 
    {
 
            if ((initSell==1) && (extraSell < martSteps))
            {
            stopLossPriceSell = currentPriceSell + (instrument.getPipValue() * stopLoss);              
            takeProfitPriceSell = currentPriceSell - (instrument.getPipValue() * (stopLoss*SLmult));   
            initSell=0;    
            }
            
            
            else if ((currentPriceSell < takeProfitPriceSell) && (extraSell < martSteps))
            {
                    
                    extraSell=1;
                    initSell=1; 
            }
            
            
            else if ((currentPriceSell > stopLossPriceSell) && (extraSell < martSteps))
            {
                    extraSell=extraSell+1;
                    initSell=1;  
            }
    
    
            else if (extraSell==martSteps)
            {
                 createOrdersSell(tick, instrument, amountDeltaSell, IEngine.OrderCommand.SELL);
                 extraSell=extraSell+1;
            }
             
            else if (currentPriceSell < takeProfitPriceSell) 
            {
                    trackSell =1;
                    amountDeltaSell = amount; amountDeltaSell1 = amount; 
                    deleteOrderFromListSell(instrument, tick);
                    extraSell=1;
                    trackSell =0;
            }
            
            else if (currentPriceSell > stopLossPriceSell) 
            {
                    trackSell =1;
                    martCountSell =martCountSell+1;                             
                    amountDeltaSell1 = m*amountDeltaSell1;     amountDeltaSell= amountDeltaSell1-(amountDeltaSell1/m);
                    createOrdersSell(tick, instrument, amountDeltaSell, IEngine.OrderCommand.SELL);
                    trackSell =0;    
            } 

         
    }
    
  
  
  
  
    //create first order, comes from the 
    private void createFirstOrdersBuy(ITick tick, Instrument instrument, double orderAmount) throws JFException 
    {
       createOrdersBuy(tick, instrument, orderAmount, IEngine.OrderCommand.BUY);
    }
    

    
    
    
    private void createFirstOrdersSell(ITick tick, Instrument instrument, double orderAmount) throws JFException 
    {
        createOrdersSell(tick, instrument, orderAmount, IEngine.OrderCommand.SELL);
    }
    


    private void deleteOrderFromListBuy(Instrument instrument, ITick tick) throws JFException 
    {
        trackBuy =1;
        for(int i=1; i<martCountBuy+1; i++)
        {
         IOrder OrderBuy = engine.getOrder(orderLabelListBuy.get(orderLabelListBuy.size()-i));
         String orderLabelBuy=OrderBuy.getLabel();
         engine.getOrder(orderLabelBuy).close();
        }
        martCountBuy =1;
        trackBuy =0;
    }
    

    
     private void deleteOrderFromListSell(Instrument instrument, ITick tick) throws JFException 
     {
        trackSell =1;
        for(int i=1; i<martCountSell+1; i++)
        {
        IOrder OrderSell = engine.getOrder(orderLabelListSell.get(orderLabelListSell.size()-i));
        String orderLabelSell=OrderSell.getLabel();
        engine.getOrder(orderLabelSell).close();
        }
        martCountSell =1;
        trackSell =0;
    }
    

    
    
    private void createOrdersBuy(ITick tick, Instrument instrument, double orderAmountBuy, OrderCommand orderCommand) throws JFException 
    {
        trackBuy = 1;
        String labelBuy = getLabelBuy(instrument);
        //engine.submitOrder(labelBuy, instrument, orderCommand, orderAmountBuy, tick.getBid());
        IOrder OrderBuy = engine.submitOrder(labelBuy, instrument, orderCommand, orderAmountBuy, tick.getBid(),20);
        OrderBuy.waitForUpdate(20000, IOrder.State.FILLED); //wait max 2 sec for FILLED
        orderLabelListBuy.add(labelBuy);
        stopLossPriceBuy = OrderBuy.getOpenPrice() - (instrument.getPipValue() * stopLoss);                
        takeProfitPriceBuy = OrderBuy.getOpenPrice() + (instrument.getPipValue() * (stopLoss*SLmult)); 
        trackBuy = 0;
       
    }
    

    
    
        private void createOrdersSell(ITick tick, Instrument instrument, double orderAmountSell, OrderCommand orderCommand) throws JFException 
        {
        trackSell = 1;
        String labelSell = getLabelSell(instrument);
        //engine.submitOrder(labelSell, instrument, orderCommand, orderAmountSell, tick.getBid());
        IOrder OrderSell =engine.submitOrder(labelSell, instrument, orderCommand, orderAmountSell, tick.getBid(),20);
        OrderSell.waitForUpdate(20000, IOrder.State.FILLED); //wait max 2 sec for FILLED
        orderLabelListSell.add(labelSell);
        stopLossPriceSell = OrderSell.getOpenPrice() + (instrument.getPipValue() * stopLoss);              
        takeProfitPriceSell = OrderSell.getOpenPrice() - (instrument.getPipValue() * (stopLoss*SLmult));  
        trackSell = 0;
        }
        
        


        protected String getLabelBuy(Instrument instrument) 
        {
        String labelBuy = instrument.name();
        labelBuy = labelBuy + (counterBuy ++);
        labelBuy = labelBuy.toUpperCase();
        return labelBuy;
        }
        
 
    
       protected String getLabelSell(Instrument instrument) 
       {
        String labelSell = instrument.name();
        labelSell = labelSell + (counterSell ++);
        labelSell = labelSell.toUpperCase();
        return labelSell;
       }
       

        
    private void print(String message) {
        console.getOut().println(message);
         }
}
