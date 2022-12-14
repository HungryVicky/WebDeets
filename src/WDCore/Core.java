package WDCore;
/**
 *
 * @author Vicky
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;

import WDFrame.ResultFrame;

public class Core 
{
    public
    ResultFrame result_Screen = new ResultFrame();
    String cuurentCommand , result;
    public String Path="null";
    String target;
    Process process;
    BufferedReader buff;
    Scanner scanner = new Scanner(System.in);
    ArrayList<String> Results = new ArrayList<>();    
   
    public void process()  
    {       
        ResultFrame.setLoading();
        try 
        {
            process = Runtime.getRuntime().exec(cuurentCommand);
        }
        catch (IOException e) {e.printStackTrace();}

        buff = new BufferedReader(new InputStreamReader(process.getInputStream()));
        PrintResult();
    }
    
    public void FileChooser(){
        
        JFileChooser jFileChooser1 = new JFileChooser();
        int response = jFileChooser1.showOpenDialog(null);
    
            if(response  == JFileChooser.APPROVE_OPTION){
                Path = jFileChooser1.getSelectedFile().getAbsolutePath();
            }
    }
   

    public void PrintResult()
    {
        String line;
        try 
        {
           while ((line = buff.readLine()) != null) 
           {
                Results.add(line);
                result += line + "\n";
                System.out.println(line);
            }
       } 
       catch (IOException e) { e.printStackTrace(); }

        try 
        {
           process.waitFor();
        } 
        catch (InterruptedException e) {e.printStackTrace();}
        ResultFrame.setLoading();  
    }
    
    public void createFile()
    {  

    if(this.Path.equals("null"))return;
    

        try (FileWriter writer = new FileWriter(this.Path)) 
        {
            for(String str: Results) 
            {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } 
        catch (IOException e) {e.printStackTrace();}
        
    }

    public void advance_createFile()  
    {
        if(this.Path.equals("null")) return;
        System.out.println("Inside createFile() " + this.Path);
        
        try (FileWriter writer = new FileWriter(this.Path)) 
        {
            writer.write(this.result + System.lineSeparator());
            writer.close();
        } 
        catch (IOException e) {e.printStackTrace();}

    }
    public void outputScreen()
    {
        result_Screen.setVisible(true);
        //loading call
        result_Screen.jTextArea1.setText(result);
        ResultFrame.jLabel3.setText(" ");  
    }

    public void pinging_Start(int index , String link)
    {
        ArrayList<String> pingingCommand = new ArrayList<>();
        pingingCommand.add("ping ");
        pingingCommand.add("ping -n 10 ");
        pingingCommand.add("ping -l 64 ");
        pingingCommand.add("ping -s 4 ");
        pingingCommand.add("ping -r 10 ");
        pingingCommand.add("ping ");
        pingingCommand.add("ping -i 10 ");

        cuurentCommand = pingingCommand.get(index) + link;
        
        this.process();
        this.outputScreen();
        this.createFile();
        
    }
    public void deets_scan(int index)
    {
        ArrayList<String> netStatCommand = new ArrayList<>();
        netStatCommand.add("netstat -a");
        netStatCommand.add("netstat -b");
        netStatCommand.add("netstat -e");
        netStatCommand.add("netstat -n");
        netStatCommand.add("netstat -o");
        netStatCommand.add("netstat -r");
        netStatCommand.add("netstat -v");
        
            cuurentCommand = netStatCommand.get(index);
            this.process();
            this.outputScreen();
            this.createFile();
    }

	public void all_ipOf_Host(String URL)
    {
        //loading call
        if(URL.contains("https://"))
        {
            URL = URL.substring(8);
        }
        System.out.println(URL);
        try {
            InetAddress[] myHost = InetAddress.getAllByName(URL);
            for (InetAddress inetAddress : myHost) {
                this.result += inetAddress.getHostAddress() + '\n';
                System.out.println(result);
                outputScreen();
            } 
        } catch (Exception e) {e.printStackTrace();}

        this.outputScreen();
        this.advance_createFile();
        
    }

    public void content_Of_WebPage(String URL) 
    {   
        System.out.println(URL);
        String line;
        URL url;
        InputStream urlStream;
        DataInputStream html;

        //loading call

        if(!URL.contains("https://"))
        {
            System.out.println("Not Contains https:// ");
            URL = "https://" + URL;
        }
        
        try 
        {
            url = new URL(URL);
            urlStream = url.openStream();
            html = new DataInputStream(urlStream);

            while ((line = html.readLine()) != null) 
            {
                this.result += line + "\n" ;
                System.out.println(line);
            }

        } catch (Exception e) {e.fillInStackTrace();}

        ResultFrame.jLabel3.setText("");
        this.outputScreen();
        this.advance_createFile();
    }

    public void port_scan(String URL) throws UnknownHostException   
    {
        
        //loading call
            InetAddress[] addresses = InetAddress.getAllByName(URL);

            for (InetAddress inetAddress : addresses) 
            {
                URL = inetAddress.toString();
            } 
        
        final String main_URL = URL.substring(URL.indexOf("/")+1);
        final String url_Head = URL;

        ConcurrentLinkedQueue openPorts = new ConcurrentLinkedQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        AtomicInteger port = new AtomicInteger(0);

        while (port.get() < 1000) 
        {
            final int currentPort = port.getAndIncrement();
            executorService.submit(() -> {
                try 
                {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(main_URL, currentPort), 200);
                    socket.close();
                    openPorts.add(currentPort);
                    System.out.println(url_Head + " ,port open: " + currentPort);
                    this.result += url_Head + " ,port open: " + currentPort + "\n";     
                }
                catch (IOException e) {}
            });
        }
        executorService.shutdown();

        try 
        {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {e.printStackTrace();}

        List openPortList = new ArrayList<>();
        System.out.println("openPortsQueue: " + openPorts.size());

        while (!openPorts.isEmpty()) 
        {
            openPortList.add(openPorts.poll());
        }
        
        this.result_Screen.jLabel3.setText("");
        this.advance_createFile();
        this.outputScreen();
    }
    public void http_header(String URL) {
        
        String urlString = URL;
        String username = "myname";
        String password = "mypassword";

        //loading call
        if(!URL.contains("http"))
        {
            URL = "http://" + URL;
        }
        
        Authenticator.setDefault(new MyAuthenticator(username, password));
     
        URL url = null;
        try 
        {
            url = new URL(urlString);

        }catch (MalformedURLException e) {e.printStackTrace();}

        InputStream content = null;

        try 
        {
            content = (InputStream) url.getContent();

        }catch (IOException e) {e.printStackTrace();}

        InputStreamReader isr = new InputStreamReader(content);
        BufferedReader in = new BufferedReader(isr);
     
        String line;
        try 
        {
            while ((line = in.readLine()) != null) 
            {
              System.out.println(line);
              this.result += line.toString() + "\n";
            }

        }catch (IOException e) {e.printStackTrace();}

        ResultFrame.jLabel3.setText("");
        outputScreen();
        createFile();
    }

    
class MyAuthenticator extends Authenticator 
{
    private String username, password;
 
    public MyAuthenticator(String username, String password) 
    {
      this.username = username;
      this.password = password;
    }
 
    protected PasswordAuthentication getPasswordAuthentication() {
      System.out.println("Requesting Host  : " + getRequestingHost());
      System.out.println("Requesting Port  : " + getRequestingPort());
      System.out.println("Requesting Prompt : " + getRequestingPrompt());
      System.out.println("Requesting Protocol: " + getRequestingProtocol());
      System.out.println("Requesting Scheme : " + getRequestingScheme());
      System.out.println("Requesting Site  : " + getRequestingSite());
      return new PasswordAuthentication(username, password.toCharArray());

    }
}
    public void Cookie (String str) throws IOException   
    {   
        final String IP = str;
        CookieManager cm = new CookieManager();
        CookieHandler.setDefault(cm);
        System.out.println("IP O :"+IP);
        URL url = new URL(IP);

        //loading call
        if(str.contains("http://") || str.contains("https://")){
           
        java.net.URLConnection con = url.openConnection();
        con.getContent();
   
        CookieStore cs = cm.getCookieStore();
   
        List<URI> uriList = cs.getURIs();
        for (URI uri : uriList) 
        {
           this.result += uri.getHost() + "\n";
           this.outputScreen();
        }
   
        List<java.net.HttpCookie> hcList = cs.getCookies();
        for (java.net.HttpCookie hc : hcList) 
        {
           System.out.println("Cookie: " + hc.getName());
           System.out.println("Value: " + hc.getValue());
           this.result += "Cookie: " + hc.getName() + " \n"+ "Value: " + hc.getValue() + "\n";
           outputScreen();
        }
        
        this.result_Screen.jLabel3.setText("");
        System.out.println("Cookies Funtion Finished File Path : "+this.Path);
        this.advance_createFile();

    }else{Cookie("https://"+str);}

    }

     public void W_R_Socket(int Port) 
     {
        new Server(6666).start();
        new Client(6666).start();
        this.createFile();
     }
    }
    
class Server extends Thread 
{
    Socket socket = null ;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    int Port;
    Core terminal = new Core();

    Server(int port)
    {
        this.Port  =  port;
    }
    public void run() 
    {
       try 
       {
          ServerSocket server = new ServerSocket(Port);
          while(true) 
          {
             socket = server.accept();
             ois = new ObjectInputStream(socket.getInputStream());
             String message = (String) ois.readObject();

             terminal.result = "Server Received: " + message + " \n";
             terminal.outputScreen();

             oos = new ObjectOutputStream(socket.getOutputStream());
             oos.writeObject("Server Reply");

             ois.close();
             oos.close();
             socket.close();
             server.close();
          }
       }catch (Exception e) {e.fillInStackTrace();}
    }
 }
  
 class Client extends Thread  
 {
    InetAddress host = null;
    Socket socket = null;
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    Core terminal = new Core();
    int Port;

    Client(int port)
    {
        this.Port = port;
    }
    public void run() 
    {
       try 
       {
          for(int x=0; x<5; x++)
           {
             host = InetAddress.getLocalHost();
             socket = new Socket(host.getHostName(), Port);

             oos = new ObjectOutputStream(socket.getOutputStream());
             oos.writeObject("Client Message " + x);
             ois = new ObjectInputStream(socket.getInputStream());

             String message = (String) ois.readObject();
             terminal.result = "Client Message " + message + " \n";
             terminal.outputScreen();

             ois.close();
             oos.close();
             socket.close();
          }
       } catch (Exception e) {e.fillInStackTrace();}
    }
 }