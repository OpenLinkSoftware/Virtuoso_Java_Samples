/*   This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2, or (at your option)
     any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software Foundation,
     Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. */

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;

import Bench.*;

class TextProgress implements Bench.Progress {
   int m_nMin = 0, m_nMax = 100;

   public void setProgressMinMax(int nMin, int nMax) {
      m_nMin = nMin; m_nMax = nMax;
      System.out.println("");
   }

   public void setProgressValue(int nValue) {
      if(m_nMax > m_nMin) {
         int nPercent = (nValue - m_nMin) * 100 / (m_nMax - m_nMin);
         System.out.print(nPercent + " %\r");
      }
   }

}

public class BenchMain extends JApplet {
   public String m_strURL = null,m_strUserName = null,m_strPassword = null,m_strDriver = null;
   public int m_nDriverType = -1;
   public boolean m_bInApplet = true;
   Bench.BenchPanel m_pane = null;
   Frame m_parentFrame = null;
   static BenchMain bench;

   public BenchMain() {
   }

   public void start() {
   }

   public void stop() {
   }

   public void init() {
      String strBuffer = new String();
      if(m_bInApplet) {
         strBuffer = getParameter("URL");
         if(strBuffer != null) m_strURL = strBuffer;
         strBuffer = getParameter("USER");
         if(strBuffer != null) m_strUserName = strBuffer;
         strBuffer = getParameter("PASSWORD");
         if(strBuffer != null) m_strPassword = strBuffer;
         strBuffer = getParameter("DRIVER");
         if(strBuffer != null) m_strDriver = strBuffer;
      }
      getContentPane().setLayout(new BorderLayout());
      setNewPane();
   }

   void setNewPane() {
      if(m_pane != null) {
         getContentPane().remove(m_pane);
         m_pane = null;
      }
      try {
         m_pane = new Bench.BenchPanel(!m_bInApplet,m_parentFrame);
         if(m_strURL != null) m_pane.doOpenConnection(m_strDriver,m_strURL,m_strUserName,m_strPassword,false);
         if(m_nDriverType != -1) m_pane.setDriver(m_nDriverType);
      } catch(Exception e) {
           String strMessage = e.getMessage();
           if(strMessage == null) strMessage = e.toString();
           JOptionPane.showMessageDialog(null,strMessage,"Error connecting to data source",JOptionPane.ERROR_MESSAGE);
       }
       getContentPane().add(BorderLayout.CENTER,m_pane);
       validate(); repaint();
   }

