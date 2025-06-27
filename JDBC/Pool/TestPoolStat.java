import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.text.*;
import virtuoso.jdbc4.*;

public class TestPoolStat  extends Thread{
  static VirtuosoConnectionPoolDataSource ds;

  static String query = "select * from Demo.demo.Countries";

  public TestPoolStat() {
  }


  public static void main(String argv[])
  {
    int c;
    try {

      ds = new VirtuosoConnectionPoolDataSource();

      ds.setDataSourceName("MyPool");
      ds.setServerName("192.168.10.130:1111");
      ds.setUser("dba");
      ds.setPassword("dba");
      ds.setMinPoolSize(4);
      ds.setMaxPoolSize(10);
      ds.setMaxIdleTime(5);
      ds.fill();

      int MAX_THREADS = 200;

      System.out.println("--Statring--");


      long s = System.currentTimeMillis();
      for(int i = 0; i < 20; i++) {

         TestPoolStat[] thr = new TestPoolStat[MAX_THREADS];
         for(int j=0; j < thr.length; j++)
           thr[j] = new TestPoolStat();

         for(int j=0; j < thr.length; j++)
           thr[j].start();

         for(int j=0; j < thr.length; j++)
           thr[j].join();
         System.out.println("["+i+"]");
      }
      
      System.out.println("Time = "+((double)(System.currentTimeMillis() - s))/1000);
      System.out.println("-- Pool Statistics --");


      System.out.println(ds.get_statistics());

      System.out.println("-- Close Datasource --");
      
      try {
        ds.close( );
      } catch( SQLException sql ) {
        System.out.println( "main - Exception during close" );
        sql.printStackTrace( );
        return;
      }

      System.out.println("-- End --");

    } catch (Exception e) {
      System.out.print(e);
      e.printStackTrace();
    }
  }



  public void run( ) 
  {
    Connection c = null;
    String name = this.getName( );
    Statement s = null;
    ResultSet rs = null;
    int row = 0;

    try {
      PooledConnection pc = ds.getPooledConnection();
      c = pc.getConnection();
    }
    catch( SQLException sql ) {
      System.out.println( name + " - Exception during getConnection" );
      sql.printStackTrace( );
      return;
    }

    try {
      s = c.createStatement( );
      rs = s.executeQuery( query );
      while( rs.next() ) {
        rs.getString(1);
        row++;
      }
    }
    catch( SQLException sql ) {
      System.out.println( name + " - Exception during getConnection" );
      sql.printStackTrace( );
      return;
    }
    finally {
      try {
        s.close( );
      }
      catch( SQLException dontcare ) { }
    }

    try {
      if( c != null ) {
        c.close( );
      }
    }
    catch( SQLException sql ) {
      System.out.println( name + "Exception during free" );
      sql.printStackTrace( );
    }
  } // run( )



}






