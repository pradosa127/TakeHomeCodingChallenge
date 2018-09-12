import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
  /**
   * Iterate through each line of input.
   */
   
  public static void main(String[] args) throws IOException {
    InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    BufferedReader in = new BufferedReader(reader);
    String line;
    while ((line = in.readLine()) != null) {
      String[] pfAndBm=line.split(":");
      String[] pfs=pfAndBm[0].split("\\|");
      String[] bms=pfAndBm[1].split("\\|");
      
      /* Create list of Assets of both portfolio and benchMarks */
      ArrayList<Asset> portfolio=new ArrayList<>();
      ArrayList<Asset> benchMarks=new ArrayList<>();
      
      
      
      for(int i=0;i<pfs.length;i++)
      {
        String[] pfsData=pfs[i].split(",");
        portfolio.add(new Asset(pfsData[0],pfsData[1],Integer.parseInt(pfsData[2]),Double.parseDouble(pfsData[3]),Double.parseDouble(pfsData[4])));
        
        String[] bmsData=bms[i].split(",");
        benchMarks.add(new Asset(bmsData[0],bmsData[1],Integer.parseInt(bmsData[2]),Double.parseDouble(bmsData[3]),Double.parseDouble(bmsData[4])));
      }
      
       /* Caluclate number of trades required */
      List<Trade> transactions=transactions(portfolio,benchMarks);
      
      /* Sort the trades in the transaction list according to the ascending order of Asset name */
      Collections.sort(transactions, new Comparator<Trade>(){
        @Override
         public int compare(Trade a, Trade b) {
         return a.getName().compareToIgnoreCase(b.getName());
          }
      });
      
      /* print the final output to standard output */
      for(Trade t: transactions)
        System.out.println(t.getType()+","+t.getName()+","+t.getNumShares());
    }
  }
  
  
   /**
    * This method calculates the total market value of all the assets in a given list
    *
    * @param  assets  List of Assets
    * @return totalMarketValue
    */
  public static double calculateTotalMarketValue(ArrayList<Asset> assets)
  {
    double totalMarketValue=0;
    for(int i=0;i<assets.size();i++)
    {
      Asset curAsset=assets.get(i);
      int shares=curAsset.getShares();
      double price=curAsset.getPrice();
      double accruedInt=curAsset.getaccInterest();

      if(curAsset.getType().equals("STOCK"))
      {
        totalMarketValue+=marketvalStock(shares,price);
      }
      else
      {
        totalMarketValue+=marketvalBond(shares,price,accruedInt);
      }
    }
    
    return totalMarketValue;
      
  }
  
  
   /**
    * This method calculates market value for "STOCKS"
    *
    * @param  shares number od shares
    * @param  price  price of each share
    * @return shares*price
    */
  public static double marketvalStock(int shares, double price)
  {
    return shares*price;
  }
  
  
  
    /**
    * This method calculates market value for "BOND"
    *
    * @param  shares number od shares
    * @param  price  price of each share
    * @param  accuredInterest for assets
    * @return marketvalBond
    */
  public static double marketvalBond(int shares, double price, double accuredInterest)
  {
    return shares*(price+accuredInterest)*0.01;
  }
  
    /**
    * This method calculates market value percenatge
    *
    * @param  marketVal market value of the Asset
    * @param  totalMarketVal total market value
    * @return marketVal/totalMarketVal
    */
  public static double marketValPercentage(double marketVal,double totalMarketVal)
  {
    return (double)marketVal/totalMarketVal;
  }
  
  
  
  
    /**
    * This method calculates transactions required to matche portfolio  to the benchmark
    *
    * @param  portfolio  portfolio of assets
    * @param  benchMarks benchmarks of assets
    * @return  List<Trade>  numTransaction 
    */
  public static List<Trade> transactions( ArrayList<Asset> portfolio,ArrayList<Asset> benchMarks)
  {
    
      List<Trade>  numTransaction=new ArrayList<>();
    
      double totalMVPortfolio=calculateTotalMarketValue(portfolio);
      double totalMVBenchmarks=calculateTotalMarketValue(benchMarks);
      Collections.sort(portfolio, new LexicographicComparator());
      Collections.sort(benchMarks, new LexicographicComparator());
      for(int i=0; i<portfolio.size();i++)
      {
           Asset pAsset=portfolio.get(i);
           Asset bAsset=benchMarks.get(i);
           
           
          if(pAsset.getType().equals("STOCK"))
          {
              /* Calculates trades for STOCKS */
             double MVPPortfolio=marketValPercentage(marketvalStock(pAsset.getShares(), pAsset.getPrice()),totalMVPortfolio);
             double MVPBenchMark=marketValPercentage(marketvalStock(bAsset.getShares(), bAsset.getPrice()),totalMVBenchmarks);
             
               if(MVPPortfolio<MVPBenchMark)
               {
                 int y=(int)Math.round((MVPBenchMark*totalMVPortfolio)/pAsset.getPrice()-pAsset.getShares());
                 numTransaction.add(new Trade("BUY",pAsset.getName(),y));
               }
               else if ( MVPPortfolio>MVPBenchMark)
               {
                  int y=(int)Math.round((MVPPortfolio*totalMVBenchmarks)/bAsset.getPrice()-bAsset.getShares());
                  numTransaction.add(new Trade("SELL",pAsset.getName(),y));
               }
           
            
          }
          else
          {
             /* Calculates trades for BONDS */
             double MVPPortfolio=marketValPercentage(marketvalBond(pAsset.getShares(), pAsset.getPrice(),pAsset.getaccInterest()),totalMVPortfolio);
             double MVPBenchMark=marketValPercentage(marketvalBond(bAsset.getShares(), bAsset.getPrice(),bAsset.getaccInterest()),totalMVBenchmarks);
               
               if(MVPPortfolio<MVPBenchMark)
               {
                 double numerator=(MVPBenchMark*totalMVPortfolio);
                 double denomenator=(pAsset.getPrice()+pAsset.getaccInterest())*0.01;
                 int y=(int)Math.round(numerator/denomenator-pAsset.getShares());
                 numTransaction.add(new Trade("BUY",pAsset.getName(),y));
               }
               else if( MVPPortfolio > MVPBenchMark)
               { 
                 double numerator=(MVPPortfolio*totalMVBenchmarks);
                 double denomenator=(bAsset.getPrice()+bAsset.getaccInterest())*0.01;
                 int y=(int)Math.round(numerator/denomenator-bAsset.getShares());
                 numTransaction.add(new Trade("SELL",pAsset.getName(),y));
               }
          }
      }
      
      return numTransaction;
    
  }
  
  
  
}

   /**
    * Overrides comparator to sort the assets in the ascending order of their names
    */