   static public void main(String argv[]) {
       System.setErr(System.out);

       try {
           UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
       } catch(Exception e) {
           try {
               UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
           } catch(Exception e1) {
               try {
                   UIManager.setLookAndFeel("javax.swing.plaf.motif.MotifLookAndFeel");
               } catch(Exception e2) {
                   try {
                       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                   } catch(Exception e3) { }
               }
           }
       }

       UIManager.put("TextField.background", new Color(255, 255, 255));
       String strCreateTables = null, strRunTests = null, strCleanUp = null, strCreateProc = null, strCreateResult = null, strDropResult = null, strLogLevel = null;
       String strTPCCRun = null, strTPCCLoad = null;
       BenchMain.bench = new BenchMain();
       bench.m_bInApplet = false;

       if(argv.length > 0)
           for(int nArg = 0;nArg < argv.length;nArg++) {
               if(argv[nArg].toUpperCase().startsWith("URL="))
                   bench.m_strURL = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("USER="))
                   bench.m_strUserName = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("PASSWORD="))
                   bench.m_strPassword = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("DRIVER="))
                   bench.m_strDriver = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("DRIVERTYPE="))
                   bench.m_nDriverType = Integer.parseInt(argv[nArg].substring(argv[nArg].indexOf("=") + 1));
               else if(argv[nArg].toUpperCase().startsWith("CREATETABLES="))
                   strCreateTables = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("RUNTEST="))
                   strRunTests = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("CLEANUP"))
                   strCleanUp = argv[nArg];
               else if(argv[nArg].toUpperCase().startsWith("CREATEPROC"))
                   strCreateProc = argv[nArg];
               else if(argv[nArg].toUpperCase().startsWith("CREATERESULT"))
                   strCreateResult = argv[nArg];
               else if(argv[nArg].toUpperCase().startsWith("DROPRESULT"))
                   strDropResult = argv[nArg];
               else if(argv[nArg].toUpperCase().startsWith("LOGLEVEL="))
                   strLogLevel = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("TPCCRUN="))
                   strTPCCRun = argv[nArg].substring(argv[nArg].indexOf("=") + 1);
               else if(argv[nArg].toUpperCase().startsWith("TPCCLOAD="))
                   strTPCCLoad = argv[nArg].substring(argv[nArg].indexOf("=") + 1);

           }

       bench.m_bInApplet = false;

       try {
           if(strCreateTables != null || strRunTests != null || strCleanUp != null || strCreateProc != null || strCreateResult != null || strDropResult != null || strTPCCRun != null || strTPCCLoad != null) {
               // batch
               TextProgress progress = new TextProgress();
               Bench.BenchPanel pane = new Bench.BenchPanel(bench.m_strDriver,bench.m_strURL,bench.m_strUserName,bench.m_strPassword,bench.m_nDriverType);
               Bench.Logger new_log = new Bench.Logger() {
                   int m_nLogLevel = 0;

                   public synchronized void log(String strMessage, int nLevel) {
                       if(nLevel <= m_nLogLevel) System.out.print(strMessage);
                   }

                   synchronized public void taskDone() {
                       notify();
                   }

                   synchronized public void waitOn() throws InterruptedException {
                       wait();
                   }

                   synchronized public void setLogLevel(int nNewLogLevel) {
                       m_nLogLevel = nNewLogLevel;
                   }

                   synchronized public int getLogLevel() {
                       return m_nLogLevel;
                   }
               };

               if(strLogLevel != null) new_log.setLogLevel(Integer.parseInt(strLogLevel));
               pane.setLogger(new_log);
               if(strCreateTables != null) {
                   StringTokenizer createTokens = new StringTokenizer(strCreateTables,",;/");
                   int nBranchCount = 10, nTellerCount = 100, nAccountCount = 1000;
                   if(createTokens.hasMoreTokens())
                       nBranchCount = Integer.parseInt(createTokens.nextToken());
                   if(createTokens.hasMoreTokens())
                       nTellerCount = Integer.parseInt(createTokens.nextToken());
                   if(createTokens.hasMoreTokens())
                       nAccountCount = Integer.parseInt(createTokens.nextToken());
               }

               if(strRunTests != null) {
                   int nNumThreads = 1, nNumRuns = 100, nTestType = 1, nWaitMinutes = -1;
                   boolean bTrans = false, bQuery = true;
                   StringTokenizer runTokens = new StringTokenizer(strRunTests,",;/");
                   if(runTokens.hasMoreTokens())
                       nNumThreads = Integer.parseInt(runTokens.nextToken());
                   if(runTokens.hasMoreTokens())
                       nNumRuns = Integer.parseInt(runTokens.nextToken());
                   if(runTokens.hasMoreTokens())
                       bTrans = runTokens.nextToken().toUpperCase().equals("TRUE");
                   if(runTokens.hasMoreTokens())
                       bQuery = runTokens.nextToken().toUpperCase().equals("TRUE");
                   if(runTokens.hasMoreTokens())
                       nTestType = Integer.parseInt(runTokens.nextToken());
                   if(runTokens.hasMoreTokens())
                       nWaitMinutes = Integer.parseInt(runTokens.nextToken());
               }
            if(strDropResult != null) pane.doDropResult();
            pane.closeConnection(true);
            System.exit(0);
         }
      } catch(Exception e) {
    	 System.out.println(e.getMessage());
      }
         // go interactive
      JFrame frame = new JFrame("JBench - Generic JDBC Benchmark Utility");
      WindowListener l = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
           BenchMain.bench.m_pane.closeConnection(true);
           System.exit(0);
        }
      };

      frame.addWindowListener(l); bench.m_parentFrame = frame;
      bench.init(); frame.getContentPane().add(bench);
      frame.setSize(675,400);
      frame.validate(); frame.setVisible(true);
   }
}