class LexicographicComparator implements Comparator<Asset> {
    @Override
    public int compare(Asset a, Asset b) {
        return a.getName().compareToIgnoreCase(b.getName());
    }
}


   /**
    * Class  represents the datastructure for trades
    */
class Trade{
  
  private String type;
  private String tName;
  private int numShares;
  
  public Trade(String type, String name,int numShares)
  {
    this.type=type;
    this.tName=name;
    this.numShares=numShares;
  }
  
  public String getType()
  {
    return this.type;
  }
  public String getName()
  {
    return this.tName;
  }
  
  public int getNumShares()
  {
    return this.numShares;
  }
  
}

   /**
    * Class  represents the datastructure for Assets
    */

  class Asset{
     private String name;
     private String assetType;
     private  int shares;
     private double price;
     private double accruedInterest; 
    
    public Asset(String name, String assetType,int share,double price,double accInt)
    {
      this.name=name;
      this.assetType=assetType;
      this.shares=share;
      this.price=price;
      this.accruedInterest=accInt;
    }
    public String getName()
    {
      return this.name;
    }
    public String getType()
    {
      return this.assetType;
    }
    public int getShares()
    {
      return shares;
    }
    public double getPrice()
    {
      return price;
    }
    public double getaccInterest()
    {
      return this.accruedInterest;
    }
  }
